// Register Offline Service Worker
if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('./sw.js').then(() => {
      updateOnlineBadge();
    });
  });
}

function updateOnlineBadge() {
  const el = document.getElementById('offlineStatus');
  if (!el) return;
  el.textContent = navigator.onLine ? 'OFFLINE READY (SW Cached)' : 'RUNNING 100% OFFLINE';
}
window.addEventListener('online', updateOnlineBadge);
window.addEventListener('offline', updateOnlineBadge);

let deferredPrompt = null;
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  const btn = document.getElementById('installPwaBtn');
  if (btn) btn.classList.remove('hidden');
});

function installOfflineApp() {
  if (deferredPrompt) deferredPrompt.prompt();
}

const BASE_KEYWORDS = new Set([
  "abstract","boolean","break","byte","case","catch","char","class","const","continue",
  "default","do","double","else","extends","final","finally","float","for","goto","if",
  "implements","import","instanceof","int","interface","long","native","new","package",
  "private","protected","public","return","short","static","super","switch","synchronized",
  "this","throw","throws","transient","try","void","volatile","while","true","false","null"
]);

const VERSIONED_KW = {
  "strictfp": 2, "assert": 4, "enum": 5, "var": 10,
  "yield": 14, "record": 16, "sealed": 17, "permits": 17
};

const DEPRECATED_TOKENS = {
  "Applet": 9, "JApplet": 9, "SecurityManager": 17,
  "stop": 2, "suspend": 2, "resume": 2, "finalize": 9
};

const PROFILES = {
  JDK_1_0: { name: "JDK 1.0 (Oak)", bytecode: 45, kw: 47, gc: 18, lang: "Classes, interfaces, packages, exceptions, synchronized monitors.", api: "java.lang, java.util (Vector, Hashtable), java.io, java.awt, java.applet.", depr: "None (baseline release).", jvm: "Classic Interpreter VM with mark-sweep stop-the-world GC." },
  JDK_1_2: { name: "Java 1.2 (Java 2)", bytecode: 46, kw: 48, gc: 32, lang: "Added 'strictfp' keyword for IEEE 754 floating-point reproducibility.", api: "Collections Framework (List, ArrayList, HashMap, Set), Swing (javax.swing), Java 2D.", depr: "Thread.stop(), Thread.suspend(), Thread.resume() deprecated for deadlock risk.", jvm: "Generational GC, ExactVM, fine-grained Policy security architecture." },
  JDK_1_4: { name: "Java 1.4 (Merlin)", bytecode: 48, kw: 49, gc: 49, lang: "Added 'assert' keyword for runtime assertions.", api: "New I/O (java.nio), Regular Expressions (java.util.regex), Logging API, XML JAXP.", depr: "Deprecated URL.encode/decode without charset.", jvm: "HotSpot Client/Server VMs, Concurrent Mark Sweep (CMS) & Parallel GC." },
  JDK_5: { name: "Java 5 (Tiger)", bytecode: 49, kw: 50, gc: 56, lang: "Generics <T>, Enhanced for-each, Enums, Annotations (@Override), Autoboxing, Varargs.", api: "java.util.concurrent, Scanner, StringBuilder, Formatter.", depr: "Unchecked warnings on raw collection types.", jvm: "JSR 133 Memory Model overhaul, Class Data Sharing (CDS)." },
  JDK_8: { name: "Java 8 LTS", bytecode: 52, kw: 50, gc: 72, lang: "Lambda expressions (->), Method references (::), Functional interfaces, Default interface methods.", api: "Stream API (java.util.stream), Optional<T>, Modern Date-Time (java.time), CompletableFuture.", depr: "Removed PermGen and JDBC-ODBC Bridge.", jvm: "Native Metaspace replaces PermGen; invokedynamic lambda call sites." },
  JDK_11: { name: "Java 11 LTS", bytecode: 55, kw: 52, gc: 82, lang: "Modules (JDK 9), 'var' local-variable type inference (JDK 10) & in lambda parameters (JDK 11).", api: "Standard HttpClient (HTTP/2 & WebSocket), String.isBlank/lines/repeat, Files.readString.", depr: "Removed Java EE & CORBA modules; Applet API deprecated.", jvm: "G1 default GC, experimental ZGC, Java Flight Recorder open-sourced." },
  JDK_17: { name: "Java 17 LTS", bytecode: 61, kw: 57, gc: 92, lang: "Switch Expressions (14), Text Blocks \"\"\" (15), Records & Pattern instanceof (16), Sealed Classes 'permits' (17).", api: "HexFormat, RandomGenerator, Deserialization Filters, Stream.toList().", depr: "Applet & SecurityManager deprecated for removal; CMS GC & Nashorn removed; strictfp obsolete.", jvm: "Production sub-millisecond ZGC & Shenandoah GC; Helpful NullPointerExceptions." },
  JDK_21: { name: "Java 21 LTS", bytecode: 65, kw: 58, gc: 97, lang: "Pattern Matching for switch, Record Patterns, Exhaustive switch over sealed hierarchies.", api: "Virtual Threads (Thread.ofVirtual), Sequenced Collections (getFirst/getLast/reversed).", depr: "Windows 32-bit x86 port deprecated for removal.", jvm: "Generational ZGC separating young/old regions for sub-ms pauses." },
  JDK_25: { name: "Java 25 LTS", bytecode: 69, kw: 60, gc: 100, lang: "Unnamed Variables '_', Markdown Javadoc '///', Compact Source Files, Module Import Declarations.", api: "Foreign Function & Memory (FFM) API, Stream Gatherers, Class-File API, Scoped Values.", depr: "SecurityManager permanently disabled; 32-bit x86 ports removed.", jvm: "Generational ZGC default, Compact Object Headers (Project Lilliput), AOT cache." }
};

const PRACTICE_LABS = [
  { id: 1, title: "1. Java 2 Collections (ArrayList & HashMap)", jdk: 2, code: `import java.util.*;\n\npublic class CollectionsLab {\n    public static void main(String args[]) {\n        List items = new ArrayList();\n        items.add("Java 1.2");\n        items.add("Java 17");\n        System.out.println("Collection count: " + items.size());\n    }\n}` },
  { id: 2, title: "2. Java 5 Generics & Enhanced For-Loop", jdk: 5, code: `import java.util.*;\n\npublic class GenericsLab {\n    public static void main(String[] args) {\n        List<String> names = Arrays.asList("Oak", "Playground", "Tiger");\n        System.out.println("Generics Lab loaded: " + names.size() + " codenames");\n    }\n}` },
  { id: 3, title: "3. Java 8 Streams & Lambda Expressions", jdk: 8, code: `import java.util.*;\n\npublic class StreamsLab {\n    public static void main(String[] args) {\n        Runnable r = () -> System.out.println("Lambda pipeline active on JDK 8+");\n        r.run();\n    }\n}` },
  { id: 4, title: "4. Java 17 Records & Sealed Classes", jdk: 17, code: `record Metric(String name, int score) {}\n\npublic class RecordsLab {\n    public static void main(String[] args) {\n        var m = new Metric("ZGC Efficiency", 92);\n        System.out.println(m.name() + " = " + m.score());\n    }\n}` },
  { id: 5, title: "5. Java 21 Virtual Threads & Pattern Switch", jdk: 21, code: `public class VirtualThreadLab {\n    public static void main(String[] args) {\n        System.out.println("Virtual Thread carrier ready on JDK 21 LTS (Bytecode 65.0)");\n    }\n}` }
];

function escapeHtml(s) {
  return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function updateHighlightingAndSave() {
  const code = document.getElementById('codeInput').value;
  const jdk = parseInt(document.getElementById('jdkSelect').value, 10);
  localStorage.setItem('ujs_web_code', code);
  localStorage.setItem('ujs_web_jdk', String(jdk));

  const sel = document.getElementById('jdkSelect');
  document.getElementById('activeJdkLabel').textContent = sel.options[sel.selectedIndex].text;

  let modernCount = 0, deprCount = 0, unsupportedCount = 0;
  const highlighted = escapeHtml(code).replace(
    /(".*?"|@\w+|\b[A-Za-z_]\w*\b)/g,
    (token) => {
      if (token.startsWith('"')) return `<span class="kw-str">${token}</span>`;
      if (token.startsWith('@')) {
        if (jdk < 5) {
          unsupportedCount++;
          return `<span class="kw-err" title="Annotations require JDK 5+">${token}</span>`;
        }
        modernCount++;
        return `<span class="kw-ann">${token}</span>`;
      }
      if (VERSIONED_KW[token] !== undefined) {
        const req = VERSIONED_KW[token];
        if (jdk < req) {
          unsupportedCount++;
          return `<span class="kw-err" title="Requires JDK ${req}+">${token}</span>`;
        }
        if (token === 'strictfp' && jdk >= 17) {
          deprCount++;
          return `<span class="kw-depr" title="Obsolete in JDK 17+">${token}</span>`;
        }
        modernCount++;
        return `<span class="kw-ver">${token}</span>`;
      }
      if (DEPRECATED_TOKENS[token] !== undefined && jdk >= DEPRECATED_TOKENS[token]) {
        deprCount++;
        return `<span class="kw-depr">${token}</span>`;
      }
      if (BASE_KEYWORDS.has(token)) {
        return `<span class="kw-base">${token}</span>`;
      }
      return token;
    }
  );
  document.getElementById('highlightPreview').innerHTML = highlighted;
  document.getElementById('lexerTelemetry').innerHTML =
    `<span><b>Active Lexer:</b> JDK ${jdk === 2 ? '1.2 (Java 2)' : jdk}</span>` +
    `<span style="color:var(--amber)"><b>Version-Specific Tokens:</b> ${modernCount}</span>` +
    `<span style="color:var(--amber)"><b>Deprecated Tokens:</b> ${deprCount}</span>` +
    `<span style="color:${unsupportedCount > 0 ? 'var(--red)' : 'var(--green)'}"><b>Unsupported for Selected JDK:</b> ${unsupportedCount}</span>`;
}

function onJdkChanged() {
  updateHighlightingAndSave();
  compileAndRun();
}

function loadTemplate(kind) {
  const box = document.getElementById('codeInput');
  if (kind === 'java2') {
    document.getElementById('jdkSelect').value = "2";
    box.value = `import java.util.*;\n\npublic strictfp class Java2Demo {\n    public static void main(String args[]) {\n        List list = new ArrayList();\n        list.add("J2SE 1.2 Collections");\n        System.out.println("Java 2 Output: " + list.size() + " item");\n    }\n}`;
  } else if (kind === 'java8') {
    document.getElementById('jdkSelect').value = "8";
    box.value = `import java.util.*;\n\npublic class Java8Streams {\n    public static void main(String[] args) {\n        System.out.println("Java 8 Lambda & Stream API active [Bytecode 52.0]");\n    }\n}`;
  } else if (kind === 'modern') {
    document.getElementById('jdkSelect').value = "17";
    box.value = `record Developer(String name, int jdkVersion) {}\n\npublic class Main {\n    public static void main(String[] args) {\n        var dev = new Developer("Universal Java Studio", 17);\n        System.out.println("Running offline on JDK " + dev.jdkVersion() + " by " + dev.name());\n    }\n}`;
  } else {
    box.value = `import java.applet.Applet;\nrecord Point(int x, int y) {}\n\npublic strictfp class Showcase extends Applet {\n    @Override\n    public void init() {\n        var p = new Point(10, 20);\n        Thread.currentThread().stop();\n        System.out.println("Switch the Active JDK dropdown between JDK 1.2 and JDK 21!");\n    }\n}`;
  }
  updateHighlightingAndSave();
  compileAndRun();
}

function compileAndRun() {
  const code = document.getElementById('codeInput').value;
  const jdk = parseInt(document.getElementById('jdkSelect').value, 10);
  const bytecodeMajor = 44 + (jdk <= 1 ? 1 : jdk);
  const errors = [];

  for (const [kw, req] of Object.entries(VERSIONED_KW)) {
    const reg = new RegExp(`\\b${kw}\\b`);
    if (reg.test(code) && jdk < req) {
      errors.push(`Error: '${kw}' is not supported in -source ${jdk === 2 ? '1.2' : jdk} (requires JDK ${req}+)`);
    }
  }
  if (/@\w+/.test(code) && jdk < 5) {
    errors.push(`Error: annotations are not supported in -source 1.${jdk} (requires JDK 5+)`);
  }
  if (/<[A-Z]\w*>/.test(code) && jdk < 5) {
    errors.push(`Error: generics are not supported in -source 1.${jdk} (requires JDK 5+)`);
  }

  const consoleEl = document.getElementById('consoleOutput');
  if (errors.length > 0) {
    consoleEl.innerHTML = `<span style="color:var(--red);font-weight:700;">COMPILATION FAILED (javac target JDK ${jdk === 2 ? '1.2' : jdk})</span>\n\n` + errors.join("\n");
    return;
  }

  const prints = [];
  const regex = /System\.out\.println\((.*?)\);/g;
  let m;
  while ((m = regex.exec(code)) !== null) {
    prints.push(m[1].replace(/["+]/g, ' ').replace(/\s+/g, ' ').trim());
  }
  const out = prints.length ? prints.join("\n") : "Program finished with exit code 0.";
  consoleEl.innerHTML =
    `<span style="color:var(--green);font-weight:700;">BUILD &amp; EXECUTION SUCCEEDED [Offline Web Engine]</span>\n` +
    `Compiler: javac ${jdk >= 9 ? '--release ' + jdk : '-source 1.' + jdk + ' -target 1.' + jdk}\n` +
    `Class File Header: 0xCAFEBABE | Bytecode Major.Minor: ${bytecodeMajor}.0\n` +
    `------------------------------------------------------------\n` +
    escapeHtml(out);
}

function switchTab(tab) {
  ['dashboard','editor','explorer','practice','tutorials','projects'].forEach(t => {
    const sec = document.getElementById('sec-' + t);
    const btn = document.getElementById('tab-' + t);
    if (sec) sec.classList.toggle('hidden', t !== tab);
    if (btn) btn.classList.toggle('active', t === tab);
  });
}

function setPair(l, r) {
  document.getElementById('verLeft').value = l;
  document.getElementById('verRight').value = r;
  renderComparison();
}

function renderComparison() {
  const l = PROFILES[document.getElementById('verLeft').value];
  const r = PROFILES[document.getElementById('verRight').value];
  document.getElementById('graphicalComparison').innerHTML = `
    <div class="bar-row">
      <div><b>Bytecode Major Version (CAFEBABE):</b> <span style="color:var(--amber)">${l.name} (v${l.bytecode}.0)</span> vs <span style="color:var(--green)">${r.name} (v${r.bytecode}.0)</span></div>
      <div class="bar-track"><div class="bar-fill-left" style="width:${(l.bytecode/70)*100}%"></div></div>
      <div class="bar-track"><div class="bar-fill-right" style="width:${(r.bytecode/70)*100}%"></div></div>
    </div>
    <div class="bar-row">
      <div><b>Language Keyword &amp; Contextual Grammar Count:</b> <span style="color:var(--amber)">${l.kw} keywords</span> vs <span style="color:var(--green)">${r.kw} keywords</span></div>
      <div class="bar-track"><div class="bar-fill-left" style="width:${(l.kw/65)*100}%"></div></div>
      <div class="bar-track"><div class="bar-fill-right" style="width:${(r.kw/65)*100}%"></div></div>
    </div>
    <div class="bar-row">
      <div><b>JVM &amp; Garbage Collection Low-Pause Efficiency Index:</b> <span style="color:var(--amber)">${l.gc}/100</span> vs <span style="color:var(--green)">${r.gc}/100</span></div>
      <div class="bar-track"><div class="bar-fill-left" style="width:${l.gc}%"></div></div>
      <div class="bar-track"><div class="bar-fill-right" style="width:${r.gc}%"></div></div>
    </div>
  `;
  document.getElementById('tabularComparison').innerHTML = `
    <thead>
      <tr><th>Comparison Dimension</th><th style="color:var(--amber)">Baseline: ${l.name}</th><th style="color:var(--green)">Target: ${r.name}</th></tr>
    </thead>
    <tbody>
      <tr><td><b>Bytecode Class Version</b></td><td>v${l.bytecode}.0 (0xCAFEBABE)</td><td>v${r.bytecode}.0 (0xCAFEBABE)</td></tr>
      <tr><td><b>Major Language Changes</b></td><td>${l.lang}</td><td>${r.lang}</td></tr>
      <tr><td><b>New API Additions</b></td><td>${l.api}</td><td>${r.api}</td></tr>
      <tr><td><b>Deprecated / Removed APIs</b></td><td>${l.depr}</td><td>${r.depr}</td></tr>
      <tr><td><b>JVM &amp; GC Advancements</b></td><td>${l.jvm}</td><td>${r.jvm}</td></tr>
    </tbody>
  `;
}

function renderPracticeLabs() {
  const chips = document.getElementById('practiceChips');
  if (!chips) return;
  chips.innerHTML = PRACTICE_LABS.map((lab, idx) =>
    `<span class="chip" onclick="openPracticeLab(${idx})">${lab.title}</span>`
  ).join('');
  openPracticeLab(0);
}

function openPracticeLab(idx) {
  const lab = PRACTICE_LABS[idx];
  const el = document.getElementById('practiceDetails');
  if (!el) return;
  el.innerHTML = `
    <div class="card" style="background:var(--surface2);">
      <h3>${lab.title} (Target: JDK ${lab.jdk === 2 ? '1.2' : lab.jdk})</h3>
      <pre style="background:#090D12;padding:10px;border-radius:8px;margin:10px 0;font-family:var(--mono);font-size:12.5px;">${escapeHtml(lab.code)}</pre>
      <button class="primary" onclick="loadLabIntoEditor(${idx})">Open &amp; Run in Code Editor</button>
    </div>
  `;
}

function loadLabIntoEditor(idx) {
  const lab = PRACTICE_LABS[idx];
  document.getElementById('jdkSelect').value = String(lab.jdk);
  document.getElementById('codeInput').value = lab.code;
  switchTab('editor');
  updateHighlightingAndSave();
  compileAndRun();
}

function getSavedProjects() {
  try {
    return JSON.parse(localStorage.getItem('ujs_web_projects') || '[]');
  } catch (e) {
    return [];
  }
}

function saveCurrentAsProject() {
  const name = (document.getElementById('newProjName').value || 'UntitledJavaProject').trim();
  const jdk = document.getElementById('newProjJdk').value;
  const code = document.getElementById('codeInput').value;
  const list = getSavedProjects();
  list.unshift({ name, jdk, code, updatedAt: new Date().toLocaleString() });
  localStorage.setItem('ujs_web_projects', JSON.stringify(list));
  renderProjects();
}

function openSavedProject(idx) {
  const list = getSavedProjects();
  const p = list[idx];
  if (!p) return;
  document.getElementById('jdkSelect').value = String(p.jdk);
  document.getElementById('codeInput').value = p.code;
  switchTab('editor');
  updateHighlightingAndSave();
  compileAndRun();
}

function renderProjects() {
  const container = document.getElementById('projectsList');
  if (!container) return;
  const list = getSavedProjects();
  if (list.length === 0) {
    container.innerHTML = `<p style="color:var(--muted);font-size:13px;">No saved custom projects yet. Click "Save Current Editor as Project" above.</p>`;
    return;
  }
  container.innerHTML = list.map((p, i) => `
    <div class="card" style="display:flex;justify-content:space-between;align-items:center;background:var(--surface2);">
      <div>
        <div style="font-weight:700;color:var(--cyan);">${escapeHtml(p.name)}</div>
        <div style="font-size:12px;color:var(--muted);">Target JDK: ${p.jdk === '2' ? 'JDK 1.2 (Java 2)' : 'JDK ' + p.jdk} • Saved: ${p.updatedAt}</div>
      </div>
      <button class="accent" onclick="openSavedProject(${i})">Open in Editor</button>
    </div>
  `).join('');
}

// Initialize on load
window.addEventListener('DOMContentLoaded', () => {
  const savedCode = localStorage.getItem('ujs_web_code');
  const savedJdk = localStorage.getItem('ujs_web_jdk');
  if (savedCode) document.getElementById('codeInput').value = savedCode;
  if (savedJdk) document.getElementById('jdkSelect').value = savedJdk;
  updateHighlightingAndSave();
  renderComparison();
  renderPracticeLabs();
  renderProjects();
  compileAndRun();
});
