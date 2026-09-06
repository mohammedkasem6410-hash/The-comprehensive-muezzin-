package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.calculator.PrayerTimeCalculator
import com.example.model.CalculationMethod
import com.example.model.JuristicMethod
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleData = PrayerTimeCalculator.calculate(
      date = LocalDate.of(2026, 9, 6),
      latitude = 30.0626,
      longitude = 31.2497,
      cityNameAr = "الفجالة، القاهرة",
      cityNameEn = "Al-Fajjala, Cairo",
      method = CalculationMethod.EGYPTIAN,
      juristicMethod = JuristicMethod.SHAFI
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        PrayerTimesScreen(
          prayerData = sampleData,
          isArabic = true,
          onOpenLocationDialog = {},
          onTestAdhanVideo = {},
          onTestPreAlert = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
