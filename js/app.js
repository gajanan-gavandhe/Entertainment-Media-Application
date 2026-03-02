// ── CONFIG ────────────────────────────────────────────────────
const API_BASE = 'http://localhost:8083/api';
const WS_URL   = 'http://localhost:8083/ws';

// ── API CLIENT ────────────────────────────────────────────────
const API = {
  getToken(){ return localStorage.getItem('sv_token'); },
  getUser(){  const u=localStorage.getItem('sv_user'); return u?JSON.parse(u):null; },
  setAuth(token,user){ localStorage.setItem('sv_token',token); localStorage.setItem('sv_user',JSON.stringify(user)); },
  clearAuth(){ localStorage.removeItem('sv_token'); localStorage.removeItem('sv_user'); },
  isLoggedIn(){ return !!this.getToken(); },

  async request(method,path,body=null,isForm=false){
    const headers = {};
    const token = this.getToken();
    if(token) headers['Authorization']=`Bearer ${token}`;
    if(!isForm) headers['Content-Type']='application/json';
    const opts = { method, headers };
    if(body) opts.body = isForm ? body : JSON.stringify(body);
    try{
      const res = await fetch(API_BASE+path, opts);
      const data = await res.json().catch(()=>({}));
      if(res.status===401){ this.clearAuth(); redirectToLogin(); throw {status:401,message:'Session expired'}; }
      if(!res.ok) throw { status:res.status, message:data.message||'Request failed' };
      return data;
    }catch(err){ if(!err.status) err.message=err.message||'Network error'; throw err; }
  },
  get(p){ return this.request('GET',p); },
  post(p,b){ return this.request('POST',p,b); },
  put(p,b){ return this.request('PUT',p,b); },
  delete(p){ return this.request('DELETE',p); },
  postForm(p,fd){ return this.request('POST',p,fd,true); },
};

// ── AUTH GUARD ────────────────────────────────────────────────
function requireAuth(){
  if(!API.isLoggedIn()){ redirectToLogin(); return false; }
  return true;
}
function requireRole(...roles){
  const u=API.getUser();
  if(!API.isLoggedIn()||!u){ redirectToLogin(); return false; }
  if(!roles.includes(u.role)){
    Toast.show('Access denied.','error');
    setTimeout(()=>{ window.location.href='home.html'; },1200);
    return false;
  }
  return true;
}
function redirectToLogin(){ window.location.href='../index.html'; }
function logout(){ API.clearAuth(); redirectToLogin(); }

// ── WEBSOCKET MANAGER (STOMP over SockJS with polling fallback) ─
const LiveData = {
  client: null,
  connected: false,
  subs: {},
  pollers: [],
  reconnectDelay: 3000,

  connect(onConnected){
    if(typeof SockJS==='undefined'||typeof Stomp==='undefined'){
      console.warn('STOMP/SockJS not loaded, using polling fallback');
      onConnected&&onConnected(false); return;
    }
    const sock = new SockJS(WS_URL);
    this.client = Stomp.over(sock);
    this.client.debug = ()=>{};
    this.client.connect({},
      ()=>{ this.connected=true; onConnected&&onConnected(true);
            document.querySelectorAll('.ws-status').forEach(el=>{ el.textContent='● LIVE'; el.style.color='var(--green)'; }); },
      ()=>{ this.connected=false;
            document.querySelectorAll('.ws-status').forEach(el=>{ el.textContent='○ Polling'; el.style.color='var(--amber)'; });
            setTimeout(()=>this.connect(onConnected), this.reconnectDelay); }
    );
  },

  subscribe(topic, callback){
    if(this.connected && this.client){
      const sub = this.client.subscribe(topic, msg=>{ try{ callback(JSON.parse(msg.body)); }catch(e){} });
      this.subs[topic] = sub;
    }
  },

  // Polling fallback: call fn every interval ms
  poll(fn, intervalMs=5000){
    fn(); // call immediately
    const id = setInterval(fn, intervalMs);
    this.pollers.push(id);
    return id;
  },

  stopAll(){
    this.pollers.forEach(id=>clearInterval(id));
    Object.values(this.subs).forEach(s=>{ try{ s.unsubscribe(); }catch(e){} });
  }
};

// ── TOAST ────────────────────────────────────────────────────
const Toast = {
  container: null,
  init(){ if(!this.container){ this.container=document.createElement('div'); this.container.className='toast-container'; document.body.appendChild(this.container); } },
  show(msg,type='info',duration=3500){
    this.init();
    const icons={success:'✅',error:'❌',info:'ℹ️',warning:'⚠️'};
    const t=document.createElement('div'); t.className=`toast ${type}`;
    t.innerHTML=`<span>${icons[type]||'ℹ️'}</span><span>${msg}</span>`;
    this.container.appendChild(t);
    setTimeout(()=>{ t.classList.add('removing'); setTimeout(()=>t.remove(),300); },duration);
  }
};

// ── NAVBAR ────────────────────────────────────────────────────
function buildNavbar(active=''){
  const u=API.getUser(); if(!u) return;
  const ini=(u.fullName||u.username||'U').split(' ').map(w=>w[0]).join('').toUpperCase().slice(0,2);
  const nav=document.getElementById('main-navbar'); if(!nav) return;
  const links=[
    {id:'home',  label:'Home',    href:'home.html'},
    {id:'browse',label:'Browse',  href:'browse.html'},
  ];
  if(['CREATOR','PUBLISHER','ADMIN'].includes(u.role)) links.push({id:'creator',label:'Studio',href:'creator.html'});
  if(u.role==='ADMIN') links.push({id:'admin',label:'Admin',href:'admin.html'});

  nav.innerHTML=`
    <a href="home.html" class="nav-logo">Stream<span>Verse</span></a>
    <ul class="nav-links">
      ${links.map(l=>`<li><a href="${l.href}" class="${active===l.id?'active':''}">${l.label}</a></li>`).join('')}
    </ul>
    <div style="display:flex;align-items:center;gap:1rem;">
      <span class="nav-live"><span class="live-dot"></span><span class="ws-status">Connecting...</span></span>
      <div class="user-wrap">
        <div class="user-avatar" onclick="toggleDD()">${ini}</div>
        <div class="user-dropdown" id="user-dd">
          <div style="padding:.7rem 1.2rem;border-bottom:1px solid var(--border);">
            <div style="font-weight:600;color:#fff;font-size:.86rem;">${u.fullName||u.username}</div>
            <div style="font-size:.73rem;color:var(--muted);">${u.email}</div>
            <span class="badge badge-purple" style="margin-top:.3rem;">${u.role}</span>
          </div>
          <a class="dd-item" href="profile.html">👤 My Profile</a>
          <div class="dd-divider"></div>
          <button class="dd-item danger" onclick="logout()">🚪 Sign Out</button>
        </div>
      </div>
    </div>`;
}

function toggleDD(){ document.getElementById('user-dd')?.classList.toggle('open'); }
document.addEventListener('click',e=>{ if(!e.target.closest('.user-wrap')) document.querySelectorAll('.user-dropdown').forEach(d=>d.classList.remove('open')); });

// ── BUTTON LOADING ────────────────────────────────────────────
function setBtnLoading(id,loading,label=''){
  const b=document.getElementById(id); if(!b) return;
  if(loading){ b.dataset.orig=b.innerHTML; b.innerHTML=`<span class="spinner"></span> ${label||'Loading...'}`;  b.disabled=true; }
  else{ b.innerHTML=b.dataset.orig||label; b.disabled=false; }
}

// ── VALIDATION ────────────────────────────────────────────────
const V = {
  required(v,n='Field'){ return (!v||!v.trim())?`${n} is required`:null; },
  email(v){ if(!v?.trim()) return 'Email is required'; return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)?null:'Enter a valid email'; },
  password(v){ if(!v) return 'Password required'; if(v.length<8) return 'Min 8 characters'; if(!/[A-Z]/.test(v)) return 'Need uppercase letter'; if(!/[0-9]/.test(v)) return 'Need a number'; return null; },
  minLen(v,n,f='Field'){ return (!v||v.length<n)?`${f} must be at least ${n} characters`:null; },
  showErr(id,msg){ const inp=document.getElementById(id),err=document.getElementById(id+'-err'); if(inp)inp.classList.add('error'); if(err){err.textContent=msg;err.classList.add('show');} },
  clearErr(id){ const inp=document.getElementById(id),err=document.getElementById(id+'-err'); if(inp)inp.classList.remove('error'); if(err)err.classList.remove('show'); },
};

// ── HELPERS ───────────────────────────────────────────────────
function fmtNum(n){ if(n===null||n===undefined) return '0'; if(n>=1e6) return (n/1e6).toFixed(1)+'M'; if(n>=1e3) return (n/1e3).toFixed(1)+'K'; return n.toString(); }
function fmtMoney(n){ return '$'+(n||0).toFixed(2); }
function fmtDate(d){ return new Date(d).toLocaleDateString('en-US',{year:'numeric',month:'short',day:'numeric'}); }
function timeAgo(d){ const diff=Date.now()-new Date(d); const m=Math.floor(diff/60000); if(m<60) return `${m}m ago`; if(m<1440) return `${Math.floor(m/60)}h ago`; return `${Math.floor(m/1440)}d ago`; }
function animateCounter(el,newVal){
  el.classList.remove('counter-update');
  void el.offsetWidth; // reflow
  el.textContent=newVal;
  el.classList.add('counter-update');
}

// ── CONFIRM DIALOG ────────────────────────────────────────────
function confirmAction(msg,onConfirm){
  const overlay=document.createElement('div');
  overlay.className='modal-overlay open';
  overlay.innerHTML=`<div class="modal" style="max-width:380px;">
    <div class="modal-title">⚠️ Confirm</div>
    <p style="color:var(--muted);font-size:.88rem;margin:1rem 0;">${msg}</p>
    <div class="modal-actions">
      <button class="btn btn-ghost btn-sm" id="cancel-confirm">Cancel</button>
      <button class="btn btn-danger btn-sm" id="ok-confirm">Confirm</button>
    </div></div>`;
  document.body.appendChild(overlay);
  overlay.querySelector('#cancel-confirm').onclick=()=>overlay.remove();
  overlay.querySelector('#ok-confirm').onclick=()=>{ overlay.remove(); onConfirm(); };
}
