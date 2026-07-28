package com.gongrab.menu.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Branch(
    val id: String,
    val name: String,
    val address: String = ""
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val displayOrder: Int = 999
)

@Serializable
data class BranchPriceConfig(
    val price: Double,
    val available: Boolean = true
)

@Serializable
data class MenuItem(
    val id: String,
    val name: String,
    val categoryId: String = "",
    val categoryName: String = "",
    val defaultPrice: Double = 0.0,
    val branches: Map<String, BranchPriceConfig> = emptyMap(),
    val displayOrder: Int = 999
)

@Serializable
data class MenuDataCache(
    val categories: List<Category> = emptyList(),
    val branches: List<Branch> = emptyList(),
    val items: List<MenuItem> = emptyList()
)

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
