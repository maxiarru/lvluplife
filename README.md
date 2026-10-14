# Lvl Up Life

Aplicación Android en Kotlin y Jetpack Compose. Incluye ingreso con Google, nombre de usuario único y personaje inicial.

## Configuración de Firebase

1. La app usa el proyecto Firebase `liveluplife` y el ID Android `com.lvlup.life`. Su configuración está en `app/google-services.json`.
2. Habilitar **Authentication → Google** y agregar las huellas SHA-1 y SHA-256 de la clave de desarrollo. Si cambia la configuración de la app en Firebase, descargar el archivo actualizado y reemplazar `app/google-services.json`.
3. Crear Cloud Firestore y publicar las reglas de `firestore.rules` con Firebase CLI (`firebase deploy --only firestore:rules --project liveluplife`).
4. Abrir el proyecto en Android Studio. Usar JDK 17 y tener instalado Android SDK API 34.

Desde PowerShell, compilar con `.\gradlew.bat assembleDebug`. Para instalar en un dispositivo o emulador conectado, usar `.\gradlew.bat installDebug`.

Para probar las reglas de acceso localmente, instalar las dependencias de desarrollo con `npm install` y ejecutar `npm run test:firestore-rules` (requiere Java y el emulador de Firestore).

La app necesita conexión para autenticar, reservar el nombre y consultar la cuenta. El mismo UID de Firebase Authentication identifica a la cuenta; una escritura atómica crea una sola reserva de nombre y un solo personaje. Las reglas de Firestore limitan la lectura de la cuenta al propietario, impiden leer las reservas de nombres y no permiten cambios directos en la progresión.

El registro de actividades y el cálculo de XP aún no están implementados.
