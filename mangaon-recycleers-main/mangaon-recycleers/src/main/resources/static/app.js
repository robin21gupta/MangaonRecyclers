// ══════════════════════════════════════════════════════════════════════════
//  MANGAON RECYCLEERS — app.js
//  Fix 1: supplierId / clientId null on form submit
//  Fix 2: 401 pe logout, 403 pe error throw
//  Fix 3: Date parse error — toInputDate() helper for edit forms
//  Fix 4: Master Inward/Outward — default 5, search = full list
//  Fix 5: Dashboard — latest 5, View All = full tab with all data
//  Fix 6: /api prefix added to all endpoints (Render deploy fix)
//  Fix 7: MASTER → ACCOUNT rename throughout
//  Fix 8: Material Outward — quantity + type fields added
//  Fix 9: Vendor type — PYROLYSIS_OIL ("Pyrolysis Oil") added
//  Fix 10: InwardMaterialType — TYRE removed, PYROLYSIS_OIL added
// ══════════════════════════════════════════════════════════════════════════

const API_BASE_URL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
    ? 'http://localhost:8080'
    : 'https://mangaon-recycleers-7.onrender.com';

function getToken()  { return localStorage.getItem('token'); }
function getRole() {
  const stored = localStorage.getItem('role');
  if (stored && stored.trim() !== '') return stored.trim();
  try {
    const token = getToken();
    if (!token) return '';
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.role || payload.roles || payload.authorities || payload.auth || '';
  } catch(e) { return ''; }
}
function isAdminOrMaster() {
  const raw = String(getRole()).toUpperCase().trim();
  return raw === 'ADMIN' || raw === 'ACCOUNT' || raw === 'ROLE_ADMIN' || raw === 'ROLE_ACCOUNT';
}
function requireAuth() {
  if (!getToken()) { window.location.href = '/login.html'; return false; }
  return true;
}
function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('role');
  window.location.href = '/login.html';
}
if (!requireAuth()) throw new Error('Not authenticated');

// ── GST STATE MAP ─────────────────────────────────────────────────────────
const gstStates = {
  "01":"Jammu & Kashmir","02":"Himachal Pradesh","03":"Punjab","04":"Chandigarh",
  "05":"Uttarakhand","06":"Haryana","07":"Delhi","08":"Rajasthan","09":"Uttar Pradesh",
  "10":"Bihar","11":"Sikkim","12":"Arunachal Pradesh","13":"Nagaland","14":"Manipur",
  "15":"Mizoram","16":"Tripura","17":"Meghalaya","18":"Assam","19":"West Bengal",
  "20":"Jharkhand","21":"Odisha","22":"Chhattisgarh","23":"Madhya Pradesh","24":"Gujarat",
  "25":"Daman & Diu","26":"Dadra & Nagar Haveli","27":"Maharashtra","28":"Andhra Pradesh",
  "29":"Karnataka","30":"Goa","31":"Lakshadweep","32":"Kerala","33":"Tamil Nadu",
  "34":"Puducherry","35":"Andaman & Nicobar","36":"Telangana","37":"Andhra Pradesh"
};
function getStateFromGST(gst) {
  if (!gst || gst.length < 2) return '';
  return gstStates[gst.substring(0,2)] || '';
}
function getEntityTypeFromGST(gst) {
  if (!gst || gst.length < 15) return '';
  const typeChar = gst.substring(2,12).charAt(3).toUpperCase();
  return ({
    'P':'Individual / Proprietorship','C':'Company','F':'Partnership Firm / LLP',
    'H':'HUF','A':'Association of Persons','T':'Trust','B':'Body of Individuals',
    'G':'Government','J':'Artificial Juridical Person','L':'Local Authority',
  })[typeChar] || 'Unknown';
}

function toInputDate(dateStr) {
  if (!dateStr) return '';
  if (dateStr.length === 10 && dateStr[2] === '-') {
    const [d, m, y] = dateStr.split('-');
    return `${y}-${m}-${d}`;
  }
  return dateStr;
}

function injectHiddenInput(selectId, hiddenId) {
  if (document.getElementById(hiddenId)) return;
  const sel = document.getElementById(selectId);
  if (!sel) return;
  const inp = document.createElement('input');
  inp.type = 'hidden'; inp.id = hiddenId; inp.name = hiddenId; inp.value = '';
  sel.parentElement.insertBefore(inp, sel);
}

// ══════════════════════════════════════════════════════════════════════════
//  AUTOCOMPLETE
// ══════════════════════════════════════════════════════════════════════════
(function injectAutocompleteStyles() {
  const style = document.createElement('style');
  style.textContent = `
    .ac-wrap { position: relative; }
    .ac-input-row { display: flex; align-items: center; gap: 0; position: relative; }
    .ac-input { width: 100%; background: var(--bg-card-2, #161f30); border: 1px solid var(--border-strong, rgba(255,255,255,.13)); border-radius: 10px; padding: .55rem .875rem .55rem 2.4rem; font-family: var(--font-display, 'Outfit', sans-serif); font-size: .855rem; color: var(--ink, #f0f4ff); outline: none; transition: border-color .15s, box-shadow .15s; caret-color: #818cf8; }
    .ac-input::placeholder { color: var(--ink-muted, #5c6a82); }
    .ac-input:focus { border-color: rgba(99,102,241,.55); box-shadow: 0 0 0 3px rgba(99,102,241,.12); }
    .ac-input.has-value { border-color: rgba(16,185,129,.4); background: rgba(16,185,129,.04); }
    .ac-icon { position: absolute; left: .75rem; top: 50%; transform: translateY(-50%); color: var(--ink-muted, #5c6a82); font-size: .8rem; pointer-events: none; z-index: 1; transition: color .15s; }
    .ac-wrap:focus-within .ac-icon { color: #818cf8; }
    .ac-clear { position: absolute; right: .5rem; top: 50%; transform: translateY(-50%); width: 22px; height: 22px; border-radius: 50%; border: none; background: rgba(255,255,255,.08); color: var(--ink-muted, #5c6a82); font-size: .7rem; cursor: pointer; display: none; align-items: center; justify-content: center; transition: all .13s; z-index: 2; }
    .ac-clear:hover { background: rgba(244,63,94,.2); color: #f43f5e; }
    .ac-wrap.has-val .ac-clear { display: flex; }
    .ac-dropdown { position: absolute; top: calc(100% + 5px); left: 0; right: 0; background: #0f1724; border: 1px solid rgba(99,102,241,.25); border-radius: 12px; box-shadow: 0 16px 48px rgba(0,0,0,.55), 0 0 0 1px rgba(99,102,241,.08); z-index: 9999; overflow: hidden; display: none; max-height: 280px; flex-direction: column; }
    .ac-dropdown.open { display: flex; }
    .ac-dd-search-wrap { padding: .55rem .65rem .45rem; border-bottom: 1px solid rgba(255,255,255,.06); position: relative; flex-shrink: 0; }
    .ac-dd-search { width: 100%; background: rgba(255,255,255,.05); border: 1px solid rgba(99,102,241,.2); border-radius: 7px; padding: .38rem .75rem .38rem 2rem; font-size: .78rem; font-family: 'JetBrains Mono', monospace; color: #f0f4ff; outline: none; transition: border-color .13s; }
    .ac-dd-search:focus { border-color: rgba(99,102,241,.5); }
    .ac-dd-search::placeholder { color: #3d4a60; font-size: .72rem; }
    .ac-dd-search-icon { position: absolute; left: 1.15rem; top: 50%; transform: translateY(-50%); color: #3d4a60; font-size: .72rem; pointer-events: none; }
    .ac-list { overflow-y: auto; flex: 1; padding: .3rem 0; }
    .ac-list::-webkit-scrollbar { width: 4px; }
    .ac-list::-webkit-scrollbar-thumb { background: rgba(99,102,241,.3); border-radius: 4px; }
    .ac-item { display: flex; align-items: center; gap: .6rem; padding: .52rem .875rem; cursor: pointer; transition: background .1s; border-left: 2px solid transparent; }
    .ac-item:hover, .ac-item.focused { background: rgba(99,102,241,.1); border-left-color: #6366f1; }
    .ac-item.selected { background: rgba(16,185,129,.08); border-left-color: #10b981; }
    .ac-item-avatar { width: 28px; height: 28px; border-radius: 7px; background: rgba(99,102,241,.15); color: #818cf8; display: flex; align-items: center; justify-content: center; font-family: 'JetBrains Mono', monospace; font-size: .65rem; font-weight: 700; flex-shrink: 0; text-transform: uppercase; }
    .ac-item-body { flex: 1; min-width: 0; }
    .ac-item-label { font-size: .83rem; font-weight: 600; color: #f0f4ff; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .ac-item-label mark { background: rgba(99,102,241,.35); color: #a5b4fc; border-radius: 2px; padding: 0 1px; }
    .ac-item-sub { font-family: 'JetBrains Mono', monospace; font-size: .65rem; color: #5c6a82; margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .ac-item-id { font-family: 'JetBrains Mono', monospace; font-size: .6rem; color: #3d4a60; flex-shrink: 0; }
    .ac-empty { padding: 1.25rem; text-align: center; font-size: .78rem; color: #3d4a60; font-family: 'JetBrains Mono', monospace; }
    .ac-empty i { display: block; font-size: 1.4rem; margin-bottom: .4rem; opacity: .2; }
    .ac-selected-chip { display: none; align-items: center; gap: .4rem; margin-top: .35rem; padding: .28rem .6rem; background: rgba(16,185,129,.1); border: 1px solid rgba(16,185,129,.25); border-radius: 6px; font-size: .72rem; font-family: 'JetBrains Mono', monospace; color: #34d399; }
    .ac-wrap.has-val .ac-selected-chip { display: flex; }
    .ac-selected-chip i { font-size: .65rem; }
  `;
  document.head.appendChild(style);
})();

function createAutocomplete({ selectId, hiddenId, items, labelFn, subFn, icon = 'bi-search', placeholder = 'Type to search…' }) {
  const originalSelect = document.getElementById(selectId);
  const hiddenInput    = document.getElementById(hiddenId);
  if (!originalSelect || !hiddenInput) { console.warn('[AC] Missing elements for', selectId, hiddenId); return { update: () => {}, setValue: () => {}, getValue: () => null }; }
  const container = originalSelect.parentElement;
  originalSelect.style.display = 'none';
  const wrap = document.createElement('div');
  wrap.className = 'ac-wrap';
  wrap.innerHTML = `<div class="ac-input-row"><i class="bi ${icon} ac-icon"></i><input type="text" class="ac-input" autocomplete="off" placeholder="${placeholder}" spellcheck="false"/><button type="button" class="ac-clear" title="Clear"><i class="bi bi-x-lg"></i></button></div><div class="ac-selected-chip"><i class="bi bi-check-circle-fill"></i><span class="chip-label"></span></div><div class="ac-dropdown"><div class="ac-dd-search-wrap"><i class="bi bi-search ac-dd-search-icon"></i><input type="text" class="ac-dd-search" placeholder="Search…" autocomplete="off"/></div><div class="ac-list"></div></div>`;
  container.insertBefore(wrap, originalSelect);
  const textInput=wrap.querySelector('.ac-input'), ddSearch=wrap.querySelector('.ac-dd-search'), ddList=wrap.querySelector('.ac-list'), dropdown=wrap.querySelector('.ac-dropdown'), clearBtn=wrap.querySelector('.ac-clear'), chipLabel=wrap.querySelector('.chip-label');
  let allItems=items||[], selectedId=null, focusedIdx=-1, filterQuery='';
  function highlight(text,query){if(!query)return escapeHtml(text);const idx=text.toLowerCase().indexOf(query.toLowerCase());if(idx<0)return escapeHtml(text);return escapeHtml(text.substring(0,idx))+'<mark>'+escapeHtml(text.substring(idx,idx+query.length))+'</mark>'+escapeHtml(text.substring(idx+query.length));}
  function renderList(query){filterQuery=query||'';const q2=filterQuery.toLowerCase().trim();const filtered=q2?allItems.filter(it=>{const label=labelFn(it)||'',sub=subFn?subFn(it)||'':'';return label.toLowerCase().includes(q2)||sub.toLowerCase().includes(q2)||String(it.id).includes(q2);}):allItems;ddList.innerHTML='';focusedIdx=-1;if(!filtered.length){ddList.innerHTML=`<div class="ac-empty"><i class="bi bi-inbox"></i>${q2?'No results for "'+escapeHtml(q2)+'"':'No items available'}</div>`;return;}filtered.forEach(item=>{const div=document.createElement('div');div.className='ac-item'+(String(item.id)===String(selectedId)?' selected':'');div.dataset.id=item.id;div.dataset.label=labelFn(item);const letter=(labelFn(item)||'?')[0].toUpperCase(),sub=subFn?subFn(item):'';div.innerHTML=`<div class="ac-item-avatar">${letter}</div><div class="ac-item-body"><div class="ac-item-label">${highlight(labelFn(item),q2)}</div>${sub?`<div class="ac-item-sub">${highlight(sub,q2)}</div>`:''}</div><span class="ac-item-id">#${item.id}</span>`;div.addEventListener('mousedown',e=>{e.preventDefault();selectItem(item);});ddList.appendChild(div);});}
  function selectItem(item){selectedId=item.id;hiddenInput.value=String(item.id);textInput.value=labelFn(item);if(chipLabel)chipLabel.textContent=labelFn(item)+(subFn&&subFn(item)?' · '+subFn(item):'');wrap.classList.add('has-val');textInput.classList.add('has-value');closeDropdown();hiddenInput.dispatchEvent(new Event('change',{bubbles:true}));}
  function clearSelection(){selectedId=null;hiddenInput.value='';textInput.value='';wrap.classList.remove('has-val');textInput.classList.remove('has-value');textInput.focus();openDropdown('');}
  function openDropdown(query){renderList(query);dropdown.classList.add('open');ddSearch.value=query||'';setTimeout(()=>ddSearch.focus(),50);}
  function closeDropdown(){dropdown.classList.remove('open');focusedIdx=-1;}
  function moveFocus(dir){const items=ddList.querySelectorAll('.ac-item');if(!items.length)return;items.forEach(el=>el.classList.remove('focused'));focusedIdx=Math.max(0,Math.min(items.length-1,focusedIdx+dir));items[focusedIdx].classList.add('focused');items[focusedIdx].scrollIntoView({block:'nearest'});}
  function selectFocused(){const focused=ddList.querySelector('.ac-item.focused');if(focused){const item=allItems.find(it=>String(it.id)===String(focused.dataset.id));if(item)selectItem(item);}}
  textInput.addEventListener('click',()=>openDropdown(filterQuery));
  textInput.addEventListener('keydown',e=>{if(!dropdown.classList.contains('open')){if(e.key==='ArrowDown'||e.key==='Enter')openDropdown('');return;}if(e.key==='Escape'){closeDropdown();e.preventDefault();}});
  ddSearch.addEventListener('input',e=>renderList(e.target.value));
  ddSearch.addEventListener('keydown',e=>{if(e.key==='ArrowDown'){moveFocus(1);e.preventDefault();}else if(e.key==='ArrowUp'){moveFocus(-1);e.preventDefault();}else if(e.key==='Enter'){selectFocused();e.preventDefault();}else if(e.key==='Escape'){closeDropdown();textInput.focus();}});
  clearBtn.addEventListener('click',clearSelection);
  document.addEventListener('mousedown',e=>{if(!wrap.contains(e.target))closeDropdown();});
  function update(newItems){allItems=newItems||[];if(dropdown.classList.contains('open'))renderList(filterQuery);if(selectedId!==null){const still=allItems.find(it=>String(it.id)===String(selectedId));if(!still)clearSelection();}}
  function setValue(id){if(!id){clearSelection();return;}const item=allItems.find(it=>String(it.id)===String(id));if(item)selectItem(item);else hiddenInput.value=String(id);}
  function getValue(){return selectedId;}
  return{update,setValue,getValue};
}

// ══════════════════════════════════════════════════════════════════════════
//  TYRE SAVE OVERLAY
// ══════════════════════════════════════════════════════════════════════════
(function buildTyreOverlay(){
  const style=document.createElement('style');
  style.textContent=`#tyre-overlay{display:none;position:fixed;inset:0;z-index:99999;background:rgba(9,14,26,0.75);backdrop-filter:blur(4px);align-items:center;justify-content:center;flex-direction:column;gap:20px;}#tyre-overlay.show{display:flex;}#tyre-overlay .tov-label{font-family:'Outfit',sans-serif;font-size:15px;font-weight:600;color:#e2e8f0;}#tyre-overlay .tov-sub{font-family:'JetBrains Mono',monospace;font-size:11px;color:rgba(226,232,240,0.5);margin-top:-12px;}@keyframes tyreFlyRight{0%{transform:translateX(0) rotate(var(--cur-rot,0deg));opacity:1;}100%{transform:translateX(58vw) rotate(calc(var(--cur-rot,0deg) + 720deg));opacity:0;}}`;
  document.head.appendChild(style);
  const overlay=document.createElement('div');overlay.id='tyre-overlay';
  overlay.innerHTML=`<svg id="tyre-wheel" width="96" height="96" viewBox="0 0 96 96" style="transform-origin:48px 48px;"><circle cx="48" cy="48" r="44" fill="#1a1a1a" stroke="#3a3a3a" stroke-width="2"/><circle cx="48" cy="48" r="36" fill="#111" stroke="#2a2a2a" stroke-width="1.5"/><circle cx="48" cy="48" r="15" fill="#2a2a2a" stroke="#444" stroke-width="1.5"/><circle cx="48" cy="48" r="5" fill="#555"/><line x1="48" y1="33" x2="48" y2="20" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="48" y1="63" x2="48" y2="76" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="33" y1="48" x2="20" y2="48" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="63" y1="48" x2="76" y2="48" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="36.7" y1="36.7" x2="27.5" y2="27.5" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="59.3" y1="59.3" x2="68.5" y2="68.5" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="59.3" y1="36.7" x2="68.5" y2="27.5" stroke="#606060" stroke-width="3" stroke-linecap="round"/><line x1="36.7" y1="59.3" x2="27.5" y2="68.5" stroke="#606060" stroke-width="3" stroke-linecap="round"/><ellipse cx="48" cy="22" rx="3.5" ry="5.5" fill="#2e2e2e" stroke="#555" stroke-width="1"/><ellipse cx="48" cy="74" rx="3.5" ry="5.5" fill="#2e2e2e" stroke="#555" stroke-width="1"/><ellipse cx="22" cy="48" rx="5.5" ry="3.5" fill="#2e2e2e" stroke="#555" stroke-width="1"/><ellipse cx="74" cy="48" rx="5.5" ry="3.5" fill="#2e2e2e" stroke="#555" stroke-width="1"/></svg><p class="tov-label" id="tov-label">Saving…</p><p class="tov-sub" id="tov-sub">Please wait</p>`;
  document.body.appendChild(overlay);
})();
let _tyreAngle=0,_tyreRaf=null;
function _tyreSpin(){_tyreAngle=(_tyreAngle+5)%360;const w=document.getElementById('tyre-wheel');if(w)w.style.transform=`rotate(${_tyreAngle}deg)`;_tyreRaf=requestAnimationFrame(_tyreSpin);}
function showTyreSaving(label){const ov=document.getElementById('tyre-overlay'),lb=document.getElementById('tov-label'),sb=document.getElementById('tov-sub'),w=document.getElementById('tyre-wheel');if(!ov)return;if(lb)lb.textContent=label||'Saving…';if(sb)sb.textContent='Please wait';w.style.cssText='transform-origin:48px 48px;';ov.classList.add('show');_tyreAngle=0;cancelAnimationFrame(_tyreRaf);_tyreSpin();}
function hideTyreSuccess(successLabel){return new Promise(resolve=>{cancelAnimationFrame(_tyreRaf);const ov=document.getElementById('tyre-overlay'),lb=document.getElementById('tov-label'),sb=document.getElementById('tov-sub'),w=document.getElementById('tyre-wheel');if(!ov){resolve();return;}if(lb)lb.textContent=successLabel||'Saved!';if(sb)sb.textContent='Moving forward…';w.style.setProperty('--cur-rot',_tyreAngle+'deg');w.style.animation='tyreFlyRight 0.65s cubic-bezier(.4,0,.2,1) forwards';setTimeout(()=>{ov.classList.remove('show');w.style.cssText='transform-origin:48px 48px;';_tyreAngle=0;resolve();},700);});}
function hideTyreError(){cancelAnimationFrame(_tyreRaf);const ov=document.getElementById('tyre-overlay');if(ov)ov.classList.remove('show');}

function applyGSTAutofill(gst,stateInputId,entityTypeInputId){const g=(gst||'').toUpperCase().trim();const stateEl=q(stateInputId),typeEl=q(entityTypeInputId);if(stateEl)stateEl.value=getStateFromGST(g);if(typeEl)typeEl.value=g.length===15?getEntityTypeFromGST(g):'';}
function q(id){return document.getElementById(id);}
function setMsg(id,msg,type){const el=q(id);if(!el)return;el.textContent=msg||'';el.className='form-feedback'+(type==='error'?' error':type==='success'?' success':'');}
function showToast(message,type){const el=q('toast');if(!el)return;el.textContent=message;el.className='toast-msg '+(type==='error'?'error':'success');el.classList.add('show');clearTimeout(el._toastTimer);el._toastTimer=setTimeout(()=>el.classList.remove('show'),3500);}
function toNumberOrNull(v){const s=String(v??'').trim();if(!s)return null;const n=Number(s);return Number.isFinite(n)?n:null;}
function toDecimalOrNull(v){const s=String(v??'').trim();if(!s)return null;const n=Number(s);return Number.isFinite(n)?n:null;}
function toStringOrNull(v){const s=String(v??'').trim();return s||null;}
function escapeHtml(s){if(s==null||s==='')return '—';const d=document.createElement('div');d.textContent=s;return d.innerHTML;}
function statusChip(status){if(!status)return '—';const map={ACTIVE:'chip chip-active',INACTIVE:'chip chip-inactive',SUSPENDED:'chip chip-suspended'};const cls=map[status]||'chip chip-inactive';return `<span class="${cls}">${status.charAt(0)+status.slice(1).toLowerCase()}</span>`;}

// ══════════════════════════════════════════════════════════════════════════
//  LIGHTBOX
// ══════════════════════════════════════════════════════════════════════════
(function buildLightbox(){
  const style=document.createElement('style');
  style.textContent=`#inv-lightbox{display:none;position:fixed;inset:0;z-index:100000;background:rgba(0,0,0,.92);backdrop-filter:blur(8px);align-items:center;justify-content:center;flex-direction:column;gap:16px;}#inv-lightbox.open{display:flex;}#inv-lightbox .lb-img-wrap{position:relative;max-width:92vw;max-height:85vh;border-radius:14px;overflow:hidden;box-shadow:0 0 0 1px rgba(255,255,255,.08),0 32px 80px rgba(0,0,0,.8);}#inv-lightbox img{display:block;max-width:92vw;max-height:85vh;object-fit:contain;border-radius:14px;}#inv-lightbox iframe{display:block;width:min(700px,92vw);height:min(85vh,800px);border:none;border-radius:14px;}#inv-lightbox .lb-meta{font-family:'JetBrains Mono',monospace;font-size:12px;color:rgba(255,255,255,.45);text-align:center;max-width:80vw;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;}#inv-lightbox .lb-actions{display:flex;gap:.5rem;}#inv-lightbox .lb-btn{display:flex;align-items:center;gap:.3rem;padding:.38rem .875rem;font-family:'JetBrains Mono',monospace;font-size:.7rem;font-weight:500;border:1px solid rgba(255,255,255,.15);border-radius:8px;background:rgba(255,255,255,.07);color:rgba(255,255,255,.7);cursor:pointer;transition:all .15s;text-decoration:none;}#inv-lightbox .lb-btn:hover{background:rgba(255,255,255,.15);color:#fff;}#inv-lightbox .lb-close{position:fixed;top:18px;right:22px;background:rgba(255,255,255,.1);border:1px solid rgba(255,255,255,.18);color:#fff;width:40px;height:40px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:18px;cursor:pointer;transition:background .15s;z-index:100001;}#inv-lightbox .lb-close:hover{background:rgba(244,63,94,.5);}#inv-lightbox .lb-loading{display:flex;flex-direction:column;align-items:center;gap:.75rem;color:rgba(255,255,255,.4);font-family:'JetBrains Mono',monospace;font-size:.78rem;}#inv-lightbox .lb-loading i{font-size:2rem;animation:spin .7s linear infinite;}@keyframes spin{to{transform:rotate(360deg);}}`;
  document.head.appendChild(style);
  const lb=document.createElement('div');lb.id='inv-lightbox';
  lb.innerHTML=`<button class="lb-close" id="lb-close" title="Close (Esc)"><i class="bi bi-x-lg"></i></button><div class="lb-img-wrap" id="lb-content"></div><div class="lb-meta" id="lb-meta"></div><div class="lb-actions" id="lb-actions"></div>`;
  document.body.appendChild(lb);
  document.getElementById('lb-close').onclick=closeLightbox;
  lb.addEventListener('click',e=>{if(e.target===lb)closeLightbox();});
  document.addEventListener('keydown',e=>{if(e.key==='Escape')closeLightbox();});
})();
function resolveInvoiceUrl(rawUrl){if(!rawUrl)return null;if(rawUrl.startsWith('blob:')||rawUrl.startsWith('/')||rawUrl.startsWith('http'))return rawUrl;return '/'+rawUrl;}
function openLightbox(src,label){const fullSrc=resolveInvoiceUrl(src);if(!fullSrc){showToast('No invoice file available','error');return;}const content=document.getElementById('lb-content'),metaEl=document.getElementById('lb-meta'),actionsEl=document.getElementById('lb-actions');content.innerHTML=`<div class="lb-loading"><i class="bi bi-arrow-repeat"></i><span>Loading invoice…</span></div>`;metaEl.textContent=label||'';actionsEl.innerHTML='';document.getElementById('inv-lightbox').classList.add('open');const isPdf=fullSrc.toLowerCase().includes('.pdf')||fullSrc.toLowerCase().includes('application/pdf');if(isPdf){content.innerHTML=`<iframe src="${fullSrc}" title="Invoice PDF"></iframe>`;actionsEl.innerHTML=`<a class="lb-btn" href="${fullSrc}" target="_blank" rel="noopener"><i class="bi bi-box-arrow-up-right"></i> Open in new tab</a>`;}else{const img=document.createElement('img');img.alt=label||'Invoice';img.onload=()=>{content.innerHTML='';content.appendChild(img);actionsEl.innerHTML=`<a class="lb-btn" href="${fullSrc}" target="_blank" rel="noopener"><i class="bi bi-box-arrow-up-right"></i> Open full size</a>`;};img.onerror=()=>{content.innerHTML=`<div class="lb-loading"><i class="bi bi-exclamation-circle"></i><span>Could not load image</span></div>`;};img.src=fullSrc;}}
function closeLightbox(){document.getElementById('inv-lightbox').classList.remove('open');const content=document.getElementById('lb-content');if(content)content.innerHTML='';}

// ══════════════════════════════════════════════════════════════════════════
//  INVOICE UPLOAD WIDGET
// ══════════════════════════════════════════════════════════════════════════
function createInvoiceWidget(prefix,entryType){
  const dz=q(`${prefix}-dz`),fileCam=q(`${prefix}-file-cam`),fileGal=q(`${prefix}-file-gal`),btnCam=q(`${prefix}-btn-cam`),btnGal=q(`${prefix}-btn-gal`),preview=q(`${prefix}-inv-preview`),previewImg=q(`${prefix}-preview-img`),fnameEl=q(`${prefix}-fname`),statusEl=q(`${prefix}-inv-status`),viewBtn=q(`${prefix}-view-btn`),removeBtn=q(`${prefix}-remove-btn`),invNumEl=q(`${prefix}-inv-num`);
  let pendingFile=null,uploadedDto=null,_blobUrl=null;
  function setStatus(type,text){if(!statusEl)return;statusEl.className='inv-status'+(type?' show '+type:'');statusEl.textContent=text||'';}
  function showPreview(file,serverUrl){if(!preview)return;const isPdf=file?file.type==='application/pdf':(serverUrl||'').toLowerCase().endsWith('.pdf');if(!isPdf&&previewImg){if(_blobUrl&&_blobUrl.startsWith('blob:')){URL.revokeObjectURL(_blobUrl);_blobUrl=null;}const src=serverUrl||(()=>{_blobUrl=URL.createObjectURL(file);return _blobUrl;})();previewImg.src=src;previewImg.style.display='block';previewImg.onclick=()=>openLightbox(src,invNumEl?.value||(file?.name)||'Invoice');}else if(previewImg){previewImg.style.display='none';}if(fnameEl)fnameEl.textContent=file?file.name:(serverUrl?'Uploaded file':'—');preview.style.display='flex';if(dz)dz.classList.add('has-file');}
  function handleFile(file){if(!file)return;pendingFile=file;uploadedDto=null;if(viewBtn)viewBtn.style.display='none';setStatus('','');showPreview(file,null);}
  if(btnCam)btnCam.addEventListener('click',e=>{e.stopPropagation();fileCam?.click();});
  if(fileCam)fileCam.addEventListener('change',()=>{if(fileCam.files[0])handleFile(fileCam.files[0]);});
  if(btnGal)btnGal.addEventListener('click',e=>{e.stopPropagation();fileGal?.click();});
  if(fileGal)fileGal.addEventListener('change',()=>{if(fileGal.files[0])handleFile(fileGal.files[0]);});
  if(dz){dz.addEventListener('dragover',e=>{e.preventDefault();dz.classList.add('drag-over');});dz.addEventListener('dragleave',()=>dz.classList.remove('drag-over'));dz.addEventListener('drop',e=>{e.preventDefault();dz.classList.remove('drag-over');const f=e.dataTransfer?.files?.[0];if(f)handleFile(f);});}
  if(removeBtn)removeBtn.addEventListener('click',()=>reset());
  if(viewBtn)viewBtn.addEventListener('click',()=>{if(!uploadedDto)return;const url=resolveInvoiceUrl(uploadedDto.viewUrl||uploadedDto.fileUrl||uploadedDto.url);if(url)openLightbox(url,uploadedDto.invoiceNumber||uploadedDto.originalFilename||'Invoice');else showToast('No preview URL available','error');});
  async function uploadPending(entryId){const invNum=invNumEl?.value?.trim()||'';if(!pendingFile&&!invNum)return null;setStatus('uploading','⏳ Uploading…');const fd=new FormData();if(pendingFile)fd.append('file',pendingFile);fd.append('entryType',entryType);fd.append('entryId',String(entryId));fd.append('invoiceNumber',invNum);try{uploadedDto=await httpMultipart(`${api.invoices}/upload`,fd);setStatus('success','✓ Saved');if(uploadedDto?.viewUrl&&viewBtn)viewBtn.style.display='inline-flex';return uploadedDto;}catch(err){setStatus('failed','✗ Failed');showToast('Invoice upload failed: '+err.message,'error');return null;}}
  async function loadForEntry(entryId){try{const token=getToken();const res=await fetch(`${api.invoices}/latest?entryType=${entryType}&entryId=${entryId}`,{headers:token?{'Authorization':'Bearer '+token}:{}});if(!res.ok)return;uploadedDto=await res.json();if(!uploadedDto)return;if(invNumEl&&uploadedDto.invoiceNumber)invNumEl.value=uploadedDto.invoiceNumber;const url=resolveInvoiceUrl(uploadedDto.viewUrl||uploadedDto.fileUrl||uploadedDto.url);if(url){showPreview(null,url);if(viewBtn)viewBtn.style.display='inline-flex';setStatus('success','✓ Uploaded');}}catch(_){}}
  function reset(){pendingFile=null;uploadedDto=null;if(_blobUrl&&_blobUrl.startsWith('blob:')){URL.revokeObjectURL(_blobUrl);_blobUrl=null;}if(invNumEl)invNumEl.value='';if(previewImg){previewImg.src='';previewImg.style.display='none';previewImg.onclick=null;}if(preview)preview.style.display='none';if(dz)dz.classList.remove('has-file','drag-over');if(fnameEl)fnameEl.textContent='—';if(viewBtn)viewBtn.style.display='none';if(fileCam)fileCam.value='';if(fileGal)fileGal.value='';setStatus('','');}
  return{uploadPending,loadForEntry,reset};
}

let tiWidget=null,moWidget=null;
function rowDeleteButton(onDelete){const btn=document.createElement('button');btn.type='button';btn.className='btn btn-outline-danger btn-sm';btn.innerHTML='<i class="bi bi-trash3"></i>';btn.title='Delete';btn.addEventListener('click',onDelete);return btn;}
function rowEditButton(label,onClick){const btn=document.createElement('button');btn.type='button';btn.className='btn btn-outline-primary btn-sm';btn.innerHTML='<i class="bi bi-pencil"></i>';btn.title=label;btn.addEventListener('click',onClick);return btn;}
function invoiceViewButton(inv){if(!inv)return null;const btn=document.createElement('button');btn.type='button';btn.className='btn btn-inv-view btn-sm';btn.title=inv.invoiceNumber?`Invoice: ${inv.invoiceNumber}`:'View Invoice';if(inv.viewUrl||inv.fileUrl||inv.url){const url=resolveInvoiceUrl(inv.viewUrl||inv.fileUrl||inv.url);btn.innerHTML=`<i class="bi bi-eye"></i>${inv.invoiceNumber?' '+escapeHtml(inv.invoiceNumber):''}`;btn.addEventListener('click',()=>openLightbox(url,inv.invoiceNumber||inv.originalFilename||'Invoice'));}else if(inv.invoiceNumber){btn.innerHTML=`<i class="bi bi-hash"></i> ${escapeHtml(inv.invoiceNumber)}`;btn.style.cursor='default';}else{return null;}return btn;}

const sectionMeta={dashboard:{title:'Dashboard',sub:'Overview of operations'},suppliers:{title:'Vendor Onboarding',sub:'Manage your supplier / vendor network'},clients:{title:'Client Onboarding',sub:'Manage clients'},uses:{title:'Uses',sub:'Material usage categories'},tyreInwards:{title:'Master Inward',sub:'Record incoming tyres and materials'},materialOutwards:{title:'Master Outward',sub:'Record sales – oil, carbon, steel'}};
function showTab(tab){document.querySelectorAll('.app-nav .nav-link[data-tab]').forEach(b=>{const isActive=b.dataset.tab===tab;b.classList.toggle('active',isActive);b.setAttribute('aria-selected',String(isActive));});['dashboard','suppliers','clients','uses','tyreInwards','materialOutwards'].forEach(p=>{const panel=q('panel-'+p);if(!panel)return;if(p===tab){panel.classList.remove('d-none');panel.style.display='';}else{panel.classList.add('d-none');panel.style.display='none';}});const meta=sectionMeta[tab];if(meta){const t=q('topbar-section-title'),s=q('topbar-section-sub');if(t)t.textContent=meta.title;if(s)s.textContent=meta.sub;}}

async function http(url,opts={}){const token=getToken();const res=await fetch(url,{headers:{'Content-Type':'application/json',...(token?{'Authorization':'Bearer '+token}:{}),...(opts.headers||{})},...opts});if(res.status===401){localStorage.removeItem('token');localStorage.removeItem('username');localStorage.removeItem('role');window.location.href='/login.html';return;}if(!res.ok){const text=await res.text().catch(()=>'');throw new Error(text||res.status+' '+res.statusText);}if(res.status===204)return null;return res.json();}
async function httpMultipart(url,formData){const token=getToken();const res=await fetch(url,{method:'POST',headers:token?{'Authorization':'Bearer '+token}:{},body:formData});if(res.status===401){localStorage.removeItem('token');localStorage.removeItem('username');localStorage.removeItem('role');window.location.href='/login.html';return;}if(!res.ok){const text=await res.text().catch(()=>'');throw new Error(text||`${res.status} ${res.statusText}`);}if(res.status===204)return null;return res.json();}

const api = {
  suppliers:        `${API_BASE_URL}/api/suppliers`,
  clients:          `${API_BASE_URL}/api/clients`,
  uses:             `${API_BASE_URL}/api/uses`,
  tyreInwards:      `${API_BASE_URL}/api/tyre-inwards`,
  materialOutwards: `${API_BASE_URL}/api/material-outwards`,
  invoices:         `${API_BASE_URL}/api/invoices`
};
let editingSupplierId=null,editingClientId=null,editingTyreInwardId=null,editingOutwardId=null;
let _dashInwards=[],_dashOutwards=[];
let acSupplier=null,acClient=null,acUseInward=null,acUseOutward=null;

// ✅ FIX 10: TYRE removed, PYROLYSIS_OIL is now the inward oil type
const inwardMaterialLabel = {
  PYROLYSIS_OIL: 'Pyrolysis Oil',
  CRUMB_RUBBER:  'Crumb Rubber',
  WOOD:          'Wood',
  OTHERS:        'Others'
};
const inwardChipClass = {
  PYROLYSIS_OIL: 'chip-inward-tyre',
  CRUMB_RUBBER:  'chip-inward-crumb-rubber',
  WOOD:          'chip-inward-wood',
  OTHERS:        'chip-inward-others'
};
const inwardTypeLabel = {
  PYROLYSIS_OIL: 'Pyrolysis Oil',
  CRUMB_RUBBER:  'Crumb Rubber',
  WOOD:          'Wood',
  OTHERS:        'Others'
};

function startEditSupplier(s){editingSupplierId=s.id;const form=q('form-supplier');if(!form)return;form.elements['supplierName'].value=s.supplierName??'';form.elements['individualName'].value=s.individualName??'';form.elements['mobileNo'].value=s.mobileNo??'';form.elements['gstNo'].value=s.gstNo??'';q('supplier-state').value=getStateFromGST(s.gstNo??'');form.elements['email'].value=s.email??'';form.elements['address'].value=s.address??'';if(form.elements['vendorType'])form.elements['vendorType'].value=s.vendorType??'';if(form.elements['entityStatus'])form.elements['entityStatus'].value=s.entityStatus??'ACTIVE';const gstAppl=s.gstApplicable==='NO'?'NO':'YES';form.querySelectorAll('input[name="gstApplicable"]').forEach(r=>r.checked=r.value===gstAppl);toggleGSTBlock('supplier',gstAppl==='YES');setMsg('supplier-msg','Editing: '+(s.supplierName||'Vendor #'+s.id),'success');showTab('suppliers');const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Update Vendor';const can=q('supplier-cancel-btn');if(can)can.style.display='inline-flex';}
function cancelEditSupplier(){editingSupplierId=null;const form=q('form-supplier');if(form){form.reset();const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Save Vendor';toggleGSTBlock('supplier',true);}const can=q('supplier-cancel-btn');if(can)can.style.display='none';setMsg('supplier-msg','');}
function startEditClient(c){editingClientId=c.id;const form=q('form-client');if(!form)return;form.elements['clientName'].value=c.clientName??'';form.elements['individualName'].value=c.individualName??'';form.elements['mobileNo'].value=c.mobileNo??'';form.elements['gstNo'].value=c.gstNo??'';q('client-state').value=getStateFromGST(c.gstNo??'');form.elements['email'].value=c.email??'';form.elements['address'].value=c.address??'';if(form.elements['clientType'])form.elements['clientType'].value=c.clientType??'';if(form.elements['entityStatus'])form.elements['entityStatus'].value=c.entityStatus??'ACTIVE';const gstAppl=c.gstApplicable==='NO'?'NO':'YES';form.querySelectorAll('input[name="gstApplicable"]').forEach(r=>r.checked=r.value===gstAppl);toggleGSTBlock('client',gstAppl==='YES');setMsg('client-msg','Editing: '+(c.clientName||'Client #'+c.id),'success');showTab('clients');const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Update Client';const can=q('client-cancel-btn');if(can)can.style.display='inline-flex';}
function cancelEditClient(){editingClientId=null;const form=q('form-client');if(form){form.reset();const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Save Client';toggleGSTBlock('client',true);}const can=q('client-cancel-btn');if(can)can.style.display='none';setMsg('client-msg','');}

function setTableLoading(tbodyId,colSpan){const tb=q(tbodyId);if(tb)tb.innerHTML=`<tr><td colspan="${colSpan}" class="table-loading">Loading</td></tr>`;}
function setTableEmpty(tbodyId,colSpan){const tb=q(tbodyId);if(tb)tb.innerHTML=`<tr><td colspan="${colSpan}" class="table-empty">No data found</td></tr>`;}

function loadSuppliers(list){const tbody=q('table-suppliers');if(!tbody)return;tbody.innerHTML='';const data=(list||[]).slice(-5).reverse();if(!data.length){setTableEmpty('table-suppliers',9);return;}data.forEach(s=>{const tr=document.createElement('tr');tr.innerHTML=`<td>${escapeHtml(s.id)}</td><td><strong>${escapeHtml(s.supplierName)}</strong></td><td>${escapeHtml(s.individualName)}</td><td>${escapeHtml(s.mobileNo)}</td><td>${escapeHtml(s.email)}</td><td><code style="font-size:0.72rem;">${escapeHtml(s.gstNo)}</code></td><td>${escapeHtml(getStateFromGST(s.gstNo??''))}</td><td>${statusChip(s.entityStatus||'ACTIVE')}</td><td class="th-actions cell-actions"></td>`;const cell=tr.querySelector('td.cell-actions');cell.appendChild(rowEditButton('Edit',()=>startEditSupplier(s)));cell.appendChild(rowDeleteButton(async()=>{if(!confirm('Delete this vendor?'))return;try{await http(`${api.suppliers}/${s.id}`,{method:'DELETE'});showToast('Vendor deleted.');await reloadAll();}catch(err){showToast(err.message||'Delete failed','error');}}));tbody.appendChild(tr);});}

function loadClients(list){const tbody=q('table-clients');if(!tbody)return;tbody.innerHTML='';const data=(list||[]).slice(-5).reverse();if(!data.length){setTableEmpty('table-clients',9);return;}data.forEach(c=>{const tr=document.createElement('tr');tr.innerHTML=`<td>${escapeHtml(c.id)}</td><td><strong>${escapeHtml(c.clientName)}</strong></td><td>${escapeHtml(c.individualName)}</td><td>${escapeHtml(c.mobileNo)}</td><td>${escapeHtml(c.email)}</td><td><code style="font-size:0.72rem;">${escapeHtml(c.gstNo)}</code></td><td>${escapeHtml(getStateFromGST(c.gstNo??''))}</td><td>${statusChip(c.entityStatus||'ACTIVE')}</td><td class="th-actions cell-actions"></td>`;const cell=tr.querySelector('td.cell-actions');cell.appendChild(rowEditButton('Edit',()=>startEditClient(c)));cell.appendChild(rowDeleteButton(async()=>{if(!confirm('Delete this client?'))return;try{await http(`${api.clients}/${c.id}`,{method:'DELETE'});showToast('Client deleted.');await reloadAll();}catch(err){showToast(err.message||'Delete failed','error');}}));tbody.appendChild(tr);});}

function loadUses(list){const tbody=q('table-uses');if(!tbody)return;tbody.innerHTML='';const data=(list||[]).slice(-5).reverse();if(!data.length){setTableEmpty('table-uses',4);return;}data.forEach(u=>{const tr=document.createElement('tr');tr.innerHTML=`<td>${escapeHtml(u.id)}</td><td>${escapeHtml(u.useName)}</td><td class="cell-desc">${escapeHtml(u.description)}</td><td class="th-actions cell-actions"></td>`;tr.querySelector('td.cell-actions').appendChild(rowDeleteButton(async()=>{if(!confirm('Delete this use?'))return;try{await http(`${api.uses}/${u.id}`,{method:'DELETE'});showToast('Use deleted.');await reloadAll();}catch(err){showToast(err.message||'Delete failed','error');}}));tbody.appendChild(tr);});}

// ══════════════════════════════════════════════════════════════════════════
//  MASTER INWARD/OUTWARD — store ALL, show 5 by default, search = full
// ══════════════════════════════════════════════════════════════════════════
let _allTyreInwards=[],_allMaterialOutwards=[];

function loadTyreInwards(list){
  _allTyreInwards=(list||[]).slice().reverse();
  const searchVal=document.getElementById('inward-search')?.value||'';
  if(searchVal.trim())filterInwards(searchVal);
  else renderInwards(_allTyreInwards.slice(0,5));
}
function loadMaterialOutwards(list){
  _allMaterialOutwards=(list||[]).slice().reverse();
  const searchVal=document.getElementById('outward-search')?.value||'';
  if(searchVal.trim())filterOutwards(searchVal);
  else renderOutwards(_allMaterialOutwards.slice(0,5));
}

// ══════════════════════════════════════════════════════════════════════════
//  DASHBOARD
// ══════════════════════════════════════════════════════════════════════════
function renderDashInwards(data){
  const tbody=q('dash-table-inwards'),badge=q('dash-inward-badge');
  if(!tbody)return;tbody.innerHTML='';
  if(!data.length){tbody.innerHTML=`<tr><td colspan="11" class="table-empty">No entries</td></tr>`;return;}
  if(badge)badge.textContent=data.length;
  data.forEach(t=>{
    const matLabel=inwardMaterialLabel[t.materialType]||t.materialType||'—';
    const chipCls=inwardChipClass[t.materialType]||'chip-inward-others';
    const matHtml=t.materialType?`<span class="chip ${chipCls}">${escapeHtml(matLabel)}</span>`:'—';
    const tr=document.createElement('tr');
    tr.innerHTML=`<td>${escapeHtml(t.id)}</td><td>${matHtml}</td><td>${escapeHtml(t.supplier?.supplierName)}</td><td>${escapeHtml(t.vehicleNumber)}</td><td>${escapeHtml(t.driverName)}${t.driverNumber?`<br><small style="color:var(--ink-muted)">${escapeHtml(t.driverNumber)}</small>`:''}</td><td>${escapeHtml(t.date)}</td><td>${escapeHtml(t.type)}</td><td>${escapeHtml(t.quantity)}</td><td><strong>${escapeHtml(t.netWeight)}</strong></td><td>${t.rate!=null?'₹'+escapeHtml(t.rate):'—'}</td><td><strong>${t.totalAmount!=null?'₹'+escapeHtml(t.totalAmount):'—'}</strong></td>`;
    tbody.appendChild(tr);
  });
}

function renderDashOutwards(data){
  const tbody=q('dash-table-outwards'),badge=q('dash-outward-badge');
  if(!tbody)return;tbody.innerHTML='';
  if(!data.length){tbody.innerHTML=`<tr><td colspan="12" class="table-empty">No entries</td></tr>`;return;}
  if(badge)badge.textContent=data.length;
  data.forEach(m=>{
    const typeLabel={TYRE_OIL:'Tyre Oil',CARBON_POWER:'Carbon Powder',STEEL:'Steel'}[m.materialType]||m.materialType;
    const tr=document.createElement('tr');
    tr.innerHTML=`<td>${escapeHtml(m.id)}</td><td><span class="chip chip-blue">${escapeHtml(typeLabel)}</span></td><td>${escapeHtml(m.client?.clientName)}</td><td>${escapeHtml(m.use?.useName)}</td><td>${escapeHtml(m.vehicleNumber)}</td><td>${escapeHtml(m.driverName)}${m.driverNumber?`<br><small style="color:var(--ink-muted)">${escapeHtml(m.driverNumber)}</small>`:''}</td><td>${escapeHtml(m.dateOfSale)}</td><td>${escapeHtml(m.type)}</td><td>${escapeHtml(m.quantity)}</td><td>${escapeHtml(m.netWeight)}</td><td>₹${escapeHtml(m.rate)}</td><td><strong>₹${escapeHtml(m.totalAmount)}</strong></td>`;
    tbody.appendChild(tr);
  });
}

function loadDashboard(suppliers,clients,tyreInwards,materialOutwards){
  const sc=q('dash-vendor-count'),cc=q('dash-client-count'),ic=q('dash-inward-count'),oc=q('dash-outward-count');
  if(sc)sc.textContent=(suppliers||[]).length;if(cc)cc.textContent=(clients||[]).length;
  if(ic)ic.textContent=(tyreInwards||[]).length;if(oc)oc.textContent=(materialOutwards||[]).length;
  _dashInwards=(tyreInwards||[]).slice().reverse();
  _dashOutwards=(materialOutwards||[]).slice().reverse();
  renderDashInwards(_dashInwards.slice(0,5));
  renderDashOutwards(_dashOutwards.slice(0,5));
}

function filterDashInwards(query){
  const q2=query.toLowerCase().trim();
  if(!q2){renderDashInwards(_dashInwards.slice(0,5));return;}
  renderDashInwards(_dashInwards.filter(t=>String(t.id).includes(q2)||(inwardMaterialLabel[t.materialType]||t.materialType||'').toLowerCase().includes(q2)||(t.supplier?.supplierName||'').toLowerCase().includes(q2)||(t.supplier?.id?String(t.supplier.id).includes(q2):false)||(t.vehicleNumber||'').toLowerCase().includes(q2)||(t.driverName||'').toLowerCase().includes(q2)||(t.driverNumber||'').toLowerCase().includes(q2)||(t.type||'').toLowerCase().includes(q2)||(t.date||'').includes(q2)||(t.notes||'').toLowerCase().includes(q2)));
}
function filterDashOutwards(query){
  const q2=query.toLowerCase().trim();
  if(!q2){renderDashOutwards(_dashOutwards.slice(0,5));return;}
  const typeMap={TYRE_OIL:'tyre oil',CARBON_POWER:'carbon powder',STEEL:'steel'};
  renderDashOutwards(_dashOutwards.filter(m=>String(m.id).includes(q2)||(typeMap[m.materialType]||m.materialType||'').toLowerCase().includes(q2)||(m.client?.clientName||'').toLowerCase().includes(q2)||(m.client?.id?String(m.client.id).includes(q2):false)||(m.use?.useName||'').toLowerCase().includes(q2)||(m.vehicleNumber||'').toLowerCase().includes(q2)||(m.driverName||'').toLowerCase().includes(q2)||(m.driverNumber||'').toLowerCase().includes(q2)||(m.dateOfSale||'').includes(q2)||(m.type||'').toLowerCase().includes(q2)||(m.notes||'').toLowerCase().includes(q2)));
}

function fillSelect(select,items,labelFn,allowEmpty=true){if(!select)return;select.innerHTML='';if(allowEmpty){const opt=document.createElement('option');opt.value='';opt.textContent='— Select —';select.appendChild(opt);}items.forEach(it=>{const opt=document.createElement('option');opt.value=it.id;opt.textContent=labelFn(it);select.appendChild(opt);});}

async function reloadAll(){
  setTableLoading('table-suppliers',9);setTableLoading('table-clients',9);setTableLoading('table-uses',4);setTableLoading('table-tyreInwards',13);setTableLoading('table-materialOutwards',14);
  try{
    const[suppliers,clients,uses,tyreInwards,materialOutwards]=await Promise.all([http(api.suppliers),http(api.clients),http(api.uses),http(api.tyreInwards),http(api.materialOutwards)]);
    loadSuppliers(suppliers);loadClients(clients);loadUses(uses);loadTyreInwards(tyreInwards);loadMaterialOutwards(materialOutwards);loadDashboard(suppliers,clients,tyreInwards,materialOutwards);
    if(acSupplier){acSupplier.update(suppliers||[]);}else{acSupplier=createAutocomplete({selectId:'ti-supplier',hiddenId:'ti-supplier-hidden',items:suppliers||[],labelFn:s=>s.supplierName||'',subFn:s=>s.mobileNo||s.gstNo||'',icon:'bi-truck',placeholder:'Type vendor name…'});}
    if(acClient){acClient.update(clients||[]);}else{acClient=createAutocomplete({selectId:'mo-client',hiddenId:'mo-client-hidden',items:clients||[],labelFn:c=>c.clientName||'',subFn:c=>c.mobileNo||c.gstNo||'',icon:'bi-building',placeholder:'Type client name…'});}
    if(acUseInward){acUseInward.update(uses||[]);}else{acUseInward=createAutocomplete({selectId:'ti-use',hiddenId:'ti-use-hidden',items:uses||[],labelFn:u=>u.useName||'',subFn:u=>u.description||'',icon:'bi-tags',placeholder:'Type use / purpose…'});}
    if(acUseOutward){acUseOutward.update(uses||[]);}else{acUseOutward=createAutocomplete({selectId:'mo-use',hiddenId:'mo-use-hidden',items:uses||[],labelFn:u=>u.useName||'',subFn:u=>u.description||'',icon:'bi-tags',placeholder:'Type use / purpose…'});}
  }catch(err){console.error(err);showToast(err.message||'Failed to load data','error');}
}

function toggleGSTBlock(prefix,show){const block=q(`${prefix}-gst-block`);if(block)block.style.display=show?'':'none';}
function attachNetWeightCalc(grossId,tareId,netId){const calc=()=>{const g=parseFloat(q(grossId)?.value)||0,t=parseFloat(q(tareId)?.value)||0,netEl=q(netId);if(netEl)netEl.value=(g>0||t>0)?(g-t).toFixed(3):'';};q(grossId)?.addEventListener('input',calc);q(tareId)?.addEventListener('input',calc);}

document.addEventListener('DOMContentLoaded',()=>{
  const usernameEl=q('topbar-username');if(usernameEl){const _n=localStorage.getItem('username')||'User';usernameEl.textContent=_n.charAt(0).toUpperCase()+_n.slice(1);}
  q('logout-btn')?.addEventListener('click',logout);
  injectHiddenInput('ti-supplier','ti-supplier-hidden');injectHiddenInput('ti-use','ti-use-hidden');injectHiddenInput('mo-client','mo-client-hidden');injectHiddenInput('mo-use','mo-use-hidden');
  const isAdmin=isAdminOrMaster();
  const reportsSection=document.getElementById('nav-reports-section');const adminSection=document.getElementById('nav-admin-section');
  if(reportsSection)reportsSection.style.display=isAdmin?'':'none';if(adminSection)adminSection.style.display=isAdmin?'':'none';
  q('dash-card-vendors')?.addEventListener('click',()=>showTab('suppliers'));
  q('dash-card-clients')?.addEventListener('click',()=>showTab('clients'));
  q('dash-card-inwards')?.addEventListener('click',()=>showTab('tyreInwards'));
  q('dash-card-outwards')?.addEventListener('click',()=>showTab('materialOutwards'));
  q('dash-inward-view-all')?.addEventListener('click',()=>{showTab('tyreInwards');const inp=q('inward-search');if(inp)inp.value='';renderInwards(_allTyreInwards);});
  q('dash-outward-view-all')?.addEventListener('click',()=>{showTab('materialOutwards');const inp=q('outward-search');if(inp)inp.value='';renderOutwards(_allMaterialOutwards);});
  q('dash-inward-search')?.addEventListener('input',e=>filterDashInwards(e.target.value));
  q('dash-outward-search')?.addEventListener('input',e=>filterDashOutwards(e.target.value));
  q('supplier-gst')?.addEventListener('input',function(){this.value=this.value.toUpperCase();applyGSTAutofill(this.value,'supplier-state','supplier-entity-type-auto');});
  q('client-gst')?.addEventListener('input',function(){this.value=this.value.toUpperCase();applyGSTAutofill(this.value,'client-state','client-entity-type-auto');});
  document.querySelectorAll('#form-supplier input[name="gstApplicable"]').forEach(r=>{r.addEventListener('change',function(){toggleGSTBlock('supplier',this.value==='YES');});});
  document.querySelectorAll('#form-client input[name="gstApplicable"]').forEach(r=>{r.addEventListener('change',function(){toggleGSTBlock('client',this.value==='YES');});});
  attachNetWeightCalc('ti-gross','ti-tare','ti-net');attachNetWeightCalc('mo-gross','mo-tare','mo-net');
  function calcInwardTotal(){const net=parseFloat(q('ti-net')?.value)||0,rate=parseFloat(q('ti-rate')?.value)||0,el=q('ti-total');if(el)el.value=(net>0&&rate>0)?(net*rate).toFixed(2):'';}
  q('ti-net')?.addEventListener('input',calcInwardTotal);q('ti-rate')?.addEventListener('input',calcInwardTotal);
  function calcOutwardTotal(){const net=parseFloat(q('mo-net')?.value)||0,rate=parseFloat(q('mo-rate')?.value)||0,el=q('mo-total');if(el)el.value=(net>0&&rate>0)?(net*rate).toFixed(2):'';}
  q('mo-net')?.addEventListener('input',calcOutwardTotal);q('mo-rate')?.addEventListener('input',calcOutwardTotal);
  tiWidget=createInvoiceWidget('ti','INWARD');moWidget=createInvoiceWidget('mo','OUTWARD');
  q('inward-search')?.addEventListener('input',e=>{const v=e.target.value.trim();if(v)filterInwards(v);else renderInwards(_allTyreInwards.slice(0,5));});
  q('outward-search')?.addEventListener('input',e=>{const v=e.target.value.trim();if(v)filterOutwards(v);else renderOutwards(_allMaterialOutwards.slice(0,5));});
  const today=new Date().toISOString().split('T')[0];if(q('ti-date'))q('ti-date').value=today;if(q('mo-date'))q('mo-date').value=today;
});

document.addEventListener('click',async(e)=>{
  const tabBtn=e.target.closest('[data-tab]');if(tabBtn){showTab(tabBtn.dataset.tab);return;}
  const actionBtn=e.target.closest('[data-action]');if(!actionBtn)return;
  const a=actionBtn.dataset.action;
  const reloadActions=['reload-suppliers','reload-clients','reload-uses','reload-tyreInwards','reload-materialOutwards'];
  if(reloadActions.includes(a)){await reloadAll();return;}
  if(a==='clear-inward-search'){const inp=q('inward-search');if(inp){inp.value='';renderInwards(_allTyreInwards.slice(0,5));inp.focus();}return;}
  if(a==='clear-outward-search'){const inp=q('outward-search');if(inp){inp.value='';renderOutwards(_allMaterialOutwards.slice(0,5));inp.focus();}return;}
  try{
    if(a==='search-suppliers'){const qv=q('supplier-search')?.value?.trim()||'';const list=qv?await http(`${api.suppliers}/search?name=${encodeURIComponent(qv)}`):await http(api.suppliers);loadSuppliers(list);}
    else if(a==='search-clients'){const qv=q('client-search')?.value?.trim()||'';const list=qv?await http(`${api.clients}/search?name=${encodeURIComponent(qv)}`):await http(api.clients);loadClients(list);}
    else if(a==='search-uses'){const qv=q('use-search')?.value?.trim()||'';const list=qv?await http(`${api.uses}/search?q=${encodeURIComponent(qv)}`):await http(api.uses);loadUses(list);}
  }catch(err){showToast(err.message||'Search failed','error');}
});

q('form-supplier')?.addEventListener('submit',async(e)=>{e.preventDefault();setMsg('supplier-msg','');const fd=new FormData(e.target),gstApplicable=fd.get('gstApplicable')||'YES';const payload={supplierName:fd.get('supplierName'),individualName:toStringOrNull(fd.get('individualName')),mobileNo:toStringOrNull(fd.get('mobileNo')),gstApplicable,gstNo:gstApplicable==='YES'?toStringOrNull(fd.get('gstNo')):null,vendorType:toStringOrNull(fd.get('vendorType')),entityStatus:toStringOrNull(fd.get('entityStatus'))||'ACTIVE',email:toStringOrNull(fd.get('email')),address:toStringOrNull(fd.get('address'))};const isEdit=editingSupplierId!=null;showTyreSaving(isEdit?'Updating vendor...':'Saving vendor...');try{if(isEdit){await http(`${api.suppliers}/${editingSupplierId}`,{method:'PUT',body:JSON.stringify(payload)});await hideTyreSuccess('Vendor updated!');cancelEditSupplier();showToast('Vendor updated.');}else{await http(api.suppliers,{method:'POST',body:JSON.stringify(payload)});await hideTyreSuccess('Vendor saved!');e.target.reset();toggleGSTBlock('supplier',true);if(q('supplier-state'))q('supplier-state').value='';if(q('supplier-entity-type-auto'))q('supplier-entity-type-auto').value='';setMsg('supplier-msg','Vendor saved.','success');showToast('Vendor saved.');}await reloadAll();}catch(err){hideTyreError();setMsg('supplier-msg',err.message||'Save failed','error');showToast(err.message||'Save failed','error');}});
q('form-client')?.addEventListener('submit',async(e)=>{e.preventDefault();setMsg('client-msg','');const fd=new FormData(e.target),gstApplicable=fd.get('gstApplicable')||'YES';const payload={clientName:fd.get('clientName'),individualName:toStringOrNull(fd.get('individualName')),mobileNo:toStringOrNull(fd.get('mobileNo')),gstApplicable,gstNo:gstApplicable==='YES'?toStringOrNull(fd.get('gstNo')):null,clientType:toStringOrNull(fd.get('clientType')),entityStatus:toStringOrNull(fd.get('entityStatus'))||'ACTIVE',email:toStringOrNull(fd.get('email')),address:toStringOrNull(fd.get('address'))};const isEdit=editingClientId!=null;showTyreSaving(isEdit?'Updating client...':'Saving client...');try{if(isEdit){await http(`${api.clients}/${editingClientId}`,{method:'PUT',body:JSON.stringify(payload)});await hideTyreSuccess('Client updated!');cancelEditClient();showToast('Client updated.');}else{await http(api.clients,{method:'POST',body:JSON.stringify(payload)});await hideTyreSuccess('Client saved!');e.target.reset();toggleGSTBlock('client',true);if(q('client-state'))q('client-state').value='';if(q('client-entity-type-auto'))q('client-entity-type-auto').value='';setMsg('client-msg','Client saved.','success');showToast('Client saved.');}await reloadAll();}catch(err){hideTyreError();setMsg('client-msg',err.message||'Save failed','error');showToast(err.message||'Save failed','error');}});
q('form-use')?.addEventListener('submit',async(e)=>{e.preventDefault();setMsg('use-msg','');const fd=new FormData(e.target);const payload={useName:fd.get('useName'),description:toStringOrNull(fd.get('description'))};showTyreSaving('Saving use...');try{await http(api.uses,{method:'POST',body:JSON.stringify(payload)});await hideTyreSuccess('Use saved!');e.target.reset();setMsg('use-msg','Saved.','success');showToast('Use saved.');await reloadAll();}catch(err){hideTyreError();setMsg('use-msg',err.message||'Save failed','error');showToast(err.message||'Save failed','error');}});

q('form-tyreInward')?.addEventListener('submit',async(e)=>{e.preventDefault();setMsg('tyreInward-msg','');const fd=new FormData(e.target);const supplierId=toNumberOrNull(q('ti-supplier-hidden')?.value),useId=toNumberOrNull(q('ti-use-hidden')?.value);const payload={materialType:toStringOrNull(fd.get('materialType')),supplierId,useId,vehicleNumber:toStringOrNull(fd.get('vehicleNumber')),driverName:toStringOrNull(fd.get('driverName')),driverNumber:toStringOrNull(fd.get('driverNumber')),date:toStringOrNull(fd.get('date')),grossWeight:toDecimalOrNull(fd.get('grossWeight')),tareWeight:toDecimalOrNull(fd.get('tareWeight')),quantity:toNumberOrNull(fd.get('quantity')),type:toStringOrNull(fd.get('type')),rate:toDecimalOrNull(fd.get('rate')),notes:toStringOrNull(fd.get('notes'))};const isEdit=editingTyreInwardId!=null;showTyreSaving(isEdit?'Updating inward entry...':'Recording inward entry...');try{let saved;if(isEdit)saved=await http(`${api.tyreInwards}/${editingTyreInwardId}`,{method:'PUT',body:JSON.stringify(payload)});else saved=await http(api.tyreInwards,{method:'POST',body:JSON.stringify(payload)});if(tiWidget&&saved?.id)await tiWidget.uploadPending(saved.id);await hideTyreSuccess(isEdit?'Inward updated!':'Inward saved!');e.target.reset();if(q('ti-total'))q('ti-total').value='';if(q('ti-date'))q('ti-date').value=new Date().toISOString().split('T')[0];if(tiWidget)tiWidget.reset();if(acSupplier)acSupplier.setValue(null);if(acUseInward)acUseInward.setValue(null);editingTyreInwardId=null;const cancelBtn=q('ti-cancel-btn');if(cancelBtn)cancelBtn.style.display='none';const submitBtn=e.target.querySelector('button[type="submit"]');if(submitBtn)submitBtn.innerHTML='<i class="bi bi-check2-circle"></i> Save Entry';setMsg('tyreInward-msg',isEdit?'Updated.':'Saved.','success');showToast(isEdit?'Inward entry updated.':'Inward entry saved.');await reloadAll();}catch(err){hideTyreError();setMsg('tyreInward-msg',err.message||'Save failed','error');showToast(err.message||'Save failed','error');}});

q('form-materialOutward')?.addEventListener('submit',async(e)=>{e.preventDefault();setMsg('materialOutward-msg','');const fd=new FormData(e.target);const clientId=toNumberOrNull(q('mo-client-hidden')?.value),useId=toNumberOrNull(q('mo-use-hidden')?.value);const payload={materialType:fd.get('materialType'),clientId,useId,vehicleNumber:toStringOrNull(fd.get('vehicleNumber')),driverName:toStringOrNull(fd.get('driverName')),driverNumber:toStringOrNull(fd.get('driverNumber')),dateOfSale:toStringOrNull(fd.get('dateOfSale')),grossWeight:toDecimalOrNull(fd.get('grossWeight')),tareWeight:toDecimalOrNull(fd.get('tareWeight')),rate:toDecimalOrNull(fd.get('rate')),quantity:toNumberOrNull(fd.get('quantity')),type:toStringOrNull(fd.get('type')),notes:toStringOrNull(fd.get('notes'))};const isEdit=editingOutwardId!=null;showTyreSaving(isEdit?'Updating outward entry...':'Recording outward entry...');try{let saved;if(isEdit)saved=await http(`${api.materialOutwards}/${editingOutwardId}`,{method:'PUT',body:JSON.stringify(payload)});else saved=await http(api.materialOutwards,{method:'POST',body:JSON.stringify(payload)});if(moWidget)await moWidget.uploadPending(saved.id);await hideTyreSuccess(isEdit?'Outward updated!':'Outward saved!');e.target.reset();if(q('mo-total'))q('mo-total').value='';if(q('mo-date'))q('mo-date').value=new Date().toISOString().split('T')[0];if(moWidget)moWidget.reset();if(acClient)acClient.setValue(null);if(acUseOutward)acUseOutward.setValue(null);editingOutwardId=null;const cancelBtn=q('mo-cancel-btn');if(cancelBtn)cancelBtn.style.display='none';const submitBtn=e.target.querySelector('button[type="submit"]');if(submitBtn)submitBtn.innerHTML='<i class="bi bi-check2-circle"></i> Save Entry';setMsg('materialOutward-msg',isEdit?'Updated.':'Saved.','success');showToast(isEdit?'Outward entry updated.':'Outward entry saved.');await reloadAll();}catch(err){hideTyreError();setMsg('materialOutward-msg',err.message||'Save failed','error');showToast(err.message||'Save failed','error');}});

q('supplier-cancel-btn')?.addEventListener('click',cancelEditSupplier);
q('client-cancel-btn')?.addEventListener('click',cancelEditClient);
q('ti-cancel-btn')?.addEventListener('click',()=>{editingTyreInwardId=null;q('form-tyreInward')?.reset();if(q('ti-total'))q('ti-total').value='';if(tiWidget)tiWidget.reset();if(acSupplier)acSupplier.setValue(null);if(acUseInward)acUseInward.setValue(null);const sub=q('form-tyreInward')?.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Save Entry';q('ti-cancel-btn').style.display='none';setMsg('tyreInward-msg','');});
q('mo-cancel-btn')?.addEventListener('click',()=>{editingOutwardId=null;q('form-materialOutward')?.reset();if(q('mo-total'))q('mo-total').value='';if(moWidget)moWidget.reset();if(acClient)acClient.setValue(null);if(acUseOutward)acUseOutward.setValue(null);const sub=q('form-materialOutward')?.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Save Entry';q('mo-cancel-btn').style.display='none';setMsg('materialOutward-msg','');});

async function fetchLatestInvoice(entryType,entryId){try{const token=getToken();const res=await fetch(`${api.invoices}/latest?entryType=${entryType}&entryId=${entryId}`,{headers:token?{'Authorization':'Bearer '+token}:{}});if(!res.ok)return null;return await res.json();}catch(_){return null;}}

// ── Render: Inwards ──
async function renderInwards(list){
  const tbody=q('table-tyreInwards'),badge=q('inward-count-badge');
  if(!tbody)return;tbody.innerHTML='';
  if(!list.length){setTableEmpty('table-tyreInwards',13);if(badge)badge.textContent='0 results';return;}
  const total=_allTyreInwards.length;
  if(badge)badge.textContent=list.length<total?`${list.length} of ${total} entries`:`${total} entr${total===1?'y':'ies'}`;
  const invData=await Promise.all(list.map(t=>fetchLatestInvoice('INWARD',t.id)));
  list.forEach((t,i)=>{
    const inv=invData[i];
    const matLabel=inwardTypeLabel[t.materialType]||t.materialType||'—';
    const chipClass=inwardChipClass[t.materialType]||'chip-inward-others';
    const tr=document.createElement('tr');
    tr.innerHTML=`<td>${escapeHtml(t.id)}</td><td>${t.materialType?`<span class="chip ${chipClass}">${escapeHtml(matLabel)}</span>`:'—'}</td><td>${escapeHtml(t.supplier?.supplierName)}</td><td>${escapeHtml(t.vehicleNumber)}</td><td>${escapeHtml(t.driverName)}${t.driverNumber?`<br><small style="color:var(--ink-muted)">${escapeHtml(t.driverNumber)}</small>`:''}</td><td>${escapeHtml(t.date)}</td><td>${escapeHtml(t.type)}</td><td>${escapeHtml(t.quantity)}</td><td><strong>${escapeHtml(t.netWeight)}</strong></td><td>${t.rate!=null?'₹'+escapeHtml(t.rate):'—'}</td><td><strong>${t.totalAmount!=null?'₹'+escapeHtml(t.totalAmount):'—'}</strong></td><td class="cell-inv"></td><td class="th-actions cell-actions"></td>`;
    const invCell=tr.querySelector('td.cell-inv');const invBtn=invoiceViewButton(inv);if(invBtn)invCell.appendChild(invBtn);else invCell.textContent='—';
    tr.querySelector('td.cell-actions').appendChild(rowEditButton('Edit',async()=>{
      editingTyreInwardId=t.id;const form=q('form-tyreInward');if(!form)return;
      form.querySelector('[name=materialType]').value=t.materialType||'';
      if(acSupplier&&t.supplier?.id)acSupplier.setValue(t.supplier.id);if(acUseInward&&t.use?.id)acUseInward.setValue(t.use.id);
      form.querySelector('[name=vehicleNumber]').value=t.vehicleNumber||'';form.querySelector('[name=driverName]').value=t.driverName||'';form.querySelector('[name=driverNumber]').value=t.driverNumber||'';
      form.querySelector('[name=date]').value=toInputDate(t.date);form.querySelector('[name=grossWeight]').value=t.grossWeight||'';form.querySelector('[name=tareWeight]').value=t.tareWeight||'';
      if(q('ti-net'))q('ti-net').value=t.netWeight||'';form.querySelector('[name=quantity]').value=t.quantity??'';form.querySelector('[name=type]').value=t.type||'';form.querySelector('[name=rate]').value=t.rate||'';
      if(q('ti-total'))q('ti-total').value=t.totalAmount||'';form.querySelector('[name=notes]').value=t.notes||'';
      const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Update Entry';const can=q('ti-cancel-btn');if(can)can.style.display='inline-flex';
      if(tiWidget)await tiWidget.loadForEntry(t.id);showTab('tyreInwards');form.scrollIntoView({behavior:'smooth'});
    }));
    tr.querySelector('td.cell-actions').appendChild(rowDeleteButton(async()=>{if(!confirm('Delete this inward entry?'))return;try{await http(`${api.tyreInwards}/${t.id}`,{method:'DELETE'});showToast('Inward entry deleted.');await reloadAll();}catch(err){showToast(err.message||'Delete failed','error');}}));
    tbody.appendChild(tr);
  });
}

// ── Render: Outwards ──
async function renderOutwards(list){
  const tbody=q('table-materialOutwards'),badge=q('outward-count-badge');
  if(!tbody)return;tbody.innerHTML='';
  if(!list.length){setTableEmpty('table-materialOutwards',14);if(badge)badge.textContent='0 results';return;}
  const total=_allMaterialOutwards.length;
  if(badge)badge.textContent=list.length<total?`${list.length} of ${total} entries`:`${total} entr${total===1?'y':'ies'}`;
  const invData=await Promise.all(list.map(m=>fetchLatestInvoice('OUTWARD',m.id)));
  list.forEach((m,i)=>{
    const inv=invData[i];
    const typeLabel={TYRE_OIL:'Tyre Oil',CARBON_POWER:'Carbon Powder',STEEL:'Steel'}[m.materialType]||m.materialType;
    const tr=document.createElement('tr');
    tr.innerHTML=`<td>${escapeHtml(m.id)}</td><td><span class="chip chip-blue">${escapeHtml(typeLabel)}</span></td><td>${escapeHtml(m.client?.clientName)}</td><td>${escapeHtml(m.use?.useName)}</td><td>${escapeHtml(m.vehicleNumber)}</td><td>${escapeHtml(m.driverName)}${m.driverNumber?`<br><small style="color:var(--ink-muted)">${escapeHtml(m.driverNumber)}</small>`:''}</td><td>${escapeHtml(m.dateOfSale)}</td><td>${escapeHtml(m.type)}</td><td>${escapeHtml(m.quantity)}</td><td>${escapeHtml(m.netWeight)}</td><td>₹${escapeHtml(m.rate)}</td><td><strong>₹${escapeHtml(m.totalAmount)}</strong></td><td class="cell-inv"></td><td class="th-actions cell-actions"></td>`;
    const invCell=tr.querySelector('td.cell-inv');const invBtn=invoiceViewButton(inv);if(invBtn)invCell.appendChild(invBtn);else invCell.textContent='—';
    tr.querySelector('td.cell-actions').appendChild(rowEditButton('Edit',async()=>{
      editingOutwardId=m.id;const form=q('form-materialOutward');if(!form)return;
      form.querySelector('[name=materialType]').value=m.materialType||'';
      if(acClient&&m.client?.id)acClient.setValue(m.client.id);if(acUseOutward&&m.use?.id)acUseOutward.setValue(m.use.id);
      form.querySelector('[name=vehicleNumber]').value=m.vehicleNumber||'';form.querySelector('[name=driverName]').value=m.driverName||'';form.querySelector('[name=driverNumber]').value=m.driverNumber||'';
      form.querySelector('[name=dateOfSale]').value=toInputDate(m.dateOfSale);form.querySelector('[name=grossWeight]').value=m.grossWeight||'';form.querySelector('[name=tareWeight]').value=m.tareWeight||'';
      if(q('mo-net'))q('mo-net').value=m.netWeight||'';
      const qtyEl=form.querySelector('[name=quantity]');if(qtyEl)qtyEl.value=m.quantity??'';
      const typeEl=form.querySelector('[name=type]');if(typeEl)typeEl.value=m.type||'';
      form.querySelector('[name=rate]').value=m.rate||'';if(q('mo-total'))q('mo-total').value=m.totalAmount||'';form.querySelector('[name=notes]').value=m.notes||'';
      const sub=form.querySelector('button[type="submit"]');if(sub)sub.innerHTML='<i class="bi bi-check2-circle"></i> Update Entry';const can=q('mo-cancel-btn');if(can)can.style.display='inline-flex';
      if(moWidget)await moWidget.loadForEntry(m.id);showTab('materialOutwards');form.scrollIntoView({behavior:'smooth'});
    }));
    tr.querySelector('td.cell-actions').appendChild(rowDeleteButton(async()=>{if(!confirm('Delete this outward entry?'))return;try{await http(`${api.materialOutwards}/${m.id}`,{method:'DELETE'});showToast('Outward entry deleted.');await reloadAll();}catch(err){showToast(err.message||'Delete failed','error');}}));
    tbody.appendChild(tr);
  });
}

function filterInwards(query){
  const q2=query.toLowerCase().trim();
  if(!q2){renderInwards(_allTyreInwards.slice(0,5));return;}
  renderInwards(_allTyreInwards.filter(t=>String(t.id).includes(q2)||(inwardTypeLabel[t.materialType]||t.materialType||'').toLowerCase().includes(q2)||(t.supplier?.supplierName||'').toLowerCase().includes(q2)||(t.supplier?.id?String(t.supplier.id).includes(q2):false)||(t.vehicleNumber||'').toLowerCase().includes(q2)||(t.driverName||'').toLowerCase().includes(q2)||(t.driverNumber||'').toLowerCase().includes(q2)||(t.type||'').toLowerCase().includes(q2)||(t.date||'').includes(q2)||(t.notes||'').toLowerCase().includes(q2)));
}
function filterOutwards(query){
  const q2=query.toLowerCase().trim();
  if(!q2){renderOutwards(_allMaterialOutwards.slice(0,5));return;}
  const typeMap={TYRE_OIL:'tyre oil',CARBON_POWER:'carbon powder',STEEL:'steel'};
  renderOutwards(_allMaterialOutwards.filter(m=>String(m.id).includes(q2)||(typeMap[m.materialType]||m.materialType||'').toLowerCase().includes(q2)||(m.client?.clientName||'').toLowerCase().includes(q2)||(m.client?.id?String(m.client.id).includes(q2):false)||(m.use?.useName||'').toLowerCase().includes(q2)||(m.vehicleNumber||'').toLowerCase().includes(q2)||(m.driverName||'').toLowerCase().includes(q2)||(m.driverNumber||'').toLowerCase().includes(q2)||(m.dateOfSale||'').includes(q2)||(m.type||'').toLowerCase().includes(q2)||(m.notes||'').toLowerCase().includes(q2)));
}

showTab('dashboard');
reloadAll();