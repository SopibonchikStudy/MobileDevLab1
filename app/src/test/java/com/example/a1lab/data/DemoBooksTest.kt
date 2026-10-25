package com.example.a1lab.data

import com.example.a1lab.domain.BookAvailability
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class DemoBooksTest {
    @Test
    fun catalogHasExactlyTwelveValidUniqueBooks() {
        assertEquals(12, demoBooks.size)
        assertEquals(12, demoBooks.map { it.id }.distinct().size)
        assertTrue(demoBooks.all { it.title.isNotBlank() && it.author.isNotBlank() && it.genre.isNotBlank() })
        assertTrue(demoBooks.all { it.rating in 0.0..5.0 })
        assertTrue(demoBooks.all { it.year in 1000..Calendar.getInstance().get(Calendar.YEAR) })
    }

    @Test
    fun catalogContainsAllAvailabilityStates() {
        assertEquals(BookAvailability.entries.toSet(), demoBooks.map { it.availability }.toSet())
    }
}
