package com.example.sistemafuncionalderecomendacionesenkotlin.reporting

// TODOS los imports deben ir aquí arriba
import com.example.sistemafuncionalderecomendacionesenkotlin.domain.Product
import com.example.sistemafuncionalderecomendacionesenkotlin.domain.Recommendation
import com.example.sistemafuncionalderecomendacionesenkotlin.recommendation.RecommendationAccumulator
import java.io.File

data class RecommendationReport(
    val totalProductsEvaluated: Int,
    val totalRecommendations: Int,
    val averageScore: Double,
    val recommendationsByCategory: Map<String, Int>,
    val rejectionReasons: Map<String, Int>
)

fun generateReport(acc: RecommendationAccumulator): RecommendationReport {
    val avgScore = if (acc.accepted.isNotEmpty()) acc.totalScore / acc.accepted.size else 0.0

    val byCat = acc.accepted
        .groupingBy { it.product.category }
        .eachCount()

    val rejectionReasons = acc.rejected
        .groupingBy { it.reason }
        .eachCount()

    return RecommendationReport(
        totalProductsEvaluated = acc.evaluatedProducts,
        totalRecommendations = acc.accepted.size,
        averageScore = avgScore,
        recommendationsByCategory = byCat,
        rejectionReasons = rejectionReasons
    )
}

fun exportResultsToFile(recommendations: List<Recommendation>, filePath: String = "resultados_recomendaciones.txt") {
    val content = recommendations.joinToString(separator = "\n-------------------------\n") { rec ->
        """
        PRODUCTO: ${rec.product.name} (ID: ${rec.product.id})
        Categoría: ${rec.product.category} | Precio: $${rec.product.price} | Rating: ${rec.product.rating}
        Puntuación Total: ${rec.score}
        Razones:
        ${rec.reasons.joinToString("\n") { "  - $it" }}
        """.trimIndent()
    }
    File(filePath).writeText("=== RESULTADOS DE RECOMENDACIONES ===\n\n$content")
}

fun exportReportToJson(report: RecommendationReport, filePath: String = "reporte.json") {
    val catJson = report.recommendationsByCategory.entries.joinToString(",") { "\"${it.key}\": ${it.value}" }
    val rejJson = report.rejectionReasons.entries.joinToString(",") { "\"${it.key}\": ${it.value}" }

    val jsonContent = """
    {
      "totalProductsEvaluated": ${report.totalProductsEvaluated},
      "totalRecommendations": ${report.totalRecommendations},
      "averageScore": ${report.averageScore},
      "recommendationsByCategory": { $catJson },
      "rejectionReasons": { $rejJson }
    }
    """.trimIndent()

    File(filePath).writeText(jsonContent)
}