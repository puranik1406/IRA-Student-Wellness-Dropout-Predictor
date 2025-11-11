package com.example.ira.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ira.ui.screens.LandingScreen
import com.example.ira.ui.screens.auth.LoginScreen
import com.example.ira.ui.screens.auth.StudentRegisterScreen
import com.example.ira.ui.screens.auth.CounselorRegisterScreen
import com.example.ira.ui.screens.StudentDashboardScreen
import com.example.ira.ui.screens.CounselorDashboardScreen
import com.example.ira.ui.screens.ChatbotScreen
import com.example.ira.ui.screens.MoodScreen
import com.example.ira.ui.screens.JournalScreen

// Define all routes
object Routes {
    const val LANDING = "landing"
    const val LOGIN = "login"
    const val STUDENT_REGISTER = "student_register"
    const val COUNSELOR_REGISTER = "counselor_register"
    const val STUDENT_DASHBOARD = "student_dashboard/{studentId}"
    const val COUNSELOR_DASHBOARD = "counselor_dashboard/{counselorId}"
    const val MOOD = "mood/{studentId}"
    const val JOURNAL = "journal/{studentId}"
    const val JOURNAL_DETAIL = "journal_detail/{journalId}"
    const val CHATBOT = "chatbot/{studentId}"

    // Helper functions to create routes with parameters
    fun studentDashboard(studentId: Long) = "student_dashboard/$studentId"
    fun counselorDashboard(counselorId: Long) = "counselor_dashboard/$counselorId"
    fun mood(studentId: Long) = "mood/$studentId"
    fun journal(studentId: Long) = "journal/$studentId"
    fun journalDetail(journalId: Long) = "journal_detail/$journalId"
    fun chatbot(studentId: Long) = "chatbot/$studentId"
}

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object StudentRegister : Screen("student_register")
    object CounselorRegister : Screen("counselor_register")
    object StudentDashboard : Screen("student_dashboard")
    object CounselorDashboard : Screen("counselor_dashboard")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Landing.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToStudentRegister = {
                    navController.navigate(Screen.StudentRegister.route)
                },
                onNavigateToCounselorRegister = {
                    navController.navigate(Screen.CounselorRegister.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = { userType ->
                    // Navigate to appropriate registration screen based on user type
                    val destination = if (userType == "student") {
                        Screen.StudentRegister.route
                    } else {
                        Screen.CounselorRegister.route
                    }
                    navController.navigate(destination)
                },
                onStudentLoginSuccess = { studentId, studentName ->
                    navController.navigate(Routes.studentDashboard(studentId)) {
                        // Clear back stack
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onCounselorLoginSuccess = { counselorId, counselorName ->
                    navController.navigate(Routes.counselorDashboard(counselorId)) {
                        // Clear back stack
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.StudentRegister.route) {
            StudentRegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Landing.route)
                    }
                },
                onRegisterSuccess = {
                    // Navigate to student dashboard after registration
                    navController.navigate(Routes.studentDashboard(1L)) { // TODO: Use actual user ID from registration
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CounselorRegister.route) {
            CounselorRegisterScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Landing.route)
                    }
                },
                onRegisterSuccess = {
                    // Navigate to counselor dashboard after registration
                    navController.navigate(Routes.counselorDashboard(1L)) { // TODO: Use actual user ID from registration
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                }
            )
        }

        // Student Dashboard with parameter
        composable(
            route = Routes.STUDENT_DASHBOARD,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            StudentDashboardScreen(
                navController = navController,
                studentId = studentId,
                onLogout = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Counselor Dashboard with parameter
        composable(
            route = Routes.COUNSELOR_DASHBOARD,
            arguments = listOf(navArgument("counselorId") { type = NavType.LongType })
        ) { backStackEntry ->
            val counselorId = backStackEntry.arguments?.getLong("counselorId") ?: 0L
            CounselorDashboardScreen(
                navController = navController,
                counselorId = counselorId,
                onLogout = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Mood Tracking
        composable(
            route = Routes.MOOD,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            MoodScreen(navController = navController, studentId = studentId)
        }

        // Journal
        composable(
            route = Routes.JOURNAL,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            JournalScreen(navController = navController, studentId = studentId)
        }

        // Journal Detail
        composable(
            route = Routes.JOURNAL_DETAIL,
            arguments = listOf(navArgument("journalId") { type = NavType.LongType })
        ) { backStackEntry ->
            val journalId = backStackEntry.arguments?.getLong("journalId") ?: 0L
            // TODO: JournalDetailScreen(navController, journalId)
        }

        // Chatbot (Ira.ai)
        composable(
            route = Routes.CHATBOT,
            arguments = listOf(navArgument("studentId") { type = NavType.LongType })
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
            ChatbotScreen(navController = navController, studentId = studentId)
        }
    }
}
