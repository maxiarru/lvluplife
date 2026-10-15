package com.lvluplife.app

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.time.LocalDate
import java.time.ZoneOffset

class ProgressRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun account() = db.collection("accounts").document(auth.currentUser?.uid ?: error("Sesión cerrada"))

    fun watch(today: LocalDate, onResult: (Result<ProgressResult>) -> Unit): ListenerRegistration {
        var recordsListener: ListenerRegistration? = null
        val accountListener = account().addSnapshotListener { account, accountError ->
            if (accountError != null) { onResult(Result.failure(accountError)); return@addSnapshotListener }
            val cycle = account?.getString("currentCycleId") ?: "initial"
            val start = account?.getTimestamp("cycleStartedAt")?.toDate()?.toInstant()
                ?.atZone(ZoneOffset.ofHours(-3))?.toLocalDate()
            // One query per cycle; a reset keeps earlier days available to HistoryView.
            recordsListener?.remove()
            recordsListener = account().collection("days").whereEqualTo("cycleId", cycle)
                .addSnapshotListener { days, error ->
                    if (error != null) { onResult(Result.failure(error)); return@addSnapshotListener }
                    val records = days?.documents.orEmpty().flatMap { day ->
                        val date = day.getTimestamp("day")?.toDate()?.toInstant()?.atZone(ZoneOffset.UTC)
                            ?.toLocalDate() ?: return@flatMap emptyList<ProgressEntry>()
                        @Suppress("UNCHECKED_CAST")
                        val entries = day.get("entries") as? Map<String, Map<String, Any?>> ?: emptyMap()
                        entries.map { (id, entry) -> ProgressEntry(id, date,
                            (entry["createdAt"] as? Timestamp)?.toDate()?.time ?: 0L,
                            entry["catalogId"] as? String, (entry["minutes"] as? Number)?.toInt() ?: 0,
                            entry["primary"] as? String ?: "", entry["secondary"] as? String) }
                    }
                    val effectiveStart = start ?: minOf(today, records.minOfOrNull { it.date } ?: today)
                    onResult(runCatching { ProgressEngine.calculate(effectiveStart, today, records) })
                }
        }
        return ListenerRegistration { accountListener.remove(); recordsListener?.remove() }
    }

    fun reset(onResult: (Result<Unit>) -> Unit) {
        val cycle = account().collection("ids").document().id
        val initial = ProgressEngine.attributes.associateWith { mapOf("level" to 1, "xp" to 0) }
        account().update(mapOf(
            "currentCycleId" to cycle,
            "cycleStartedAt" to FieldValue.serverTimestamp(),
            "character" to mapOf("generalLevel" to 1, "class" to "Novato", "attributes" to initial),
        )).addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }
}
