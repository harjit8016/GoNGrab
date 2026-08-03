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
                    _branches.value = querySnapshot.documents.map { doc ->
                        val b = doc.data<Branch>()
                        if (b.id.isBlank()) b.copy(id = doc.id) else b
                    }
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
                        .map { doc ->
                            val c = doc.data<Category>()
                            if (c.id.isBlank()) c.copy(id = doc.id) else c
                        }
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
                            .map { doc ->
                                val item = doc.data<MenuItem>()
                                val finalId = if (item.id.isBlank()) doc.id else item.id
                                item.copy(id = finalId)
                            }
                            .sortedBy { it.displayOrder }
                    }
            } catch (e: Exception) {
                println("Firebase sync error [items]: ${e.message}")
            }
        }
    }

    override suspend fun addBranch(branch: Branch) {
        try { firestore.collection("branches").document(branch.id).set(branch) } catch (e: Exception) { println("Firestore addBranch error: ${e.message}") }
    }
    override suspend fun updateBranch(branch: Branch) {
        try { firestore.collection("branches").document(branch.id).set(branch) } catch (e: Exception) { println("Firestore updateBranch error: ${e.message}") }
    }
    override suspend fun deleteBranch(branchId: String) {
        try {
            println("Firestore deleteBranch: $branchId")
            firestore.collection("branches").document(branchId).delete()

            _items.value.filter { it.branches.containsKey(branchId) }.forEach { item ->
                val updatedBranches = item.branches.filterKeys { it != branchId }
                val updatedItem = item.copy(branches = updatedBranches)
                firestore.collection("items").document(item.id).set(updatedItem)
            }
        } catch (e: Exception) {
            println("Firestore deleteBranch error [$branchId]: ${e.message}")
        }
    }
    override suspend fun duplicateBranch(sourceBranchId: String, newBranch: Branch) {
        try {
            println("Firestore duplicateBranch: $sourceBranchId -> ${newBranch.id}")
            firestore.collection("branches").document(newBranch.id).set(newBranch)

            _items.value.forEach { item ->
                val sourceConfig = item.branches[sourceBranchId]
                if (sourceConfig != null) {
                    val updatedBranches = item.branches.toMutableMap()
                    updatedBranches[newBranch.id] = sourceConfig.copy()
                    val updatedItem = item.copy(branches = updatedBranches)
                    firestore.collection("items").document(item.id).set(updatedItem)
                }
            }
        } catch (e: Exception) {
            println("Firestore duplicateBranch error: ${e.message}")
        }
    }

    override suspend fun addCategory(category: Category) {
        val docId = category.id.ifBlank { category.name.trim().lowercase().replace(" ", "_").ifBlank { "cat_${kotlin.random.Random.nextLong(100000, 999999)}" } }
        val toSave = category.copy(id = docId, name = category.name.trim())
        try {
            println("Firestore addCategory: $docId -> ${toSave.name}")
            firestore.collection("categories").document(docId).set(toSave)
        } catch (e: Exception) {
            println("Firestore addCategory error: ${e.message}")
        }
    }
    override suspend fun updateCategory(category: Category) {
        if (category.id.isBlank()) return
        try {
            println("Firestore updateCategory: ${category.id} -> ${category.name}")
            firestore.collection("categories").document(category.id).set(category)

            // Cascading sync: Update categoryName on all items belonging to this category
            val itemsToUpdate = _items.value.filter {
                it.categoryId == category.id || it.categoryName.equals(category.id, ignoreCase = true)
            }
            itemsToUpdate.forEach { item ->
                val updatedItem = item.copy(categoryId = category.id, categoryName = category.name)
                firestore.collection("items").document(item.id).set(updatedItem)
                println("Cascading categoryName update for item ${item.id} -> ${category.name}")
            }
        } catch (e: Exception) {
            println("Firestore updateCategory error [${category.id}]: ${e.message}")
        }
    }
    override suspend fun deleteCategory(categoryId: String) {
        try {
            println("Firestore deleteCategory: $categoryId")
            firestore.collection("categories").document(categoryId).delete()

            val itemsToReassign = _items.value.filter { it.categoryId == categoryId }
            itemsToReassign.forEach { item ->
                val updatedItem = item.copy(categoryId = "general", categoryName = "General")
                firestore.collection("items").document(item.id).set(updatedItem)
            }
        } catch (e: Exception) {
            println("Firestore deleteCategory error [$categoryId]: ${e.message}")
        }
    }

    override suspend fun addMenuItem(item: MenuItem) {
        val docId = item.id.ifBlank { "item_${kotlin.random.Random.nextLong(100000, 999999)}" }
        val toSave = item.copy(id = docId)
        try {
            println("Firestore addMenuItem doc: $docId")
            firestore.collection("items").document(docId).set(toSave)
        } catch (e: Exception) {
            println("Firestore addMenuItem error: ${e.message}")
        }
    }
    override suspend fun updateMenuItem(item: MenuItem) {
        if (item.id.isBlank()) return
        try {
            println("Firestore updateMenuItem doc: ${item.id} -> ${item.branches}")
            firestore.collection("items").document(item.id).set(item)
        } catch (e: Exception) {
            println("Firestore updateMenuItem error [${item.id}]: ${e.message}")
        }
    }
    override suspend fun deleteMenuItem(itemId: String) {
        try {
            println("Firestore deleteMenuItem doc: $itemId")
            firestore.collection("items").document(itemId).delete()
        } catch (e: Exception) {
            println("Firestore deleteMenuItem error [$itemId]: ${e.message}")
        }
    }

    override suspend fun saveAnimatedSvgToPack(item: AnimatedSvgItem) {
        try { firestore.collection("animatedSvgPack").document(item.id).set(item) } catch (e: Exception) { println("Firestore saveAnimatedSvgToPack error: ${e.message}") }
    }
}
