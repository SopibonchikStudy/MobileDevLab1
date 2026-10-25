package com.example.a1lab.data

import com.example.a1lab.domain.BookCard

interface BookRepository {
    suspend fun getBooks(): List<BookCard>
    suspend fun refreshBooks(): List<BookCard>
    suspend fun replaceBooks(books: List<BookCard>)
    suspend fun clearCachedBooks()
}
