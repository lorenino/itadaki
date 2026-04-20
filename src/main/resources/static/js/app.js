const API = '';
let token = localStorage.getItem('itadaki_token') || null;
let currentUser = null;
let currentMealId = null;
let historyPage = 0;

// ─── INIT ──────────────────────────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
    if (token) {
        showApp();
    }
});

// ─── AUTH ──────────────────────────────────────────────────────────────────
async function login(e) {
    e.preventDefault();
    clearError('auth-error');
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    try {
        const res = await fetch(`${API}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || 'Échec de la connexion');
        token = data.token;
        currentUser = data.user;
        localStorage.setItem('itadaki_token', token);
        showApp();
    } catch (err) {
        showError('auth-error', err.message);
    }
}

async function register(e) {
    e.preventDefault();
    clearError('auth-error');
    const username = document.getElementById('reg-username').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;

    try {
        const res = await fetch(`${API}/api/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.message || "Échec de l'inscription");
        token = data.token;
        currentUser = data.user;
        localStorage.setItem('itadaki_token', token);
        showApp();
    } catch (err) {
        showError('auth-error', err.message);
    }
}

function logout() {
    token = null;
    currentUser = null;
    currentMealId = null;
    localStorage.removeItem('itadaki_token');
    document.getElementById('app-screen').classList.add('hidden');
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('login-form').reset();
}

function showApp() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('app-screen').classList.remove('hidden');
    if (currentUser) {
        document.getElementById('user-label').textContent = currentUser.email || '';
    }
    loadHistory();
    loadStats();
}

// ─── UPLOAD / ANALYZE ──────────────────────────────────────────────────────
function previewFile(input) {
    const file = input.files[0];
    if (!file) return;

    const img = document.getElementById('preview-img');
    const placeholder = document.getElementById('drop-placeholder');
    img.src = URL.createObjectURL(file);
    img.classList.remove('hidden');
    placeholder.classList.add('hidden');
    document.getElementById('upload-btn').disabled = false;
    document.getElementById('analysis-card').classList.add('hidden');
}

async function uploadMeal() {
    const file = document.getElementById('file-input').files[0];
    if (!file) return;

    setLoading(true);
    clearError('upload-error');
    document.getElementById('analysis-card').classList.add('hidden');

    try {
        // 1. Upload photo
        const form = new FormData();
        form.append('image', file);
        const uploadRes = await authFetch('/api/meals', { method: 'POST', body: form });
        if (!uploadRes.ok) {
            const d = await uploadRes.json();
            throw new Error(d.message || 'Échec du téléversement');
        }
        const uploadData = await uploadRes.json();
        currentMealId = uploadData.mealId;

        // 2. Trigger analysis
        const analysisRes = await authFetch(`/api/analysis/${currentMealId}`, { method: 'POST' });
        if (!analysisRes.ok) {
            const d = await analysisRes.json();
            throw new Error(d.message || "Échec de l'analyse");
        }
        const analysis = await analysisRes.json();
        displayAnalysis(analysis);
        loadHistory();
    } catch (err) {
        showError('upload-error', err.message);
    } finally {
        setLoading(false);
    }
}

function displayAnalysis(analysis) {
    document.getElementById('res-dish').textContent = analysis.detectedDishName || 'Inconnu';
    document.getElementById('res-calories').textContent =
        analysis.estimatedTotalCalories ? `${Math.round(analysis.estimatedTotalCalories)} kcal` : '—';
    document.getElementById('res-confidence').textContent =
        formatConfidence(analysis.confidenceScore);

    const itemsEl = document.getElementById('res-items');
    itemsEl.innerHTML = '';
    (analysis.detectedItems || []).forEach(item => {
        const tag = document.createElement('span');
        tag.className = 'item-tag';
        tag.textContent = item.name || item;
        itemsEl.appendChild(tag);
    });

    document.getElementById('correction-dish').value = analysis.detectedDishName || '';
    document.getElementById('correction-calories').value =
        analysis.estimatedTotalCalories ? Math.round(analysis.estimatedTotalCalories) : '';
    document.getElementById('correction-msg').classList.add('hidden');
    document.getElementById('analysis-card').classList.remove('hidden');
}

function formatConfidence(score) {
    if (score == null) return '—';
    if (score >= 0.8) return '🟢 Haute';
    if (score >= 0.4) return '🟡 Moyenne';
    return '🔴 Basse';
}

// ─── CORRECTION ────────────────────────────────────────────────────────────
async function submitCorrection() {
    if (!currentMealId) return;
    const dish = document.getElementById('correction-dish').value;
    const calories = parseFloat(document.getElementById('correction-calories').value) || null;
    const comment = document.getElementById('correction-comment').value;

    try {
        const res = await authFetch(`/api/correction/${currentMealId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                correctedDishName: dish,
                correctedTotalCalories: calories,
                userComment: comment,
                correctedItems: []
            })
        });
        if (!res.ok) {
            const d = await res.json();
            throw new Error(d.message || 'Échec de la correction');
        }
        const msg = document.getElementById('correction-msg');
        msg.textContent = 'Correction enregistrée !';
        msg.classList.remove('hidden');
    } catch (err) {
        const msg = document.getElementById('correction-msg');
        msg.textContent = err.message;
        msg.classList.remove('hidden');
        msg.style.cssText = 'background:rgba(232,90,90,.12);border-color:rgba(232,90,90,.3);color:#e85a5a';
    }
}

// ─── HISTORY ───────────────────────────────────────────────────────────────
async function loadHistory(page = 0) {
    historyPage = page;
    try {
        const res = await authFetch(`/api/history?page=${page}&size=10`);
        if (!res.ok) return;
        const data = await res.json();
        renderHistory(data.content || []);
        document.getElementById('page-label').textContent = `Page ${page + 1}`;
        document.getElementById('prev-btn').disabled = page === 0;
        document.getElementById('next-btn').disabled = data.last;
    } catch (_) {}
}

async function filterByDate(date) {
    if (!date) { loadHistory(0); return; }
    try {
        const res = await authFetch(`/api/history/date/${date}`);
        if (!res.ok) return;
        const items = await res.json();
        renderHistory(items);
        document.getElementById('prev-btn').disabled = true;
        document.getElementById('next-btn').disabled = true;
    } catch (_) {}
}

function renderHistory(items) {
    const list = document.getElementById('history-list');
    if (!items.length) {
        list.innerHTML = '<p class="empty-state">Aucun repas trouvé.</p>';
        return;
    }
    list.innerHTML = items.map(item => `
        <div class="history-item">
            <div>
                <div class="dish">${item.detectedDishName || 'Repas sans nom'}</div>
                <div class="meta">${formatDate(item.uploadedAt)}</div>
            </div>
            <div class="cal">${item.totalCalories ? Math.round(item.totalCalories) + ' kcal' : '—'}</div>
        </div>
    `).join('');
}

function changePage(delta) {
    loadHistory(Math.max(0, historyPage + delta));
}

// ─── STATS ─────────────────────────────────────────────────────────────────
async function loadStats() {
    try {
        const [overviewRes, dailyRes] = await Promise.all([
            authFetch('/api/stats/overview'),
            authFetch('/api/stats/daily')
        ]);
        if (overviewRes.ok) {
            const o = await overviewRes.json();
            document.getElementById('st-meals').textContent = o.totalMeals ?? '0';
            document.getElementById('st-total-cal').textContent =
                o.totalCalories ? `${Math.round(o.totalCalories)} kcal` : '0';
            document.getElementById('st-avg-cal').textContent =
                o.averageDailyCalories ? `${Math.round(o.averageDailyCalories)} kcal` : '0';
        }
        if (dailyRes.ok) {
            const daily = await dailyRes.json();
            renderDailyChart(daily);
        }
    } catch (_) {}
}

function renderDailyChart(items) {
    const chart = document.getElementById('daily-chart');
    if (!items.length) { chart.innerHTML = '<p class="empty-state">Aucune donnée.</p>'; return; }
    const max = Math.max(...items.map(i => i.totalCalories || 0), 1);
    chart.innerHTML = items.slice(0, 30).reverse().map(item => {
        const height = Math.max(4, Math.round(((item.totalCalories || 0) / max) * 120));
        const cal = item.totalCalories ? Math.round(item.totalCalories) : 0;
        const label = (item.date || '').slice(5); // MM-DD
        return `
            <div class="bar-wrap" title="${item.date}: ${cal} kcal">
                <div class="bar" style="height:${height}px"></div>
                <span class="bar-label">${label}</span>
            </div>
        `;
    }).join('');
}

// ─── NAV ───────────────────────────────────────────────────────────────────
function showView(viewId, btn) {
    document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
    document.querySelectorAll('.nav-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(viewId).classList.remove('hidden');
    btn.classList.add('active');
    if (viewId === 'stats-view') loadStats();
    if (viewId === 'history-view') {
        document.getElementById('history-date-filter').value = '';
        loadHistory(0);
    }
}

function showTab(tabId, btn) {
    document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    document.getElementById(tabId).classList.add('active');
    btn.classList.add('active');
}

// ─── HELPERS ───────────────────────────────────────────────────────────────
function authFetch(url, options = {}) {
    return fetch(API + url, {
        ...options,
        headers: {
            ...options.headers,
            'Authorization': `Bearer ${token}`
        }
    });
}

function setLoading(on) {
    document.getElementById('loading-overlay').classList.toggle('hidden', !on);
}

function showError(id, msg) {
    const el = document.getElementById(id);
    el.textContent = msg;
    el.classList.remove('hidden');
}

function clearError(id) {
    const el = document.getElementById(id);
    el.textContent = '';
    el.classList.add('hidden');
}

function formatDate(str) {
    if (!str) return '';
    try {
        return new Date(str).toLocaleDateString('fr-FR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    } catch (_) { return str; }
}
