#!/bin/bash

# Script de instalación específico para ColorOS 15
# OPPO Reno 13 5G

echo "=== Instalador Ritsu AI para ColorOS 15 ==="
echo "Dispositivo: OPPO Reno 13 5G"
echo "Sistema: ColorOS 15"
echo ""

# Verificar si ADB está disponible
if ! command -v adb &> /dev/null; then
    echo "ERROR: ADB no está instalado o no está en el PATH"
    echo "Por favor, instala Android SDK Platform Tools"
    exit 1
fi

# Verificar conexión del dispositivo
echo "Verificando conexión del dispositivo..."
adb devices

# Verificar que el dispositivo esté conectado
if ! adb devices | grep -q "device$"; then
    echo "ERROR: No se detectó ningún dispositivo conectado"
    echo "Por favor, conecta tu OPPO Reno 13 5G y habilita la depuración USB"
    exit 1
fi

echo ""
echo "Dispositivo detectado. Iniciando instalación..."

# Habilitar instalación de fuentes desconocidas
echo "Habilitando instalación de fuentes desconocidas..."
adb shell settings put global install_non_market_apps 1

# Configurar permisos específicos de ColorOS
echo "Configurando permisos específicos de ColorOS 15..."

# Habilitar auto-inicio
adb shell settings put secure auto_start_com.ritsu.ai 1

# Deshabilitar optimización de batería
adb shell settings put secure battery_optimization_com.ritsu.ai 0

# Habilitar acceso a notificaciones
adb shell settings put secure notification_access_com.ritsu.ai 1

# Habilitar servicio de accesibilidad
adb shell settings put secure accessibility_enabled 1

# Habilitar estadísticas de uso
adb shell settings put secure usage_stats_enabled 1

# Instalar la aplicación
echo "Instalando Ritsu AI..."
if adb install -r app-release.apk; then
    echo "✅ Instalación exitosa!"
else
    echo "❌ Error en la instalación"
    echo "Intentando instalación con permisos forzados..."
    adb install -r -d app-release.apk
fi

# Configurar permisos adicionales después de la instalación
echo "Configurando permisos adicionales..."

# Otorgar permisos de accesibilidad
adb shell pm grant com.ritsu.ai android.permission.BIND_ACCESSIBILITY_SERVICE

# Otorgar permisos de overlay
adb shell pm grant com.ritsu.ai android.permission.SYSTEM_ALERT_WINDOW

# Otorgar permisos de notificaciones
adb shell pm grant com.ritsu.ai android.permission.BIND_NOTIFICATION_LISTENER_SERVICE

# Otorgar permisos de uso de estadísticas
adb shell pm grant com.ritsu.ai android.permission.PACKAGE_USAGE_STATS

# Otorgar permisos de llamadas
adb shell pm grant com.ritsu.ai android.permission.ANSWER_PHONE_CALLS
adb shell pm grant com.ritsu.ai android.permission.READ_PHONE_STATE
adb shell pm grant com.ritsu.ai android.permission.CALL_PHONE

# Otorgar permisos de audio
adb shell pm grant com.ritsu.ai android.permission.RECORD_AUDIO
adb shell pm grant com.ritsu.ai android.permission.MODIFY_AUDIO_SETTINGS

# Otorgar permisos de almacenamiento
adb shell pm grant com.ritsu.ai android.permission.READ_EXTERNAL_STORAGE
adb shell pm grant com.ritsu.ai android.permission.WRITE_EXTERNAL_STORAGE

# Otorgar permisos de ubicación
adb shell pm grant com.ritsu.ai android.permission.ACCESS_FINE_LOCATION
adb shell pm grant com.ritsu.ai android.permission.ACCESS_COARSE_LOCATION

# Otorgar permisos de WiFi
adb shell pm grant com.ritsu.ai android.permission.ACCESS_WIFI_STATE
adb shell pm grant com.ritsu.ai android.permission.CHANGE_WIFI_STATE

# Otorgar permisos de Bluetooth
adb shell pm grant com.ritsu.ai android.permission.BLUETOOTH
adb shell pm grant com.ritsu.ai android.permission.BLUETOOTH_ADMIN
adb shell pm grant com.ritsu.ai android.permission.BLUETOOTH_CONNECT

# Configurar para que la app se ejecute en segundo plano
echo "Configurando ejecución en segundo plano..."

# Habilitar ejecución en segundo plano
adb shell settings put global background_activity_enabled 1

# Configurar para que no se mate la app
adb shell settings put global background_process_limit 100

# Configurar para que la app tenga prioridad alta
adb shell settings put global background_priority 1

echo ""
echo "=== Instalación completada ==="
echo ""
echo "Pasos adicionales necesarios:"
echo "1. Abre la aplicación Ritsu AI"
echo "2. Ve a Configuración > Accesibilidad"
echo "3. Habilita el servicio de Ritsu AI"
echo "4. Ve a Configuración > Aplicaciones > Ritsu AI"
echo "5. Habilita 'Auto-inicio' y 'Ejecutar en segundo plano'"
echo "6. Deshabilita 'Optimización de batería'"
echo "7. Habilita 'Mostrar sobre otras aplicaciones'"
echo ""
echo "¡La aplicación está lista para usar!"
echo ""
echo "Para verificar la instalación:"
echo "adb shell pm list packages | grep ritsu"
echo "adb shell dumpsys package com.ritsu.ai"