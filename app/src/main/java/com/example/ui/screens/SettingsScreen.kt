package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppSettings
import com.example.model.CalculationMethod
import com.example.model.JuristicMethod

@Composable
fun SettingsScreen(
    settings: AppSettings,
    isArabic: Boolean,
    onSetMethod: (CalculationMethod) -> Unit,
    onSetJuristic: (JuristicMethod) -> Unit,
    onSetPreAlert: (Boolean, Int) -> Unit,
    onTogglePersistentNotification: (Boolean) -> Unit,
    onSetAdhanVideo: (String?) -> Unit,
    onToggleLanguage: () -> Unit,
    onTestAdhanVideo: () -> Unit,
    onTestPreAlert: () -> Unit
) {
    var showMethodDialog by remember { mutableStateOf(false) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onSetAdhanVideo(uri.toString())
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Calculation Method Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("method_settings_card"),
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
                        text = if (isArabic) "طريقة حساب مواقيت الصلاة" else "Prayer Calculation Method",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedButton(
                        onClick = { showMethodDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("select_method_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = settings.calculationMethod.getName(isArabic),
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }

                    // Asr juristic method
                    Text(
                        text = if (isArabic) "مذهب حساب صلاة العصر:" else "Asr Juristic Method:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JuristicMethod.entries.forEach { juristic ->
                            val isSelected = settings.juristicMethod == juristic
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSetJuristic(juristic) },
                                label = { Text(juristic.getName(isArabic)) },
                                modifier = Modifier.testTag("juristic_chip_${juristic.name}")
                            )
                        }
                    }
                }
            }
        }

        // Pre-Alert Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pre_alert_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "التنبيه المسبق قبل الأذان" else "Pre-Adhan Reminder",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) "عرض شاشة: «يتبقى على صلاة [اسم الصلاة]»" else "Displays pre-alert reminder screen",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = settings.preAlertEnabled,
                            onCheckedChange = { onSetPreAlert(it, settings.preAlertMinutes) },
                            modifier = Modifier.testTag("pre_alert_switch")
                        )
                    }

                    if (settings.preAlertEnabled) {
                        Text(
                            text = if (isArabic) "التنبيه قبل الصلاة بـ:" else "Alert before prayer by:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 20, 30).forEach { mins ->
                                val isSelected = settings.preAlertMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetPreAlert(true, mins) },
                                    label = { Text(if (isArabic) "$mins دقائق" else "$mins min") },
                                    modifier = Modifier.testTag("pre_alert_chip_$mins")
                                )
                            }
                        }

                        Button(
                            onClick = onTestPreAlert,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_pre_alert_screen_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isArabic) "تجربة شاشة التنبيه المسبق الآن" else "Test Pre-Alert Screen Now")
                        }
                    }
                }
            }
        }

        // Fullscreen Video Adhan Settings Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("video_adhan_settings_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isArabic) "فيديو الأذان ملء الشاشة" else "Fullscreen Video Adhan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = if (isArabic)
                            "يعرض فيديو الأذان بدون نصوص نهائياً، مع دعم الإيقاف بضغطتين أو أزرار الصوت."
                        else
                            "Plays fullscreen video without text, dismissible via double tap or volume keys.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { videoPickerLauncher.launch("video/*") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_video_button")
                        ) {
                            Icon(Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "اختيار فيديو" else "Pick Video")
                        }

                        Button(
                            onClick = onTestAdhanVideo,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_fullscreen_video_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "تجربة الفيديو" else "Test Video")
                        }
                    }

                    if (settings.adhanVideoUri != null) {
                        TextButton(
                            onClick = { onSetAdhanVideo(null) }
                        ) {
                            Text(if (isArabic) "استعادة الفيديو الافتراضي" else "Reset to default video")
                        }
                    }
                }
            }
        }

        // Persistent Notification Card (Ongoing)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("persistent_notification_card"),
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
                            text = if (isArabic) "الإشعار الدائم المخصص" else "Custom Persistent Notification",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic)
                                "إشعار مثبت بخلفية إسلامية برونزية، اسم الصلاة القادمة، والتاريخ الهجري والعد التنازلي"
                            else
                                "Ongoing bronze Islamic notification with countdown and full Hijri date",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = settings.persistentNotificationEnabled,
                        onCheckedChange = { onTogglePersistentNotification(it) },
                        modifier = Modifier.testTag("persistent_notification_switch")
                    )
                }
            }
        }

        // Language & Information Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("language_card"),
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
                    Column {
                        Text(
                            text = if (isArabic) "لغة التطبيق" else "Language",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) "العربية (Arabic)" else "English",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleLanguage,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_language_button")
                    ) {
                        Text(if (isArabic) "English" else "العربية")
                    }
                }
            }
        }
    }

    // Calculation Method Selection Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = if (isArabic) "طرق الحساب العالمية" else "Calculation Methods",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(CalculationMethod.entries) { method ->
                        val isSelected = settings.calculationMethod == method
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSetMethod(method)
                                    showMethodDialog = false
                                }
                                .testTag("method_option_${method.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = method.getName(isArabic),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Fajr: ${method.fajrAngle}° | Isha: ${if (method.ishaIntervalMinutes > 0) "${method.ishaIntervalMinutes}m" else "${method.ishaAngle}°"}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text(if (isArabic) "إغلاق" else "Close")
                }
            }
        )
    }
}
