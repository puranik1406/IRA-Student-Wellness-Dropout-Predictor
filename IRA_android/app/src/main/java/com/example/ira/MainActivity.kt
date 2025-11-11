package com.example.ira

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ira.ui.navigation.NavGraph
import com.example.ira.ui.navigation.Routes
import com.example.ira.ui.theme.IRATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = application as IRAApplication
        val sessionManager = application.sessionManager
        
        setContent {
            IRATheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Check if user is logged in
                    val userSession by sessionManager.userSession.collectAsState(initial = null)
                    
                    // Determine start destination based on session
                    val startDestination = when {
                        userSession == null -> Routes.LANDING
                        userSession?.userType == "student" -> 
                            Routes.studentDashboard(userSession?.userId ?: 0L)
                        userSession?.userType == "counselor" -> 
                            Routes.counselorDashboard(userSession?.userId ?: 0L)
                        else -> Routes.LANDING
                    }
                    
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}