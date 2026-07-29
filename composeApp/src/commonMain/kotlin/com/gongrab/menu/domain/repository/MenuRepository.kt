package com.gongrab.menu.domain.repository

import com.gongrab.menu.domain.model.AnimatedSvgItem
import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import kotlinx.coroutines.flow.StateFlow

interface MenuRepository {
    val branches: StateFlow<List<Branch>>
    val categories: StateFlow<List<Category>>
    val items: StateFlow<List<MenuItem>>
    val animatedSvgPack: StateFlow<List<AnimatedSvgItem>>

    suspend fun addBranch(branch: Branch)
    suspend fun updateBranch(branch: Branch)
    suspend fun deleteBranch(branchId: String)
    suspend fun duplicateBranch(sourceBranchId: String, newBranch: Branch)

    suspend fun addCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategory(categoryId: String)

    suspend fun addMenuItem(item: MenuItem)
    suspend fun updateMenuItem(item: MenuItem)
    suspend fun deleteMenuItem(itemId: String)
    
    suspend fun saveAnimatedSvgToPack(item: AnimatedSvgItem)
    suspend fun reloadData()
}
