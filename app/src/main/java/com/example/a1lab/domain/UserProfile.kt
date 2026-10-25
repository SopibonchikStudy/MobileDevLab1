package com.example.a1lab.domain

data class UserProfile(
    val login: String,
    val displayName: String,
    val age: Int? = null,
    val avatar: String = "book"
)
