package com.example.a1lab.data

import com.example.a1lab.domain.JokeCard
import com.example.a1lab.domain.JokeCategory
import com.example.a1lab.domain.JokeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JokeFeedPolicyTest {
    @Test
    fun duplicateIdsAreRemovedAndFirstVersionIsKept() {
        val first = joke(1, "First")
        val duplicate = joke(1, "Duplicate")
        val second = joke(2, "Second")

        val merged = JokeFeedPolicy.merge(listOf(first), listOf(duplicate, second))

        assertEquals(listOf(first, second), merged)
    }

    @Test
    fun feedNeverContainsMoreThanTwentyJokes() {
        val jokes = (1..25).map { joke(it, "Joke $it") }

        val merged = JokeFeedPolicy.merge(emptyList(), jokes)

        assertEquals(JokeFeedPolicy.MAX_JOKES, merged.size)
        assertEquals((1..20).toList(), merged.map { it.id })
    }

    @Test
    fun batchPolicyCanCollectMinimumFeedSize() {
        val batches = listOf(
            (1..9).map { joke(it, "Joke $it") },
            (10..18).map { joke(it, "Joke $it") }
        )

        val merged = batches.fold(emptyList<JokeCard>()) { current, batch ->
            JokeFeedPolicy.merge(current, batch)
        }

        assertTrue(merged.size >= JokeFeedPolicy.MIN_JOKES)
        assertEquals(18, merged.map { it.id }.distinct().size)
    }

    private fun joke(id: Int, setup: String) = JokeCard(
        id = id,
        category = JokeCategory.PROGRAMMING,
        type = JokeType.TWOPART,
        setup = setup,
        punchline = "Punchline",
        safe = true,
        language = "en",
        wordCount = 2
    )
}
