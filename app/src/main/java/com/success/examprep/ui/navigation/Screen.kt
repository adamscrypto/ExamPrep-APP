package com.success.examprep.ui.navigation

/**
 * Type-safe navigation routes for ExamPrep CBT
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object SubjectSelection : Screen("subjects/{examId}") {
        fun createRoute(examId: String) = "subjects/$examId"
    }
    data object Cbt : Screen("cbt/{examId}/{subjectId}") {
        fun createRoute(examId: String, subjectId: String) = "cbt/$examId/$subjectId"
    }
    data object Result : Screen("result")
    data object Unlock : Screen("unlock/{examId}/{subjectId}") {
        fun createRoute(examId: String, subjectId: String) = "unlock/$examId/$subjectId"
    }
    data object Auth : Screen("auth")
    data object Profile : Screen("profile")
    data object Admin : Screen("admin")
}
