package com.example.sistemafuncionalderecomendacionesenkotlin.recursive

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.CategoryNode

fun flattenCategories(category: CategoryNode): List<CategoryNode> {
    return listOf(category) + category.children.flatMap { flattenCategories(it) }
}