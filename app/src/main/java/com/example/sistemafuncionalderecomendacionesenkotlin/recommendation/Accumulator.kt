package com.example.sistemafuncionalderecomendacionesenkotlin.recommendation

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.Product
import com.example.sistemafuncionalderecomendacionesenkotlin.domain.Recommendation
import com.example.sistemafuncionalderecomendacionesenkotlin.domain.RejectedProduct

/**
 * Acumulador inmutable para procesar recomendaciones mediante fold (Punto 11).
 */
data class RecommendationAccumulator(
    val evaluatedProducts: Int = 0,
    val accepted: List<Recommendation> = emptyList(),
    val rejected: List<RejectedProduct> = emptyList(),
    val totalScore: Double = 0.0
) {
    /**
     * Devuelve una NUEVA instancia del acumulador agregando el resultado del producto evaluado.
     */
    fun accumulate(product: Product, score: Double, reasons: List<String>): RecommendationAccumulator {
        return if (score > 0) {
            val recommendation = Recommendation(product, score, reasons)
            copy(
                evaluatedProducts = evaluatedProducts + 1,
                accepted = accepted + recommendation,
                totalScore = totalScore + score
            )
        } else {
            val rejectionReason = reasons.firstOrNull() ?: "Puntuacion no positiva ($score)"
            val rejectedProduct = RejectedProduct(product, rejectionReason)
            copy(
                evaluatedProducts = evaluatedProducts + 1,
                rejected = rejected + rejectedProduct
            )
        }
    }
}