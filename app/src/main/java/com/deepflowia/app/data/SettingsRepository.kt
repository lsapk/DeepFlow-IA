package com.deepflowia.app.data

import android.content.Context
import android.content.SharedPreferences
import com.deepflowia.app.viewmodel.PrivacySettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("privacy_settings", Context.MODE_PRIVATE)

    private val _settingsState = MutableStateFlow(loadSettings())
    val settingsState: StateFlow<PrivacySettingsState> = _settingsState.asStateFlow()

    private fun loadSettings(): PrivacySettingsState {
        return PrivacySettingsState(
            canAccessTasks = prefs.getBoolean("canAccessTasks", true),
            canAccessHabits = prefs.getBoolean("canAccessHabits", true),
            canAccessGoals = prefs.getBoolean("canAccessGoals", true),
            canAccessJournal = prefs.getBoolean("canAccessJournal", false),
            canAccessFocus = prefs.getBoolean("canAccessFocus", true),
            canAccessPersonalInfo = prefs.getBoolean("canAccessPersonalInfo", false)
        )
    }

    fun saveSettings(state: PrivacySettingsState) {
        _settingsState.update { state }
        with(prefs.edit()) {
            putBoolean("canAccessTasks", state.canAccessTasks)
            putBoolean("canAccessHabits", state.canAccessHabits)
            putBoolean("canAccessGoals", state.canAccessGoals)
            putBoolean("canAccessJournal", state.canAccessJournal)
            putBoolean("canAccessFocus", state.canAccessFocus)
            putBoolean("canAccessPersonalInfo", state.canAccessPersonalInfo)
            apply()
        }
    }
}
