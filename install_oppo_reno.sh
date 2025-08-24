#!/bin/bash

# 📱 Script de Instalación Automática para OPPO Reno 13 5G
# Soluciona el problema "no es analizable" usando ADB

echo "🚀 INSTALADOR AUTOMÁTICO PARA OPPO RENO 13 5G"
echo "=============================================="
echo ""

# Verificar si ADB está instalado
if ! command -v adb &> /dev/null; then
    echo "❌ ERROR: ADB no está instalado"
    echo "📥 Descarga Android SDK Platform Tools desde:"
    echo "   https://developer.android.com/studio/releases/platform-tools"
    exit 1
fi

# Verificar si el APK existe
APK_FILE="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_FILE" ]; then
    echo "❌ ERROR: No se encontró el APK en $APK_FILE"
    echo "🔍 Buscando APK en otras ubicaciones..."
    
    # Buscar APK en diferentes ubicaciones
    APK_FOUND=false
    for file in $(find . -name "*.apk" -type f 2>/dev/null); do
        echo "📱 APK encontrado: $file"
        APK_FILE="$file"
        APK_FOUND=true
        break
    done
    
    if [ "$APK_FOUND" = false ]; then
        echo "❌ No se encontró ningún APK en el directorio actual"
        echo "💡 Asegúrate de que el APK esté en el directorio o compílalo primero"
        exit 1
    fi
fi

echo "✅ APK encontrado: $APK_FILE"
echo ""

# Verificar conexión del dispositivo
echo "📱 Verificando conexión del dispositivo..."
adb devices

# Verificar si hay dispositivos conectados
DEVICES=$(adb devices | grep -v "List of devices" | grep -v "^$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "❌ ERROR: No hay dispositivos Android conectados"
    echo ""
    echo "🔧 SOLUCIÓN:"
    echo "1. Conecta tu OPPO Reno 13 5G por USB"
    echo "2. Activa 'Depuración USB' en Opciones de desarrollador"
    echo "3. Acepta la autorización en tu teléfono"
    echo "4. Ejecuta este script nuevamente"
    exit 1
fi

echo "✅ Dispositivo conectado correctamente"
echo ""

# Mostrar información del dispositivo
echo "📊 Información del dispositivo:"
adb shell getprop ro.product.model
adb shell getprop ro.build.version.release
echo ""

# Verificar si es un OPPO
MODEL=$(adb shell getprop ro.product.model)
if [[ "$MODEL" == *"OPPO"* ]] || [[ "$MODEL" == *"Reno"* ]]; then
    echo "✅ Dispositivo OPPO detectado: $MODEL"
    echo "🔧 Aplicando configuraciones específicas para ColorOS..."
    echo ""
else
    echo "⚠️  Dispositivo no identificado como OPPO: $MODEL"
    echo "💡 Continuando con instalación estándar..."
    echo ""
fi

# Configuraciones específicas para OPPO/ColorOS
echo "🔧 Configurando permisos específicos para OPPO..."

# Desactivar verificación de seguridad temporalmente
echo "🛡️  Desactivando verificación de seguridad..."
adb shell settings put global package_verifier_enable 0

# Permitir instalación de fuentes desconocidas
echo "🔓 Configurando fuentes desconocidas..."
adb shell settings put secure install_non_market_apps 1

# Configurar permisos de instalación
echo "📦 Configurando permisos de instalación..."
adb shell pm grant com.android.packageinstaller android.permission.REQUEST_INSTALL_PACKAGES

echo "✅ Configuraciones aplicadas"
echo ""

# Intentar instalación
echo "📥 Instalando APK..."
echo "📁 Archivo: $APK_FILE"
echo ""

# Instalación con opciones específicas para OPPO
echo "🔄 Intento 1: Instalación estándar..."
if adb install "$APK_FILE"; then
    echo "✅ ¡INSTALACIÓN EXITOSA!"
    SUCCESS=true
else
    echo "⚠️  Instalación estándar falló, intentando método alternativo..."
    
    echo "🔄 Intento 2: Instalación forzada..."
    if adb install -r -d "$APK_FILE"; then
        echo "✅ ¡INSTALACIÓN EXITOSA con método forzado!"
        SUCCESS=true
    else
        echo "⚠️  Instalación forzada falló, intentando último método..."
        
        echo "🔄 Intento 3: Instalación sin verificación..."
        if adb install --bypass-low-target-sdk-block "$APK_FILE"; then
            echo "✅ ¡INSTALACIÓN EXITOSA sin verificación!"
            SUCCESS=true
        else
            echo "❌ Todos los métodos de instalación fallaron"
            SUCCESS=false
        fi
    fi
fi

echo ""

if [ "$SUCCESS" = true ]; then
    echo "🎉 ¡INSTALACIÓN COMPLETADA!"
    echo "=============================================="
    echo ""
    echo "📱 Próximos pasos:"
    echo "1. Abre la app 'Ritsu AI' en tu teléfono"
    echo "2. Configura los permisos necesarios:"
    echo "   - Accesibilidad (OBLIGATORIO)"
    echo "   - Superposición (OBLIGATORIO)"
    echo "   - Almacenamiento"
    echo "   - Micrófono"
    echo "   - Cámara"
    echo ""
    echo "3. ¡Disfruta de tu nueva asistente kawaii! 🌸"
    echo ""
    
    # Restaurar configuraciones de seguridad
    echo "🔒 Restaurando configuraciones de seguridad..."
    adb shell settings put global package_verifier_enable 1
    echo "✅ Configuraciones restauradas"
    
else
    echo "❌ INSTALACIÓN FALLIDA"
    echo "=============================================="
    echo ""
    echo "🔧 SOLUCIONES ALTERNATIVAS:"
    echo ""
    echo "1. 📱 CONFIGURACIÓN MANUAL EN EL TELÉFONO:"
    echo "   - Ve a Configuración > Seguridad"
    echo "   - Desactiva 'Verificación de seguridad'"
    echo "   - Activa 'Fuentes desconocidas'"
    echo "   - Intenta instalar manualmente"
    echo ""
    echo "2. 📥 APPS ALTERNATIVAS:"
    echo "   - Descarga 'APK Installer' desde Google Play"
    echo "   - Usa esa app para instalar el APK"
    echo ""
    echo "3. 💻 INSTALACIÓN DESDE COMPUTADORA:"
    echo "   - Transfiere el APK por USB"
    echo "   - Instálalo directamente desde el gestor de archivos"
    echo ""
    echo "4. 🌐 NAVEGADOR ALTERNATIVO:"
    echo "   - Usa Firefox en lugar de Chrome"
    echo "   - Descarga e instala desde Firefox"
    echo ""
    echo "📞 Si nada funciona, consulta la guía completa: GUIA_OPPO_RENO_13.md"
fi

echo ""
echo "✨ Script completado"