package com.elevare.active

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.util.Locale

/**
 * Visual QA only: exports the real production motionFrame output.
 * No second implementation of inverse kinematics or gait exists in this exporter/browser.
 * Usage: MotionPreviewKt <new-output-directory>. Refuses to overwrite an existing directory.
 */
private data class PreviewMove(val id:String,val title:String,val side:String="")
private val previewMoves=listOf(
    PreviewMove("sprint","Kontrollü koşu"),
    PreviewMove("walk","Rahat yürüyüş"),
    PreviewMove("catcow","Kedi–inek"),
    PreviewMove("child","Çocuk pozu"),
    PreviewMove("lunge","Alçak hamle · sol","left"),
    PreviewMove("squat","Sandalyeye otur–kalk"),
    PreviewMove("march","Yerinde yürüyüş"),
    PreviewMove("wall","Duvar şınavı"),
    PreviewMove("calf","Topuk yükseltme"),
    PreviewMove("balance","Destekli denge · sol","left"),
    PreviewMove("step","Yana adım"),
    PreviewMove("reach","Yukarı uzanma"),
    PreviewMove("shoulder","Omuz çevirme"),
    PreviewMove("breath","Rahat nefes · sabit figür"),
    PreviewMove("lunge","Alçak hamle · sağ","right"),
    PreviewMove("balance","Destekli denge · sağ","right")
)
private fun number(value:Float)=String.format(Locale.US,"%.3f",value)
private fun xml(text:String)=text.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;")
private fun json(text:String)="\""+text.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("<","\\u003c")+"\""
private fun sample(move:PreviewMove,phase:Float):StickFrame = motionFrame(
    move.id,phase,if(move.id in setOf("child","lunge","balance","reach"))phase else null,move.side
)
private fun frameJson(f:StickFrame):String =
    "{\"joints\":["+f.joints.joinToString(","){"["+number(it.x)+","+number(it.y)+"]"}+
    "],\"support\":"+json(f.support)+",\"frontContact\":"+f.frontContact+
    ",\"rearContact\":"+f.rearContact+",\"groundOffset\":"+number(f.groundOffset)+
    ",\"spineCurve\":"+number(f.spineCurve)+",\"stage\":"+json(f.stage)+"}"

private fun frameSvg(move:PreviewMove,f:StickFrame):String=buildString{
    fun line(x1:Float,y1:Float,x2:Float,y2:Float,color:String="#f4efe8",width:Float=3.2f,opacity:Float=1f){
        append("<line x1=\"").append(number(x1)).append("\" y1=\"").append(number(y1))
            .append("\" x2=\"").append(number(x2)).append("\" y2=\"").append(number(y2))
            .append("\" stroke=\"").append(color).append("\" stroke-width=\"").append(number(width))
            .append("\" stroke-linecap=\"round\" opacity=\"").append(number(opacity)).append("\"/>")
    }
    fun bone(a:Int,b:Int,color:String="#f4efe8",width:Float=3.2f,opacity:Float=1f){
        line(f.joints[a].x,f.joints[a].y,f.joints[b].x,f.joints[b].y,color,width,opacity)
    }
    line(7f,94f,96f,94f,width=1f,opacity=.22f)
    if(move.id in setOf("sprint","walk")){
        val period=if(move.id=="sprint")24.375f else 21.774193f
        for(i in -1..5){
            val x=i*period-f.groundOffset%period
            if(x in -12f..100f)line(x.coerceAtLeast(0f),101f,(x+9f).coerceIn(0f,100f),101f,"#e8b9a5",1f,.22f)
        }
    }
    when(f.support){
        "wall"->line(84f,8f,84f,97f,"#e8b9a5",2f,.55f)
        "rail"->{
            val x=if(move.side=="right")24f else 76f
            line(x,28f,x,97f,"#e8b9a5",2f,.5f);line(x-6f,28f,x+6f,28f,"#e8b9a5",2f,.5f)
        }
        "chair"->{
            line(48f,72f,82f,72f,"#e8b9a5",2f,.5f);line(81f,49f,81f,94f,"#e8b9a5",2f,.5f)
            line(51f,72f,51f,94f,"#e8b9a5",2f,.5f)
        }
        "mat"->append("<rect x=\"7\" y=\"94.5\" width=\"86\" height=\"4\" rx=\"2\" fill=\"#e8b9a5\" opacity=\".12\"/>")
    }
    val rear=listOf(1 to 5,5 to 6,2 to 9,9 to 10)
    rear.forEach{bone(it.first,it.second,opacity=.48f)}
    fun foot(index:Int,color:String,opacity:Float=1f){
        val p=f.joints[index]
        if(move.id=="calf")line(p.x,p.y,p.x+4.5f,94f,color,3.2f,opacity)
        else line(p.x,p.y,p.x+(if(move.side=="right")-4f else 4f),p.y,color,3.2f,opacity)
    }
    foot(10,"#f4efe8",.48f)
    if(move.id=="catcow"){
        val n=f.joints[1];val h=f.joints[2]
        append("<path d=\"M").append(number(n.x)).append(" ").append(number(n.y))
            .append(" C").append(number(n.x-10)).append(" ").append(number(n.y+f.spineCurve))
            .append(" ").append(number(h.x+10)).append(" ").append(number(h.y+f.spineCurve))
            .append(" ").append(number(h.x)).append(" ").append(number(h.y))
            .append("\" fill=\"none\" stroke=\"#f4efe8\" stroke-width=\"3.7\" stroke-linecap=\"round\"/>")
    }else bone(1,2,width=3.7f)
    val h=f.joints[0];val n=f.joints[1]
    val distance=kotlin.math.hypot(n.x-h.x,n.y-h.y).coerceAtLeast(.001f)
    line(h.x+(n.x-h.x)/distance*6.5f,h.y+(n.y-h.y)/distance*6.5f,n.x,n.y)
    append("<circle cx=\"").append(number(h.x)).append("\" cy=\"").append(number(h.y))
        .append("\" r=\"6.5\" fill=\"none\" stroke=\"#f4efe8\" stroke-width=\"2.8\"/>")
    val arcStart=215.0*Math.PI/180;val arcEnd=290.0*Math.PI/180
    append("<path d=\"M${number(h.x+6.5f*kotlin.math.cos(arcStart).toFloat())} ${number(h.y+6.5f*kotlin.math.sin(arcStart).toFloat())} A6.5 6.5 0 0 1 ${number(h.x+6.5f*kotlin.math.cos(arcEnd).toFloat())} ${number(h.y+6.5f*kotlin.math.sin(arcEnd).toFloat())}\" fill=\"none\" stroke=\"#f28b74\" stroke-width=\"2.8\" stroke-linecap=\"round\"/>")
    val accent=if(move.id=="sprint")"#f28b74" else "#e8b9a5"
    listOf(1 to 3,3 to 4,2 to 7,7 to 8).forEach{bone(it.first,it.second,accent)}
    foot(8,accent)
    listOf(1,2).forEach{index->val joint=f.joints[index];append("<circle cx=\"${number(joint.x)}\" cy=\"${number(joint.y)}\" r=\"2\" fill=\"#f4efe8\"/>")}
}
private fun contactSheet():String=buildString{
    append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"1480\" height=\"1410\" viewBox=\"0 0 1480 1410\">")
    append("<rect width=\"1480\" height=\"1410\" fill=\"#151414\"/>")
    append("<g font-family=\"Segoe UI,Arial,sans-serif\" fill=\"#f4efe8\">")
    append("<text x=\"26\" y=\"40\" font-size=\"26\" font-weight=\"700\">elevare / hareket geometrisi</text>")
    append("<text x=\"26\" y=\"68\" font-size=\"15\" fill=\"#bdb3ab\">Doğrudan üretim kodundan alınan pozlar · Tasarım QA çıktısı · Uzman onayı değildir</text>")
    previewMoves.take(6).forEachIndexed{row,move->
        val y=96+row*212
        append("<text x=\"26\" y=\"").append(y).append("\" font-size=\"18\" font-weight=\"600\">").append(xml(move.title)).append("</text>")
        val phases=if(move.id in setOf("child","lunge"))listOf(0f,.1f,.2f,.4f,.65f,.84f,.93f,1f) else (0..7).map{it/8f}
        phases.forEachIndexed{column,phase->
            val x=26+column*180
            append("<rect x=\"").append(x).append("\" y=\"").append(y+12).append("\" width=\"168\" height=\"166\" rx=\"14\" fill=\"#211d1c\"/>")
            append("<svg x=\"").append(x+10).append("\" y=\"").append(y+18).append("\" width=\"148\" height=\"136\" viewBox=\"-6 -3 112 112\">")
                .append(frameSvg(move,sample(move,phase))).append("</svg>")
            append("<text x=\"").append(x+84).append("\" y=\"").append(y+164).append("\" text-anchor=\"middle\" font-size=\"12\" fill=\"#bdb3ab\">")
                .append((phase*100).toInt()).append("%").append("</text>")
        }
    }
    append("<text x=\"26\" y=\"1390\" font-size=\"13\" fill=\"#bdb3ab\">Kaynak: app/src/main/java/com/elevare/active/StickGeometry.kt · JSON ve oynatılabilir HTML aynı örneklenmiş eklem verisini kullanır.</text>")
    append("</g></svg>")
}

private val htmlTemplate="""
<!doctype html>
<html lang="tr">
<head>
<meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Elevare · Training Arc hareket incelemesi</title>
<style>
:root{color-scheme:dark;--bg:#151414;--card:#211d1c;--text:#f4efe8;--muted:#bdb3ab;--blue:#e8b9a5;--coral:#f28b74}
*{box-sizing:border-box}body{margin:0;background:var(--bg);color:var(--text);font:16px/1.5 system-ui,sans-serif}
main{max-width:1060px;margin:auto;padding:28px 20px}header{margin-bottom:24px}.brand{font-size:25px;font-weight:800}
h1{font-size:clamp(25px,4vw,36px);line-height:1.15;margin:12px 0}.quiet{color:var(--muted);font-size:14px}
.layout{display:grid;grid-template-columns:1.25fr 1fr;gap:20px}.stage,.controls{background:var(--card);border:1px solid #4b403c;border-radius:22px;padding:20px}
svg{display:block;width:100%;height:350px}.badge{font-size:12px;letter-spacing:.08em;color:var(--coral);font-weight:700}
label{display:block;margin:16px 0 7px;font-weight:600}select,button{font:inherit;border-radius:12px;border:1px solid #94867e;padding:12px;background:#302623;color:var(--text);min-height:48px}
select{width:100%}button{cursor:pointer;font-weight:700}button.primary{background:#49342f;border-color:#49342f}
button:focus-visible,select:focus-visible,input:focus-visible{outline:3px solid var(--coral);outline-offset:3px}
.buttons{display:flex;gap:10px;margin-top:16px}.buttons>*{flex:1}input[type=range]{width:100%;accent-color:var(--coral);min-height:44px}
small{color:var(--muted)}.meta{display:flex;justify-content:space-between;color:var(--muted);font-size:13px}
#stage-label{min-height:48px;font-size:17px;font-weight:600;margin:12px 0 0}
footer{margin-top:24px;padding-top:18px;border-top:1px solid #4b403c}.note{font-size:13px;color:var(--muted)}
@media(max-width:720px){.layout{grid-template-columns:1fr}main{padding:20px 16px}svg{height:290px}.controls{padding-top:6px}}
@media(prefers-reduced-motion:reduce){*{scroll-behavior:auto}}
</style>
</head>
<body><main>
<header><div class="brand">elevare / Training Arc</div><h1>Çizgi atlet. Aynı hareket, yeni karakter.</h1>
<p class="quiet">Bu görüntüler uygulamadaki gerçek çizgi adam kodundan üretildi. Antrenman veya tıbbi doğrulama değildir.</p></header>
<div class="layout">
<section class="stage" aria-label="Hareket önizlemesi">
<div class="badge">ÜRETİM GEOMETRİSİ / 2D ÇİZGİ REHBER</div>
<svg id="figure" viewBox="-6 -3 112 112" role="img" aria-label="Kontrollü koşu çizimi"></svg>
<p id="stage-label">Kontrollü koşu</p><div class="meta"><span id="source-id">sprint</span><span id="frame-readout">1 / 120</span></div>
</section>
<section class="controls" aria-label="İnceleme kontrolleri">
<label for="move">Hareket</label><select id="move"></select>
<label for="speed">Gösterim hızı</label><select id="speed"><option value="1">Normal</option><option value=".5">Yavaş · 0,5×</option><option value=".25">Ayrıntı · 0,25×</option></select>
<div class="buttons"><button id="play" class="primary" aria-pressed="true">Duraklat</button><button id="restart">Başa dön</button></div>
<label for="frame">Pozu elle incele</label><input id="frame" type="range" min="0" max="119" value="0" aria-valuetext="1. poz">
<p class="quiet" id="hint">Kareler, motionFrame fonksiyonundan örneklendi.</p>
<p class="note">Sağ/sol uzuv rengi yön bulmak içindir. Figürün hızını kopyalamak gerekmez. Görüntüde takılma, yanlış temas veya anlaşılmayan pozlar ayrıca değerlendirilmelidir.</p>
</section></div>
<footer><div class="badge">GÖRSEL KABUL KAPISI</div>
<p class="note">Koşu ve yürüyüş ayrımı, ayak teması, uzuv oranları, yoga tutuşları ve döngü birleşimleri için hazırlanmıştır. Cihaz performansı, ses eşleşmesi ve hareket uzmanı incelemesinin yerine geçmez. Çevrimdışı çalışır; hesap, ağ isteği veya veri toplama yok.</p></footer>
<script type="application/json" id="motion-data">__MOTION_DATA__</script>
<script>
(function(){
'use strict';
const data=JSON.parse(document.getElementById('motion-data').textContent);
const select=document.getElementById('move'),speed=document.getElementById('speed'),slider=document.getElementById('frame');
const svg=document.getElementById('figure'),label=document.getElementById('stage-label'),play=document.getElementById('play');
let index=0,phase=0,playing=!matchMedia('(prefers-reduced-motion: reduce)').matches,previous=null,scheduled=null;
data.moves.forEach(function(m,i){const o=document.createElement('option');o.value=i;o.textContent=m.title;select.appendChild(o)});
function line(x1,y1,x2,y2,color,width,opacity){return '<line x1="'+x1+'" y1="'+y1+'" x2="'+x2+'" y2="'+y2+'" stroke="'+(color||'#f4efe8')+'" stroke-width="'+(width||3.2)+'" stroke-linecap="round" opacity="'+(opacity===undefined?1:opacity)+'"/>'}
function draw(){
const m=data.moves[index],n=Math.min(m.frames.length-1,Math.floor(phase*m.frames.length)),f=m.frames[n],p=f.joints;
const parts=[line(7,94,96,94,'#f4efe8',1,.22)];
function bone(a,b,color,width,opacity){parts.push(line(p[a][0],p[a][1],p[b][0],p[b][1],color,width,opacity))}
function foot(i,color,opacity){parts.push(line(p[i][0],p[i][1],p[i][0]+(m.id==='calf'?4.5:m.side==='right'?-4:4),m.id==='calf'?94:p[i][1],color,3.2,opacity))}
if(m.id==='sprint'||m.id==='walk'){
const period=m.id==='sprint'?24.375:21.774193;
for(let i=-1;i<=5;i++){const x=i*period-f.groundOffset%period;if(x>=-12&&x<=100)parts.push(line(Math.max(0,x),101,Math.max(0,Math.min(100,x+9)),101,'#e8b9a5',1,.22))}
}
if(f.support==='wall')parts.push(line(84,8,84,97,'#e8b9a5',2,.55));
if(f.support==='rail'){const x=m.side==='right'?24:76;parts.push(line(x,28,x,97,'#e8b9a5',2,.5),line(x-6,28,x+6,28,'#e8b9a5',2,.5))}
if(f.support==='chair')parts.push(line(48,72,82,72,'#e8b9a5',2,.5),line(81,49,81,94,'#e8b9a5',2,.5),line(51,72,51,94,'#e8b9a5',2,.5));
if(f.support==='mat')parts.push('<rect x="7" y="94.5" width="86" height="4" rx="2" fill="#e8b9a5" opacity=".12"/>');
[[1,5],[5,6],[2,9],[9,10]].forEach(function(b){bone(b[0],b[1],'#f4efe8',3.2,.48)});foot(10,'#f4efe8',.48);
if(m.id==='catcow')parts.push('<path d="M'+p[1][0]+' '+p[1][1]+' C'+(p[1][0]-10)+' '+(p[1][1]+f.spineCurve)+' '+(p[2][0]+10)+' '+(p[2][1]+f.spineCurve)+' '+p[2][0]+' '+p[2][1]+'" fill="none" stroke="#f4efe8" stroke-width="3.7" stroke-linecap="round"/>');else bone(1,2,'#f4efe8',3.7);
const dx=p[1][0]-p[0][0],dy=p[1][1]-p[0][1],distance=Math.max(.001,Math.hypot(dx,dy));
parts.push(line(p[0][0]+dx/distance*6.5,p[0][1]+dy/distance*6.5,p[1][0],p[1][1]));
parts.push('<circle cx="'+p[0][0]+'" cy="'+p[0][1]+'" r="6.5" fill="none" stroke="#f4efe8" stroke-width="2.8"/>');
const a=215*Math.PI/180,b=290*Math.PI/180;
parts.push('<path d="M'+(p[0][0]+6.5*Math.cos(a))+' '+(p[0][1]+6.5*Math.sin(a))+' A6.5 6.5 0 0 1 '+(p[0][0]+6.5*Math.cos(b))+' '+(p[0][1]+6.5*Math.sin(b))+'" fill="none" stroke="#f28b74" stroke-width="2.8" stroke-linecap="round"/>');
const accent=m.id==='sprint'?'#f28b74':'#e8b9a5';
[[1,3],[3,4],[2,7],[7,8]].forEach(function(b){bone(b[0],b[1],accent)});foot(8,accent);
svg.innerHTML=parts.join('');svg.setAttribute('aria-label',m.title+' çizimi. '+(playing?'Oynatılıyor.':'Duraklatıldı.'));
label.textContent=f.stage||m.title;document.getElementById('source-id').textContent=m.id+(m.side?' / '+m.side:'');
document.getElementById('frame-readout').textContent=(n+1)+' / '+m.frames.length;
slider.value=n;slider.max=m.frames.length-1;slider.setAttribute('aria-valuetext',(n+1)+'. poz');
document.getElementById('hint').textContent=m.cue;
}
function button(){play.textContent=playing?'Duraklat':'Oynat';play.setAttribute('aria-pressed',String(playing));if(playing&&!document.hidden&&scheduled===null)scheduled=requestAnimationFrame(loop);else if(!playing&&scheduled!==null){cancelAnimationFrame(scheduled);scheduled=null;previous=null}}
function loop(now){scheduled=null;if(previous!==null&&playing&&!document.hidden){phase=(phase+(now-previous)*Number(speed.value)/data.moves[index].durationMs)%1;draw()}previous=now;if(playing&&!document.hidden)scheduled=requestAnimationFrame(loop)}
select.addEventListener('change',function(){index=Number(select.value);phase=0;previous=null;draw()});
play.addEventListener('click',function(){playing=!playing;previous=null;button();draw()});
document.getElementById('restart').addEventListener('click',function(){phase=0;previous=null;draw()});
slider.addEventListener('input',function(){playing=false;phase=Number(slider.value)/data.moves[index].frames.length;button();draw()});
document.addEventListener('visibilitychange',function(){if(document.hidden){playing=false;button()}previous=null});
button();draw();
})();
</script></body></html>
""".trimIndent()

fun main(args:Array<String>){
    require(args.size==1){"Pass one NEW output directory."}
    val directory:Path=Paths.get(args[0]).toAbsolutePath().normalize()
    require(!Files.exists(directory)){"Refusing to overwrite existing output directory: "+directory}
    Files.createDirectories(directory)
    val count=120
    val data="{\"generatedAt\":"+json(Instant.now().toString())+
        ",\"source\":\"app/src/main/java/com/elevare/active/StickGeometry.kt\",\"sampleCount\":"+count+
        ",\"moves\":["+previewMoves.joinToString(","){move->
            "{\"id\":"+json(move.id)+",\"title\":"+json(move.title)+",\"side\":"+json(move.side)+
                ",\"cue\":"+json(motionCue(move.id))+",\"durationMs\":"+motionDuration(move.id)+
                ",\"frames\":["+(0 until count).joinToString(","){i->frameJson(sample(move,i.toFloat()/count))}+"]}"
        }+"]}"
    Files.writeString(directory.resolve("motion-frames.json"),data,StandardCharsets.UTF_8)
    Files.writeString(directory.resolve("motion-contact-sheet.svg"),contactSheet(),StandardCharsets.UTF_8)
    Files.writeString(directory.resolve("motion-preview.html"),htmlTemplate.replace("__MOTION_DATA__",data),StandardCharsets.UTF_8)
    println("Exported production geometry: "+previewMoves.size+" movement/side variants x "+count+" frames.")
    println(directory.resolve("motion-contact-sheet.svg"))
    println(directory.resolve("motion-preview.html"))
    println(directory.resolve("motion-frames.json"))
}
