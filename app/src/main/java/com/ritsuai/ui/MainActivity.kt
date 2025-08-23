package com.ritsuai.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ritsuai.RitsuAIApplication
import com.ritsuai.services.AIService
import com.ritsuai.services.FloatingAvatarService
import com.ritsuai.utils.PreferenceManager
import com.ritsuai.viewmodels.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var speechRecognizer: SpeechRecognizer
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            setupServices()
        } else {
            showPermissionError()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferenceManager = PreferenceManager(this)
        
        // Verificar permisos
        checkAndRequestPermissions()
        
        // Configurar reconocimiento de voz
        setupSpeechRecognition()
        
        setContent {
            RitsuAITheme {
                MainScreen(
                    onPermissionRequest = { requestPermissions() },
                    onStartFloatingAvatar = { startFloatingAvatar() },
                    onStopFloatingAvatar = { stopFloatingAvatar() }
                )
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        // Permisos básicos
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        
        // Permisos de comunicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.SEND_SMS)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CALL_PHONE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CONTACTS)
        }
        
        if (permissions.isNotEmpty()) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        } else {
            setupServices()
        }
    }
    
    private fun requestPermissions() {
        checkAndRequestPermissions()
    }
    
    private fun setupServices() {
        // Inicializar servicios de IA
        val app = application as RitsuAIApplication
        app.scope.launch {
            AIService.initialize(this@MainActivity)
            FloatingAvatarService.initialize(this@MainActivity)
        }
    }
    
    private fun setupSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {}
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.firstOrNull()?.let { text ->
                        // Procesar comando de voz
                        processVoiceCommand(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }
    
    private fun processVoiceCommand(command: String) {
        // Procesar comando de voz con la IA
        val app = application as RitsuAIApplication
        app.scope.launch {
            val response = AIService.getInstance().generateResponse(command, "voice_command", true)
            Toast.makeText(this@MainActivity, response, Toast.LENGTH_LONG).show()
        }
    }
    
    private fun startFloatingAvatar() {
        // Verificar permisos de superposición
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        } else {
            val app = application as RitsuAIApplication
            app.scope.launch {
                FloatingAvatarService.initialize(this@MainActivity)
            }
        }
    }
    
    private fun stopFloatingAvatar() {
        FloatingAvatarService.getInstance()?.hideAvatar()
    }
    
    private fun showPermissionError() {
        Toast.makeText(
            this,
            "Se requieren permisos para que Ritsu funcione correctamente",
            Toast.LENGTH_LONG
        ).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

@Composable
fun MainScreen(
    onPermissionRequest: () -> Unit,
    onStartFloatingAvatar: () -> Unit,
    onStopFloatingAvatar: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var showChat by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showClothing by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "🤖 Ritsu AI",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Estado del sistema
            SystemStatusCard(
                onPermissionRequest = onPermissionRequest
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones principales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MainActionButton(
                    text = "💬 Chat",
                    onClick = { showChat = true }
                )
                
                MainActionButton(
                    text = "👗 Ropa",
                    onClick = { showClothing = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MainActionButton(
                    text = "🎭 Avatar",
                    onClick = onStartFloatingAvatar
                )
                
                MainActionButton(
                    text = "⚙️ Ajustes",
                    onClick = { showSettings = true }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control del avatar flotante
            AvatarControlCard(
                onStart = onStartFloatingAvatar,
                onStop = onStopFloatingAvatar
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del sistema
            SystemInfoCard()
        }
        
        // Diálogos
        if (showChat) {
            ChatDialog(
                onDismiss = { showChat = false }
            )
        }
        
        if (showSettings) {
            SettingsDialog(
                onDismiss = { showSettings = false }
            )
        }
        
        if (showClothing) {
            ClothingDialog(
                onDismiss = { showClothing = false }
            )
        }
    }
}

@Composable
fun SystemStatusCard(onPermissionRequest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Estado del Sistema",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Permisos básicos")
                Text(
                    text = "✅ Concedidos",
                    color = Color.Green
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Servicio de accesibilidad")
                Text(
                    text = "❌ No activado",
                    color = Color.Red
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Permiso de superposición")
                Text(
                    text = "❌ No concedido",
                    color = Color.Red
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onPermissionRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar Permisos")
            }
        }
    }
}

@Composable
fun MainActionButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .width(120.dp)
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AvatarControlCard(
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Control del Avatar",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green
                    )
                ) {
                    Text("Mostrar Avatar")
                }
                
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Ocultar Avatar")
                }
            }
        }
    }
}

@Composable
fun SystemInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información del Sistema",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text("Ritsu AI v1.0")
            Text("Android ${Build.VERSION.RELEASE}")
            Text("API Level ${Build.VERSION.SDK_INT}")
        }
    }
}

@Composable
fun ChatDialog(onDismiss: () -> Unit) {
    // Implementar diálogo de chat
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chat con Ritsu") },
        text = { Text("Aquí irá el chat con Ritsu") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun SettingsDialog(onDismiss: () -> Unit) {
    // Implementar diálogo de ajustes
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes") },
        text = { Text("Aquí irán los ajustes de Ritsu") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun ClothingDialog(onDismiss: () -> Unit) {
    // Implementar diálogo de ropa
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generador de Ropa") },
        text = { Text("Aquí podrás generar ropa para Ritsu") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun RitsuAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
        content = content
    )
}