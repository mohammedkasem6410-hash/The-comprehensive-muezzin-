package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.SuhoorAlertMode
import com.example.receiver.PrayerAlarmReceiver
import com.example.service.PrayerAlarmScheduler
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import android.content.Intent

@Composable
fun SuhoorScreen(
    settings: AppSettings,
    isArabic: Boolean,
    onUpdateSuhoor: (
        enabled: Boolean,
        mode: SuhoorAlertMode,
        minutesBeforeFajr: Int,
        fixedHour: Int,
        fixedMinute: Int,
        mediaUri: String?
    ) -> Unit
) {
    val context = LocalContext.current

    var enabled by remember(settings.suhoorEnabled) { mutableStateOf(settings.suhoorEnabled) }
    var mode by remember(settings.suhoorMode) { mutableStateOf(settings.suhoorMode) }
    var minutesBeforeFajr by remember(settings.suhoorMinutesBeforeFajr) { mutableStateOf(settings.suhoorMinutesBeforeFajr) }
    var fixedHour by remember(settings.suhoorFixedHour) { mutableStateOf(settings.suhoorFixedHour) }
    var fixedMinute by remember(settings.suhoorFixedMinute) { mutableStateOf(settings.suhoorFixedMinute) }
    var mediaUri by remember(settings.suhoorMediaUri) { mutableStateOf(settings.suhoorMediaUri) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            mediaUri = uri.toString()
            onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, uri.toString())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Hero Ramadan Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("suhoor_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF3B1D0E),
                                    Color(0xFF5D2E14),
                                    Color(0xFF281308)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GoldPrimary.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NightsStay,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isArabic) "إعدادات المسحراتي (السحور)" else "Musaharati & Suhoor Alerts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isArabic)
                                "«اصحى يا نايم وحّد الدايم.. السحور بركة»\nتنبيه مخصص لإيقاظك قبل الفجر بنغمات وفيديوهات مسحراتي تراثية"
                            else
                                "Dedicated Suhoor reminder to awaken you before Fajr with custom audio or video clips",
                            fontSize = 13.sp,
                            color = Color(0xFFFDE68A).copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Enable / Disable Switch
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("suhoor_enable_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تفعيل تنبيه المسحراتي" else "Enable Musaharati Alert",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "إصدار تنبيه صوتي ومرئي قبل الفجر" else "Sound and visual alert before Fajr",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = enabled,
                        onCheckedChange = { isChecked ->
                            enabled = isChecked
                            onUpdateSuhoor(isChecked, mode, minutesBeforeFajr, fixedHour, fixedMinute, mediaUri)
                        },
                        modifier = Modifier.testTag("suhoor_switch")
                    )
                }
            }
        }

        if (enabled) {
            // Mode Selection: Before Fajr or Fixed Time
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("suhoor_mode_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = if (isArabic) "طريقة تحديد وقت التنبيه:" else "Alert Timing Method:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Option 1: Before Fajr
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (mode == SuhoorAlertMode.BEFORE_FAJR)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    mode = SuhoorAlertMode.BEFORE_FAJR
                                    onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, mediaUri)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == SuhoorAlertMode.BEFORE_FAJR,
                                onClick = {
                                    mode = SuhoorAlertMode.BEFORE_FAJR
                                    onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, mediaUri)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "قبل أذان الفجر بمدة زمنية" else "Relative duration before Fajr",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isArabic) "يتغير التوقيت تلقائياً بحسب موعد الفجر" else "Automatically adjusts with Fajr time",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (mode == SuhoorAlertMode.BEFORE_FAJR) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(30, 45, 60, 90).forEach { mins ->
                                    val isSelected = minutesBeforeFajr == mins
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            minutesBeforeFajr = mins
                                            onUpdateSuhoor(enabled, mode, mins, fixedHour, fixedMinute, mediaUri)
                                        },
                                        label = {
                                            Text(if (isArabic) "$mins د" else "$mins m")
                                        },
                                        modifier = Modifier.testTag("chip_suhoor_$mins")
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        // Option 2: Fixed Time
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (mode == SuhoorAlertMode.FIXED_TIME)
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    mode = SuhoorAlertMode.FIXED_TIME
                                    onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, mediaUri)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = mode == SuhoorAlertMode.FIXED_TIME,
                                onClick = {
                                    mode = SuhoorAlertMode.FIXED_TIME
                                    onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, mediaUri)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "في توقيت ثابت يومياً" else "Fixed daily time",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isArabic) "مثال: 03:30 صباحاً" else "e.g. 03:30 AM",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (mode == SuhoorAlertMode.FIXED_TIME) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf(Pair(3, 0), Pair(3, 30), Pair(4, 0)).forEach { (h, m) ->
                                    val isSelected = fixedHour == h && fixedMinute == m
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            fixedHour = h
                                            fixedMinute = m
                                            onUpdateSuhoor(enabled, mode, minutesBeforeFajr, h, m, mediaUri)
                                        },
                                        label = {
                                            Text(String.format(java.util.Locale.ENGLISH, "%02d:%02d ص", h, m))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Media / Video / Audio Selector
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("suhoor_media_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isArabic) "نغمة أو فيديو المسحراتي:" else "Musaharati Clip (Audio/Video):",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (mediaUri == null) {
                                    if (isArabic) "المسحراتي التراثي المدمج (افتراضي)" else "Built-in Heritage Chant (Default)"
                                } else {
                                    if (isArabic) "مقطع مخصص من الجهاز" else "Custom Device Clip"
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { mediaPickerLauncher.launch("video/*,audio/*") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("pick_suhoor_media_button")
                            ) {
                                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isArabic) "اختيار من الجهاز" else "Pick File")
                            }
                        }

                        if (mediaUri != null) {
                            TextButton(
                                onClick = {
                                    mediaUri = null
                                    onUpdateSuhoor(enabled, mode, minutesBeforeFajr, fixedHour, fixedMinute, null)
                                }
                            ) {
                                Text(if (isArabic) "استعادة النغمة الافتراضية" else "Reset to default")
                            }
                        }
                    }
                }
            }

            // Test Musaharati Alert Button
            item {
                Button(
                    onClick = {
                        val testIntent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                            action = PrayerAlarmScheduler.ACTION_SUHOOR_ALERT
                        }
                        context.sendBroadcast(testIntent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("test_suhoor_alert_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDark)
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "تجربة تنبيه المسحراتي الآن" else "Test Musaharati Alert Now",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
