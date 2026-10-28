package com.example.a1lab.domain

import kotlin.random.Random

object HardcodedJokes {
    val all: List<JokeCard> = listOf(
        twoPart(1, JokeCategory.PROGRAMMING, "Why do programmers prefer dark mode?", "Because light attracts bugs."),
        twoPart(2, JokeCategory.PROGRAMMING, "Why did the developer go broke?", "Because they used up all their cache."),
        twoPart(3, JokeCategory.PROGRAMMING, "Why do Java developers wear glasses?", "Because they do not C sharp."),
        twoPart(4, JokeCategory.PROGRAMMING, "How many programmers does it take to change a light bulb?", "None. That is a hardware problem."),
        twoPart(5, JokeCategory.PUN, "What do you call a fake noodle?", "An impasta."),
        twoPart(6, JokeCategory.PUN, "Why did the scarecrow win an award?", "Because he was outstanding in his field."),
        twoPart(7, JokeCategory.PUN, "What do you call cheese that is not yours?", "Nacho cheese."),
        twoPart(8, JokeCategory.MISC, "Why did the coffee file a police report?", "It got mugged."),
        twoPart(9, JokeCategory.MISC, "What do you call a bear with no teeth?", "A gummy bear."),
        twoPart(10, JokeCategory.MISC, "Why did the bicycle fall over?", "Because it was two-tired."),
        twoPart(11, JokeCategory.SPOOKY, "Why do ghosts love elevators?", "Because they lift their spirits."),
        twoPart(12, JokeCategory.SPOOKY, "What is a skeleton's favorite instrument?", "A trom-bone."),
        twoPart(13, JokeCategory.CHRISTMAS, "What do elves learn at school?", "The elf-abet."),
        twoPart(14, JokeCategory.CHRISTMAS, "Why was the Christmas tree so good at knitting?", "It had lots of needles."),
        twoPart(15, JokeCategory.DARK, "Why did the calendar take a holiday?", "It needed some time off."),
        twoPart(16, JokeCategory.MISC, "Why do ducks have feathers?", "To cover their quack-side.")
    )

    fun shuffled(random: Random = Random.Default): List<JokeCard> = all.shuffled(random)

    private fun twoPart(id: Int, category: JokeCategory, setup: String, punchline: String) = JokeCard(
        id = id,
        category = category,
        type = JokeType.TWOPART,
        setup = setup,
        punchline = punchline,
        safe = true,
        language = "en",
        wordCount = "$setup $punchline".split(Regex("\\s+")).count(String::isNotBlank)
    )
}
