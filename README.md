# DeepFlow-IA
Application de Productivité
Description de l'application Android native (Kotlin)
Pour l'application native, nous allons transposer ces fonctionnalités en adoptant les meilleures pratiques et les conventions d'interface utilisateur d'Android pour une expérience optimale sur mobile.

Objectif : Offrir toutes les fonctionnalités de l'application web dans une interface rapide, fluide et parfaitement intégrée à l'écosystème Android.

Technologie et Architecture :

Langage : Kotlin (le langage officiel pour le développement Android).
Interface Utilisateur : Jetpack Compose (le toolkit moderne et déclaratif d'Android pour construire des UI natives).
Architecture : MVVM (Model-View-ViewModel) pour une séparation claire des responsabilités, une meilleure testabilité et une maintenance plus simple.
Navigation : Jetpack Navigation Compose pour gérer les transitions entre les différents écrans de l'application.
Opérations Asynchrones : Kotlin Coroutines pour gérer les appels réseau vers Supabase sans bloquer l'interface utilisateur.
Client Supabase : Utilisation de la bibliothèque officielle Supabase pour Kotlin.
Structure des écrans :

Écran de connexion / inscription : Le point d'entrée de l'application pour l'authentification.
Écran principal (Tableau de bord) :
Utilisation d'une barre de navigation inférieure (BottomNavigationView) avec 4 onglets principaux : Tâches, Objectifs, Habitudes, Journal.
L'écran d'accueil pourrait afficher un résumé de la journée : nombre de tâches restantes, habitudes à compléter, etc.
Écran des Tâches :
Une liste (LazyColumn) des tâches de l'utilisateur.
Un bouton d'action flottant (FloatingActionButton) pour ajouter rapidement une nouvelle tâche.
Des gestes comme le "swipe-to-delete" ou "swipe-to-complete" pour une interaction rapide.
Écran des Objectifs :
Une liste d'objectifs avec des barres de progression visuelles.
En cliquant sur un objectif, l'utilisateur accède à une vue détaillée où il peut mettre à jour sa progression.
Écran des Habitudes :
Une vue claire des habitudes de la journée/semaine, avec des cases à cocher ou des boutons pour les marquer comme complétées.
Affichage des "streaks" (séries) pour motiver l'utilisateur.
Écran du Journal :
Liste des entrées de journal, affichant le titre et la date.
Un clic ouvre l'entrée complète en lecture seule. Un bouton permet de passer en mode édition.
Un bouton d'action flottant pour créer une nouvelle entrée.
Écran de Profil / Paramètres : Accessible depuis une icône dans la barre supérieure, pour gérer les informations du compte et les préférences de l'application.
3. Rapport sur la base de données Supabase
L'un des plus grands avantages de votre projet est que l'application web et l'application native partageront la même base de données Supabase. Cela signifie que les données d'un utilisateur seront synchronisées en temps réel entre les deux plateformes.

URL Supabase : https://xzgdfetnjnwrberyddmf.supabase.co Clé publique : (Vous devrez l'intégrer de manière sécurisée dans votre application Android).

Voici la correspondance entre les tables de votre base de données et les fonctionnalités de l'application :

Nom de la table	Rôle dans l'application	Opérations Supabase (côté client Kotlin)
user_profiles	Stocke les informations publiques des utilisateurs (nom d'utilisateur, avatar). Lié à la table auth.users de Supabase.	supabase.auth.signIn(), supabase.auth.signUp(), supabase.from("user_profiles").select()
tasks	Contient toutes les tâches créées par les utilisateurs. Chaque tâche est liée à un user_id.	select() pour lister, insert() pour créer, update() pour modifier (ex: changer l'état is_completed), delete() pour supprimer.
goals	Enregistre les objectifs à long terme des utilisateurs, avec leur user_id.	select(), insert(), update() (pour la progression), delete().
habits	Définit les habitudes que les utilisateurs veulent suivre.	select(), insert(), delete(). Il faudra probablement une table supplémentaire (habit_logs par exemple) pour suivre les completions quotidiennes.
journal_entries	Stocke le contenu de chaque entrée de journal, liée à un user_id.	select(), insert(), update(), delete().
