package com.elevare.active

import android.content.*
import android.media.*
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

// Local Turkish TTS only. No recordings, microphone, voice upload or speech backlog.
class CoachAudio(context:Context,private val interrupted:()->Unit):AutoCloseable{
    private val context=context.applicationContext
    private val handler=Handler(Looper.getMainLooper())
    private val manager=context.getSystemService(AudioManager::class.java)
    private var engine:TextToSpeech?=null
    private var tone:ToneGenerator?=null
    private var closed=false
    private var active=false
    private var voiceEnabled=false
    private var sound="off"
    private var pending:String?=null
    private var lastInstruction=""
    private var lastTick=""
    private var focusHeld=false
    private var utteranceNumber=0L
    private var currentUtterance=""
    var ready by mutableStateOf(false);private set
    var initializing by mutableStateOf(false);private set
    var status by mutableStateOf("");private set
    private val attributes=AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
    private val focusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        .setAudioAttributes(attributes).setOnAudioFocusChangeListener({change->
            if(active&&change in listOf(AudioManager.AUDIOFOCUS_LOSS,AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK)){
                stop();interrupted()
            }
        },handler).build()
    private val releaseFocus=Runnable{if(engine?.isSpeaking!=true)release()}
    private fun gain():Boolean{
        if(focusHeld)return true
        focusHeld=manager.requestAudioFocus(focusRequest)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        return focusHeld
    }
    private fun release(){
        if(focusHeld){focusHeld=false;manager.abandonAudioFocusRequest(focusRequest)}
    }
    private fun initVoice(){
        if(engine!=null||closed)return
        initializing=true
        status="Türkçe ses hazırlanıyor."
        engine=TextToSpeech(context){result->handler.post{
            if(closed)return@post
            initializing=false
            val tts=engine?:return@post
            if(result!=TextToSpeech.SUCCESS){
                status="Ses motoru açılamadı. Yazılı rehberle devam edebilirsin.";pending=null;return@post
            }
            val offlineVoice=runCatching{
                tts.voices?.filter{it.locale.language=="tr"&&!it.isNetworkConnectionRequired}?.maxByOrNull{it.quality}
            }.getOrNull()
            if(offlineVoice==null||runCatching{tts.setVoice(offlineVoice)}.getOrDefault(TextToSpeech.ERROR)==TextToSpeech.ERROR){
                status="Cihazında çevrimdışı Türkçe ses yok. Android metin okuma ayarlarından ses yükleyebilir veya sessiz devam edebilirsin."
                pending=null;return@post
            }
            tts.setAudioAttributes(attributes);tts.setSpeechRate(1f)
            tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
                override fun onStart(id:String?){}
                override fun onDone(id:String?){handler.post{
                    // A cancelled, older utterance must not release the newer instruction's focus.
                    if(id==currentUtterance){currentUtterance="";release()}
                }}
                @Deprecated("Platform callback")
                override fun onError(id:String?){handler.post{
                    if(id==currentUtterance){
                        currentUtterance="";status="Ses okunamadı. Yazılı rehber kullanılabilir.";release()
                    }
                }}
            })
            ready=true;status=""
            val latest=pending;pending=null
            if(active&&voiceEnabled&&latest!=null)speak(latest)
        }}
    }
    fun retryVoice(){
        pending=null;ready=false;engine?.stop();engine?.shutdown();engine=null
        currentUtterance="";lastInstruction="";release()
        if(voiceEnabled)initVoice()
    }
    fun configure(voice:Boolean,mode:String,running:Boolean){
        voiceEnabled=voice
        sound=if(mode in listOf("off","countdown","every_second"))mode else "off"
        if(!running){stop();return}
        active=true
        if(!voice){
            pending=null;lastInstruction="";currentUtterance="";engine?.stop();release()
        }else initVoice()
    }
    fun speak(text:String){
        if(closed||!active||!voiceEnabled||text.isBlank())return
        if(!ready){pending=text;return} // A single replaceable slot, never a queue.
        handler.removeCallbacks(releaseFocus);tone?.stopTone()
        if(!gain()){status="Ses başka bir uygulamada kullanımda. Yazılı rehber hazır.";return}
        currentUtterance="elevare-"+(++utteranceNumber)
        if(engine?.speak(text,TextToSpeech.QUEUE_FLUSH,null,currentUtterance)==TextToSpeech.ERROR){
            currentUtterance="";status="Ses okunamadı. Yazılı rehber hazır.";release()
        }
    }
    fun instruction(key:String,text:String){
        if(!active||!voiceEnabled||key==lastInstruction)return
        lastInstruction=key;speak(text)
    }
    fun tick(key:String,remaining:Int){
        if(closed||!active||key==lastTick||!shouldBeep(sound,remaining))return
        lastTick=key
        // Speech has priority. A missed metronome tick is not replayed late.
        if(engine?.isSpeaking==true||currentUtterance.isNotEmpty()||pending!=null)return
        if(!gain())return
        runCatching{
            if(tone==null)tone=ToneGenerator(AudioManager.STREAM_MUSIC,22)
            tone?.startTone(if(remaining==0)ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP,
                if(remaining==0)140 else 50)
        }
        handler.removeCallbacks(releaseFocus);handler.postDelayed(releaseFocus,180)
    }
    fun stop(){
        active=false;pending=null;lastInstruction="";currentUtterance=""
        engine?.stop();tone?.stopTone();handler.removeCallbacks(releaseFocus);release()
    }
    override fun close(){
        closed=true;stop();engine?.shutdown();engine=null;tone?.release();tone=null
    }
}
@Composable fun rememberSessionAudio(store:Store,a:ActiveSession,move:Move,left:Int,rest:Boolean):CoachAudio{
    val context=LocalContext.current
    val lifecycle=LocalLifecycleOwner.current.lifecycle
    var resumed by remember(lifecycle){mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))}
    val audio=remember(context){CoachAudio(context){store.pauseTimer()}}
    DisposableEffect(audio,lifecycle){
        val observer=LifecycleEventObserver{_,event->
            resumed=lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            if(event==Lifecycle.Event.ON_PAUSE){audio.stop();store.pauseTimer()}
        }
        val unplug=object:BroadcastReceiver(){
            override fun onReceive(c:Context?,i:Intent?){audio.stop();store.pauseTimer()}
        }
        lifecycle.addObserver(observer)
        ContextCompat.registerReceiver(context,unplug,IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
            ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose{
            lifecycle.removeObserver(observer);runCatching{context.unregisterReceiver(unplug)};audio.close()
        }
    }
    LaunchedEffect(a.id,a.step,a.running,store.state.voiceCoach,store.state.timerSound,resumed){
        audio.configure(store.state.voiceCoach,store.state.timerSound,
            a.running&&resumed)
        if(a.running&&left>0){
            val step=activeWorkout(a).steps.getOrNull(a.step)
            val text=step?.let{sessionCoachInstruction(it,move)}
                ?:if(rest)"Yavaşla ve yürüyüşe geç." else move.hint
            audio.instruction(a.id+":"+a.step,text)
        }
    }
    LaunchedEffect(a.id,a.step,a.running,left,store.state.timerSound){
        if(a.running)audio.tick(a.id+":"+a.step+":"+left,left)
    }
    return audio
}
