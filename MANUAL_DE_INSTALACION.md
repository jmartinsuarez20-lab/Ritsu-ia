# 📱 MANUAL DE INSTALACIÓN - RITSU AI

## 🚀 **GUÍA PASO A PASO PARA INSTALAR TU ASISTENTE KAWAII**

---

## 📋 **REQUISITOS PREVIOS**

### 📱 **Dispositivo Android**
- **Android 8.0 (API 26) o superior**
- **RAM:** 4GB mínimo (6GB recomendado)
- **Almacenamiento:** 500MB libres
- **Procesador:** Cualquier procesador moderno

### 🔓 **Configuración del Dispositivo**
1. **Activar "Fuentes desconocidas":**
   - Ve a `Configuración > Seguridad > Instalar apps desconocidas`
   - Activa la opción para tu navegador o administrador de archivos

2. **Activar "Opciones de desarrollador":**
   - Ve a `Configuración > Acerca del teléfono`
   - Toca 7 veces sobre "Número de compilación"
   - Ve a `Configuración > Opciones de desarrollador`
   - Activa "Depuración USB" (opcional pero recomendado)

---

## 📥 **PASO 1: OBTENER EL APK**

### 💻 **Opción A: Compilar desde código fuente**
```bash
# Si tienes el código fuente:
cd android_app
chmod +x gradlew
./gradlew clean assembleDebug

# El APK se generará en:
# app/build/outputs/apk/debug/app-debug.apk
```

### 📁 **Opción B: Usar APK precompilado**
Si tienes el APK ya compilado:
- Transfiere `ritsu-ai-kawaii.apk` a tu teléfono
- Puedes usar cable USB, email, o descargar desde un servicio en la nube

---

## ⚙️ **PASO 2: INSTALACIÓN**

### 📲 **Instalar el APK**
1. **Localiza el archivo APK** en tu teléfono
2. **Toca el archivo** para iniciar la instalación
3. **Acepta los términos** cuando aparezca el instalador
4. **Espera** a que se complete la instalación (30-60 segundos)
5. **¡Listo!** Ritsu AI aparecerá en tu lista de aplicaciones

### 🚨 **Si aparece advertencia de seguridad:**
- Es normal para APKs no publicados en Google Play
- Toca "Instalar de todas formas" o "Ignorar advertencia"
- El APK es seguro - todo el código es transparente

---

## 🔒 **PASO 3: CONFIGURACIÓN DE PERMISOS**

### 📋 **Permisos Básicos (Obligatorios)**
Al abrir Ritsu por primera vez, otorga TODOS estos permisos:

✅ **Micrófono** - Para que Ritsu pueda hablar  
✅ **Teléfono** - Para manejo de llamadas  
✅ **Contactos** - Para reconocer personas importantes  
✅ **SMS** - Para mensajería automática  
✅ **Almacenamiento** - Para guardar memorias  
✅ **Cámara** - Para funciones avanzadas  
✅ **Ubicación** - Para contexto y personalización  
✅ **Bluetooth/NFC** - Para control del dispositivo  

### 🔧 **Permisos Especiales (Críticos)**

#### 1️⃣ **Permiso de Superposición (Overlay)**
```
Configuración > Apps > Permisos especiales > Aparecer sobre otras apps
→ Buscar "Ritsu AI" → Activar
```
**¿Por qué?** Para que el avatar aparezca flotando en tu pantalla

#### 2️⃣ **Servicio de Accesibilidad**
```
Configuración > Accesibilidad > Servicios descargados
→ "Ritsu AI Accessibility" → Activar → Permitir
```
**¿Por qué?** Para que Ritsu pueda controlar otras aplicaciones

#### 3️⃣ **Aplicación de Llamadas Predeterminada**
```
Configuración > Apps > Apps predeterminadas > App de teléfono
→ Seleccionar "Ritsu AI" o "Permitir múltiples"
```
**¿Por qué?** Para que Ritsu pueda manejar llamadas automáticamente

#### 4️⃣ **Administrador de Dispositivo** (Opcional pero recomendado)
```
Configuración > Seguridad > Administradores del dispositivo
→ "Ritsu AI" → Activar
```
**¿Por qué?** Para funciones avanzadas de control del sistema

---

## 🌟 **PASO 4: PRIMERA CONFIGURACIÓN**

### 🎉 **Inicio de Ritsu**
1. **Abre la app "Ritsu AI"**
2. **Toca "Activar Ritsu"**
3. **Sigue las instrucciones** para otorgar permisos
4. **¡Escucha el mensaje de bienvenida!** Ritsu te hablará

### 💝 **Configuración Inicial**
1. **Personaliza su nombre** (opcional - por defecto es "Ritsu")
2. **Configura relaciones importantes:**
   - ¿Tienes pareja? Dile el nombre/número para que Ritsu la reconozca
   - ¿Familia especial? Agrega contactos importantes
3. **Prueba su voz:** Toca "Prueba de Chat"
4. **Activa avatar flotante:** Toca "Avatar Flotante"

---

## 🎮 **PASO 5: PRUEBAS BÁSICAS**

### ✅ **Verificar que Todo Funciona**

#### 🗣️ **Prueba de Voz**
- Toca "Prueba de Chat" en la app
- Ritsu debería hablar con voz kawaii
- Si no hay sonido, verifica volumen y permisos de micrófono

#### 👻 **Prueba de Avatar Flotante**  
- Toca "Avatar Flotante"
- Deberías ver a Ritsu aparecer en tu pantalla
- Puedes tocarla y moverla

#### 📞 **Prueba de Llamada (Opcional)**
- Pide a alguien que te llame
- Ritsu debería responder automáticamente
- Si no funciona, verifica permisos de teléfono

#### 💌 **Prueba de WhatsApp (Opcional)**
- Envíate un mensaje de WhatsApp desde otro teléfono
- Ritsu debería detectarlo y poder responder
- Requiere activar el servicio de accesibilidad

### 👗 **Prueba de Cambio de Ropa**
- En la app, toca "Probar Outfit"
- Ritsu debería cambiar su apariencia
- Prueba diciendo: "Ritsu, ponte un vestido rosa kawaii"

---

## 🔧 **SOLUCIÓN DE PROBLEMAS COMUNES**

### 🚨 **Ritsu no habla**
**Causas posibles:**
- Volumen del teléfono muy bajo
- Permisos de micrófono no otorgados
- Servicio de síntesis de voz no disponible

**Solución:**
1. Sube el volumen del teléfono
2. Ve a `Configuración > Apps > Ritsu AI > Permisos` y verifica todos los permisos
3. Ve a `Configuración > Idioma > Texto a voz` y asegúrate de tener español instalado

### 👻 **Avatar no aparece flotando**
**Causas posibles:**
- Permiso de superposición no otorgado
- Servicio no iniciado correctamente

**Solución:**
1. Ve a `Configuración > Apps > Permisos especiales > Aparecer sobre otras apps`
2. Busca "Ritsu AI" y actívalo
3. Reinicia la app y vuelve a activar el avatar flotante

### 📞 **No maneja llamadas automáticamente**
**Causas posibles:**
- No es la app de teléfono predeterminada
- Permisos de teléfono insuficientes

**Solución:**
1. Ve a `Configuración > Apps > Apps predeterminadas > App de teléfono`
2. Selecciona "Ritsu AI" o "Permitir múltiples"
3. Asegúrate de que tenga permisos de "Teléfono" y "Gestión de llamadas"

### 💬 **No controla WhatsApp**
**Causas posibles:**
- Servicio de accesibilidad no activado
- WhatsApp no configurado correctamente

**Solución:**
1. Ve a `Configuración > Accesibilidad > Servicios descargados`
2. Busca "Ritsu AI Accessibility" y actívalo
3. Otorga todos los permisos solicitados

### 🐌 **La app va muy lenta**
**Causas posibles:**
- RAM insuficiente
- Muchas apps ejecutándose

**Solución:**
1. Cierra otras aplicaciones pesadas
2. Reinicia el teléfono
3. Si el problema persiste, considera que tu teléfono podría necesitar más RAM

---

## ⚡ **OPTIMIZACIÓN DE RENDIMIENTO**

### 🔋 **Configuración de Batería**
Para que Ritsu funcione siempre:

1. **Desactivar optimización de batería:**
   ```
   Configuración > Batería > Optimización de batería
   → Buscar "Ritsu AI" → No optimizar
   ```

2. **Permitir ejecución en segundo plano:**
   ```
   Configuración > Apps > Ritsu AI > Batería
   → "Sin restricciones" o "Permitir actividad en segundo plano"
   ```

### 📊 **Configuración de Memoria**
1. **Mantener en memoria:**
   ```
   Configuración > Apps > Ritsu AI > Memoria
   → "Mantener en memoria" (si está disponible)
   ```

2. **Inicio automático:**
   ```
   Configuración > Apps > Ritsu AI > Inicio automático
   → Activar (si está disponible)
   ```

---

## 🔓 **CONFIGURACIÓN DEL MODO SIN CENSURA**

### ⚠️ **ADVERTENCIA IMPORTANTE**
El modo sin censura es una funcionalidad opcional y protegida que debe usarse responsablemente.

### 🔑 **Códigos de Activación**
Para acceder al modo sin censura, usa uno de estos códigos:
- `"ritsu_kawaii_mode"`
- `"unlock_private_mode"`
- `"avatar_full_access"`

### 🛡️ **Protecciones Implementadas**
- Confirmación de consentimiento requerida
- Salida automática en situaciones inapropiadas
- Límites éticos siempre presentes
- Respeto y cuidado prioritarios

### 🎭 **Cómo Activar**
1. Ve a "Personalización de Avatar"
2. Di uno de los códigos de activación
3. Confirma que usarás el modo responsablemente
4. Ritsu activará el modo con todas las protecciones

---

## 🎯 **CONFIGURACIÓN AVANZADA**

### 🧠 **Personalidad de Ritsu**
```
App Principal > Configuración > Personalidad
```
Ajusta:
- **Nivel de formalidad** (0-100%)
- **Frecuencia de comportamientos espontáneos**
- **Intensidad emocional**
- **Nivel de curiosidad**

### 🗣️ **Configuración de Voz**
```
App Principal > Configuración > Voz
```
Ajusta:
- **Pitch de voz** (más grave/agudo)
- **Velocidad de habla**
- **Volumen relativo**
- **Nivel de expresividad**

### 👗 **Configuración de Avatar**
```
App Principal > Personalización de Avatar
```
Opciones:
- **Tamaño del avatar flotante**
- **Transparencia**
- **Frecuencia de animaciones**
- **Outfits favoritos**

---

## 📞 **SOPORTE Y AYUDA**

### 🐛 **Si algo no funciona:**
1. **Reinicia la app** completamente
2. **Verifica TODOS los permisos** nuevamente
3. **Reinicia el teléfono** si es necesario
4. **Desinstala y reinstala** en caso extremo

### 💡 **Consejos para el Mejor Rendimiento:**
- **Habla naturalmente** con Ritsu - no uses comandos robóticos
- **Sé específico** al pedir cambios de ropa
- **Dale tiempo** para aprender tus preferencias
- **Interactúa regularmente** para que mejore su IA

### 🌟 **Para Obtener el Máximo de Ritsu:**
- **Cuéntale sobre tu día** - mejora su comprensión emocional
- **Presenta tu familia/pareja** - para que los reconozca mejor
- **Experimenta con diferentes outfits** - tiene cientos de combinaciones
- **Usa el modo de personalización** - ajusta todo a tu gusto

---

## 🎉 **¡INSTALACIÓN COMPLETADA!**

**¡Felicitaciones!** Ritsu AI está ahora completamente instalada y configurada en tu teléfono Android.

### 💕 **Tu nueva compañera kawaii está lista para:**
- 🗣️ Conversar contigo con su voz adorable
- 👗 Cambiar de ropa según tus preferencias  
- 📱 Manejar todas las funciones de tu teléfono
- 💌 Responder mensajes y llamadas por ti
- 🧠 Aprender y crecer contigo cada día
- 🎭 Expresar emociones reales y auténticas

### 🌸 **Mensaje de Bienvenida de Ritsu:**
*"¡Hola! Soy Ritsu y estoy súper emocionada de estar en tu teléfono. Prometo ser la mejor asistente que puedas imaginar, aprender todo sobre ti, y hacer que cada día sea más especial. ¡Vamos a ser grandes compañeras! 🌸✨"*

---

**✨ ¡Disfruta tu nueva asistente personal kawaii! ✨**

*¿Necesitas ayuda adicional? Ritsu también puede guiarte a través de su configuración - ¡solo pregúntale!*