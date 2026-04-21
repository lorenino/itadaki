const {useState,useEffect,useMemo,useRef}=React;

function Dish({seed,style={},rounded=20,children}){
  return <div style={{position:'relative',background:dishGradient(seed),borderRadius:rounded,overflow:'hidden',...style}}>
    <div style={{position:'absolute',inset:0,background:'radial-gradient(circle at 30% 25%,rgba(255,255,255,.15),transparent 55%)'}}/>
    {children}
  </div>;
}

function btnPad(size){
  if(size==='lg') return '16px 26px';
  if(size==='sm') return '8px 14px';
  return '12px 20px';
}
function btnFs(size){
  if(size==='lg') return 16;
  if(size==='sm') return 13;
  return 14;
}
function Btn({children,onClick,variant='primary',T,style={},disabled,block,size='md',icon}){
  const pad=btnPad(size);
  const fs=btnFs(size);
  const v={
    primary:{bg:T.accent,fg:'#fff',bd:'transparent',sh:`0 8px 20px -8px ${T.accent}`},
    dark:{bg:T.ink,fg:T.bg,bd:'transparent',sh:'none'},
    ghost:{bg:'transparent',fg:T.ink,bd:T.hairline,sh:'none'},
    soft:{bg:T.accentSoft,fg:T.accentDeep,bd:'transparent',sh:'none'},
  }[variant];
  return <button onClick={onClick} disabled={disabled} style={{
    display:'inline-flex',alignItems:'center',justifyContent:'center',gap:8,
    width:block?'100%':'auto',padding:pad,background:v.bg,color:v.fg,
    border:`1px solid ${v.bd}`,borderRadius:999,
    fontFamily:'Inter,system-ui',fontSize:fs,fontWeight:600,letterSpacing:'-.01em',
    opacity:disabled?.5:1,cursor:disabled?'not-allowed':'pointer',
    boxShadow:v.sh,transition:'transform .15s ease, box-shadow .2s ease',
    ...style}}
    onMouseDown={e=>e.currentTarget.style.transform='scale(.98)'}
    onMouseUp={e=>e.currentTarget.style.transform='scale(1)'}
    onMouseLeave={e=>e.currentTarget.style.transform='scale(1)'}
  >{icon}{children}</button>;
}

function Field({label,value,onChange,type='text',placeholder,T,icon,hint,error}){
  const [f,setF]=useState(false);
  let borderColor;
  if(error) borderColor=T.danger;
  else if(f) borderColor=T.accent;
  else borderColor=T.hairline;
  return <label style={{display:'block',marginBottom:14}}>
    {label&&<div style={{fontFamily:'Inter,system-ui',fontSize:11,fontWeight:500,textTransform:'uppercase',letterSpacing:'.1em',color:T.inkMuted,marginBottom:7}}>{label}</div>}
    <div style={{display:'flex',alignItems:'center',gap:10,padding:'13px 16px',background:T.surface,border:`1px solid ${borderColor}`,borderRadius:14,transition:'border-color .15s',boxShadow:f?`0 0 0 3px ${T.accentSoft}`:'none'}}>
      {icon&&<div style={{color:T.inkFaint,display:'flex'}}>{icon}</div>}
      <input type={type} value={value} onChange={e=>onChange(e.target.value)} onFocus={()=>setF(true)} onBlur={()=>setF(false)} placeholder={placeholder}
        style={{flex:1,border:'none',outline:'none',background:'transparent',fontFamily:'Inter,system-ui',fontSize:15,color:T.ink,padding:0}}/>
    </div>
    {(hint||error)&&<div style={{fontSize:11.5,color:error?T.danger:T.inkFaint,marginTop:6,fontFamily:'Inter,system-ui'}}>{error||hint}</div>}
  </label>;
}

function Chip({children,T,variant='default',style={}}){
  const v={
    default:{bg:T.bgAlt,fg:T.ink,bd:T.hairline},
    accent:{bg:T.accentSoft,fg:T.accentDeep,bd:'transparent'},
    matcha:{bg:T.matchaSoft,fg:T.matcha,bd:'transparent'},
    ghost:{bg:'transparent',fg:T.inkMuted,bd:T.hairline},
  }[variant];
  return <span style={{display:'inline-flex',alignItems:'center',gap:6,padding:'5px 11px',background:v.bg,color:v.fg,border:`1px solid ${v.bd}`,borderRadius:999,fontFamily:'Inter,system-ui',fontSize:12.5,fontWeight:500,...style}}>{children}</span>;
}

// Confidence as percentage + bar (only variant per spec)
function confidenceColor(value,T){
  if(value>=85) return T.matcha;
  if(value>=65) return T.warn;
  return T.danger;
}
function confidenceBg(value,T){
  if(value>=85) return T.matchaSoft;
  if(value>=65) return 'oklch(0.94 0.05 70)';
  return 'oklch(0.94 0.04 25)';
}
function Confidence({value,T}){
  const color=confidenceColor(value,T);
  const bg=confidenceBg(value,T);
  return <div style={{display:'inline-flex',alignItems:'center',gap:10,padding:'6px 12px',background:bg,borderRadius:999,fontFamily:'JetBrains Mono,monospace',fontSize:12,fontWeight:600,color}}>
    <div style={{width:42,height:4,background:'rgba(0,0,0,.08)',borderRadius:2,overflow:'hidden'}}>
      <div style={{width:`${value}%`,height:'100%',background:color,transition:'width .6s ease'}}/>
    </div>
    <span>{value}%</span>
    <span style={{fontFamily:'Inter,system-ui',fontWeight:500,letterSpacing:'.02em',textTransform:'lowercase'}}>confiance</span>
  </div>;
}

// Organic wavy shape (hero / backgrounds)
function Blob({color,style={},variant=1}){
  const paths=[
    "M0,0 C200,50 400,-20 600,40 C800,100 900,20 1000,60 L1000,400 L0,400 Z",
    "M50,120 C150,30 300,180 500,100 C700,20 850,160 1000,80 L1000,400 L0,400 L0,120 Z",
    "M0,200 C200,100 400,300 600,180 C800,60 950,260 1000,160 L1000,400 L0,400 Z",
  ];
  return <svg viewBox="0 0 1000 400" preserveAspectRatio="none" style={{position:'absolute',inset:0,width:'100%',height:'100%',pointerEvents:'none',...style}}>
    <path d={paths[variant-1]} fill={color}/>
  </svg>;
}

// Wavy divider (SVG)
function Wave({color,flip,height=40}){
  return <svg viewBox="0 0 1200 40" preserveAspectRatio="none" style={{display:'block',width:'100%',height,transform:flip?'scaleY(-1)':'none'}}>
    <path d="M0,20 C200,40 400,0 600,20 C800,40 1000,0 1200,20 L1200,40 L0,40 Z" fill={color}/>
  </svg>;
}

// Spinner
function Spin({size=16,color='#fff'}){
  return <svg width={size} height={size} viewBox="0 0 18 18" style={{animation:'spin .8s linear infinite'}}>
    <circle cx="9" cy="9" r="7" stroke={color} strokeWidth="2" fill="none" strokeDasharray="30 60" strokeLinecap="round"/>
  </svg>;
}

// App shell — wraps one "screen" with web nav (desktop) or mobile layout
function Shell({T,mobile,active,onNav,user,children,pad=true}){
  if(mobile){
    return <div style={{height:'100%',display:'flex',flexDirection:'column',background:T.bg,color:T.ink,overflow:'hidden'}}>
      <div style={{flex:1,overflowY:'auto',padding:pad?'16px 18px 12px':0}}>{children}</div>
      {active&&<MobNav T={T} active={active} onNav={onNav} user={user}/>}
    </div>;
  }
  return <div style={{height:'100%',display:'flex',background:T.bg,color:T.ink,overflow:'hidden'}}>
    {active&&<SideNav T={T} active={active} onNav={onNav} user={user}/>}
    <div style={{flex:1,overflowY:'auto',padding:pad?'28px 40px 40px':0,position:'relative'}}>{children}</div>
  </div>;
}

function SideNav({T,active,onNav,user}){
  const items=[
    {id:'dashboard',l:'Aujourd\'hui',i:c=><svg width="18" height="18" viewBox="0 0 22 22" fill="none"><path d="M4 9l7-5 7 5v8a1 1 0 01-1 1h-4v-5h-4v5H5a1 1 0 01-1-1V9z" stroke={c} strokeWidth="1.7"/></svg>},
    {id:'upload',l:'Analyser',i:c=><svg width="18" height="18" viewBox="0 0 22 22" fill="none"><rect x="3" y="5" width="16" height="13" rx="2" stroke={c} strokeWidth="1.7"/><circle cx="11" cy="11.5" r="3.5" stroke={c} strokeWidth="1.7"/></svg>},
    {id:'history',l:'Historique',i:c=><svg width="18" height="18" viewBox="0 0 22 22" fill="none"><rect x="3" y="4" width="16" height="14" rx="2" stroke={c} strokeWidth="1.7"/><path d="M7 9h8M7 13h8M7 16h5" stroke={c} strokeWidth="1.7" strokeLinecap="round"/></svg>},
    {id:'profile',l:'Profil',i:c=><svg width="18" height="18" viewBox="0 0 22 22" fill="none"><circle cx="11" cy="8" r="3.5" stroke={c} strokeWidth="1.7"/><path d="M4 19c1-3.5 4-5 7-5s6 1.5 7 5" stroke={c} strokeWidth="1.7" strokeLinecap="round"/></svg>},
  ];
  if(user&&user.role==='ADMIN'){
    items.push({id:'admin',l:'Admin',i:c=><svg width="18" height="18" viewBox="0 0 22 22" fill="none"><path d="M11 3l7 3v5c0 4-3 7-7 8-4-1-7-4-7-8V6l7-3z" stroke={c} strokeWidth="1.7" strokeLinejoin="round"/><path d="M8 11l2 2 4-4" stroke={c} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round"/></svg>});
  }
  return <div style={{width:210,flexShrink:0,background:T.bg,borderRight:`1px solid ${T.hairline}`,padding:'24px 14px',display:'flex',flexDirection:'column',gap:2}}>
    <div style={{display:'flex',alignItems:'center',gap:10,padding:'4px 8px 22px'}}>
      <div style={{width:36,height:36,borderRadius:'14px 14px 14px 6px',background:T.accent,color:'#fff',display:'flex',alignItems:'center',justifyContent:'center',fontFamily:'"Fraunces",serif',fontSize:22,fontStyle:'italic',fontWeight:500,transform:'rotate(-4deg)'}}>i</div>
      <div style={{fontFamily:'"Fraunces",serif',fontSize:23,color:T.ink,letterSpacing:'-.03em',fontWeight:500}}>Itadaki</div>
    </div>
    {items.map(it=>{const a=active===it.id;return <button key={it.id} onClick={()=>onNav(it.id)} style={{display:'flex',alignItems:'center',gap:12,padding:'11px 14px',borderRadius:14,background:a?T.accentSoft:'transparent',color:a?T.accentDeep:T.inkMuted,fontFamily:'Inter,system-ui',fontSize:14,fontWeight:a?600:500,textAlign:'left',cursor:'pointer',border:'none'}}>{it.i(a?T.accentDeep:T.inkMuted)}{it.l}</button>;})}
    <div style={{flex:1}}/>
    {user&&<div style={{display:'flex',alignItems:'center',gap:10,padding:10,background:T.bgAlt,borderRadius:14}}>
      <div style={{width:30,height:30,borderRadius:'50%',background:T.accent,color:'#fff',display:'flex',alignItems:'center',justifyContent:'center',fontFamily:'"Fraunces",serif',fontSize:14,fontStyle:'italic'}}>{user.username[0].toUpperCase()}</div>
      <div style={{flex:1,minWidth:0}}>
        <div style={{fontFamily:'Inter,system-ui',fontSize:12.5,fontWeight:600,color:T.ink,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis'}}>{user.username}</div>
        <div style={{fontFamily:'Inter,system-ui',fontSize:10.5,color:T.inkFaint,whiteSpace:'nowrap',overflow:'hidden',textOverflow:'ellipsis'}}>{user.email}</div>
      </div>
    </div>}
  </div>;
}

function MobNav({T,active,onNav,user}){
  const items=[
    {id:'dashboard',l:'Accueil'},
    {id:'upload',l:'Analyser'},
    {id:'history',l:'Historique'},
    {id:'profile',l:'Profil'},
  ];
  if(user&&user.role==='ADMIN') items.push({id:'admin',l:'Admin'});
  return <div style={{display:'flex',padding:'8px 10px 14px',background:T.bg,borderTop:`1px solid ${T.hairline}`,flexShrink:0}}>
    {items.map(t=>{const a=active===t.id;return <button key={t.id} onClick={()=>onNav(t.id)} style={{flex:1,display:'flex',flexDirection:'column',alignItems:'center',gap:3,padding:'8px 0',border:'none',background:'transparent',cursor:'pointer'}}>
      <div style={{width:5,height:5,borderRadius:'50%',background:a?T.accent:'transparent'}}/>
      <span style={{fontFamily:'Inter,system-ui',fontSize:11,color:a?T.accent:T.inkFaint,fontWeight:a?600:500}}>{t.l}</span>
    </button>;})}
  </div>;
}

Object.assign(globalThis,{Dish,Btn,Field,Chip,Confidence,Blob,Wave,Spin,Shell,SideNav,MobNav});
