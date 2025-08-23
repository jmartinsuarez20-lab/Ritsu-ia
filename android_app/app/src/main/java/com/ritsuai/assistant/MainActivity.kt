package com.ritsuai.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.ritsuai.assistant.databinding.ActivityMainBinding
import com.ritsuai.assistant.ui.RitsuAvatarView
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var avatarView: RitsuAvatarView
    
    // Scope para operaciones asíncronas
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.CAMERA,
        Manifest.permission.VIBRATE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.NFC,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.WRITE_SETTINGS,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.WAKE_LOCK
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        requestPermissions()
    }
    
    private fun setupUI() {
        avatarView = binding.avatarView
        
        binding.btnActivateRitsu.setOnClickListener {
            activateRitsu()
        }
        
        binding.btnSettings.setOnClickListener {
            openAccessibilitySettings()
        }
        
        binding.btnPermissions.setOnClickListener {
            openPermissionSettings()
        }
        
        binding.btnAvatarSettings.setOnClickListener {
            openAvatarCustomization()
        }
        
        // Configurar chat de prueba con IA real
        binding.btnTestChat.setOnClickListener {
            testChatWithRitsu()
        }
    }
    
    private fun requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(*requiredPermissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        initializeRitsu()
                    } else {
                        Toast.makeText(this@MainActivity, 
                            "Ritsu necesita todos los permisos para funcionar correctamente", 
                            Toast.LENGTH_LONG).show()
                    }
                }
                
                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
            })
            .check()
    }
    
    private fun initializeRitsu() {
        if (!checkAllPermissions()) {
            Toast.makeText(this, "Ritsu necesita todos los permisos para funcionar correctamente", Toast.LENGTH_LONG).show()
            return
        }
        
        // Verificar permiso de overlay
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        
        // Iniciar servicios principales
        startRitsuServices()
        
        // Mostrar que Ritsu está activa
        avatarView.setRitsuActive(true)
        binding.statusText.text = "✨ Ritsu está completamente activa y lista para vivir en tu teléfono! 🌸"
        
        // Mensaje de bienvenida con voz
        mainScope.launch {
            delay(1000)
            val welcomeMessage = "¡Hola! Soy Ritsu, tu nueva asistente personal. Estoy muy emocionada de poder ayudarte con todo lo que necesites. ¡Vamos a ser grandes compañeras!"
            voiceSystem.speak(welcomeMessage, "excited")
            
            // Mostrar expresión feliz
            avatarView.setExpression("happy")
        }
    }
    
    private fun startRitsuServices() {
        // Servicio principal de IA
        val aiServiceIntent = Intent(this, RitsuAIService::class.java)
        startForegroundService(aiServiceIntent)
        
        // Servicio de avatar kawaii flotante
        val avatarServiceIntent = Intent(this, KawaiiAvatarService::class.java)
        startForegroundService(avatarServiceIntent)
        
        Toast.makeText(this, "🚀 Todos los sistemas de Ritsu activados", Toast.LENGTH_LONG).show()
    }
    
    private fun activateRitsu() {
        if (checkPermissions()) {
            initializeRitsu()
        } else {
            requestPermissions()
        }
    }
    
    private fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
        Toast.makeText(this, 
            "Por favor activa el servicio de accesibilidad de Ritsu para WhatsApp", 
            Toast.LENGTH_LONG).show()
    }
    
    private fun testChatWithRitsu() {
        mainScope.launch {
            try {
                // Simular input del usuario
                val testInput = "Hola Ritsu, ¿cómo estás hoy?"
                
                // Crear contexto de conversación
                val context = RitsuAICore.ConversationContext(
                    platform = "test_app",
                    sender = "Usuario Test",
                    relationship = RitsuAICore.Relationship(
                        type = RitsuAICore.RelationshipType.FRIEND,
                        closeness = 0.7f,
                        interactionCount = 5,
                        lastInteraction = System.currentTimeMillis()
                    ),
                    timestamp = System.currentTimeMillis()
                )
                
                // Procesar con IA real
                val response = ritsuAICore.processUserInput(testInput, context)
                
                // Mostrar respuesta
                runOnUiThread {
                    avatarView.setExpression(response.expression)
                    binding.chatOutput.text = "Usuario: $testInput\n\nRitsu: ${response.text}"
                }
                
                // Hablar respuesta
                voiceSystem.speak(response.text, response.expression)
                
            } catch (e: Exception) {
                runOnUiThread {
                    binding.chatOutput.text = "Error al procesar con IA: ${e.message}"
                }
            }
        }
    }
    
    private fun testOutfitChange() {
        mainScope.launch {
            val outfitRequest = "Quiero un vestido rosa kawaii con flores"
            
            // Simular cambio de outfit
            avatarView.setExpression("excited")
            
            val response = "¡Oh, qué emocionante! 💕 Me encanta cambiarme de ropa. Voy a crear un hermoso vestido rosa kawaii con flores para ti. ¡Va a quedar absolutamente adorable! 🌸✨"
            
            runOnUiThread {
                binding.chatOutput.text = "Usuario: $outfitRequest\n\nRitsu: $response"
            }
            
            voiceSystem.speak(response, "excited")
            
            // Simular proceso de cambio
            delay(2000)
            avatarView.setExpression("happy")
            
            val completedMessage = "¡Listo! ¿Qué te parece mi nuevo vestido rosa? ¡Me siento tan linda! 🥰"
            voiceSystem.speak(completedMessage, "happy")
        }
    }
    
    private fun activateFloatingAvatar() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        
        val avatarServiceIntent = Intent(this, KawaiiAvatarService::class.java)
        startForegroundService(avatarServiceIntent)
        
        Toast.makeText(this, "✨ Avatar kawaii flotante activado! Ritsu aparecerá en tu pantalla", Toast.LENGTH_LONG).show()
        
        // Mensaje de activación
        voiceSystem.speak("¡Ahora puedes verme en toda tu pantalla! Voy a acompañarte en todo lo que hagas. ¡Será súper divertido!", "excited")
    }
    
    private fun openAvatarCustomization() {
        val intent = Intent(this, AvatarCustomizationActivity::class.java)
        startActivity(intent)
    }
    
    private fun openPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
        
        Toast.makeText(this, 
            "Por favor otorga TODOS los permisos para que Ritsu funcione completamente", 
            Toast.LENGTH_LONG).show()
    }
    
    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
        
        Toast.makeText(this, 
            "Ritsu necesita permiso para aparecer sobre otras aplicaciones", 
            Toast.LENGTH_LONG).show()
    }
    
    private fun checkAllPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mainScope.cancel()
        
        // Limpiar sistemas
        if (::ritsuAICore.isInitialized) {
            ritsuAICore.cleanup()
        }
        
        if (::voiceSystem.isInitialized) {
            voiceSystem.cleanup()
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // Verificar si se otorgaron permisos mientras estaba fuera
        if (checkAllPermissions() && Settings.canDrawOverlays(this)) {
            binding.btnActivateRitsu.text = "✨ Ritsu Lista"
            binding.btnActivateRitsu.isEnabled = true
        }
    }
}