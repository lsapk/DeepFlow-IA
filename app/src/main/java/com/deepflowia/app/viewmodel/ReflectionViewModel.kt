package com.deepflowia.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepflowia.app.data.SupabaseManager
import com.deepflowia.app.models.DailyReflection
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReflectionViewModel : ViewModel() {

    private val _reflections = MutableStateFlow<List<DailyReflection>>(emptyList())
    val reflections: StateFlow<List<DailyReflection>> = _reflections

    private val _questions = MutableStateFlow<List<String>>(emptyList())
    val questions: StateFlow<List<String>> = _questions

    private val _selectedReflection = MutableStateFlow<DailyReflection?>(null)
    val selectedReflection: StateFlow<DailyReflection?> = _selectedReflection

    init {
        fetchQuestions()
        fetchReflections()
    }

    private fun fetchQuestions() {
        _questions.value = listOf(
            "Quelle a été ma plus grande réussite cette semaine et pourquoi ?",
            "Qu'est-ce que j'ai appris de nouveau aujourd'hui ?",
            "Pour quoi suis-je reconnaissant(e) en ce moment ?",
            "Quelle action simple puis-je faire demain pour me rapprocher de mes objectifs ?",
            "Comment ai-je pris soin de moi aujourd'hui ?",
            "Quelle conversation m'a marqué(e) récemment et pourquoi ?"
        )
    }

    fun fetchReflections() {
        viewModelScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val result = SupabaseManager.client.postgrest.from("daily_reflections")
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<DailyReflection>()
                    _reflections.value = result.sortedByDescending { it.createdAt }
                }
            } catch (e: Exception) {
                Log.e("ReflectionViewModel", "Erreur lors de la récupération des réflexions", e)
            }
        }
    }

    fun getReflectionById(id: String) {
        viewModelScope.launch {
            try {
                if (id == "-1") {
                    _selectedReflection.value = null
                    return@launch
                }
                val result = SupabaseManager.client.postgrest.from("daily_reflections")
                    .select {
                        filter { eq("id", id) }
                    }
                    .decodeSingleOrNull<DailyReflection>()
                _selectedReflection.value = result
            } catch (e: Exception) {
                Log.e("ReflectionViewModel", "Erreur lors de la récupération de la réflexion", e)
                _selectedReflection.value = null
            }
        }
    }

    fun addReflection(question: String, answer: String) {
        viewModelScope.launch {
            try {
                val userId = SupabaseManager.client.auth.currentUserOrNull()?.id ?: return@launch
                val newReflection = DailyReflection(
                    userId = userId,
                    question = question,
                    answer = answer
                )
                SupabaseManager.client.postgrest.from("daily_reflections").insert(newReflection)
                fetchReflections()
            } catch (e: Exception) {
                Log.e("ReflectionViewModel", "Erreur lors de l'ajout de la réflexion", e)
            }
        }
    }

    fun updateReflection(reflection: DailyReflection) {
        viewModelScope.launch {
            try {
                reflection.id?.let {
                    SupabaseManager.client.postgrest.from("daily_reflections")
                        .update({
                            set("question", reflection.question)
                            set("answer", reflection.answer)
                        }) {
                            filter { eq("id", it) }
                        }
                    fetchReflections()
                }
            } catch (e: Exception) {
                Log.e("ReflectionViewModel", "Erreur lors de la mise à jour de la réflexion", e)
            }
        }
    }

    fun deleteReflection(reflection: DailyReflection) {
        viewModelScope.launch {
            try {
                reflection.id?.let {
                    SupabaseManager.client.postgrest.from("daily_reflections")
                        .delete {
                            filter { eq("id", it) }
                        }
                    fetchReflections()
                }
            } catch (e: Exception) {
                Log.e("ReflectionViewModel", "Erreur lors de la suppression de la réflexion", e)
            }
        }
    }
}
