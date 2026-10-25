package com.example.a1lab.data

import com.example.a1lab.domain.UserProfile

interface UserRepository {
    suspend fun register(profile: UserProfile, password: CharArray): Result<Unit>
    suspend fun authenticate(login: String, password: CharArray): Result<UserProfile>
    suspend fun getCurrentProfile(): UserProfile?
    suspend fun updateProfile(profile: UserProfile): Result<Unit>
}
