# ⚡ COMANDOS RÁPIDOS PARA OPPO RENO 13 5G

## 🚨 **PROBLEMA: "No es analizable"**

### 📱 **SOLUCIÓN RÁPIDA - PASOS EN EL TELÉFONO**

#### **Paso 1: Activar Fuentes Desconocidas**
```
Configuración > Seguridad > Fuentes desconocidas ✅ ACTIVAR
```

#### **Paso 2: Configurar por App**
```
Configuración > Aplicaciones > [Tu Navegador] > Permisos > Instalar apps desconocidas ✅ ACTIVAR
```

#### **Paso 3: Desactivar Verificación**
```
Configuración > Seguridad > Verificación de seguridad ❌ DESACTIVAR
```

#### **Paso 4: Opciones de Desarrollador**
```
Configuración > Acerca del teléfono > Número de compilación (TOCAR 7 VECES)
Configuración > Opciones de desarrollador > Depuración USB ✅ ACTIVAR
```

---

## 💻 **SOLUCIÓN CON COMPUTADORA (ADB)**

### **Si tienes acceso a una computadora:**

#### **Instalar ADB:**
```bash
# En Linux/Mac:
sudo apt install android-tools-adb

# En Windows, descarga desde:
# https://developer.android.com/studio/releases/platform-tools
```

#### **Conectar y instalar:**
```bash
# Conectar teléfono por USB
adb devices

# Configurar para OPPO
adb shell settings put global package_verifier_enable 0
adb shell settings put secure install_non_market_apps 1

# Instalar APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### **O usar el script automático:**
```bash
# Hacer ejecutable
chmod +x install_oppo_reno.sh

# Ejecutar
./install_oppo_reno.sh
```

---

## 📱 **MÉTODOS ALTERNATIVOS**

### **Método 1: APK Installer**
1. Descarga "APK Installer" desde Google Play
2. Usa esta app para instalar el APK
3. Suele tener menos restricciones

### **Método 2: Firefox**
1. Usa Firefox en lugar de Chrome
2. Descarga el APK con Firefox
3. Instala desde Firefox

### **Método 3: Gestor de Archivos**
1. Transfiere APK por USB
2. Usa el gestor de archivos nativo
3. Instala directamente

---

## 🔧 **COMANDOS ADB AVANZADOS**

### **Verificar dispositivo:**
```bash
adb devices
adb shell getprop ro.product.model
```

### **Configuraciones específicas OPPO:**
```bash
# Desactivar verificación
adb shell settings put global package_verifier_enable 0

# Permitir fuentes desconocidas
adb shell settings put secure install_non_market_apps 1

# Configurar permisos
adb shell pm grant com.android.packageinstaller android.permission.REQUEST_INSTALL_PACKAGES
```

### **Instalación con diferentes métodos:**
```bash
# Método estándar
adb install app-debug.apk

# Método forzado
adb install -r -d app-debug.apk

# Sin verificación
adb install --bypass-low-target-sdk-block app-debug.apk
```

### **Restaurar configuraciones:**
```bash
# Reactivar verificación
adb shell settings put global package_verifier_enable 1
```

---

## 🎯 **RESUMEN DE SOLUCIÓN**

### **Orden de prioridad:**
1. ✅ **Configuración manual en teléfono** (más fácil)
2. ✅ **APK Installer** (app de terceros)
3. ✅ **Firefox** (navegador alternativo)
4. ✅ **ADB** (si tienes computadora)
5. ✅ **Gestor de archivos** (transfiriendo por USB)

### **Configuraciones clave:**
- 🔓 **Fuentes desconocidas** → ACTIVAR
- 🛡️ **Verificación de seguridad** → DESACTIVAR (temporalmente)
- 👨‍💻 **Opciones de desarrollador** → ACTIVAR
- 📱 **Depuración USB** → ACTIVAR (para ADB)

---

## ⚠️ **IMPORTANTE**

### **Después de instalar:**
1. ✅ **Reactiva** la verificación de seguridad
2. ✅ **Configura** los permisos de Ritsu
3. ✅ **Prueba** las funciones básicas

### **Si nada funciona:**
- 📞 Contacta soporte OPPO
- 🔧 Considera usar una app de terceros
- 💻 Usa ADB como último recurso

---

**✨ ¡Con estos comandos deberías poder instalar Ritsu AI en tu OPPO Reno 13 5G! ✨**