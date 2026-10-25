package com.example.a1lab.data

import com.example.a1lab.domain.JokeCard

interface JokeRepository {
    suspend fun getJokes(): List<JokeCard>
    suspend fun refreshJokes(): List<JokeCard>
    suspend fun replaceJokes(jokes: List<JokeCard>)
    suspend fun clearCachedJokes()
}

internal object JokeFeedPolicy {
    const val MIN_JOKES = 12
    const val MAX_JOKES = 20
    const val BATCH_SIZE = 10
    const val MAX_BATCHES = 3

    fun merge(current: List<JokeCard>, incoming: List<JokeCard>): List<JokeCard> =
        (current + incoming).distinctBy { it.id }.take(MAX_JOKES)
}
