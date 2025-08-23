package com.ritsu.ai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.ritsu.ai.R
import com.ritsu.ai.databinding.ActivityMainBinding
import com.ritsu.ai.service.RitsuAccessibilityService
import com.ritsu.ai.service.RitsuOverlayService
import com.ritsu.ai.util.PermissionManager
import com.ritsu.ai.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var permissionManager: PermissionManager
    
    // Contrato para solicitar permisos
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            onAllPermissionsGranted()
        } else {
            showPermissionDeniedMessage()
        }
    }
    
    // Contrato para configuración de accesibilidad
    private val accessibilitySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (RitsuAccessibilityService.isEnabled()) {
            onAccessibilityEnabled()
        } else {
            showAccessibilityRequiredMessage()
        }
    }
    
    // Contrato para configuración de superposición
    private val overlaySettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            onOverlayPermissionGranted()
        } else {
            showOverlayPermissionRequiredMessage()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        
        // Inicializar gestor de permisos
        permissionManager = PermissionManager(this)
        
        // Configurar UI
        setupUI()
        
        // Configurar observadores
        setupObservers()
        
        // Verificar estado inicial
        checkInitialState()
    }
    
    private fun setupUI() {
        // Configurar botones
        binding.btnStartRitsu.setOnClickListener {
            startRitsu()
        }
        
        binding.btnSettings.setOnClickListener {
            openSettings()
        }
        
        binding.btnPermissions.setOnClickListener {
            requestPermissions()
        }
        
        binding.btnAccessibility.setOnClickListener {
            openAccessibilitySettings()
        }
        
        binding.btnOverlay.setOnClickListener {
            openOverlaySettings()
        }
        
        // Configurar chat
        binding.btnSendMessage.setOnClickListener {
            sendMessage()
        }
        
        // Configurar avatar
        binding.btnChangeClothes.setOnClickListener {
            openClothingMenu()
        }
        
        binding.btnAvatarSettings.setOnClickListener {
            openAvatarSettings()
        }
    }
    
    private fun setupObservers() {
        // Observar estado de permisos
        viewModel.permissionsState.observe(this) { state ->
            updatePermissionsUI(state)
        }
        
        // Observar estado de servicios
        viewModel.servicesState.observe(this) { state ->
            updateServicesUI(state)
        }
        
        // Observar mensajes del chat
        viewModel.chatMessages.observe(this) { messages ->
            updateChatUI(messages)
        }
        
        // Observar estado del avatar
        viewModel.avatarState.observe(this) { state ->
            updateAvatarUI(state)
        }
    }
    
    private fun checkInitialState() {
        // Verificar permisos básicos
        if (permissionManager.hasBasicPermissions()) {
            viewModel.updatePermissionsState(true)
        }
        
        // Verificar servicio de accesibilidad
        if (RitsuAccessibilityService.isEnabled()) {
            viewModel.updateAccessibilityState(true)
        }
        
        // Verificar permiso de superposición
        if (Settings.canDrawOverlays(this)) {
            viewModel.updateOverlayState(true)
        }
        
        // Verificar si Ritsu está ejecutándose
        if (viewModel.isRitsuRunning()) {
            viewModel.updateRitsuState(true)
        }
    }
    
    private fun startRitsu() {
        if (!checkPrerequisites()) {
            return
        }
        
        try {
            // Iniciar servicio de superposición
            RitsuOverlayService.startService(this)
            
            // Iniciar servicio de IA
            viewModel.startRitsuAI()
            
            // Actualizar UI
            viewModel.updateRitsuState(true)
            
            // Mostrar mensaje de bienvenida
            showWelcomeMessage()
            
            // Minimizar la aplicación
            moveTaskToBack(true)
            
        } catch (e: Exception) {
            showError("Error al iniciar Ritsu: ${e.message}")
        }
    }
    
    private fun checkPrerequisites(): Boolean {
        if (!permissionManager.hasBasicPermissions()) {
            showPermissionRequiredMessage()
            return false
        }
        
        if (!RitsuAccessibilityService.isEnabled()) {
            showAccessibilityRequiredMessage()
            return false
        }
        
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionRequiredMessage()
            return false
        }
        
        return true
    }
    
    private fun requestPermissions() {
        val permissions = permissionManager.getRequiredPermissions()
        requestPermissionLauncher.launch(permissions.toTypedArray())
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        accessibilitySettingsLauncher.launch(intent)
    }
    
    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        overlaySettingsLauncher.launch(intent)
    }
    
    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun openClothingMenu() {
        val intent = Intent(this, ClothingActivity::class.java)
        startActivity(intent)
    }
    
    private fun openAvatarSettings() {
        val intent = Intent(this, AvatarSettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun sendMessage() {
        val message = binding.etMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            viewModel.sendMessage(message)
            binding.etMessage.text?.clear()
        }
    }
    
    private fun onAllPermissionsGranted() {
        viewModel.updatePermissionsState(true)
        Toast.makeText(this, "Todos los permisos concedidos", Toast.LENGTH_SHORT).show()
    }
    
    private fun onAccessibilityEnabled() {
        viewModel.updateAccessibilityState(true)
        Toast.makeText(this, "Servicio de accesibilidad habilitado", Toast.LENGTH_SHORT).show()
    }
    
    private fun onOverlayPermissionGranted() {
        viewModel.updateOverlayState(true)
        Toast.makeText(this, "Permiso de superposición concedido", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPermissionDeniedMessage() {
        Toast.makeText(this, "Se requieren todos los permisos para que Ritsu funcione", Toast.LENGTH_LONG).show()
    }
    
    private fun showAccessibilityRequiredMessage() {
        Toast.makeText(this, "Se requiere habilitar el servicio de accesibilidad", Toast.LENGTH_LONG).show()
    }
    
    private fun showOverlayPermissionRequiredMessage() {
        Toast.makeText(this, "Se requiere permiso para superponer en pantalla", Toast.LENGTH_LONG).show()
    }
    
    private fun showPermissionRequiredMessage() {
        Toast.makeText(this, "Se requieren permisos para continuar", Toast.LENGTH_SHORT).show()
    }
    
    private fun showWelcomeMessage() {
        Toast.makeText(this, "¡Ritsu está ahora viva en tu teléfono! 🎉", Toast.LENGTH_LONG).show()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    private fun updatePermissionsUI(state: Boolean) {
        binding.tvPermissionsStatus.text = if (state) "✅ Concedidos" else "❌ Pendientes"
        binding.btnPermissions.visibility = if (state) View.GONE else View.VISIBLE
    }
    
    private fun updateServicesUI(state: Boolean) {
        binding.tvServicesStatus.text = if (state) "✅ Activos" else "❌ Inactivos"
        binding.btnStartRitsu.isEnabled = state
    }
    
    private fun updateChatUI(messages: List<String>) {
        // Actualizar lista de mensajes del chat
        // Implementar según la UI específica
    }
    
    private fun updateAvatarUI(state: Any) {
        // Actualizar estado del avatar
        // Implementar según la UI específica
    }
    
    override fun onResume() {
        super.onResume()
        // Verificar estado actualizado de permisos y servicios
        checkInitialState()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Limpiar recursos si es necesario
    }
}