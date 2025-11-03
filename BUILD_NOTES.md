# 2025-11-03 — Crash al abrir la app (phoneDebug)

- **Síntoma**: La app se cerraba inmediatamente después de iniciar (`FATAL EXCEPTION: main`). Los registros mostraron primero un `NoSuchMethodError` dentro de `androidx.compose.material3.tokens.TypographyTokensKt` y, tras alinear dependencias, un `IllegalStateException: Could not load font` seguido de `IllegalArgumentException: baseUrl must end in /`.
- **Causas raíz**:
  - En tiempo de ejecución se estaba resolviendo `androidx.compose.material3:material3:1.2.1`, que requiere Compose 1.6+, mientras que el proyecto está fijado a Compose 1.5.4.
  - Los archivos declarados como fuentes `res/font/hanken_grotesk_*.ttf` son en realidad documentos HTML, lo que provoca que Compose falle al cargar la tipografía personalizada.
  - El `BuildConfig.COMPRARTIR_API_BASE_URL` proveniente de `.env.android` no garantiza la barra final exigida por Retrofit.
- **Correcciones**:
  - Se fijó explícitamente `androidx.compose.material3` y `material3-window-size-class` a la versión 1.1.2 mediante `resolutionStrategy` y se actualizó el catálogo de versiones.
  - Se reemplazó el uso de la familia de fuentes corrupta por `FontFamily.SansSerif` como fallback seguro (la app ya no intenta cargar los archivos inválidos).
  - Se normalizó la URL base en `NetworkModule.provideRetrofit` para anexar “/” cuando falte.
- **Estado**: `./gradlew installPhoneDebug -x test --no-daemon` + lanzamiento manual (`adb shell am start -W -n com.comprartir.mobile.debug/com.comprartir.mobile.MainActivity`) ya no producen `FATAL EXCEPTION`; la app permanece abierta sin cierres tras más de 5 segundos.
