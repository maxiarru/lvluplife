package com.lvluplife.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val accounts by lazy { AccountRepository(auth) }
    private val credentials by lazy { CredentialManager.create(this) }
    private var state by mutableStateOf<ScreenState>(ScreenState.Loading)
    private var busy by mutableStateOf(false)
    private var error by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Scaffold { padding -> AccountScreen(
                    state = state,
                    busy = busy,
                    error = error,
                    padding = padding,
                    onSignIn = ::signIn,
                    onCreate = ::createAccount,
                    onSignOut = ::signOut,
                ) }
            }
        }
        refresh()
    }

    private fun refresh() {
        error = null
        if (auth.currentUser == null) {
            state = ScreenState.SignedOut
            return
        }
        state = ScreenState.Loading
        accounts.load { result ->
            result.onSuccess { state = if (it == null) ScreenState.NeedsUsername else ScreenState.Ready(it) }
                .onFailure { error = it.message ?: "No se pudo cargar la cuenta"; state = ScreenState.LoadFailed }
        }
    }

    private fun signIn() {
        if (busy) return
        busy = true
        error = null
        lifecycleScope.launch {
            try {
                val option = GetGoogleIdOption.Builder()
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
                val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
                val credential = credentials.getCredential(this@MainActivity, request).credential
                require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    "Credencial de Google inválida"
                }
                val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null))
                    .addOnSuccessListener { busy = false; refresh() }
                    .addOnFailureListener { busy = false; error = it.message ?: "No se pudo iniciar sesión" }
            } catch (exception: Exception) {
                busy = false
                error = exception.message ?: "No se pudo iniciar sesión"
            }
        }
    }

    private fun createAccount(username: String) {
        if (busy) return
        busy = true
        error = null
        accounts.create(username) { result ->
            busy = false
            result.onSuccess { state = ScreenState.Ready(it) }
                .onFailure { error = it.message ?: "No se pudo crear la cuenta" }
        }
    }

    private fun signOut() {
        auth.signOut()
        state = ScreenState.SignedOut
        error = null
        lifecycleScope.launch {
            try { credentials.clearCredentialState(ClearCredentialStateRequest()) }
            catch (_: Exception) { /* La sesión de Firebase ya está cerrada. */ }
        }
    }
}

private sealed interface ScreenState {
    data object Loading : ScreenState
    data object SignedOut : ScreenState
    data object NeedsUsername : ScreenState
    data object LoadFailed : ScreenState
    data class Ready(val account: Account) : ScreenState
}

@Composable
private fun AccountScreen(
    state: ScreenState,
    busy: Boolean,
    error: String?,
    padding: PaddingValues,
    onSignIn: () -> Unit,
    onCreate: (String) -> Unit,
    onSignOut: () -> Unit,
) {
    var username by androidx.compose.runtime.remember(state) { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text("Lvl Up Life", style = MaterialTheme.typography.headlineLarge)
        when (state) {
            ScreenState.Loading -> CircularProgressIndicator()
            ScreenState.SignedOut -> {
                Text("Convertí tus actividades en progreso.")
                Button(onClick = onSignIn, enabled = !busy) { Text("Continuar con Google") }
            }
            ScreenState.NeedsUsername -> {
                Text("Elegí tu nombre de usuario")
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de usuario") },
                    supportingText = { Text("1 a 15 letras ASCII, números o guion bajo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = { onCreate(username) }, enabled = !busy && Regex("[A-Za-z0-9_]{1,15}").matches(username)) {
                    Text("Crear personaje")
                }
                Button(onClick = onSignOut, enabled = !busy) { Text("Cerrar sesión") }
            }
            ScreenState.LoadFailed -> {
                Text("No se pudo cargar tu cuenta. Reabrí la aplicación para intentar otra vez.")
                Button(onClick = onSignOut) { Text("Cerrar sesión") }
            }
            is ScreenState.Ready -> {
                Text("Hola, ${state.account.username}", style = MaterialTheme.typography.headlineMedium)
                Text("${state.account.character.characterClass} · Nivel ${state.account.character.generalLevel}")
                state.account.character.attributes.forEach { (name, attribute) ->
                    Text("$name · Nivel ${attribute.level} · ${attribute.xp} XP")
                }
                Button(onClick = onSignOut) { Text("Cerrar sesión") }
            }
        }
        if (busy) CircularProgressIndicator()
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
    }
}
