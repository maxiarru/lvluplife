package com.lvluplife.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Locale

data class Account(val username: String, val character: Character)
data class Character(
    val generalLevel: Int,
    val characterClass: String,
    val attributes: Map<String, Attribute>,
)
data class Attribute(val level: Int, val xp: Int)

class AccountRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun load(onResult: (Result<Account?>) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(Result.failure(IllegalStateException("Sesión cerrada")))
        db.collection("accounts").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    onResult(Result.success(null))
                    return@addOnSuccessListener
                }
                val username = snapshot.getString("username")
                val rawCharacter = snapshot.get("character") as? Map<*, *>
                val rawAttributes = rawCharacter?.get("attributes") as? Map<*, *>
                if (username == null || rawCharacter == null || rawAttributes == null) {
                    onResult(Result.failure(IllegalStateException("Cuenta incompleta")))
                    return@addOnSuccessListener
                }
                val attributes = listOf("STR", "DEX", "CON", "INT", "WIT", "MEN").associateWith { key ->
                    val raw = rawAttributes[key] as? Map<*, *>
                    Attribute((raw?.get("level") as? Number)?.toInt() ?: 0, (raw?.get("xp") as? Number)?.toInt() ?: 0)
                }
                onResult(Result.success(Account(username, Character(
                    (rawCharacter["generalLevel"] as? Number)?.toInt() ?: 0,
                    rawCharacter["class"] as? String ?: "",
                    attributes,
                ))))
            }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun create(username: String, onResult: (Result<Account>) -> Unit) {
        if (!USERNAME.matches(username)) {
            onResult(Result.failure(IllegalArgumentException("Usá de 1 a 15 letras ASCII, números o guion bajo")))
            return
        }
        val uid = auth.currentUser?.uid ?: return onResult(Result.failure(IllegalStateException("Sesión cerrada")))
        val key = username.lowercase(Locale.ROOT)
        val accountRef = db.collection("accounts").document(uid)
        val nameRef = db.collection("usernames").document(key)
        val initialAttributes = listOf("STR", "DEX", "CON", "INT", "WIT", "MEN")
            .associateWith { mapOf("level" to 1, "xp" to 0) }
        val character = mapOf("generalLevel" to 1, "class" to "Novato", "attributes" to initialAttributes)

        db.batch()
            .set(nameRef, mapOf("uid" to uid))
            .set(accountRef, mapOf("username" to username, "usernameKey" to key, "character" to character))
            .commit()
            .addOnSuccessListener {
                onResult(Result.success(Account(username, Character(1, "Novato", initialAttributes.mapValues { Attribute(1, 0) }))))
            }
            .addOnFailureListener { error ->
                val message = if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
                    "No se pudo reservar el nombre; puede estar ocupado"
                else error.message ?: "No se pudo crear la cuenta"
                onResult(Result.failure(IllegalStateException(message, error)))
            }
    }

    companion object {
        private val USERNAME = Regex("[A-Za-z0-9_]{1,15}")
    }
}
