package com.lvluplife.app

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

data class ActivityOption(
    val id: String, val name: String, val category: String, val kind: String,
    val primary: String, val secondary: String? = null, val custom: Boolean = false,
)

data class ActivityRecord(
    val id: String, val dayId: String, val date: String, val cycleId: String,
    val name: String, val kind: String, val minutes: Int, val primary: String,
    val secondary: String?, val catalogId: String?, val customId: String?,
    val createdAtMillis: Long,
)

object BasicActivities {
    val all = listOf(
        ActivityOption("STRENGTH", "Entrenamiento de fuerza", "Actividad física", "duration", "STR"),
        ActivityOption("AEROBIC", "Actividad aeróbica", "Actividad física", "duration", "DEX"),
        ActivityOption("MEAL", "Comida saludable", "Nutrición", "meal", "CON"),
        ActivityOption("READ", "Leer / estudiar", "Aprendizaje", "duration", "INT"),
        ActivityOption("HOBBY", "Ocio / hobby", "Bienestar", "duration", "WIT"),
        ActivityOption("SLEEP", "Dormir", "Descanso", "duration", "MEN"),
        ActivityOption("CROSSFIT", "CrossFit", "Actividad física", "duration", "STR", "DEX"),
        ActivityOption("MARTIAL_ARTS", "Artes marciales", "Actividad física", "duration", "DEX", "STR"),
        ActivityOption("YOGA", "Yoga", "Bienestar físico", "duration", "WIT", "DEX"),
        ActivityOption("PRACTICAL_SKILL", "Aprender una habilidad práctica", "Aprendizaje", "duration", "INT", "DEX"),
        ActivityOption("STRATEGY", "Juegos de estrategia", "Ocio / aprendizaje", "duration", "WIT", "INT"),
    )
}

class ActivityRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun account() = db.collection("accounts").document(auth.currentUser?.uid ?: error("Sesión cerrada"))
    private fun days() = account().collection("days")
    private fun stamp(date: LocalDate) = Timestamp(Date.from(date.atStartOfDay(ZoneOffset.UTC).toInstant()))
    private fun dayId(cycleId: String, date: LocalDate) = "${cycleId}_${stamp(date).toDate().time}"

    fun today(onResult: (Result<LocalDate>) -> Unit) {
        val ref = account().collection("clock").document("server")
        ref.set(mapOf("now" to FieldValue.serverTimestamp()))
            .addOnSuccessListener {
                ref.get(Source.SERVER).addOnSuccessListener { snapshot ->
                    val instant = snapshot.getTimestamp("now")?.toDate()?.toInstant()
                    if (instant == null) onResult(Result.failure(IllegalStateException("Reloj del servidor no disponible")))
                    else onResult(Result.success(instant.atZone(ZoneOffset.ofHours(-3)).toLocalDate()))
                }.addOnFailureListener { onResult(Result.failure(it)) }
            }.addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun createCustom(name: String, category: String, primary: String, secondary: String?, onResult: (Result<Unit>) -> Unit) {
        val cleanedName = name.trim()
        val cleanedCategory = category.trim()
        if (cleanedName.length !in 1..50 || cleanedCategory.isEmpty() || primary !in ATTRIBUTES ||
            (secondary != null && (secondary !in ATTRIBUTES || secondary == primary))) {
            onResult(Result.failure(IllegalArgumentException("Revisá el nombre, la categoría y los atributos")))
            return
        }
        account().collection("customActivities").document().set(mapOf(
            "name" to cleanedName, "category" to cleanedCategory, "kind" to "duration",
            "primary" to primary, "secondary" to secondary, "createdAt" to FieldValue.serverTimestamp(),
        )).addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    fun mutate(action: String, record: ActivityRecord?, date: LocalDate?, option: ActivityOption?,
               hours: Int?, minutes: Int?, onResult: (Result<Unit>) -> Unit) {
        if (action !in setOf("create", "edit", "delete") || (action != "create" && record == null) ||
            (action != "delete" && (date == null || option == null))) {
            onResult(Result.failure(IllegalArgumentException("Operación inválida"))); return
        }
        val duration = if (option?.kind == "meal" || action == "delete") 0 else {
            if (hours == null || minutes == null || hours !in 0..24 || minutes !in 0..59 || hours * 60 + minutes !in 1..1440) {
                onResult(Result.failure(IllegalArgumentException("Duración inválida"))); return
            }
            hours * 60 + minutes
        }
        val recordId = record?.id ?: account().collection("ids").document().id
        val accountRef = account()
        db.runTransaction { tx ->
            val accountSnap = tx.get(accountRef)
            if (!accountSnap.exists()) error("Creá tu personaje primero")
            val cycleId = accountSnap.getString("currentCycleId") ?: "initial"
            if (record != null && record.cycleId != cycleId) error("El registro es de un ciclo anterior")
            val oldRef = record?.let { days().document(it.dayId) }
            val newRef = if (action == "delete") null else days().document(dayId(cycleId, date!!))
            val oldSnap = oldRef?.let { tx.get(it) }
            val newSnap = if (newRef == oldRef) oldSnap else newRef?.let { tx.get(it) }
            val oldEntries = entries(oldSnap)
            val previous = if (record == null) null else oldEntries[recordId] ?: error("Registro inexistente")
            if (previous != null && previous["kind"] != option?.kind && action == "edit") error("No se puede convertir comida")
            val fresh = if (action == "delete") null else mapOf<String, Any?>(
                "name" to option!!.name, "category" to option.category, "kind" to option.kind,
                "catalogId" to if (option.custom) null else option.id,
                "customId" to if (option.custom) option.id else null,
                "primary" to option.primary, "secondary" to option.secondary, "minutes" to duration,
                "createdAt" to (previous?.get("createdAt") ?: FieldValue.serverTimestamp()),
            )
            if (oldRef != null && oldRef != newRef) {
                oldEntries.remove(recordId)
                tx.set(oldRef, dayData(cycleId, LocalDate.parse(record.date), oldEntries,
                    recordId, newRef?.id))
            }
            if (newRef != null) {
                val newEntries = if (newRef == oldRef) oldEntries else entries(newSnap)
                newEntries[recordId] = fresh!!
                tx.set(newRef, dayData(cycleId, date!!, newEntries, recordId,
                    if (oldRef != null && oldRef != newRef) oldRef.id else null))
            }
            Unit
        }.addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { onResult(Result.failure(it)) }
    }

    private fun entries(snapshot: DocumentSnapshot?): MutableMap<String, Map<String, Any?>> {
        @Suppress("UNCHECKED_CAST")
        return ((snapshot?.get("entries") as? Map<String, Map<String, Any?>>).orEmpty()).toMutableMap()
    }

    private fun dayData(cycleId: String, date: LocalDate, entries: Map<String, Map<String, Any?>>,
                        changedId: String, movePeer: String?): Map<String, Any?> = mapOf(
        "cycleId" to cycleId, "day" to stamp(date), "entries" to entries,
        "totalMinutes" to entries.values.sumOf { (it["minutes"] as? Number)?.toInt() ?: 0 },
        "changedId" to changedId, "movePeer" to movePeer,
    )

    fun watchCustom(onResult: (Result<List<ActivityOption>>) -> Unit): ListenerRegistration =
        account().collection("customActivities").addSnapshotListener { snapshot, error ->
            if (error != null) { onResult(Result.failure(error)); return@addSnapshotListener }
            onResult(Result.success(snapshot?.documents.orEmpty().map { doc ->
                ActivityOption(doc.id, doc.getString("name").orEmpty(), doc.getString("category").orEmpty(),
                    "duration", doc.getString("primary").orEmpty(), doc.getString("secondary"), true)
            }.sortedBy { it.name.lowercase() }))
        }

    fun watchHistory(start: LocalDate, endExclusive: LocalDate, onResult: (Result<List<ActivityRecord>>) -> Unit): ListenerRegistration =
        days().whereGreaterThanOrEqualTo("day", stamp(start)).whereLessThan("day", stamp(endExclusive))
            .addSnapshotListener { snapshot, error ->
                if (error != null) { onResult(Result.failure(error)); return@addSnapshotListener }
                onResult(Result.success(snapshot.toRecords()))
            }

    private fun QuerySnapshot?.toRecords(): List<ActivityRecord> = this?.documents.orEmpty().flatMap { day ->
        @Suppress("UNCHECKED_CAST")
        val data = day.get("entries") as? Map<String, Map<String, Any?>> ?: emptyMap()
        val date = day.getTimestamp("day")?.toDate()?.toInstant()?.atZone(ZoneOffset.UTC)?.toLocalDate()?.toString().orEmpty()
        data.map { (id, entry) ->
            ActivityRecord(id, day.id, date, day.getString("cycleId").orEmpty(),
                entry["name"] as? String ?: "", entry["kind"] as? String ?: "",
                (entry["minutes"] as? Number)?.toInt() ?: 0, entry["primary"] as? String ?: "",
                entry["secondary"] as? String, entry["catalogId"] as? String,
                entry["customId"] as? String, (entry["createdAt"] as? Timestamp)?.toDate()?.time ?: 0L)
        }
    }.sortedWith(compareByDescending<ActivityRecord> { it.date }.thenByDescending { it.createdAtMillis }.thenBy { it.id })

    companion object { private val ATTRIBUTES = setOf("STR", "DEX", "INT", "WIT", "MEN") }
}
