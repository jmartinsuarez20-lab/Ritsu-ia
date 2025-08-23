# 🚀 Instrucciones de Compilación - Ritsu AI

## 📋 Requisitos Previos

### Software Necesario
- **Android Studio** (versión Hedgehog o superior)
- **Java Development Kit (JDK)** 17 o superior
- **Android SDK** API 26+ (Android 8.0+)
- **Gradle** 8.0+

### Hardware Recomendado
- **RAM**: 8GB mínimo, 16GB recomendado
- **Almacenamiento**: 10GB de espacio libre
- **Procesador**: Intel i5/AMD Ryzen 5 o superior

## 🔧 Pasos de Compilación

### 1. Preparar el Entorno
```bash
# Clonar el repositorio
git clone [URL_DEL_REPOSITORIO]
cd ritsu-ai

# Verificar que tienes Java 17+
java -version
```

### 2. Abrir en Android Studio
1. Abrir **Android Studio**
2. Seleccionar **"Open an existing Android Studio project"**
3. Navegar a la carpeta del proyecto y seleccionarla
4. Esperar a que se sincronice el proyecto

### 3. Configurar Variables de Entorno
```bash
# En tu archivo ~/.bashrc o ~/.zshrc
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

### 4. Sincronizar Dependencias
1. En Android Studio, ir a **File > Sync Project with Gradle Files**
2. O hacer clic en el botón **"Sync Now"** que aparece
3. Esperar a que se descarguen todas las dependencias

### 5. Configurar API Key de OpenAI
1. Crear archivo `local.properties` en la raíz del proyecto
2. Agregar tu clave de OpenAI:
```properties
OPENAI_API_KEY=tu_clave_aqui
```

### 6. Compilar el Proyecto
```bash
# Desde la línea de comandos
./gradlew assembleDebug

# O desde Android Studio
# Build > Make Project
```

### 7. Generar APK
```bash
# APK de debug
./gradlew assembleDebug

# APK de release (requiere configuración de signing)
./gradlew assembleRelease
```

## 📱 Instalación en Dispositivo

### 1. Habilitar Fuentes Desconocidas
1. Ir a **Configuración > Seguridad**
2. Activar **"Fuentes desconocidas"**

### 2. Instalar APK
1. Transferir el APK al dispositivo
2. Abrir el archivo APK
3. Seguir las instrucciones de instalación

### 3. Configurar Permisos
1. **Accesibilidad**: Configuración > Accesibilidad > Ritsu AI
2. **Superposición**: Configuración > Aplicaciones > Ritsu AI > Permisos
3. **Administrador**: Configuración > Seguridad > Administradores de dispositivo

## 🛠️ Solución de Problemas

### Error: "SDK location not found"
```bash
# Crear archivo local.properties
echo "sdk.dir=$HOME/Android/Sdk" > local.properties
```

### Error: "Failed to resolve dependencies"
```bash
# Limpiar cache de Gradle
./gradlew clean
./gradlew --refresh-dependencies
```

### Error: "OutOfMemoryError"
```bash
# Aumentar memoria de Gradle en gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Error: "API level not found"
1. Abrir **SDK Manager** en Android Studio
2. Instalar **Android 8.0 (API 26)** o superior
3. Sincronizar proyecto

## 📦 Estructura del APK Generado

```
app/build/outputs/apk/debug/
├── app-debug.apk          # APK principal
└── app-debug.apk.idsig   # Firma del APK
```

## 🔍 Verificar Compilación

### 1. Logs de Compilación
```bash
# Ver logs detallados
./gradlew assembleDebug --info

# Ver logs completos
./gradlew assembleDebug --debug
```

### 2. Verificar APK
```bash
# Verificar contenido del APK
aapt dump badging app-debug.apk

# Verificar permisos
aapt dump permissions app-debug.apk
```

## 🚨 Notas Importantes

### ⚠️ Permisos Especiales
- Esta aplicación requiere **permisos de accesibilidad**
- Necesita **permisos de superposición** para el avatar flotante
- Requiere **permisos de administrador** para control total

### 🔒 Seguridad
- **NO** instalar en dispositivos de producción sin revisar
- **NO** usar en dispositivos con datos sensibles
- **SÍ** probar en dispositivo de desarrollo

### 📱 Compatibilidad
- **Android mínimo**: 8.0 (API 26)
- **Android objetivo**: 14 (API 34)
- **Arquitecturas**: ARM64, ARM32, x86_64

## 🎯 Próximos Pasos

1. **Probar en dispositivo real**
2. **Configurar servicios de accesibilidad**
3. **Personalizar avatar y ropa**
4. **Configurar OpenAI API**
5. **Probar funcionalidades avanzadas**

## 📞 Soporte

Si encuentras problemas durante la compilación:

1. **Revisar logs** de Android Studio
2. **Verificar dependencias** en build.gradle
3. **Comprobar SDK** y herramientas
4. **Consultar documentación** oficial de Android

---

**¡Feliz compilación! 🎉**