#!/bin/bash

# Script de compilación para Ritsu AI
# Este script compila la aplicación Android y genera el APK

echo "🚀 Iniciando compilación de Ritsu AI..."

# Verificar si Android SDK está configurado
if [ -z "$ANDROID_HOME" ]; then
    echo "❌ Error: ANDROID_HOME no está configurado"
    echo "Por favor, configura la variable de entorno ANDROID_HOME"
    exit 1
fi

# Verificar si Java está disponible
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java no está instalado o no está en el PATH"
    exit 1
fi

# Verificar si Gradle está disponible
if ! command -v ./gradlew &> /dev/null; then
    echo "❌ Error: Gradle wrapper no está disponible"
    echo "Ejecutando: chmod +x gradlew"
    chmod +x gradlew
fi

echo "✅ Verificaciones completadas"
echo "📱 Compilando aplicación..."

# Limpiar build anterior
echo "🧹 Limpiando build anterior..."
./gradlew clean

# Compilar en modo debug
echo "🔨 Compilando en modo debug..."
./gradlew assembleDebug

# Verificar si la compilación fue exitosa
if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa!"
    echo "📦 APK generado en: app/build/outputs/apk/debug/"
    
    # Mostrar información del APK
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
    if [ -f "$APK_PATH" ]; then
        echo "📊 Información del APK:"
        echo "   Tamaño: $(du -h "$APK_PATH" | cut -f1)"
        echo "   Ruta: $APK_PATH"
        
        # Verificar que el APK es válido
        if unzip -t "$APK_PATH" > /dev/null 2>&1; then
            echo "✅ APK válido"
        else
            echo "❌ APK corrupto"
        fi
    fi
    
    echo ""
    echo "🎉 ¡Ritsu AI está lista para instalar!"
    echo "📱 Transfiere el APK a tu dispositivo Android"
    echo "🔧 Recuerda habilitar 'Instalar apps de fuentes desconocidas'"
    
else
    echo "❌ Error en la compilación"
    echo "📋 Revisa los logs de error arriba"
    exit 1
fi

echo ""
echo "📚 Próximos pasos:"
echo "1. Transfiere el APK a tu dispositivo Android"
echo "2. Habilita 'Instalar apps de fuentes desconocidas'"
echo "3. Instala la aplicación"
echo "4. Concede todos los permisos solicitados"
echo "5. ¡Disfruta de Ritsu AI!"