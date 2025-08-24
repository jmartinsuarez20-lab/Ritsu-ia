# ⚡ COMANDOS RÁPIDOS PARA COLOROS 15 - OPPO RENO 13 5G

## 🚨 **PROBLEMA: "No es analizable" en ColorOS 15**

### 📱 **SOLUCIÓN RÁPIDA - PASOS EN EL TELÉFONO (ColorOS 15)**

#### **Paso 1: Activar Fuentes Desconocidas (NUEVA UBICACIÓN)**
```
Configuración > Privacidad y seguridad > Instalación de aplicaciones ✅ ACTIVAR
```

#### **Paso 2: Configurar por App (OBLIGATORIO en ColorOS 15)**
```
Configuración > Aplicaciones > [Tu Navegador] > Permisos > Instalar apps desconocidas ✅ ACTIVAR
```

#### **Paso 3: Desactivar Verificaciones (NUEVAS en ColorOS 15)**
```
Configuración > Privacidad y seguridad > Verificación de seguridad ❌ DESACTIVAR
Configuración > Privacidad y seguridad > Análisis de aplicaciones ❌ DESACTIVAR
Configuración > Privacidad y seguridad > Verificación de integridad ❌ DESACTIVAR
```

#### **Paso 4: Opciones de Desarrollador**
```
Configuración > Acerca del teléfono > Número de compilación (TOCAR 7 VECES)
Configuración > Opciones de desarrollador > Depuración USB ✅ ACTIVAR
```

---

## 💻 **SOLUCIÓN CON COMPUTADORA (ADB) - ColorOS 15**

### **Si tienes acceso a una computadora:**

#### **Instalar ADB:**
```bash
# En Linux/Mac:
sudo apt install android-tools-adb

# En Windows, descarga desde:
# https://developer.android.com/studio/releases/platform-tools
```

#### **Conectar y configurar para ColorOS 15:**
```bash
# Conectar teléfono por USB
adb devices

# Configuraciones específicas para ColorOS 15
adb shell settings put global package_verifier_enable 0
adb shell settings put secure install_non_market_apps 1
adb shell settings put global adb_enabled 1

# Instalar APK
adb install -r -d app/build/outputs/apk/debug/app-debug.apk
```

#### **O usar el script automático para ColorOS 15:**
```bash
# Hacer ejecutable
chmod +x install_coloros_15.sh

# Ejecutar
./install_coloros_15.sh
```

---

## 📱 **MÉTODOS ALTERNATIVOS (RECOMENDADOS para ColorOS 15)**

### **Método 1: APK Installer Pro (MÁS EFECTIVO)**
1. Descarga **"APK Installer Pro"** desde Google Play
2. Usa esta app para instalar el APK
3. Tiene menos restricciones en ColorOS 15

### **Método 2: Package Installer**
1. Descarga **"Package Installer"** desde Google Play
2. Alternativa gratuita y efectiva
3. Funciona bien con ColorOS 15

### **Método 3: Firefox**
1. Usa **Firefox** en lugar de Chrome
2. Descarga el APK con Firefox
3. Firefox tiene menos restricciones en ColorOS 15

---

## 🔧 **COMANDOS ADB AVANZADOS - ColorOS 15**

### **Verificar dispositivo y versión:**
```bash
adb devices
adb shell getprop ro.product.model
adb shell getprop ro.build.version.opporom
```

### **Configuraciones específicas ColorOS 15:**
```bash
# Desactivar verificación
adb shell settings put global package_verifier_enable 0

# Permitir fuentes desconocidas
adb shell settings put secure install_non_market_apps 1

# Habilitar ADB
adb shell settings put global adb_enabled 1

# Configurar permisos
adb shell pm grant com.android.packageinstaller android.permission.REQUEST_INSTALL_PACKAGES

# Desactivar análisis de aplicaciones
adb shell settings put global package_verifier_enable 0
```

### **Instalación con diferentes métodos (ColorOS 15):**
```bash
# Método estándar
adb install app-debug.apk

# Método forzado
adb install -r -d app-debug.apk

# Sin verificación (ColorOS 15)
adb install --bypass-low-target-sdk-block app-debug.apk

# Con permisos elevados
adb install -g app-debug.apk
```

### **Restaurar configuraciones:**
```bash
# Reactivar verificación
adb shell settings put global package_verifier_enable 1
```

---

## 🎯 **RESUMEN DE SOLUCIÓN - ColorOS 15**

### **Orden de prioridad (ColorOS 15):**
1. ✅ **APK Installer Pro** (más efectivo para ColorOS 15)
2. ✅ **Configuración manual en teléfono** (múltiples ubicaciones)
3. ✅ **Firefox** (navegador alternativo)
4. ✅ **ADB** (si tienes computadora)
5. ✅ **Package Installer** (app de terceros)

### **Configuraciones clave (ColorOS 15):**
- 🔓 **Fuentes desconocidas** → ACTIVAR (múltiples ubicaciones)
- 🛡️ **Verificación de seguridad** → DESACTIVAR (temporalmente)
- 🔍 **Análisis de aplicaciones** → DESACTIVAR (temporalmente)
- 🔒 **Verificación de integridad** → DESACTIVAR (temporalmente)
- 👨‍💻 **Opciones de desarrollador** → ACTIVAR
- 📱 **Depuración USB** → ACTIVAR (para ADB)

---

## ⚠️ **IMPORTANTE - ColorOS 15**

### **Después de instalar:**
1. ✅ **Reactiva** la verificación de seguridad
2. ✅ **Reactiva** el análisis de aplicaciones
3. ✅ **Reactiva** la verificación de integridad
4. ✅ **Configura** los permisos de Ritsu
5. ✅ **Prueba** las funciones básicas

### **Si nada funciona:**
- 📞 Contacta soporte OPPO (ColorOS 15 es muy nuevo)
- 🔧 Usa apps de terceros como "APK Installer Pro"
- 💻 Usa ADB como último recurso
- 🌐 Considera usar navegadores alternativos

---

## 🔧 **CONFIGURACIONES ESPECÍFICAS COLOROS 15**

### **Ubicaciones específicas en ColorOS 15:**
```
Configuración > Privacidad y seguridad > Instalación de aplicaciones
Configuración > Privacidad y seguridad > Verificación de seguridad
Configuración > Privacidad y seguridad > Análisis de aplicaciones
Configuración > Privacidad y seguridad > Verificación de integridad
Configuración > Aplicaciones > Configuración especial de acceso
```

### **Apps recomendadas para ColorOS 15:**
- **APK Installer Pro** (Google Play)
- **Package Installer** (Google Play)
- **Firefox** (navegador alternativo)
- **Opera** (navegador alternativo)

---

**✨ ¡Con estos comandos deberías poder instalar Ritsu AI en tu OPPO Reno 13 5G con ColorOS 15! ✨**

---

*ColorOS 15 es la versión más restrictiva hasta ahora. Los comandos están específicamente diseñados para esta versión.*