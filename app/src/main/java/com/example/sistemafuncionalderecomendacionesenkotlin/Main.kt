package com.example.sistemafuncionalderecomendacionesenkotlin
import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*
import com.example.sistemafuncionalderecomendacionesenkotlin.infrastructure.generateTestData
import com.example.sistemafuncionalderecomendacionesenkotlin.normalization.normalizeProductPipeline
import com.example.sistemafuncionalderecomendacionesenkotlin.profile.buildUserProfile
import com.example.sistemafuncionalderecomendacionesenkotlin.recommendation.*
import com.example.sistemafuncionalderecomendacionesenkotlin.recursive.flattenCategories
import com.example.sistemafuncionalderecomendacionesenkotlin.reporting.generateReport
import com.example.sistemafuncionalderecomendacionesenkotlin.result.fold
import com.example.sistemafuncionalderecomendacionesenkotlin.scoring.allScoringRules
import com.example.sistemafuncionalderecomendacionesenkotlin.similarity.findSimilarUsers
import kotlin.system.measureTimeMillis

fun main() {
    println("=====================================================")
    println("   MOTOR DE RECOMENDACIONES DE COMERCIO ELECTRONICO  ")
    println("=====================================================\n")

    println("--> Generando datos de prueba masivos...")
    val (users, products, interactions) = generateTestData()
    val sampleUser = users.first()
    val productsMap = products.associateBy { it.id }

    println("✓ Datos generados: ${users.size} usuarios, ${products.size} productos, ${interactions.size} interacciones.\n")

    println("-----------------------------------------------------")
    println("1. BENCHMARK DE RENDIMIENTO: List vs Sequence")
    println("-----------------------------------------------------")

    val timeList = measureTimeMillis {
        products
            .map { normalizeProductPipeline(it) }
            .filter { it.stock > 0 }
            .filterNot { it.category in sampleUser.blockedCategories }
            .map { product ->
                val evals = allScoringRules.map { rule -> rule(sampleUser, product, interactions) }
                Recommendation(product, evals.sumOf { it.score }, evals.mapNotNull { it.reason })
            }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(10)
    }

    val timeSequence = measureTimeMillis {
        products.asSequence()
            .map { normalizeProductPipeline(it) }
            .filter { it.stock > 0 }
            .filterNot { it.category in sampleUser.blockedCategories }
            .map { product ->
                val evals = allScoringRules.map { rule -> rule(sampleUser, product, interactions) }
                Recommendation(product, evals.sumOf { it.score }, evals.mapNotNull { it.reason })
            }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(10)
            .toList()
    }

    println("• Tiempo con List (Eager):     $timeList ms")
    println("• Tiempo con Sequence (Lazy):  $timeSequence ms")
    println("-> Resultado: Sequence proceso los datos sin crear colecciones intermedias innecesarias.\n")

    println("-----------------------------------------------------")
    println("2. GENERACION DE RECOMENDACIONES PARA EL USUARIO #${sampleUser.id}")
    println("-----------------------------------------------------")

    val result = generateRecommendations(
        user = sampleUser,
        products = products,
        interactions = interactions,
        rules = allScoringRules,
        limit = 3
    )

    result.fold(
        onSuccess = { recommendations ->
            println("Top Recomendaciones:")
            recommendations.forEachIndexed { index, rec ->
                println("\n  [#${index + 1}] ${rec.product.name} (Cat: ${rec.product.category})")
                println("      Puntuacion: ${rec.score}")
                println("      Razones:")
                rec.reasons.forEach { reason -> println("       - $reason") }
            }
        },
        onFailure = { errors ->
            println("❌ Error al generar recomendaciones:")
            errors.forEach { err -> println("   - $err") }
        }
    )

    println("\n-----------------------------------------------------")
    println("3. REPORTE ACUMULADO MEDIANTE FOLD (Punto 11 y 14)")
    println("-----------------------------------------------------")

    val sampleProducts = products.take(200).map { normalizeProductPipeline(it) }

    val finalAccumulator = sampleProducts.fold(RecommendationAccumulator()) { acc, product ->
        val evals = allScoringRules.map { rule -> rule(sampleUser, product, interactions) }
        val score = evals.sumOf { it.score }
        val reasons = evals.mapNotNull { it.reason }
        acc.accumulate(product, score, reasons)
    }

    val report = generateReport(finalAccumulator)
    println("Reporte Generado:")
    println("• Productos Evaluados: ${report.totalProductsEvaluated}")
    println("• Aceptados:            ${report.totalRecommendations}")
    println("• Puntuacion Promedio:  ${String.format("%.2f", report.averageScore)}")
    println("• Distribucion por Categoria: ${report.recommendationsByCategory}")
    println("• Causas de Rechazo:          ${report.rejectionReasons}")

    println("\n-----------------------------------------------------")
    println("4. DEMOSTRACIÓN DE RECURSIVIDAD (Categorías jerarquicas)")
    println("-----------------------------------------------------")

    val tree = CategoryNode(
        name = "Tecnología",
        children = listOf(
            CategoryNode(name = "Computadoras", children = listOf(CategoryNode(name = "Laptops"))),
            CategoryNode(name = "Celulares")
        )
    )

    val flatCategories = flattenCategories(tree)
    println("Árbol aplanado recursivamente sin usar ciclos:")
    println("• ${flatCategories.map { it.name }}")

    println("\n-----------------------------------------------------")
    println("5. PERFIL DE USUARIO Y SIMILITUD ENTRE USUARIOS")
    println("-----------------------------------------------------")

    val targetProfile = buildUserProfile(sampleUser.id, interactions, productsMap)
    val allProfiles = users.take(100).associate { user ->
        user.id to buildUserProfile(user.id, interactions, productsMap)
    }

    val similarUsers = findSimilarUsers(sampleUser, users.take(100), allProfiles, limit = 3)

    println("Usuarios mas similares a ${sampleUser.name}:")
    if (similarUsers.isEmpty()) {
        println(" - No se encontraron usuarios con preferencias compartidas.")
    } else {
        similarUsers.forEach { (user, similarity) ->
            val porcentaje = similarity * 100
            println(" • Usuario: ${user.name} | Coeficiente de Jaccard: ${String.format("%.2f", similarity)} (${porcentaje.toInt()}%)")
        }
    }

    println("\n=====================================================")
    println("      PROCESO DE RECOMENDACION FINALIZADO            ")
    println("=====================================================")

    result.fold(
        onSuccess = { recommendations ->
            com.example.sistemafuncionalderecomendacionesenkotlin.reporting.exportResultsToFile(recommendations)
        },
        onFailure = {}
    )
    com.example.sistemafuncionalderecomendacionesenkotlin.reporting.exportReportToJson(report)

    println("\n[OK] ¡Archivos 'resultados_recomendaciones.txt' y 'reporte.json' generados exitosamente!")
}