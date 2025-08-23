package com.ritsuai.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState
    
    init {
        // Inicializar estado
    }
    
    fun updatePermissionStatus(permission: String, granted: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                permissions = _uiState.value.permissions + (permission to granted)
            )
        }
    }
    
    fun updateAvatarStatus(visible: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                avatarVisible = visible
            )
        }
    }
}

data class MainUiState(
    val permissions: Map<String, Boolean> = emptyMap(),
    val avatarVisible: Boolean = false,
    val isLoading: Boolean = false
)