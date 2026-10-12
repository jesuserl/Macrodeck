package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DeckPreferencesManager
import com.example.model.DeckButton
import com.example.model.ServerProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches Macro Deck`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Macro Deck", appName)
    }

    @Test
    fun `preferences stores and retrieves server configuration`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = DeckPreferencesManager(context)

        prefs.serverHost = "192.168.1.155"
        prefs.serverPort = 8191
        prefs.autoHideTopBar = true
        prefs.keepScreenOn = true

        assertEquals("192.168.1.155", prefs.serverHost)
        assertEquals(8191, prefs.serverPort)
        assertTrue(prefs.autoHideTopBar)
        assertTrue(prefs.keepScreenOn)
    }

    @Test
    fun `deck button model properties are retained`() {
        val button = DeckButton(
            id = "btn_1_2",
            positionX = 1,
            positionY = 2,
            label = "OBS STREAM",
            backgroundColorHex = "#FF0055",
            state = true
        )

        assertEquals("btn_1_2", button.id)
        assertEquals(1, button.positionX)
        assertEquals(2, button.positionY)
        assertEquals("OBS STREAM", button.label)
        assertTrue(button.state)
    }

    @Test
    fun `profiles can be saved and retrieved`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = DeckPreferencesManager(context)

        val profile = ServerProfile(
            id = "test_profile_id",
            name = "Estudio Gaming",
            host = "10.0.0.45",
            port = 8191
        )
        prefs.saveProfile(profile)

        val retrieved = prefs.getSavedProfiles().find { it.id == "test_profile_id" }
        assertNotNull(retrieved)
        assertEquals("Estudio Gaming", retrieved?.name)
        assertEquals("10.0.0.45", retrieved?.host)
    }
}
