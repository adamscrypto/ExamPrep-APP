package com.success.examprep.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.success.examprep.data.access.LocalAccessManager
import com.success.examprep.data.auth.FirebaseAuthRepository
import com.success.examprep.data.model.ExamType
import com.success.examprep.data.model.SubjectType
import com.success.examprep.data.repository.DefaultEntitlementRepository
import com.success.examprep.data.repository.DefaultStudentRepository
import com.success.examprep.data.repository.FirestoreQuestionRepository
import com.success.examprep.data.repository.FirestoreTestHistoryRepository
import com.success.examprep.ui.screens.admin.AdminDashboardScreen
import com.success.examprep.ui.screens.admin.AdminViewModel
import com.success.examprep.ui.screens.auth.AuthScreen
import com.success.examprep.ui.screens.auth.AuthViewModel
import com.success.examprep.ui.screens.cbt.CbtScreen
import com.success.examprep.ui.screens.cbt.CbtViewModel
import com.success.examprep.ui.screens.home.HomeScreen
import com.success.examprep.ui.screens.profile.ProfileScreen
import com.success.examprep.ui.screens.profile.ProfileViewModel
import com.success.examprep.ui.screens.result.ResultScreen
import com.success.examprep.ui.screens.splash.SplashScreen
import com.success.examprep.ui.screens.subject.SubjectSelectionScreen
import com.success.examprep.ui.screens.unlock.UnlockScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current.applicationContext
    val authRepository = remember { FirebaseAuthRepository.getInstance(context) }
    val historyRepository = remember { FirestoreTestHistoryRepository.getInstance(context) }
    val questionRepository = remember { FirestoreQuestionRepository.getInstance(context) }
    val studentRepository = remember { DefaultStudentRepository.getInstance(context) }
    val entitlementRepository = remember { DefaultEntitlementRepository.getInstance(context) }

    // Connect Stage 5 Student Entitlements to AccessManager
    val accessManager = remember { LocalAccessManager.getInstance() }
    accessManager.entitlementChecker = { examType, subjectType ->
        val user = authRepository.getCurrentUser()
        if (user != null) {
            entitlementRepository.hasActiveEntitlementSync(user.uid, examType, subjectType)
        } else {
            false
        }
    }

    val cbtViewModel: CbtViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CbtViewModel(
                    authRepository = authRepository,
                    testHistoryRepository = historyRepository
                ) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // 1. Splash Screen
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen (Guest and Authenticated Student)
        composable(route = Screen.Home.route) {
            HomeScreen(
                onExamSelected = { examType ->
                    navController.navigate(Screen.SubjectSelection.createRoute(examType.id))
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route)
                },
                authRepository = authRepository
            )
        }

        // 3. Subject Selection Screen
        composable(
            route = Screen.SubjectSelection.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId")
            val examType = ExamType.fromId(examId)

            val currentUser = authRepository.getCurrentUser()

            SubjectSelectionScreen(
                examType = examType,
                onNavigateBack = { navController.popBackStack() },
                onSubjectSelected = { subject ->
                    cbtViewModel.startExam(examType, subject.id)
                    navController.navigate(Screen.Cbt.createRoute(examType.id, subject.id))
                },
                entitlementRepository = entitlementRepository,
                currentStudentUid = currentUser?.uid
            )
        }

        // 4. CBT Screen
        composable(
            route = Screen.Cbt.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("subjectId") { type = NavType.StringType }
            )
        ) {
            CbtScreen(
                viewModel = cbtViewModel,
                onExamSubmitted = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Cbt.route) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUnlock = {
                    val currentState = cbtViewModel.uiState.value
                    val currentSubjectId = currentState.subject?.id ?: "mathematics"
                    navController.navigate(
                        Screen.Unlock.createRoute(currentState.examType.id, currentSubjectId)
                    )
                }
            )
        }

        // 5. Result Screen
        composable(route = Screen.Result.route) {
            ResultScreen(
                viewModel = cbtViewModel,
                onTryAgain = {
                    val currentState = cbtViewModel.uiState.value
                    val currentSubjectId = currentState.subject?.id ?: "mathematics"
                    navController.navigate(
                        Screen.Cbt.createRoute(currentState.examType.id, currentSubjectId)
                    ) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onChooseAnotherSubject = {
                    val currentState = cbtViewModel.uiState.value
                    navController.navigate(Screen.SubjectSelection.createRoute(currentState.examType.id)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToUnlock = {
                    val currentState = cbtViewModel.uiState.value
                    val currentSubjectId = currentState.subject?.id ?: "mathematics"
                    navController.navigate(
                        Screen.Unlock.createRoute(currentState.examType.id, currentSubjectId)
                    )
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Auth.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                authRepository = authRepository
            )
        }

        // 6. Unlock Full Access Screen (Stage 3 Access Control)
        composable(
            route = Screen.Unlock.route,
            arguments = listOf(
                navArgument("examId") { type = NavType.StringType },
                navArgument("subjectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId")
            val subjectId = backStackEntry.arguments?.getString("subjectId")
            val examType = ExamType.fromId(examId)
            val subjectType = SubjectType.fromId(subjectId)

            UnlockScreen(
                examType = examType,
                subjectType = subjectType,
                viewModel = cbtViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. Optional Authentication Screen (Stage 4)
        composable(route = Screen.Auth.route) {
            val authViewModel: AuthViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AuthViewModel(authRepository = authRepository) as T
                    }
                }
            )

            AuthScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToStudent = {
                    // Check if there is an unsaved test result to record
                    val currentResult = cbtViewModel.uiState.value.result
                    val user = authRepository.getCurrentUser()
                    if (currentResult != null && user != null && !cbtViewModel.uiState.value.isResultSavedToProfile) {
                        cbtViewModel.saveResultForStudent(user.uid)
                    }
                    navController.popBackStack()
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        // 8. Authenticated Student Profile & Performance History Screen (Stage 4)
        composable(route = Screen.Profile.route) {
            val profileViewModel: ProfileViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return ProfileViewModel(
                            authRepository = authRepository,
                            historyRepository = historyRepository
                        ) as T
                    }
                }
            )

            ProfileScreen(
                viewModel = profileViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartTest = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onSignedOut = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route)
                }
            )
        }

        // 9. Administrator Dashboard (Stage 5)
        composable(route = Screen.Admin.route) {
            val adminViewModel: AdminViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AdminViewModel(
                            authRepository = authRepository,
                            questionRepository = questionRepository,
                            studentRepository = studentRepository,
                            entitlementRepository = entitlementRepository
                        ) as T
                    }
                }
            )

            AdminDashboardScreen(
                viewModel = adminViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
