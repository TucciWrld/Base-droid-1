package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Content
import com.example.data.GenerateContentRequest
import com.example.data.Part
import com.example.data.RetrofitClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// --- Data Models ---

enum class Screen {
    BOOT, DESKTOP, APPS_LIST, SETTINGS, TERMINAL, ASSISTANT, STORE, FILES, SECURITY, PERFORMANCE, AOD
}

enum class MultitaskingMode {
    STANDARD, SPLIT_SCREEN, DESKTOP_MODE
}

data class FloatingWindow(
    val id: String,
    val title: String,
    val screen: Screen,
    var offsetX: Float = 50f,
    var offsetY: Float = 150f,
    var widthDp: Int = 320,
    var heightDp: Int = 400,
    var isMinimized: Boolean = false,
    var isMaximized: Boolean = false
)

data class VirtualFile(
    val name: String,
    val path: String,
    val sizeKb: Int,
    var content: String,
    val isSystem: Boolean = false
)

data class TerminalLine(
    val text: String,
    val type: LineType = LineType.INFO
)

enum class LineType {
    INPUT, INFO, SUCCESS, ERROR, ACCENT
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "12:00"
)

data class StoreApp(
    val packageName: String,
    val name: String,
    val category: String,
    val description: String,
    val rating: Float,
    val isInstalledByDefault: Boolean = false,
    var installState: InstallState = InstallState.NOT_INSTALLED
)

enum class InstallState {
    NOT_INSTALLED, INSTALLING, INSTALLED, UPDATE_AVAILABLE
}

data class FileCategory(
    val name: String,
    val color: Color,
    val sizeGb: Float
)

class BaseDroidViewModel : ViewModel() {

    // --- Boot & Screen States ---
    private val _currentScreen = MutableStateFlow(Screen.BOOT)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _bootProgress = MutableStateFlow(0f)
    val bootProgress: StateFlow<Float> = _bootProgress.asStateFlow()

    private val _bootLogs = MutableStateFlow<List<String>>(emptyList())
    val bootLogs: StateFlow<List<String>> = _bootLogs.asStateFlow()

    // --- Device Status / Telemetry ---
    private val _usedMemoryMb = MutableStateFlow(4210) // of 8192 MB (8GB)
    val usedMemoryMb: StateFlow<Int> = _usedMemoryMb.asStateFlow()

    private val _cpuTemp = MutableStateFlow(41.4f)
    val cpuTemp: StateFlow<Float> = _cpuTemp.asStateFlow()

    private val _batteryLevel = MutableStateFlow(84)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _batterySaver = MutableStateFlow(false)
    val batterySaver: StateFlow<Boolean> = _batterySaver.asStateFlow()

    private val _gamingMode = MutableStateFlow(false)
    val gamingMode: StateFlow<Boolean> = _gamingMode.asStateFlow()

    private val _performanceGovernor = MutableStateFlow("Balanced") // Performance, Balanced, PowerSave
    val performanceGovernor: StateFlow<String> = _performanceGovernor.asStateFlow()

    private val _wiFiEnabled = MutableStateFlow(true)
    val wiFiEnabled: StateFlow<Boolean> = _wiFiEnabled.asStateFlow()

    private val _bluetoothEnabled = MutableStateFlow(true)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    private val _nfcEnabled = MutableStateFlow(false)
    val nfcEnabled: StateFlow<Boolean> = _nfcEnabled.asStateFlow()

    private val _hotspotEnabled = MutableStateFlow(false)
    val hotspotEnabled: StateFlow<Boolean> = _hotspotEnabled.asStateFlow()

    private val _screenCasting = MutableStateFlow(false)
    val screenCasting: StateFlow<Boolean> = _screenCasting.asStateFlow()

    // --- Multitasking Modes ---
    private val _multitaskingMode = MutableStateFlow(MultitaskingMode.STANDARD)
    val multitaskingMode: StateFlow<MultitaskingMode> = _multitaskingMode.asStateFlow()

    private val _floatingWindows = MutableStateFlow<List<FloatingWindow>>(emptyList())
    val floatingWindows: StateFlow<List<FloatingWindow>> = _floatingWindows.asStateFlow()

    private val _splitScreenTop = MutableStateFlow(Screen.PERFORMANCE)
    val splitScreenTop: StateFlow<Screen> = _splitScreenTop.asStateFlow()

    private val _splitScreenBottom = MutableStateFlow(Screen.TERMINAL)
    val splitScreenBottom: StateFlow<Screen> = _splitScreenBottom.asStateFlow()

    // --- Customization ---
    private val _accentColorIndex = MutableStateFlow(0) // 0: Cyan, 1: Pink, 2: Emerald, 3: Purple, 4: Amber
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    private val _clockStyleIndex = MutableStateFlow(0) // 0: Cyber Analog, 1: Big Digital, 2: Minimalist
    val clockStyleIndex: StateFlow<Int> = _clockStyleIndex.asStateFlow()

    private val _bootAnimationIndex = MutableStateFlow(0) // 0: Grid Matrix, 1: Infinite Circle, 2: Retro Terminal
    val bootAnimationIndex: StateFlow<Int> = _bootAnimationIndex.asStateFlow()

    private val _alwaysOnDisplayIndex = MutableStateFlow(0) // 0: Dot Clock, 1: Neon Ring, 2: Matrix Rain
    val alwaysOnDisplayIndex: StateFlow<Int> = _alwaysOnDisplayIndex.asStateFlow()

    private val _currentWallpaperIndex = MutableStateFlow(0) // Custom Wallpaper Backgrounds
    val currentWallpaperIndex: StateFlow<Int> = _currentWallpaperIndex.asStateFlow()

    val accentColors = listOf(
        Color(0xFF00F0FF), // Cyber Cyan
        Color(0xFFFF007F), // Synth Pink
        Color(0xFF00FF66), // Electronic Emerald
        Color(0xFFBD00FF), // Ultraviolet Purple
        Color(0xFFFFB300)  // Golden Amber
    )

    val colorNames = listOf("Neon Cyan", "Synth Pink", "Vivid Emerald", "Electric Purple", "Cyber Amber")

    // --- Malware Scanning ---
    private val _isScanningMalware = MutableStateFlow(false)
    val isScanningMalware: StateFlow<Boolean> = _isScanningMalware.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanningFileName = MutableStateFlow("")
    val scanningFileName: StateFlow<String> = _scanningFileName.asStateFlow()

    private val _threatsFound = MutableStateFlow(0)
    val threatsFound: StateFlow<Int> = _threatsFound.asStateFlow()

    private val _malwareFilesChecked = MutableStateFlow(0)
    val malwareFilesChecked: StateFlow<Int> = _malwareFilesChecked.asStateFlow()

    // --- Store App Data ---
    private val _storeApps = MutableStateFlow<List<StoreApp>>(emptyList())
    val storeApps: StateFlow<List<StoreApp>> = _storeApps.asStateFlow()

    // --- Virtual Files State ---
    private val _virtualFiles = MutableStateFlow<List<VirtualFile>>(emptyList())
    val virtualFiles: StateFlow<List<VirtualFile>> = _virtualFiles.asStateFlow()

    // --- Code Editor Target ---
    private val _editingFile = MutableStateFlow<VirtualFile?>(null)
    val editingFile: StateFlow<VirtualFile?> = _editingFile.asStateFlow()

    // --- Interactive Terminal ---
    private val _terminalHistory = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalHistory: StateFlow<List<TerminalLine>> = _terminalHistory.asStateFlow()

    // --- AI Assistant ---
    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    init {
        setupInitialFiles()
        setupStoreApps()
        runBootSequence()
        setupInitialTerminal()
        setupInitialAIChat()
        startSensorsSimulation()
    }

    // --- Boot Animation Sequence ---
    private fun runBootSequence() {
        viewModelScope.launch {
            val kernels = listOf(
                "Initializing Linux Kernel v6.11.2-aosp+...",
                "Loading Base Droid Hardware Abstraction Layer (HAL)...",
                "Booting Android Runtime (ART) with Qualcomm Elite optimizations...",
                "Starting SurfaceFlinger compositor on ARM64-v8a target...",
                "Mounting secure /userdata & /system with AES-256 encryption...",
                "Spawning Base Droid Core Subsystems (Zygote)...",
                "Readying SystemUI, Launcher, and Devtools toolkit...",
                "Base Droid OS loaded successfully. Interface unlocked."
            )
            for (i in kernels.indices) {
                _bootLogs.value = _bootLogs.value + kernels[i]
                delay(220)
                _bootProgress.value = (i + 1).toFloat() / kernels.size
            }
            _currentScreen.value = Screen.DESKTOP
        }
    }

    fun skipBoot() {
        _currentScreen.value = Screen.DESKTOP
    }

    // --- Sensors Simulation Logic ---
    private fun startSensorsSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(3000)
                // CPU Thermal Fluctuations
                val baseTemp = if (_gamingMode.value) 48.0f else if (_batterySaver.value) 36.0f else 41.0f
                val delta = (Math.random().toFloat() - 0.5f) * 1.5f
                _cpuTemp.value = (baseTemp + delta).coerceIn(30f, 65f)

                // Battery Drain
                val rate = if (_batterySaver.value) 0.05f else if (_gamingMode.value) 0.3f else 0.1f
                if (Math.random() < rate) {
                    _batteryLevel.value = (_batteryLevel.value - 1).coerceAtLeast(1)
                }

                // RAM allocations fluctuation slightly
                val ramBase = if (_gamingMode.value) 5300 else if (_batterySaver.value) 3100 else 4200
                _usedMemoryMb.value = (ramBase + (Math.random() * 80).toInt()).coerceIn(2000, 8192)
            }
        }
    }

    // --- Setup Initial Apps & Files ---
    private fun setupStoreApps() {
        _storeApps.value = listOf(
            StoreApp(
                packageName = "com.aistudio.base.terminal",
                name = "Base Terminal Core",
                category = "Development",
                description = "Advanced command-line environment for auditing, scripting, and shell customization.",
                rating = 4.9f,
                isInstalledByDefault = true,
                installState = InstallState.INSTALLED
            ),
            StoreApp(
                packageName = "com.aistudio.base.files",
                name = "Base Files Pro",
                category = "System Utilities",
                description = "Secure sandboxed storage controller with multi-format archives, deep audits, and disk cleaning.",
                rating = 4.8f,
                isInstalledByDefault = true,
                installState = InstallState.INSTALLED
            ),
            StoreApp(
                packageName = "com.aistudio.base.assistant",
                name = "Hyper AI Optimizer",
                category = "AI / Assistance",
                description = "Intelligent AI module providing code generation, diagnostic advice, and hardware tuning helper.",
                rating = 4.7f,
                isInstalledByDefault = true,
                installState = InstallState.INSTALLED
            ),
            StoreApp(
                packageName = "com.aistudio.camera.pro",
                name = "Camdroid Advanced",
                category = "Multimedia",
                description = "Professional camera abstraction layer using full Camera2 architecture with RAW extraction & filters.",
                rating = 4.5f,
                installState = InstallState.NOT_INSTALLED
            ),
            StoreApp(
                packageName = "com.aistudio.game.decr",
                name = "Subsystem Emulator",
                category = "Development",
                description = "X86 / ARM virtualization wrapper optimized to run legacy games with hardware graphics mapping layers.",
                rating = 4.9f,
                installState = InstallState.NOT_INSTALLED
            ),
            StoreApp(
                packageName = "com.aistudio.crypto.locker",
                name = "Crypto Locker",
                category = "Security",
                description = "Encapsulates selected folders in an isolated container secured with dynamic cryptographic tokens.",
                rating = 4.6f,
                installState = InstallState.NOT_INSTALLED
            )
        )
    }

    private fun setupInitialFiles() {
        _virtualFiles.value = listOf(
            VirtualFile(
                name = "readme.md",
                path = "/sdcard/readme.md",
                sizeKb = 2,
                content = """# Base Droid Operating System
Welcome to Base Droid - A high-performance, ultra-secured custom Android operating system simulation built on the Android Open Source Project (AOSP) framework.

## Key Features
- High-Performance Custom Launcher
- Memory, Battery, and Gaming Engines
- Smart Terminal Controller
- Adaptive Theming Ecosystem
- Core-Sandbox Encrypted Vaults
- Built-in AI Assistant & Security Inspector
"""
            ),
            VirtualFile(
                name = "neofetch.config",
                path = "/system/etc/neofetch.config",
                sizeKb = 1,
                content = """# Neofetch Theme Configuration
SHOW_OS_BRAND=true
SHOW_KERNEL=true
SHOW_HARDWARE_EMULATOR=true
COLOR_PALETTE=cyber_glow
THEME_FONT=Space Grotesk
"""
            ),
            VirtualFile(
                name = "performance_tune.sh",
                path = "/sdcard/scripts/performance_tune.sh",
                sizeKb = 3,
                content = """#!/system/bin/sh
echo "=== Base Droid Memory Optimization ==="
echo "Stopping redundant background services..."
am force-stop com.android.providers.downloads
am force-stop com.android.printspooler
echo "Clearing system memory cache buffers..."
sysctl -w vm.drop_caches=3
echo "Governor changed to performance..."
echo "performance" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor
echo "Memory optimizations initialized. Delta recovered!"
"""
            ),
            VirtualFile(
                name = "index.html",
                path = "/sdcard/www/index.html",
                sizeKb = 1,
                content = """<!DOCTYPE html>
<html>
<head>
  <style>
    body { background-color: #08080c; color: #00f0ff; font-family: monospace; }
  </style>
</head>
<body>
  <h1>Base Droid ROM Status WebApp</h1>
  <p>System operational on custom port 8080.</p>
</body>
</html>
"""
            )
        )
    }

    private fun setupInitialTerminal() {
        _terminalHistory.value = listOf(
            TerminalLine("Base Droid DevTools Subshell [Version AOSP-17.06]", LineType.ACCENT),
            TerminalLine("Kernel mapping: com.aistudio.basedroid.qkwjny core-host initialized.", LineType.INFO),
            TerminalLine("Type 'help' to see list of valid operating system Commands.", LineType.SUCCESS),
            TerminalLine("", LineType.INFO)
        )
    }

    private fun setupInitialAIChat() {
        _aiMessages.value = listOf(
            ChatMessage("Hello! I am your Base Droid AI System Companion. I am loaded directly into the core ROM to assist you with device diagnostics, terminal shell scripts, optimization recommendations, or standard translations. How can I assist you today?", false, "12:00")
        )
    }

    // --- Action Handlers: System Toggles ---
    fun setPerformanceGovernor(gov: String) {
        _performanceGovernor.value = gov
        // Update simulation telemetry
        when (gov) {
            "Performance" -> {
                _usedMemoryMb.value = (_usedMemoryMb.value + 600).coerceAtMost(8192)
                _cpuTemp.value = (_cpuTemp.value + 6.0f).coerceAtMost(65f)
            }
            "PowerSave" -> {
                _usedMemoryMb.value = (_usedMemoryMb.value - 400).coerceAtLeast(1500)
                _cpuTemp.value = (_cpuTemp.value - 4.0f).coerceAtLeast(30f)
            }
            "Balanced" -> {
                _cpuTemp.value = 41.5f
            }
        }
        addTerminalSystemLog("System governor altered to: $gov mode.")
    }

    fun toggleBatterySaver() {
        val active = !_batterySaver.value
        _batterySaver.value = active
        if (active) {
            _gamingMode.value = false
            _performanceGovernor.value = "PowerSave"
            _usedMemoryMb.value = (_usedMemoryMb.value - 600).coerceAtLeast(1800)
            _cpuTemp.value = (_cpuTemp.value - 3.5f).coerceAtLeast(30f)
        } else {
            _performanceGovernor.value = "Balanced"
        }
        addTerminalSystemLog("Battery Saver ${if (active) "ENABLED" else "DISABLED"}.")
    }

    fun toggleGamingMode() {
        val active = !_gamingMode.value
        _gamingMode.value = active
        if (active) {
            _batterySaver.value = false
            _performanceGovernor.value = "Performance"
            _usedMemoryMb.value = (_usedMemoryMb.value + 800).coerceAtMost(8100)
            _cpuTemp.value = (_cpuTemp.value + 5.0f).coerceAtMost(65f)
        } else {
            _performanceGovernor.value = "Balanced"
        }
        addTerminalSystemLog("Gaming Engine ${if (active) "ACTIVE" else "DEACTIVATED"}. Graphics renderer focused.")
    }

    fun toggleWiFi() { _wiFiEnabled.value = !_wiFiEnabled.value }
    fun toggleBluetooth() { _bluetoothEnabled.value = !_bluetoothEnabled.value }
    fun toggleNfc() { _nfcEnabled.value = !_nfcEnabled.value }
    fun toggleHotspot() { _hotspotEnabled.value = !_hotspotEnabled.value }
    fun toggleScreenCasting() { _screenCasting.value = !_screenCasting.value }

    // --- Theme Configurations ---
    fun selectAccentColor(index: Int) {
        _accentColorIndex.value = index
        addTerminalSystemLog("Accent Palette adjusted to: ${colorNames[index]}.")
    }

    fun selectClockStyle(index: Int) { _clockStyleIndex.value = index }
    fun selectBootAnimation(index: Int) { _bootAnimationIndex.value = index }
    fun selectAlwaysOnDisplay(index: Int) { _alwaysOnDisplayIndex.value = index }
    fun selectWallpaper(index: Int) { _currentWallpaperIndex.value = index }

    // --- Screen Navigation ---
    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // --- Multitasking View Management ---
    fun setMultitaskingMode(mode: MultitaskingMode) {
        _multitaskingMode.value = mode
        if (mode == MultitaskingMode.DESKTOP_MODE) {
            // Setup default open floating windows if none are open
            if (_floatingWindows.value.isEmpty()) {
                _floatingWindows.value = listOf(
                    FloatingWindow("term", "Base Terminal", Screen.TERMINAL, 40f, 160f, 310, 320),
                    FloatingWindow("perf", "Performance Diagnostics", Screen.PERFORMANCE, 80f, 400f, 310, 300)
                )
            }
        }
    }

    fun closeFloatingWindow(id: String) {
        _floatingWindows.value = _floatingWindows.value.filter { it.id != id }
    }

    fun updateFloatingWindowOffset(id: String, dX: Float, dY: Float) {
        _floatingWindows.value = _floatingWindows.value.map { w ->
            if (w.id == id) {
                w.copy(offsetX = (w.offsetX + dX).coerceIn(0f, 1000f), offsetY = (w.offsetY + dY).coerceIn(100f, 1800f))
            } else w
        }
    }

    fun focusFloatingWindow(id: String) {
        val window = _floatingWindows.value.find { it.id == id } ?: return
        _floatingWindows.value = _floatingWindows.value.filter { it.id != id } + window
    }

    fun toggleMinimizeFloatingWindow(id: String) {
        _floatingWindows.value = _floatingWindows.value.map { w ->
            if (w.id == id) w.copy(isMinimized = !w.isMinimized) else w
        }
    }

    fun openAppInFloatingWindow(title: String, screen: Screen) {
        val id = screen.name.lowercase()
        // If already open, just focus and unminimize
        if (_floatingWindows.value.any { it.id == id }) {
            _floatingWindows.value = _floatingWindows.value.map { w ->
                if (w.id == id) w.copy(isMinimized = false) else w
            }
            focusFloatingWindow(id)
        } else {
            val newWin = FloatingWindow(
                id = id,
                title = title,
                screen = screen,
                offsetX = 80f + (_floatingWindows.value.size * 30f),
                offsetY = 180f + (_floatingWindows.value.size * 30f),
                widthDp = 300,
                heightDp = 380
            )
            _floatingWindows.value = _floatingWindows.value + newWin
        }
        _multitaskingMode.value = MultitaskingMode.DESKTOP_MODE
    }

    fun setSplitScreenTopApp(screen: Screen) { _splitScreenTop.value = screen }
    fun setSplitScreenBottomApp(screen: Screen) { _splitScreenBottom.value = screen }

    // --- Memory Optimization Engine ---
    fun optimizeMemory() {
        viewModelScope.launch {
            _usedMemoryMb.value = (_usedMemoryMb.value + 400).coerceAtMost(8192) // surge temporarily
            delay(1200)
            // Reclaim
            val finalTarget = if (_gamingMode.value) 4400 else if (_batterySaver.value) 2400 else 3110
            _usedMemoryMb.value = finalTarget
            _cpuTemp.value = (_cpuTemp.value - 2.5f).coerceAtLeast(32f)

            // Log update
            addTerminalSystemLog("Memory reclaimed. Freed up redundant heap fragments successfully.")
        }
    }

    // --- Malware Scanning Operations ---
    fun runMalwareScan() {
        viewModelScope.launch {
            _isScanningMalware.value = true
            _scanProgress.value = 0f
            _threatsFound.value = 0
            _malwareFilesChecked.value = 0

            val itemsToScan = listOf(
                "/system/bin/init", "/system/lib64/libart.so", "/vendor/firmware/nv_modem.bin",
                "/data/app/com.unauthorized.cryptominer/base.apk", "/sdcard/downloads/updater_v2.apk",
                "/system/xbin/su", "/sdcard/DCIM/camera_cache.idx", "/data/system/users/0/tokens.db",
                "/system/app/PackageInstaller.apk", "/vendor/lib/hw/audio.r_submix.so"
            )

            for (i in itemsToScan.indices) {
                _scanningFileName.value = itemsToScan[i]
                delay(350)
                _malwareFilesChecked.value = _malwareFilesChecked.value + 1
                _scanProgress.value = (i + 1).toFloat() / itemsToScan.size

                // Simulate finding one trace in downloads directory
                if (itemsToScan[i].contains("cryptominer") || itemsToScan[i].contains("updater_v2")) {
                    _threatsFound.value = _threatsFound.value + 1
                }
            }

            _isScanningMalware.value = false
            addTerminalSystemLog("Malware Scan complete: ${_threatsFound.value} threads quarantined.")
        }
    }

    // --- App Store Operations ---
    fun installApp(packageName: String) {
        viewModelScope.launch {
            _storeApps.value = _storeApps.value.map { app ->
                if (app.packageName == packageName) {
                    app.copy(installState = InstallState.INSTALLING)
                } else app
            }

            delay(2000) // Install simulation delay

            _storeApps.value = _storeApps.value.map { app ->
                if (app.packageName == packageName) {
                    app.copy(installState = InstallState.INSTALLED)
                } else app
            }

            val appName = _storeApps.value.find { it.packageName == packageName }?.name ?: "App"
            addTerminalSystemLog("Installed OS Package: $appName successfully.")
        }
    }

    // --- Virtual Files Actions ---
    fun createVirtualFile(filename: String, folder: String, content: String) {
        val path = "${folder}/${filename}"
        val newFile = VirtualFile(
            name = filename,
            path = path,
            sizeKb = (content.length / 1024).coerceAtLeast(1),
            content = content
        )
        _virtualFiles.value = _virtualFiles.value.filterNot { it.path == path } + newFile
        addTerminalSystemLog("Created file: $path.")
    }

    fun startEditingFile(file: VirtualFile) {
        _editingFile.value = file
    }

    fun saveEditingFile(updatedContent: String) {
        val file = _editingFile.value ?: return
        _virtualFiles.value = _virtualFiles.value.map { f ->
            if (f.path == file.path) {
                f.copy(content = updatedContent, sizeKb = (updatedContent.length / 1024).coerceAtLeast(1))
            } else f
        }
        _editingFile.value = null
        addTerminalSystemLog("Modified file: ${file.path}")
    }

    fun cancelEditingFile() {
        _editingFile.value = null
    }

    fun deleteVirtualFile(path: String) {
        _virtualFiles.value = _virtualFiles.value.filterNot { f -> f.path == path }
        addTerminalSystemLog("Deleted file: $path.")
    }

    // --- Terminal Engine Commands ---
    fun executeTerminalCommand(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return

        val cmdHist = _terminalHistory.value.toMutableList()
        cmdHist.add(TerminalLine("$ $trimmed", LineType.INPUT))

        val parts = trimmed.split("\\s+".toRegex())
        val command = parts[0].lowercase()
        val args = parts.drop(1)

        when (command) {
            "help" -> {
                cmdHist.add(TerminalLine("=== Base Droid OS Terminus Assistant (AOSP-17) ===", LineType.ACCENT))
                cmdHist.add(TerminalLine("help             - Show detailed utility catalogs", LineType.INFO))
                cmdHist.add(TerminalLine("neofetch         - Inspect custom kernel specifications", LineType.INFO))
                cmdHist.add(TerminalLine("clean-ram        - Command optimization memory flush", LineType.INFO))
                cmdHist.add(TerminalLine("cpu-load         - Live sensor temperature feedback", LineType.INFO))
                cmdHist.add(TerminalLine("pm list          - Output package dependencies status", LineType.INFO))
                cmdHist.add(TerminalLine("storage audit    - Render details container storage", LineType.INFO))
                cmdHist.add(TerminalLine("vi [filename]    - Quick read/write workspace text files", LineType.INFO))
                cmdHist.add(TerminalLine("logcat           - Stream real-time kernel thread registers", LineType.INFO))
                cmdHist.add(TerminalLine("ai-diagnose      - Run offline smart ROM audit advisory", LineType.INFO))
                cmdHist.add(TerminalLine("scan             - Perform quick malware inspection", LineType.INFO))
                cmdHist.add(TerminalLine("clear            - Flush shell output registers", LineType.INFO))
            }
            "neofetch" -> {
                val colorSet = colorNames[_accentColorIndex.value]
                val block = """
                     _     Base Droid OS AOSP 17.0.6
                   / \    -------------------------
                  |   |   Platform: Snapdragon 8-Elite Custom Abstraction
                   \ /    Kernel: Linux-BaseDroid-6.11.2-aosp+
                    V     Uptime: ${getSimulatedUptime()}
                          App Containers: ${_storeApps.value.filter { it.installState == InstallState.INSTALLED }.size}
                          Memory: ${_usedMemoryMb.value} MB / 8192 MB (8GB)
                          Temperature: ${_cpuTemp.value}°C
                          Theme Tone: $colorSet Theme [Dark Grid]
                """.trimIndent()
                block.split("\n").forEach { line ->
                    cmdHist.add(TerminalLine(line, LineType.SUCCESS))
                }
            }
            "clean-ram" -> {
                cmdHist.add(TerminalLine("Dropping caches system registers (vm/drop_caches=3)...", LineType.INFO))
                cmdHist.add(TerminalLine("Reclaiming redundant ART thread cache arrays...", LineType.INFO))
                optimizeMemory()
                cmdHist.add(TerminalLine("Done! Heap optimization recovered 1.1GB space.", LineType.SUCCESS))
            }
            "cpu-load" -> {
                cmdHist.add(TerminalLine("Checking CPU clock speeds and thermals...", LineType.INFO))
                cmdHist.add(TerminalLine("Core 0-3 Cluster (Efficiency): 1.84 GHz  |  41.0°C", LineType.SUCCESS))
                cmdHist.add(TerminalLine("Core 4-6 Cluster (Gold):       2.42 GHz  |  43.2°C", LineType.SUCCESS))
                cmdHist.add(TerminalLine("Core 7   Cluster (Prime):      3.00 GHz  |  ${_cpuTemp.value}°C", LineType.ACCENT))
                cmdHist.add(TerminalLine("Governor: ${_performanceGovernor.value} | Thermal State: Optimal", LineType.SUCCESS))
            }
            "pm" -> {
                if (args.firstOrNull() == "list") {
                    cmdHist.add(TerminalLine("Active ROM System Packages:", LineType.ACCENT))
                    _storeApps.value.forEach { app ->
                        val state = if (app.installState == InstallState.INSTALLED) "ACTIVE" else "DISABLED"
                        cmdHist.add(TerminalLine("- ${app.packageName} ($state)", LineType.INFO))
                    }
                } else {
                    cmdHist.add(TerminalLine("Error: Invalid pm argument. Use 'pm list'.", LineType.ERROR))
                }
            }
            "storage" -> {
                if (args.firstOrNull() == "audit") {
                    cmdHist.add(TerminalLine("Disk Storage Audit Map:", LineType.ACCENT))
                    cmdHist.add(TerminalLine("/system   [RootFS]:  2.8 GB / 4.0 GB (Solid)", LineType.INFO))
                    cmdHist.add(TerminalLine("/data     [User]:    1.4 GB / 8.0 GB (Encrypted)", LineType.INFO))
                    cmdHist.add(TerminalLine("/sdcard   [Shared]:  0.9 GB / 16.0 GB", LineType.INFO))
                    cmdHist.add(TerminalLine("/recovery [Safemod]: 0.1 GB / 0.5 GB", LineType.INFO))
                    val totalVirtual = _virtualFiles.value.size
                    cmdHist.add(TerminalLine("Local simulation workspace contains $totalVirtual files.", LineType.SUCCESS))
                } else {
                    cmdHist.add(TerminalLine("Error: Bad storage command. Type 'storage audit'.", LineType.ERROR))
                }
            }
            "vi" -> {
                val targetName = args.firstOrNull()
                if (targetName == null) {
                    cmdHist.add(TerminalLine("Error: File parameter blank. Usage: 'vi [filename]'", LineType.ERROR))
                } else {
                    val file = _virtualFiles.value.find { it.name == targetName }
                    if (file != null) {
                        cmdHist.add(TerminalLine("=== Opened: ${file.name} ===", LineType.ACCENT))
                        file.content.split("\n").forEach { line ->
                            cmdHist.add(TerminalLine(line, LineType.INFO))
                        }
                        cmdHist.add(TerminalLine("==============================", LineType.ACCENT))
                        cmdHist.add(TerminalLine("Notice: For rich edits, launch File Explorer Core directly.", LineType.ACCENT))
                    } else {
                        // Create immediately
                        cmdHist.add(TerminalLine("File not found. Generating dummy text file: /sdcard/$targetName", LineType.INFO))
                        createVirtualFile(targetName, "/sdcard", "# New Script Base\n")
                    }
                }
            }
            "logcat" -> {
                cmdHist.add(TerminalLine("Dumping last 6 virtual events from buffer register:", LineType.ACCENT))
                getSimulatedLogs().forEach { log ->
                    cmdHist.add(TerminalLine(log, LineType.INFO))
                }
            }
            "clear" -> {
                _terminalHistory.value = emptyList()
                return
            }
            "ai-diagnose" -> {
                cmdHist.add(TerminalLine("Querying Base Droid AI diagnostic agent. Please stand by...", LineType.INFO))
                triggerAIDiagnoseInTerm()
            }
            "scan" -> {
                cmdHist.add(TerminalLine("Spawning virtual malware inspection threat sweeps...", LineType.INFO))
                runMalwareScan()
                cmdHist.add(TerminalLine("Malware sweeps completed. Inspect Security Hub for detailed quarantine records.", LineType.SUCCESS))
            }
            else -> {
                cmdHist.add(TerminalLine("bash: command not found: $command", LineType.ERROR))
                cmdHist.add(TerminalLine("Type 'help' to review valid OS and kernel commands.", LineType.INFO))
            }
        }

        _terminalHistory.value = cmdHist
    }

    private fun triggerAIDiagnoseInTerm() {
        viewModelScope.launch {
            delay(1200)
            val currentHist = _terminalHistory.value.toMutableList()
            val prompt = "Based on current OS configurations: Memory=$_usedMemoryMb MB, Temp=$_cpuTemp°C, Gov=$_performanceGovernor, what are your quick diagnostic optimization suggestions? Act as Base Droid OS helper."
            _isAiThinking.value = true
            val response = getInternalAIResponse(prompt)
            _isAiThinking.value = false
            currentHist.add(TerminalLine("=== Base Droid AI Diagnostic Output ===", LineType.ACCENT))
            response.split("\n").forEach { line ->
                currentHist.add(TerminalLine(line, LineType.SUCCESS))
            }
            _terminalHistory.value = currentHist
        }
    }

    private fun addTerminalSystemLog(message: String) {
        val line = TerminalLine("[System Monitor] $message", LineType.SUCCESS)
        _terminalHistory.value = _terminalHistory.value + line
    }

    private fun getSimulatedUptime(): String {
        return "2h 43m 12s"
    }

    private fun getSimulatedLogs(): List<String> {
        return listOf(
            "I/BaseDroidLauncher: loading grid launcher dashboard in 22ms",
            "D/PackageManagerService: loading security manager tokens",
            "I/ThermalEngine: core 0 efficiency profiles configured",
            "D/MemoryGovernor: dropped 4 background garbage collect pools",
            "W/WifiController: wireless connectivity ping checked - latency optimal",
            "I/BaseDroidStore: checking system applications repository updates"
        )
    }

    // --- AI Chat Assistant Handler ---
    fun sendChatMessage(text: String) {
        val trimmed = text.trim()
        if (trimText(trimmed).isEmpty()) return

        val messages = _aiMessages.value.toMutableList()
        messages.add(ChatMessage(trimmed, true))
        _aiMessages.value = messages

        viewModelScope.launch {
            _isAiThinking.value = true
            val apiResponse = getInternalAIResponse(trimmed)
            _isAiThinking.value = false
            _aiMessages.value = _aiMessages.value + ChatMessage(apiResponse, false)
        }
    }

    // Support offline queries with direct matrix fallback if BuildConfig is not loaded/empty
    private suspend fun getInternalAIResponse(prompt: String): String {
        val apiKey = com.example.BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (hasKey) {
            // Direct REST API Call based on gemini-api directives
            val request = GenerateContentRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                systemInstruction = Content(parts = listOf(Part(text = "You are Base Droid AI, the built-in cybernetic artificial helper for the custom Base Droid Android OS ROM. Respond with a technical, high-performance cyber-assistant personality but remain highly helpful, precise, and polite. Tell how to tweak settings, write shell files, run terminal diagnostics, or answer questions.")))
            )
            return try {
                val response = RetrofitClient.service.generateContent(apiKey, request)
                response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "No system response obtained from the core kernel gateway."
            } catch (e: Exception) {
                getLocalResponseFallback(prompt) + "\n\n(AI note: REST connection timed out; falling back to offline diagnostic database)."
            }
        } else {
            return getLocalResponseFallback(prompt)
        }
    }

    private fun getLocalResponseFallback(prompt: String): String {
        val l = prompt.lowercase()
        return when {
            l.contains("memory") || l.contains("ram") || l.contains("optimize") -> {
                "**[Base Droid Performance Report]** Memory allocation is perfectly normal. Running `clean-ram` dropped caches to 2.8 GB. I recommend gaming governor configs to release higher dynamic clock triggers."
            }
            l.contains("battery") || l.contains("saver") || l.contains("drain") -> {
                "**[Base Droid Diagnostics]** Under battery-saver configuration, background processes are strictly limited and CPU prime core performance is limited to 1.84 GHz, extending runtime by approximately 3.4 hours."
            }
            l.contains("malware") || l.contains("hazard") || l.contains("security") -> {
                "**[Base Droid Security]** Sandbox partition is running normal AES-256 protocols. I suggest starting a 'Malware Inspection Sweep' via the Security Dashboard to audit external app signatures."
            }
            l.contains("terminal") || l.contains("shell") || l.contains("commands") -> {
                "**[Base Droid Terminal]** Core commands available include: `neofetch`, `clean-ram`, `cpu-load`, `pm list`, `storage audit`, `vi`, and `logcat`."
            }
            l.contains("code") || l.contains("scripts") || l.contains("html") -> {
                "**[Base Droid Developer]** I see files like `performance_tune.sh` are defined in your workspace storage. Launch our Files utility and select edit files to modify code directly in our built-in Editor."
            }
            else -> {
                "**[Base Droid AI Optimizer]** Core operational status: OPTIMAL.\n\n- System governor: ${_performanceGovernor.value}\n- Core Heat status: ${_cpuTemp.value}°C\n- Total ROM app modules: ${_storeApps.value.size}\n\nAsk me about security manager status, customization settings, memory optimizations, or how to write modular shell scripts."
            }
        }
    }

    private fun trimText(t: String): String {
        return t.trim()
    }
}
