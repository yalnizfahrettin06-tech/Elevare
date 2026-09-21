// Local editorial inventory; does not validate scientific claims or invent reviewers.
import fs from 'node:fs';
const data=JSON.parse(fs.readFileSync(new URL('../app/src/main/assets/science-cards.json',import.meta.url),'utf8'));
const today=new Date().toISOString().slice(0,10);
console.log('# Editorial inventory — '+today+'\n');
console.log('Automated metadata inventory only. Independent expert approval is a separate gate.\n');
console.log('| Card | Source | Ages | Status | Reviewer | Review due |\n|---|---|---|---|---|---|');
for(const f of data.facts){
 if(!data.sources.some(s=>s.id===f.sourceId))throw Error('Missing source: '+f.id);
 console.log('| '+[f.id,f.sourceId,f.minAge+'–'+f.maxAge,f.status||'legacy_source_summary',f.reviewer||'NOT REVIEWED',f.nextReviewAt||'UNASSIGNED'].join(' | ')+' |');
}
const reviewed=data.facts.filter(f=>f.reviewer&&f.reviewedAt&&f.nextReviewAt>=today&&['published','publishable'].includes(f.status));
console.log('\nReview records currently valid: '+reviewed.length+' / '+data.facts.length);

