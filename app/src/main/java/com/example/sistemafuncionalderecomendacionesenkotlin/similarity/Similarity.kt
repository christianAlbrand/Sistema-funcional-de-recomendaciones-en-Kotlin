package com.example.sistemafuncionalderecomendacionesenkotlin.similarity

import com.example.sistemafuncionalderecomendacionesenkotlin.domain.User
import com.example.sistemafuncionalderecomendacionesenkotlin.profile.UserProfile

/**
 * Calcula la similitud mediante el Coeficiente de Jaccard (Punto 9).
 * Jaccard = (Elementos en común) / (Total de elementos únicos combinados)
 */
fun calculateSimilarity(
    user1: User,
    profile1: UserProfile,
    user2: User,
    profile2: UserProfile
): Double {
    // Combinamos las categorías declaradas y las inferidas por compras
    val set1 = user1.preferredCategories + profile1.favoriteCategories + profile1.favoriteTags
    val set2 = user2.preferredCategories + profile2.favoriteCategories + profile2.favoriteTags

    if (set1.isEmpty() && set2.isEmpty()) return 0.0

    val intersection = set1.intersect(set2).size.toDouble()
    val union = set1.union(set2).size.toDouble()

    return if (union > 0.0) intersection / union else 0.0
}

/**
 * Encuentra a los usuarios más similares a un usuario objetivo.
 */
fun findSimilarUsers(
    targetUser: User,
    users: List<User>,
    profiles: Map<Int, UserProfile>,
    limit: Int
): List<Pair<User, Double>> {
    val targetProfile = profiles[targetUser.id] ?: return emptyList()

    return users.asSequence()
        .filter { it.id != targetUser.id } // Excluir al mismo usuario
        .map { otherUser ->
            val otherProfile = profiles[otherUser.id] ?: UserProfile(
                favoriteCategories = emptyList(),
                favoriteTags = emptyList(),
                averagePurchasePrice = 0.0,
                viewedProductIds = emptySet(),
                purchasedProductIds = emptySet(),
                interactionFrequency = emptyMap()
            )
            val similarity = calculateSimilarity(targetUser, targetProfile, otherUser, otherProfile)
            otherUser to similarity
        }
        .filter { it.second > 0.0 } // Solo mostrar si tienen algo en común
        .sortedByDescending { it.second }
        .take(limit)
        .toList()
}