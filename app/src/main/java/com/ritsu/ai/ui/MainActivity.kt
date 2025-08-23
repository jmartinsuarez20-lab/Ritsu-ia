package com.ritsu.ai.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ritsu.ai.R
import com.ritsu.ai.RitsuApplication
import com.ritsu.ai.util.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    private val scope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        preferenceManager = PreferenceManager(this)
        
        // Verificar si es la primera ejecución
        if (preferenceManager.isFirstTimeSetup()) {
            setupFirstTime()
        } else {
            checkPermissionsAndStart()
        }
    }
    
    private fun setupFirstTime() {
        // Mostrar tutorial de configuración
        showSetupTutorial()
    }
    
    private fun showSetupTutorial() {
        // Por ahora, mostrar un mensaje simple
        Toast.makeText(this, "¡Bienvenido a Ritsu AI! Configurando...", Toast.LENGTH_LONG).show()
        
        // Marcar como configurado
        preferenceManager.setFirstTimeSetup(false)
        
        // Continuar con la verificación de permisos
        checkPermissionsAndStart()
    }
    
    private fun checkPermissionsAndStart() {
        scope.launch {
            // Verificar permisos críticos
            if (!checkAccessibilityPermission()) {
                requestAccessibilityPermission()
                return@launch
            }
            
            if (!checkOverlayPermission()) {
                requestOverlayPermission()
                return@launch
            }
            
            // Todos los permisos están concedidos, iniciar servicios
            startRitsuServices()
        }
    }
    
    private fun checkAccessibilityPermission(): Boolean {
        val accessibilityEnabled = Settings.Secure.getInt(
            contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED, 0
        )
        
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            return services?.contains("com.ritsu.ai/.service.RitsuAccessibilityService") == true
        }
        return false
    }
    
    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }
    
    private fun requestAccessibilityPermission() {
        Toast.makeText(
            this,
            "Se requiere permiso de accesibilidad para que Ritsu funcione",
            Toast.LENGTH_LONG
        ).show()
        
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    private fun requestOverlayPermission() {
        Toast.makeText(
            this,
            "Se requiere permiso de superposición para mostrar el avatar",
            Toast.LENGTH_LONG
        ).show()
        
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
        startActivity(intent)
    }
    
    private fun startRitsuServices() {
        // Iniciar servicios de Ritsu
        val application = application as RitsuApplication
        application.startRitsuServices()
        
        // Mostrar mensaje de bienvenida
        Toast.makeText(
            this,
            "¡Ritsu está lista para ayudarte!",
            Toast.LENGTH_LONG
        ).show()
        
        // Cerrar la actividad principal
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        
        // Verificar permisos nuevamente cuando se regrese a la app
        if (!preferenceManager.isFirstTimeSetup()) {
            checkPermissionsAndStart()
        }
    }
}