package com.example.a1lab.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HardcodedJokesTest {
    @Test
    fun catalogContainsEnoughUniqueLocalJokes() {
        assertTrue(HardcodedJokes.all.size >= 12)
        assertEquals(HardcodedJokes.all.size, HardcodedJokes.all.map(JokeCard::id).distinct().size)
        assertTrue(HardcodedJokes.all.all { it.setup.isNotBlank() && it.punchline?.isNotBlank() == true })
    }
}
