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

// Short, local-only utterances; no recordings, microphone or account.
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
 var ready by mutableStateOf(false);private set
 var status by mutableStateOf("");private set
 private val attributes=AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build()
 private val focusRequest=AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).setAudioAttributes(attributes).setOnAudioFocusChangeListener({change->
  if(change==AudioManager.AUDIOFOCUS_LOSS||change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT||change==AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK){stop();interrupted()}
 },handler).build()
 private val releaseFocus=Runnable{release()}
 private fun gain():Boolean{
  if(focusHeld)return true
  focusHeld=manager.requestAudioFocus(focusRequest)==AudioManager.AUDIOFOCUS_REQUEST_GRANTED
  return focusHeld
 }
 private fun release(){if(focusHeld){manager.abandonAudioFocusRequest(focusRequest);focusHeld=false}}
 private fun initVoice(){
  if(engine!=null||closed)return
  status="Türkçe ses hazırlanıyor"
  engine=TextToSpeech(context){result->handler.post{
   if(closed)return@post
   val tts=engine?:return@post
   if(result!=TextToSpeech.SUCCESS){status="Ses motoru açılamadı; yazılı rehber hazır.";pending=null;return@post}
   val voice=tts.voices?.filter{it.locale.language=="tr"&&!it.isNetworkConnectionRequired}?.maxByOrNull{it.quality}
   if(voice==null||tts.setVoice(voice)==TextToSpeech.ERROR){status="Çevrimdışı Türkçe ses bulunamadı. Android metin okuma ayarlarını kontrol et.";pending=null;return@post}
   tts.setAudioAttributes(attributes);tts.setSpeechRate(.95f)
   tts.setOnUtteranceProgressListener(object:UtteranceProgressListener(){
    override fun onStart(id:String?){}
    override fun onDone(id:String?){handler.post{release()}}
    @Deprecated("Platform callback") override fun onError(id:String?){handler.post{status="Ses okunamadı; yazılı adımları izleyebilirsin.";release()}}
   })
   ready=true;status=""
   val text=pending;pending=null
   if(active&&voiceEnabled&&text!=null)speak(text)
  }}
 }
 fun configure(voice:Boolean,mode:String,running:Boolean){
  voiceEnabled=voice;sound=mode
  if(!running){stop();return}
  active=true
  if(!voice){pending=null;lastInstruction="";engine?.stop();release()}else initVoice()
 }
 fun speak(text:String){
  if(closed||!active||!voiceEnabled)return
  if(!ready){pending=text;return}
  handler.removeCallbacks(releaseFocus)
  if(!gain()){status="Ses başka bir uygulamada kullanımda.";return}
  if(engine?.speak(text,TextToSpeech.QUEUE_FLUSH,null,"elevare-coach")==TextToSpeech.ERROR){status="Ses okunamadı.";release()}
 }
 fun instruction(key:String,text:String){if(!active||!voiceEnabled||key==lastInstruction)return;lastInstruction=key;speak(text)}
 fun tick(key:String,remaining:Int){
  if(closed||!active||key==lastTick||!shouldBeep(sound,remaining))return
  lastTick=key
  // Speech takes precedence over the metronome.
  if(engine?.isSpeaking==true)return
  if(!gain())return
  runCatching{if(tone==null)tone=ToneGenerator(AudioManager.STREAM_MUSIC,25);tone?.startTone(if(remaining==0)ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_BEEP,if(remaining==0)160 else 55)}
  handler.removeCallbacks(releaseFocus);handler.postDelayed(releaseFocus,200)
 }
 fun stop(){active=false;pending=null;engine?.stop();tone?.stopTone();handler.removeCallbacks(releaseFocus);release()}
 override fun close(){closed=true;stop();engine?.shutdown();engine=null;tone?.release();tone=null}
}
fun shouldBeep(mode:String,remaining:Int)=remaining>=0&&(mode=="every_second"||(mode=="countdown"&&remaining<=3))

@Composable fun rememberSessionAudio(store:Store,a:ActiveSession,move:Move,left:Int,rest:Boolean):CoachAudio{
 val context=LocalContext.current
 val lifecycle=LocalLifecycleOwner.current.lifecycle
 val audio=remember(context){CoachAudio(context){store.pauseTimer()}}
 DisposableEffect(audio,lifecycle){
  val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_PAUSE){audio.stop();store.pauseTimer()}}
  val unplug=object:BroadcastReceiver(){override fun onReceive(c:Context?,i:Intent?){audio.stop();store.pauseTimer()}}
  lifecycle.addObserver(observer)
  ContextCompat.registerReceiver(context,unplug,IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),ContextCompat.RECEIVER_NOT_EXPORTED)
  onDispose{lifecycle.removeObserver(observer);runCatching{context.unregisterReceiver(unplug)};audio.close()}
 }
 LaunchedEffect(a.id,a.step,a.running,store.state.voiceCoach,store.state.timerSound){
  audio.configure(store.state.voiceCoach,store.state.timerSound,a.running&&lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
  if(a.running&&left>0)audio.instruction(a.id+":"+a.step,if(rest)"Dinlenme. Kendi hızında rahatla." else move.title+". "+move.hint)
 }
 LaunchedEffect(a.id,a.step,a.running,left,store.state.timerSound){if(a.running)audio.tick(a.id+":"+a.step+":"+left,left)}
 return audio
}
