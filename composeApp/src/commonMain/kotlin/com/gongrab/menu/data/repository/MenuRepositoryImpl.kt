package com.gongrab.menu.data.repository

import com.gongrab.menu.domain.model.AnimatedSvgItem
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

    private fun findValidDataFile(): File? {
        val currentDir = File(rootPath)
        val candidateFiles = mutableListOf<File>()

        candidateFiles.add(File(currentDir, "data_cache.json"))
        candidateFiles.add(File(currentDir, "data.json"))
        
        currentDir.parentFile?.let { parent ->
            candidateFiles.add(File(parent, "data_cache.json"))
            candidateFiles.add(File(parent, "data.json"))
        }

        return candidateFiles.firstOrNull { it.exists() && it.length() > 100 }
    }

    private val activeDataFile: File by lazy {
        findValidDataFile() ?: File(rootPath, "data_cache.json")
    }

    private val _branches = MutableStateFlow<List<Branch>>(emptyList())
    override val branches: StateFlow<List<Branch>> = _branches.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _items = MutableStateFlow<List<MenuItem>>(emptyList())
    override val items: StateFlow<List<MenuItem>> = _items.asStateFlow()

    private val _animatedSvgPack = MutableStateFlow<List<AnimatedSvgItem>>(emptyList())
    override val animatedSvgPack: StateFlow<List<AnimatedSvgItem>> = _animatedSvgPack.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            reloadData()
        }
    }

    override suspend fun reloadData() {
        try {
            val targetFile = findValidDataFile()
            if (targetFile != null) {
                val content = targetFile.readText()
                val cache = json.decodeFromString<MenuDataCache>(content)
                _branches.value = cache.branches.ifEmpty { defaultBranches() }
                _categories.value = cache.categories
                _items.value = cache.items
                _animatedSvgPack.value = cache.animatedSvgPack
            } else {
                _branches.value = defaultBranches()
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
                items = _items.value,
                animatedSvgPack = _animatedSvgPack.value
            )
            val jsonText = json.encodeToString(MenuDataCache.serializer(), cache)
            
            val mainCache = File(rootPath, "data_cache.json")
            mainCache.writeText(jsonText)

            val rootData = File(rootPath, "data.json")
            rootData.writeText(jsonText)

            val publicData = File(rootPath, "public/data.json")
            if (publicData.parentFile?.exists() == true) {
                publicData.writeText(jsonText)
            }

            val active = activeDataFile
            if (active.absolutePath != mainCache.absolutePath && active.exists()) {
                active.writeText(jsonText)
            }
        } catch (e: Exception) {
            println("Error saving menu data: ${e.message}")
        }
    }

    override suspend fun saveAnimatedSvgToPack(item: AnimatedSvgItem) {
        val existing = _animatedSvgPack.value.filter { it.id != item.id }
        _animatedSvgPack.value = existing + item
        saveToDisk()
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
        _items.value = _items.value.map { item ->
            item.copy(branches = item.branches.filterKeys { it != branchId })
        }
        saveToDisk()
    }

    override suspend fun duplicateBranch(sourceBranchId: String, newBranch: Branch) {
        // 1. Add new branch
        _branches.value = _branches.value + newBranch

        // 2. Clone all item branch configs (prices and availability) from sourceBranchId -> newBranch.id
        _items.value = _items.value.map { item ->
            val sourceConfig = item.branches[sourceBranchId]
            if (sourceConfig != null) {
                val updatedBranches = item.branches.toMutableMap()
                updatedBranches[newBranch.id] = sourceConfig.copy()
                item.copy(branches = updatedBranches)
            } else {
                item
            }
        }

        saveToDisk()
    }

    private fun syncCategoryToFirebaseServer(category: Category) {
        try {
            val url = java.net.URI("http://localhost:3000/api/categories").toURL()
            val conn = url.openConnection() as java.net.HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.doOutput = true

            val nameJson = json.encodeToString(kotlinx.serialization.serializer(), category.name)
            val svgJson = json.encodeToString(kotlinx.serialization.serializer(), category.animatedSvg)

            val jsonPayload = """{"id":"${category.id}","name":$nameJson,"displayOrder":${category.displayOrder},"animatedSvg":$svgJson}"""
            val input = jsonPayload.toByteArray(Charsets.UTF_8)
            conn.setFixedLengthStreamingMode(input.size)

            conn.outputStream.use { os ->
                os.write(input, 0, input.size)
            }

            val code = conn.responseCode
            println("✓ Synced category '${category.id}' (${input.size} bytes) to Firebase server. Status: $code")
        } catch (e: Exception) {
            println("Category sync to Firebase notice: ${e.message}")
        }
    }

    override suspend fun addCategory(category: Category) {
        _categories.value = _categories.value + category
        saveToDisk()
        syncCategoryToFirebaseServer(category)
    }

    override suspend fun updateCategory(category: Category) {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        _items.value = _items.value.map { item ->
            if (item.categoryId == category.id) item.copy(categoryName = category.name) else item
        }
        saveToDisk()
        syncCategoryToFirebaseServer(category)
    }

    override suspend fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
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
