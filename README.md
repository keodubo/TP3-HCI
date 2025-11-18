# Comprartir Mobile – Guía rápida

Este repositorio contiene la versión Android (Jetpack Compose) de Comprartir. El objetivo de este README es explicar únicamente cómo compilarla, ejecutarla, registrar usuarios y conectar la API junto con el servicio de mailing basado en Ethereal.

## 1. Cómo compilar
1. Instala Android Studio Giraffe (o superior) con el SDK 34.
2. Clona el repositorio y abre una terminal en la raíz.
3. Ejecuta `./gradlew assemblePhoneDebug assembleTabletDebug` para generar los APKs de desarrollo.  
   - Si solo necesitas un sabor específico, usa `./gradlew assemblePhoneDebug` o `./gradlew assembleTabletDebug`.

## 2. Cómo ejecutar
1. Lanza Android Studio y abre el proyecto.
2. Sincroniza Gradle (`Sync Project with Gradle Files`).
3. Selecciona el sabor que quieras probar (`phoneDebug` o `tabletDebug`).
4. Conecta un dispositivo/emulador y pulsa **Run ▶**.  
   También puedes usar la terminal: `./gradlew installPhoneDebug` para instalar directamente en el dispositivo conectado.

## 3. Cómo registrar usuarios
1. Inicia la app y presiona **Register** en la pantalla de inicio de sesión.
2. Completa nombre, correo y contraseña siguiendo las validaciones mostradas.
3. Tras confirmar, revisa el correo de verificación (ver sección Ethereal) y sigue el enlace para activar la cuenta.
4. Una vez verificada, vuelve a la app e inicia sesión con las nuevas credenciales.

## 4. Cómo conectar la API
1. Define la URL base del backend en `local.properties` u `./.env.android`:
   ```
   comprartir.apiBaseUrl=https://tu-servidor/api
   ```
2. Si no se define, la app usa `http://10.0.2.2:8080/api` (localhost para el emulador).
3. El módulo `NetworkModule` lee este valor y lo expone vía `BuildConfig.COMPRARTIR_API_BASE_URL`, así que no es necesario modificar código adicional.

## 5. Cómo configurar Ethereal para el mailing
1. Crea una cuenta de pruebas en [https://ethereal.email](https://ethereal.email) y copia el usuario/contraseña SMTP generados.
2. En el backend, añade las credenciales al archivo `.env` o variables de entorno esperadas (por ejemplo):
   ```
   MAIL_HOST=smtp.ethereal.email
   MAIL_PORT=587
   MAIL_USER=tu-usuario@ethereal.email
   MAIL_PASS=tu-contraseña
   ```
3. Reinicia el backend y verifica que el endpoint de registro envíe correos utilizando Ethereal.
4. Durante las pruebas, abre el panel web de Ethereal y consulta la bandeja “Messages” para revisar los enlaces de verificación enviados por la app.

Con estos pasos puedes compilar, ejecutar y vincular el flujo de registro completo con el backend y el servicio de correos de prueba.
