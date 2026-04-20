// Itadaki — SPA principale, routing + câblage REST
// Stratégie : squelette SPA inspiré v1, design visuel v2 intact
// React hooks (useState, useEffect, useMemo, useRef) deja declares dans primitives.jsx

// ─── Wrapper fetch global ────────────────────────────────────────────────────
const API = {
  async call(method, path, { body, isFormData } = {}) {
    const token = localStorage.getItem('itadaki.token');
    const headers = {};
    if (token) headers['Authorization'] = 'Bearer ' + token;
    if (!isFormData && body) headers['Content-Type'] = 'application/json';
    let res;
    try {
      res = await fetch(path, {
        method,
        headers,
        body: isFormData ? body : (body ? JSON.stringify(body) : undefined),
      });
    } catch (e) {
      throw { status: 0, body: 'Erreur réseau : ' + e.message };
    }
    if (res.status === 401) {
      localStorage.removeItem('itadaki.token');
      localStorage.removeItem('itadaki.user');
      location.reload();
      return;
    }
    if (!res.ok) {
      let txt;
      try { txt = await res.text(); } catch { txt = ''; }
      throw { status: res.status, body: txt };
    }
    if (res.status === 204) return null;
    return res.json();
  },

  auth: {
    register: (username, email, password) =>
      API.call('POST', '/api/auth/register', { body: { username, email, password } }),
    login: (email, password) =>
      API.call('POST', '/api/auth/login', { body: { email, password } }),
  },

  meals: {
    upload: (file) => {
      const fd = new FormData();
      fd.append('image', file);
      return API.call('POST', '/api/meals', { body: fd, isFormData: true });
    },
    get: (id) => API.call('GET', '/api/meals/' + id),
    del: (id) => API.call('DELETE', '/api/meals/' + id),
  },

  analyses: {
    analyze: (mealId) =>
      API.call('POST', '/api/analyses/' + mealId, { body: {} }),
    reanalyze: (mealId, hint) =>
      API.call('POST', '/api/analyses/' + mealId, { body: { hint } }),
    get: (mealId) => API.call('GET', '/api/analyses/' + mealId),
  },

  history: {
    list: (page = 0, size = 20) =>
      API.call('GET', '/api/history?page=' + page + '&size=' + size),
    byDate: (date) => API.call('GET', '/api/history/date/' + date),
  },

  stats: {
    overview: () => API.call('GET', '/api/stats/overview'),
    daily: (from, to) =>
      API.call('GET', '/api/stats/daily?from=' + from + '&to=' + to),
  },

  corrections: {
    create: (mealId, dto) =>
      API.call('POST', '/api/corrections/' + mealId, { body: dto }),
    get: (mealId) => API.call('GET', '/api/corrections/' + mealId),
  },
};

// ─── Utilitaires ─────────────────────────────────────────────────────────────

// Convertit un MealHistoryItemDto + MealAnalysisResponseDto en shape utilisée par les composants v2
function toViewMeal(histItem, analysis) {
  const mid = analysis
    ? Math.round(analysis.estimatedTotalCalories || 0)
    : Math.round(histItem.totalCalories || 0);
  const seed = histItem.id ? Number(histItem.id) % 99 + 1 : 42;
  const ing = analysis && analysis.detectedItems
    ? analysis.detectedItems.map(i => i.name || i.toString())
    : [];
  return {
    id: 'server-' + histItem.id,
    serverId: histItem.id,
    name: histItem.detectedDishName || analysis?.detectedDishName || 'Repas',
    ing,
    portion: 'moyen',
    kMin: Math.round(mid * 0.85),
    kMax: Math.round(mid * 1.15),
    conf: analysis ? Math.round((analysis.confidenceScore || 0.8) * 100) : 80,
    date: histItem.uploadedAt || new Date().toISOString(),
    meal: 'Repas',
    seed,
    photoUrl: histItem.photoUrl || null,
    analysisRaw: analysis,
  };
}

// last7 alimenté par les stats API (ou fallback vide)
function buildLast7(dailyStats) {
  const today = new Date();
  const labels = ['D', 'L', 'M', 'M', 'J', 'V', 'S'];
  return Array.from({ length: 7 }, (_, i) => {
    const d = new Date(today);
    d.setDate(d.getDate() - (6 - i));
    const ds = d.toISOString().slice(0, 10);
    const entry = dailyStats ? dailyStats.find(e => e.date === ds) : null;
    return {
      date: d,
      label: labels[d.getDay()],
      dayNum: d.getDate(),
      calories: entry ? Math.round(entry.totalCalories || 0) : 0,
      isToday: i === 6,
    };
  });
}

// ─── Écran Auth câblé ─────────────────────────────────────────────────────────
function AuthScreenWired({ T, onAuth, mobile }) {
  const [mode, setMode] = useState('signup');
  const [em, setEm] = useState('');
  const [un, setUn] = useState('');
  const [pw, setPw] = useState('');
  const [err, setErr] = useState({});
  const [ld, setLd] = useState(false);
  const [apiErr, setApiErr] = useState('');

  const go = async () => {
    const e = {};
    if (!em || !em.includes('@')) e.em = 'Email invalide';
    if (mode === 'signup' && (!un || un.length < 3)) e.un = 'Au moins 3 caractères';
    if (!pw || pw.length < 6) e.pw = '6 caractères minimum';
    setErr(e);
    if (Object.keys(e).length) return;
    setLd(true);
    setApiErr('');
    try {
      let res;
      if (mode === 'signup') {
        res = await API.auth.register(un, em, pw);
      } else {
        res = await API.auth.login(em, pw);
      }
      // AuthResponseDto : { accessToken, tokenType, expiresIn, user: { id, username, email } }
      const token = res.accessToken || res.token;
      const user = res.user;
      localStorage.setItem('itadaki.token', token);
      localStorage.setItem('itadaki.user', JSON.stringify(user));
      onAuth(user);
    } catch (ex) {
      // Ahmed utilise 404 pour "email déjà pris" (ResourceNotFoundException)
      if (ex.status === 404 || ex.status === 409 || ex.status === 400) {
        setApiErr('Email ou nom d\'utilisateur déjà utilisé.');
      } else if (ex.status === 401) {
        setApiErr('Email ou mot de passe incorrect.');
      } else {
        setApiErr('Erreur serveur (' + ex.status + '). Réessayez.');
      }
    } finally {
      setLd(false);
    }
  };

  const handleKey = (e) => { if (e.key === 'Enter') go(); };

  const formW = mobile ? '100%' : 420;
  return (
    <div style={{ minHeight: '100%', display: 'flex', flexDirection: mobile ? 'column' : 'row', background: T.bg }}>
      {/* Gauche — collage food */}
      <div style={{ flex: mobile ? '0 0 auto' : '1 1 50%', position: 'relative', minHeight: mobile ? 260 : '100%', background: T.bgAlt, overflow: 'hidden' }}>
        <svg viewBox="0 0 600 800" preserveAspectRatio="xMidYMid slice" style={{ position: 'absolute', inset: 0, width: '100%', height: '100%' }}>
          <path d="M-100,200 Q150,50 300,250 Q500,430 700,200 L700,-100 L-100,-100 Z" fill={T.accentSoft} />
          <path d="M-100,600 Q150,500 350,650 Q550,800 700,600 L700,900 L-100,900 Z" fill={T.matchaSoft} opacity="0.8" />
        </svg>
        <div style={{ position: 'absolute', top: '18%', left: '8%', width: mobile ? 140 : 180, aspectRatio: '1/1', transform: 'rotate(-6deg)' }}>
          <Dish seed={42} style={{ width: '100%', height: '100%', borderRadius: '50%', boxShadow: '0 24px 50px -10px rgba(80,40,10,.3)' }} />
        </div>
        <div style={{ position: 'absolute', top: mobile ? '10%' : '40%', right: mobile ? '6%' : '12%', width: mobile ? 120 : 200, aspectRatio: '1/1', transform: 'rotate(9deg)' }}>
          <Dish seed={13} style={{ width: '100%', height: '100%', borderRadius: '50%', boxShadow: '0 24px 50px -10px rgba(80,40,10,.25)' }} />
        </div>
        {!mobile && (
          <div style={{ position: 'absolute', bottom: '12%', left: '22%', width: 160, aspectRatio: '1/1', transform: 'rotate(3deg)' }}>
            <Dish seed={7} style={{ width: '100%', height: '100%', borderRadius: '50%', boxShadow: '0 24px 50px -10px rgba(80,40,10,.25)' }} />
          </div>
        )}
        <div style={{ position: 'absolute', top: mobile ? 20 : 32, left: mobile ? 20 : 40, display: 'flex', alignItems: 'center', gap: 10 }}>
          <div style={{ width: 36, height: 36, borderRadius: '14px 14px 14px 6px', background: T.accent, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: '"Fraunces",serif', fontSize: 22, fontStyle: 'italic', fontWeight: 500, transform: 'rotate(-4deg)' }}>i</div>
          <div style={{ fontFamily: '"Fraunces",serif', fontSize: 24, color: T.ink, letterSpacing: '-.03em', fontWeight: 500 }}>Itadaki</div>
        </div>
        {!mobile && (
          <div style={{ position: 'absolute', bottom: 32, left: 40, right: 40, fontFamily: '"Fraunces",serif', fontSize: 44, color: T.ink, letterSpacing: '-.03em', lineHeight: 1.05, fontWeight: 500 }}>
            Photographiez,<br /><span style={{ fontStyle: 'italic', color: T.accent }}>mangez mieux.</span>
          </div>
        )}
      </div>

      {/* Droite — formulaire */}
      <div style={{ flex: mobile ? '1 1 auto' : '1 1 50%', display: 'flex', alignItems: 'center', justifyContent: 'center', padding: mobile ? '26px 22px 32px' : '48px 40px' }}>
        <div style={{ width: formW, maxWidth: '100%' }}>
          {mobile && (
            <div style={{ fontFamily: '"Fraunces",serif', fontSize: 30, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.08, fontWeight: 500, marginBottom: 16 }}>
              Photographiez,<br /><span style={{ fontStyle: 'italic', color: T.accent }}>mangez mieux.</span>
            </div>
          )}
          <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginBottom: 22, lineHeight: 1.5 }}>
            Une photo de votre assiette — l'IA identifie le plat, les ingrédients et estime les calories en quelques secondes.
          </div>
          <div style={{ display: 'flex', padding: 4, background: T.bgAlt, borderRadius: 999, marginBottom: 22 }}>
            {[{ id: 'signup', l: "S'inscrire" }, { id: 'signin', l: 'Se connecter' }].map(t => (
              <button key={t.id} onClick={() => { setMode(t.id); setErr({}); setApiErr(''); }}
                style={{ flex: 1, padding: '10px 16px', border: 'none', background: mode === t.id ? T.surface : 'transparent', borderRadius: 999, fontFamily: 'Inter,system-ui', fontSize: 13, fontWeight: 600, color: mode === t.id ? T.ink : T.inkMuted, cursor: 'pointer', boxShadow: mode === t.id ? '0 1px 3px rgba(0,0,0,.06)' : 'none' }}>{t.l}</button>
            ))}
          </div>
          <Field T={T} label="Email" value={em} onChange={setEm} type="email" placeholder="vous@exemple.fr" error={err.em}
            icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="2" y="4" width="12" height="9" rx="1.5" stroke="currentColor" strokeWidth="1.4" /><path d="M2.5 5l5.5 4 5.5-4" stroke="currentColor" strokeWidth="1.4" /></svg>}
          />
          {mode === 'signup' && (
            <Field T={T} label="Nom d'utilisateur" value={un} onChange={setUn} placeholder="kenji_42" error={err.un}
              icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><circle cx="8" cy="6" r="2.6" stroke="currentColor" strokeWidth="1.4" /><path d="M3 14c.7-2.5 2.8-3.5 5-3.5s4.3 1 5 3.5" stroke="currentColor" strokeWidth="1.4" /></svg>}
            />
          )}
          <div onKeyDown={handleKey}>
            <Field T={T} label="Mot de passe" value={pw} onChange={setPw} type="password" placeholder="••••••••" error={err.pw} hint={mode === 'signup' ? '6 caractères minimum' : undefined}
              icon={<svg width="16" height="16" viewBox="0 0 16 16" fill="none"><rect x="3" y="7" width="10" height="7" rx="1.5" stroke="currentColor" strokeWidth="1.4" /><path d="M5 7V5a3 3 0 016 0v2" stroke="currentColor" strokeWidth="1.4" /></svg>}
            />
          </div>
          {apiErr && (
            <div style={{ padding: '10px 14px', background: 'oklch(0.95 0.03 25)', border: '1px solid ' + T.danger, borderRadius: 12, fontFamily: 'Inter,system-ui', fontSize: 13, color: T.danger, marginBottom: 14 }}>
              {apiErr}
            </div>
          )}
          <div style={{ marginTop: 8 }}>
            <Btn T={T} block onClick={go} disabled={ld} size="lg">
              {ld ? <><Spin size={14} /> Un instant…</> : (mode === 'signup' ? 'Créer mon compte' : 'Se connecter')}
            </Btn>
          </div>
          <div style={{ textAlign: 'center', fontSize: 11, color: T.inkFaint, marginTop: 18, fontFamily: 'Inter,system-ui', lineHeight: 1.5 }}>
            En continuant vous acceptez nos <span style={{ textDecoration: 'underline', color: T.inkMuted }}>conditions</span>. Les estimations ne sont pas des valeurs médicales.
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── Dashboard câblé ─────────────────────────────────────────────────────────
function DashboardWired({ T, user, onUpload, onHistory, onMeal, mobile }) {
  const [meals, setMeals] = useState([]);
  const [days, setDays] = useState(() => buildLast7(null));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const [histPage, dailyData] = await Promise.all([
          API.history.list(0, 20).catch(() => ({ content: [] })),
          (async () => {
            const today = new Date();
            const from = new Date(today); from.setDate(from.getDate() - 6);
            return API.stats.daily(
              from.toISOString().slice(0, 10),
              today.toISOString().slice(0, 10)
            ).catch(() => []);
          })(),
        ]);
        const items = histPage.content || [];
        const viewMeals = items.map(h => toViewMeal(h, null));
        setMeals(viewMeals);
        setDays(buildLast7(dailyData));
      } catch (e) {
        console.warn('Dashboard load error', e);
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  if (loading) {
    return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: T.inkMuted, fontFamily: 'Inter,system-ui', fontSize: 14 }}>Chargement…</div>;
  }

  // On réutilise le composant Dashboard de v2/screens.jsx en lui passant les données réelles
  return <Dashboard T={T} user={user} meals={meals} days_override={days}
    onUpload={onUpload} onHistory={onHistory} onMeal={onMeal} mobile={mobile} />;
}

// ─── Upload câblé ─────────────────────────────────────────────────────────────
function UploadWired({ T, onCancel, onAnalyzed, mobile }) {
  const [stage, setStage] = useState('idle');
  const [img, setImg] = useState(null);
  const [file, setFile] = useState(null);
  const [drag, setDrag] = useState(false);
  const [prog, setProg] = useState(0);
  const [loadVar] = useState(() => Math.floor(Math.random() * 3));
  const [apiErr, setApiErr] = useState('');
  const fileRef = useRef();

  const pick = (f) => {
    if (!f) return;
    setFile(f);
    const r = new FileReader();
    r.onload = e => { setImg(e.target.result); setStage('preview'); };
    r.readAsDataURL(f);
  };

  const analyze = async () => {
    setStage('loading');
    setProg(10);
    setApiErr('');
    try {
      // Étape 1 — upload image
      const uploadRes = await API.meals.upload(file);
      const mealId = uploadRes.mealId;
      setProg(40);

      // Étape 2 — déclenche l'analyse IA
      const analysisRes = await API.analyses.analyze(mealId);
      setProg(100);

      // Convertit en shape v2
      const pseudo = {
        id: 'new-' + mealId,
        serverId: mealId,
        name: analysisRes.detectedDishName || 'Repas analysé',
        ing: (analysisRes.detectedItems || []).map(i => i.name || String(i)),
        portion: 'moyen',
        kMin: Math.round((analysisRes.estimatedTotalCalories || 500) * 0.85),
        kMax: Math.round((analysisRes.estimatedTotalCalories || 500) * 1.15),
        conf: Math.round((analysisRes.confidenceScore || 0.8) * 100),
        date: analysisRes.analyzedAt || new Date().toISOString(),
        meal: 'Repas',
        seed: mealId % 99 + 1,
        img,
        mealId,
        analysisRaw: analysisRes,
      };
      setTimeout(() => onAnalyzed(pseudo), 300);
    } catch (e) {
      setApiErr('Erreur lors de l\'analyse : ' + (e.body || e.message || 'serveur indisponible'));
      setStage('preview');
      setProg(0);
    }
  };

  if (stage === 'loading') {
    const variants = [
      <div key="v1" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 26 }}>
        <div style={{ position: 'relative', width: mobile ? 180 : 220, height: mobile ? 180 : 220 }}>
          {img && img !== 'PLACEHOLDER'
            ? <img src={img} style={{ position: 'absolute', inset: 14, borderRadius: '50%', width: 'calc(100% - 28px)', height: 'calc(100% - 28px)', objectFit: 'cover' }} />
            : <Dish seed={13} style={{ position: 'absolute', inset: 14, borderRadius: '50%' }} />}
          <svg width="100%" height="100%" viewBox="0 0 220 220" style={{ position: 'absolute', inset: 0, animation: 'spin 3s linear infinite' }}>
            <circle cx="110" cy="110" r="104" fill="none" stroke={T.accent} strokeWidth="3" strokeDasharray="10 14" strokeLinecap="round" />
          </svg>
          <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ width: 46, height: 46, borderRadius: '50%', background: 'rgba(255,255,255,.95)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 14px rgba(0,0,0,.2)' }}>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 13, color: '#222', fontWeight: 600 }}>{Math.round(prog)}%</div>
            </div>
          </div>
        </div>
      </div>,
      <div key="v2" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 18, width: '100%', maxWidth: 360 }}>
        <Dish seed={13} style={{ width: 120, height: 120, borderRadius: '50%' }} />
        <div style={{ width: '100%', background: T.bgAlt, borderRadius: 18, padding: 18, fontFamily: 'JetBrains Mono,monospace', fontSize: 13, color: T.inkMuted }}>
          {['→ upload en cours…', '→ plat détecté', '→ ingrédients identifiés', '→ calories estimées'].slice(0, Math.min(4, Math.ceil(prog / 25))).map((l, i) => <div key={i} style={{ padding: '3px 0' }}>{l}</div>)}
          <div style={{ height: 3, background: T.hairline, borderRadius: 2, marginTop: 10, overflow: 'hidden' }}><div style={{ width: prog + '%', height: '100%', background: T.accent, transition: 'width .3s' }} /></div>
        </div>
      </div>,
      <div key="v3" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 22, width: '100%', maxWidth: 360 }}>
        <div style={{ position: 'relative', width: 160, height: 160 }}>
          {[0, 1, 2, 3, 4].map(i => <div key={i} style={{ position: 'absolute', left: '50%', top: '50%', width: 10, height: 10, borderRadius: '50%', background: i % 2 ? T.accent : T.matcha, transform: 'translate(-50%,-50%) rotate(' + (i * 72) + 'deg) translateY(-60px)', animation: 'breathe 1.' + (4 + i) + 's ease-in-out infinite', animationDelay: (i * 0.1) + 's' }} />)}
          <div style={{ position: 'absolute', inset: 30 }}><Dish seed={13} style={{ width: '100%', height: '100%', borderRadius: '50%' }} /></div>
        </div>
        <div style={{ width: '100%', height: 6, background: T.hairline, borderRadius: 3, overflow: 'hidden' }}>
          <div style={{ width: prog + '%', height: '100%', background: 'linear-gradient(90deg,' + T.accent + ',' + T.matcha + ')', borderRadius: 3, transition: 'width .3s' }} />
        </div>
      </div>,
    ];
    const captions = ['Upload de la photo…', 'On observe votre assiette…', 'On identifie les ingrédients…', 'On fait le calcul…'];
    const idx = Math.min(3, Math.floor(prog / 25));
    return (
      <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px 24px', background: T.bg, color: T.ink, textAlign: 'center' }}>
        {variants[loadVar]}
        <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 20 : 24, letterSpacing: '-.02em', marginTop: 30, fontStyle: 'italic', fontWeight: 500 }}>{captions[idx]}</div>
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

      {apiErr && (
        <div style={{ padding: '10px 14px', background: 'oklch(0.95 0.03 25)', border: '1px solid ' + T.danger, borderRadius: 12, fontFamily: 'Inter,system-ui', fontSize: 13, color: T.danger, marginBottom: 14 }}>
          {apiErr}
        </div>
      )}

      {stage === 'idle' && (
        <>
          <div
            onDragOver={e => { e.preventDefault(); setDrag(true); }}
            onDragLeave={() => setDrag(false)}
            onDrop={e => { e.preventDefault(); setDrag(false); pick(e.dataTransfer.files[0]); }}
            onClick={() => fileRef.current.click()}
            style={{ border: '2px dashed ' + (drag ? T.accent : T.hairline), borderRadius: 24, padding: mobile ? '34px 18px' : '52px 28px', textAlign: 'center', background: drag ? T.accentSoft : T.bgAlt, cursor: 'pointer', transition: 'all .2s', position: 'relative', overflow: 'hidden' }}>
            <svg viewBox="0 0 600 200" preserveAspectRatio="none" style={{ position: 'absolute', inset: 0, width: '100%', height: '100%', opacity: .4, pointerEvents: 'none' }}>
              <path d="M0,130 Q150,80 300,120 Q450,160 600,100 L600,200 L0,200 Z" fill={T.matchaSoft} />
            </svg>
            <div style={{ position: 'relative' }}>
              <div style={{ width: 72, height: 72, margin: '0 auto 16px', borderRadius: 24, background: T.surface, display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 6px 18px rgba(0,0,0,.06)' }}>
                <svg width="32" height="32" viewBox="0 0 28 28" fill="none"><rect x="4" y="7" width="20" height="16" rx="2" stroke={T.accent} strokeWidth="1.7" /><circle cx="14" cy="15" r="4.5" stroke={T.accent} strokeWidth="1.7" /><path d="M9 7l1.5-2.5h7L19 7" stroke={T.accent} strokeWidth="1.7" strokeLinejoin="round" /></svg>
              </div>
              <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 20 : 22, color: T.ink, letterSpacing: '-.02em', fontWeight: 500 }}>Glissez une photo ici</div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginTop: 6 }}>ou cliquez pour parcourir · JPG, PNG, HEIC · 10 Mo max</div>
            </div>
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={e => pick(e.target.files[0])} />
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 14, flexWrap: 'wrap' }}>
            <Btn T={T} variant="soft" onClick={() => fileRef.current.click()} icon={<svg width="14" height="14" viewBox="0 0 16 16" fill="none"><rect x="2" y="4" width="12" height="10" rx="1.5" stroke="currentColor" strokeWidth="1.5" /><circle cx="8" cy="9" r="2.8" stroke="currentColor" strokeWidth="1.5" /></svg>}>Prendre une photo</Btn>
          </div>
        </>
      )}

      {stage === 'preview' && (
        <>
          <div style={{ borderRadius: 24, overflow: 'hidden', background: T.bgAlt, position: 'relative', aspectRatio: '4/3' }}>
            <img src={img} alt="Aperçu" style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 14 }}>
            <Btn T={T} variant="ghost" onClick={() => { setStage('idle'); setImg(null); setFile(null); setApiErr(''); }}>Remplacer</Btn>
            <Btn T={T} onClick={analyze} block size="lg">Analyser →</Btn>
          </div>
        </>
      )}
    </div>
  );
}

// ─── Correction câblée (2ᵉ passe LLM) ───────────────────────────────────────
function CorrectionWired({ T, meal, onSave, onCancel, mobile }) {
  const [hint, setHint] = useState('');
  const [ld, setLd] = useState(false);
  const [reanalyzed, setReanalyzed] = useState(null);
  const [apiErr, setApiErr] = useState('');

  const doReanalyze = async () => {
    if (!hint.trim() || !meal.mealId) return;
    setLd(true);
    setApiErr('');
    try {
      const res = await API.analyses.reanalyze(meal.mealId, hint.trim());
      // Met à jour localement l'affichage de la correction
      const updated = {
        ...meal,
        name: res.detectedDishName || meal.name,
        ing: (res.detectedItems || []).map(i => i.name || String(i)),
        kMin: Math.round((res.estimatedTotalCalories || 0) * 0.85),
        kMax: Math.round((res.estimatedTotalCalories || 0) * 1.15),
        conf: Math.round((res.confidenceScore || 0.8) * 100),
        analysisRaw: res,
      };
      setReanalyzed(updated);
    } catch (e) {
      setApiErr('Erreur lors de la 2ᵉ passe : ' + (e.body || 'serveur indisponible'));
    } finally {
      setLd(false);
    }
  };

  const displayMeal = reanalyzed || meal;

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
          {displayMeal.img && displayMeal.img !== 'PLACEHOLDER'
            ? <img src={displayMeal.img} style={{ width: '100%', aspectRatio: '1/1', objectFit: 'cover', borderRadius: 24, boxShadow: '0 10px 30px -10px rgba(80,40,10,.3)' }} />
            : <Dish seed={displayMeal.seed} style={{ width: '100%', aspectRatio: '1/1', boxShadow: '0 10px 30px -10px rgba(80,40,10,.3)' }} rounded={24} />
          }
          <div style={{ marginTop: 14, padding: 14, background: T.accentSoft, borderRadius: 16 }}>
            <Confidence value={displayMeal.conf} T={T} />
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.accentDeep, marginTop: 10, lineHeight: 1.5 }}>
              {reanalyzed ? 'Analyse mise à jour avec votre correction.' : 'Les détails ont été estimés par l\'IA. Vous pouvez fournir une indication ci-contre pour une 2ᵉ passe.'}
            </div>
          </div>
        </div>

        <div>
          <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 26 : 30, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.1, marginBottom: 4, fontWeight: 500 }}>
            {displayMeal.name}
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 20 }}>
            {displayMeal.ing.map((i, idx) => <Chip key={idx} T={T} variant="default">{i}</Chip>)}
          </div>
          <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 18, color: T.ink, fontWeight: 600, marginBottom: 6 }}>
            {displayMeal.kMin}–{displayMeal.kMax} <span style={{ fontSize: 13, color: T.inkFaint, fontFamily: 'Inter,system-ui' }}>kcal estimées</span>
          </div>

          {/* 2ᵉ passe LLM — BF3 */}
          <div style={{ marginTop: 20, padding: 18, background: T.bgAlt, borderRadius: 18, border: '1px solid ' + T.hairline }}>
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkMuted, marginBottom: 8 }}>
              L'IA s'est trompée ? Donnez-lui une indication
            </div>
            <textarea
              value={hint}
              onChange={e => setHint(e.target.value)}
              placeholder="Ex: en fait c'est du tofu, pas du poulet. La portion est petite."
              rows={3}
              style={{ width: '100%', padding: '12px 14px', background: T.surface, border: '1px solid ' + T.hairline, borderRadius: 12, fontFamily: 'Inter,system-ui', fontSize: 14, color: T.ink, resize: 'vertical', boxSizing: 'border-box', outline: 'none' }}
            />
            {apiErr && <div style={{ fontSize: 12, color: T.danger, marginTop: 6, fontFamily: 'Inter,system-ui' }}>{apiErr}</div>}
            <Btn T={T} variant="soft" onClick={doReanalyze} disabled={ld || !hint.trim()} style={{ marginTop: 10 }}>
              {ld ? <><Spin size={13} color={T.accentDeep} /> 2ᵉ passe en cours…</> : 'Relancer l\'analyse'}
            </Btn>
          </div>

          <div style={{ marginTop: 22, display: 'flex', gap: 10 }}>
            <Btn T={T} variant="ghost" onClick={onCancel}>Annuler</Btn>
            <Btn T={T} onClick={() => onSave(displayMeal)} block size="lg">Enregistrer le repas</Btn>
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── History câblée ───────────────────────────────────────────────────────────
function HistoryWired({ T, onMeal, mobile }) {
  const [meals, setMeals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [filter, setFilter] = useState('all');

  const load = async (p = 0) => {
    setLoading(true);
    try {
      const res = await API.history.list(p, 20);
      const items = (res.content || []).map(h => toViewMeal(h, null));
      setMeals(prev => p === 0 ? items : [...prev, ...items]);
      setTotalPages(res.totalPages || 1);
      setPage(p);
    } catch (e) {
      console.warn('History load error', e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(0); }, []);

  const filtered = filter === 'all' ? meals : meals.filter(m => m.meal === filter);

  // Groupement par date
  const groups = {};
  filtered.forEach(m => {
    const k = (m.date || '').slice(0, 10);
    (groups[k] = groups[k] || []).push(m);
  });
  const days = Object.keys(groups).sort().reverse();

  const todayStr = new Date().toISOString().slice(0, 10);
  const yesterStr = (() => { const d = new Date(); d.setDate(d.getDate() - 1); return d.toISOString().slice(0, 10); })();
  const dayName = k => {
    if (k === todayStr) return "Aujourd'hui";
    if (k === yesterStr) return 'Hier';
    try { return new Date(k + 'T12:00:00').toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' }); }
    catch { return k; }
  };

  return (
    <div style={{ maxWidth: 820, margin: '0 auto' }}>
      <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 28 : 36, color: T.ink, letterSpacing: '-.03em', lineHeight: 1.08, fontWeight: 500, marginBottom: 6 }}>
        <span style={{ fontStyle: 'italic' }}>Historique</span>
      </div>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginBottom: 20 }}>{meals.length} repas enregistrés</div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20, overflowX: 'auto', paddingBottom: 4 }}>
        {[{ id: 'all', l: 'Tout' }, { id: 'Petit-déj.', l: 'Petit-déj.' }, { id: 'Déjeuner', l: 'Déjeuner' }, { id: 'Dîner', l: 'Dîner' }].map(f => (
          <button key={f.id} onClick={() => setFilter(f.id)}
            style={{ padding: '7px 14px', background: filter === f.id ? T.ink : T.bgAlt, color: filter === f.id ? T.bg : T.inkMuted, border: 'none', borderRadius: 999, fontFamily: 'Inter,system-ui', fontSize: 12.5, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap', flexShrink: 0 }}>{f.l}</button>
        ))}
      </div>

      {loading && meals.length === 0 && (
        <div style={{ textAlign: 'center', padding: 40, color: T.inkMuted, fontFamily: 'Inter,system-ui', fontSize: 14 }}>Chargement…</div>
      )}

      {!loading && meals.length === 0 && (
        <div style={{ textAlign: 'center', padding: 40 }}>
          <div style={{ fontFamily: '"Fraunces",serif', fontSize: 22, color: T.ink, fontStyle: 'italic' }}>Aucun repas encore</div>
          <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginTop: 8 }}>Analysez votre premier repas pour commencer.</div>
        </div>
      )}

      {days.map(k => {
        const dayMeals = groups[k];
        const total = dayMeals.reduce((s, m) => s + (m.kMin + m.kMax) / 2, 0);
        return (
          <div key={k} style={{ marginBottom: 24 }}>
            <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: 10, paddingBottom: 8, borderBottom: '1px solid ' + T.hairline }}>
              <div style={{ fontFamily: '"Fraunces",serif', fontSize: 17, color: T.ink, letterSpacing: '-.01em', fontWeight: 500, textTransform: 'capitalize' }}>{dayName(k)}</div>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 12, color: T.inkMuted }}>{Math.round(total)} kcal</div>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: mobile ? '1fr' : '1fr 1fr', gap: 10 }}>
              {dayMeals.map(m => <MealRow key={m.id} m={m} T={T} onClick={() => onMeal(m)} />)}
            </div>
          </div>
        );
      })}

      {page + 1 < totalPages && (
        <div style={{ textAlign: 'center', marginTop: 16 }}>
          <Btn T={T} variant="ghost" onClick={() => load(page + 1)} disabled={loading}>
            {loading ? 'Chargement…' : 'Charger plus'}
          </Btn>
        </div>
      )}
    </div>
  );
}

// ─── Profile câblé ────────────────────────────────────────────────────────────
function ProfileWired({ T, user, onLogout, mobile, dark, setDark }) {
  const handleLogout = () => {
    localStorage.removeItem('itadaki.token');
    localStorage.removeItem('itadaki.user');
    onLogout();
  };

  return <Profile T={T} user={user} onLogout={handleLogout} mobile={mobile} dark={dark} setDark={setDark} />;
}

// ─── App principale — Router SPA ──────────────────────────────────────────────
function App() {
  const [dark, setDark] = useState(() => {
    try { return JSON.parse(localStorage.getItem('itadaki.dark')) ?? false; } catch { return false; }
  });
  const T = dark ? THEMES.dark : THEMES.light;

  useEffect(() => {
    document.documentElement.style.setProperty('--page-bg', T.pageBg);
    document.documentElement.style.setProperty('--scroll', T.scroll);
    localStorage.setItem('itadaki.dark', JSON.stringify(dark));
  }, [dark]);

  // État auth
  const [user, setUser] = useState(() => {
    try {
      const u = localStorage.getItem('itadaki.user');
      return u ? JSON.parse(u) : null;
    } catch { return null; }
  });

  // Router
  const [screen, setScreen] = useState(() => {
    const s = localStorage.getItem('itadaki.screen');
    return s || 'auth';
  });
  const [currentMeal, setCurrentMeal] = useState(null);

  useEffect(() => {
    localStorage.setItem('itadaki.screen', screen);
  }, [screen]);

  const onAuth = (u) => {
    setUser(u);
    setScreen('dashboard');
  };

  const onLogout = () => {
    setUser(null);
    setScreen('auth');
    localStorage.removeItem('itadaki.screen');
  };

  const onAnalyzed = (meal) => {
    setCurrentMeal(meal);
    setScreen('correction');
  };

  const onSaved = () => {
    setCurrentMeal(null);
    setScreen('dashboard');
  };

  // Responsive : détecte mobile
  const [isMobile, setIsMobile] = useState(() => window.innerWidth < 768);
  useEffect(() => {
    const h = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', h);
    return () => window.removeEventListener('resize', h);
  }, []);

  // Si non connecté, forcer auth
  const effectiveScreen = (!user && screen !== 'auth') ? 'auth' : screen;

  let content;
  if (effectiveScreen === 'auth') {
    content = <AuthScreenWired T={T} onAuth={onAuth} mobile={isMobile} />;
  } else if (effectiveScreen === 'dashboard') {
    content = (
      <Shell T={T} mobile={isMobile} active="dashboard" onNav={id => setScreen(id)} user={user}>
        <DashboardWired T={T} user={user} onUpload={() => setScreen('upload')} onHistory={() => setScreen('history')} onMeal={m => { setCurrentMeal(m); setScreen('correction'); }} mobile={isMobile} />
      </Shell>
    );
  } else if (effectiveScreen === 'upload') {
    content = (
      <Shell T={T} mobile={isMobile} active="upload" onNav={id => setScreen(id)} user={user} pad={false}>
        <div style={{ padding: isMobile ? '16px 18px' : '28px 40px', overflowY: 'auto', height: '100%' }}>
          <UploadWired T={T} onCancel={() => setScreen('dashboard')} onAnalyzed={onAnalyzed} mobile={isMobile} />
        </div>
      </Shell>
    );
  } else if (effectiveScreen === 'correction' && currentMeal) {
    content = (
      <Shell T={T} mobile={isMobile} active="upload" onNav={id => setScreen(id)} user={user} pad={false}>
        <div style={{ padding: isMobile ? '16px 18px' : '28px 40px', overflowY: 'auto', height: '100%' }}>
          <CorrectionWired T={T} meal={currentMeal} onSave={onSaved} onCancel={() => setScreen('dashboard')} mobile={isMobile} />
        </div>
      </Shell>
    );
  } else if (effectiveScreen === 'history') {
    content = (
      <Shell T={T} mobile={isMobile} active="history" onNav={id => setScreen(id)} user={user}>
        <HistoryWired T={T} onMeal={m => { setCurrentMeal(m); setScreen('correction'); }} mobile={isMobile} />
      </Shell>
    );
  } else if (effectiveScreen === 'profile') {
    content = (
      <Shell T={T} mobile={isMobile} active="profile" onNav={id => setScreen(id)} user={user}>
        <ProfileWired T={T} user={user} onLogout={onLogout} mobile={isMobile} dark={dark} setDark={setDark} />
      </Shell>
    );
  } else {
    // Fallback
    setScreen('dashboard');
    content = null;
  }

  return (
    <div style={{
      minHeight: '100vh',
      background: T.pageBg,
      color: T.ink,
      fontFamily: 'Inter, -apple-system, system-ui, sans-serif',
      WebkitFontSmoothing: 'antialiased',
    }}>
      {content}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
