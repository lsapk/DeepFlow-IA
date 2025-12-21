package com.deepflowia.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.deepflowia.app.data.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class PrivacySettingsState(
    val canAccessTasks: Boolean = true,
    val canAccessHabits: Boolean = true,
    val canAccessGoals: Boolean = true,
    val canAccessJournal: Boolean = false, // Default to false for privacy
    val canAccessFocus: Boolean = true,
    val canAccessPersonalInfo: Boolean = false // Default to false for privacy
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsRepository(application.applicationContext)
    val settingsState: StateFlow<PrivacySettingsState> = repository.settingsState

    fun setCanAccessTasks(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessTasks = canAccess)
        repository.saveSettings(newState)
    }

    fun setCanAccessHabits(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessHabits = canAccess)
        repository.saveSettings(newState)
    }

    fun setCanAccessGoals(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessGoals = canAccess)
        repository.saveSettings(newState)
    }

    fun setCanAccessJournal(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessJournal = canAccess)
        repository.saveSettings(newState)
    }

    fun setCanAccessFocus(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessFocus = canAccess)
        repository.saveSettings(newState)
    }

    fun setCanAccessPersonalInfo(canAccess: Boolean) {
        val newState = settingsState.value.copy(canAccessPersonalInfo = canAccess)
        repository.saveSettings(newState)
    }
}
