// Upload, Correction, History, Profile screens (static/demo versions — not wired to API)

function Upload({ T, onCancel, onAnalyzed, mobile }) {
  const [stage, setStage] = useState('idle'); // idle | preview | loading
  const [img, setImg] = useState(null);
  const [drag, setDrag] = useState(false);
  const [prog, setProg] = useState(0);
  const [loadVar] = useState(() => Math.floor(Math.random() * 3)); // 0..2 variants
  const fileRef = useRef();

  const pick = (f) => {
    if (!f) return;
    const r = new FileReader();
    r.onload = e => { setImg(e.target.result); setStage('preview'); };
    r.readAsDataURL(f);
  };

  const usePlaceholder = () => { setImg('PLACEHOLDER'); setStage('preview'); };

  const analyze = () => {
    setStage('loading');
    setProg(0);
    let p = 0;
    const t = setInterval(() => {
      p += 4 + Math.random() * 6;
      if (p >= 100) {
        clearInterval(t);
        setProg(100);
        setTimeout(() => onAnalyzed({
          id: 'new',
          name: 'Ramen shoyu au porc chashu',
          ing: ['nouilles ramen', 'bouillon shoyu', 'porc chashu', 'œuf mollet', 'pousses de bambou', 'oignon vert'],
          portion: 'grand', kMin: 720, kMax: 880, conf: 71,
          date: new Date().toISOString(), meal: 'Dîner', seed: 13, img,
        }), 300);
      } else {
        setProg(p);
      }
    }, 90);
  };

  if (stage === 'loading') {
    const variants = [
      // v1 — dish with rotating ring
      <div key="v1" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 26 }}>
        <div style={{ position: 'relative', width: mobile ? 180 : 220, height: mobile ? 180 : 220 }}>
          <Dish seed={13} style={{ position: 'absolute', inset: 14, borderRadius: '50%' }} />
          <svg width="100%" height="100%" viewBox="0 0 220 220" style={{ position: 'absolute', inset: 0, animation: 'spin 3s linear infinite' }}>
            <circle cx="110" cy="110" r="104" fill="none" stroke={T.accent} strokeWidth="3" strokeDasharray="10 14" strokeLinecap="round" />
          </svg>
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ width: 46, height: 46, borderRadius: '50%', background: 'rgba(255,255,255,.95)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 14px rgba(0,0,0,.2)' }}>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 13, color: T.ink, fontWeight: 600 }}>{Math.round(prog)}%</div>
            </div>
          </div>
        </div>
      </div>,

      // v2 — ingredient list typewriter
      <div key="v2" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 18, width: '100%', maxWidth: 360 }}>
        <Dish seed={13} style={{ width: 120, height: 120, borderRadius: '50%' }} />
        <div style={{ width: '100%', background: T.bgAlt, borderRadius: 18, padding: 18, fontFamily: 'JetBrains Mono,monospace', fontSize: 13, color: T.inkMuted }}>
          {['→ plat détecté', '→ ingrédients: 6/7', '→ portion estimée', '→ calories…']
            .slice(0, Math.min(4, Math.ceil(prog / 25)))
            .map(l => <div key={l} style={{ padding: '3px 0', opacity: 1, animation: 'rise .3s ease' }}>{l}</div>)
          }
          <div style={{ height: 3, background: T.hairline, borderRadius: 2, marginTop: 10, overflow: 'hidden' }}>
            <div style={{ width: `${prog}%`, height: '100%', background: T.accent, transition: 'width .3s' }} />
          </div>
        </div>
      </div>,

      // v3 — orbiting dots + progress bar
      <div key="v3" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 22, width: '100%', maxWidth: 360 }}>
        <div style={{ position: 'relative', width: 160, height: 160 }}>
          {[0, 1, 2, 3, 4].map(i => (
            <div key={`dot-${i}`} style={{
              position: 'absolute', left: '50%', top: '50%',
              width: 10, height: 10, borderRadius: '50%',
              background: i % 2 ? T.accent : T.matcha,
              transform: `translate(-50%,-50%) rotate(${i * 72}deg) translateY(-60px)`,
              animation: `breathe 1.${4 + i}s ease-in-out infinite`,
              animationDelay: `${i * 0.1}s`,
            }} />
          ))}
          <div style={{ position: 'absolute', inset: 30 }}>
            <Dish seed={13} style={{ width: '100%', height: '100%', borderRadius: '50%' }} />
          </div>
        </div>
        <div style={{ width: '100%', height: 6, background: T.hairline, borderRadius: 3, overflow: 'hidden' }}>
          <div style={{ width: `${prog}%`, height: '100%', background: `linear-gradient(90deg,${T.accent},${T.matcha})`, borderRadius: 3, transition: 'width .3s' }} />
        </div>
      </div>,
    ];

    const captions = ['On observe votre assiette…', 'On identifie les ingrédients…', 'On pèse les portions…', 'On fait le calcul…'];
    const captionIdx = Math.min(3, Math.floor(prog / 25));

    return (
      <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px 24px', background: T.bg, color: T.ink, textAlign: 'center' }}>
        {variants[loadVar]}
        <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 20 : 24, letterSpacing: '-.02em', marginTop: 30, fontStyle: 'italic', fontWeight: 500 }}>
          {captions[captionIdx]}
        </div>
        <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12.5, color: T.inkFaint, marginTop: 8, maxWidth: 340, lineHeight: 1.5 }}>
          Cela peut prendre quelques secondes. Vous pourrez ajuster les détails à l'étape suivante.
        </div>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 760, margin: '0 auto', padding: mobile ? '8px 4px' : '0' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <button onClick={onCancel} style={{ border: 'none', background: 'transparent', color: T.inkMuted, fontFamily: 'Inter,system-ui', fontSize: 13.5, fontWeight: 500, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}>
          <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M11 4L6 9l5 5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round" /></svg>
          Retour
        </button>
        <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11.5, color: T.inkFaint, textTransform: 'uppercase', letterSpacing: '.1em' }}>Étape 1 / 2</div>
      </div>

      <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 28 : 38, color: T.ink, letterSpacing: '-.03em', lineHeight: 1.08, fontWeight: 500, marginBottom: 8 }}>
        Montrez-nous<br /><span style={{ fontStyle: 'italic', color: T.accent }}>votre assiette.</span>
      </div>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13.5, color: T.inkMuted, marginBottom: 22, lineHeight: 1.5 }}>
        Une photo nette et lumineuse, vue de dessus — l'IA s'en occupe.
      </div>

      {stage === 'idle' && (
        <>
          <div
            role="button"
            tabIndex={0}
            onDragOver={e => { e.preventDefault(); setDrag(true); }}
            onDragLeave={() => setDrag(false)}
            onDrop={e => { e.preventDefault(); setDrag(false); pick(e.dataTransfer.files[0]); }}
            onClick={() => fileRef.current.click()}
            onKeyDown={e => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); fileRef.current.click(); } }}
            style={{ border: `2px dashed ${drag ? T.accent : T.hairline}`, borderRadius: 24, padding: mobile ? '34px 18px' : '52px 28px', textAlign: 'center', background: drag ? T.accentSoft : T.bgAlt, cursor: 'pointer', transition: 'all .2s', position: 'relative', overflow: 'hidden' }}
          >
            <svg viewBox="0 0 600 200" preserveAspectRatio="none" style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: .4, pointerEvents: 'none' }}>
              <path d="M0,130 Q150,80 300,120 Q450,160 600,100 L600,200 L0,200 Z" fill={T.matchaSoft} />
            </svg>
            <div style={{ position: 'relative' }}>
              <div style={{ width: 72, height: 72, margin: '0 auto 16px', borderRadius: 24, background: T.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 6px 18px rgba(0,0,0,.06)' }}>
                <svg width="32" height="32" viewBox="0 0 28 28" fill="none">
                  <rect x="4" y="7" width="20" height="16" rx="2" stroke={T.accent} strokeWidth="1.7" />
                  <circle cx="14" cy="15" r="4.5" stroke={T.accent} strokeWidth="1.7" />
                  <path d="M9 7l1.5-2.5h7L19 7" stroke={T.accent} strokeWidth="1.7" strokeLinejoin="round" />
                </svg>
              </div>
              <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 20 : 22, color: T.ink, letterSpacing: '-.02em', fontWeight: 500 }}>Glissez une photo ici</div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginTop: 6 }}>ou cliquez pour parcourir · JPG, PNG, HEIC · 20 Mo max</div>
            </div>
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={e => pick(e.target.files[0])} />
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 14, flexWrap: 'wrap' }}>
            <Btn T={T} variant="soft" onClick={() => fileRef.current.click()} icon={<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><rect x="2" y="4" width="12" height="10" rx="1.5" stroke="currentColor" strokeWidth="1.5" /><circle cx="8" cy="9" r="2.8" stroke="currentColor" strokeWidth="1.5" /></svg>}>Prendre une photo</Btn>
            <Btn T={T} variant="ghost" onClick={usePlaceholder} icon={<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="8" r="6" stroke="currentColor" strokeWidth="1.5" /><path d="M8 5v3l2 1.5" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" /></svg>}>Utiliser un exemple</Btn>
          </div>
        </>
      )}

      {stage === 'preview' && (
        <>
          <div style={{ borderRadius: 24, overflow: 'hidden', background: T.bgAlt, position: 'relative', aspectRatio: '4/3' }}>
            {img === 'PLACEHOLDER'
              ? <Dish seed={13} style={{ width: '100%', height: '100%' }} rounded={0} />
              : <img src={img} alt="" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
            }
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 14 }}>
            <Btn T={T} variant="ghost" onClick={() => { setStage('idle'); setImg(null); }}>Remplacer</Btn>
            <Btn T={T} onClick={analyze} block size="lg">Analyser →</Btn>
          </div>
        </>
      )}
    </div>
  );
}

function Correction({ T, meal, onSave, onCancel, mobile }) {
  const [name, setName] = useState(meal.name);
  const [ing, setIng] = useState(meal.ing.join(', '));
  const [portion, setPortion] = useState(meal.portion);
  const [kMin, setKMin] = useState(meal.kMin);
  const [kMax, setKMax] = useState(meal.kMax);
  const [mealT, setMealT] = useState(meal.meal);

  return (
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <button onClick={onCancel} style={{ border: 'none', background: 'transparent', color: T.inkMuted, fontFamily: 'Inter,system-ui', fontSize: 13.5, fontWeight: 500, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 6 }}>
          <svg width="16" height="16" viewBox="0 0 18 18" fill="none"><path d="M11 4L6 9l5 5" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" /></svg>
          Retour
        </button>
        <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11.5, color: T.inkFaint, textTransform: 'uppercase', letterSpacing: '.1em' }}>Étape 2 / 2</div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: mobile ? '1fr' : '240px 1fr', gap: 20, alignItems: 'start' }}>
        <div>
          <Dish seed={meal.seed} style={{ width: '100%', aspectRatio: '1/1', boxShadow: '0 10px 30px -10px rgba(80,40,10,.3)' }} rounded={24} />
          <div style={{ marginTop: 14, padding: 14, background: T.accentSoft, borderRadius: 16 }}>
            <Confidence value={meal.conf} T={T} />
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.accentDeep, marginTop: 10, lineHeight: 1.5 }}>
              Les détails ci-contre ont été estimés. Corrigez ce qui vous semble faux — vos modifications améliorent les futures analyses.
            </div>
          </div>
        </div>

        <div>
          <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 26 : 30, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.1, marginBottom: 4, fontWeight: 500 }}>Ajustez les détails</div>
          <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginBottom: 20 }}>Touchez à ce qui doit changer. Tout le reste sera enregistré tel quel.</div>

          <Field T={T} label="Nom du plat" value={name} onChange={setName} />
          <Field T={T} label="Ingrédients" value={ing} onChange={setIng} hint="Séparez par une virgule" />

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkMuted, marginBottom: 7 }}>Portion</div>
              <div style={{ display: 'flex', gap: 4, padding: 4, background: T.bgAlt, borderRadius: 12 }}>
                {['petit', 'moyen', 'grand'].map(p => (
                  <button key={p} onClick={() => setPortion(p)} style={{ flex: 1, padding: '8px 4px', border: 'none', background: portion === p ? T.surface : 'transparent', borderRadius: 8, fontFamily: 'Inter,system-ui', fontSize: 12, fontWeight: 600, color: portion === p ? T.ink : T.inkMuted, cursor: 'pointer', boxShadow: portion === p ? '0 1px 3px rgba(0,0,0,.08)' : 'none', textTransform: 'capitalize' }}>{p}</button>
                ))}
              </div>
            </div>
            <div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkMuted, marginBottom: 7 }}>Moment</div>
              <select value={mealT} onChange={e => setMealT(e.target.value)} style={{ width: '100%', padding: '12px 14px', background: T.surface, border: `1px solid ${T.hairline}`, borderRadius: 12, fontFamily: 'Inter,system-ui', fontSize: 14, color: T.ink, cursor: 'pointer' }}>
                {['Petit-déj.', 'Déjeuner', 'Goûter', 'Dîner', 'Collation'].map(x => <option key={x}>{x}</option>)}
              </select>
            </div>
          </div>

          <div style={{ marginTop: 16 }}>
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkMuted, marginBottom: 7 }}>Calories estimées</div>
            <div style={{ display: 'flex', gap: 10, alignItems: 'center', padding: '14px 16px', background: T.surface, border: `1px solid ${T.hairline}`, borderRadius: 14 }}>
              <input type="number" value={kMin} onChange={e => setKMin(+e.target.value)} style={{ width: 70, border: 'none', outline: 'none', background: 'transparent', fontFamily: 'JetBrains Mono,monospace', fontSize: 16, color: T.ink, fontWeight: 600 }} />
              <span style={{ color: T.inkFaint, fontFamily: 'JetBrains Mono,monospace' }}>–</span>
              <input type="number" value={kMax} onChange={e => setKMax(+e.target.value)} style={{ width: 70, border: 'none', outline: 'none', background: 'transparent', fontFamily: 'JetBrains Mono,monospace', fontSize: 16, color: T.ink, fontWeight: 600 }} />
              <span style={{ color: T.inkFaint, fontFamily: 'Inter,system-ui', fontSize: 13, marginLeft: 'auto' }}>kcal</span>
            </div>
          </div>

          <div style={{ marginTop: 22, display: 'flex', gap: 10 }}>
            <Btn T={T} variant="ghost" onClick={onCancel}>Annuler</Btn>
            <Btn T={T} onClick={() => onSave({ ...meal, name, ing: ing.split(',').map(x => x.trim()).filter(Boolean), portion, kMin, kMax, meal: mealT })} block size="lg">Enregistrer le repas</Btn>
          </div>
        </div>
      </div>
    </div>
  );
}

function History({ T, meals, onMeal, mobile }) {
  const [filter, setFilter] = useState('all');

  const groups = {};
  meals.forEach(m => {
    if (!m.date) return;
    const k = m.date.slice(0, 10);
    if (!groups[k]) groups[k] = [];
    groups[k].push(m);
  });
  const days = Object.keys(groups).sort((a, b) => b.localeCompare(a));

  const dayName = (k) => {
    const d = new Date(k + 'T12:00:00');
    const today = new Date().toISOString().slice(0, 10);
    const yd = new Date();
    yd.setDate(yd.getDate() - 1);
    const yesterday = yd.toISOString().slice(0, 10);
    if (k === today)     return 'Aujourd\'hui';
    if (k === yesterday) return 'Hier';
    return d.toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' });
  };

  const filters = [
    { id: 'all', l: 'Tout' },
    { id: 'Petit-déj', l: 'Petit-déj' },
    { id: 'Déjeuner',  l: 'Déjeuner' },
    { id: 'Dîner',     l: 'Dîner' },
  ];

  return (
    <div style={{ maxWidth: 820, margin: '0 auto' }}>
      <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 28 : 36, color: T.ink, letterSpacing: '-.03em', lineHeight: 1.08, fontWeight: 500, marginBottom: 6 }}>
        <span style={{ fontStyle: 'italic' }}>Historique</span>
      </div>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginBottom: 20 }}>{meals.length} repas enregistrés</div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20, overflowX: 'auto', paddingBottom: 4 }}>
        {filters.map(f => (
          <button key={f.id} onClick={() => setFilter(f.id)} style={{ padding: '7px 14px', background: filter === f.id ? T.ink : T.bgAlt, color: filter === f.id ? T.bg : T.inkMuted, border: 'none', borderRadius: 999, fontFamily: 'Inter,system-ui', fontSize: 12.5, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap', flexShrink: 0 }}>{f.l}</button>
        ))}
      </div>

      {days.map(k => {
        const dayMeals = groups[k].filter(m => filter === 'all' || m.meal === filter);
        if (!dayMeals.length) return null;
        const total = dayMeals.reduce((s, m) => s + (m.kMin + m.kMax) / 2, 0);
        return (
          <div key={k} style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10, paddingBottom: 8, borderBottom: `1px solid ${T.hairline}` }}>
              <div style={{ fontFamily: '"Fraunces",serif', fontSize: 17, color: T.ink, letterSpacing: '-.01em', fontWeight: 500, textTransform: 'capitalize' }}>{dayName(k)}</div>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 12, color: T.inkMuted }}>{Math.round(total)} kcal</div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: mobile ? '1fr' : '1fr 1fr', gap: 10 }}>
              {dayMeals.map(m => <MealRow key={m.id} m={m} T={T} onClick={() => onMeal(m)} />)}
            </div>
          </div>
        );
      })}
    </div>
  );
}

function Profile({ T, user, onLogout, mobile, dark, setDark }) {
  const target = 2200;
  return (
    <div style={{ maxWidth: 620, margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 18, marginBottom: 28 }}>
        <div style={{ width: 74, height: 74, borderRadius: '50%', background: T.accent, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: '"Fraunces",serif', fontSize: 34, fontStyle: 'italic', fontWeight: 500, flexShrink: 0, boxShadow: `0 10px 28px -10px ${T.accent}` }}>
          {user.username[0].toUpperCase()}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 24 : 28, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.1, fontWeight: 500, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user.username}</div>
          <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginTop: 3, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{user.email}</div>
        </div>
      </div>

      <Section T={T} title="Vos objectifs">
        <Row T={T} label="Apport quotidien cible" value={`${target} kcal`} />
        <Row T={T} label="Activité" value="Modérée" />
        <Row T={T} label="Préférence" value="Équilibré" />
      </Section>

      <Section T={T} title="Apparence">
        <div style={{ padding: '4px 0' }}>
          <div style={{ display: 'flex', gap: 8, padding: 5, background: T.bgAlt, borderRadius: 14 }}>
            {[{ id: false, l: 'Clair', i: '☀' }, { id: true, l: 'Sombre', i: '☾' }].map(m => (
              <button key={m.l} onClick={() => setDark(m.id)} style={{ flex: 1, padding: '11px 10px', border: 'none', background: dark === m.id ? T.surface : 'transparent', borderRadius: 10, fontFamily: 'Inter,system-ui', fontSize: 13, fontWeight: 600, color: dark === m.id ? T.ink : T.inkMuted, cursor: 'pointer', boxShadow: dark === m.id ? '0 1px 3px rgba(0,0,0,.08)' : 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8 }}>
                <span style={{ fontSize: 15 }}>{m.i}</span>{m.l}
              </button>
            ))}
          </div>
        </div>
      </Section>

      <Section T={T} title="Compte">
        <RowBtn T={T} label="Exporter mes données" sub="CSV · 14 jours" />
        <RowBtn T={T} label="Confidentialité & données" />
        <RowBtn T={T} label="Aide & contact" />
        <RowLink T={T} label="Documentation API" href="/swagger-ui/index.html" />
        <RowBtn T={T} label="Se déconnecter" danger onClick={onLogout} />
      </Section>

      <div style={{ textAlign: 'center', fontSize: 11, color: T.inkFaint, fontFamily: 'JetBrains Mono,monospace', marginTop: 20 }}>Itadaki v2.0 · demo</div>
    </div>
  );
}

function Section({ T, title, children }) {
  return (
    <div style={{ marginBottom: 20 }}>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.12em', color: T.inkMuted, marginBottom: 10, paddingLeft: 4 }}>{title}</div>
      <div style={{ background: T.surface, border: `1px solid ${T.hairline}`, borderRadius: 18, padding: '6px 16px' }}>{children}</div>
    </div>
  );
}

function Row({ T, label, value }) {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', padding: '13px 0', borderBottom: `1px solid ${T.hairline}`, fontFamily: 'Inter,system-ui', fontSize: 14 }}>
      <span style={{ color: T.inkMuted }}>{label}</span>
      <span style={{ color: T.ink, fontWeight: 600 }}>{value}</span>
    </div>
  );
}

function RowBtn({ T, label, sub, danger, onClick }) {
  return (
    <button onClick={onClick} style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '13px 0', borderBottom: `1px solid ${T.hairline}`, border: 'none', background: 'transparent', cursor: 'pointer', textAlign: 'left', fontFamily: 'Inter,system-ui' }}>
      <div>
        <div style={{ fontSize: 14, color: danger ? T.danger : T.ink, fontWeight: 500 }}>{label}</div>
        {sub && <div style={{ fontSize: 11.5, color: T.inkFaint, marginTop: 2 }}>{sub}</div>}
      </div>
      <svg width="14" height="14" viewBox="0 0 18 18" fill="none"><path d="M7 4l5 5-5 5" stroke={T.inkFaint} strokeWidth="1.5" strokeLinecap="round" /></svg>
    </button>
  );
}

function RowLink({ T, label, href }) {
  return (
    <a href={href} target="_blank" rel="noopener" style={{ width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '13px 0', borderBottom: `1px solid ${T.hairline}`, textDecoration: 'none', fontFamily: 'Inter,system-ui' }}>
      <div style={{ fontSize: 14, color: T.ink, fontWeight: 500 }}>{label}</div>
      <svg width="14" height="14" viewBox="0 0 18 18" fill="none">
        <path d="M10 4h4v4" stroke={T.inkFaint} strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
        <path d="M14 4L8 10" stroke={T.inkFaint} strokeWidth="1.5" strokeLinecap="round" />
        <path d="M8 6H5a1 1 0 00-1 1v6a1 1 0 001 1h6a1 1 0 001-1v-3" stroke={T.inkFaint} strokeWidth="1.5" strokeLinecap="round" />
      </svg>
    </a>
  );
}

Object.assign(globalThis, { Upload, Correction, History, Profile });
