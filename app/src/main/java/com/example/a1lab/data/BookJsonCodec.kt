package com.example.a1lab.data

import com.example.a1lab.domain.BookAvailability
import com.example.a1lab.domain.BookCard
import org.json.JSONArray
import org.json.JSONObject

internal object BookJsonCodec {
    fun encode(books: List<BookCard>): JSONArray = JSONArray().apply {
        books.forEach { book ->
            put(JSONObject().apply {
                put("id", book.id)
                put("title", book.title)
                put("author", book.author)
                put("genre", book.genre)
                put("year", book.year)
                put("rating", book.rating)
                put("availability", book.availability.name)
                put("coverUrl", book.coverUrl ?: JSONObject.NULL)
            })
        }
    }

    fun decode(array: JSONArray): List<BookCard> = buildList {
        for (index in 0 until array.length()) {
            val item = array.getJSONObject(index)
            val availability = BookAvailability.valueOf(item.getString("availability"))
            val book = BookCard(
                id = item.getInt("id"),
                title = item.getString("title"),
                author = item.getString("author"),
                genre = item.getString("genre"),
                year = item.getInt("year"),
                rating = item.getDouble("rating"),
                availability = availability,
                coverUrl = item.optString("coverUrl").takeIf { it.isNotBlank() && it != "null" }
            )
            require(book.id > 0 && book.title.isNotBlank() && book.author.isNotBlank())
            add(book)
        }
    }
}
