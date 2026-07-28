package com.gongrab.menu.domain.repository

import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import kotlinx.coroutines.flow.StateFlow

interface MenuRepository {
    val branches: StateFlow<List<Branch>>
    val categories: StateFlow<List<Category>>
    val items: StateFlow<List<MenuItem>>

    suspend fun addBranch(branch: Branch)
    suspend fun updateBranch(branch: Branch)
    suspend fun deleteBranch(branchId: String)

    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)

    suspend fun addMenuItem(item: MenuItem)
    suspend fun updateMenuItem(item: MenuItem)
    suspend fun deleteMenuItem(itemId: String)
    
    suspend fun reloadData()
}
