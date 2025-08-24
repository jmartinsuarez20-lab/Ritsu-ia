# Guía de Instalación Ritsu AI para ColorOS 15
## OPPO Reno 13 5G

### ⚠️ IMPORTANTE: Configuración Específica para ColorOS 15

Esta aplicación ha sido optimizada específicamente para funcionar en dispositivos OPPO con ColorOS 15. Sigue estos pasos cuidadosamente para evitar problemas de instalación.

## 📋 Requisitos Previos

1. **Dispositivo**: OPPO Reno 13 5G
2. **Sistema**: ColorOS 15
3. **Depuración USB**: Habilitada
4. **Fuentes Desconocidas**: Habilitadas
5. **ADB**: Instalado en tu computadora

## 🔧 Configuración Inicial

### 1. Habilitar Depuración USB

1. Ve a **Configuración** > **Acerca del teléfono**
2. Toca **Número de compilación** 7 veces para habilitar opciones de desarrollador
3. Ve a **Configuración** > **Opciones de desarrollador**
4. Habilita **Depuración USB**
5. Habilita **Instalar vía USB**

### 2. Habilitar Fuentes Desconocidas

1. Ve a **Configuración** > **Seguridad**
2. Habilita **Fuentes desconocidas**
3. Si aparece una advertencia, confirma

### 3. Configurar Permisos de ColorOS

1. Ve a **Configuración** > **Aplicaciones** > **Gestión de aplicaciones**
2. Busca **Ritsu AI** (después de instalarla)
3. Habilita **Auto-inicio**
4. Habilita **Ejecutar en segundo plano**
5. Deshabilita **Optimización de batería**
6. Habilita **Mostrar sobre otras aplicaciones**

## 📱 Instalación Automática

### Opción 1: Script Automático (Recomendado)

1. Conecta tu dispositivo via USB
2. Ejecuta el script de instalación:
   ```bash
   ./install_coloros15.sh
   ```

### Opción 2: Instalación Manual

1. Transfiere el archivo APK a tu dispositivo
2. Abre el archivo APK
3. Confirma la instalación
4. Sigue los pasos de configuración

## ⚙️ Configuración Post-Instalación

### 1. Servicio de Accesibilidad

1. Abre **Ritsu AI**
2. Ve a **Configuración** > **Accesibilidad**
3. Busca **Ritsu AI**
4. Habilita el servicio
5. Confirma todos los permisos

### 2. Permisos de Notificaciones

1. Ve a **Configuración** > **Notificaciones y barra de estado**
2. Busca **Ritsu AI**
3. Habilita todas las notificaciones
4. Habilita **Acceso a notificaciones**

### 3. Permisos de Llamadas

1. Ve a **Configuración** > **Aplicaciones** > **Ritsu AI**
2. Ve a **Permisos**
3. Habilita **Llamadas**
4. Habilita **Teléfono**

### 4. Permisos de Audio

1. En la misma sección de permisos
2. Habilita **Micrófono**
3. Habilita **Audio**

## 🔒 Configuraciones Específicas de ColorOS 15

### Auto-Inicio
- **Ubicación**: Configuración > Aplicaciones > Gestión de aplicaciones > Ritsu AI > Auto-inicio
- **Estado**: ✅ Habilitado

### Optimización de Batería
- **Ubicación**: Configuración > Batería > Optimización de batería > Ritsu AI
- **Estado**: ❌ Deshabilitado

### Ejecución en Segundo Plano
- **Ubicación**: Configuración > Aplicaciones > Gestión de aplicaciones > Ritsu AI > Ejecutar en segundo plano
- **Estado**: ✅ Habilitado

### Mostrar sobre Otras Aplicaciones
- **Ubicación**: Configuración > Aplicaciones > Gestión de aplicaciones > Ritsu AI > Mostrar sobre otras aplicaciones
- **Estado**: ✅ Habilitado

### Acceso a Notificaciones
- **Ubicación**: Configuración > Notificaciones y barra de estado > Acceso a notificaciones > Ritsu AI
- **Estado**: ✅ Habilitado

### Estadísticas de Uso
- **Ubicación**: Configuración > Aplicaciones > Acceso especial > Uso de aplicaciones > Ritsu AI
- **Estado**: ✅ Habilitado

## 🎯 Configuración de Llamadas Automáticas

### 1. Habilitar Respuesta Automática

1. Abre **Ritsu AI**
2. Ve a **Configuración** > **Llamadas**
3. Habilita **Respuesta automática**
4. Configura el **Tiempo de espera** (recomendado: 2 segundos)

### 2. Configurar Respuestas

1. En la misma sección de llamadas
2. Configura tu **Nombre de usuario**
3. Habilita **Modo conversación**
4. Configura **Respuestas personalizadas**

### 3. Optimización de Audio

1. Habilita **Modo claridad de voz**
2. Habilita **Optimización OPPO**
3. Configura **Volumen de llamada**

## 🚨 Solución de Problemas

### Error: "No se puede instalar la aplicación"

**Solución**:
1. Ve a **Configuración** > **Seguridad** > **Fuentes desconocidas**
2. Habilita para **Ritsu AI** específicamente
3. Intenta la instalación nuevamente

### Error: "La aplicación se cierra inesperadamente"

**Solución**:
1. Ve a **Configuración** > **Aplicaciones** > **Ritsu AI**
2. Habilita **Auto-inicio**
3. Deshabilita **Optimización de batería**
4. Habilita **Ejecutar en segundo plano**

### Error: "No responde a llamadas automáticamente"

**Solución**:
1. Verifica que el servicio de accesibilidad esté habilitado
2. Verifica que los permisos de llamadas estén otorgados
3. Reinicia la aplicación

### Error: "No se escucha bien durante las llamadas"

**Solución**:
1. Habilita **Modo claridad de voz** en la configuración
2. Verifica que el volumen esté configurado correctamente
3. Asegúrate de que **Optimización OPPO** esté habilitada

## 📞 Configuración de Llamadas

### Respuesta Automática
- **Funcionalidad**: La app responde automáticamente a llamadas entrantes
- **Tiempo**: Configurable (1-5 segundos)
- **Personalización**: Respuestas basadas en hora del día y contacto

### Calidad de Audio
- **Optimización**: Específica para OPPO Reno 13 5G
- **Claridad**: Modo mejorado para llamadas
- **Reducción de ruido**: Automática
- **Cancelación de eco**: Habilitada

### Modo Conversación
- **Funcionalidad**: Mantiene conversaciones automáticas
- **Contexto**: Respuestas inteligentes basadas en el contexto
- **Personalización**: Aprende de tus patrones de uso

## 🔧 Comandos ADB Útiles

### Verificar Instalación
```bash
adb shell pm list packages | grep ritsu
```

### Verificar Permisos
```bash
adb shell dumpsys package com.ritsu.ai
```

### Forzar Permisos
```bash
adb shell pm grant com.ritsu.ai android.permission.BIND_ACCESSIBILITY_SERVICE
adb shell pm grant com.ritsu.ai android.permission.SYSTEM_ALERT_WINDOW
```

### Reiniciar Aplicación
```bash
adb shell am force-stop com.ritsu.ai
adb shell am start -n com.ritsu.ai/.ui.MainActivity
```

## 📞 Soporte

Si encuentras problemas durante la instalación:

1. **Verifica** que sigues todos los pasos exactamente
2. **Reinicia** el dispositivo después de la instalación
3. **Verifica** que todos los permisos estén habilitados
4. **Contacta** soporte técnico si el problema persiste

## ✅ Lista de Verificación

- [ ] Depuración USB habilitada
- [ ] Fuentes desconocidas habilitadas
- [ ] Aplicación instalada correctamente
- [ ] Servicio de accesibilidad habilitado
- [ ] Permisos de notificaciones otorgados
- [ ] Permisos de llamadas otorgados
- [ ] Auto-inicio habilitado
- [ ] Optimización de batería deshabilitada
- [ ] Ejecución en segundo plano habilitada
- [ ] Mostrar sobre otras aplicaciones habilitado
- [ ] Respuesta automática configurada
- [ ] Audio optimizado para OPPO

¡Tu Ritsu AI está listo para responder automáticamente a tus llamadas! 🎉