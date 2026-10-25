package com.example.a1lab.data

import com.example.a1lab.domain.BookAvailability
import com.example.a1lab.domain.BookCard

val demoBooks: List<BookCard> = listOf(
    BookCard(1, "Мастер и Маргарита", "Михаил Булгаков", "Роман", 1967, 4.8, BookAvailability.AVAILABLE, null),
    BookCard(2, "Преступление и наказание", "Фёдор Достоевский", "Классика", 1866, 4.7, BookAvailability.ON_LOAN, null),
    BookCard(3, "Маленький принц", "Антуан де Сент-Экзюпери", "Повесть", 1943, 4.9, BookAvailability.AVAILABLE, null),
    BookCard(4, "1984", "Джордж Оруэлл", "Антиутопия", 1949, 4.6, BookAvailability.AVAILABLE, null),
    BookCard(5, "Гарри Поттер и философский камень", "Дж. К. Роулинг", "Фэнтези", 1997, 4.7, BookAvailability.UNAVAILABLE, null),
    BookCard(6, "Три товарища", "Эрих Мария Ремарк", "Роман", 1936, 4.6, BookAvailability.ON_LOAN, null),
    BookCard(7, "Дюна", "Фрэнк Герберт", "Фантастика", 1965, 4.7, BookAvailability.AVAILABLE, null),
    BookCard(8, "Пикник на обочине", "Аркадий и Борис Стругацкие", "Фантастика", 1972, 4.5, BookAvailability.AVAILABLE, null),
    BookCard(9, "Портрет Дориана Грея", "Оскар Уайльд", "Классика", 1890, 4.5, BookAvailability.ON_LOAN, null),
    BookCard(10, "Сто лет одиночества", "Габриэль Гарсиа Маркес", "Роман", 1967, 4.4, BookAvailability.UNAVAILABLE, null),
    BookCard(11, "Убить пересмешника", "Харпер Ли", "Роман", 1960, 4.6, BookAvailability.AVAILABLE, null),
    BookCard(12, "Солярис", "Станислав Лем", "Фантастика", 1961, 4.3, BookAvailability.AVAILABLE, null)
)
