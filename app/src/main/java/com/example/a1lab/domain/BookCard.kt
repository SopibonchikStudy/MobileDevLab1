package com.example.a1lab.domain

data class BookCard(
    val id: Int,
    val title: String,
    val author: String,
    val genre: String,
    val year: Int,
    val rating: Double,
    val availability: BookAvailability,
    val coverUrl: String?
)

enum class BookAvailability {
    AVAILABLE,
    ON_LOAN,
    UNAVAILABLE
}
