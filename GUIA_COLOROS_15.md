# 📱 GUÍA ESPECÍFICA PARA COLOROS 15 - OPPO RENO 13 5G

## 🚨 **COLOROS 15 - CONFIGURACIONES ACTUALIZADAS**

ColorOS 15 es la versión más reciente y tiene configuraciones de seguridad **MUY ESTRICTAS**. Esta guía está específicamente diseñada para esta versión.

---

## 🔧 **PASO 1: CONFIGURACIÓN BÁSICA COLOROS 15**

### 📱 **Activar Fuentes Desconocidas (NUEVA UBICACIÓN)**

#### **Ubicación Principal (Nueva en ColorOS 15):**
1. Ve a **Configuración** (⚙️)
2. Busca **"Privacidad y seguridad"** (nueva ubicación)
3. Busca **"Instalación de aplicaciones"** o **"Fuentes desconocidas"**
4. **ACTIVA** esta opción

#### **Ubicación Secundaria:**
1. Ve a **Configuración** > **Aplicaciones**
2. Busca **"Configuración especial de acceso"**
3. Busca **"Instalar aplicaciones desconocidas"**
4. **ACTIVA** para **TODAS** las apps que uses

#### **Ubicación por App (OBLIGATORIO en ColorOS 15):**
1. Ve a **Configuración** > **Aplicaciones**
2. Busca tu **navegador** (Chrome, Firefox, etc.)
3. Toca en **"Permisos"**
4. Busca **"Instalar aplicaciones desconocidas"**
5. **ACTIVA** esta opción

---

## 🛡️ **PASO 2: CONFIGURACIONES ESPECÍFICAS COLOROS 15**

### 🔒 **Nuevas Protecciones de Seguridad**

#### **Opción A: Configuración de Privacidad (NUEVA)**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Análisis de aplicaciones"** o **"Verificación de apps"**
3. **DESACTIVA** temporalmente esta opción

#### **Opción B: Configuración de Seguridad**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Verificación de seguridad"** o **"Análisis de seguridad"**
3. **DESACTIVA** temporalmente

#### **Opción C: Protección Avanzada (NUEVA en ColorOS 15)**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Protección avanzada"** o **"Protección del sistema"**
3. **DESACTIVA** temporalmente

#### **Opción D: Gestión de Aplicaciones (NUEVA)**
1. Ve a **Configuración** > **Aplicaciones**
2. Busca **"Gestión de aplicaciones"**
3. Busca **"Instalación de aplicaciones"**
4. **ACTIVA** todas las opciones relacionadas

---

## 🔓 **PASO 3: OPCIONES DE DESARROLLADOR COLOROS 15**

### 👨‍💻 **Activar Opciones de Desarrollador**
1. Ve a **Configuración** > **Acerca del teléfono**
2. Busca **"Número de compilación"** o **"Versión de ColorOS"**
3. **TOCA 7 VECES** rápidamente sobre este texto
4. Verás un mensaje: "Ya eres desarrollador"

### ⚙️ **Configurar Opciones de Desarrollador (ACTUALIZADAS)**
1. Ve a **Configuración** > **Opciones de desarrollador**
2. Busca y **ACTIVA** estas opciones:
   - **"Depuración USB"**
   - **"Instalar vía USB"**
   - **"Verificar apps vía USB"**
   - **"Permitir instalación de apps de fuentes desconocidas"**
   - **"OEM desbloqueo"** (si está disponible)
   - **"Depuración de aplicaciones"**

---

## 📥 **PASO 4: MÉTODOS DE INSTALACIÓN COLOROS 15**

### 💻 **Método 1: ADB (RECOMENDADO para ColorOS 15)**

```bash
# Configuraciones específicas para ColorOS 15
adb shell settings put global package_verifier_enable 0
adb shell settings put secure install_non_market_apps 1
adb shell settings put global adb_enabled 1

# Instalar APK
adb install -r -d app/build/outputs/apk/debug/app-debug.apk
```

### 📱 **Método 2: Apps de Terceros (RECOMENDADO)**
ColorOS 15 es muy restrictivo, usa estas apps:

1. **"APK Installer Pro"** - Descarga desde Google Play
2. **"APK Installer"** - Versión gratuita
3. **"Package Installer"** - Alternativa

### 🌐 **Método 3: Navegadores Específicos**
1. **Firefox** - Menos restricciones en ColorOS 15
2. **Opera** - Alternativa confiable
3. **Edge** - Funciona bien con OPPO

---

## 🔧 **PASO 5: CONFIGURACIONES AVANZADAS COLOROS 15**

### 🛡️ **Desactivar Protecciones Específicas**

#### **Protección de Aplicaciones (NUEVA):**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Protección de aplicaciones"**
3. **DESACTIVA** temporalmente

#### **Análisis de Seguridad (NUEVA):**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Análisis de seguridad"**
3. **DESACTIVA** temporalmente

#### **Verificación de Integridad (NUEVA):**
1. Ve a **Configuración** > **Privacidad y seguridad**
2. Busca **"Verificación de integridad"**
3. **DESACTIVA** temporalmente

### 📊 **Configurar Gestión de Aplicaciones**
1. Ve a **Configuración** > **Aplicaciones**
2. Busca **"Configuración especial de acceso"**
3. Busca **"Instalar aplicaciones desconocidas"**
4. **ACTIVA** para **TODAS** las apps que uses

---

## 🚨 **PASO 6: SOLUCIÓN ESPECÍFICA COLOROS 15**

### 🔍 **Problema Específico de ColorOS 15**
ColorOS 15 tiene **verificación de integridad mejorada** que bloquea APKs no verificados.

### ✅ **Solución Paso a Paso:**

#### **Paso 6.1: Limpiar Cache del Sistema**
1. Ve a **Configuración** > **Almacenamiento**
2. Toca **"Liberar espacio"**
3. Limpia **"Cache del sistema"**
4. Limpia **"Datos de aplicaciones"** del navegador
5. Reinicia el teléfono

#### **Paso 6.2: Verificar APK**
1. Asegúrate de que el APK esté **completo** (no corrupto)
2. El tamaño debe ser **15-20MB** aproximadamente
3. Descarga el APK nuevamente si es necesario

#### **Paso 6.3: Usar Método de Instalación Forzada**
1. Ve a **Configuración** > **Aplicaciones**
2. Busca **"Configuración especial de acceso"**
3. Busca **"Instalar aplicaciones desconocidas"**
4. **ACTIVA** para **TODAS** las apps que uses

#### **Paso 6.4: Instalación con Comandos ADB (ColorOS 15)**
```bash
# Configuraciones específicas para ColorOS 15
adb shell settings put global package_verifier_enable 0
adb shell settings put secure install_non_market_apps 1
adb shell settings put global adb_enabled 1

# Forzar instalación
adb install -r -d ritsu-ai-debug.apk

# O instalar sin verificación
adb install --bypass-low-target-sdk-block ritsu-ai-debug.apk
```

---

## 🔄 **PASO 7: REINTENTO DE INSTALACIÓN COLOROS 15**

### 📱 **Después de Configurar Todo:**
1. **Reinicia** tu teléfono completamente
2. **Descarga** el APK de Ritsu nuevamente
3. **Abre** el archivo APK
4. Si aparece advertencia, toca **"Instalar de todas formas"**
5. **Espera** a que termine la instalación

### 🎯 **Si Aún No Funciona:**
1. **Desactiva temporalmente** el antivirus de OPPO
2. **Usa un gestor de archivos** de terceros
3. **Intenta instalar** desde una ubicación diferente (SD card vs almacenamiento interno)
4. **Usa una app de terceros** como "APK Installer Pro"

---

## ⚠️ **CONFIGURACIONES DE SEGURIDAD A RESTAURAR**

### 🔒 **Después de Instalar Ritsu:**
1. **Reactiva** la verificación de seguridad
2. **Reactiva** la protección avanzada
3. **Reactiva** el análisis de aplicaciones
4. **Reactiva** la verificación de integridad
5. **Mantén** las fuentes desconocidas activadas para Ritsu

---

## 🎉 **PASO 8: VERIFICACIÓN FINAL COLOROS 15**

### ✅ **Comprobar que Ritsu Funciona:**
1. **Abre** la app Ritsu AI
2. **Verifica** que se inicie correctamente
3. **Configura** los permisos necesarios
4. **Prueba** las funciones básicas

### 🔧 **Configuración de Permisos para Ritsu (ColorOS 15):**
Una vez instalado, configura estos permisos:
- **Accesibilidad** (OBLIGATORIO)
- **Superposición** (OBLIGATORIO)
- **Almacenamiento**
- **Micrófono**
- **Cámara**
- **Teléfono**
- **Contactos**

---

## 📞 **SI NADA FUNCIONA EN COLOROS 15**

### 🆘 **Últimos Recursos:**
1. **Contacta soporte OPPO** - pueden desbloquear tu dispositivo
2. **Usa una app de terceros** como "APK Installer Pro"
3. **Instala desde una computadora** usando ADB
4. **Considera rootear** el dispositivo (solo si es necesario)

### 💡 **Consejo Final:**
ColorOS 15 es la versión más restrictiva hasta ahora. Las configuraciones anteriores están específicamente diseñadas para esta versión y deberían resolver el problema "no es analizable".

---

## 🎯 **RESUMEN RÁPIDO COLOROS 15**

1. ✅ **Activa fuentes desconocidas** en múltiples ubicaciones
2. ✅ **Desactiva temporalmente** verificación de seguridad
3. ✅ **Desactiva** análisis de aplicaciones
4. ✅ **Desactiva** verificación de integridad
5. ✅ **Activa opciones de desarrollador**
6. ✅ **Usa método ADB** si es posible
7. ✅ **Reinicia** y vuelve a intentar
8. ✅ **Configura permisos** después de instalar

**¡Con estos pasos deberías poder instalar Ritsu AI en tu OPPO Reno 13 5G con ColorOS 15 sin problemas!** 🚀

---

*¿Necesitas ayuda adicional? Los pasos están diseñados específicamente para ColorOS 15 y deberían resolver el problema "no es analizable".*