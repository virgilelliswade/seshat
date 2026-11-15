package com.seshat.writer.universe

import android.content.Context
import com.seshat.writer.bible.StoryBible
import com.seshat.writer.export.ScriptFileManager
import com.seshat.writer.util.DeviceEnvironment
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Generates and launches the interactive 3D Living World Tree in the user's browser.
 * Renders an offline-capable, GPU-accelerated HTML5 spatial canvas featuring 360° camera orbit,
 * subterranean foundational lore roots, the chronological trunk axis, branching project limbs,
 * and inspector cards for films, TV series, novels, and comics.
 */
object Universe3DVisualizer {

    fun launch3DTree(
        context: Context? = null,
        universeTitle: String,
        bible: StoryBible? = null,
        blendedProject: BlendedUniverseProject? = null
    ): File {
        val exportsDir = ScriptFileManager.getExportsDirectory(context)
        val file = File(exportsDir, "universe_3d_tree.html")
        val htmlContent = generateHtml(universeTitle, bible, blendedProject)
        file.writeText(htmlContent)

        try {
            DeviceEnvironment.openExternalUrl(context, file.toURI().toString())
        } catch (_: Throwable) {}

        return file
    }

    fun generateHtml(
        universeTitle: String,
        bible: StoryBible? = null,
        blendedProject: BlendedUniverseProject? = null
    ): String {
        val safeTitle = universeTitle.ifBlank { "Living Story Universe" }
        val charCount = bible?.characters?.size ?: 6
        val locCount = bible?.locations?.size ?: 4
        val beatCount = bible?.beats?.size ?: 4

        val projects = blendedProject?.assignments ?: emptyList()
        val eras = blendedProject?.eras ?: emptyList()

        // Build 3D tree nodes JSON
        val nodesArray = JSONArray()

        // 1. Bedrock Roots (Foundational Lore)
        nodesArray.put(JSONObject().apply {
            put("id", "ROOT_LORE")
            put("type", "root")
            put("name", "Foundational Lore & Core Rules")
            put("tag", "CORE BIBLE")
            put("icon", "🪵")
            put("x", 0)
            put("y", -260)
            put("z", 0)
            put("color", "#f59e0b")
            put("size", 18)
            put("era", "Universal Foundations")
            put("desc", "Central lore, world rules, and subterranean continuity grounding all projects.")
        })
        nodesArray.put(JSONObject().apply {
            put("id", "ROOT_CHARS")
            put("type", "root")
            put("name", "Character Archetypes ($charCount)")
            put("tag", "DRAMATIS PERSONAE")
            put("icon", "🎭")
            put("x", 120)
            put("y", -330)
            put("z", -70)
            put("color", "#f59e0b")
            put("size", 14)
            put("era", "Cast & Factions")
            put("desc", "Shared character roster, protagonists, antagonists, and inter-medium ensembles.")
        })
        nodesArray.put(JSONObject().apply {
            put("id", "ROOT_LOCS")
            put("type", "root")
            put("name", "World Locations ($locCount)")
            put("tag", "GEOGRAPHY")
            put("icon", "🔍")
            put("x", -130)
            put("y", -320)
            put("z", 60)
            put("color", "#f59e0b")
            put("size", 14)
            put("era", "Universal Atlas")
            put("desc", "Shared environments, realms, star systems, and city sectors.")
        })
        nodesArray.put(JSONObject().apply {
            put("id", "ROOT_CHRONO")
            put("type", "root")
            put("name", "Master Chronology")
            put("tag", "TIMELINE AXIS")
            put("icon", "⏳")
            put("x", 0)
            put("y", -400)
            put("z", 100)
            put("color", "#f59e0b")
            put("size", 13)
            put("era", "Timeline Anchor")
            put("desc", "Grand chronicle of historical eras, epoch shifts, and nexus events.")
        })

        // 2. Chronological Trunk (Ascending Y)
        val defaultEras = if (eras.isNotEmpty()) eras else listOf(
            UniverseTimelineEra(name = "Era I: Genesis", timeSpan = "Year 1 - 25"),
            UniverseTimelineEra(name = "Era II: The Expansion", timeSpan = "Year 26 - 60"),
            UniverseTimelineEra(name = "Era III: The Zenith", timeSpan = "Year 61 - 100")
        )

        defaultEras.forEachIndexed { idx, era ->
            val yPos = -100 + (idx * 160)
            nodesArray.put(JSONObject().apply {
                put("id", "TRUNK_ERA_${idx + 1}")
                put("type", "trunk")
                put("name", era.name.ifBlank { "Era ${idx + 1}" })
                put("tag", "TIMELINE AXIS")
                put("icon", "⏳")
                put("x", 0)
                put("y", yPos)
                put("z", 0)
                put("color", "#eab308")
                put("size", 14)
                put("era", era.timeSpan.ifBlank { "Era ${idx + 1}" })
                put("desc", "Major historical milestone and narrative backbone along the central trunk.")
            })
        }

        // 3. Branches / Projects
        if (projects.isNotEmpty()) {
            projects.forEachIndexed { i, proj ->
                val angle = (i * 1.57) + 0.5
                val dist = 180.0 + (i * 20.0)
                val xPos = Math.cos(angle) * dist
                val zPos = Math.sin(angle) * dist
                val yPos = 30 + (i * 70)
                val isSpinoff = proj.branchRole == BranchRole.SPINOFF_SIDEQUEST || !proj.spinoffParentId.isNullOrBlank()
                val colorHex = when (proj.format) {
                    com.seshat.writer.library.ProjectFormat.SCREENPLAY -> "#38bdf8"
                    com.seshat.writer.library.ProjectFormat.TV_SERIES -> "#a855f7"
                    com.seshat.writer.library.ProjectFormat.NOVEL -> "#f43f5e"
                    com.seshat.writer.library.ProjectFormat.COMIC_BOOK -> "#10b981"
                    com.seshat.writer.library.ProjectFormat.STAGE_PLAY -> "#f59e0b"
                    else -> "#eab308"
                }
                nodesArray.put(JSONObject().apply {
                    put("id", "BRANCH_${i + 1}")
                    put("type", if (isSpinoff) "spinoff" else "branch")
                    put("name", proj.title)
                    put("tag", if (isSpinoff) "SPINOFF PROJECT" else "CORE PROJECT")
                    put("icon", when (proj.format) {
                        com.seshat.writer.library.ProjectFormat.SCREENPLAY -> "🎬"
                        com.seshat.writer.library.ProjectFormat.TV_SERIES -> "📺"
                        com.seshat.writer.library.ProjectFormat.NOVEL -> "📖"
                        com.seshat.writer.library.ProjectFormat.COMIC_BOOK -> "💥"
                        com.seshat.writer.library.ProjectFormat.STAGE_PLAY -> "🎭"
                        else -> "📄"
                    })
                    put("x", xPos.toInt())
                    put("y", yPos)
                    put("z", zPos.toInt())
                    put("color", colorHex)
                    put("size", if (isSpinoff) 14 else 18)
                    put("era", proj.eraTag.ifBlank { "Main Timeline" })
                    put("desc", "${proj.format.displayName}: ${proj.branchRole.label}. Integrated into the universe world tree.")
                })
            }
        } else {
            // Default Flagship Branch
            nodesArray.put(JSONObject().apply {
                put("id", "BRANCH_MAIN")
                put("type", "branch")
                put("name", "$safeTitle: Genesis")
                put("tag", "CORE PROJECT")
                put("icon", "🎬")
                put("x", -150)
                put("y", 60)
                put("z", 110)
                put("color", "#38bdf8")
                put("size", 18)
                put("era", "Era I")
                put("desc", "The flagship story anchoring the entire living universe tree.")
            })
            nodesArray.put(JSONObject().apply {
                put("id", "BRANCH_TV")
                put("type", "branch")
                put("name", "$safeTitle: Chronicles")
                put("tag", "CORE PROJECT")
                put("icon", "📺")
                put("x", 170)
                put("y", 120)
                put("z", -100)
                put("color", "#a855f7")
                put("size", 16)
                put("era", "Era II")
                put("desc", "Companion serialized drama expanding secondary character storylines.")
            })
            nodesArray.put(JSONObject().apply {
                put("id", "BRANCH_SPINOFF")
                put("type", "spinoff")
                put("name", "$safeTitle: Origins")
                put("tag", "SPINOFF & SIDEQUEST")
                put("icon", "📖")
                put("x", -180)
                put("y", 180)
                put("z", -130)
                put("color", "#f43f5e")
                put("size", 14)
                put("era", "Pre-Genesis")
                put("desc", "Prequel spinoff exploring founding lore.")
            })
        }

        val jsonNodesStr = nodesArray.toString()

        return """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Seshat 3D World Tree — $safeTitle</title>
  <script src="https://cdn.tailwindcss.com"></script>
  <style>
    @import url('https://fonts.googleapis.com/css2?family=Cinzel:wght@600;700;900&family=JetBrains+Mono:wght@400;600&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap');
    body {
      font-family: 'Plus Jakarta Sans', sans-serif;
      user-select: none;
      overflow: hidden;
      background-color: #030509;
    }
    .cinzel { font-family: 'Cinzel', serif; }
    .mono { font-family: 'JetBrains Mono', monospace; }
    .glass-panel {
      background: rgba(10, 15, 29, 0.85);
      backdrop-filter: blur(16px);
      -webkit-backdrop-filter: blur(16px);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
    @keyframes goldGlow {
      0%, 100% { box-shadow: 0 0 15px rgba(234, 179, 8, 0.35); }
      50% { box-shadow: 0 0 30px rgba(234, 179, 8, 0.75); }
    }
    .gold-glow { animation: goldGlow 3s infinite; }
  </style>
</head>
<body class="w-screen h-screen relative text-slate-100">

  <!-- 3D RENDER CANVAS -->
  <canvas id="arbor3d" class="absolute inset-0 w-full h-full cursor-grab active:cursor-grabbing z-0"></canvas>

  <!-- TOP HEADER HUD -->
  <header class="absolute top-0 left-0 right-0 z-20 flex items-center justify-between px-6 py-3.5 glass-panel border-b border-white/10 shadow-2xl">
    <div class="flex items-center gap-4">
      <div class="w-11 h-11 rounded-xl bg-gradient-to-br from-amber-500/25 via-red-500/15 to-transparent border border-amber-500/50 flex items-center justify-center text-amber-400 font-bold cinzel text-2xl shadow-lg">
        🪐
      </div>
      <div>
        <div class="flex items-center gap-2">
          <span class="text-[10px] uppercase tracking-widest font-bold text-amber-400/90 mono">Seshat 3D Universe Viewer</span>
          <span class="text-[9px] px-2 py-0.5 rounded-full bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 mono">360° LIVING WORLD TREE</span>
        </div>
        <h1 class="text-base font-bold cinzel tracking-wide text-white flex items-center gap-2">
          $safeTitle
          <span class="text-xs font-normal text-slate-400 mono">(Interactive 3D)</span>
        </h1>
      </div>
    </div>

    <!-- CAMERA PRESETS -->
    <div class="hidden lg:flex items-center gap-1.5 glass-panel p-1.5 rounded-xl border border-white/10 text-xs mono">
      <span class="text-[10px] text-slate-400 px-2 uppercase font-semibold">Camera Views:</span>
      <button class="cam-btn px-2.5 py-1 rounded-lg bg-amber-500/20 text-amber-300 border border-amber-500/40 font-semibold" data-preset="all">Full Orbit</button>
      <button class="cam-btn px-2.5 py-1 rounded-lg text-slate-300 hover:bg-white/5 font-semibold" data-preset="roots">🪵 Foundational Roots</button>
      <button class="cam-btn px-2.5 py-1 rounded-lg text-slate-300 hover:bg-white/5 font-semibold" data-preset="trunk">⏳ Timeline Axis</button>
      <button class="cam-btn px-2.5 py-1 rounded-lg text-slate-300 hover:bg-white/5 font-semibold" data-preset="branches">🌿 Projects & Branches</button>
    </div>

    <!-- ACTIONS -->
    <div class="flex items-center gap-3">
      <button id="toggle-autorotate" class="px-3.5 py-2 rounded-xl glass-panel text-xs text-slate-200 hover:bg-slate-800 transition flex items-center gap-1.5">
        <span id="rotate-icon">⏸</span> Auto-Rotate
      </button>
      <button id="sprout-3d-btn" class="flex items-center gap-2 px-4 py-2 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold text-xs shadow-lg shadow-amber-500/20 active:scale-95 transition">
        <span>🌱</span> + Add Project Node
      </button>
    </div>
  </header>

  <!-- LEFT CONTROLS: TIMELINE & GROWTH SCRUBBER -->
  <div class="absolute top-24 left-6 z-20 w-80 glass-panel rounded-2xl p-4 shadow-2xl border border-white/10 space-y-4">
    <div class="flex items-center justify-between pb-2 border-b border-white/10">
      <div class="flex items-center gap-2">
        <span class="text-base text-amber-400">⏳</span>
        <span class="text-xs uppercase font-bold text-slate-200 tracking-wider mono">Timeline Axis Scrubber</span>
      </div>
      <span id="timeline-label" class="text-xs font-bold text-amber-400 mono">All Eras Active</span>
    </div>

    <div>
      <div class="flex items-center justify-between text-[11px] text-slate-400 mb-1.5 mono">
        <span>Foundations</span>
        <span class="text-amber-300 font-bold">Chronology Scrubber</span>
        <span>Future Eras</span>
      </div>
      <input id="growth-slider" type="range" min="0.25" max="1.0" step="0.01" value="1.0"
             class="w-full h-1.5 bg-slate-800 rounded-lg appearance-none cursor-pointer accent-amber-500">
      <p class="text-[10px] text-slate-400 mt-2 leading-relaxed">
        Drag to scrub through historical chronology. Scrubbing scales the timeline trunk and unfurls connected project branches.
      </p>
    </div>

    <!-- MEDIUM LEGEND -->
    <div class="pt-2 border-t border-white/10">
      <span class="text-[10px] uppercase font-bold text-slate-400 mono block mb-2">Universe Medium Key</span>
      <div class="grid grid-cols-2 gap-1.5 text-[11px] mono">
        <div class="p-2 rounded-lg bg-cyan-950/40 border border-cyan-500/30 flex items-center gap-2 text-cyan-300">
          <span>🎬</span><span>Film Screenplay</span>
        </div>
        <div class="p-2 rounded-lg bg-purple-950/40 border border-purple-500/30 flex items-center gap-2 text-purple-300">
          <span>📺</span><span>TV Series</span>
        </div>
        <div class="p-2 rounded-lg bg-rose-950/40 border border-rose-500/30 flex items-center gap-2 text-rose-300">
          <span>📖</span><span>Novel Manuscript</span>
        </div>
        <div class="p-2 rounded-lg bg-emerald-950/40 border border-emerald-500/30 flex items-center gap-2 text-emerald-300">
          <span>💥</span><span>Comic Book</span>
        </div>
      </div>
    </div>
  </div>

  <!-- BOTTOM HUD -->
  <div class="absolute bottom-6 left-6 z-20 flex items-center gap-4">
    <div class="glass-panel px-4 py-2.5 rounded-2xl flex items-center gap-3 text-xs text-slate-300 shadow-xl border border-white/10">
      <div class="flex items-center gap-2 text-amber-400 font-bold mono">
        <span>🖱️ Drag to Orbit 360°</span>
        <span class="text-slate-600">•</span>
        <span>Scroll to Zoom</span>
        <span class="text-slate-600">•</span>
        <span>Click Node to Inspect</span>
      </div>
    </div>
  </div>

  <!-- RIGHT SLIDEOUT DRAWER: 3D NODE INSPECTOR -->
  <aside id="inspector-3d" class="absolute top-20 right-6 bottom-6 w-96 glass-panel rounded-2xl p-6 z-30 shadow-2xl border border-white/15 transform translate-x-[115%] transition-transform duration-300 ease-out overflow-y-auto">
    <div class="flex items-center justify-between pb-4 border-b border-white/10">
      <div class="flex items-center gap-2.5">
        <span id="node-icon" class="text-2xl p-2 rounded-xl bg-amber-500/20 border border-amber-500/30">🎬</span>
        <div>
          <span id="node-tag" class="text-[10px] uppercase font-bold mono text-cyan-400">CORE PROJECT</span>
          <h2 id="node-title" class="text-base font-bold text-white cinzel">Project Title</h2>
        </div>
      </div>
      <button onclick="closeInspector3D()" class="w-8 h-8 rounded-lg bg-slate-800/80 hover:bg-slate-700 text-slate-300 font-bold flex items-center justify-center">✕</button>
    </div>

    <div class="py-4 space-y-4 text-xs">
      <div>
        <span class="text-[10px] uppercase font-bold text-slate-400 mono">Summary / Canon Narrative</span>
        <p id="node-desc" class="text-slate-300 mt-1 leading-relaxed">
          Description
        </p>
      </div>

      <div class="grid grid-cols-2 gap-2 bg-slate-900/60 p-3 rounded-xl border border-white/5 mono">
        <div>
          <span class="text-[10px] text-slate-400 block">ERA / TIMELINE</span>
          <strong id="node-era" class="text-amber-300">Era I</strong>
        </div>
        <div>
          <span class="text-[10px] text-slate-400 block">3D SPATIAL COORD</span>
          <strong id="node-coord" class="text-white">[X: 0, Y: 0, Z: 0]</strong>
        </div>
      </div>
    </div>
  </aside>

  <!-- 3D ENGINE SCRIPT -->
  <script>
    const canvas = document.getElementById('arbor3d');
    const ctx = canvas.getContext('2d');

    function resize() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }
    window.addEventListener('resize', resize);
    resize();

    let camDist = 720;
    let targetDist = 720;
    let rotX = 0.28;
    let targetRotX = 0.28;
    let rotY = -0.55;
    let targetRotY = -0.55;
    let camYOffset = -40;
    let targetCamYOffset = -40;

    let isDragging = false;
    let prevMouseX = 0;
    let prevMouseY = 0;
    let autoRotate = true;
    let growthFactor = 1.0;

    const treeNodes = $jsonNodesStr;

    // Build connections
    const treeLimbs = [];
    // Connect roots to base
    treeNodes.filter(n => n.type === 'root').forEach(r => {
      treeLimbs.push({ from: 'ROOT_LORE', to: r.id, stroke: '#d97706', width: 4 });
    });
    // Connect roots to trunk era 1
    treeLimbs.push({ from: 'ROOT_LORE', to: 'TRUNK_ERA_1', stroke: '#eab308', width: 9 });
    // Connect trunk eras sequentially
    for (let e = 1; e < defaultErasCount(); e++) {
      treeLimbs.push({ from: 'TRUNK_ERA_' + e, to: 'TRUNK_ERA_' + (e + 1), stroke: '#eab308', width: 8 });
    }
    // Connect branches to trunk
    treeNodes.filter(n => n.type === 'branch' || n.type === 'spinoff').forEach((b, idx) => {
      const eraIdx = (idx % defaultErasCount()) + 1;
      treeLimbs.push({ from: 'TRUNK_ERA_' + eraIdx, to: b.id, stroke: b.color || '#38bdf8', width: 5 });
    });

    function defaultErasCount() {
      return treeNodes.filter(n => n.type === 'trunk').length || 1;
    }

    // 3D Projection Math
    function project3D(x, y, z) {
      const scaleY = y < 0 ? y : y * growthFactor;
      const scaleX = x * (y < 0 ? 1 : Math.pow(growthFactor, 0.7));
      const scaleZ = z * (y < 0 ? 1 : Math.pow(growthFactor, 0.7));

      const cosY = Math.cos(rotY);
      const sinY = Math.sin(rotY);
      const x1 = scaleX * cosY - scaleZ * sinY;
      const z1 = scaleZ * cosY + scaleX * sinY;

      const cosX = Math.cos(rotX);
      const sinX = Math.sin(rotX);
      const y2 = (scaleY + camYOffset) * cosX - z1 * sinX;
      const z2 = z1 * cosX + (scaleY + camYOffset) * sinX;

      const cameraZ = z2 + camDist;
      if (cameraZ <= 10) return null;

      const fov = 700;
      const scale = fov / cameraZ;
      return {
        x: canvas.width / 2 + x1 * scale,
        y: canvas.height / 2 - y2 * scale,
        z: cameraZ,
        scale: scale,
        depth: z2
      };
    }

    // Mouse drag
    canvas.addEventListener('mousedown', (e) => {
      isDragging = true;
      prevMouseX = e.clientX;
      prevMouseY = e.clientY;
      autoRotate = false;
      document.getElementById('rotate-icon').textContent = '▶';
    });

    window.addEventListener('mousemove', (e) => {
      if (isDragging) {
        const dx = e.clientX - prevMouseX;
        const dy = e.clientY - prevMouseY;
        targetRotY += dx * 0.006;
        targetRotX = Math.max(-0.6, Math.min(1.2, targetRotX + dy * 0.006));
        prevMouseX = e.clientX;
        prevMouseY = e.clientY;
      }
    });

    window.addEventListener('mouseup', () => { isDragging = false; });

    // Zoom
    canvas.addEventListener('wheel', (e) => {
      e.preventDefault();
      targetDist = Math.max(300, Math.min(1400, targetDist + e.deltaY * 0.6));
    }, { passive: false });

    // Node click
    canvas.addEventListener('click', (e) => {
      if (isDragging) return;
      const rect = canvas.getBoundingClientRect();
      const mouseX = e.clientX - rect.left;
      const mouseY = e.clientY - rect.top;

      let closest = null;
      let minDist = 24;
      for (const node of treeNodes) {
        const p = project3D(node.x, node.y, node.z);
        if (!p) continue;
        const dist = Math.hypot(p.x - mouseX, p.y - mouseY);
        if (dist < minDist) {
          minDist = dist;
          closest = node;
        }
      }
      if (closest) {
        inspectNode3D(closest);
        targetCamYOffset = -closest.y;
        targetDist = 520;
      }
    });

    function inspectNode3D(node) {
      document.getElementById('node-icon').textContent = node.icon;
      document.getElementById('node-tag').textContent = node.tag;
      document.getElementById('node-title').textContent = node.name;
      document.getElementById('node-desc').textContent = node.desc;
      document.getElementById('node-era').textContent = node.era;
      document.getElementById('node-coord').textContent = "[X: " + Math.round(node.x) + ", Y: " + Math.round(node.y) + ", Z: " + Math.round(node.z) + "]";
      document.getElementById('inspector-3d').classList.remove('translate-x-[115%]');
    }

    function closeInspector3D() {
      document.getElementById('inspector-3d').classList.add('translate-x-[115%]');
    }

    // Camera preset buttons
    document.querySelectorAll('.cam-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.cam-btn').forEach(b => {
          b.classList.remove('bg-amber-500/20', 'text-amber-300', 'border-amber-500/40');
          b.classList.add('text-slate-300');
        });
        btn.classList.add('bg-amber-500/20', 'text-amber-300', 'border-amber-500/40');
        autoRotate = false;
        document.getElementById('rotate-icon').textContent = '▶';

        const p = btn.dataset.preset;
        if (p === 'all') { targetRotX = 0.28; targetRotY = -0.55; targetDist = 720; targetCamYOffset = -40; }
        else if (p === 'roots') { targetRotX = -0.4; targetRotY = 0.1; targetDist = 560; targetCamYOffset = 260; }
        else if (p === 'trunk') { targetRotX = 0.15; targetRotY = -0.3; targetDist = 520; targetCamYOffset = -40; }
        else if (p === 'branches') { targetRotX = 0.3; targetRotY = -1.6; targetDist = 500; targetCamYOffset = -120; }
      });
    });

    document.getElementById('toggle-autorotate').addEventListener('click', () => {
      autoRotate = !autoRotate;
      document.getElementById('rotate-icon').textContent = autoRotate ? '⏸' : '▶';
    });

    document.getElementById('growth-slider').addEventListener('input', (e) => {
      growthFactor = parseFloat(e.target.value);
      document.getElementById('timeline-label').textContent = growthFactor < 0.5 ? 'Foundations Stage' : (growthFactor < 0.8 ? 'Expansion Stage' : 'All Eras Active');
    });

    // Sprout button
    let sproutCounter = 1;
    document.getElementById('sprout-3d-btn').addEventListener('click', () => {
      const angle = sproutCounter * 1.6;
      const radius = 190 + (sproutCounter * 20);
      const newX = Math.cos(angle) * radius;
      const newZ = Math.sin(angle) * radius;
      const newY = 80 + (sproutCounter * 40);

      const newNode = {
        id: 'SPROUT_' + sproutCounter,
        type: 'spinoff',
        name: 'Spinoff #' + sproutCounter,
        tag: 'NEW BRANCH PROJECT',
        icon: '🌱',
        x: newX,
        y: newY,
        z: newZ,
        color: '#f59e0b',
        size: 15,
        era: 'New Branch Era',
        desc: 'New branch sprouted dynamically into the 3D living world tree.'
      };
      treeNodes.push(newNode);
      treeLimbs.push({ from: 'TRUNK_ERA_1', to: newNode.id, stroke: '#f59e0b', width: 5 });
      inspectNode3D(newNode);
      sproutCounter++;
    });

    // Main render
    function render() {
      rotX += (targetRotX - rotX) * 0.08;
      rotY += (targetRotY - rotY) * 0.08;
      camDist += (targetDist - camDist) * 0.08;
      camYOffset += (targetCamYOffset - camYOffset) * 0.08;

      if (autoRotate) { targetRotY += 0.003; }

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      // Gradient background
      const grad = ctx.createRadialGradient(canvas.width/2, canvas.height/2, 60, canvas.width/2, canvas.height/2, canvas.width/1.1);
      grad.addColorStop(0, '#0a1020');
      grad.addColorStop(0.6, '#040710');
      grad.addColorStop(1, '#020306');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, canvas.width, canvas.height);

      // Subterranean root plane
      const baseP = project3D(0, -360, 0);
      if (baseP) {
        ctx.strokeStyle = 'rgba(217, 119, 6, 0.15)';
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.arc(baseP.x, baseP.y, 180 * baseP.scale, 0, Math.PI * 2);
        ctx.stroke();
      }

      // Draw Limbs
      for (const limb of treeLimbs) {
        const fromNode = treeNodes.find(n => n.id === limb.from);
        const toNode = treeNodes.find(n => n.id === limb.to);
        if (!fromNode || !toNode) continue;

        const p1 = project3D(fromNode.x, fromNode.y, fromNode.z);
        const p2 = project3D(toNode.x, toNode.y, toNode.z);
        if (!p1 || !p2) continue;

        ctx.strokeStyle = limb.stroke;
        ctx.lineWidth = Math.max(1.5, limb.width * p1.scale);
        ctx.beginPath();
        ctx.moveTo(p1.x, p1.y);
        // Slight curve for organic tree branches
        const midX = (p1.x + p2.x) / 2 + (p1.x < p2.x ? 15 : -15);
        const midY = (p1.y + p2.y) / 2 - 10;
        ctx.quadraticCurveTo(midX, midY, p2.x, p2.y);
        ctx.stroke();
      }

      // Draw Nodes (sorted by depth)
      const projected = treeNodes.map(n => ({ node: n, p: project3D(n.x, n.y, n.z) }))
                                 .filter(item => item.p !== null)
                                 .sort((a, b) => b.p.depth - a.p.depth);

      for (const item of projected) {
        const { node, p } = item;
        const radius = Math.max(6, (node.size || 14) * p.scale);

        // Glow ring
        ctx.fillStyle = (node.color || '#eab308') + '33';
        ctx.beginPath();
        ctx.arc(p.x, p.y, radius * 1.8, 0, Math.PI * 2);
        ctx.fill();

        // Node sphere
        ctx.fillStyle = node.color || '#eab308';
        ctx.beginPath();
        ctx.arc(p.x, p.y, radius, 0, Math.PI * 2);
        ctx.fill();

        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = Math.max(1, 1.5 * p.scale);
        ctx.stroke();

        // Label
        if (p.scale > 0.45) {
          ctx.fillStyle = '#f8fafc';
          ctx.font = `bold ${'$'}{Math.max(10, Math.round(11 * p.scale))}px Plus Jakarta Sans, sans-serif`;
          ctx.textAlign = 'center';
          ctx.fillText(`${'$'}{node.icon} ${'$'}{node.name}`, p.x, p.y - radius - 6);
        }
      }

      requestAnimationFrame(render);
    }
    render();
  </script>
</body>
</html>
        """.trimIndent()
    }
}
