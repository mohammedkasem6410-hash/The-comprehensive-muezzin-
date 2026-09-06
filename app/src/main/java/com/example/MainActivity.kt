package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.FullscreenAdhanActivity
import com.example.ui.PreAlertActivity
import com.example.ui.screens.LocationDialog
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SuhoorScreen
import com.example.ui.theme.BlueAccentLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekDarkHeader
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.PrayerViewModel

class MainActivity : ComponentActivity() {

    private val prayerViewModel: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by prayerViewModel.settings.collectAsState()
            val prayerData by prayerViewModel.prayerData.collectAsState()
            val isLocating by prayerViewModel.isLocating.collectAsState()
            val message by prayerViewModel.message.collectAsState()

            val layoutDirection = if (settings.isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    MainAppContent(
                        viewModel = prayerViewModel,
                        settings = settings,
                        prayerData = prayerData,
                        isLocating = isLocating,
                        message = message,
                        onLaunchAdhanVideo = {
                            val intent = Intent(this@MainActivity, FullscreenAdhanActivity::class.java).apply {
                                putExtra(FullscreenAdhanActivity.EXTRA_PRAYER_NAME, prayerData.nextPrayer.getDisplayName(settings.isArabic, prayerData.isFriday))
                            }
                            startActivity(intent)
                        },
                        onLaunchPreAlert = {
                            val intent = Intent(this@MainActivity, PreAlertActivity::class.java).apply {
                                putExtra(PreAlertActivity.EXTRA_PRAYER_NAME, prayerData.nextPrayer.getDisplayName(settings.isArabic, prayerData.isFriday))
                                putExtra(PreAlertActivity.EXTRA_REMAINING_MINUTES, settings.preAlertMinutes)
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(
    viewModel: PrayerViewModel,
    settings: com.example.model.AppSettings,
    prayerData: com.example.model.PrayerTimesData,
    isLocating: Boolean,
    message: String?,
    onLaunchAdhanVideo: () -> Unit,
    onLaunchPreAlert: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLocationDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Request Runtime Permissions: Notifications (Android 13+) and Location
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = (if (settings.isArabic) prayerData.cityNameAr else prayerData.cityName).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = SlateTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (settings.isArabic) prayerData.hijriDateFormattedAr else prayerData.hijriDateFormattedEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLanguage() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1E293B), CircleShape)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("appbar_language_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekDarkHeader
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0F172A).copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(width = 1.dp, color = Color(0xFF1E293B))
                    .testTag("bottom_nav_bar")
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlueAccentLight,
                    selectedTextColor = BlueAccentLight,
                    indicatorColor = Color(0x333B82F6),
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                )

                // Tab 0: Prayer Times
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(Icons.Default.Schedule, contentDescription = null)
                    },
                    label = {
                        Text(if (settings.isArabic) "الرئيسية" else "Prayers", fontSize = 11.sp)
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_tab_prayers")
                )

                // Tab 1: Suhoor / Ramadan
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(Icons.Default.NightsStay, contentDescription = null)
                    },
                    label = {
                        Text(if (settings.isArabic) "المسحراتي" else "Suhoor", fontSize = 11.sp)
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_tab_suhoor")
                )

                // Tab 2: Settings
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    },
                    label = {
                        Text(if (settings.isArabic) "الإعدادات" else "Settings", fontSize = 11.sp)
                    },
                    colors = navColors,
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> PrayerTimesScreen(
                    prayerData = prayerData,
                    isArabic = settings.isArabic,
                    onOpenLocationDialog = { showLocationDialog = true },
                    onTestAdhanVideo = onLaunchAdhanVideo,
                    onTestPreAlert = onLaunchPreAlert
                )
                1 -> SuhoorScreen(
                    settings = settings,
                    isArabic = settings.isArabic,
                    onUpdateSuhoor = { enabled, mode, mins, h, m, media ->
                        viewModel.setSuhoorSettings(enabled, mode, mins, h, m, media)
                    }
                )
                2 -> SettingsScreen(
                    settings = settings,
                    isArabic = settings.isArabic,
                    onSetMethod = { viewModel.setCalculationMethod(it) },
                    onSetJuristic = { viewModel.setJuristicMethod(it) },
                    onSetPreAlert = { enabled, mins -> viewModel.setPreAlert(enabled, mins) },
                    onTogglePersistentNotification = { viewModel.togglePersistentNotification(it) },
                    onSetAdhanVideo = { viewModel.setAdhanVideoUri(it) },
                    onToggleLanguage = { viewModel.toggleLanguage() },
                    onTestAdhanVideo = onLaunchAdhanVideo,
                    onTestPreAlert = onLaunchPreAlert
                )
            }
        }

        if (showLocationDialog) {
            LocationDialog(
                isArabic = settings.isArabic,
                selectedCityIndex = settings.selectedCityIndex,
                isLocating = isLocating,
                onSelectCity = { city ->
                    viewModel.selectPresetCity(city)
                },
                onRequestGps = {
                    viewModel.requestGpsLocation()
                },
                onDismiss = { showLocationDialog = false }
            )
        }
    }
}
