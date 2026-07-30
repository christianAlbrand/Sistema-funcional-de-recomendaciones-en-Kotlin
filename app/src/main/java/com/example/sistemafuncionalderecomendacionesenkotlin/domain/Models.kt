package com.example.sistemafuncionalderecomendacionesenkotlin.domain
data class User(
    val id: Int,
    val name: String,
    val preferredCategories: Set<String>,
    val blockedCategories: Set<String>
)

data class Product(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val tags: Set<String>
)

enum class InteractionType {
    VIEW, FAVORITE, PURCHASE, REMOVE_FROM_CART
}

data class Interaction(
    val userId: Int,
    val productId: Int,
    val type: InteractionType,
    val timestamp: Long
)

data class RuleEvaluation(
    val score: Double,
    val reason: String?
)

data class Recommendation(
    val product: Product,
    val score: Double,
    val reasons: List<String>
)

data class RejectedProduct(
    val product: Product,
    val reason: String
)

data class CategoryNode(
    val name: String,
    val children: List<CategoryNode> = emptyList()
)