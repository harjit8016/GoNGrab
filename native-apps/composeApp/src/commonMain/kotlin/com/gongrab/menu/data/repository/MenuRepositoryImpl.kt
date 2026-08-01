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
    private val rootPath: String = System.getProperty("user.dir") ?: "."
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
        // Force live network fetch from Firebase/Express backend server FIRST
        val networkSuccess = loadFromNetworkOrSeed()
        if (networkSuccess) {
            println("✓ Successfully loaded live data from backend server")
            return
        }

        // Fall back to local disk cache only if network fetch fails completely
        try {
            val targetFile = findValidDataFile()
            if (targetFile != null) {
                val content = targetFile.readText()
                val cache = json.decodeFromString<MenuDataCache>(content)
                _branches.value = cache.branches.ifEmpty { defaultBranches() }
                _categories.value = cache.categories
                _items.value = cache.items
                _animatedSvgPack.value = cache.animatedSvgPack
                println("✓ Loaded ${_items.value.size} items from local file system fallback cache")
            } else {
                _branches.value = defaultBranches()
            }
        } catch (e: Exception) {
            println("Error loading local menu data file: ${e.message}")
            _branches.value = defaultBranches()
        }
    }

    private fun loadFromNetworkOrSeed(): Boolean {
        val hostCandidates = listOf(
            "http://127.0.0.1:3000",
            "http://172.20.10.4:3000",
            "http://localhost:3000",
            "http://10.0.2.2:3000"
        )

        // Try fetching from live local/network Express REST API server
        for (host in hostCandidates) {
            try {
                println("Network try: $host/api/data...")
                val dataUrl = java.net.URI("$host/api/data").toURL()
                val conn = dataUrl.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                val code = conn.responseCode
                if (code == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val cache = json.decodeFromString<MenuDataCache>(text)
                    if (cache.items.isNotEmpty()) {
                        _branches.value = cache.branches.ifEmpty { defaultBranches() }
                        _categories.value = cache.categories
                        _items.value = cache.items
                        _animatedSvgPack.value = cache.animatedSvgPack
                        println("✓ SUCCESS: Loaded ${cache.items.size} live items directly from main database server ($host/api/data)")
                        return true
                    }
                } else {
                    println("Network response code from $host/api/data: $code")
                }
            } catch (e: Exception) {
                println("Network exception for $host: ${e.message}")
            }
        }

        return false
    }

    private fun sendApiSync(endpointPath: String, method: String, jsonBody: String? = null) {
        scope.launch(Dispatchers.IO) {
            val hostCandidates = listOf(
                "http://localhost:3000",
                "http://10.0.2.2:3000",
                "http://172.20.10.4:3000"
            )
            for (host in hostCandidates) {
                try {
                    val url = java.net.URI("$host$endpointPath").toURL()
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = method
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    if (jsonBody != null) {
                        conn.doOutput = true
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        conn.outputStream.use { os ->
                            os.write(jsonBody.toByteArray(Charsets.UTF_8))
                        }
                    }
                    val code = conn.responseCode
                    if (code in 200..299) {
                        println("✓ REST API sync $method $endpointPath succeeded on $host ($code)")
                        break
                    }
                } catch (e: Exception) {
                    // try next candidate
                }
            }
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
            if (mainCache.parentFile?.canWrite() == true || mainCache.exists()) {
                mainCache.writeText(jsonText)
            }

            val rootData = File(rootPath, "data.json")
            if (rootData.parentFile?.canWrite() == true || rootData.exists()) {
                rootData.writeText(jsonText)
            }
        } catch (e: Exception) {
            println("Notice on saveToDisk: ${e.message}")
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
        val body = json.encodeToString(Branch.serializer(), branch)
        sendApiSync("/api/branches", "POST", body)
    }

    override suspend fun updateBranch(branch: Branch) {
        _branches.value = _branches.value.map { if (it.id == branch.id) branch else it }
        saveToDisk()
        val body = json.encodeToString(Branch.serializer(), branch)
        sendApiSync("/api/branches/${branch.id}", "PUT", body)
    }

    override suspend fun deleteBranch(branchId: String) {
        _branches.value = _branches.value.filter { it.id != branchId }
        _items.value = _items.value.map { item ->
            item.copy(branches = item.branches.filterKeys { it != branchId })
        }
        saveToDisk()
        sendApiSync("/api/branches/$branchId", "DELETE")
    }

    override suspend fun duplicateBranch(sourceBranchId: String, newBranch: Branch) {
        _branches.value = _branches.value + newBranch

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
        val payload = json.encodeToString(
            kotlinx.serialization.json.JsonObject.serializer(),
            kotlinx.serialization.json.buildJsonObject {
                put("sourceBranchId", kotlinx.serialization.json.JsonPrimitive(sourceBranchId))
                put("name", kotlinx.serialization.json.JsonPrimitive(newBranch.name))
                put("code", kotlinx.serialization.json.JsonPrimitive(newBranch.id.uppercase()))
            }
        )
        sendApiSync("/api/branches/duplicate", "POST", payload)
    }

    override suspend fun addCategory(category: Category) {
        _categories.value = _categories.value + category
        saveToDisk()
        val body = json.encodeToString(Category.serializer(), category)
        sendApiSync("/api/categories", "POST", body)
    }

    override suspend fun updateCategory(category: Category) {
        _categories.value = _categories.value.map { if (it.id == category.id) category else it }
        saveToDisk()
        val body = json.encodeToString(Category.serializer(), category)
        sendApiSync("/api/categories", "POST", body)
    }

    override suspend fun deleteCategory(categoryId: String) {
        _categories.value = _categories.value.filter { it.id != categoryId }
        saveToDisk()
        sendApiSync("/api/categories/$categoryId", "DELETE")
    }

    override suspend fun addMenuItem(item: MenuItem) {
        _items.value = _items.value + item
        saveToDisk()
        val body = json.encodeToString(MenuItem.serializer(), item)
        sendApiSync("/api/items", "POST", body)
    }

    override suspend fun updateMenuItem(item: MenuItem) {
        _items.value = _items.value.map { if (it.id == item.id) item else it }
        saveToDisk()
        val body = json.encodeToString(MenuItem.serializer(), item)
        sendApiSync("/api/items", "POST", body)
    }

    override suspend fun deleteMenuItem(itemId: String) {
        _items.value = _items.value.filter { it.id != itemId }
        saveToDisk()
        sendApiSync("/api/items/$itemId", "DELETE")
    }

    private fun getSeedFallbackData(): Pair<List<Category>, List<MenuItem>> {
        val baseCats = listOf(
            Category("shake", "Shake", 1),
            Category("mojito", "Mojito", 2),
            Category("smoothies", "Smoothies", 3),
            Category("ice_tea", "Ice Tea", 4),
            Category("pasta", "Pasta", 5),
            Category("maggie", "Maggie", 6),
            Category("dessert", "Dessert", 7),
            Category("sandwich", "Sandwich", 8),
            Category("sub_sandwich", "Sub Sandwich", 9),
            Category("garlic_bread", "Garlic Bread", 10),
            Category("burger", "Burger", 11),
            Category("wrap", "Wrap", 12),
            Category("fries", "Fries & Sides", 13),
            Category("pizza", "Pizza", 14),
            Category("waffle", "Waffle", 15),
            Category("pancake", "Pancake", 16),
            Category("momos", "Momos", 17),
            Category("spring_roll", "Spring Roll", 18),
            Category("chai_coffee", "Chai & Coffee", 19),
            Category("nachos", "Nachos", 20),
            Category("add_ons", "Add-ons", 21)
        )

        val cats = baseCats.map { cat ->
            val svg = com.gongrab.menu.presentation.ui.components.DEFAULT_ANIMATED_PRESETS.find { preset ->
                preset.name.lowercase().contains(cat.name.lowercase()) || cat.name.lowercase().contains(preset.name.lowercase().replace("animated", "").trim())
            }?.svgContent ?: ""
            cat.copy(animatedSvg = svg)
        }

        val items = listOf(
            MenuItem("shk_1", "Chocolate Shake", "shake", "Shake", 140.0),
            MenuItem("shk_2", "Oreo Shake", "shake", "Shake", 150.0),
            MenuItem("shk_3", "KitKat Shake", "shake", "Shake", 160.0),
            MenuItem("shk_4", "Strawberry Shake", "shake", "Shake", 130.0),

            MenuItem("moj_1", "Virgin Mojito", "mojito", "Mojito", 120.0),
            MenuItem("moj_2", "Green Apple Mojito", "mojito", "Mojito", 130.0),
            MenuItem("moj_3", "Watermelon Mojito", "mojito", "Mojito", 130.0),

            MenuItem("brg_1", "Classic Veg Burger", "burger", "Burger", 99.0),
            MenuItem("brg_2", "Cheese Burst Burger", "burger", "Burger", 149.0),
            MenuItem("brg_3", "Paneer Crisp Burger", "burger", "Burger", 169.0),

            MenuItem("pza_1", "Margherita Pizza", "pizza", "Pizza", 199.0),
            MenuItem("pza_2", "Farmhouse Veg Pizza", "pizza", "Pizza", 279.0),
            MenuItem("pza_3", "Paneer Tikka Pizza", "pizza", "Pizza", 299.0),

            MenuItem("snd_1", "Veg Grilled Sandwich", "sandwich", "Sandwich", 110.0),
            MenuItem("snd_2", "Cheese Corn Sandwich", "sandwich", "Sandwich", 130.0),

            MenuItem("frs_1", "Peri Peri Fries", "fries", "Fries & Sides", 119.0),
            MenuItem("frs_2", "Cheese Loaded Fries", "fries", "Fries & Sides", 149.0),

            MenuItem("pst_1", "White Sauce Pasta", "pasta", "Pasta", 180.0),
            MenuItem("pst_2", "Red Sauce Pasta", "pasta", "Pasta", 170.0),

            MenuItem("wfl_1", "Nutella Waffle", "waffle", "Waffle", 160.0),
            MenuItem("wfl_2", "Belgian Chocolate Waffle", "waffle", "Waffle", 170.0),

            MenuItem("mmo_1", "Steamed Veg Momos", "momos", "Momos", 110.0),
            MenuItem("mmo_2", "Fried Paneer Momos", "momos", "Momos", 140.0),

            MenuItem("cof_1", "Cold Coffee", "chai_coffee", "Chai & Coffee", 110.0),
            MenuItem("cof_2", "Hot Cappuccino", "chai_coffee", "Chai & Coffee", 90.0)
        )

        return Pair(cats, items)
    }
}
