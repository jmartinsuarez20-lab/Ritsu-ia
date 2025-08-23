package com.ritsu.ai

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import com.ritsu.ai.databinding.ActivityMainBinding
import com.ritsu.ai.services.RitsuAIService
import com.ritsu.ai.services.RitsuFloatingService
import com.ritsu.ai.ui.RitsuAvatarView
import com.ritsu.ai.util.PreferenceManager
import com.ritsu.ai.util.AIManager
import com.ritsu.ai.util.AvatarManager
import com.ritsu.ai.util.ClothingGenerator
import com.ritsu.ai.util.LearningManager

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var avatarView: RitsuAvatarView
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var aiManager: AIManager
    private lateinit var avatarManager: AvatarManager
    private lateinit var clothingGenerator: ClothingGenerator
    private lateinit var learningManager: LearningManager
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS,
        Manifest.permission.SYSTEM_ALERT_WINDOW,
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        initializeManagers()
        setupUI()
        requestPermissions()
    }
    
    private fun initializeManagers() {
        preferenceManager = PreferenceManager(this)
        aiManager = AIManager(this)
        avatarManager = AvatarManager(this)
        clothingGenerator = ClothingGenerator(this)
        learningManager = LearningManager(this)
    }
    
    private fun setupUI() {
        avatarView = binding.avatarView
        
        binding.btnActivateRitsu.setOnClickListener {
            activateRitsu()
        }
        
        binding.btnSettings.setOnClickListener {
            openAccessibilitySettings()
        }
        
        // Configurar chat de prueba
        binding.btnTestChat.setOnClickListener {
            testChatWithRitsu()
        }
        
        // Configurar modo especial
        binding.btnSpecialMode.setOnClickListener {
            showSpecialModeDialog()
        }
        
        // Configurar generación de ropa
        binding.btnGenerateClothing.setOnClickListener {
            showClothingGenerationDialog()
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
        // Iniciar el servicio de IA
        val serviceIntent = Intent(this, RitsuAIService::class.java)
        startForegroundService(serviceIntent)
        
        // Mostrar que Ritsu está activa
        avatarView.setRitsuActive(true)
        binding.statusText.text = "Ritsu está despierta y lista para ayudarte! 🌸"
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
        avatarView.showExpression("talking")
        binding.chatOutput.text = "Ritsu: ¡Hola! Soy Ritsu, tu asistente personal. Esté aquí para ayudarte con lo que necesites. ¿Cómo puedo asistirte hoy? 🌸"
    }
    
    private fun showSpecialModeDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Modo Especial de Ritsu")
            .setMessage("Ingresa la contraseña para desbloquear funcionalidades especiales:")
            .setView(android.widget.EditText(this).apply {
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                hint = "Contraseña"
            })
            .setPositiveButton("Desbloquear") { dialog, _ ->
                val password = (dialog as android.app.AlertDialog)
                    .findViewById<android.widget.EditText>(android.R.id.text1)?.text.toString()
                if (password == "262456") {
                    preferenceManager.putBoolean("special_mode_enabled", true)
                    avatarManager.toggleSpecialMode(true)
                    Toast.makeText(this, "¡Modo especial activado! 🌸", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }
    
    private fun showClothingGenerationDialog() {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Generar Ropa para Ritsu")
            .setMessage("Describe la ropa que quieres que Ritsu use:")
            .setView(android.widget.EditText(this).apply {
                hint = "Ej: vestido elegante rosa con flores"
                inputType = android.text.InputType.TYPE_CLASS_TEXT
            })
            .setPositiveButton("Generar") { dialog, _ ->
                val description = (dialog as android.app.AlertDialog)
                    .findViewById<android.widget.EditText>(android.R.id.text1)?.text.toString()
                if (description.isNotEmpty()) {
                    generateClothing(description)
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }
    
    private fun generateClothing(description: String) {
        // Simular generación de ropa
        val clothing = clothingGenerator.generateClothingFromDescription(description)
        avatarManager.updateClothing(clothing)
        binding.chatOutput.text = "Ritsu: ¡Perfecto! He generado una nueva prenda basada en tu descripción: '$description'. ¿Te gusta cómo me veo? 🌸"
    }
    
    private fun initializeRitsu() {
        // Iniciar el servicio de IA
        val serviceIntent = Intent(this, RitsuAIService::class.java)
        startForegroundService(serviceIntent)
        
        // Iniciar servicio flotante si está habilitado
        if (preferenceManager.isOverlayEnabled()) {
            val floatingIntent = Intent(this, RitsuFloatingService::class.java)
            startForegroundService(floatingIntent)
        }
        
        // Mostrar que Ritsu está activa
        avatarView.setRitsuActive(true)
        binding.statusText.text = "Ritsu está despierta y lista para ayudarte! 🌸"
        
        // Inicializar aprendizaje
        learningManager.initialize()
    }
}