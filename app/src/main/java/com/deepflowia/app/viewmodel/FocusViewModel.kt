package com.deepflowia.app.viewmodel

import androidx.lifecycle.ViewModel

// La logique du minuteur a été déplacée dans FocusTimerService pour garantir son exécution en arrière-plan.
// L'interface utilisateur interagira désormais directement avec le service via un ServiceConnection.
class FocusViewModel : ViewModel() {
    // Ce ViewModel pourra être utilisé ultérieurement pour contenir l'état de configuration
    // ou pour récupérer des données liées (par exemple, une liste de tâches à associer),
    // mais la logique principale du minuteur n'est plus de sa responsabilité.
}
