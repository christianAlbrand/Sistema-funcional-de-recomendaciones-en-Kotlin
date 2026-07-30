package com.example.sistemafuncionalderecomendacionesenkotlin.scoring

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*

typealias ExplanatoryScoringRule = (User, Product, List<Interaction>) -> RuleEvaluation

val preferredCategoryRule: ExplanatoryScoringRule = { user, product, _ ->
    if (product.category in user.preferredCategories)
        RuleEvaluation(30.0, "Coincide con una categoria preferida (${product.category})")
    else RuleEvaluation(0.0, null)
}

val blockedCategoryRule: ExplanatoryScoringRule = { user, product, _ ->
    if (product.category in user.blockedCategories)
        RuleEvaluation(-100.0, "Pertenece a una categoria bloqueada")
    else RuleEvaluation(0.0, null)
}

val ratingRule: ExplanatoryScoringRule = { _, product, _ ->
    val score = product.rating * 5.0
    if (product.rating >= 4.0) RuleEvaluation(score, "El producto tiene una alta calificacion (${product.rating})")
    else RuleEvaluation(score, null)
}

val purchaseHistoryRule: ExplanatoryScoringRule = { user, product, interactions ->
    val userPurchases = interactions.filter { it.userId == user.id && it.type == InteractionType.PURCHASE }
    if (userPurchases.any { it.productId == product.id })
        RuleEvaluation(-50.0, "Ya fue comprado recientemente")
    else RuleEvaluation(0.0, null)
}

val cartRemovalPenaltyRule: ExplanatoryScoringRule = { user, product, interactions ->
    val removed = interactions.any { it.userId == user.id && it.productId == product.id && it.type == InteractionType.REMOVE_FROM_CART }
    if (removed) RuleEvaluation(-20.0, "Fue eliminado previamente del carrito") else RuleEvaluation(0.0, null)
}

val outOfStockPenaltyRule: ExplanatoryScoringRule = { _, product, _ ->
    if (product.stock == 0) RuleEvaluation(-1000.0, "Sin stock disponible") else RuleEvaluation(0.0, null)
}

val allScoringRules = listOf(
    preferredCategoryRule,
    blockedCategoryRule,
    ratingRule,
    purchaseHistoryRule,
    cartRemovalPenaltyRule,
    outOfStockPenaltyRule
)