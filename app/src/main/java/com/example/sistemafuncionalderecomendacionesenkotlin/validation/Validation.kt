package com.example.sistemafuncionalderecomendacionesenkotlin.validation

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.*
import com.example.sistemafuncionalderecomendacionesenkotlin.result.AppResult

typealias ValidationRule<T> = (T) -> List<String>

fun <T> validate(value: T, rules: List<ValidationRule<T>>): AppResult<T> {
    val errors = rules.flatMap { rule -> rule(value) }
    return if (errors.isEmpty()) AppResult.Success(value) else AppResult.Failure(errors)
}

// Reglas de Usuario
val userValidations: List<ValidationRule<User>> = listOf(
    { user -> if (user.id <= 0) listOf("El id debe ser positivo.") else emptyList() },
    { user -> if (user.name.isBlank()) listOf("El nombre no puede estar vacio.") else emptyList() },
    { user -> if (user.preferredCategories.isEmpty()) listOf("Debe tener al menos una categoría preferida.") else emptyList() },
    { user ->
        val overlap = user.preferredCategories.intersect(user.blockedCategories)
        if (overlap.isNotEmpty()) listOf("Las categorias no pueden estar en preferencia y bloqueo a la vez: $overlap") else emptyList()
    }
)

// Reglas de Producto
val productValidations: List<ValidationRule<Product>> = listOf(
    { p -> if (p.id <= 0) listOf("ID debe ser positivo") else emptyList() },
    { p -> if (p.name.isBlank()) listOf("Nombre no puede estar vacio") else emptyList() },
    { p -> if (p.price <= 0.0) listOf("Precio debe ser mayor a cero") else emptyList() },
    { p -> if (p.rating !in 0.0..5.0) listOf("Calificacion debe estar entre 0 y 5") else emptyList() },
    { p -> if (p.stock < 0) listOf("Stock no puede ser negativo") else emptyList() },
    { p -> if (p.category.isBlank()) listOf("Categoria no puede estar vacia") else emptyList() }
)