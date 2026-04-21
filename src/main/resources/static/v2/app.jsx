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
    setType: (id, mealType) => API.call('PATCH', '/api/meals/' + id + '/type', { body: { mealType } }),
  },

  analyses: {
    analyze: (mealId) =>
      API.call('POST', '/api/analyses/' + mealId, { body: {} }),
    reanalyze: (mealId, hint) =>
      API.call('POST', '/api/analyses/' + mealId, { body: { hint } }),
    get: (mealId) => API.call('GET', '/api/analyses/' + mealId),
    // Streaming NDJSON : fetch + ReadableStream parse ligne par ligne.
    // Le back emet {type:"token"|"complete"|"error"} par ligne.
    // Resout avec l'analyse finale, rejette si error event ou reseau KO.
    stream: async (mealId, hint, onToken) => {
      const token = localStorage.getItem('itadaki.token');
      const res = await fetch('/api/analyses/stream/' + mealId, {
        method: 'POST',
        headers: {
          'Authorization': 'Bearer ' + token,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(hint ? { hint } : {}),
      });
      if (!res.ok) {
        let body = ''; try { body = await res.text(); } catch {}
        throw { status: res.status, body };
      }
      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let finalAnalysis = null;
      while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        let nl;
        while ((nl = buffer.indexOf('\n')) >= 0) {
          const line = buffer.slice(0, nl).trim();
          buffer = buffer.slice(nl + 1);
          if (!line) continue;
          try {
            const msg = JSON.parse(line);
            if (msg.type === 'token') onToken?.(msg.content);
            else if (msg.type === 'complete') finalAnalysis = msg.analysis;
            else if (msg.type === 'error') throw { body: msg.message || 'stream error' };
          } catch (e) {
            if (e && e.body) throw e; // vraie erreur metier propagee
            // sinon ligne non-parsable, on ignore
          }
        }
      }
      if (!finalAnalysis) throw { body: 'Stream termine sans resultat' };
      return finalAnalysis;
    },
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
    streak: () => API.call('GET', '/api/stats/streak'),
    weeklySummary: () => API.call('GET', '/api/stats/weekly-summary'),
    mealSuggestion: () => API.call('GET', '/api/stats/meal-suggestion'),
  },

  corrections: {
    create: (mealId, dto) =>
      API.call('POST', '/api/corrections/' + mealId, { body: dto }),
    get: (mealId) => API.call('GET', '/api/corrections/' + mealId),
  },

  admin: {
    users: (page = 0, size = 20) => API.call('GET', `/api/admin/users?page=${page}&size=${size}`),
    deleteUser: (id) => API.call('DELETE', '/api/admin/users/' + id),
    setRole: (id, role) => API.call('PATCH', '/api/admin/users/' + id + '/role', { body: { role } }),
    meals: (page = 0, size = 20) => API.call('GET', `/api/admin/meals?page=${page}&size=${size}`),
    deleteMeal: (id) => API.call('DELETE', '/api/admin/meals/' + id),
    stats: () => API.call('GET', '/api/admin/stats'),
  },
};

// ─── Utilitaires ─────────────────────────────────────────────────────────────
// healthScore est defini dans screens.jsx (charge avant app.jsx) et accessible
// via le scope global partage. Ne pas redefinir ici pour eviter la divergence.

// Label FR d'un MealType enum back (BREAKFAST/LUNCH/SNACK/DINNER)
const MEAL_TYPE_LABELS = { BREAKFAST: 'Petit-déj', LUNCH: 'Déjeuner', SNACK: 'Goûter', DINNER: 'Dîner' };

// Id de meal : harmonise les 3 shapes possibles (histItem.id, pseudo uploadRes.mealId, serverId)
const getMealId = (m) => m && (m.mealId || m.serverId || m.id);

// Fourchette kcal ±15% (le back ne persiste que le max, on reconstruit un min/max d'affichage)
const kcalRange = (cal) => ({ kMin: Math.round(cal * 0.85), kMax: Math.round(cal * 1.15) });

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
    mealId: histItem.id,
    name: histItem.detectedDishName || analysis?.detectedDishName || 'Repas',
    ing,
    portion: 'moyen',
    ...kcalRange(mid),
    conf: analysis ? Math.round((analysis.confidenceScore || 0.8) * 100) : 80,
    date: histItem.uploadedAt || new Date().toISOString(),
    mealType: histItem.mealType || null,
    meal: MEAL_TYPE_LABELS[histItem.mealType] || 'Repas',
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
      // 409 Conflict = email/username deja pris (depuis fix GlobalExceptionHandler)
      // 404 = legacy (avant fix : Ahmed utilisait ResourceNotFoundException)
      if (ex.status === 409 || ex.status === 404) {
        setApiErr('Email ou nom d\'utilisateur déjà utilisé.');
      } else if (ex.status === 400) {
        // Validation Bean (password court, email invalide, username bad format)
        // Le back renvoie un message specifique dans body
        let msg = 'Données invalides.';
        try {
          const parsed = JSON.parse(ex.body || '{}');
          if (parsed.message) msg = parsed.message;
        } catch { /* body pas JSON */ }
        setApiErr(msg);
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
  const [overview, setOverview] = useState(null);
  const [streak, setStreak] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const [histPage, dailyData, overviewData, streakData] = await Promise.all([
          API.history.list(0, 20).catch(() => ({ content: [] })),
          (async () => {
            const today = new Date();
            const from = new Date(today); from.setDate(from.getDate() - 6);
            return API.stats.daily(
              from.toISOString().slice(0, 10),
              today.toISOString().slice(0, 10)
            ).catch(() => []);
          })(),
          API.stats.overview().catch(() => null),
          API.stats.streak().catch(() => null),
        ]);
        const items = histPage.content || [];
        const viewMeals = items.map(h => toViewMeal(h, null));
        setMeals(viewMeals);
        setDays(buildLast7(dailyData));
        setOverview(overviewData);
        setStreak(streakData);
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
  return <Dashboard T={T} user={user} meals={meals} days_override={days} overview={overview}
    streak={streak} onUpload={onUpload} onHistory={onHistory} onMeal={onMeal} mobile={mobile} />;
}

// ─── Helpers upload ──────────────────────────────────────────────────────────
// Redimensionne + compresse en JPEG pour reduire le payload multipart et
// la latence tunnel ngrok (photos smartphone modernes = 15-30 Mo, Qwen2.5-VL
// accepte 1280px large). Garde l'extension .jpg pour que ValidImageFile passe.
async function resizeForUpload(file, maxSide = 1280, quality = 0.85) {
  if (!file || !file.type || !file.type.startsWith('image/')) return file;
  // Petites images : passe direct (< 2 Mo, pas d'agreg)
  if (file.size < 2 * 1024 * 1024) return file;
  const bitmap = await createImageBitmap(file);
  const ratio = Math.min(maxSide / bitmap.width, maxSide / bitmap.height, 1);
  const w = Math.round(bitmap.width * ratio);
  const h = Math.round(bitmap.height * ratio);
  const canvas = document.createElement('canvas');
  canvas.width = w; canvas.height = h;
  canvas.getContext('2d').drawImage(bitmap, 0, 0, w, h);
  const blob = await new Promise(res => canvas.toBlob(res, 'image/jpeg', quality));
  if (!blob) return file; // fallback
  const newName = file.name.replace(/\.[^.]+$/, '') + '.jpg';
  return new File([blob], newName, { type: 'image/jpeg' });
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
  // Tokens Ollama accumules pendant le streaming (mode #1 wow : brain dump live)
  const [liveTokens, setLiveTokens] = useState('');
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
    setLiveTokens('');
    try {
      // Etape 0 — resize + compress pour reduire le payload (photos smartphone 16-30Mo).
      // Qwen2.5-VL accepte 1280px large largement. JPEG 0.85 => ~300-600Ko.
      const compressed = await resizeForUpload(file);

      // Etape 1 — upload image (compressee)
      const uploadRes = await API.meals.upload(compressed);
      const mealId = uploadRes.mealId;
      setProg(20);

      // Etape 2 — streaming : les tokens qwen2.5vl arrivent en direct via NDJSON.
      // Progression basee sur la longueur du JSON typique (~500 chars).
      // En cas de KO stream (ngrok buffer, CORS, etc.), fallback polling.
      let analysisRes = null;
      try {
        let acc = '';
        analysisRes = await API.analyses.stream(mealId, null, (token) => {
          acc += token;
          setLiveTokens(acc);
          setProg(20 + Math.min(75, Math.floor(acc.length / 7)));
        });
      } catch (streamErr) {
        console.warn('Stream fail, fallback polling:', streamErr);
        // Fallback : l'ancien flow fire-and-forget + polling
        let postErr = null;
        API.analyses.analyze(mealId).catch(e => { postErr = e; });
        for (let i = 0; i < 150; i++) {
          await new Promise(r => setTimeout(r, 2000));
          try {
            const r = await API.analyses.get(mealId);
            if (r && r.id) { analysisRes = r; break; }
          } catch (e) {
            if (e.status !== 404) throw e;
          }
          if (postErr && postErr.status && postErr.status !== 0 && postErr.status !== 504) {
            throw postErr;
          }
          setProg(30 + Math.min(65, i));
        }
        if (!analysisRes) throw { status: 504, body: 'Analyse trop longue (>5 min). Reessayez.' };
      }
      setProg(100);

      // Convertit en shape v2. mealType auto-detecte par heure (fallback
      // si le back n'a pas renvoye la valeur persistee).
      const mtNow = detectMealType();
      const pseudo = {
        id: 'new-' + mealId,
        serverId: mealId,
        name: analysisRes.detectedDishName || 'Repas analyse',
        ing: (analysisRes.detectedItems || []).map(i => i.name || String(i)),
        portion: 'moyen',
        ...kcalRange(analysisRes.estimatedTotalCalories || 500),
        conf: Math.round((analysisRes.confidenceScore || 0.8) * 100),
        date: analysisRes.analyzedAt || new Date().toISOString(),
        mealType: mtNow,
        meal: MEAL_TYPE_LABELS[mtNow],
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
    // Stepper "Qwen pense..." : 3 phases pedagogiques qui rendent le raisonnement IA visible
    // Phase 1 (0-33%)  : observation -> scan line qui balaye l'image floutee
    // Phase 2 (33-66%) : identification -> ingredients plausibles s'affichent un par un
    // Phase 3 (66-100%): calcul -> chiffre kcal qui defile type slot machine
    const phase = prog < 33 ? 0 : prog < 66 ? 1 : 2;
    const hasImg = img && img !== 'PLACEHOLDER';
    const fakeIngredients = ['légumes verts', 'protéines animales', 'féculents', 'matières grasses', 'épices', 'sauce'];
    const visibleIngredients = Math.min(fakeIngredients.length, Math.max(0, Math.floor((prog - 33) / 5)));
    const fakeCalories = 180 + ((Math.floor(prog * 7) * 37) % 820);

    const phases = [
      { icon: '🧠', title: 'Qwen observe l\'image…', detail: 'Décodage visuel multimodal', color: T.accent },
      { icon: '🔍', title: 'Identification des ingrédients…', detail: 'Segmentation et reconnaissance', color: '#7fa644' },
      { icon: '🧮', title: 'Calcul des apports nutritionnels…', detail: 'Agrégation calories + macros', color: '#d4a13c' },
    ];

    return (
      <div style={{ minHeight: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '40px 24px', background: T.bg, color: T.ink, textAlign: 'center' }}>
        <style>{`
          @keyframes scanline { 0%{top:0;opacity:0}15%{opacity:1}85%{opacity:1}100%{top:100%;opacity:0} }
          @keyframes fadein   { from{opacity:0;transform:translateY(-4px)} to{opacity:1;transform:translateY(0)} }
          @keyframes slotPulse{ 0%,100%{transform:translateY(0)} 50%{transform:translateY(-1px)} }
        `}</style>

        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 24 }}>
          {/* Image + anneau + scan line (phase 1) + % central */}
          <div style={{ position: 'relative', width: mobile ? 200 : 240, height: mobile ? 200 : 240 }}>
            <div style={{ position: 'absolute', inset: 14, borderRadius: '50%', overflow: 'hidden' }}>
              {hasImg
                ? <img src={img} alt="repas en cours d'analyse" style={{
                    width: '100%', height: '100%', objectFit: 'cover',
                    filter: 'blur(3px) brightness(0.92)', transition: 'filter .6s ease',
                  }} />
                : <Dish seed={13} style={{ width: '100%', height: '100%', borderRadius: '50%' }} />
              }
              {/* Scan line — visible en phase 1 uniquement */}
              {phase === 0 && <div style={{
                position: 'absolute', left: 0, right: 0, height: 4,
                background: 'linear-gradient(90deg, transparent, ' + T.accent + ', transparent)',
                boxShadow: '0 0 16px ' + T.accent,
                animation: 'scanline 1.8s linear infinite',
              }} />}
            </div>
            <svg width="100%" height="100%" viewBox="0 0 220 220" style={{ position: 'absolute', inset: 0, animation: 'spin 3s linear infinite' }}>
              <circle cx="110" cy="110" r="104" fill="none" stroke={phases[phase].color} strokeWidth="3" strokeDasharray="10 14" strokeLinecap="round" />
            </svg>
            <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <div style={{ width: 56, height: 56, borderRadius: '50%', background: 'rgba(255,255,255,.96)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 4px 14px rgba(0,0,0,.25)' }}>
                <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 15, color: T.ink, fontWeight: 600 }}>{Math.round(prog)}%</div>
              </div>
            </div>
          </div>

          {/* Stepper 3 phases */}
          <div style={{ width: mobile ? 300 : 380, display: 'flex', flexDirection: 'column', gap: 10 }}>
            {phases.map((p, i) => {
              const active = i === phase;
              const done = i < phase;
              return (
                <div key={i} style={{
                  display: 'flex', alignItems: 'flex-start', gap: 12,
                  padding: '10px 14px', borderRadius: 14,
                  background: active ? T.bgAlt : 'transparent',
                  border: active ? `1px solid ${p.color}30` : '1px solid transparent',
                  opacity: done ? 0.55 : 1,
                  transition: 'all .4s ease',
                }}>
                  <div style={{
                    flexShrink: 0, width: 32, height: 32, borderRadius: '50%',
                    background: active ? p.color : (done ? T.matcha + '40' : T.hairline),
                    color: active || done ? '#fff' : T.inkFaint,
                    display: 'flex', alignItems: 'center', justifyContent: 'center',
                    fontSize: 16, transition: 'background .3s',
                  }}>{done ? '✓' : p.icon}</div>
                  <div style={{ flex: 1, minWidth: 0, textAlign: 'left' }}>
                    <div style={{
                      fontFamily: 'Inter,system-ui', fontSize: 13, fontWeight: 600,
                      color: active ? T.ink : T.inkMuted,
                    }}>{p.title}</div>
                    <div style={{
                      fontFamily: 'JetBrains Mono,monospace', fontSize: 10.5,
                      color: T.inkFaint, marginTop: 2,
                      textTransform: 'uppercase', letterSpacing: '.08em',
                    }}>{p.detail}</div>

                    {/* Détail dynamique selon phase active */}
                    {active && i === 1 && visibleIngredients > 0 &&
                      <div style={{ marginTop: 8, display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                        {fakeIngredients.slice(0, visibleIngredients).map((ing, idx) =>
                          <span key={idx} style={{
                            fontSize: 10.5, padding: '2px 8px', borderRadius: 999,
                            background: p.color + '20', color: p.color, fontWeight: 500,
                            animation: 'fadein .35s ease',
                          }}>{ing}</span>
                        )}
                      </div>
                    }
                    {active && i === 2 &&
                      <div style={{
                        marginTop: 6, fontFamily: 'JetBrains Mono,monospace',
                        fontSize: 18, fontWeight: 600, color: p.color,
                        animation: 'slotPulse .3s ease-in-out infinite',
                      }}>~ {fakeCalories} kcal</div>
                    }
                  </div>
                </div>
              );
            })}
            <div style={{ height: 4, background: T.hairline, borderRadius: 2, marginTop: 6, overflow: 'hidden' }}>
              <div style={{ width: prog + '%', height: '100%', background: 'linear-gradient(90deg,' + T.accent + ',' + T.matcha + ')', transition: 'width .4s' }} />
            </div>
          </div>

          {/* Brain dump : tokens qwen streames en direct via NDJSON */}
          {liveTokens && (
            <div style={{
              width: mobile ? 300 : 380,
              padding: '12px 14px',
              background: '#0f0f10',
              border: '1px solid #26262a',
              borderRadius: 14,
              fontFamily: 'JetBrains Mono,monospace',
              textAlign: 'left',
              maxHeight: 120,
              overflow: 'hidden',
              position: 'relative',
            }}>
              <style>{'@keyframes blink { 50% { opacity: 0; } }'}</style>
              <div style={{ fontSize: 9, textTransform: 'uppercase', letterSpacing: '.14em', color: '#6b7280', marginBottom: 6 }}>
                qwen2.5vl · pensée en direct
              </div>
              <div style={{ fontSize: 10.5, color: '#e5e7eb', whiteSpace: 'pre-wrap', wordBreak: 'break-all', lineHeight: 1.45 }}>
                {liveTokens.length > 240 ? '…' + liveTokens.slice(-240) : liveTokens}
                <span style={{ color: T.accent, animation: 'blink 1s step-end infinite' }}>▋</span>
              </div>
            </div>
          )}
        </div>

        <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 18 : 22, letterSpacing: '-.02em', marginTop: 24, fontStyle: 'italic', fontWeight: 500, color: T.inkMuted }}>{phases[phase].title.replace(/…$/, '')}</div>
        <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkFaint, marginTop: 6, maxWidth: 360, lineHeight: 1.5 }}>
          Le modèle qwen2.5vl:7b raisonne localement — quelques secondes patience.
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
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginTop: 6 }}>ou cliquez pour parcourir · JPG, PNG, WebP · 30 Mo max</div>
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
// Auto-detecte le type de repas selon l'heure (miroir backend MealType.detectFromTime)
function detectMealType(dateStrOrNow) {
  const d = dateStrOrNow ? new Date(dateStrOrNow) : new Date();
  const h = d.getHours();
  if (h >= 5 && h < 11) return 'BREAKFAST';
  if (h >= 11 && h < 15) return 'LUNCH';
  if (h >= 15 && h < 18) return 'SNACK';
  return 'DINNER';
}

function CorrectionWired({ T, meal, onSave, onCancel, mobile }) {
  const [hint, setHint] = useState('');
  const [ld, setLd] = useState(false);
  const [reanalyzed, setReanalyzed] = useState(null);
  const [apiErr, setApiErr] = useState('');
  const [mealType, setMealType] = useState(() => meal.mealType || detectMealType(meal.date));
  const [typeErr, setTypeErr] = useState('');
  // Correction manuelle persistante cote back (POST /api/corrections/{mealId})
  const [savedCorrection, setSavedCorrection] = useState(null);
  const [corrLd, setCorrLd] = useState(false);
  const [corrMsg, setCorrMsg] = useState('');

  // Charge la correction existante si le meal est deja status CORRECTED
  useEffect(() => {
    const mid = getMealId(meal);
    if (!mid || String(mid).startsWith('new-')) return;
    API.corrections.get(mid)
      .then(c => { if (c && c.id) setSavedCorrection(c); })
      .catch(() => { /* 404 = pas de correction, normal */ });
  }, []);

  const saveMealType = async (newType) => {
    const mealId = getMealId(meal);
    const prev = mealType;
    setMealType(newType); // optimistic
    setTypeErr('');
    if (!mealId) return;
    try {
      await API.meals.setType(mealId, newType);
    } catch (e) {
      setMealType(prev);
      setTypeErr('Impossible de mettre à jour le type.');
    }
  };

  const doReanalyze = async () => {
    const mealId = getMealId(meal);
    if (!hint.trim()) return;
    if (!mealId) { setApiErr('ID du repas manquant'); return; }
    setLd(true);
    setApiErr('');
    try {
      // Fire-and-forget + polling. Le back UPSERT conserve le meme id et parfois
      // le meme analyzedAt (@CreationTimestamp Hibernate ne se remplace pas a
      // l'update). On compare donc le CONTENU : detectedDishName + items + kcal
      // changent quand le LLM integre le hint.
      const snapshot = (r) => JSON.stringify([
        r?.detectedDishName || '',
        (r?.detectedItems || []).map(i => i.name || '').join('|'),
        r?.estimatedTotalCalories || 0,
      ]);
      const oldSnap = snapshot(meal.analysisRaw);
      let postErr = null;
      const postPromise = API.analyses.reanalyze(mealId, hint.trim())
        .catch(e => { postErr = e; return null; });

      let res = null;
      for (let i = 0; i < 150; i++) { // 150 * 2s = 5 min max
        await new Promise(r => setTimeout(r, 2000));
        try {
          const r = await API.analyses.get(mealId);
          if (r && r.id && snapshot(r) !== oldSnap) {
            res = r;
            break;
          }
        } catch (e) {
          if (e.status !== 404) throw e;
        }
        if (postErr && postErr.status && postErr.status !== 0 && postErr.status !== 504) {
          throw postErr;
        }
      }
      // Fallback : si le polling n'a pas distingue la nouvelle, attend le POST
      if (!res) {
        res = await postPromise;
      }
      if (!res) throw { status: 504, body: '2e passe trop longue (>5 min)' };

      const updated = {
        ...meal,
        name: res.detectedDishName || meal.name,
        ing: (res.detectedItems || []).map(i => i.name || String(i)),
        ...kcalRange(res.estimatedTotalCalories || 0),
        conf: Math.round((res.confidenceScore || 0.8) * 100),
        analysisRaw: res,
      };
      setReanalyzed(updated);
    } catch (e) {
      setApiErr('Erreur lors de la 2ᵉ passe : ' + (e.body || e.message || 'serveur indisponible'));
    } finally {
      setLd(false);
    }
  };

  // Enregistre la correction manuelle de l'utilisateur (nom + items + kcal + commentaire)
  // via POST /api/corrections/{mealId}. Le back passe le meal en status CORRECTED.
  const saveCorrection = async () => {
    const mealId = getMealId(meal);
    if (!mealId) { setCorrMsg('ID du repas manquant'); return; }
    const view = reanalyzed || meal;
    // Reutilise les items du back si dispo, sinon reconstruit depuis les noms
    const items = view.analysisRaw && view.analysisRaw.detectedItems && view.analysisRaw.detectedItems.length
      ? view.analysisRaw.detectedItems.map(i => ({
          name: i.name || String(i),
          quantity: i.quantity ?? 1,
          unit: i.unit || 'portion',
          calories: i.calories ?? null,
        }))
      : (view.ing || []).map(n => ({ name: n, quantity: 1, unit: 'portion', calories: null }));
    if (items.length === 0) items.push({ name: view.name || 'Repas', quantity: 1, unit: 'portion', calories: null });
    const totalKcal = view.kMin != null && view.kMax != null ? (view.kMin + view.kMax) / 2 : null;
    setCorrLd(true);
    setCorrMsg('');
    try {
      const saved = await API.corrections.create(mealId, {
        correctedDishName: view.name,
        correctedItems: items,
        correctedTotalCalories: totalKcal,
        userComment: hint || null,
      });
      setSavedCorrection(saved);
      setCorrMsg('Correction enregistrée.');
    } catch (e) {
      if (e.status === 409) setCorrMsg('Une correction existe deja pour ce repas.');
      else setCorrMsg('Erreur : ' + (e.body || e.message || 'enregistrement impossible'));
    } finally {
      setCorrLd(false);
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
          {(() => {
            const photo = (displayMeal.img && displayMeal.img !== 'PLACEHOLDER') ? displayMeal.img : displayMeal.photoUrl;
            return photo
              ? <img src={photo} alt="repas analysé" style={{ width: '100%', aspectRatio: '1/1', objectFit: 'cover', borderRadius: 24, boxShadow: '0 10px 30px -10px rgba(80,40,10,.3)' }} />
              : <Dish seed={displayMeal.seed} style={{ width: '100%', aspectRatio: '1/1', boxShadow: '0 10px 30px -10px rgba(80,40,10,.3)' }} rounded={24} />;
          })()}
          <div style={{ marginTop: 14, padding: 14, background: T.accentSoft, borderRadius: 16 }}>
            <Confidence value={displayMeal.conf} T={T} />
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.accentDeep, marginTop: 10, lineHeight: 1.5 }}>
              {reanalyzed ? 'Analyse mise à jour avec votre correction.' : 'Les détails ont été estimés par l\'IA. Vous pouvez fournir une indication ci-contre pour une 2ᵉ passe.'}
            </div>
          </div>
        </div>

        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 4 }}>
            <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 26 : 30, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.1, fontWeight: 500, flex: 1, minWidth: 0 }}>
              {displayMeal.name}
            </div>
            {(() => {
              const hs = healthScore(displayMeal);
              return (
                <div title={'Score santé : ' + hs.grade} style={{
                  width: 60, height: 60, borderRadius: '50%', flexShrink: 0,
                  background: hs.color, color: '#fff',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  fontFamily: '"Fraunces",serif', fontSize: 30, fontWeight: 700, fontStyle: 'italic',
                  boxShadow: '0 4px 14px rgba(0,0,0,.18)',
                }}>
                  {hs.grade}
                </div>
              );
            })()}
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6, marginBottom: 14 }}>
            {(() => {
              const items = displayMeal.analysisRaw && displayMeal.analysisRaw.detectedItems
                ? displayMeal.analysisRaw.detectedItems
                : displayMeal.ing.map(name => ({ name, calories: null, protein: null, carbs: null, fat: null }));
              return items.map((item, idx) => {
                const name = item.name || item;
                const hasMacros = item.calories != null || item.protein != null;
                const macroStr = hasMacros
                  ? `${Math.round(item.calories || 0)} kcal · ${Math.round(item.protein || 0)}P / ${Math.round(item.carbs || 0)}G / ${Math.round(item.fat || 0)}L`
                  : null;
                return (
                  <div key={idx} style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: 2 }}>
                    <Chip T={T} variant="default">{name}</Chip>
                    {macroStr && (
                      <span style={{ fontFamily: 'Inter,system-ui', fontSize: 10.5, color: T.inkMuted, paddingLeft: 2 }}>
                        {macroStr}
                      </span>
                    )}
                  </div>
                );
              });
            })()}
          </div>

          {/* Classement du repas — petit-dej / dej / snack / diner */}
          <div style={{ marginBottom: 16 }}>
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkMuted, marginBottom: 6 }}>
              Catégorie du repas
            </div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
              {['BREAKFAST', 'LUNCH', 'SNACK', 'DINNER'].map(t => {
                const selected = mealType === t;
                return (
                  <button
                    key={t}
                    onClick={() => saveMealType(t)}
                    style={{
                      padding: '6px 14px', borderRadius: 999,
                      border: '1px solid ' + (selected ? T.accent : T.hairline),
                      background: selected ? T.accent : 'transparent',
                      color: selected ? '#fff' : T.ink,
                      fontFamily: 'Inter,system-ui', fontSize: 12.5, fontWeight: 500,
                      cursor: 'pointer', transition: 'all .15s ease',
                    }}
                  >{MEAL_TYPE_LABELS[t]}</button>
                );
              })}
            </div>
            {typeErr && <div style={{ color: '#b8452b', fontFamily: 'Inter,system-ui', fontSize: 11, marginTop: 4 }}>{typeErr}</div>}
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
            <div style={{ display: 'flex', gap: 8, marginTop: 10, flexWrap: 'wrap' }}>
              <Btn T={T} variant="soft" onClick={doReanalyze} disabled={ld || !hint.trim()}>
                {ld ? <><Spin size={13} color={T.accentDeep} /> 2ᵉ passe en cours…</> : 'Relancer l\'analyse'}
              </Btn>
              <Btn T={T} variant="ghost" onClick={saveCorrection} disabled={corrLd || !!savedCorrection}>
                {corrLd ? <><Spin size={13} /> Enregistrement…</> : (savedCorrection ? 'Correction enregistrée ✓' : 'Enregistrer ma correction')}
              </Btn>
            </div>
            {corrMsg && (
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, marginTop: 8, color: savedCorrection ? T.accentDeep : T.danger }}>
                {corrMsg}
              </div>
            )}
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
  // Filtre par date (YYYY-MM-DD). Quand set, on appelle GET /api/history/date/{date}
  // et la pagination "Charger plus" est desactivee.
  const [dateFilter, setDateFilter] = useState('');

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

  const loadByDate = async (date) => {
    setLoading(true);
    try {
      const list = await API.history.byDate(date);
      const items = (list || []).map(h => toViewMeal(h, null));
      setMeals(items);
      setTotalPages(1);
      setPage(0);
    } catch (e) {
      console.warn('History byDate error', e);
      setMeals([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (dateFilter) loadByDate(dateFilter);
    else load(0);
  }, [dateFilter]);

  const handleDelete = async (m) => {
    const mid = getMealId(m);
    if (!mid) return;
    if (!window.confirm('Supprimer ce repas ?')) return;
    try {
      await API.meals.del(mid);
      setMeals(prev => prev.filter(x => getMealId(x) !== mid));
    } catch (e) {
      alert('Suppression impossible : ' + (e.body || e.message || 'erreur serveur'));
    }
  };

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

      <div style={{ display: 'flex', gap: 8, marginBottom: 12, overflowX: 'auto', paddingBottom: 4 }}>
        {[{ id: 'all', l: 'Tout' }, { id: 'Petit-déj', l: 'Petit-déj' }, { id: 'Déjeuner', l: 'Déjeuner' }, { id: 'Dîner', l: 'Dîner' }].map(f => (
          <button key={f.id} onClick={() => setFilter(f.id)}
            style={{ padding: '7px 14px', background: filter === f.id ? T.ink : T.bgAlt, color: filter === f.id ? T.bg : T.inkMuted, border: 'none', borderRadius: 999, fontFamily: 'Inter,system-ui', fontSize: 12.5, fontWeight: 600, cursor: 'pointer', whiteSpace: 'nowrap', flexShrink: 0 }}>{f.l}</button>
        ))}
      </div>

      <div style={{ display: 'flex', gap: 8, marginBottom: 20, alignItems: 'center', flexWrap: 'wrap' }}>
        <label style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted, display: 'flex', alignItems: 'center', gap: 6 }}>
          <svg width="14" height="14" viewBox="0 0 16 16" fill="none"><rect x="2" y="3" width="12" height="11" rx="1.5" stroke="currentColor" strokeWidth="1.4"/><path d="M2 6h12M5.5 2v2M10.5 2v2" stroke="currentColor" strokeWidth="1.4"/></svg>
          Filtrer par date
        </label>
        <input
          type="date"
          value={dateFilter}
          onChange={e => setDateFilter(e.target.value)}
          max={new Date().toISOString().slice(0, 10)}
          style={{ padding: '6px 10px', background: T.bgAlt, border: '1px solid ' + T.hairline, borderRadius: 10, fontFamily: 'Inter,system-ui', fontSize: 12.5, color: T.ink, outline: 'none' }}
        />
        {dateFilter && (
          <button onClick={() => setDateFilter('')}
            style={{ padding: '6px 12px', background: T.accentSoft, color: T.accentDeep, border: 'none', borderRadius: 999, fontFamily: 'Inter,system-ui', fontSize: 12, fontWeight: 600, cursor: 'pointer' }}>
            Effacer le filtre
          </button>
        )}
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
              {dayMeals.map(m => (
                <div key={m.id} style={{ position: 'relative' }}>
                  <MealRow m={m} T={T} onClick={() => onMeal(m)} />
                  <button
                    onClick={(e) => { e.stopPropagation(); handleDelete(m); }}
                    title="Supprimer ce repas"
                    style={{ position: 'absolute', top: 8, right: 8, width: 30, height: 30, borderRadius: '50%', background: 'rgba(255,255,255,0.92)', border: '1px solid ' + T.hairline, color: T.danger, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 2px 8px rgba(0,0,0,.08)' }}
                  >
                    <svg width="14" height="14" viewBox="0 0 16 16" fill="none"><path d="M3 5h10M6 5V3h4v2M5 5l.8 9a1 1 0 001 .9h2.4a1 1 0 001-.9L11 5" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round" strokeLinejoin="round"/></svg>
                  </button>
                </div>
              ))}
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

// ─── Admin câblé ─────────────────────────────────────────────────────────────
// Panel reserve aux utilisateurs role=ADMIN (enforce cote back par @PreAuthorize).
// Le tab "Admin" n'apparait dans SideNav/MobNav que si user.role==='ADMIN'.
function AdminPageWired({ T, currentUser, mobile }) {
  const [stats, setStats] = useState(null);
  const [users, setUsers] = useState({ content: [], totalPages: 1 });
  const [meals, setMeals] = useState({ content: [], totalPages: 1 });
  const [usersPage, setUsersPage] = useState(0);
  const [mealsPage, setMealsPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState('');

  const reloadStats = () => API.admin.stats().then(setStats).catch(() => setStats(null));
  const reloadUsers = (p = usersPage) => API.admin.users(p, 10).then(r => { setUsers(r); setUsersPage(p); }).catch(e => setErr('Chargement users : ' + (e.body || e.message)));
  const reloadMeals = (p = mealsPage) => API.admin.meals(p, 10).then(r => { setMeals(r); setMealsPage(p); }).catch(e => setErr('Chargement meals : ' + (e.body || e.message)));

  useEffect(() => {
    (async () => {
      setLoading(true);
      await Promise.all([reloadStats(), reloadUsers(0), reloadMeals(0)]);
      setLoading(false);
    })();
  }, []);

  const deleteUser = async (u) => {
    if (currentUser && currentUser.id === u.id) { alert('Vous ne pouvez pas supprimer votre propre compte.'); return; }
    if (!window.confirm(`Supprimer ${u.username} et tous ses repas ?`)) return;
    try {
      await API.admin.deleteUser(u.id);
      await Promise.all([reloadUsers(), reloadMeals(), reloadStats()]);
    } catch (e) { alert('Suppression impossible : ' + (e.body || e.message)); }
  };

  const toggleRole = async (u) => {
    const next = u.role === 'ADMIN' ? 'USER' : 'ADMIN';
    if (!window.confirm(`Changer le role de ${u.username} en ${next} ?`)) return;
    try {
      await API.admin.setRole(u.id, next);
      await reloadUsers();
    } catch (e) { alert('Changement impossible : ' + (e.body || e.message)); }
  };

  const deleteMeal = async (m) => {
    if (!window.confirm(`Supprimer le repas #${m.id} de ${m.userName} ?`)) return;
    try {
      await API.admin.deleteMeal(m.id);
      await Promise.all([reloadMeals(), reloadStats()]);
    } catch (e) { alert('Suppression impossible : ' + (e.body || e.message)); }
  };

  if (loading) {
    return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%', color: T.inkMuted, fontFamily: 'Inter,system-ui', fontSize: 14 }}>Chargement…</div>;
  }

  const statCard = (label, value, suffix) => (
    <div style={{ flex: '1 1 160px', padding: 18, background: T.bgAlt, borderRadius: 18, border: '1px solid ' + T.hairline }}>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkFaint }}>{label}</div>
      <div style={{ fontFamily: '"Fraunces",serif', fontSize: 30, color: T.ink, letterSpacing: '-.02em', fontWeight: 500, marginTop: 4 }}>
        {value}{suffix && <span style={{ fontSize: 14, color: T.inkMuted, marginLeft: 4, fontFamily: 'Inter,system-ui' }}>{suffix}</span>}
      </div>
    </div>
  );

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto' }}>
      <div style={{ fontFamily: '"Fraunces",serif', fontSize: mobile ? 28 : 36, color: T.ink, letterSpacing: '-.03em', fontWeight: 500, marginBottom: 4 }}>
        Panel <span style={{ fontStyle: 'italic', color: T.accent }}>Admin</span>
      </div>
      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, marginBottom: 22 }}>
        Supervision utilisateurs, moderation des repas, metriques globales.
      </div>

      {err && <div style={{ padding: '10px 14px', background: 'oklch(0.95 0.03 25)', border: '1px solid ' + T.danger, borderRadius: 12, fontFamily: 'Inter,system-ui', fontSize: 13, color: T.danger, marginBottom: 18 }}>{err}</div>}

      {stats && (
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, marginBottom: 28 }}>
          {statCard('Utilisateurs', stats.totalUsers)}
          {statCard('Repas', stats.totalMeals)}
          {statCard('Analyses IA', stats.totalAnalyses)}
          {statCard('Kcal moy. / analyse', stats.avgCaloriesPerMeal != null ? Math.round(stats.avgCaloriesPerMeal) : '—', 'kcal')}
        </div>
      )}

      {/* Users */}
      <div style={{ marginBottom: 34 }}>
        <div style={{ fontFamily: '"Fraunces",serif', fontSize: 22, color: T.ink, letterSpacing: '-.02em', fontWeight: 500, marginBottom: 12 }}>Utilisateurs</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {(users.content || []).map(u => {
            const isSelf = currentUser && currentUser.id === u.id;
            return (
              <div key={u.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 12, background: T.surface, border: '1px solid ' + T.hairline, borderRadius: 14, flexWrap: 'wrap' }}>
                <div style={{ width: 36, height: 36, borderRadius: '50%', background: T.accent, color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontFamily: '"Fraunces",serif', fontSize: 16, fontStyle: 'italic' }}>{(u.username || '?')[0].toUpperCase()}</div>
                <div style={{ flex: '1 1 180px', minWidth: 0 }}>
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 14, fontWeight: 600, color: T.ink }}>{u.username}{isSelf && <span style={{ marginLeft: 8, fontSize: 11, color: T.inkFaint, fontWeight: 500 }}>(vous)</span>}</div>
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted }}>{u.email}</div>
                </div>
                <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 999, background: u.role === 'ADMIN' ? T.accent : T.bgAlt, color: u.role === 'ADMIN' ? '#fff' : T.inkMuted, textTransform: 'uppercase', letterSpacing: '.08em' }}>{u.role}</div>
                <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 12, color: T.inkFaint }}>{u.mealCount} repas</div>
                <Btn T={T} variant="ghost" onClick={() => toggleRole(u)} disabled={isSelf}>
                  {u.role === 'ADMIN' ? 'Retrograder' : 'Promouvoir'}
                </Btn>
                <Btn T={T} variant="ghost" onClick={() => deleteUser(u)} disabled={isSelf}>
                  Supprimer
                </Btn>
              </div>
            );
          })}
        </div>
        {users.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 12 }}>
            <Btn T={T} variant="ghost" disabled={usersPage === 0} onClick={() => reloadUsers(usersPage - 1)}>← Précédent</Btn>
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted, alignSelf: 'center' }}>Page {usersPage + 1} / {users.totalPages}</div>
            <Btn T={T} variant="ghost" disabled={usersPage + 1 >= users.totalPages} onClick={() => reloadUsers(usersPage + 1)}>Suivant →</Btn>
          </div>
        )}
      </div>

      {/* Meals */}
      <div style={{ marginBottom: 34 }}>
        <div style={{ fontFamily: '"Fraunces",serif', fontSize: 22, color: T.ink, letterSpacing: '-.02em', fontWeight: 500, marginBottom: 12 }}>Repas</div>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {(meals.content || []).map(m => (
            <div key={m.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: 12, background: T.surface, border: '1px solid ' + T.hairline, borderRadius: 14, flexWrap: 'wrap' }}>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 12, color: T.inkFaint }}>#{m.id}</div>
              <div style={{ flex: '1 1 160px', fontFamily: 'Inter,system-ui', fontSize: 13, color: T.ink, fontWeight: 500 }}>{m.userName || 'inconnu'}</div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 999, background: T.bgAlt, color: T.inkMuted, textTransform: 'uppercase', letterSpacing: '.08em' }}>{m.mealType || '—'}</div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 600, padding: '3px 10px', borderRadius: 999, background: m.status === 'ANALYSED' ? T.matchaSoft : T.bgAlt, color: T.inkMuted, textTransform: 'uppercase', letterSpacing: '.08em' }}>{m.status}</div>
              <div style={{ fontFamily: 'JetBrains Mono,monospace', fontSize: 12, color: T.inkMuted }}>{m.calories != null ? Math.round(m.calories) + ' kcal' : '—'}</div>
              <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, color: T.inkFaint }}>{m.uploadedAt ? new Date(m.uploadedAt).toLocaleDateString('fr-FR') : ''}</div>
              <Btn T={T} variant="ghost" onClick={() => deleteMeal(m)}>Supprimer</Btn>
            </div>
          ))}
        </div>
        {meals.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'center', gap: 8, marginTop: 12 }}>
            <Btn T={T} variant="ghost" disabled={mealsPage === 0} onClick={() => reloadMeals(mealsPage - 1)}>← Précédent</Btn>
            <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted, alignSelf: 'center' }}>Page {mealsPage + 1} / {meals.totalPages}</div>
            <Btn T={T} variant="ghost" disabled={mealsPage + 1 >= meals.totalPages} onClick={() => reloadMeals(mealsPage + 1)}>Suivant →</Btn>
          </div>
        )}
      </div>
    </div>
  );
}

// ─── Coach IA — Bouton flottant + panel (bilan hebdo + suggestion de repas) ──
// Monte au-dessus de tous les ecrans authentifies. Au clic, ouvre un panel
// avec 2 actions : bilan narratif de la semaine, suggestion du prochain repas.
// Les appels LLM prennent 5-30s (cold start Ollama) donc on affiche un loader.
function CoachFab({ T, mobile }) {
  const [open, setOpen] = useState(false);
  const [view, setView] = useState('menu'); // 'menu' | 'summary' | 'suggestion'
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState('');
  const [summary, setSummary] = useState(null);
  const [suggestion, setSuggestion] = useState(null);
  // Effet typewriter : affiche `summary.summary` caractere par caractere
  const [typed, setTyped] = useState('');

  useEffect(() => {
    if (view !== 'summary' || !summary || !summary.summary) { setTyped(''); return; }
    let i = 0;
    const full = summary.summary;
    setTyped('');
    const id = setInterval(() => {
      i++;
      setTyped(full.slice(0, i));
      if (i >= full.length) clearInterval(id);
    }, 18);
    return () => clearInterval(id);
  }, [summary, view]);

  const loadSummary = async () => {
    setView('summary'); setLoading(true); setErr(''); setSummary(null);
    try { setSummary(await API.stats.weeklySummary()); }
    catch (e) { setErr('Coach indisponible : ' + (e.body || e.message || 'erreur')); }
    finally { setLoading(false); }
  };

  const loadSuggestion = async () => {
    setView('suggestion'); setLoading(true); setErr(''); setSuggestion(null);
    try { setSuggestion(await API.stats.mealSuggestion()); }
    catch (e) { setErr('Coach indisponible : ' + (e.body || e.message || 'erreur')); }
    finally { setLoading(false); }
  };

  const close = () => { setOpen(false); setView('menu'); setErr(''); };

  const mealTypeLabel = (t) => ({ BREAKFAST: 'Petit-dejeuner', LUNCH: 'Dejeuner', SNACK: 'Gouter', DINNER: 'Diner' }[t] || 'Prochain repas');

  const fabSize = mobile ? 56 : 60;
  const fabBottom = mobile ? 80 : 28;

  return (
    <>
      {!open && (
        <button
          onClick={() => setOpen(true)}
          title="Ton coach IA"
          style={{
            position: 'fixed', bottom: fabBottom, right: 22,
            width: fabSize, height: fabSize, borderRadius: '50%',
            background: 'linear-gradient(135deg, ' + T.accent + ', ' + T.matcha + ')',
            color: '#fff', border: 'none', cursor: 'pointer',
            boxShadow: '0 12px 28px -6px rgba(80,40,10,.45), 0 0 0 4px ' + T.bg,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            zIndex: 200, fontSize: 24,
            animation: 'coachPulse 2.4s ease-in-out infinite',
          }}
        >
          <svg width="26" height="26" viewBox="0 0 24 24" fill="none">
            <path d="M12 2l2 5 5 2-5 2-2 5-2-5-5-2 5-2 2-5z" fill="currentColor"/>
          </svg>
          <style>{'@keyframes coachPulse {0%,100%{transform:scale(1)}50%{transform:scale(1.08)}}'}</style>
        </button>
      )}

      {open && (
        <div style={{
          position: 'fixed', inset: 0, zIndex: 300,
          background: 'rgba(20,14,8,0.55)',
          display: 'flex', alignItems: mobile ? 'flex-end' : 'center', justifyContent: 'center',
          padding: mobile ? 0 : 24,
          animation: 'fadeIn .25s ease',
        }} onClick={close}>
          <style>{'@keyframes fadeIn {from{opacity:0}to{opacity:1}} @keyframes slideUp {from{transform:translateY(24px);opacity:0}to{transform:translateY(0);opacity:1}}'}</style>
          <div onClick={e => e.stopPropagation()} style={{
            width: mobile ? '100%' : 520, maxHeight: mobile ? '88vh' : '80vh',
            background: T.bg, color: T.ink,
            borderRadius: mobile ? '22px 22px 0 0' : 24,
            boxShadow: '0 40px 80px -20px rgba(80,40,10,.45)',
            overflow: 'hidden', display: 'flex', flexDirection: 'column',
            animation: 'slideUp .3s ease',
          }}>
            <div style={{ padding: '18px 22px', borderBottom: '1px solid ' + T.hairline, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                <div style={{ width: 34, height: 34, borderRadius: 12, background: 'linear-gradient(135deg,' + T.accent + ',' + T.matcha + ')', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2l2 5 5 2-5 2-2 5-2-5-5-2 5-2 2-5z"/></svg>
                </div>
                <div>
                  <div style={{ fontFamily: '"Fraunces",serif', fontSize: 20, fontWeight: 500, letterSpacing: '-.02em' }}>Coach <span style={{ fontStyle: 'italic', color: T.accent }}>IA</span></div>
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, color: T.inkFaint, textTransform: 'uppercase', letterSpacing: '.08em' }}>Propulse par qwen2.5vl</div>
                </div>
              </div>
              <button onClick={close} style={{ width: 32, height: 32, borderRadius: '50%', border: 'none', background: T.bgAlt, color: T.inkMuted, cursor: 'pointer' }}>×</button>
            </div>

            <div style={{ padding: 22, overflowY: 'auto', flex: 1 }}>
              {view === 'menu' && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13.5, color: T.inkMuted, marginBottom: 4, lineHeight: 1.55 }}>
                    Deux choses que je peux faire pour toi :
                  </div>
                  <button onClick={loadSummary} style={{
                    textAlign: 'left', padding: 16, border: '1px solid ' + T.hairline, borderRadius: 16,
                    background: T.bgAlt, cursor: 'pointer', display: 'flex', gap: 12, alignItems: 'flex-start',
                  }}>
                    <div style={{ fontSize: 26 }}>📖</div>
                    <div>
                      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 14, fontWeight: 600, color: T.ink }}>Mon bilan de la semaine</div>
                      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted, marginTop: 2 }}>Un resume narratif de tes 7 derniers jours.</div>
                    </div>
                  </button>
                  <button onClick={loadSuggestion} style={{
                    textAlign: 'left', padding: 16, border: '1px solid ' + T.hairline, borderRadius: 16,
                    background: T.bgAlt, cursor: 'pointer', display: 'flex', gap: 12, alignItems: 'flex-start',
                  }}>
                    <div style={{ fontSize: 26 }}>🍽️</div>
                    <div>
                      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 14, fontWeight: 600, color: T.ink }}>Que manger maintenant ?</div>
                      <div style={{ fontFamily: 'Inter,system-ui', fontSize: 12, color: T.inkMuted, marginTop: 2 }}>Une suggestion de plat adaptee a tes stats et a l'heure.</div>
                    </div>
                  </button>
                </div>
              )}

              {view !== 'menu' && loading && (
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '40px 20px', gap: 14 }}>
                  <Spin size={26} />
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 13, color: T.inkMuted, textAlign: 'center' }}>
                    Le coach reflechit<br/>
                    <span style={{ fontSize: 11, color: T.inkFaint }}>(5 a 30s si Ollama demarre a froid)</span>
                  </div>
                </div>
              )}

              {view !== 'menu' && err && !loading && (
                <div style={{ padding: 16, background: 'oklch(0.95 0.03 25)', border: '1px solid ' + T.danger, borderRadius: 12, color: T.danger, fontFamily: 'Inter,system-ui', fontSize: 13 }}>
                  {err}
                </div>
              )}

              {view === 'summary' && summary && !loading && (
                <div>
                  <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap', marginBottom: 16 }}>
                    <div style={{ padding: '8px 14px', background: T.accentSoft, color: T.accentDeep, borderRadius: 999, fontFamily: 'JetBrains Mono,monospace', fontSize: 12, fontWeight: 600 }}>
                      {summary.mealCount} repas
                    </div>
                    <div style={{ padding: '8px 14px', background: T.matchaSoft, color: T.inkMuted, borderRadius: 999, fontFamily: 'JetBrains Mono,monospace', fontSize: 12, fontWeight: 600 }}>
                      {Math.round(summary.totalCalories || 0)} kcal total
                    </div>
                    {summary.bestDay && (
                      <div style={{ padding: '8px 14px', background: T.bgAlt, color: T.inkMuted, borderRadius: 999, fontFamily: 'JetBrains Mono,monospace', fontSize: 12 }}>
                        top : {summary.bestDay} ({Math.round(summary.bestDayCalories || 0)} kcal)
                      </div>
                    )}
                  </div>
                  <div style={{ fontFamily: '"Fraunces",serif', fontSize: 18, color: T.ink, lineHeight: 1.55, letterSpacing: '-.01em', minHeight: 80 }}>
                    {typed}<span style={{ opacity: typed.length < (summary.summary || '').length ? 1 : 0, color: T.accent }}>▋</span>
                  </div>
                </div>
              )}

              {view === 'suggestion' && suggestion && !loading && (
                <div>
                  <div style={{ fontFamily: 'Inter,system-ui', fontSize: 11, fontWeight: 500, textTransform: 'uppercase', letterSpacing: '.1em', color: T.inkFaint, marginBottom: 8 }}>
                    {mealTypeLabel(suggestion.mealType)}
                  </div>
                  <div style={{ fontFamily: '"Fraunces",serif', fontSize: 30, color: T.ink, letterSpacing: '-.02em', lineHeight: 1.1, fontWeight: 500 }}>
                    <span style={{ fontStyle: 'italic', color: T.accent }}>{suggestion.dishName}</span>
                  </div>
                  {suggestion.estimatedCalories != null && (
                    <div style={{ marginTop: 8, fontFamily: 'JetBrains Mono,monospace', fontSize: 14, color: T.inkMuted }}>
                      ~ {suggestion.estimatedCalories} kcal
                    </div>
                  )}
                  {suggestion.reason && (
                    <div style={{ marginTop: 14, padding: 14, background: T.accentSoft, color: T.accentDeep, borderRadius: 14, fontFamily: 'Inter,system-ui', fontSize: 13.5, lineHeight: 1.55 }}>
                      {suggestion.reason}
                    </div>
                  )}
                </div>
              )}

              {view !== 'menu' && !loading && (
                <div style={{ marginTop: 20 }}>
                  <Btn T={T} variant="ghost" onClick={() => { setView('menu'); setErr(''); }}>← Retour</Btn>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
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

  // Ouvre un meal depuis l'historique en fetchant son analyse (ingredients, confiance)
  // pour eviter d'afficher une vue incomplete (les histItems ne portent pas
  // detectedItems).
  const openMealFromServer = async (m) => {
    setCurrentMeal(m);
    setScreen('correction');
    const mealId = m.mealId || m.serverId || m.id;
    if (!mealId || String(mealId).startsWith('new-')) return;
    try {
      const analysis = await API.analyses.get(mealId);
      if (analysis && analysis.id) {
        const ing = (analysis.detectedItems || []).map(i => i.name || String(i));
        setCurrentMeal(prev => prev && (prev.mealId === mealId || prev.serverId === mealId)
          ? {
              ...prev,
              ing: ing.length ? ing : prev.ing,
              conf: Math.round((analysis.confidenceScore || 0.8) * 100),
              analysisRaw: analysis,
            }
          : prev);
      }
    } catch { /* 404 = pas d'analyse pour ce meal, on garde l'etat courant */ }
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
        <DashboardWired T={T} user={user} onUpload={() => setScreen('upload')} onHistory={() => setScreen('history')} onMeal={openMealFromServer} mobile={isMobile} />
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
        <HistoryWired T={T} onMeal={openMealFromServer} mobile={isMobile} />
      </Shell>
    );
  } else if (effectiveScreen === 'profile') {
    content = (
      <Shell T={T} mobile={isMobile} active="profile" onNav={id => setScreen(id)} user={user}>
        <ProfileWired T={T} user={user} onLogout={onLogout} mobile={isMobile} dark={dark} setDark={setDark} />
      </Shell>
    );
  } else if (effectiveScreen === 'admin' && user && user.role === 'ADMIN') {
    content = (
      <Shell T={T} mobile={isMobile} active="admin" onNav={id => setScreen(id)} user={user}>
        <AdminPageWired T={T} currentUser={user} mobile={isMobile} />
      </Shell>
    );
  } else {
    // Fallback
    setScreen('dashboard');
    content = null;
  }

  return (
    <div style={{
      height: '100vh',
      background: T.pageBg,
      color: T.ink,
      fontFamily: 'Inter, -apple-system, system-ui, sans-serif',
      WebkitFontSmoothing: 'antialiased',
      overflow: 'hidden',
    }}>
      {content}
      {/* Coach IA flottant, visible sur tous les ecrans authentifies sauf upload/correction */}
      {user && !['auth', 'upload', 'correction'].includes(effectiveScreen) && (
        <CoachFab T={T} mobile={isMobile} />
      )}
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
