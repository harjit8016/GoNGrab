package com.gongrab.menu.domain.validation

import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import com.gongrab.menu.domain.model.ValidationResult

object ValidationEngine {

    fun validateBranch(name: String, existingBranches: List<Branch>, currentId: String? = null): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Branch name cannot be empty.")
        }
        if (trimmed.length < 2) {
            return ValidationResult(false, "Branch name must be at least 2 characters long.")
        }
        val duplicate = existingBranches.any { it.name.equals(trimmed, ignoreCase = true) && it.id != currentId }
        if (duplicate) {
            return ValidationResult(false, "A branch with this name already exists.")
        }
        return ValidationResult(true)
    }

    fun validateCategory(name: String, existingCategories: List<Category>, currentId: String? = null): ValidationResult {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, "Category name cannot be empty.")
        }
        if (trimmed.length < 2) {
            return ValidationResult(false, "Category name must be at least 2 characters long.")
        }
        val duplicate = existingCategories.any { it.name.equals(trimmed, ignoreCase = true) && it.id != currentId }
        if (duplicate) {
            return ValidationResult(false, "A category with this name already exists.")
        }
        return ValidationResult(true)
    }

    fun validateMenuItem(
        name: String,
        categoryId: String,
        defaultPriceText: String,
        selectedBranchIds: Set<String>,
        branchPricesMap: Map<String, String>,
        existingItems: List<MenuItem>,
        currentId: String? = null
    ): ValidationResult {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return ValidationResult(false, "Item name cannot be empty.")
        }
        if (categoryId.isEmpty()) {
            return ValidationResult(false, "Please select a category for this item.")
        }
        val defaultPrice = defaultPriceText.toDoubleOrNull()
        if (defaultPrice == null || defaultPrice < 0) {
            return ValidationResult(false, "Please enter a valid base price (₹).")
        }
        if (selectedBranchIds.isEmpty()) {
            return ValidationResult(false, "Please select at least one branch for this item.")
        }
        for (bId in selectedBranchIds) {
            val priceStr = branchPricesMap[bId] ?: defaultPriceText
            val bPrice = priceStr.toDoubleOrNull()
            if (bPrice == null || bPrice < 0) {
                return ValidationResult(false, "Price for selected branch must be a valid non-negative number.")
            }
        }
        val duplicate = existingItems.any { it.name.equals(trimmedName, ignoreCase = true) && it.id != currentId }
        if (duplicate) {
            return ValidationResult(false, "An item with this name already exists.")
        }
        return ValidationResult(true)
    }
}
