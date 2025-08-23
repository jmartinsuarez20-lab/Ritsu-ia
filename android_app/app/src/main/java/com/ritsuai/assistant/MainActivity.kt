package com.ritsuai.assistant

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
import com.ritsuai.assistant.databinding.ActivityMainBinding
import com.ritsuai.assistant.services.RitsuAIService
import com.ritsuai.assistant.ui.RitsuAvatarView

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var avatarView: RitsuAvatarView
    
    private val requiredPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
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
        
        // Configurar chat de prueba
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
}