# 📱 Guía de Instalación de Ritsu AI

## 🎯 Descripción General

Ritsu AI es una aplicación de inteligencia artificial avanzada para Android que funciona como un asistente personal completo. La aplicación incluye un avatar anime kawaii personalizable, control total del dispositivo, generación automática de ropa, y capacidades de aprendizaje automático.

## ⚠️ Requisitos Previos

### 📱 Dispositivo
- **Sistema Operativo**: Android 8.0 (API 26) o superior
- **RAM**: Mínimo 4GB, recomendado 6GB o más
- **Almacenamiento**: Mínimo 2GB de espacio libre
- **Arquitectura**: ARM64, ARM32, x86, o x86_64

### 🔧 Software
- **Java**: JDK 8 o superior
- **Android SDK**: API 26 o superior
- **Gradle**: Versión 7.0 o superior

## 🚀 Pasos de Instalación

### 1. Preparación del Entorno

#### Configurar Variables de Entorno
```bash
# En Linux/macOS
export ANDROID_HOME=/path/to/android/sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# En Windows
set ANDROID_HOME=C:\path\to\android\sdk
set PATH=%PATH%;%ANDROID_HOME%\tools;%ANDROID_HOME%\platform-tools
```

#### Verificar Instalaciones
```bash
# Verificar Java
java -version

# Verificar Android SDK
adb version

# Verificar Gradle
./gradlew --version
```

### 2. Compilación del Proyecto

#### Clonar o Descargar el Código
```bash
# Si tienes Git
git clone <repository-url>
cd ritsu-ai

# O descargar y extraer el ZIP
unzip ritsu-ai.zip
cd ritsu-ai
```

#### Compilar la Aplicación
```bash
# Dar permisos al script de compilación
chmod +x build.sh

# Ejecutar compilación automática
./build.sh

# O compilar manualmente
./gradlew clean
./gradlew assembleDebug
```

#### Verificar Compilación
- El APK se generará en: `app/build/outputs/apk/debug/app-debug.apk`
- Verificar que el archivo existe y tiene un tamaño razonable (>10MB)

### 3. Instalación en el Dispositivo

#### Preparar el Dispositivo Android
1. **Habilitar Opciones de Desarrollador**
   - Ir a `Configuración` → `Acerca del teléfono`
   - Tocar 7 veces en `Número de compilación`
   - Volver a `Configuración` → `Opciones de desarrollador`

2. **Habilitar Depuración USB**
   - En `Opciones de desarrollador` → `Depuración USB` → `Activado`

3. **Habilitar Instalación de Fuentes Desconocidas**
   - En `Opciones de desarrollador` → `Instalar apps de fuentes desconocidas`
   - Seleccionar tu explorador de archivos o aplicación de transferencia

#### Transferir e Instalar el APK
1. **Transferir el APK**
   - Conectar el dispositivo por USB
   - Copiar `app-debug.apk` al dispositivo
   - O usar ADB: `adb install app-debug.apk`

2. **Instalar la Aplicación**
   - Abrir el explorador de archivos del dispositivo
   - Navegar al APK transferido
   - Tocar en el archivo para iniciar la instalación
   - Confirmar la instalación

## 🔐 Configuración de Permisos

### Permisos Automáticos
La aplicación solicitará automáticamente los permisos necesarios durante la primera ejecución.

### Permisos Manuales (si es necesario)

#### 1. Permisos de Accesibilidad
1. Ir a `Configuración` → `Accesibilidad`
2. Buscar `Ritsu AI`
3. Activar el servicio
4. Confirmar la activación

#### 2. Permisos de Superposición
1. Ir a `Configuración` → `Aplicaciones` → `Ritsu AI`
2. Tocar en `Permisos`
3. Activar `Mostrar sobre otras aplicaciones`
4. Confirmar el permiso

#### 3. Permisos del Sistema
1. En la aplicación, ir a `Configuración` → `Permisos`
2. Solicitar permisos por categoría
3. Confirmar cada permiso solicitado

## 🎮 Primera Configuración

### 1. Iniciar Ritsu AI
- Abrir la aplicación
- Completar el tutorial inicial
- Configurar preferencias básicas

### 2. Configurar Avatar
- Personalizar apariencia del avatar
- Generar ropa personalizada
- Ajustar expresiones y animaciones

### 3. Configurar IA
- Ajustar preferencias de lenguaje
- Configurar respuestas
- Habilitar aprendizaje automático

## 🔧 Solución de Problemas

### Errores Comunes

#### Error de Compilación
```bash
# Limpiar proyecto
./gradlew clean

# Actualizar dependencias
./gradlew --refresh-dependencies

# Verificar versión de Gradle
./gradlew --version
```

#### Error de Permisos
- Verificar que todos los permisos estén concedidos
- Reiniciar la aplicación después de conceder permisos
- Verificar configuración de accesibilidad

#### Error de Instalación
- Verificar que `Instalar apps de fuentes desconocidas` esté habilitado
- Verificar espacio disponible en el dispositivo
- Reiniciar el dispositivo si es necesario

### Logs y Debug
```bash
# Ver logs en tiempo real
adb logcat | grep "RitsuAI"

# Ver logs de la aplicación
adb logcat -s "RitsuAI:*"

# Verificar instalación
adb shell pm list packages | grep ritsu
```

## 📱 Funcionalidades Principales

### 🎭 Avatar Personalizable
- **Cuerpo completo anime kawaii**
- **Generación automática de ropa**
- **Expresiones y animaciones**
- **Modo especial desbloqueable**

### 🧠 Inteligencia Artificial
- **Conversación natural en español**
- **Control total del dispositivo**
- **Aprendizaje automático**
- **Respuestas contextuales**

### 📱 Control del Sistema
- **Gestión de aplicaciones**
- **Mensajes y llamadas**
- **Archivos y multimedia**
- **Configuración del sistema**

## 🎯 Modo Especial

### Desbloquear Funcionalidades Avanzadas
- **Palabra clave**: `ritsu_sin_censura_2024`
- **Funcionalidades adicionales del avatar**
- **Modo de ropa avanzado**
- **Expresiones especiales**

## 🔄 Actualizaciones

### Actualización Automática
- La aplicación verifica actualizaciones automáticamente
- Notificaciones cuando hay nuevas versiones
- Descarga e instalación automática

### Actualización Manual
1. Descargar nueva versión del código
2. Compilar siguiendo los pasos anteriores
3. Instalar sobre la versión existente

## 📞 Soporte Técnico

### Documentación
- **README.md**: Información general del proyecto
- **INSTALACION.md**: Esta guía de instalación
- **Código fuente**: Comentarios detallados en el código

### Contacto
- **Issues**: Reportar bugs o solicitar funcionalidades
- **Discussions**: Preguntas y soporte de la comunidad
- **Wiki**: Documentación adicional y tutoriales

## 🎉 ¡Listo!

Una vez completados todos los pasos, Ritsu AI estará completamente funcional en tu dispositivo Android. La aplicación te ayudará con todas las tareas del teléfono y aprenderá de tus preferencias para mejorar con el tiempo.

### Próximos Pasos Recomendados
1. **Explorar funcionalidades**: Probar todas las capacidades de Ritsu
2. **Personalizar avatar**: Crear outfits únicos y personalizados
3. **Configurar preferencias**: Ajustar la IA según tus necesidades
4. **Compartir experiencia**: Ayudar a otros usuarios con la instalación

---

**Nota**: Esta aplicación requiere permisos extensivos para funcionar completamente. Todos los datos se procesan localmente en tu dispositivo para garantizar tu privacidad.