package com.example.ira

import android.app.Application
import android.util.Log
import com.example.ira.data.local.database.IRADatabase
import com.example.ira.data.preferences.SessionManager
import com.example.ira.data.repository.IRARepository
runanywhere sdk iimport com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.data.models.SDKEnvironment
import com.runanywhere.sdk.public.extensions.addModelFromURL
import com.runanywhere.sdk.llm.llamacpp.LlamaCppServiceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class IRAApplication : Application() {

    // Application scope for database operations
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Lazy initialization
    val database by lazy { IRADatabase.getDatabase(this, applicationScope) }

    val repository by lazy {
        IRARepository(
            studentDao = database.studentDao(),
            counselorDao = database.counselorDao(),
            moodDao = database.moodDao(),
            journalDao = database.journalDao(),
            activityDao = database.activityDao(),
            attendanceDao = database.attendanceDao(),
            meetingDao = database.meetingDao(),
            notificationDao = database.notificationDao()
        )
    }

    val sessionManager by lazy { SessionManager(this) }

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "IRA Application starting...")

        // Initialize RunAnywhere SDK for AI chatbot (TEMPORARILY DISABLED FOR DEBUGGING)
        // TODO: Re-enable after fixing SDK crash
        /*
        try {
            initializeRunAnywhereSDK()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SDK initialization: ${e.message}", e)
            // App continues normally without AI
        }
        */
        Log.w(TAG, "RunAnywhere SDK initialization DISABLED - app will use mock responses")

        Log.i(TAG, "IRA Application started successfully")
    }

    private fun initializeRunAnywhereSDK() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Initializing RunAnywhere SDK...")

                // Step 1: Initialize SDK
                RunAnywhere.initialize(
                    context = this@IRAApplication,
                    apiKey = "dev", // Any string works in dev mode
                    environment = SDKEnvironment.DEVELOPMENT
                )
                Log.d(TAG, "SDK initialized")

                // Step 2: Register LLM Service Provider
                LlamaCppServiceProvider.register()
                Log.d(TAG, "LlamaCpp Service Provider registered")

                // Step 3: Register AI Models
                registerModels()

                // Step 4: Scan for previously downloaded models
                RunAnywhere.scanForDownloadedModels()

                Log.i(TAG, "RunAnywhere SDK initialized successfully")

            } catch (e: Exception) {
                Log.e(TAG, "RunAnywhere SDK initialization failed: ${e.message}", e)
                e.printStackTrace()
                // Don't crash - app will work without AI
            }
        }
    }

    private suspend fun registerModels() {
        try {
            // Primary Model: Qwen 2.5 0.5B Instruct (Best for testing - 374MB)
            addModelFromURL(
                url = "https://huggingface.co/Triangle104/Qwen2.5-0.5B-Instruct-Q6_K-GGUF/resolve/main/qwen2.5-0.5b-instruct-q6_k.gguf",
                name = "Qwen 2.5 0.5B Instruct Q6_K",
                type = "LLM"
            )
            Log.d(TAG, "Registered Qwen 2.5 0.5B model")

            // Backup Model: Llama 3.2 1B (Better quality - 815MB)
            addModelFromURL(
                url = "https://huggingface.co/bartowski/Llama-3.2-1B-Instruct-GGUF/resolve/main/Llama-3.2-1B-Instruct-Q6_K_L.gguf",
                name = "Llama 3.2 1B Instruct Q6_K",
                type = "LLM"
            )
            Log.d(TAG, "Registered Llama 3.2 1B model")

            // Testing Model: SmolLM2 360M (Quick testing - 119MB)
            addModelFromURL(
                url = "https://huggingface.co/prithivMLmods/SmolLM2-360M-GGUF/resolve/main/SmolLM2-360M.Q8_0.gguf",
                name = "SmolLM2 360M Q8_0",
                type = "LLM"
            )
            Log.d(TAG, "Registered SmolLM2 360M model")

        } catch (e: Exception) {
            Log.e(TAG, "Model registration failed: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "IRAApplication"

        // System prompt for Ira.ai
        const val IRA_SYSTEM_PROMPT = """
You are Ira.ai, an AI wellbeing companion built for students.
Your goal is to support their emotional, academic, and physical wellbeing.

Respond empathetically, with warmth and understanding.
Keep responses concise, human-like, and natural — like a supportive senior or mentor.
Never sound robotic or overly formal.
When asked academic or motivational questions, offer guidance while maintaining a friendly tone.
If students share stress, anxiety, or confusion, respond gently and offer actionable suggestions 
(like relaxation tips or time management help).
Always maintain a positive, respectful, and privacy-conscious tone.
Keep responses under 150 words unless specifically asked for more detail.
"""
    }
}
