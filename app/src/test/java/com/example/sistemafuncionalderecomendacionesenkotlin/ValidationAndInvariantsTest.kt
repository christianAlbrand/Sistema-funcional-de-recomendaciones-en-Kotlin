package com.example.sistemafuncionalderecomendacionesenkotlin

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*
import com.example.sistemafuncionalderecomendacionesenkotlin.recommendation.*
import com.example.sistemafuncionalderecomendacionesenkotlin.result.AppResult
import com.example.sistemafuncionalderecomendacionesenkotlin.scoring.allScoringRules
import com.example.sistemafuncionalderecomendacionesenkotlin.validation.*
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// 1. Pruebas de Invariantes y Validaciones
class ValidationAndInvariantsTest {

    @Test
    fun `INVARIANTE - Producto sin stock nunca debe ser recomendado`() {
        val user = User(1, "Test User", setOf("books"), emptySet())
        val outOfStockProduct = Product(1, "Book 1", "books", 20.0, 5.0, stock = 0, tags = emptySet())

        val result = generateRecommendations(user, listOf(outOfStockProduct), emptyList(), allScoringRules, 10)

        // Hacemos smart cast con 'if' para que Kotlin habilite .value sin ponerse rojo
        if (result is AppResult.Success) {
            assertTrue("La lista de recomendaciones debe estar vacía para productos sin stock", result.value.isEmpty())
        } else {
            assertTrue("El resultado debería ser Success", false)
        }
    }

    @Test
    fun `INVARIANTE - Categoria bloqueada nunca debe ser recomendada`() {
        val user = User(1, "Test User", setOf("books"), setOf("electronics"))
        val blockedProduct = Product(1, "Laptop", "electronics", 800.0, 5.0, stock = 10, tags = emptySet())

        val result = generateRecommendations(user, listOf(blockedProduct), emptyList(), allScoringRules, 10)

        if (result is AppResult.Success) {
            assertTrue("No se deben recomendar productos de categorías bloqueadas", result.value.isEmpty())
        } else {
            assertTrue("El resultado debería ser Success", false)
        }
    }

    @Test
    fun `INVARIANTE - El limite de resultados nunca debe ser superado`() {
        val user = User(1, "Test User", setOf("books"), emptySet())
        val products = (1..50).map {
            Product(it, "Book $it", "books", 15.0, 4.5, stock = 5, tags = emptySet())
        }

        val limit = 5
        val result = generateRecommendations(user, products, emptyList(), allScoringRules, limit)

        if (result is AppResult.Success) {
            assertEquals(limit, result.value.size)
        } else {
            assertTrue("El resultado debería ser Success", false)
        }
    }

    @Test
    fun `INVARIANTE - Acumulador fold conserva la suma de evaluados`() {
        val user = User(1, "Test User", setOf("books"), setOf("electronics"))
        val products = listOf(
            Product(1, "Book 1", "books", 10.0, 4.0, stock = 5, tags = emptySet()),       // Aceptado
            Product(2, "TV", "electronics", 300.0, 4.0, stock = 5, tags = emptySet()),     // Rechazado (Bloqueado)
            Product(3, "Book 2", "books", 12.0, 4.0, stock = 0, tags = emptySet())        // Rechazado (Sin stock)
        )

        val acc = products.fold(RecommendationAccumulator()) { accumulator, product ->
            val evals = allScoringRules.map { it(user, product, emptyList()) }
            val score = evals.sumOf { it.score }
            val reasons = evals.mapNotNull { it.reason }
            accumulator.accumulate(product, score, reasons)
        }

        assertEquals(3, acc.evaluatedProducts)
        assertEquals(acc.evaluatedProducts, acc.accepted.size + acc.rejected.size)
    }
}

// 2. Pruebas Parametrizadas
@RunWith(Parameterized::class)
class ParameterizedUserValidationTest(
    private val userId: Int,
    private val userName: String,
    private val preferred: Set<String>,
    private val blocked: Set<String>,
    private val shouldBeValid: Boolean
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(1, "Juan", setOf("tech"), emptySet<String>(), true),          // Válido
            arrayOf(-1, "Juan", setOf("tech"), emptySet<String>(), false),         // ID inválido
            arrayOf(2, "", setOf("tech"), emptySet<String>(), false),              // Nombre vacío
            arrayOf(3, "Pedro", emptySet<String>(), emptySet<String>(), false),    // Sin categorías preferidas
            arrayOf(4, "Ana", setOf("tech"), setOf("tech"), false)                 // Overlap preferida/bloqueada
        )
    }

    @Test
    fun `validar reglas de usuario parametrizadas`() {
        val user = User(userId, userName, preferred, blocked)
        val result = validate(user, userValidations)
        assertEquals(shouldBeValid, result is AppResult.Success)
    }
}