package com.lvluplife.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class ProgressEngineTest {
    private val day = LocalDate.of(2026, 10, 14)
    private fun entry(id: String, date: LocalDate = day, minutes: Int = 30,
                      primary: String = "STR", secondary: String? = null,
                      catalogId: String = "STRENGTH", created: Long = 1L) =
        ProgressEntry(id, date, created, catalogId, minutes, primary, secondary)

    @Test fun durationSplitAndDailyCap() {
        val entries = (1..9).map { entry("e$it") } + entry("split", minutes = 10, secondary = "DEX")
        val result = ProgressEngine.calculate(day, day, entries)
        assertEquals(48, result.dailyXp[day]?.get("STR"))
        assertEquals(0, result.dailyXp[day]?.get("DEX"))
        assertEquals(2, result.attributes.getValue("STR").level)
        assertEquals(18, result.attributes.getValue("STR").xp)
    }

    @Test fun sleepAccumulatesOnlyWithinDateAndMealsStopAtFour() {
        val entries = listOf(
            entry("s1", minutes = 30, primary = "MEN", catalogId = "SLEEP"),
            entry("s2", minutes = 30, primary = "MEN", catalogId = "SLEEP", created = 2),
            entry("s3", day.minusDays(1), 30, "MEN", catalogId = "SLEEP"),
        ) + (1..5).map { entry("m$it", minutes = 0, primary = "CON", catalogId = "MEAL", created = it.toLong()) }
        val result = ProgressEngine.calculate(day.minusDays(1), day, entries)
        assertEquals(3, result.dailyXp[day]?.get("MEN"))
        assertEquals(0, result.dailyXp[day.minusDays(1)]?.get("MEN"))
        assertEquals(28, result.dailyXp[day]?.get("CON"))
    }

    @Test fun correctionRebuildsAndInactivityHasFloor() {
        val original = ProgressEngine.calculate(day.minusDays(10), day, listOf(entry("a", day.minusDays(10), 60)))
        val corrected = ProgressEngine.calculate(day.minusDays(10), day, listOf(entry("a", day.minusDays(1), 60)))
        assertEquals(0, original.attributes.getValue("STR").xp)
        assertEquals(12, corrected.attributes.getValue("STR").xp)
        assertEquals(12, ProgressEngine.calculate(day.minusDays(10), day, listOf(entry("a", day.minusDays(1), 60))).attributes.getValue("STR").xp)
    }

    @Test fun generalLevelAndClassAndVisualStage() {
        val result = ProgressEngine.calculate(day, day, listOf(entry("a", minutes = 300)))
        assertEquals(2, result.attributes.getValue("STR").level)
        assertEquals(2, result.generalLevel)
        assertEquals("Guerrero", result.characterClass)
        assertEquals(1, ProgressEngine.stage(14))
        assertEquals(2, ProgressEngine.stage(15))
        assertEquals(2, ProgressEngine.stage(34))
        assertEquals(3, ProgressEngine.stage(35))
    }

    @Test fun mealsAfterFourthAndZeroXpDoNotChangeProgress() {
        val fiveMeals = (1..5).map { entry("meal$it", minutes = 0, primary = "CON", catalogId = "MEAL", created = it.toLong()) }
        val fourMeals = fiveMeals.dropLast(1)
        val first = ProgressEngine.calculate(day, day, fiveMeals)
        val second = ProgressEngine.calculate(day, day, fourMeals)
        assertEquals(second.attributes, first.attributes)
        assertEquals(28, first.dailyXp[day]?.get("CON"))
    }

    @Test fun classBalanceUsesExactEightyPercent() {
        val levels = ProgressEngine.attributes.associateWith { 1 }.toMutableMap()
        levels["STR"] = 10
        levels["DEX"] = 8
        assertEquals("Maestro de Armas", ProgressEngine.classFor(levels))
        levels["DEX"] = 7
        assertEquals("Guerrero", ProgressEngine.classFor(levels))
    }

    @Test fun inactivityStartsOnSeventhClosedDayAndNeverLowersLevel() {
        val firstDay = day.minusDays(12)
        val activity = entry("start", firstDay, 210)
        val beforePenalty = ProgressEngine.calculate(firstDay, firstDay.plusDays(7), listOf(activity))
        val seventhClosed = ProgressEngine.calculate(firstDay, firstDay.plusDays(8), listOf(activity))
        val eighthClosed = ProgressEngine.calculate(firstDay, firstDay.plusDays(9), listOf(activity))
        assertEquals(Attribute(2, 12), beforePenalty.attributes.getValue("STR"))
        assertEquals(Attribute(2, 7), seventhClosed.attributes.getValue("STR"))
        assertEquals(Attribute(2, 2), eighthClosed.attributes.getValue("STR"))
        assertEquals(Attribute(2, 0), ProgressEngine.calculate(firstDay, firstDay.plusDays(10), listOf(activity)).attributes.getValue("STR"))
    }

    @Test fun zeroXpDoesNotInterruptInactivityAndBackdatedXpRebuildsIt() {
        val firstDay = day.minusDays(12)
        val activity = entry("start", firstDay, 60)
        val zeroXp = entry("zero", firstDay.plusDays(7), 4)
        val afterSeventhClosed = firstDay.plusDays(8)
        val penalized = ProgressEngine.calculate(firstDay, afterSeventhClosed, listOf(activity, zeroXp))
        assertEquals(7, penalized.attributes.getValue("STR").xp)

        val backdated = entry("backdated", firstDay.plusDays(7), 5, created = 2)
        val rebuilt = ProgressEngine.calculate(firstDay, afterSeventhClosed, listOf(activity, zeroXp, backdated))
        assertEquals(13, rebuilt.attributes.getValue("STR").xp)
        assertEquals(1, rebuilt.dailyXp[firstDay.plusDays(7)]?.get("STR"))
    }
}
