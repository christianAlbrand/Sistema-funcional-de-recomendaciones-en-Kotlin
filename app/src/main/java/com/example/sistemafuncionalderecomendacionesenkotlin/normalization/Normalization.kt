package com.example.sistemafuncionalderecomendacionesenkotlin.normalization

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.Product

import kotlin.math.round

infix fun <A, B, C> ((A) -> B).then(next: (B) -> C): (A) -> C = { value -> next(this(value)) }

fun normalizeName(product: Product): Product =
    product.copy(name = product.name.trim().replace("\\s+".toRegex(), " "))

fun normalizeCategory(product: Product): Product =
    product.copy(category = product.category.trim().lowercase())

fun normalizeTags(product: Product): Product =
    product.copy(
        tags = product.tags.asSequence()
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .toSet()
    )

fun roundPrice(product: Product): Product =
    product.copy(price = round(product.price * 100) / 100.0)

val normalizeProductPipeline: (Product) -> Product =
    ::normalizeName then ::normalizeCategory then ::normalizeTags then ::roundPrice