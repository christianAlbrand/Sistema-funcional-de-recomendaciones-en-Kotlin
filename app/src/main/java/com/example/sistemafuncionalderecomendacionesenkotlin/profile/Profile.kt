package com.example.sistemafuncionalderecomendacionesenkotlin.profile

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*

data class UserProfile(
    val favoriteCategories: List<String>,
    val favoriteTags: List<String>,
    val averagePurchasePrice: Double,
    val viewedProductIds: Set<Int>,
    val purchasedProductIds: Set<Int>,
    val interactionFrequency: Map<InteractionType, Int>
)

fun buildUserProfile(
    userId: Int,
    interactions: List<Interaction>,
    productsMap: Map<Int, Product>
): UserProfile {
    val userInteractions = interactions.filter { it.userId == userId }

    val frequency = userInteractions.groupingBy { it.type }.eachCount()

    val viewed = userInteractions.filter { it.type == InteractionType.VIEW }.map { it.productId }.toSet()
    val purchased = userInteractions.filter { it.type == InteractionType.PURCHASE }.map { it.productId }.toSet()

    val purchasedProducts = purchased.mapNotNull { productsMap[it] }
    val avgPrice = if (purchasedProducts.isNotEmpty()) purchasedProducts.map { it.price }.average() else 0.0

    val topCategories = purchasedProducts
        .groupingBy { it.category }
        .eachCount()
        .entries.sortedByDescending { it.value }
        .map { it.key }

    val topTags = purchasedProducts
        .flatMap { it.tags }
        .groupingBy { it }
        .eachCount()
        .entries.sortedByDescending { it.value }
        .map { it.key }

    return UserProfile(
        favoriteCategories = topCategories,
        favoriteTags = topTags,
        averagePurchasePrice = avgPrice,
        viewedProductIds = viewed,
        purchasedProductIds = purchased,
        interactionFrequency = frequency
    )
}