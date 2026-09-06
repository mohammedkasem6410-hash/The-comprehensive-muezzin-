package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Prayer
import com.example.model.PrayerTimeEntry
import com.example.model.PrayerTimesData
import com.example.ui.theme.*

@Composable
fun PrayerTimesScreen(
    prayerData: PrayerTimesData,
    isArabic: Boolean,
    onOpenLocationDialog: () -> Unit,
    onTestAdhanVideo: () -> Unit,
    onTestPreAlert: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp)
    ) {
        // Hero Bronze Prayer Card (Sleek Interface)
        item {
            HeroPrayerCard(
                prayerData = prayerData,
                isArabic = isArabic,
                onOpenLocationDialog = onOpenLocationDialog,
                onTestAdhanVideo = onTestAdhanVideo,
                onTestPreAlert = onTestPreAlert
            )
        }

        // 5-Column Sleek Horizontal Quick Grid (Sleek Interface)
        item {
            SleekPrayerTimesGrid(
                prayerData = prayerData,
                isArabic = isArabic
            )
        }

        // Section Title: Prayer Times
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "مواقيت الصلاة اليوم" else "Today's Prayer Times",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )

                Text(
                    text = prayerData.method.getName(isArabic),
                    fontSize = 11.sp,
                    color = SlateTextSecondary,
                    maxLines = 1
                )
            }
        }

        // 6 Prayers Detailed List
        items(prayerData.entries) { entry ->
            PrayerItemCard(
                entry = entry,
                prayerData = prayerData,
                isArabic = isArabic,
                onTestAdhanVideo = onTestAdhanVideo
            )
        }

        // Persistent Notification & Widget Preview (Sleek Interface showcase)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic) "الإشعار الدائم والودجت" else "Persistent Notification & Widget",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = SlateTextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item {
            SleekNotificationAndWidgetPreview(
                prayerData = prayerData,
                isArabic = isArabic
            )
        }
    }
}

@Composable
private fun HeroPrayerCard(
    prayerData: PrayerTimesData,
    isArabic: Boolean,
    onOpenLocationDialog: () -> Unit,
    onTestAdhanVideo: () -> Unit,
    onTestPreAlert: () -> Unit
) {
    val nextEntry = prayerData.getEntry(prayerData.nextPrayer)
    val nextPrayerName = prayerData.nextPrayer.getDisplayName(isArabic, prayerData.isFriday)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(36.dp), spotColor = BronzeShadow, ambientColor = BronzeShadow)
            .testTag("hero_prayer_card"),
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.cardColors(containerColor = BronzeGradientStart)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BronzeGradientStart,
                            BronzeGradientEnd
                        )
                    )
                )
                .padding(20.dp)
        ) {
            // Ambient soft glowing radial circle in corner
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-30).dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Location Chip + Adhan Preview Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onOpenLocationDialog() }
                            .testTag("location_chip"),
                        color = Color.Black.copy(alpha = 0.22f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = OrangeTextLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) prayerData.cityNameAr else prayerData.cityName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Quick Test Fullscreen Video Button
                    IconButton(
                        onClick = onTestAdhanVideo,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(alpha = 0.22f), CircleShape)
                            .testTag("quick_test_adhan_video")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Test Fullscreen Video Adhan",
                            tint = OrangeTextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Next Prayer Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = if (isArabic) "الصلاة القادمة: $nextPrayerName" else "Next Prayer: $nextPrayerName",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrangeTextLight,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Next Prayer Time (Bold Sleek Display)
                Text(
                    text = nextEntry?.timeFormatted ?: "12:05",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-1).sp,
                    modifier = Modifier.testTag("countdown_text")
                )

                // Countdown subtext
                Text(
                    text = if (isArabic) "يتبقى ${prayerData.formattedRemainingCountdown} دقيقة" else "${prayerData.formattedRemainingCountdown} remaining",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = OrangeTextSub
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dates Box: Hijri & Gregorian
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Black.copy(alpha = 0.22f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isArabic) prayerData.hijriDateFormattedAr else prayerData.hijriDateFormattedEn,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeTextLight,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) prayerData.gregorianDateFormattedAr else prayerData.gregorianDateFormattedEn,
                            fontSize = 12.sp,
                            color = SlateTextPrimary.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Test Pre-alert bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.20f))
                        .clickable { onTestPreAlert() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("test_pre_alert_banner"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = OrangeTextLight,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "تجربة شاشة التنبيه المسبق (يتبقى على الصلاة)" else "Test Pre-Alert Screen",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SleekPrayerTimesGrid(
    prayerData: PrayerTimesData,
    isArabic: Boolean
) {
    // 5 main prayers: Fajr, Dhuhr, Asr, Maghrib, Isha
    val mainPrayers = prayerData.entries.filter { it.prayer != Prayer.SUNRISE }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sleek_prayer_grid"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (entry in mainPrayers) {
            val isNext = entry.prayer == prayerData.nextPrayer
            val isCurrent = entry.prayer == prayerData.currentPrayer
            val isActive = isNext || isCurrent

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isActive) Color(0x332563EB) else Color(0x801E293B)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) Color(0xFF3B82F6) else Color(0x60334155),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Color(0xFF3B82F6), CircleShape)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = entry.prayer.getDisplayName(isArabic, prayerData.isFriday),
                        fontSize = 10.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        color = if (isActive) Color(0xFF60A5FA) else Color(0xFF94A3B8),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = entry.timeFormatted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color.White else Color(0xFFCBD5E1),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PrayerItemCard(
    entry: PrayerTimeEntry,
    prayerData: PrayerTimesData,
    isArabic: Boolean,
    onTestAdhanVideo: () -> Unit
) {
    val isCurrent = entry.prayer == prayerData.currentPrayer
    val isNext = entry.prayer == prayerData.nextPrayer
    val isSunrise = entry.prayer == Prayer.SUNRISE

    val displayName = entry.prayer.getDisplayName(isArabic, prayerData.isFriday)

    val cardModifier = if (isNext) {
        Modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF3B82F6), RoundedCornerShape(18.dp))
            .testTag("prayer_item_${entry.prayer.key}_next")
    } else {
        Modifier
            .fillMaxWidth()
            .border(1.dp, if (isCurrent) BronzeGradientStart else Color(0x40334155), RoundedCornerShape(18.dp))
            .testTag("prayer_item_${entry.prayer.key}")
    }

    val containerColor = when {
        isNext -> Color(0x223B82F6)
        isCurrent -> Color(0x1F8B5E3C)
        else -> Color(0x801E293B)
    }

    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(if (isNext) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Prayer Name + Tag
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            color = when {
                                isNext -> Color(0x333B82F6)
                                isCurrent -> Color(0x338B5E3C)
                                else -> Color(0x1A94A3B8)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPrayerIcon(entry.prayer),
                        contentDescription = displayName,
                        tint = when {
                            isNext -> Color(0xFF60A5FA)
                            isCurrent -> OrangeTextLight
                            else -> Color(0xFF94A3B8)
                        },
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayName,
                            fontSize = 16.sp,
                            fontWeight = if (isNext || isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isNext || isCurrent) Color.White else Color(0xFFE2E8F0)
                        )

                        if (prayerData.isFriday && entry.prayer == Prayer.DHUHR) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BronzeGradientEnd
                            ) {
                                Text(
                                    text = if (isArabic) "يوم الجمعة" else "Friday",
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // Status pill
                    when {
                        isCurrent -> {
                            Text(
                                text = if (isArabic) "الآن" else "Now",
                                fontSize = 11.sp,
                                color = OrangeTextLight,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        isNext -> {
                            Text(
                                text = if (isArabic) "الصلاة القادمة" else "Upcoming",
                                fontSize = 11.sp,
                                color = Color(0xFF60A5FA),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Right: Time Display + Play adhan button
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = entry.timeFormatted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isNext) Color(0xFF60A5FA) else Color(0xFFF8FAFC)
                )

                if (!isSunrise) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onTestAdhanVideo,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("play_adhan_${entry.prayer.key}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Adhan",
                            tint = if (isNext) Color(0xFF60A5FA) else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SleekNotificationAndWidgetPreview(
    prayerData: PrayerTimesData,
    isArabic: Boolean
) {
    val remainingMinutes = (prayerData.millisUntilNextPrayer / (1000 * 60)) % 60
    val remainingHours = prayerData.millisUntilNextPrayer / (1000 * 60 * 60)
    val notifCountdown = String.format(java.util.Locale.ENGLISH, "m%02dh %02d", remainingMinutes, remainingHours)

    val totalRemainingMinutes = prayerData.millisUntilNextPrayer / (1000 * 60)
    val remainingSecs = (prayerData.millisUntilNextPrayer / 1000) % 60
    val widgetCountdown = String.format(java.util.Locale.ENGLISH, "+ %02d:%02d", totalRemainingMinutes, remainingSecs)

    val hijriInfo = com.example.calculator.HijriCalendarHelper.getHijriDate(prayerData.date)
    val hijriWithDay = if (isArabic) {
        "${hijriInfo.dayNameAr} ${hijriInfo.day} ${hijriInfo.monthNameAr} ${hijriInfo.year}"
    } else {
        "${hijriInfo.dayNameEn} ${hijriInfo.day} ${hijriInfo.monthNameEn} ${hijriInfo.year}"
    }
    val gregorianShort = "${prayerData.date.dayOfMonth}-${prayerData.date.monthValue}-${prayerData.date.year}"
    val cityDisplay = (if (isArabic) prayerData.cityNameAr else prayerData.cityName).split("،", ",").first().trim()

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 1. Persistent Bronze Notification (Matching Screenshot 1)
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF341E10)),
            border = BorderStroke(1.dp, Color(0xFF5C3B22)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sleek_notification_preview")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Ornate Islamic Corner Motif
                Icon(
                    painter = painterResource(id = R.drawable.ic_islamic_corner_ornament),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(46.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Left-Center: Next Prayer Name & Hijri Date
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = prayerData.nextPrayer.getDisplayName(isArabic, prayerData.isFriday),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isArabic) prayerData.hijriDateFormattedAr else prayerData.hijriDateFormattedEn,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }

                // Right-Center: Remaining Time Label & Countdown (m57h 02)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        text = if (isArabic) "الوقت المتبقي" else "Remaining",
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Text(
                        text = notifCountdown,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Far Right: Teal Circular Mosque Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_mosque_green_circle),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // 2. Sky Blue App Widget Card Preview (Matching Screenshot 2)
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1512)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sleek_widget_preview")
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Bar: City Name (Right-aligned in white bold text)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cityDisplay,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Cyan/Sky Blue Header Banner (Prayer Name on Right, + 47:45 on Left)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0284C7), Color(0xFF0EA5E9))
                            ),
                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = widgetCountdown,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val activeOrNextPrayer = prayerData.currentPrayer ?: prayerData.nextPrayer
                        Text(
                            text = activeOrNextPrayer.getDisplayName(isArabic, prayerData.isFriday),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // 5-Column Grid (RTL: Fajr, Dhuhr, Asr, Maghrib, Isha)
                val displayPrayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

                fun formatWidgetTime(p: Prayer): String {
                    val time = prayerData.getEntry(p)?.time ?: return "--:--"
                    val hour = if (time.hour % 12 == 0) 12 else time.hour % 12
                    val minute = time.minute
                    val period = if (isArabic) {
                        if (time.hour >= 12) "م" else "ص"
                    } else {
                        if (time.hour >= 12) "PM" else "AM"
                    }
                    return String.format(java.util.Locale.ENGLISH, "%02d:%02d %s", hour, minute, period)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    displayPrayers.forEachIndexed { index, prayer ->
                        val isCurrent = prayer == prayerData.currentPrayer
                        val isNext = prayer == prayerData.nextPrayer

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isCurrent) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                                )
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top Tag
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    isCurrent -> {
                                        Text(
                                            text = if (isArabic) "الان" else "NOW",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    isNext -> {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF0284C7), RoundedCornerShape(2.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "لاحقا" else "NEXT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Prayer Name
                            Text(
                                text = prayer.getDisplayName(isArabic, prayerData.isFriday),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else Color(0xFF334155)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Prayer Time
                            Text(
                                text = formatWidgetTime(prayer),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.White else Color(0xFF334155),
                                maxLines = 1
                            )
                        }

                        if (index < displayPrayers.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(60.dp)
                                    .background(Color(0xFFCBD5E1))
                            )
                        }
                    }
                }

                // Bottom Bar: Dark Brown with Gregorian Date (Left) & Hijri Date with Day Name (Right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF22140C),
                            shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = gregorianShort,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Text(
                        text = hijriWithDay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

private fun getPrayerIcon(prayer: Prayer) = when (prayer) {
    Prayer.FAJR -> Icons.Default.NightsStay
    Prayer.SUNRISE -> Icons.Default.WbSunny
    Prayer.DHUHR -> Icons.Default.WbTwilight
    Prayer.ASR -> Icons.Default.LightMode
    Prayer.MAGHRIB -> Icons.Default.WbCloudy
    Prayer.ISHA -> Icons.Default.Bedtime
}
