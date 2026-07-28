package com.gongrab.menu.data.repository

import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuDataCache
import com.gongrab.menu.domain.model.MenuItem
import com.gongrab.menu.domain.repository.MenuRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File

class MenuRepositoryImpl(
    private val rootPath: String = System.getProperty("user.dir")
) : MenuRepository {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val dataCacheFile = File(rootPath, "data_cache.json")
    private val dataJsonFile = File(rootPath, "data.json")

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    override val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    override val items: StateFlow<List<MenuItem>> = _items.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            reloadData()
        }
    }

    override suspend fun reloadData() {
        try {
            val targetFile = if (dataCacheFile.exists()) dataCacheFile else if (dataJsonFile.exists()) dataJsonFile else null
            if (targetFile != null && targetFile.length() > 0) {
                val content = targetFile.readText()
                val cache = json.decodeFromString<MenuDataCache>(content)
                _branches.value = cache.branches.ifEmpty { defaultBranches() }
                _categories.value = cache.categories
                _items.value = cache.items
            } else {
                // Initialize default seeds if missing
                val defaultB = defaultBranches()
                _branches.value = defaultB
                _categories.value = emptyList()
                _items.value = emptyList()
                saveToDisk()
            }
        } catch (e: Exception) {
            println("Error loading menu data: ${e.message}")
            _branches.value = defaultBranches()
        }
    }

    private fun defaultBranches(): List<Branch> {
        return listOf(
            Branch("branch_1", "Branch 1", "Main Street"),
            Branch("branch_2", "Branch 2", "Downtown Center")
        )
    }

    private fun saveToDisk() {
        try {
            val cache = MenuDataCache(
                categories = _categories.value,
                branches = _branches.value,
                items = _items.value
            )
            val jsonText = json.encodeToString(MenuDataCache.serializer(), cache)
            dataCacheFile.writeText(jsonText)
            dataJsonFile.writeText(jsonText)
        } catch (e: Exception) {
            println("Error saving menu data: ${e.message}")
        }
    }

    override suspend fun addBranch(branch: Branch) {
        _branches.value = _branches.value + branch
        saveToDisk()
    }

    override suspend fun updateBranch(branch: Branch) {
        _branches.value = _branches.value.map { if (it.id == branch.id) branch else it }
        saveToDisk()
    }

    override suspend fun deleteBranch(branchId: String) {
        _branches.value = _branches.value.filter { it.id != branchId }
        // Clean references in items
        _items.value = _items.value.map { item ->
            item.copy(branches = item.branches.filterKeys { it != branchId })
        }
        saveToDisk()
    }

    override suspend fun addCategory(category: Category) {
        _categories.value = _categories.value + category
        saveToDisk()
    }

    override suspend fun updateCategory(category: Category) {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        // Update categoryName in items if updated
        _items.value = _items.value.map { item ->
            if (item.categoryId == category.id) item.copy(categoryName = category.name) else item
        }
        saveToDisk()
    }

    override suspend fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        // Delete items under this category
        _items.value = _items.value.filter { it.categoryId != categoryId }
        saveToDisk()
    }

    override suspend fun addMenuItem(item: MenuItem) {
        _items.value = _items.value + item
        saveToDisk()
    }

    override suspend fun updateMenuItem(item: MenuItem) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
        saveToDisk()
    }

    override suspend fun deleteMenuItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
        saveToDisk()
    }
}
