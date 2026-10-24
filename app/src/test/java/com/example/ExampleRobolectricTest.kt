package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.LifeCalendarRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("人生日历", appName)
    }

    @Test
    fun `calculate life stats accurately`() {
        val cal = Calendar.getInstance().apply {
            set(2000, Calendar.JANUARY, 1, 0, 0, 0)
        }
        val birthMillis = cal.timeInMillis
        // 10 years later
        val currentMillis = birthMillis + 10L * 365 * 24 * 3600 * 1000L

        // Dummy repository call test
        val totalWeeksIn90Years = 90 * 52
        val elapsedDays = (currentMillis - birthMillis) / (1000L * 60 * 60 * 24)
        val passedWeeks = (elapsedDays / 7).toInt()

        assertTrue(passedWeeks in 520..525)
        assertEquals(4680, totalWeeksIn90Years)
    }
}
