package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceLighter
import com.example.ui.theme.GridLineColor
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemViewModel: BaseDroidViewModel = viewModel()
            val accentColorIndex by systemViewModel.accentColorIndex.collectAsState()
            val activeAccent = systemViewModel.accentColors[accentColorIndex]

            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CarbonDark
                ) {
                    BaseDroidRootApp(systemViewModel)
                }
            }
        }
    }
}

@Composable
fun BaseDroidRootApp(viewModel: BaseDroidViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAodMode = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isAodMode.value) {
            AlwaysOnDisplayScreen(viewModel) {
                isAodMode.value = false
            }
        } else {
            when (currentScreen) {
                Screen.BOOT -> BootScreen(viewModel)
                else -> MainDesktopEnvironment(viewModel) {
                    isAodMode.value = true
                }
            }
        }
    }
}

// --- MODULE 1: Boot Screen & Animation ---
@Composable
fun BootScreen(viewModel: BaseDroidViewModel) {
    val bootProgress by viewModel.bootProgress.collectAsState()
    val bootLogs by viewModel.bootLogs.collectAsState()
    val listState = rememberLazyListState()

    // Keep scrolling to the last boot log
    LaunchedEffect(bootLogs.size) {
        if (bootLogs.isNotEmpty()) {
            listState.animateScrollToItem(bootLogs.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .drawBehind {
                // Drawing cyber faint diagnostic grid lines
                val gridStep = 45.dp.toPx()
                var x = 0f
                val strokeColor = GridLineColor.copy(alpha = 0.15f)
                while (x < size.width) {
                    drawLine(strokeColor, Offset(x, 0f), Offset(x, size.height), 1f)
                    x += gridStep
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(strokeColor, Offset(0f, y), Offset(size.width, y), 1f)
                    y += gridStep
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AOSP KERNEL TARGET: SECURE_ARM64",
                    color = Color.Green.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    text = "RECOVERY: STABLE",
                    color = Color(0xFF00F0FF).copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            // Central Logo Area
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                // Outer glowing circle
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .border(1.5.dp, Color(0xFF00F0FF), CircleShape)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF00F0FF).copy(alpha = 0.08f),
                            radius = size.width / 2
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings, // Adjusted logo to standard Settings Icon
                        contentDescription = "Base Droid Logo",
                        tint = Color(0xFF00F0FF),
                        modifier = Modifier.size(72.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "B A S E   D R O I D",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "CUSTOM MOD COMPATIBLE PLATFORM",
                    color = Color(0xFF00F0FF).copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Real-time kernel booting logs output
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(1.dp, Color(0xFF14243B), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(bootLogs) { log ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(
                                text = ">>",
                                color = Color(0xFF00F0FF),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = log,
                                color = Color.White.copy(alpha = 0.9f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Lower progress bars
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { bootProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(CircleShape),
                    color = Color(0xFF00F0FF),
                    trackColor = Color(0xFF1F202E),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { viewModel.skipBoot() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF121420)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("skip_boot_button")
                ) {
                    Text(
                        text = "FAST INTERFACES UNLOCK",
                        color = Color(0xFF00F0FF),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- MODULE 2: Always On Display (AOD) Simulator ---
@Composable
fun AlwaysOnDisplayScreen(viewModel: BaseDroidViewModel, onExit: () -> Unit) {
    val aodStyle by viewModel.alwaysOnDisplayIndex.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    val currentTime = remember { mutableStateOf(timeFormat.format(Date())) }
    val currentDate = remember { mutableStateOf(dateFormat.format(Date())) }

    // Update AOD Clock
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime.value = timeFormat.format(Date())
            currentDate.value = dateFormat.format(Date())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                onClick = onExit,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            when (aodStyle) {
                0 -> { // Dot Clock Glow Style
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(190.dp)) {
                            drawCircle(
                                color = activeAccent.copy(alpha = 0.05f),
                                radius = size.width / 2,
                                style = Stroke(width = 3f)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTime.value,
                                color = activeAccent,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentDate.value,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                1 -> { // Neon Glowing Ring
                    Box(contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.size(240.dp)) {
                            drawCircle(
                                color = activeAccent,
                                radius = size.width / 2 - 10,
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentTime.value,
                                color = Color.White,
                                fontSize = 54.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentDate.value,
                                color = activeAccent,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.SansSerif,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
                2 -> { // Matrix Drop style
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ A O S P   L O C K ]",
                            color = Color.Green.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = currentTime.value,
                            color = Color.White,
                            fontSize = 62.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentDate.value,
                            color = Color.Green,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))

            // Simulated Battery telemetry in AOD
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning, // Battery charging substitute
                    contentDescription = "Battery charge",
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "84%  •  Base Droid OS Secure Hub",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "[ D o u b l e   T a p   T o   U n l o c k ]",
                color = Color.Gray.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 18.dp)
            )
        }
    }
}

// --- MODULE 3: Launcher Navigation Bar & Custom Grid Backdrop ---
@Composable
fun MainDesktopEnvironment(viewModel: BaseDroidViewModel, onPowerLock: () -> Unit) {
    val activeScreen by viewModel.currentScreen.collectAsState()
    val multitaskingMode by viewModel.multitaskingMode.collectAsState()

    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Scaffold(
        topBar = { DesktopTopBar(viewModel, onPowerLock) },
        bottomBar = { DesktopBottomBar(viewModel) },
        containerColor = CarbonDark,
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Drawing dark electronic circuit backdrops
                val gridStep = 45.dp.toPx()
                var x = 0f
                val strokeColor = GridLineColor.copy(alpha = 0.32f)
                while (x < size.width) {
                    drawLine(strokeColor, Offset(x, 0f), Offset(x, size.height), 1f)
                    x += gridStep
                }
                var y = 0f
                while (y < size.height) {
                    drawLine(strokeColor, Offset(0f, y), Offset(size.width, y), 1f)
                    y += gridStep
                }
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (multitaskingMode) {
                MultitaskingMode.STANDARD -> {
                    // Render standard single panel
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (activeScreen) {
                            Screen.DESKTOP -> DesktopWidgetLauncher(viewModel)
                            Screen.APPS_LIST -> AppDrawerCategoryScreen(viewModel)
                            Screen.SETTINGS -> CustomSettingsScreen(viewModel)
                            Screen.TERMINAL -> TerminalShellScreen(viewModel)
                            Screen.ASSISTANT -> AICompanionScreen(viewModel)
                            Screen.STORE -> AppStoreRepositoryScreen(viewModel)
                            Screen.FILES -> VirtualFilesWorkspaceScreen(viewModel)
                            Screen.SECURITY -> SecuritySandboxScreen(viewModel)
                            Screen.PERFORMANCE -> PerformanceOptimizerScreen(viewModel)
                            else -> DesktopWidgetLauncher(viewModel)
                        }
                    }
                }
                MultitaskingMode.SPLIT_SCREEN -> {
                    SplitScreenMultitaskingBody(viewModel)
                }
                MultitaskingMode.DESKTOP_MODE -> {
                    DesktopFloatingWindowsManager(viewModel)
                }
            }
        }
    }
}

// --- MODULE 4: Advanced Top Status Bar ---
@Composable
fun DesktopTopBar(viewModel: BaseDroidViewModel, onPowerLock: () -> Unit) {
    val batterySaver by viewModel.batterySaver.collectAsState()
    val gamingMode by viewModel.gamingMode.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val currentTime = remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime.value = timeFormat.format(Date())
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Status info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Base Logo",
                    tint = activeAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BASE DROID ROM",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                if (gamingMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFF007F).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFFF007F), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "GAME BOOST",
                            color = Color(0xFFFF007F),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (batterySaver) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFB300).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(0.5.dp, Color(0xFFFFB300), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "POWERSAVE",
                            color = Color(0xFFFFB300),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Central Smart clock
            Text(
                text = currentTime.value,
                color = activeAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Right hardware metrics togglers
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wifi",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = "Bluetooth",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Gps",
                    tint = Color.Green,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "84%",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = onPowerLock,
                    modifier = Modifier.size(24.dp).testTag("power_lock_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock, // Lock Screen clock activation
                        contentDescription = "Always On Display Screen",
                        tint = Color.Red,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// --- MODULE 5: Aesthetic Navigation Dock ---
@Composable
fun DesktopBottomBar(viewModel: BaseDroidViewModel) {
    val activeScreen by viewModel.currentScreen.collectAsState()
    val multitaskingMode by viewModel.multitaskingMode.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.Black.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
    ) {
        Column {
            // Task Manager / Mode selectors
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DESKTOP PIPELINES",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Standard State Tab
                    Text(
                        text = "STANDARD",
                        color = if (multitaskingMode == MultitaskingMode.STANDARD) activeAccent else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.setMultitaskingMode(MultitaskingMode.STANDARD) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Split screen toggle
                    Text(
                        text = "SPLIT",
                        color = if (multitaskingMode == MultitaskingMode.SPLIT_SCREEN) activeAccent else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.setMultitaskingMode(MultitaskingMode.SPLIT_SCREEN) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // Desktop Multi Window Toggle
                    Text(
                        text = "DECK_WINDOWS",
                        color = if (multitaskingMode == MultitaskingMode.DESKTOP_MODE) activeAccent else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.setMultitaskingMode(MultitaskingMode.DESKTOP_MODE) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Divider(color = GridLineColor.copy(alpha = 0.3f), thickness = 1.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val screens = listOf(
                    Triple(Screen.DESKTOP, Icons.Default.Home, "Desktop"),
                    Triple(Screen.APPS_LIST, Icons.Default.Menu, "Apps"),
                    Triple(Screen.TERMINAL, Icons.Default.Build, "Terminal"),
                    Triple(Screen.ASSISTANT, Icons.Default.Star, "AI Assistant"),
                    Triple(Screen.FILES, Icons.Default.Search, "Files"),
                    Triple(Screen.SECURITY, Icons.Default.Lock, "Security")
                )

                screens.forEach { (scr, icon, text) ->
                    val isSelected = activeScreen == scr && multitaskingMode == MultitaskingMode.STANDARD
                    IconButton(
                        onClick = {
                            viewModel.setMultitaskingMode(MultitaskingMode.STANDARD)
                            viewModel.navigateTo(scr)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_button_${text.lowercase().replace(" ", "_")}")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = icon,
                                contentDescription = text,
                                tint = if (isSelected) activeAccent else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = text,
                                color = if (isSelected) activeAccent else Color.White.copy(alpha = 0.5f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.SansSerif,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 6: Desktop Launch and Quick Widgets ---
@Composable
fun DesktopWidgetLauncher(viewModel: BaseDroidViewModel) {
    val usedMemory by viewModel.usedMemoryMb.collectAsState()
    val cpuTemp by viewModel.cpuTemp.collectAsState()
    val batteryLevel by viewModel.batteryLevel.collectAsState()
    val batterySaver by viewModel.batterySaver.collectAsState()
    val gamingMode by viewModel.gamingMode.collectAsState()
    val performanceGovernor by viewModel.performanceGovernor.collectAsState()

    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome telemetry hero card banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "AOSP BASE DROID SUB-STATION",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Secure Kernel Terminal Active",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hardware Abstraction Engine: Qualcomm Elite Virtio. Refresh Rates synced at fluid 120Hz.",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Row of circular metrics gauges using Canvas drawings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Memory Gauge (RAM)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RAM LOAD",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(65.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track background
                            drawArc(
                                color = Color.White.copy(alpha = 0.05f),
                                startAngle = -225f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Filled arc
                            val ramRatio = usedMemory.toFloat() / 8192f
                            drawArc(
                                color = activeAccent,
                                startAngle = -225f,
                                sweepAngle = 270f * ramRatio,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", usedMemory.toFloat() / 1024f)}G",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "of 8G",
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CLEAN CACHES",
                        color = activeAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.optimizeMemory() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // CPU Gauge
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CPU TEMP",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(65.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = Color.White.copy(alpha = 0.05f),
                                startAngle = -225f,
                                sweepAngle = 270f,
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                            val cpuRatio = (cpuTemp - 20f) / 60f
                            drawArc(
                                color = if (cpuTemp > 46f) Color.Red else Color.Green,
                                startAngle = -225f,
                                sweepAngle = (270f * cpuRatio).coerceIn(0f, 270f),
                                useCenter = false,
                                style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", cpuTemp)}°C",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = performanceGovernor,
                                color = Color.Gray,
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "TUNE STATE",
                        color = Color.Green,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(Screen.PERFORMANCE) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Interactive Toggles widget section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AOSP SYSTEM CONTROL PANEL",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                val toggles = listOf(
                    Triple(viewModel.wiFiEnabled.collectAsState(), Icons.Default.Wifi, "Wi-Fi"),
                    Triple(viewModel.bluetoothEnabled.collectAsState(), Icons.Default.Bluetooth, "Bluetooth"),
                    Triple(viewModel.nfcEnabled.collectAsState(), Icons.Default.PlayArrow, "NFC"), // Changed to PlayArrow Core Icon
                    Triple(viewModel.hotspotEnabled.collectAsState(), Icons.Default.Share, "Hotspot"), // Changed to Share Core Icon
                    Triple(viewModel.screenCasting.collectAsState(), Icons.Default.Share, "Cast Scan") // Changed to Share Core Icon
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toggles) { (flowState, icon, label) ->
                        val active = flowState.value
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) activeAccent.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (active) activeAccent else GridLineColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                .clickable {
                                    when (label) {
                                        "Wi-Fi" -> viewModel.toggleWiFi()
                                        "Bluetooth" -> viewModel.toggleBluetooth()
                                        "NFC" -> viewModel.toggleNfc()
                                        "Hotspot" -> viewModel.toggleHotspot()
                                        "Cast Scan" -> viewModel.toggleScreenCasting()
                                    }
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (active) activeAccent else Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    color = if (active) Color.White else Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Quick launch panel linking other screens
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "AOSP CONSOLE SHORTCUTS",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.SETTINGS) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceLighter),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.2f))
                    ) {
                        Text("SYSTEM UI", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = { viewModel.navigateTo(Screen.STORE) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceLighter),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        border = BorderStroke(1.dp, activeAccent.copy(alpha = 0.2f))
                    ) {
                        Text("PKG STORE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// --- MODULE 7: Smart AI Workspace Terminal ---
@Composable
fun AICompanionScreen(viewModel: BaseDroidViewModel) {
    val messages by viewModel.aiMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // AI Profile Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceLighter)
                .border(1.dp, activeAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(activeAccent.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, activeAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "AI",
                        tint = activeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "BASE DROID SYSTEM GPT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "AOSP Core Model: Gemini 3.5 Flash",
                        color = Color.Green,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chats workspace container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (msg.isUser) 12.dp else 0.dp,
                                        bottomEnd = if (msg.isUser) 0.dp else 12.dp
                                    )
                                )
                                .background(if (msg.isUser) activeAccent.copy(alpha = 0.18f) else DarkSurface)
                                .border(1.dp, if (msg.isUser) activeAccent.copy(alpha = 0.5f) else GridLineColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = if (msg.isUser) Color.White else Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontFamily = if (msg.isUser) FontFamily.SansSerif else FontFamily.Monospace
                            )
                        }
                    }
                }

                if (isThinking) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = activeAccent,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Querying local model pipelines & variables...",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chat prompt text-editor input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                placeholder = { Text("Query AI on ROM, code, optimizer tuning...", color = Color.Gray, fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = activeAccent,
                    unfocusedBorderColor = GridLineColor.copy(alpha = 0.6f),
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                })
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.sendChatMessage(textInput)
                        textInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(activeAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, activeAccent, RoundedCornerShape(12.dp))
                    .testTag("chat_send_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send prompt",
                    tint = activeAccent
                )
            }
        }
    }
}

// --- MODULE 8: AOSP Shell Terminal Assistant ---
@Composable
fun TerminalShellScreen(viewModel: BaseDroidViewModel) {
    val history by viewModel.terminalHistory.collectAsState()
    val scrollState = rememberScrollState()

    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    var cmdInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Automatically scroll core log history down
    LaunchedEffect(history.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Quick tools shortcuts for terminal commands
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val shortcuts = listOf("neofetch", "clean-ram", "cpu-load", "storage audit", "ai-diagnose")
            shortcuts.forEach { cmd ->
                Button(
                    onClick = { viewModel.executeTerminalCommand(cmd) },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    border = BorderStroke(0.5.dp, activeAccent.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = cmd.uppercase(),
                        color = activeAccent,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Output Shell Logs
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
            ) {
                history.forEach { line ->
                    val color = when (line.type) {
                        LineType.INPUT -> Color.White
                        LineType.ACCENT -> activeAccent
                        LineType.INFO -> Color.Gray
                        LineType.SUCCESS -> Color.Green
                        LineType.ERROR -> Color.Red
                    }
                    Text(
                        text = line.text,
                        color = color,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prompt Console input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "root@basedroid:~# ",
                color = activeAccent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(4.dp))

            BasicTextField(
                value = cmdInput,
                onValueChange = { cmdInput = it },
                textStyle = TextStyle(
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminal_input_field"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (cmdInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(cmdInput)
                        cmdInput = ""
                        keyboardController?.hide()
                    }
                })
            )

            IconButton(
                onClick = {
                    if (cmdInput.isNotBlank()) {
                        viewModel.executeTerminalCommand(cmdInput)
                        cmdInput = ""
                        keyboardController?.hide()
                    }
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Execute CLI Command",
                    tint = activeAccent
                )
            }
        }
    }
}

// --- MODULE 9: Open-Source App Repository Store ---
@Composable
fun AppStoreRepositoryScreen(viewModel: BaseDroidViewModel) {
    val storeApps by viewModel.storeApps.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Store Header Banner
        Text(
            text = "AOSP OFFICIAL STORE",
            color = activeAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(storeApps) { app ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(activeAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = app.category,
                                        color = activeAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.description,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // App Store Install trigger Button
                        when (app.installState) {
                            InstallState.NOT_INSTALLED -> {
                                Button(
                                    onClick = { viewModel.installApp(app.packageName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("INSTALL", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            InstallState.INSTALLING -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        color = activeAccent,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DOWNLOADING...", color = activeAccent, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                            InstallState.INSTALLED -> {
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, Color.Green, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("PAIRED", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            InstallState.UPDATE_AVAILABLE -> {
                                Button(
                                    onClick = { viewModel.installApp(app.packageName) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("UPDATE", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 10: Virtual Sandboxed File Explorer + Source Code Editor ---
@Composable
fun VirtualFilesWorkspaceScreen(viewModel: BaseDroidViewModel) {
    val files by viewModel.virtualFiles.collectAsState()
    val editingFile by viewModel.editingFile.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    // If a user has clicked to edit a code file, we present the terminal code editor!
    if (editingFile != null) {
        SourceCodeEditorWorkspace(viewModel)
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AOSP FILE EXPLORER WORKSPACE",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Trigger to create mock code files directly
                Button(
                    onClick = {
                        viewModel.createVirtualFile("new_script.py", "/sdcard/scripts", "import logging\nprint('Base Droid initialized')")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceLighter),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("+ python file", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Disk allocation donuts metrics
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.size(60.dp)) {
                        // Drawing beautiful distribution ring charts
                        drawArc(Color.Green, -90f, 160f, false, style = Stroke(width = 6.dp.toPx()))
                        drawArc(activeAccent, 70f, 100f, false, style = Stroke(width = 6.dp.toPx()))
                        drawArc(Color.Gray.copy(alpha = 0.5f), 170f, 100f, false, style = Stroke(width = 6.dp.toPx()))
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column {
                        Text("WORKSPACE AUDIT DATA", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("/sdcard : 0.9 / 16.0 GB total cached", color = Color.Gray, fontSize = 11.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color.Green, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Media: 6G", color = Color.Gray, fontSize = 8.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(activeAccent, CircleShape))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Scripts: 1G", color = Color.Gray, fontSize = 8.sp)
                            }
                        }
                    }
                }
            }

            // Scrollable List of virtual documents
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files) { file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, GridLineColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build, // Guaranteed Build/Script icon
                                contentDescription = "Draft File",
                                tint = activeAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = file.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${file.path} • ${file.sizeKb} KB",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Code editing and deleting actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.startEditingFile(file) }) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit in Web Editor Simulator", tint = Color.Green, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { viewModel.deleteVirtualFile(file.path) }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Trash documents", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Code source editor simulator matching developers requirements
@Composable
fun SourceCodeEditorWorkspace(viewModel: BaseDroidViewModel) {
    val file by viewModel.editingFile.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    if (file == null) return

    var editsBuffer by remember { mutableStateOf(file!!.content) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AOSP WORKSPACE EDITOR",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Target editing: ${file!!.name}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { viewModel.cancelEditingFile() }
                ) {
                    Text("DISCARD", color = Color.Red, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.saveEditingFile(editsBuffer) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Text("SAVE FILE", color = Color.Black, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Row of quick template helpers for quick script injection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val helpers = listOf("print()", "sys.argv", "echo", "chmod +x", "AOSP_BOOT")
            helpers.forEach { text ->
                Box(
                    modifier = Modifier
                        .background(DarkSurfaceLighter, RoundedCornerShape(4.dp))
                        .clickable { editsBuffer += " $text" }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text, color = activeAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Code Area
        OutlinedTextField(
            value = editsBuffer,
            onValueChange = { editsBuffer = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("code_editor_field"),
            textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = activeAccent,
                unfocusedBorderColor = GridLineColor.copy(alpha = 0.5f),
                focusedContainerColor = Color.Black.copy(alpha = 0.7f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.7f)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
    }
}

// --- MODULE 11: Permissions and Threat Sandboxed Analyzer ---
@Composable
fun SecuritySandboxScreen(viewModel: BaseDroidViewModel) {
    val isScanning by viewModel.isScanningMalware.collectAsState()
    val progress by viewModel.scanProgress.collectAsState()
    val scanningFile by viewModel.scanningFileName.collectAsState()
    val threats by viewModel.threatsFound.collectAsState()
    val checkedFiles by viewModel.malwareFilesChecked.collectAsState()

    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Text(
            text = "SECURITY ENGINE SHIELD",
            color = activeAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // Scanner Module Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Automated Malware Sweeper", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Active status: ${if (isScanning) "INSPECTING..." else "STANDBY"}", color = Color.Gray, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }

                    if (!isScanning) {
                        Button(
                            onClick = { viewModel.runMalwareScan() },
                            colors = ButtonDefaults.buttonColors(containerColor = activeAccent),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("RUN SWEEP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (isScanning || progress > 0f) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(CircleShape),
                        color = if (threats > 0) Color.Red else activeAccent,
                        trackColor = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Inspecting: $scanningFile",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Analyzed: $checkedFiles folders", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            text = "Threats logged: $threats",
                            color = if (threats > 0) Color.Red else Color.Green,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Permission auditing sandbox
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Permit Sandbox Controllers", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val permissionsList = listOf(
                    "ALLOW_RAW_CAMERA_FRAME_FEEDS" to true,
                    "BYPASS_UNTRUSTED_APK_SIGNATURES" to false,
                    "ENABLE_SANDBOXED_STORAGE_AES_VAULTS" to true,
                    "ISOLATE_DEV_TERMINAL_ENV" to true
                )

                permissionsList.forEach { (perm, status) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perm,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (status) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (status) "GRANTED" else "BLOCKED",
                                color = if (status) Color.Green else Color.Red,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 12: Customizable Settings & Custom ROM Theme Engine ---
@Composable
fun CustomSettingsScreen(viewModel: BaseDroidViewModel) {
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val currentWallpaperIndex by viewModel.currentWallpaperIndex.collectAsState()
    val clockStyleIndex by viewModel.clockStyleIndex.collectAsState()
    val aodStyleIndex by viewModel.alwaysOnDisplayIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme Engine Title
        Text(
            text = "CUSTOM ROM PALETTES",
            color = activeAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // 1. Accent color Customizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = "AOSP System Accent Tuning",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    viewModel.accentColors.forEachIndexed { idx, color ->
                        val isSel = idx == accentColorIndex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    border = BorderStroke(
                                        width = if (isSel) 3.dp else 0.dp,
                                        color = if (isSel) Color.White else Color.Transparent
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { viewModel.selectAccentColor(idx) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Current color: ${viewModel.colorNames[accentColorIndex]} Theme",
                    color = activeAccent,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // 2. Lock screen clocks preferences
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text("Select Lock Screen Clocks Mode", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val optionClocks = listOf("Dot Ambient", "Glowing Ring", "Minimalist Matrix")
                optionClocks.forEachIndexed { idx, design ->
                    val isSel = idx == clockStyleIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectClockStyle(idx) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = design, color = if (isSel) activeAccent else Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        if (isSel) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Checked", tint = activeAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 3. Always On Display designs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column {
                Text("Always-On Display Configurator", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                val customAods = listOf("Cypher Clock Ambient", "Neon Astro Loop", "Data Registers Rain")
                customAods.forEachIndexed { idx, item ->
                    val isSel = idx == aodStyleIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectAlwaysOnDisplay(idx) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = item, color = if (isSel) activeAccent else Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        if (isSel) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = activeAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 13: Performance Governor Tuning Dashboard ---
@Composable
fun PerformanceOptimizerScreen(viewModel: BaseDroidViewModel) {
    val governor by viewModel.performanceGovernor.collectAsState()
    val gamingActive by viewModel.gamingMode.collectAsState()
    val powerSaverActive by viewModel.batterySaver.collectAsState()

    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "AOSP KERNEL DEVIATION METRIC",
            color = activeAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )

        // Circular live telemetry and optimization triggers
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, GridLineColor.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Live Subsystem governor clock states",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                val listGov = listOf("PowerSave", "Balanced", "Performance")
                listGov.forEach { mode ->
                    val isSel = governor == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setPerformanceGovernor(mode) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mode.uppercase(),
                            color = if (isSel) activeAccent else Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                        if (isSel) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Matched", tint = activeAccent, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Advanced profiles configurations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Gaming Mode Switch Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (gamingActive) Color(0xFFFF007F).copy(alpha = 0.12f) else DarkSurface)
                    .border(1.dp, if (gamingActive) Color(0xFFFF007F) else GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleGamingMode() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Games", tint = if (gamingActive) Color(0xFFFF007F) else Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Gaming Boost", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(if (gamingActive) "READY" else "OFF", color = if (gamingActive) Color(0xFFFF007F) else Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Power Saver Switch Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (powerSaverActive) Color(0xFFFFB300).copy(alpha = 0.12f) else DarkSurface)
                    .border(1.dp, if (powerSaverActive) Color(0xFFFFB300) else GridLineColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { viewModel.toggleBatterySaver() }
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = "Battery saver", tint = if (powerSaverActive) Color(0xFFFFB300) else Color.White)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Battery Saver", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(if (powerSaverActive) "ACTIVE" else "OFF", color = if (powerSaverActive) Color(0xFFFFB300) else Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

// --- MODULE 14: Categories Application Drawer ---
@Composable
fun AppDrawerCategoryScreen(viewModel: BaseDroidViewModel) {
    val storeApps by viewModel.storeApps.collectAsState()
    val installedApps = storeApps.filter { it.installState == InstallState.INSTALLED }
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    val categories = installedApps.groupBy { it.category }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "COMPUTED SYSTEM APPLICATIONS",
            color = activeAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                Text("No app modules installed or functional inside core OS workspace.", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            categories.forEach { (cat, list) ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = cat.uppercase(),
                        color = activeAccent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.heightIn(max = 130.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(list) { app ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, GridLineColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        // Open standard app directly 
                                        val scr = when (app.name) {
                                            "Base Terminal Core" -> Screen.TERMINAL
                                            "Base Files Pro" -> Screen.FILES
                                            "Hyper AI Optimizer" -> Screen.ASSISTANT
                                            else -> Screen.DESKTOP
                                        }
                                        viewModel.navigateTo(scr)
                                    }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                    Icon(
                                        imageVector = when (app.name) {
                                            "Base Terminal Core" -> Icons.Default.Build
                                            "Base Files Pro" -> Icons.Default.Search
                                            "Hyper AI Optimizer" -> Icons.Default.Star
                                            else -> Icons.Default.Settings
                                        },
                                        contentDescription = app.name,
                                        tint = activeAccent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = app.name.replace("Base ", "").replace(" Pro", ""),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- MODULE 15: Split-Screen Dual Multitasking Simulated Views ---
@Composable
fun SplitScreenMultitaskingBody(viewModel: BaseDroidViewModel) {
    val topApp by viewModel.splitScreenTop.collectAsState()
    val bottomApp by viewModel.splitScreenBottom.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Column(modifier = Modifier.fillMaxSize()) {
        // TOP Split Panel View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, activeAccent.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f))
                    .align(Alignment.TopCenter)
                    .padding(vertical = 2.dp)
            ) {
                Text("[ TOP DUAL CORE SCREEN: ${topApp.name} ]", color = activeAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (topApp) {
                    Screen.PERFORMANCE -> PerformanceOptimizerScreen(viewModel)
                    Screen.TERMINAL -> TerminalShellScreen(viewModel)
                    Screen.ASSISTANT -> AICompanionScreen(viewModel)
                    Screen.FILES -> VirtualFilesWorkspaceScreen(viewModel)
                    else -> PerformanceOptimizerScreen(viewModel)
                }
            }
        }

        // BOTTOM Split Panel View
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, activeAccent.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f))
                    .align(Alignment.TopCenter)
                    .padding(vertical = 2.dp)
            ) {
                Text("[ BOTTOM DUAL CORE SCREEN: ${bottomApp.name} ]", color = activeAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Box(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                when (bottomApp) {
                    Screen.PERFORMANCE -> PerformanceOptimizerScreen(viewModel)
                    Screen.TERMINAL -> TerminalShellScreen(viewModel)
                    Screen.ASSISTANT -> AICompanionScreen(viewModel)
                    Screen.FILES -> VirtualFilesWorkspaceScreen(viewModel)
                    else -> TerminalShellScreen(viewModel)
                }
            }
        }
    }
}

// --- MODULE 16: Dynamic Draggable Floating Windows Desktop Simulation ---
@Composable
fun DesktopFloatingWindowsManager(viewModel: BaseDroidViewModel) {
    val windows by viewModel.floatingWindows.collectAsState()
    val accentColorIndex by viewModel.accentColorIndex.collectAsState()
    val activeAccent = viewModel.accentColors[accentColorIndex]

    Box(modifier = Modifier.fillMaxSize()) {
        if (windows.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No window screens loaded in AOSP Desktop frame.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.openAppInFloatingWindow("Base Terminal", Screen.TERMINAL) },
                        colors = ButtonDefaults.buttonColors(containerColor = activeAccent)
                    ) {
                        Text("LAUNCH TERMINAL WINDOW", color = Color.Black)
                    }
                }
            }
        } else {
            windows.forEach { win ->
                if (!win.isMinimized) {
                    key(win.id) {
                        FloatingCardWindowWrapper(
                            window = win,
                            activeAccent = activeAccent,
                            onClose = { viewModel.closeFloatingWindow(win.id) },
                            onMove = { dX, dY -> viewModel.updateFloatingWindowOffset(win.id, dX, dY) },
                            onFocus = { viewModel.focusFloatingWindow(win.id) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(CarbonDark)
                            ) {
                                when (win.screen) {
                                    Screen.TERMINAL -> TerminalShellScreen(viewModel)
                                    Screen.PERFORMANCE -> PerformanceOptimizerScreen(viewModel)
                                    Screen.ASSISTANT -> AICompanionScreen(viewModel)
                                    Screen.FILES -> VirtualFilesWorkspaceScreen(viewModel)
                                    else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Generic System Stream", color = Color.White) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingCardWindowWrapper(
    window: FloatingWindow,
    activeAccent: Color,
    onClose: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onFocus: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .offset { IntOffset(window.offsetX.toInt(), window.offsetY.toInt()) }
            .size(window.widthDp.dp, window.heightDp.dp)
            .border(1.5.dp, activeAccent, RoundedCornerShape(12.dp))
            .clickable(onClick = onFocus), // Bring to focus
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag Header Bar of Window
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceLighter)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { onFocus() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onMove(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings, // Adjusted logo to standard Settings Icon
                        contentDescription = "Active process",
                        tint = activeAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = window.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Minimize 
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.Yellow, CircleShape)
                            .clickable { onClose() }
                    )
                    // Close red button
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.Red, CircleShape)
                            .clickable { onClose() }
                    )
                }
            }

            // Client Windows body content
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}
