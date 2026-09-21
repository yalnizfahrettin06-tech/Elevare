// Lightweight design-contract checks. Does not simulate Android layout or gestures.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import vm from 'node:vm';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const source=name=>fs.readFileSync(path.join(root,'app/src/main/java/com/elevare/active',name+'.kt'),'utf8');
let count=0;
function check(condition,label){if(!condition)throw new Error(label);console.log('PASS: '+label);count++;}
function luminance(hex){const c=hex.match(/../g).map(v=>parseInt(v,16)/255).map(v=>v<=.04045?v/12.92:((v+.055)/1.055)**2.4);return .2126*c[0]+.7152*c[1]+.0722*c[2];}
function contrast(a,b){const x=luminance(a),y=luminance(b);return (Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
const theme=source('Theme'),arc=source('ArcDesign'),onboarding=source('OnboardingV5'),main=source('MainActivity');
for(const hex of ['151414','F28B74','F4EFE8','BDB3AB','4B403C'])check(theme.includes(hex),'Palette token '+hex);
for(const [fg,bg] of [['F4EFE8','151414'],['BDB3AB','2B2725'],['151414','F28B74'],['F28B74','151414']]){
 const ratio=contrast(fg,bg);check(ratio>=4.5,`Text contrast #${fg}/#${bg}: ${ratio.toFixed(2)}:1`);
}
check((arc.match(/val \w+=icon\(/g)||[]).length>=40,'Original line icon family');
check(arc.includes('listOf(1,15,29,50,78)'),'Chapter boundaries match existing cycle');
check(arc.includes('clearAndSetSemantics'),'Chapter/decorative semantics provided');
check(main.includes('Destination("Bugün",ArcIcons.Home)')&&main.includes('Destination("Rutinim",ArcIcons.Program)')&&main.includes('Destination("Rehber",ArcIcons.Book)'),'Three destinations use Arc icons');
check(main.includes('heightIn(min=56.dp)'),'Primary CTA minimum height');
check(onboarding.includes('Hikâyeme başla')&&onboarding.includes('İlk bölümün hazır.'),'Arc onboarding copy');
check(onboarding.includes('FlowRow')&&source('WorkoutUI').includes('FlowRow'),'Metadata supports line wrapping');
check(source('TrainingScreens').includes('val pose=motionFrame'),'Production geometry remains the motion source');
check(!theme.includes('101D2D')&&!theme.includes('155FBA'),'Previous blue theme removed');
check(fs.readFileSync(path.join(root,'app/build.gradle.kts'),'utf8').includes('versionName = "0.12.0"'),'0.12.0 version');
const preview=process.argv[2];
if(preview){
 const html=fs.readFileSync(path.join(preview,'motion-preview.html'),'utf8');
 const inline=html.match(/<script>\s*([\s\S]*?)<\/script>/);
 check(!!inline,'Preview has inline player script');
 new vm.Script(inline[1]);check(true,'Preview player JavaScript syntax');
 const data=JSON.parse(fs.readFileSync(path.join(preview,'motion-frames.json'),'utf8'));
 check(data.moves.length===16&&data.moves.every(m=>m.frames.length===120),'16 variants, 120 frames each');
 check(data.moves.every(m=>m.frames.every(f=>f.joints.every(p=>p.every(Number.isFinite)))),'All preview coordinates finite');
}
console.log(`${count} design checks passed. Not Android device/layout validation.`);
