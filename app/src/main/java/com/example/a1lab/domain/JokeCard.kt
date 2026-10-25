package com.example.a1lab.domain

data class JokeCard(
    val id: Int,
    val category: JokeCategory,
    val type: JokeType,
    val setup: String,
    val punchline: String?,
    val safe: Boolean,
    val language: String,
    val wordCount: Int
)

enum class JokeCategory {
    PROGRAMMING,
    MISC,
    DARK,
    PUN,
    SPOOKY,
    CHRISTMAS,
    UNKNOWN
}

enum class JokeType {
    SINGLE,
    TWOPART,
    UNKNOWN
}
