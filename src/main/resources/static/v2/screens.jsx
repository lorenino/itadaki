// Screens — Auth, Dashboard, Upload, Correction, History, Profile — responsive

// Score santé A→E (heuristique calories + macros)
// Définie ici car screens.jsx est chargé avant app.jsx ; app.jsx expose aussi window.healthScore
function healthScore(m){
  const kcal=(m.kMin+m.kMax)/2;
  const items=(m.analysisRaw&&m.analysisRaw.detectedItems)||[];
  const prot=items.reduce((s,i)=>s+(i.protein||0),0);
  const carbs=items.reduce((s,i)=>s+(i.carbs||0),0);
  const fat=items.reduce((s,i)=>s+(i.fat||0),0);
  let pts=0;
  if(kcal<700)pts+=2;else if(kcal<900)pts+=1;
  if(prot>=20)pts+=2;else if(prot>=15)pts+=1;
  if(fat<20)pts+=1;
  if(carbs>40&&carbs<80)pts+=1;
  const grade=['E','D','C','B','A'][Math.min(4,pts)];
  const color={A:'#4a8a3c',B:'#7fa644',C:'#d4a13c',D:'#d47a2f',E:'#c14a2f'}[grade];
  return{grade,color};
}

function Auth({T,onAuth,mobile}){
  const [mode,setMode]=useState('signup');
  const [em,setEm]=useState(''),[un,setUn]=useState(''),[pw,setPw]=useState('');
  const [err,setErr]=useState({}),[ld,setLd]=useState(false);
  const go=()=>{const e={};if(!em||!em.includes('@'))e.em='Email invalide';if(mode==='signup'&&(!un||un.length<3))e.un='Au moins 3 caractères';if(!pw||pw.length<6)e.pw='6 caractères minimum';setErr(e);if(Object.keys(e).length)return;setLd(true);setTimeout(()=>{setLd(false);onAuth({email:em,username:un||em.split('@')[0]});},700);};

  const formW=mobile?'100%':420;
  return <div style={{minHeight:'100%',display:'flex',flexDirection:mobile?'column':'row',background:T.bg}}>
    {/* Left hero — food collage */}
    <div style={{flex:mobile?'0 0 auto':'1 1 50%',position:'relative',minHeight:mobile?260:'100%',background:T.bgAlt,overflow:'hidden'}}>
      <svg viewBox="0 0 600 800" preserveAspectRatio="xMidYMid slice" style={{position:'absolute',inset:0,width:'100%',height:'100%'}}>
        <path d="M-100,200 Q150,50 300,250 Q500,430 700,200 L700,-100 L-100,-100 Z" fill={T.accentSoft}/>
        <path d="M-100,600 Q150,500 350,650 Q550,800 700,600 L700,900 L-100,900 Z" fill={T.matchaSoft} opacity="0.8"/>
      </svg>
      <div style={{position:'absolute',top:'18%',left:'8%',width:mobile?140:180,aspectRatio:'1/1',transform:'rotate(-6deg)'}}>
        <Dish seed={42} style={{width:'100%',height:'100%',borderRadius:'50%',boxShadow:'0 24px 50px -10px rgba(80,40,10,.3)'}}/>
      </div>
      <div style={{position:'absolute',top:mobile?'10%':'40%',right:mobile?'6%':'12%',width:mobile?120:200,aspectRatio:'1/1',transform:'rotate(9deg)'}}>
        <Dish seed={13} style={{width:'100%',height:'100%',borderRadius:'50%',boxShadow:'0 24px 50px -10px rgba(80,40,10,.25)'}}/>
      </div>
      {!mobile&&<div style={{position:'absolute',bottom:'12%',left:'22%',width:160,aspectRatio:'1/1',transform:'rotate(3deg)'}}>
        <Dish seed={7} style={{width:'100%',height:'100%',borderRadius:'50%',boxShadow:'0 24px 50px -10px rgba(80,40,10,.25)'}}/>
      </div>}
      <div style={{position:'absolute',top:mobile?20:32,left:mobile?20:40,display:'flex',alignItems:'center',gap:10}}>
        <div style={{width:36,height:36,borderRadius:'14px 14px 14px 6px',background:T.accent,color:'#fff',display:'flex',alignItems:'center',justifyContent:'center',fontFamily:'"Fraunces",serif',fontSize:22,fontStyle:'italic',fontWeight:500,transform:'rotate(-4deg)'}}>i</div>
        <div style={{fontFamily:'"Fraunces",serif',fontSize:24,color:T.ink,letterSpacing:'-.03em',fontWeight:500}}>Itadaki</div>
      </div>
      {!mobile&&<div style={{position:'absolute',bottom:32,left:40,right:40,fontFamily:'"Fraunces",serif',fontSize:44,color:T.ink,letterSpacing:'-.03em',lineHeight:1.05,fontWeight:500}}>
        Photographiez,<br/><span style={{fontStyle:'italic',color:T.accent}}>mangez mieux.</span>
      </div>}
    </div>

    {/* Right form */}
    <div style={{flex:mobile?'1 1 auto':'1 1 50%',display:'flex',alignItems:'center',justifyContent:'center',padding:mobile?'26px 22px 32px':'48px 40px'}}>
      <div style={{width:formW,maxWidth:'100%'}}>
        {mobile&&<div style={{fontFamily:'"Fraunces",serif',fontSize:30,color:T.ink,letterSpacing:'-.02em',lineHeight:1.08,fontWeight:500,marginBottom:16}}>
          Photographiez,<br/><span style={{fontStyle:'italic',color:T.accent}}>mangez mieux.</span>
        </div>}
        <div style={{fontFamily:'Inter,system-ui',fontSize:13,color:T.inkMuted,marginBottom:22,lineHeight:1.5}}>
          Une photo de votre assiette — l'IA identifie le plat, les ingrédients et estime les calories en quelques secondes.
        </div>
        <div style={{display:'flex',padding:4,background:T.bgAlt,borderRadius:999,marginBottom:22}}>
          {[{id:'signup',l:'S\'inscrire'},{id:'signin',l:'Se connecter'}].map(t=>
            <button key={t.id} onClick={()=>{setMode(t.id);setErr({});}} style={{flex:1,padding:'10px 16px',border:'none',background:mode===t.id?T.surface:'transparent',borderRadius:999,fontFamily:'Inter,system-ui',fontSize:13,fontWeight:600,color:mode===t.id?T.ink:T.inkMuted,cursor:'pointer',boxShadow:mode===t.id?'0 1px 3px rgba(0,0,0,.06)':'none'}}>{t.l}</button>)}
        </div>
        <Field T={T} label="Email" value={em} onChange={setEm} type="email" placeholder="vous@exemple.fr" error={err.em}
          icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="2" y="4" width="12" height="9" rx="1.5" stroke="currentColor" strokeWidth="1.4"/><path d="M2.5 5l5.5 4 5.5-4" stroke="currentColor" strokeWidth="1.4"/></svg>}/>
        {mode==='signup'&&<Field T={T} label="Nom d'utilisateur" value={un} onChange={setUn} placeholder="kenji_42" error={err.un}
          icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="6" r="2.6" stroke="currentColor" strokeWidth="1.4"/><path d="M3 14c.7-2.5 2.8-3.5 5-3.5s4.3 1 5 3.5" stroke="currentColor" strokeWidth="1.4"/></svg>}/>}
        <Field T={T} label="Mot de passe" value={pw} onChange={setPw} type="password" placeholder="••••••••" error={err.pw} hint={mode==='signup'?'6 caractères minimum':undefined}
          icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.4"/><path d="M5 7V5a3 3 0 016 0v2" stroke="currentColor" strokeWidth="1.4"/></svg>}/>
        <div style={{marginTop:8}}>
          <Btn T={T} block onClick={go} disabled={ld} size="lg">
            {ld?<><Spin size={14}/> Un instant…</>:(mode==='signup'?'Créer mon compte':'Se connecter')}
          </Btn>
        </div>
        <div style={{textAlign:'center',fontSize:11,color:T.inkFaint,marginTop:18,fontFamily:'Inter,system-ui',lineHeight:1.5}}>
          En continuant vous acceptez nos <span style={{textDecoration:'underline',color:T.inkMuted}}>conditions</span>. Les estimations ne sont pas des valeurs médicales.
        </div>
      </div>
    </div>
  </div>;
}

function Dashboard({T,user,meals,onUpload,onHistory,onMeal,mobile,days_override,overview}){
  const days=days_override||last7(meals);
  const today=days[6];
  const target=2200;
  const pct=Math.min(1,today.calories/target);
  const qual=today.calories<1400?'Léger':today.calories<2000?'Équilibré':today.calories<2400?'Copieux':'Au-delà';
  const qualC=today.calories<1400?T.matcha:today.calories<2000?T.matcha:today.calories<2400?T.warn:T.danger;
  const maxBar=Math.max(...days.map(d=>d.calories),1800);
  const todayStr=new Date().toISOString().slice(0,10);
  const todayMeals=meals.filter(m=>m.date&&m.date.slice(0,10)===todayStr);
  const todayLabel=new Date().toLocaleDateString('fr-FR',{weekday:'long',day:'numeric',month:'long'});
  const [hv,setHv]=useState(null);

  return <div style={{maxWidth:1000,margin:'0 auto'}}>
    <div style={{display:'flex',alignItems:'baseline',justifyContent:'space-between',marginBottom:mobile?16:24,gap:12,flexWrap:'wrap'}}>
      <div style={{minWidth:0,flex:1}}>
        <div style={{fontFamily:'Inter,system-ui',fontSize:11.5,color:T.inkFaint,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:500,textTransform:'capitalize'}}>{todayLabel}</div>
        <div style={{fontFamily:'"Fraunces",serif',fontSize:mobile?26:32,color:T.ink,letterSpacing:'-.03em',marginTop:3,lineHeight:1.1,fontWeight:500,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis'}}>
          Bonjour, <span style={{fontStyle:'italic'}}>{user.username}</span>
        </div>
      </div>
    </div>

    <div style={{display:'grid',gridTemplateColumns:mobile?'1fr':'1.2fr 1fr',gap:mobile?12:16,marginBottom:mobile?12:16}}>
      {/* Day summary */}
      <div style={{background:T.surface,borderRadius:24,padding:mobile?18:24,border:`1px solid ${T.hairline}`,display:'flex',alignItems:'center',gap:mobile?16:22,position:'relative',overflow:'hidden'}}>
        <svg viewBox="0 0 400 200" preserveAspectRatio="none" style={{position:'absolute',inset:0,width:'100%',height:'100%',opacity:.35,pointerEvents:'none'}}>
          <path d="M0,140 Q100,100 200,130 Q300,160 400,110 L400,200 L0,200 Z" fill={T.accentSoft}/>
        </svg>
        <div style={{position:'relative',width:mobile?100:120,height:mobile?100:120,flexShrink:0}}>
          <svg width={mobile?100:120} height={mobile?100:120} viewBox="0 0 120 120" style={{transform:'rotate(-90deg)'}}>
            <circle cx="60" cy="60" r="50" fill="none" stroke={T.hairline} strokeWidth="9"/>
            <circle cx="60" cy="60" r="50" fill="none" stroke={T.accent} strokeWidth="9" strokeLinecap="round"
              strokeDasharray={`${2*Math.PI*50*pct} ${2*Math.PI*50}`} style={{transition:'stroke-dasharray .8s ease'}}/>
          </svg>
          <div style={{position:'absolute',inset:0,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'center'}}>
            <div style={{fontFamily:'JetBrains Mono,monospace',fontSize:mobile?22:26,fontWeight:600,color:T.ink,letterSpacing:'-.02em'}}>{today.calories}</div>
            <div style={{fontFamily:'Inter,system-ui',fontSize:10,color:T.inkFaint,textTransform:'uppercase',letterSpacing:'.12em'}}>kcal</div>
          </div>
        </div>
        <div style={{flex:1,minWidth:0,position:'relative'}}>
          <div style={{fontFamily:'Inter,system-ui',fontSize:11.5,color:T.inkMuted,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:500}}>Apport du jour</div>
          <div style={{fontFamily:'"Fraunces",serif',fontSize:mobile?30:36,color:qualC,letterSpacing:'-.02em',marginTop:4,fontStyle:'italic',lineHeight:1,fontWeight:500}}>{qual}</div>
          <div style={{fontFamily:'Inter,system-ui',fontSize:13,color:T.inkMuted,marginTop:8,lineHeight:1.4}}>
            {todayMeals.length?`${todayMeals.length} repas · objectif ${target} kcal`:`Aucun repas · objectif ${target} kcal`}
          </div>
        </div>
      </div>

      {/* Graph */}
      <div style={{background:T.surface,borderRadius:24,padding:mobile?18:22,border:`1px solid ${T.hairline}`}}>
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'baseline',marginBottom:14}}>
          <div style={{fontFamily:'Inter,system-ui',fontSize:11.5,color:T.inkMuted,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:500}}>7 derniers jours</div>
          <div style={{fontFamily:'JetBrains Mono,monospace',fontSize:11,color:T.inkFaint}}>moy. {Math.round(days.reduce((s,d)=>s+d.calories,0)/7)} kcal</div>
        </div>
        <div style={{display:'flex',alignItems:'flex-end',gap:8,height:mobile?90:110,position:'relative'}}>
          <div style={{position:'absolute',left:0,right:0,bottom:`${(target/(maxBar*1.2))*(mobile?90:110)}px`,borderTop:`1px dashed ${T.hairline}`}}>
            <span style={{position:'absolute',right:0,top:-13,fontFamily:'JetBrains Mono,monospace',fontSize:9,color:T.inkFaint}}>obj.</span>
          </div>
          {days.map((d,i)=>{
            const h=d.calories>0?(d.calories/(maxBar*1.2))*(mobile?90:110):4;
            const over=d.calories>target;
            return <div key={i} style={{flex:1,display:'flex',flexDirection:'column',alignItems:'center',justifyContent:'flex-end',height:'100%',cursor:'pointer',position:'relative'}} onMouseEnter={()=>setHv(i)} onMouseLeave={()=>setHv(null)}>
              {hv===i&&<div style={{position:'absolute',top:-8,transform:'translateY(-100%)',background:T.ink,color:T.bg,padding:'3px 8px',borderRadius:6,fontFamily:'JetBrains Mono,monospace',fontSize:10,whiteSpace:'nowrap'}}>{d.calories} kcal</div>}
              <div style={{width:'100%',maxWidth:22,height:h,background:d.isToday?T.accent:(over?T.danger:T.ink),opacity:d.isToday?1:(over?.75:.8),borderRadius:'6px 6px 3px 3px',transition:'height .5s ease'}}/>
            </div>;
          })}
        </div>
        <div style={{display:'flex',gap:8,marginTop:10}}>
          {days.map((d,i)=><div key={i} style={{flex:1,textAlign:'center',fontFamily:'Inter,system-ui',fontSize:10,color:d.isToday?T.accent:T.inkFaint,fontWeight:d.isToday?700:500,textTransform:'uppercase'}}>{d.label}<div style={{fontSize:9,fontFamily:'JetBrains Mono,monospace',marginTop:1}}>{d.dayNum}</div></div>)}
        </div>
      </div>
    </div>

    {/* Profil nutritionnel — macros moyennes par repas */}
    {overview&&overview.avgProtein!=null&&(overview.avgProtein>0||overview.avgCarbs>0||overview.avgFat>0)&&
    <div style={{display:'grid',gridTemplateColumns:mobile?'repeat(3,1fr)':'repeat(3,1fr)',gap:mobile?8:12,marginBottom:mobile?16:20}}>
      {[
        {l:'Protéines',v:overview.avgProtein,c:'#d07a52',bg:T.accentSoft},
        {l:'Glucides',  v:overview.avgCarbs,  c:'#8aa661',bg:T.matchaSoft},
        {l:'Lipides',   v:overview.avgFat,    c:'#b88242',bg:T.accentSoft},
      ].map(m=><div key={m.l} style={{background:T.surface,borderRadius:18,padding:mobile?12:16,border:`1px solid ${T.hairline}`,textAlign:'center',position:'relative',overflow:'hidden'}}>
        <div style={{position:'absolute',top:0,left:0,right:0,height:3,background:m.c}}/>
        <div style={{fontFamily:'Inter,system-ui',fontSize:10,color:T.inkFaint,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:500,marginBottom:4}}>{m.l}</div>
        <div style={{fontFamily:'JetBrains Mono,monospace',fontSize:mobile?20:22,color:T.ink,fontWeight:600,letterSpacing:'-.02em'}}>{Math.round(m.v)}<span style={{fontSize:11,color:T.inkFaint,marginLeft:2,fontFamily:'Inter,system-ui',fontWeight:500}}>g</span></div>
        <div style={{fontFamily:'Inter,system-ui',fontSize:9.5,color:T.inkFaint,marginTop:2,letterSpacing:'.04em'}}>moy / repas</div>
      </div>)}
    </div>}

    {/* CTA */}
    <button onClick={onUpload} style={{width:'100%',padding:mobile?20:26,background:T.ink,color:T.bg,border:'none',borderRadius:24,display:'flex',alignItems:'center',gap:mobile?14:20,cursor:'pointer',textAlign:'left',boxShadow:'0 14px 34px -10px rgba(30,15,5,.4)',position:'relative',overflow:'hidden',marginBottom:mobile?16:20}}>
      <div style={{position:'absolute',right:-40,top:-50,width:200,height:200,opacity:.3,borderRadius:'50%',background:dishGradient(42)}}/>
      <div style={{width:mobile?46:54,height:mobile?46:54,borderRadius:16,background:T.accent,display:'flex',alignItems:'center',justifyContent:'center',flexShrink:0,position:'relative',zIndex:1}}>
        <svg width="22" height="22" viewBox="0 0 22 22" fill="none"><rect x="3" y="5" width="16" height="13" rx="2" stroke="#fff" strokeWidth="1.7"/><circle cx="11" cy="11.5" r="3.5" stroke="#fff" strokeWidth="1.7"/><path d="M7 5l1.5-2h5L15 5" stroke="#fff" strokeWidth="1.7" strokeLinejoin="round"/></svg>
      </div>
      <div style={{position:'relative',zIndex:1,flex:1}}>
        <div style={{fontFamily:'"Fraunces",serif',fontSize:mobile?22:26,letterSpacing:'-.02em',fontWeight:500}}>Analyser mon repas</div>
        <div style={{fontFamily:'Inter,system-ui',fontSize:12,opacity:.7,marginTop:3}}>Photo · glisser-déposer · caméra</div>
      </div>
      <svg width="20" height="20" viewBox="0 0 18 18" fill="none" style={{position:'relative',zIndex:1}}><path d="M6 3l6 6-6 6" stroke={T.bg} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round"/></svg>
    </button>

    {/* Recent */}
    <div style={{display:'flex',alignItems:'baseline',justifyContent:'space-between',marginBottom:10}}>
      <div style={{fontFamily:'Inter,system-ui',fontSize:11.5,color:T.inkMuted,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:500}}>Repas récents</div>
      <button onClick={onHistory} style={{border:'none',background:'transparent',color:T.accent,fontFamily:'Inter,system-ui',fontSize:12,fontWeight:600,cursor:'pointer'}}>Tout voir →</button>
    </div>
    <div style={{display:'grid',gridTemplateColumns:mobile?'1fr':'1fr 1fr',gap:10}}>
      {meals.slice(0,mobile?3:4).map(m=><MealRow key={m.id} m={m} T={T} onClick={()=>onMeal(m)}/>)}
    </div>
  </div>;
}

function MealRow({m,T,onClick,compact}){
  const d=new Date(m.date);
  const t=d.toLocaleTimeString('fr-FR',{hour:'2-digit',minute:'2-digit'});
  const mid=Math.round((m.kMin+m.kMax)/2);
  const photo=m.img||m.photoUrl;
  const [imgFailed,setImgFailed]=useState(false);
  const hs=healthScore(m);
  return <button onClick={onClick} style={{padding:'9px 10px',display:'flex',alignItems:'center',gap:10,background:T.surface,border:`1px solid ${T.hairline}`,borderRadius:16,cursor:'pointer',textAlign:'left',width:'100%'}}>
    <div style={{position:'relative',flexShrink:0}}>
      {photo && !imgFailed
        ? <img src={photo} alt="" onError={()=>setImgFailed(true)} style={{width:46,height:46,borderRadius:12,objectFit:'cover',display:'block'}}/>
        : <Dish seed={m.seed} style={{width:46,height:46}} rounded={12}/>
      }
      {/* Badge santé A→E */}
      <div style={{position:'absolute',bottom:-4,right:-6,width:22,height:22,borderRadius:'50%',background:hs.color,color:'#fff',display:'flex',alignItems:'center',justifyContent:'center',fontFamily:'"Fraunces",serif',fontSize:11,fontWeight:700,boxShadow:'0 1px 4px rgba(0,0,0,.25)',border:'1.5px solid #fff',lineHeight:1}} title={'Score santé '+hs.grade}>{hs.grade}</div>
    </div>
    <div style={{flex:1,minWidth:0}}>
      <div style={{display:'flex',alignItems:'center',gap:6,flexWrap:'nowrap'}}>
        <div style={{fontFamily:'Inter,system-ui',fontSize:13,color:T.ink,fontWeight:600,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis',minWidth:0,flex:'1 1 0'}}>{m.name}</div>
        {(m.status==='CORRECTED'||(m.analysisRaw&&m.analysisRaw.hint))&&
          <span style={{flexShrink:0,fontSize:10,fontFamily:'Inter,system-ui',fontWeight:600,padding:'2px 6px',borderRadius:999,background:T.matcha||'#4caf50',color:'#fff',lineHeight:1.4}}>Corrigé</span>}
      </div>
      <div style={{fontFamily:'Inter,system-ui',fontSize:11,color:T.inkFaint,marginTop:2,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis'}}>{m.meal} · {t}</div>
    </div>
    <div style={{textAlign:'right',flexShrink:0,display:'flex',alignItems:'baseline',gap:3}}>
      <div style={{fontFamily:'JetBrains Mono,monospace',fontSize:14,color:T.ink,fontWeight:600}}>{mid}</div>
      <div style={{fontFamily:'Inter,system-ui',fontSize:9.5,color:T.inkFaint,letterSpacing:'.05em'}}>kcal</div>
    </div>
  </button>;
}

Object.assign(window,{Auth,Dashboard,MealRow});
