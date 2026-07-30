package com.gongrab.menu.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gongrab.menu.domain.model.AnimatedSvgItem
import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.model.BranchPriceConfig
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import com.gongrab.menu.domain.repository.MenuRepository
import com.gongrab.menu.domain.validation.ValidationEngine
import com.gongrab.menu.presentation.theme.BorderGreen
import com.gongrab.menu.presentation.theme.CardNavySurface
import com.gongrab.menu.presentation.theme.DarkNavyBg
import com.gongrab.menu.presentation.theme.LeafGreen
import com.gongrab.menu.presentation.theme.TextMuted
import com.gongrab.menu.presentation.ui.components.AnimatedSvgPickerModal
import kotlinx.coroutines.launch

enum class MobileNavTab { ITEMS, CATEGORIES, BRANCHES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAppScreen(repository: MenuRepository) {
    val coroutineScope = rememberCoroutineScope()

    var activeTab by remember { mutableStateOf(MobileNavTab.ITEMS) }
    val branches by repository.branches.collectAsState()
    val categories by repository.categories.collectAsState()
    val items by repository.items.collectAsState()
    val animatedSvgPack by repository.animatedSvgPack.collectAsState()

    var selectedBranchId by remember { mutableStateOf("branch_1") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var showBranchDrawer by remember { mutableStateOf(false) }

    // Resolve active branch name
    val currentBranchName = when (selectedBranchId) {
        "all" -> "🌐 All Branches"
        else -> branches.find { it.id == selectedBranchId }?.name ?: "📍 Branch 1"
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardNavySurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = activeTab == MobileNavTab.ITEMS) {
                            showBranchDrawer = true
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(LeafGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = when (activeTab) {
                                        MobileNavTab.ITEMS -> currentBranchName
                                        MobileNavTab.CATEGORIES -> "📁 Categories (${categories.size})"
                                        MobileNavTab.BRANCHES -> "🏢 Branches (${branches.size})"
                                    },
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (activeTab == MobileNavTab.ITEMS) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = LeafGreen
                                    )
                                }
                            }
                            Text(
                                text = "Go N Grab 24/7",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = LeafGreen)
                    }
                }

                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search ${activeTab.name.lowercase()}...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LeafGreen,
                            unfocusedBorderColor = BorderGreen,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted)
                                }
                            }
                        }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardNavySurface,
                contentColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == MobileNavTab.ITEMS,
                    onClick = { activeTab = MobileNavTab.ITEMS },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "Items") },
                    label = { Text("Items (${items.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0A1017),
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MobileNavTab.CATEGORIES,
                    onClick = { activeTab = MobileNavTab.CATEGORIES },
                    icon = { Icon(Icons.Default.List, contentDescription = "Categories") },
                    label = { Text("Categories (${categories.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0A1017),
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = activeTab == MobileNavTab.BRANCHES,
                    onClick = { activeTab = MobileNavTab.BRANCHES },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Branches") },
                    label = { Text("Branches (${branches.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF0A1017),
                        selectedTextColor = LeafGreen,
                        indicatorColor = LeafGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        containerColor = DarkNavyBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                MobileNavTab.ITEMS -> MobileItemsView(
                    items = items,
                    categories = categories,
                    branches = branches,
                    selectedBranchId = selectedBranchId,
                    selectedCategoryId = selectedCategoryId,
                    searchQuery = searchQuery,
                    animatedSvgPack = animatedSvgPack,
                    onSelectCategory = { selectedCategoryId = it },
                    repository = repository
                )

                MobileNavTab.CATEGORIES -> MobileCategoriesView(
                    categories = categories,
                    searchQuery = searchQuery,
                    animatedSvgPack = animatedSvgPack,
                    repository = repository
                )

                MobileNavTab.BRANCHES -> MobileBranchesView(
                    branches = branches,
                    searchQuery = searchQuery,
                    repository = repository
                )
            }
        }
    }

    // Branch Selector Drawer
    if (showBranchDrawer) {
        MobileBranchSelectorModal(
            branches = branches,
            selectedBranchId = selectedBranchId,
            onSelectBranch = { id ->
                selectedBranchId = id
                showBranchDrawer = false
            },
            onDismiss = { showBranchDrawer = false }
        )
    }
}

// =========================================================================
// 1. MOBILE ITEMS MANAGEMENT VIEW
// =========================================================================
@Composable
fun MobileItemsView(
    items: List<MenuItem>,
    categories: List<Category>,
    branches: List<Branch>,
    selectedBranchId: String,
    selectedCategoryId: String?,
    searchQuery: String,
    animatedSvgPack: List<AnimatedSvgItem>,
    onSelectCategory: (String?) -> Unit,
    repository: MenuRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var showAddItemSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var showSvgPickerForItem by remember { mutableStateOf<MenuItem?>(null) }

    val filteredItems = remember(items, selectedCategoryId, searchQuery) {
        items.filter { item ->
            val matchesCat = selectedCategoryId == null || item.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSearch
        }.sortedBy { it.displayOrder }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Category Carousel with Animated SVG Badges
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { onSelectCategory(null) },
                    label = { Text("All (${items.size})", fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LeafGreen,
                        selectedLabelColor = Color(0xFF0A1017),
                        containerColor = CardNavySurface,
                        labelColor = Color.White
                    )
                )
            }

            items(categories) { cat ->
                val isSelected = selectedCategoryId == cat.id
                val count = items.count { it.categoryId == cat.id }
                val hasSvg = cat.animatedSvg.isNotBlank()

                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectCategory(cat.id) },
                    label = { Text("${if (hasSvg) "🎬 " else ""}${cat.name} ($count)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LeafGreen,
                        selectedLabelColor = Color(0xFF0A1017),
                        containerColor = CardNavySurface,
                        labelColor = Color.White
                    )
                )
            }
        }

        // Add Item Floating Action Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredItems.size} items",
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = { showAddItemSheet = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add New Item", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items found in database", color = TextMuted, fontSize = 15.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    val branchConfig = item.branches[selectedBranchId]
                    val isAvailable = branchConfig?.available ?: true
                    val currentPrice = branchConfig?.price ?: item.defaultPrice
                    val categoryName = categories.find { it.id == item.categoryId }?.name ?: "General"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (isAvailable) BorderGreen else Color(0xFF334155), RoundedCornerShape(14.dp)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("📂 $categoryName", color = TextMuted, fontSize = 12.sp)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = LeafGreen.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LeafGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "₹$currentPrice",
                                        color = LeafGreen,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isAvailable) "In Stock" else "Out of Stock",
                                        color = if (isAvailable) LeafGreen else Color(0xFFEF4444),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = isAvailable,
                                        onCheckedChange = { available ->
                                            coroutineScope.launch {
                                                val updatedBranches = item.branches.toMutableMap()
                                                val current = updatedBranches[selectedBranchId] ?: BranchPriceConfig(price = item.defaultPrice)
                                                updatedBranches[selectedBranchId] = current.copy(available = available)
                                                repository.updateMenuItem(item.copy(branches = updatedBranches))
                                            }
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = LeafGreen,
                                            checkedTrackColor = LeafGreen.copy(alpha = 0.3f)
                                        )
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { editingItem = item }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF38BDF8))
                                    }
                                    IconButton(onClick = { coroutineScope.launch { repository.deleteMenuItem(item.id) } }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddItemSheet || editingItem != null) {
        MobileItemDialog(
            item = editingItem,
            categories = categories,
            branches = branches,
            onDismiss = {
                showAddItemSheet = false
                editingItem = null
            },
            onSave = { newItem ->
                coroutineScope.launch {
                    if (editingItem != null) repository.updateMenuItem(newItem) else repository.addMenuItem(newItem)
                    showAddItemSheet = false
                    editingItem = null
                }
            }
        )
    }

    showSvgPickerForItem?.let { item ->
        AnimatedSvgPickerModal(
            selectedAnimatedSvg = "",
            dbAnimatedSvgPack = animatedSvgPack,
            onAnimatedSvgSelected = { svgContent ->
                coroutineScope.launch {
                    repository.saveAnimatedSvgToPack(
                        AnimatedSvgItem(
                            id = "svg_${kotlin.random.Random.nextLong(100000, 999999)}",
                            name = "${item.name} Svg",
                            svgContent = svgContent
                        )
                    )
                    showSvgPickerForItem = null
                }
            },
            onUploadToDbPack = { newItem ->
                coroutineScope.launch { repository.saveAnimatedSvgToPack(newItem) }
            },
            onDismiss = { showSvgPickerForItem = null }
        )
    }
}

// =========================================================================
// 2. MOBILE CATEGORIES MANAGEMENT VIEW (WITH ANIMATED SVG PICKER)
// =========================================================================
@Composable
fun MobileCategoriesView(
    categories: List<Category>,
    searchQuery: String,
    animatedSvgPack: List<AnimatedSvgItem>,
    repository: MenuRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var categoryForSvgPicker by remember { mutableStateOf<Category?>(null) }

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Categories List", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { editingCategory = null; showCategoryDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Add Category", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredCategories, key = { it.id }) { cat ->
                val hasAnimatedSvg = cat.animatedSvg.isNotBlank()

                Card(
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LeafGreen)
                                Text("ID: ${cat.id} | Display Order: ${cat.displayOrder}", color = TextMuted, fontSize = 12.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (hasAnimatedSvg) LeafGreen.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (hasAnimatedSvg) LeafGreen else BorderGreen)
                            ) {
                                Text(
                                    text = if (hasAnimatedSvg) "🎬 SVG ACTIVE" else "NO SVG",
                                    color = if (hasAnimatedSvg) LeafGreen else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { categoryForSvgPicker = cat },
                                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen.copy(alpha = 0.15f), contentColor = LeafGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LeafGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "SVG", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🎬 Pick Animated SVG", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row {
                                TextButton(onClick = { editingCategory = cat; showCategoryDialog = true }) {
                                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { coroutineScope.launch { repository.deleteCategory(cat.id) } }) {
                                    Text("Delete", color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryDialog) {
        MobileCategoryDialog(
            category = editingCategory,
            existingCategories = categories,
            animatedSvgPack = animatedSvgPack,
            repository = repository,
            onDismiss = { showCategoryDialog = false },
            onSave = { category ->
                coroutineScope.launch {
                    if (editingCategory == null) repository.addCategory(category) else repository.updateCategory(category)
                    showCategoryDialog = false
                }
            }
        )
    }

    categoryForSvgPicker?.let { cat ->
        AnimatedSvgPickerModal(
            selectedAnimatedSvg = cat.animatedSvg,
            dbAnimatedSvgPack = animatedSvgPack,
            onAnimatedSvgSelected = { newSvg ->
                coroutineScope.launch {
                    repository.updateCategory(cat.copy(animatedSvg = newSvg))
                    categoryForSvgPicker = null
                }
            },
            onUploadToDbPack = { newItem ->
                coroutineScope.launch { repository.saveAnimatedSvgToPack(newItem) }
            },
            onDismiss = { categoryForSvgPicker = null }
        )
    }
}

// =========================================================================
// 3. MOBILE BRANCHES MANAGEMENT VIEW (WITH DUPLICATE / COPY BRANCH)
// =========================================================================
@Composable
fun MobileBranchesView(
    branches: List<Branch>,
    searchQuery: String,
    repository: MenuRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var showBranchDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var editingBranch by remember { mutableStateOf<Branch?>(null) }
    var sourceBranchToCopy by remember { mutableStateOf<Branch?>(null) }

    val filteredBranches = remember(branches, searchQuery) {
        if (searchQuery.isBlank()) branches
        else branches.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Branch Locations", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { editingBranch = null; showBranchDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Add Branch", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredBranches, key = { it.id }) { branch ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("📍 ${branch.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LeafGreen)
                                Text("Address: ${branch.address.ifEmpty { "Main Location" }}", color = TextMuted, fontSize = 12.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFF334155))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    sourceBranchToCopy = branch
                                    showDuplicateDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen.copy(alpha = 0.2f), contentColor = LeafGreen),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LeafGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("📋 Copy / Duplicate Branch", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Row {
                                TextButton(onClick = { editingBranch = branch; showBranchDialog = true }) {
                                    Text("Edit", color = Color.White)
                                }
                                TextButton(onClick = { coroutineScope.launch { repository.deleteBranch(branch.id) } }) {
                                    Text("Delete", color = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBranchDialog) {
        MobileBranchDialog(
            branch = editingBranch,
            existingBranches = branches,
            onDismiss = { showBranchDialog = false },
            onSave = { branch ->
                coroutineScope.launch {
                    if (editingBranch == null) repository.addBranch(branch) else repository.updateBranch(branch)
                    showBranchDialog = false
                }
            }
        )
    }

    if (showDuplicateDialog && sourceBranchToCopy != null) {
        MobileDuplicateBranchDialog(
            sourceBranch = sourceBranchToCopy!!,
            existingBranches = branches,
            onDismiss = { showDuplicateDialog = false },
            onDuplicate = { newBranch ->
                coroutineScope.launch {
                    repository.duplicateBranch(sourceBranchToCopy!!.id, newBranch)
                    showDuplicateDialog = false
                }
            }
        )
    }
}

// =========================================================================
// DIALOGS & MODALS
// =========================================================================
@Composable
fun MobileBranchSelectorModal(
    branches: List<Branch>,
    selectedBranchId: String,
    onSelectBranch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = { Text("🏬 Select Restaurant Branch", color = LeafGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                branches.forEach { b ->
                    val isSelected = selectedBranchId == b.id
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBranch(b.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LeafGreen.copy(alpha = 0.2f) else DarkNavyBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) LeafGreen else BorderGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📍 ${b.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            if (isSelected) Icon(Icons.Default.Check, contentDescription = "Selected", tint = LeafGreen)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CLOSE", color = TextMuted) } }
    )
}

@Composable
fun MobileItemDialog(
    item: MenuItem?,
    categories: List<Category>,
    branches: List<Branch>,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var defaultPriceText by remember { mutableStateOf(item?.defaultPrice?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == item?.categoryId } ?: categories.firstOrNull()) }
    var expandedCat by remember { mutableStateOf(false) }

    // Multi-branch price entries
    val branchPricesMap = remember {
        mutableStateMapOf<String, String>().apply {
            branches.forEach { b ->
                val price = item?.branches?.get(b.id)?.price?.toString() ?: defaultPriceText
                put(b.id, price)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = { Text(if (item != null) "Edit Menu Item" else "Add New Item", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = defaultPriceText,
                    onValueChange = { defaultPriceText = it },
                    label = { Text("Base Default Price (₹)", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Box {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGreen)
                    ) {
                        Text("Category: ${selectedCategory?.name ?: "Select Category"}", color = Color.White)
                    }

                    DropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; expandedCat = false })
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFF334155))

                Text("Branch Specific Prices (₹):", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                branches.forEach { b ->
                    OutlinedTextField(
                        value = branchPricesMap[b.id] ?: defaultPriceText,
                        onValueChange = { branchPricesMap[b.id] = it },
                        label = { Text("Price for ${b.name}", color = TextMuted) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val basePrice = defaultPriceText.toDoubleOrNull() ?: 0.0
                    val catId = selectedCategory?.id ?: "general"
                    val catName = selectedCategory?.name ?: "General"

                    val updatedBranches = mutableMapOf<String, BranchPriceConfig>()
                    branches.forEach { b ->
                        val bPrice = branchPricesMap[b.id]?.toDoubleOrNull() ?: basePrice
                        val currentAvail = item?.branches?.get(b.id)?.available ?: true
                        updatedBranches[b.id] = BranchPriceConfig(price = bPrice, available = currentAvail)
                    }

                    val newItem = (item ?: MenuItem(
                        id = "item_${kotlin.random.Random.nextLong(100000, 999999)}",
                        name = name,
                        categoryId = catId,
                        categoryName = catName,
                        defaultPrice = basePrice,
                        branches = updatedBranches
                    )).copy(
                        name = name,
                        categoryId = catId,
                        categoryName = catName,
                        defaultPrice = basePrice,
                        branches = updatedBranches
                    )

                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Text("SAVE ITEM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) } }
    )
}

@Composable
fun MobileCategoryDialog(
    category: Category?,
    existingCategories: List<Category>,
    animatedSvgPack: List<AnimatedSvgItem>,
    repository: MenuRepository,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var orderText by remember { mutableStateOf(category?.displayOrder?.toString() ?: "1") }
    var animatedSvg by remember { mutableStateOf(category?.animatedSvg ?: "") }
    var showSvgPickerModal by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = { Text(if (category == null) "Create Category" else "Edit Category", color = LeafGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = orderText,
                    onValueChange = { orderText = it },
                    label = { Text("Display Order Index", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = { showSvgPickerModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, contentDescription = "SVG", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("🎬 ✨ Pick / Upload Animated SVG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                if (animatedSvg.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✓ Animated SVG Active (${animatedSvg.length} chars)", color = LeafGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { animatedSvg = "" }) { Text("Clear", color = Color(0xFFEF4444), fontSize = 11.sp) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val order = orderText.toIntOrNull() ?: 999
                    val id = category?.id ?: name.trim().lowercase().replace(" ", "_").ifBlank { "cat_${kotlin.random.Random.nextLong(100000, 999999)}" }
                    onSave(Category(id = id, name = name.trim(), displayOrder = order, animatedSvg = animatedSvg))
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Text("SAVE CATEGORY", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) } }
    )

    if (showSvgPickerModal) {
        AnimatedSvgPickerModal(
            selectedAnimatedSvg = animatedSvg,
            dbAnimatedSvgPack = animatedSvgPack,
            onAnimatedSvgSelected = { newSvg -> animatedSvg = newSvg },
            onUploadToDbPack = { newItem ->
                coroutineScope.launch { repository.saveAnimatedSvgToPack(newItem) }
            },
            onDismiss = { showSvgPickerModal = false }
        )
    }
}

@Composable
fun MobileBranchDialog(
    branch: Branch?,
    existingBranches: List<Branch>,
    onDismiss: () -> Unit,
    onSave: (Branch) -> Unit
) {
    var name by remember { mutableStateOf(branch?.name ?: "") }
    var address by remember { mutableStateOf(branch?.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = { Text(if (branch == null) "Create Branch" else "Edit Branch", color = LeafGreen, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Branch Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val id = branch?.id ?: "branch_${kotlin.random.Random.nextLong(10000, 99999)}"
                    onSave(Branch(id = id, name = name.trim(), address = address.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Text("SAVE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) } }
    )
}

@Composable
fun MobileDuplicateBranchDialog(
    sourceBranch: Branch,
    existingBranches: List<Branch>,
    onDismiss: () -> Unit,
    onDuplicate: (Branch) -> Unit
) {
    var name by remember { mutableStateOf("${sourceBranch.name} (Copy)") }
    var address by remember { mutableStateOf(sourceBranch.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = { Text("📋 Copy Branch: ${sourceBranch.name}", color = LeafGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "This will clone all menu items, price rules, and availability settings from ${sourceBranch.name} into the new branch.",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("New Branch Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LeafGreen, unfocusedBorderColor = BorderGreen, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val id = "branch_${kotlin.random.Random.nextLong(10000, 99999)}"
                    onDuplicate(Branch(id = id, name = name.trim(), address = address.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Text("CLONE & DUPLICATE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = TextMuted) } }
    )
}
