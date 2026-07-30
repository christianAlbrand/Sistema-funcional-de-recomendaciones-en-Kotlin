package com.example.sistemafuncionalderecomendacionesenkotlin.recommendation

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*
import com.example.sistemafuncionalderecomendacionesenkotlin.normalization.normalizeProductPipeline
import com.example.sistemafuncionalderecomendacionesenkotlin.result.AppResult
import com.example.sistemafuncionalderecomendacionesenkotlin.result.map
import com.example.sistemafuncionalderecomendacionesenkotlin.scoring.ExplanatoryScoringRule
import com.example.sistemafuncionalderecomendacionesenkotlin.validation.userValidations
import com.example.sistemafuncionalderecomendacionesenkotlin.validation.validate


fun generateRecommendations(
    user: User,
    products: List<Product>,
    interactions: List<Interaction>,
    rules: List<ExplanatoryScoringRule>,
    limit: Int
): AppResult<List<Recommendation>> {
    // 1. Validar al usuario
    return validate(user, userValidations).map { validUser ->
        // Usamos Sequence para optimizar el rendimiento (Punto 10)
        products.asSequence()
            // 2. Normalizar los productos
            .map { normalizeProductPipeline(it) }
            // 3. Eliminar productos sin stock
            .filter { it.stock > 0 }
            // 4. Excluir categorías bloqueadas
            .filterNot { it.category in validUser.blockedCategories }
            // 5 y 6. Calcular la puntuación de cada producto y recopilar razones
            .map { product ->
                val evaluations = rules.map { rule -> rule(validUser, product, interactions) }
                val totalScore = evaluations.sumOf { it.score }
                val reasons = evaluations.mapNotNull { it.reason }
                Recommendation(product, totalScore, reasons)
            }
            // 7. Eliminar productos con puntuación no positiva
            .filter { it.score > 0 }
            // 8 y 9. Ordenar de mayor a menor puntuación y resolver empates por rating y precio
            .sortedWith(
                compareByDescending<Recommendation> { it.score }
                    .thenByDescending { it.product.rating }
                    .thenBy { it.product.price }
            )
            // 10. Devolver únicamente la cantidad solicitada
            .take(limit)
            .toList()
    }
}