// Itadaki v2 — Playful organic, responsive web, FR only
const THEMES = {
  light: {
    pageBg: 'oklch(0.95 0.02 65)',
    bg: 'oklch(0.985 0.01 70)',
    bgAlt: 'oklch(0.96 0.018 65)',
    surface: 'oklch(1 0 0)',
    ink: 'oklch(0.22 0.025 40)',
    inkMuted: 'oklch(0.46 0.022 50)',
    inkFaint: 'oklch(0.63 0.015 55)',
    hairline: 'oklch(0.9 0.012 60)',
    accent: 'oklch(0.67 0.16 52)',
    accentDeep: 'oklch(0.55 0.17 45)',
    accentSoft: 'oklch(0.93 0.05 60)',
    matcha: 'oklch(0.6 0.1 130)',
    matchaSoft: 'oklch(0.93 0.04 130)',
    warn: 'oklch(0.68 0.14 65)',
    danger: 'oklch(0.55 0.18 25)',
    scroll: 'oklch(0.85 0.02 60)',
  },
  dark: {
    pageBg: 'oklch(0.14 0.015 55)',
    bg: 'oklch(0.18 0.018 55)',
    bgAlt: 'oklch(0.22 0.02 55)',
    surface: 'oklch(0.24 0.022 55)',
    ink: 'oklch(0.96 0.01 70)',
    inkMuted: 'oklch(0.72 0.015 60)',
    inkFaint: 'oklch(0.55 0.015 55)',
    hairline: 'oklch(0.3 0.02 55)',
    accent: 'oklch(0.74 0.16 55)',
    accentDeep: 'oklch(0.62 0.17 48)',
    accentSoft: 'oklch(0.3 0.06 55)',
    matcha: 'oklch(0.72 0.1 130)',
    matchaSoft: 'oklch(0.3 0.04 130)',
    warn: 'oklch(0.76 0.14 70)',
    danger: 'oklch(0.66 0.18 25)',
    scroll: 'oklch(0.3 0.02 55)',
  },
};

const MEALS = [
  { id:'m1', name:'Poulet teriyaki aux légumes', ing:['poulet','sauce teriyaki','brocoli','carotte','riz blanc','sésame'], portion:'moyen', kMin:480, kMax:620, conf:92, date:'2026-04-20T12:34:00', meal:'Déjeuner', seed:42 },
  { id:'m2', name:'Salade quinoa, avocat, pois chiches', ing:['quinoa','avocat','pois chiches','tomate','feta','citron'], portion:'moyen', kMin:420, kMax:510, conf:88, date:'2026-04-20T08:15:00', meal:'Petit-déj.', seed:7 },
  { id:'m3', name:'Ramen shoyu au porc chashu', ing:['nouilles ramen','bouillon shoyu','porc chashu','œuf mollet','pousses de bambou','oignon vert'], portion:'grand', kMin:720, kMax:880, conf:71, date:'2026-04-19T19:50:00', meal:'Dîner', seed:13 },
  { id:'m4', name:'Buddha bowl au tofu fumé', ing:['tofu fumé','riz complet','chou rouge','edamame','carotte','tahini'], portion:'moyen', kMin:540, kMax:650, conf:85, date:'2026-04-19T12:20:00', meal:'Déjeuner', seed:88 },
  { id:'m5', name:'Tartine avocat œuf poché', ing:['pain de seigle','avocat','œuf poché','piment d\'Espelette','roquette'], portion:'petit', kMin:320, kMax:410, conf:90, date:'2026-04-19T08:40:00', meal:'Petit-déj.', seed:99 },
  { id:'m6', name:'Pad thaï aux crevettes', ing:['nouilles de riz','crevettes','cacahuètes','soja','citron vert','ciboule'], portion:'moyen', kMin:590, kMax:720, conf:74, date:'2026-04-18T20:10:00', meal:'Dîner', seed:55 },
  { id:'m7', name:'Pizza margherita', ing:['pâte','tomate','mozzarella di bufala','basilic','huile d\'olive'], portion:'grand', kMin:820, kMax:980, conf:94, date:'2026-04-18T13:00:00', meal:'Déjeuner', seed:21 },
  { id:'m8', name:'Saumon grillé, pommes de terre vapeur', ing:['saumon','pomme de terre','haricots verts','citron','aneth'], portion:'moyen', kMin:520, kMax:640, conf:89, date:'2026-04-17T20:00:00', meal:'Dîner', seed:3 },
];

function seededRand(s){let x=s;return()=>{x=(x*9301+49297)%233280;return x/233280;};}

function dishGradient(seed){
  const r=seededRand(seed);
  const hues=[28,18,42,55,140,85,12,35,65];
  const pick=()=>hues[Math.floor(r()*hues.length)];
  return `
    radial-gradient(ellipse at ${20+r()*25}% ${25+r()*20}%, oklch(0.75 0.14 ${pick()}) 0%, transparent 30%),
    radial-gradient(ellipse at ${55+r()*25}% ${60+r()*25}%, oklch(0.72 0.13 ${pick()}) 0%, transparent 35%),
    radial-gradient(ellipse at ${70+r()*15}% ${25+r()*20}%, oklch(0.68 0.11 ${pick()}) 0%, transparent 28%),
    radial-gradient(ellipse at ${30+r()*15}% ${70+r()*15}%, oklch(0.7 0.1 ${pick()}) 0%, transparent 28%),
    radial-gradient(circle at 50% 50%, oklch(0.92 0.03 60) 0%, oklch(0.82 0.02 55) 80%)
  `;
}

function last7(meals){
  const out=[];
  // Utilise la date courante (pas hardcodée) pour que le graphe soit toujours correct
  const today=new Date();
  today.setHours(23,0,0,0);
  const labels=['D','L','M','M','J','V','S'];
  for(let i=6;i>=0;i--){
    const d=new Date(today); d.setDate(d.getDate()-i);
    const ds=d.toISOString().slice(0,10);
    const dayMeals=meals.filter(m=>m.date&&m.date.slice(0,10)===ds);
    const mid=dayMeals.reduce((s,m)=>s+(m.kMin+m.kMax)/2,0);
    out.push({date:d,label:labels[d.getDay()],dayNum:d.getDate(),calories:Math.round(mid),isToday:i===0});
  }
  return out;
}

window.THEMES=THEMES;
window.MEALS=MEALS;
window.dishGradient=dishGradient;
window.last7=last7;
