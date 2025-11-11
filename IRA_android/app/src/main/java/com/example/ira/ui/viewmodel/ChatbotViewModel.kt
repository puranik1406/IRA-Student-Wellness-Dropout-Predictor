package com.example.ira.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ira.IRAApplication
import com.example.ira.ai.VoiceService
import com.example.ira.ai.VoiceRecognitionResult
import com.runanywhere.sdk.public.RunAnywhere
import com.runanywhere.sdk.public.extensions.listAvailableModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val isStreaming: Boolean = false
)

data class ModelInfo(
    val id: String,
    val name: String,
    val size: String,
    val isDownloaded: Boolean,
    val isLoaded: Boolean
)

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isModelLoaded: Boolean = false,
    val availableModels: List<ModelInfo> = emptyList(),
    val selectedModel: String? = null,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val partialVoiceInput: String = "",
    val error: String? = null
)

class ChatbotViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private val voiceService = VoiceService(application.applicationContext)
    private val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private var messageIdCounter = 0L

    companion object {
        private const val TAG = "ChatbotViewModel"
    }

    init {
        // Initialize TTS
        voiceService.initializeTTS { success ->
            Log.d(TAG, "TTS initialized: $success")
        }

        // Load available models (with error handling)
        try {
            loadAvailableModels()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load models - SDK may not be initialized", e)
            addMessage(
                "Hi! I'm Ira.ai 🧸\n\nThe AI system is currently initializing. Please try again in a moment, or continue using the app's other features!",
                isUser = false
            )
        }
    }

    /**
     * Load and display available AI models
     */
    fun loadAvailableModels() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val models = listAvailableModels()
                val modelInfoList = models.map { model ->
                    ModelInfo(
                        id = model.id,
                        name = model.name,
                        size = "~${estimateModelSize(model.name)}",
                        isDownloaded = model.isDownloaded,
                        isLoaded = false // Will be determined by actual load status
                    )
                }

                _uiState.update {
                    it.copy(
                        availableModels = modelInfoList,
                        selectedModel = modelInfoList.firstOrNull()?.id,
                        isModelLoaded = false // User needs to load explicitly
                    )
                }

                Log.d(TAG, "Loaded ${modelInfoList.size} models")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load models", e)
                _uiState.update { it.copy(error = "Failed to load models: ${e.message}") }
            }
        }
    }

    private fun estimateModelSize(name: String): String {
        return when {
            name.contains("0.5B", ignoreCase = true) -> "374 MB"
            name.contains("1B", ignoreCase = true) -> "815 MB"
            name.contains("1.5B", ignoreCase = true) -> "1.2 GB"
            else -> "Unknown"
        }
    }

    /**
     * Download selected AI model
     */
    fun downloadModel(modelId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isDownloading = true, downloadProgress = 0f) }

                addMessage("Starting model download... This may take a few minutes.", isUser = false)

                RunAnywhere.downloadModel(modelId).collect { progress ->
                    _uiState.update { it.copy(downloadProgress = progress) }
                    Log.d(TAG, "Download progress: ${(progress * 100).toInt()}%")
                }

                _uiState.update {
                    it.copy(isDownloading = false, downloadProgress = 1f)
                }

                addMessage("Model downloaded successfully! Now loading...", isUser = false)
                Log.d(TAG, "Model downloaded successfully")
                
                // Refresh model list
                loadAvailableModels()

            } catch (e: Exception) {
                Log.e(TAG, "Model download failed", e)
                _uiState.update {
                    it.copy(
                        isDownloading = false,
                        error = "Download failed: ${e.message}"
                    )
                }
                addMessage("Download failed: ${e.message}", isUser = false)
            }
        }
    }

    /**
     * Load AI model into memory
     */
    fun loadModel(modelId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true) }
                addMessage("Loading model into memory... Please wait.", isUser = false)

                val success = RunAnywhere.loadModel(modelId)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isModelLoaded = success,
                        selectedModel = if (success) modelId else null
                    )
                }

                if (success) {
                    Log.d(TAG, "Model loaded successfully")
                    addMessage(
                        "Model loaded! I'm ready to chat. How can I help you today? 😊",
                        isUser = false
                    )
                } else {
                    _uiState.update { it.copy(error = "Failed to load model") }
                    addMessage("Failed to load model. Please try again.", isUser = false)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Model load failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isModelLoaded = false,
                        error = "Load failed: ${e.message}"
                    )
                }
                addMessage("Load failed: ${e.message}", isUser = false)
            }
        }
    }

    /**
     * Send text message to AI
     */
    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Check if model is loaded
        if (!_uiState.value.isModelLoaded) {
            addMessage("Please download and load a model first!", isUser = false)
            return
        }

        // Add user message
        addMessage(text, isUser = true)

        // Generate AI response
        generateResponse(text)
    }

    /**
     * Generate AI response using RunAnywhere SDK
     */
    private fun generateResponse(userMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true) }

                // Create AI message placeholder
                val aiMessageId = ++messageIdCounter
                val aiMessage = ChatMessage(
                    id = aiMessageId,
                    text = "",
                    isUser = false,
                    timestamp = dateFormat.format(Date()),
                    isStreaming = true
                )

                // Add placeholder message
                _uiState.update {
                    it.copy(messages = it.messages + aiMessage)
                }

                // Build prompt with system instruction
                val fullPrompt = "${IRAApplication.IRA_SYSTEM_PROMPT}\n\nUser: $userMessage\n\nAssistant:"

                // Stream response
                var fullResponse = ""
                RunAnywhere.generateStream(fullPrompt).collect { token ->
                    fullResponse += token

                    // Update streaming message
                    _uiState.update {
                        val updatedMessages = it.messages.map { msg ->
                            if (msg.id == aiMessageId) {
                                msg.copy(text = fullResponse)
                            } else {
                                msg
                            }
                        }
                        it.copy(messages = updatedMessages)
                    }
                }

                // Finalize message (remove streaming flag)
                _uiState.update {
                    val updatedMessages = it.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(isStreaming = false)
                        } else {
                            msg
                        }
                    }
                    it.copy(messages = updatedMessages, isLoading = false)
                }

                Log.d(TAG, "Response generated: ${fullResponse.take(50)}...")

            } catch (e: Exception) {
                Log.e(TAG, "Response generation failed", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to generate response: ${e.message}"
                    )
                }

                addMessage(
                    "I'm having trouble responding right now. Please try again. Error: ${e.message}",
                    isUser = false
                )
            }
        }
    }

    /**
     * Start voice input (Speech-to-Text)
     */
    fun startVoiceInput() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isListening = true, partialVoiceInput = "") }

                voiceService.startListening().collect { result ->
                    when (result) {
                        is VoiceRecognitionResult.PartialResult -> {
                            _uiState.update { it.copy(partialVoiceInput = result.text) }
                        }

                        is VoiceRecognitionResult.Success -> {
                            _uiState.update {
                                it.copy(isListening = false, partialVoiceInput = "")
                            }
                            // Send recognized text as message
                            sendMessage(result.text)
                        }

                        is VoiceRecognitionResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isListening = false,
                                    partialVoiceInput = "",
                                    error = result.message
                                )
                            }
                        }

                        else -> {
                            Log.d(TAG, "Voice recognition state: $result")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Voice input failed", e)
                _uiState.update {
                    it.copy(
                        isListening = false,
                        partialVoiceInput = "",
                        error = "Voice input failed: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Stop voice input
     */
    fun stopVoiceInput() {
        voiceService.stopListening()
        _uiState.update { it.copy(isListening = false, partialVoiceInput = "") }
    }

    /**
     * Speak AI response (Text-to-Speech)
     */
    fun speakMessage(text: String) {
        _uiState.update { it.copy(isSpeaking = true) }

        voiceService.speak(
            text = text,
            onStart = {
                Log.d(TAG, "Started speaking")
            },
            onDone = {
                _uiState.update { it.copy(isSpeaking = false) }
                Log.d(TAG, "Finished speaking")
            },
            onError = { error ->
                _uiState.update {
                    it.copy(isSpeaking = false, error = "TTS error: $error")
                }
                Log.e(TAG, "TTS error: $error")
            }
        )
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        voiceService.stopSpeaking()
        _uiState.update { it.copy(isSpeaking = false) }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Add message to chat
     */
    private fun addMessage(text: String, isUser: Boolean) {
        val message = ChatMessage(
            id = ++messageIdCounter,
            text = text,
            isUser = isUser,
            timestamp = dateFormat.format(Date())
        )

        _uiState.update {
            it.copy(messages = it.messages + message)
        }
    }

    /**
     * Format bytes to human-readable size
     */
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024 * 1024))} GB"
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceService.release()
    }
}
