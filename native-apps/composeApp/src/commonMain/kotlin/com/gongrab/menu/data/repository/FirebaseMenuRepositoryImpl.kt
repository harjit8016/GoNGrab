package com.gongrab.menu.data.repository

import com.gongrab.menu.domain.model.AnimatedSvgItem
import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import com.gongrab.menu.domain.repository.MenuRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Firebase-backed MenuRepository providing real-time data sync via Kotlin Flows.
 * Follows SRP: Solely responsible for syncing data from Firestore.
 */
class FirebaseMenuRepositoryImpl : MenuRepository {
    private val firestore = Firebase.firestore

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    override val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    override val items: StateFlow<List<MenuItem>> = _items.asStateFlow()

    private val _animatedSvgPack = MutableStateFlow<List<AnimatedSvgItem>>(emptyList())
    override val animatedSvgPack: StateFlow<List<AnimatedSvgItem>> = _animatedSvgPack.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch { reloadData() }
    }

    override suspend fun reloadData() {
        // Real-time branches sync
        scope.launch {
            try {
                firestore.collection("branches").snapshots.collect { querySnapshot ->
                    _branches.value = querySnapshot.documents.map { it.data<Branch>() }
                }
            } catch (e: Exception) {
                println("Firebase sync error [branches]: ${e.message}")
            }
        }

        // Real-time categories sync
        scope.launch {
            try {
                firestore.collection("categories").snapshots.collect { querySnapshot ->
                    _categories.value = querySnapshot.documents
                        .map { it.data<Category>() }
                        .sortedBy { it.displayOrder }
                }
            } catch (e: Exception) {
                println("Firebase sync error [categories]: ${e.message}")
            }
        }

        // Real-time items sync (Fetch all items, branch availability filtered in UI)
        scope.launch {
            try {
                firestore.collection("items")
                    .snapshots.collect { querySnapshot ->
                        _items.value = querySnapshot.documents
                            .map { it.data<MenuItem>() }
                            .sortedBy { it.displayOrder }
                    }
            } catch (e: Exception) {
                println("Firebase sync error [items]: ${e.message}")
            }
        }
    }

    // Write operations are generally not needed for the TV display app (read-only)
    override suspend fun addBranch(branch: Branch) {}
    override suspend fun updateBranch(branch: Branch) {}
    override suspend fun deleteBranch(branchId: String) {}
    override suspend fun duplicateBranch(sourceBranchId: String, newBranch: Branch) {}

    override suspend fun addCategory(category: Category) {}
    override suspend fun updateCategory(category: Category) {}
    override suspend fun deleteCategory(categoryId: String) {}

    override suspend fun addMenuItem(item: MenuItem) {}
    override suspend fun updateMenuItem(item: MenuItem) {}
    override suspend fun deleteMenuItem(itemId: String) {}

    override suspend fun saveAnimatedSvgToPack(item: AnimatedSvgItem) {}
}
