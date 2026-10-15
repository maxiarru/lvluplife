package com.lvluplife.app

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ProgressEntry(
    val id: String, val date: LocalDate, val createdAtMillis: Long,
    val catalogId: String?, val minutes: Int, val primary: String, val secondary: String?,
)

data class ProgressResult(
    val attributes: Map<String, Attribute>, val generalLevel: Int,
    val generalSteps: Int, val characterClass: String,
    val dailyXp: Map<LocalDate, Map<String, Int>>,
)

object ProgressEngine {
    val attributes = listOf("STR", "DEX", "CON", "INT", "WIT", "MEN")
    val caps = mapOf("STR" to 48, "DEX" to 48, "CON" to 28, "INT" to 48, "WIT" to 48, "MEN" to 30)
    val pureClasses = mapOf("STR" to "Guerrero", "DEX" to "Explorador", "CON" to "Guardián",
        "INT" to "Sabio", "WIT" to "Monje", "MEN" to "Místico")
    val pairClasses = mapOf(
        setOf("STR", "DEX") to "Maestro de Armas", setOf("STR", "CON") to "Paladín",
        setOf("STR", "INT") to "Guerrero Arcano", setOf("STR", "WIT") to "Luchador Monje",
        setOf("STR", "MEN") to "Guerrero Místico", setOf("DEX", "CON") to "Ágil Guardián",
        setOf("DEX", "INT") to "Artífice", setOf("DEX", "WIT") to "Ninja",
        setOf("DEX", "MEN") to "Explorador de Sueños", setOf("CON", "INT") to "Guardián de la Sabiduría",
        setOf("CON", "WIT") to "Monje Guardián", setOf("CON", "MEN") to "Druida",
        setOf("INT", "WIT") to "Ilusionista", setOf("INT", "MEN") to "Oráculo",
        setOf("WIT", "MEN") to "Monje Espiritual",
    )
    val allClasses = listOf("Novato") + pureClasses.values + pairClasses.values +
        listOf("Aventurero Versátil", "Maestro del Equilibrio", "Héroe Ascendente", "Héroe Integral")

    fun classCondition(name: String): String = when (name) {
        "Novato" -> "Seis atributos en nivel 1"
        "Aventurero Versátil" -> "3 atributos equilibrados"
        "Maestro del Equilibrio" -> "4 atributos equilibrados"
        "Héroe Ascendente" -> "5 atributos equilibrados"
        "Héroe Integral" -> "6 atributos equilibrados"
        else -> pureClasses.entries.firstOrNull { it.value == name }?.key
            ?: pairClasses.entries.firstOrNull { it.value == name }?.key
                ?.sortedBy { attributes.indexOf(it) }?.joinToString(" + ")
            ?: ""
    }

    fun stage(level: Int) = when { level >= 35 -> 3; level >= 15 -> 2; else -> 1 }
    fun classFor(levels: Map<String, Int>): String {
        if (attributes.all { levels.getValue(it) == 1 }) return "Novato"
        val max = levels.values.max()
        val balanced = attributes.filter { levels.getValue(it) * 5 >= max * 4 }
        return when (balanced.size) {
            1 -> pureClasses.getValue(balanced.first())
            2 -> pairClasses.getValue(balanced.toSet())
            3 -> "Aventurero Versátil"
            4 -> "Maestro del Equilibrio"
            5 -> "Héroe Ascendente"
            else -> "Héroe Integral"
        }
    }

    private fun requiredSteps(level: Int) = when (level) {
        1 -> 1
        in 2..4 -> 2
        in 5..8 -> 3
        in 9..14 -> 4
        else -> 5
    }

    fun calculate(cycleStart: LocalDate, today: LocalDate, records: List<ProgressEntry>): ProgressResult {
        val state = attributes.associateWith { Attribute(1, 0) }.toMutableMap()
        val daily = mutableMapOf<LocalDate, Map<String, Int>>()
        val sorted = records.sortedWith(compareBy<ProgressEntry> { it.date }
            .thenBy { it.createdAtMillis }.thenBy { it.id })
        var general = 1
        var steps = 0
        val lastDay = today.coerceAtLeast(cycleStart)
        // Registros de ayer/anteayer creados después del reinicio aportan al ciclo nuevo.
        val firstDay = minOf(cycleStart, sorted.firstOrNull()?.date ?: cycleStart)
        var date = firstDay
        while (!date.isAfter(lastDay)) {
            val earned = attributes.associateWith { 0 }.toMutableMap()
            var meals = 0
            var sleepMinutes = 0
            sorted.asSequence().filter { it.date == date }.forEach { entry ->
                val raw = when (entry.catalogId) {
                    "MEAL" -> { meals++; if (meals <= 4) 7 else 0 }
                    "SLEEP" -> {
                        val before = sleepMinutes / 60
                        sleepMinutes += entry.minutes
                        (sleepMinutes / 60 - before) * 3
                    }
                    else -> entry.minutes / 5
                }
                val primaryXp = if (entry.secondary == null) raw else (raw * 2 + 2) / 3
                val contributions = listOf(entry.primary to primaryXp) +
                    (entry.secondary?.let { listOf(it to raw - primaryXp) } ?: emptyList())
                contributions.forEach { (key, candidate) ->
                    if (key !in caps) return@forEach
                    val effective = if (state.getValue(key).level == 50) 0 else
                        minOf(candidate, caps.getValue(key) - earned.getValue(key))
                    earned[key] = earned.getValue(key) + effective
                    val current = state.getValue(key)
                    if (current.level < 50) {
                        var level = current.level
                        var xp = current.xp + effective
                        while (level < 50 && xp >= level * 30) {
                            xp -= level * 30
                            level++
                            steps++
                            while (general < 50 && steps >= requiredSteps(general)) {
                                steps -= requiredSteps(general)
                                general++
                            }
                        }
                        state[key] = Attribute(level, if (level == 50) 0 else xp)
                    }
                }
            }
            daily[date] = earned.toMap()
            // La inactividad arranca en la fecha del ciclo, incluso si hubo registros atrasados.
            if (!date.isBefore(cycleStart) && date.isBefore(today)) {
                attributes.forEach { key ->
                    if (earned.getValue(key) == 0) {
                        val latest = daily.entries.asSequence().filter { (d, xp) ->
                            !d.isBefore(cycleStart) && d.isBefore(date) && xp.getValue(key) > 0
                        }.maxByOrNull { it.key }?.key
                        val missed = ChronoUnit.DAYS.between(latest ?: cycleStart.minusDays(1), date)
                        if (missed >= 7) {
                            val current = state.getValue(key)
                            state[key] = current.copy(xp = (current.xp - 5).coerceAtLeast(0))
                        }
                    }
                }
            }
            date = date.plusDays(1)
        }
        return ProgressResult(state, general, steps, classFor(state.mapValues { it.value.level }), daily)
    }
}
