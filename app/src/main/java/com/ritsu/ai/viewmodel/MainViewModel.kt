package com.ritsu.ai.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritsu.ai.data.model.ChatMessage
import com.ritsu.ai.data.model.UserPreferences
import com.ritsu.ai.data.repository.ChatRepository
import com.ritsu.ai.data.repository.UserPreferencesRepository
import com.ritsu.ai.service.RitsuAIService
import com.ritsu.ai.util.AIEngine
import com.ritsu.ai.util.AvatarManager
import com.ritsu.ai.util.ClothingGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {
    
    // Estados observables
    private val _permissionsState = MutableLiveData<Boolean>(false)
    val permissionsState: LiveData<Boolean> = _permissionsState
    
    private val _accessibilityState = MutableLiveData<Boolean>(false)
    val accessibilityState: LiveData<Boolean> = _accessibilityState
    
    private val _overlayState = MutableLiveData<Boolean>(false)
    val overlayState: LiveData<Boolean> = _overlayState
    
    private val _ritsuState = MutableLiveData<Boolean>(false)
    val ritsuState: LiveData<Boolean> = _ritsuState
    
    private val _servicesState = MutableLiveData<Boolean>(false)
    val servicesState: LiveData<Boolean> = _servicesState
    
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages
    
    private val _avatarState = MutableLiveData<AvatarState>()
    val avatarState: LiveData<AvatarState> = _avatarState
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    // Repositorios
    private lateinit var chatRepository: ChatRepository
    private lateinit var userPreferencesRepository: UserPreferencesRepository
    
    // Utilidades
    private lateinit var aiEngine: AIEngine
    private lateinit var avatarManager: AvatarManager
    private lateinit var clothingGenerator: ClothingGenerator
    
    // Estado interno
    private var currentConversationId: String? = null
    private var userPreferences: UserPreferences? = null
    
    fun initialize(
        context: Context,
        chatRepo: ChatRepository,
        userPrefRepo: UserPreferencesRepository,
        ai: AIEngine,
        avatar: AvatarManager,
        clothing: ClothingGenerator
    ) {
        chatRepository = chatRepo
        userPreferencesRepository = userPrefRepo
        aiEngine = ai
        avatarManager = avatar
        clothingGenerator = clothing
        
        // Cargar preferencias del usuario
        loadUserPreferences()
        
        // Cargar historial de chat
        loadChatHistory()
        
        // Inicializar avatar
        initializeAvatar()
    }
    
    // Gestión de permisos
    fun updatePermissionsState(state: Boolean) {
        _permissionsState.value = state
        updateServicesState()
    }
    
    fun updateAccessibilityState(state: Boolean) {
        _accessibilityState.value = state
        updateServicesState()
    }
    
    fun updateOverlayState(state: Boolean) {
        _overlayState.value = state
        updateServicesState()
    }
    
    private fun updateServicesState() {
        val allReady = _permissionsState.value == true &&
                _accessibilityState.value == true &&
                _overlayState.value == true
        _servicesState.value = allReady
    }
    
    // Gestión de Ritsu
    fun startRitsuAI() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Iniciar servicio de IA
                RitsuAIService.startService(aiEngine.getContext())
                
                // Actualizar estado
                _ritsuState.value = true
                
                // Generar mensaje de bienvenida
                generateWelcomeMessage()
                
            } catch (e: Exception) {
                // Manejar error
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun stopRitsuAI() {
        viewModelScope.launch {
            try {
                // Detener servicio de IA
                RitsuAIService.stopService(aiEngine.getContext())
                
                // Actualizar estado
                _ritsuState.value = false
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun isRitsuRunning(): Boolean {
        return _ritsuState.value == true
    }
    
    // Gestión del chat
    fun sendMessage(message: String) {
        viewModelScope.launch {
            try {
                // Crear mensaje del usuario
                val userMessage = ChatMessage(
                    id = System.currentTimeMillis().toString(),
                    content = message,
                    sender = ChatMessage.Sender.USER,
                    timestamp = System.currentTimeMillis(),
                    conversationId = currentConversationId ?: createNewConversation()
                )
                
                // Agregar mensaje del usuario al chat
                addMessageToChat(userMessage)
                
                // Guardar en base de datos
                chatRepository.saveMessage(userMessage)
                
                // Generar respuesta de Ritsu
                generateRitsuResponse(message)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private suspend fun generateRitsuResponse(userMessage: String) {
        try {
            // Mostrar indicador de escritura
            showTypingIndicator()
            
            // Generar respuesta usando el motor de IA
            val response = aiEngine.generateResponse(userMessage, userPreferences)
            
            // Crear mensaje de Ritsu
            val ritsuMessage = ChatMessage(
                id = System.currentTimeMillis().toString(),
                content = response,
                sender = ChatMessage.Sender.RITSU,
                timestamp = System.currentTimeMillis(),
                conversationId = currentConversationId
            )
            
            // Agregar respuesta al chat
            addMessageToChat(ritsuMessage)
            
            // Guardar en base de datos
            chatRepository.saveMessage(ritsuMessage)
            
            // Actualizar avatar según el contexto
            updateAvatarForResponse(response)
            
        } catch (e: Exception) {
            e.printStackTrace()
            // Mostrar mensaje de error
            showErrorMessage()
        } finally {
            hideTypingIndicator()
        }
    }
    
    private fun addMessageToChat(message: ChatMessage) {
        val currentMessages = _chatMessages.value?.toMutableList() ?: mutableListOf()
        currentMessages.add(message)
        _chatMessages.value = currentMessages
    }
    
    private fun showTypingIndicator() {
        // Implementar indicador de escritura
    }
    
    private fun hideTypingIndicator() {
        // Ocultar indicador de escritura
    }
    
    private fun showErrorMessage() {
        val errorMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            content = "Lo siento, tuve un problema procesando tu mensaje. ¿Podrías intentarlo de nuevo?",
            sender = ChatMessage.Sender.RITSU,
            timestamp = System.currentTimeMillis(),
            conversationId = currentConversationId
        )
        addMessageToChat(errorMessage)
    }
    
    // Gestión del avatar
    fun changeAvatarClothing(description: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Generar nueva ropa
                val clothing = clothingGenerator.generateClothing(description)
                
                // Aplicar al avatar
                avatarManager.changeClothing(clothing)
                
                // Guardar preferencia
                userPreferences?.let { prefs ->
                    prefs.currentClothing = clothing.id
                    userPreferencesRepository.savePreferences(prefs)
                }
                
                // Actualizar estado del avatar
                updateAvatarState()
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun unlockSpecialMode(keyword: String) {
        if (keyword == "ritsu_sin_censura_2024") {
            viewModelScope.launch {
                try {
                    // Habilitar modo especial
                    avatarManager.enableSpecialMode()
                    
                    // Actualizar preferencias
                    userPreferences?.let { prefs ->
                        prefs.specialModeUnlocked = true
                        userPreferencesRepository.savePreferences(prefs)
                    }
                    
                    // Actualizar estado
                    updateAvatarState()
                    
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    // Carga de datos
    private fun loadUserPreferences() {
        viewModelScope.launch {
            try {
                userPreferences = userPreferencesRepository.getPreferences()
                
                // Aplicar preferencias al avatar
                userPreferences?.let { prefs ->
                    avatarManager.applyPreferences(prefs)
                    updateAvatarState()
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val messages = chatRepository.getRecentMessages(50)
                _chatMessages.value = messages
                
                // Establecer conversación actual
                if (messages.isNotEmpty()) {
                    currentConversationId = messages.last().conversationId
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun initializeAvatar() {
        viewModelScope.launch {
            try {
                avatarManager.initialize()
                updateAvatarState()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateAvatarState() {
        viewModelScope.launch {
            try {
                val state = avatarManager.getCurrentState()
                _avatarState.value = state
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateAvatarForResponse(response: String) {
        viewModelScope.launch {
            try {
                // Analizar respuesta para determinar expresión del avatar
                val emotion = aiEngine.analyzeEmotion(response)
                avatarManager.setEmotion(emotion)
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun generateWelcomeMessage() {
        val welcomeMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            content = "¡Hola! Soy Ritsu, tu asistente personal. Estoy aquí para ayudarte con todo lo que necesites en tu dispositivo. ¿En qué puedo ayudarte hoy?",
            sender = ChatMessage.Sender.RITSU,
            timestamp = System.currentTimeMillis(),
            conversationId = currentConversationId
        )
        addMessageToChat(welcomeMessage)
    }
    
    private fun createNewConversation(): String {
        val conversationId = "conv_${System.currentTimeMillis()}"
        currentConversationId = conversationId
        return conversationId
    }
    
    override fun onCleared() {
        super.onCleared()
        // Limpiar recursos si es necesario
    }
    
    // Estados del avatar
    data class AvatarState(
        val currentClothing: String = "",
        val emotion: String = "neutral",
        val isSpecialModeEnabled: Boolean = false,
        val position: Position = Position.CENTER,
        val isVisible: Boolean = true
    )
    
    data class Position(
        val x: Float = 0.5f,
        val y: Float = 0.5f
    )
}