package com.example.lineofthree

import android.app.Application
import com.example.lineofthree.data.SettingsRepository

class LineOfThreeApp : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
