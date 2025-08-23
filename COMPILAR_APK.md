# 🔧 COMPILAR APK RITSU AI - PASO A PASO

## 📋 **REQUISITOS PREVIOS**

### **Software Necesario:**
- ✅ **Android Studio** (versión Hedgehog o superior)
- ✅ **Java JDK 17** o superior
- ✅ **Android SDK** API 26+ (Android 8.0+)
- ✅ **10GB espacio libre** en disco

### **Descargas:**
- **Android Studio**: https://developer.android.com/studio
- **Java JDK**: https://adoptium.net/ (versión 17+)

## 🚀 **PASO 1: PREPARAR EL ENTORNO**

### **1.1 Instalar Android Studio:**
1. Descargar Android Studio
2. Ejecutar instalador
3. Seguir wizard de instalación
4. **IMPORTANTE**: Instalar Android SDK durante la instalación

### **1.2 Verificar Java:**
```bash
java -version
# Debe mostrar Java 17 o superior
```

### **1.3 Configurar Variables de Entorno:**
```bash
# En Windows (cmd):
set ANDROID_HOME=C:\Users\TuUsuario\AppData\Local\Android\Sdk
set PATH=%PATH%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools

# En macOS/Linux:
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## 🚀 **PASO 2: ABRIR EL PROYECTO**

### **2.1 Abrir Android Studio:**
1. Lanzar Android Studio
2. Seleccionar **"Open an existing Android Studio project"**
3. Navegar a la carpeta `android_app`
4. Seleccionar la carpeta y hacer clic en **"OK"**

### **2.2 Esperar Sincronización:**
- Android Studio descargará Gradle
- Sincronizará dependencias
- Configurará el proyecto
- **Tiempo estimado**: 5-15 minutos

## 🚀 **PASO 3: CONFIGURAR OPENAI API**

### **3.1 Crear Archivo local.properties:**
1. En Android Studio, ir a **Project View**
2. Expandir **Gradle Scripts**
3. Hacer doble clic en **local.properties**
4. Agregar tu clave de OpenAI:

```properties
## This file must *NOT* be checked into Version Control Systems,
# as it contains information specific to your local configuration.
#
# Location of the SDK. This is only used by Gradle.
sdk.dir=C\:\\Users\\TuUsuario\\AppData\\Local\\Android\\Sdk

# OpenAI API Key
OPENAI_API_KEY=tu_clave_aqui
```

### **3.2 Obtener Clave de OpenAI:**
1. Ir a https://platform.openai.com/
2. Crear cuenta o iniciar sesión
3. Ir a **API Keys**
4. Crear nueva clave
5. Copiar y pegar en `local.properties`

## 🚀 **PASO 4: COMPILAR EL PROYECTO**

### **4.1 Sincronizar Dependencias:**
1. En Android Studio, ir a **File > Sync Project with Gradle Files**
2. O hacer clic en el botón **"Sync Now"** (icono de elefante)
3. Esperar a que se complete la sincronización

### **4.2 Compilar desde Android Studio:**
1. **Build > Make Project** (Ctrl+F9)
2. Esperar a que se complete la compilación
3. Verificar que no hay errores en **Build Output**

### **4.3 Compilar desde Línea de Comandos:**
```bash
# En la carpeta del proyecto:
cd android_app

# Compilar APK de debug:
./gradlew assembleDebug

# O en Windows:
gradlew.bat assembleDebug
```

## 🚀 **PASO 5: ENCONTRAR EL APK**

### **5.1 Ubicación del APK:**
```
android_app/app/build/outputs/apk/debug/app-debug.apk
```

### **5.2 Verificar que se Generó:**
1. En Android Studio, ir a **Project View**
2. Expandir **app > build > outputs > apk > debug**
3. Deberías ver `app-debug.apk`

### **5.3 Tamaño Esperado:**
- **APK de debug**: 15-25 MB
- **APK de release**: 10-20 MB

## 🚀 **PASO 6: INSTALAR EN DISPOSITIVO**

### **6.1 Habilitar Fuentes Desconocidas:**
1. En tu dispositivo Android
2. **Configuración > Seguridad**
3. Activar **"Fuentes desconocidas"**

### **6.2 Transferir APK:**
1. Copiar `app-debug.apk` a tu dispositivo
2. Usar USB, email, o servicio en la nube

### **6.3 Instalar APK:**
1. Abrir archivo APK en tu dispositivo
2. Seguir instrucciones de instalación
3. Conceder permisos solicitados

## 🚀 **PASO 7: CONFIGURAR PERMISOS**

### **7.1 Permisos Básicos:**
- ✅ **Micrófono** - Para comandos de voz
- ✅ **Almacenamiento** - Para guardar datos
- ✅ **Contactos** - Para reconocimiento
- ✅ **Teléfono** - Para llamadas

### **7.2 Permisos Críticos:**
- ✅ **Accesibilidad** - **OBLIGATORIO**
- ✅ **Superposición** - **OBLIGATORIO**
- ✅ **Administrador** - Opcional

### **7.3 Configurar Accesibilidad:**
1. **Configuración > Accesibilidad**
2. Buscar **"Ritsu AI"**
3. Activar servicio
4. Confirmar activación

### **7.4 Configurar Superposición:**
1. **Configuración > Aplicaciones > Ritsu AI**
2. **Permisos > Aparecer en primer plano**
3. Activar permiso

## 🚀 **PASO 8: PRIMERA CONFIGURACIÓN**

### **8.1 Abrir Ritsu AI:**
1. Buscar app en el launcher
2. Abrir aplicación
3. Seguir tutorial inicial

### **8.2 Configurar Personalidad:**
- Nombre de Ritsu
- Estilo de voz
- Preferencias de avatar
- Configuración de IA

### **8.3 Probar Funcionalidades:**
- Avatar flotante
- Generación de ropa
- Comandos de voz
- Control de aplicaciones

## 🛠️ **SOLUCIÓN DE PROBLEMAS**

### **Error: "SDK location not found"**
```bash
# Crear archivo local.properties
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### **Error: "Failed to resolve dependencies"**
```bash
# Limpiar cache de Gradle
./gradlew clean
./gradlew --refresh-dependencies
```

### **Error: "OutOfMemoryError"**
```bash
# Aumentar memoria en gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### **Error: "API level not found"**
1. En Android Studio, ir a **SDK Manager**
2. Instalar **Android 8.0 (API 26)** o superior
3. Sincronizar proyecto

### **Error: "OpenAI API key not found"**
1. Verificar archivo `local.properties`
2. Asegurar que la clave esté correcta
3. Reiniciar Android Studio

## 📱 **VERIFICAR INSTALACIÓN**

### **Funcionalidades a Probar:**
1. ✅ **Avatar aparece** en pantalla
2. ✅ **Responde a comandos** de voz
3. ✅ **Genera ropa** con prompts
4. ✅ **Controla aplicaciones** (WhatsApp, etc.)
5. ✅ **Modo sin censura** funciona

## 🎯 **RESULTADO FINAL**

**Una vez completados todos los pasos, tendrás:**

- 🤖 **Ritsu AI completamente funcional**
- 🎭 **Avatar kawaii que vive en tu teléfono**
- 👗 **Generador de ropa con IA**
- 📱 **Control total del dispositivo**
- 🧠 **Inteligencia artificial súper avanzada**

## 🆘 **SI ALGO NO FUNCIONA**

1. **Revisar logs** en Android Studio
2. **Verificar permisos** en el dispositivo
3. **Comprobar configuración** de OpenAI
4. **Reiniciar** aplicación y dispositivo

---

## 🎉 **¡FELICIDADES!**

**Has compilado e instalado Ritsu AI exitosamente. Ahora tienes una IA que literalmente "vive" en tu teléfono.**

**¡Disfruta de tu nueva asistente personal! 🤖✨**