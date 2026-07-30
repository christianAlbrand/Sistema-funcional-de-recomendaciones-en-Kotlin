package com.example.sistemafuncionalderecomendacionesenkotlin.infrastructure

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*


fun generateTestData(): Triple<List<User>, List<Product>, List<Interaction>> {
    val categories = listOf("electronics", "books", "fashion", "home", "sports")

    val users = (1..1000).map { id ->
        User(
            id = id,
            name = "User_$id",
            preferredCategories = setOf(categories[id % categories.size]),
            blockedCategories = setOf(categories[(id + 2) % categories.size])
        )
    }

    val products = (1..10000).map { id ->
        Product(
            id = id,
            name = " Product  Name $id ",
            category = categories[id % categories.size],
            price = 10.0 + (id % 100),
            rating = 1.0 + (id % 5),
            stock = if (id % 10 == 0) 0 else 50,
            tags = setOf("Tag$id", "Popular")
        )
    }

    val interactions = (1..100000).map { id ->
        Interaction(
            userId = (id % 1000) + 1,
            productId = (id % 10000) + 1,
            type = InteractionType.values()[id % InteractionType.values().size],
            timestamp = 1600000000L + id
        )
    }

    return Triple(users, products, interactions)
}