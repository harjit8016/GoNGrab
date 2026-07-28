package com.gongrab.menu.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import kotlinx.coroutines.launch

enum class NavTab { BRANCHES, CATEGORIES, ITEMS }

@Composable
fun MainAppScreen(repository: MenuRepository) {
    var selectedTab by remember { mutableStateOf(NavTab.ITEMS) }
    val branches by repository.branches.collectAsState()
    val categories by repository.categories.collectAsState()
    val items by repository.items.collectAsState()

    Row(modifier = Modifier.fillMaxSize().background(DarkNavyBg)) {
        // --- SIDEBAR ---
        Column(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .background(CardNavySurface)
                .padding(16.dp)
        ) {
            Text(
                text = "Go N Grab 24/7",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = LeafGreen,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Menu Management App",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SidebarButton(
                title = "🏢  Branches (${branches.size})",
                isSelected = selectedTab == NavTab.BRANCHES,
                onClick = { selectedTab = NavTab.BRANCHES }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SidebarButton(
                title = "📁  Categories (${categories.size})",
                isSelected = selectedTab == NavTab.CATEGORIES,
                onClick = { selectedTab = NavTab.CATEGORIES }
            )
            Spacer(modifier = Modifier.height(8.dp))
            SidebarButton(
                title = "🍔  Menu Items (${items.size})",
                isSelected = selectedTab == NavTab.ITEMS,
                onClick = { selectedTab = NavTab.ITEMS }
            )
        }

        // --- CONTENT AREA ---
        Box(modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            when (selectedTab) {
                NavTab.BRANCHES -> BranchesView(branches, repository)
                NavTab.CATEGORIES -> CategoriesView(categories, repository)
                NavTab.ITEMS -> ItemsView(items, categories, branches, repository)
            }
        }
    }
}

@Composable
fun SidebarButton(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(
                color = if (isSelected) LeafGreen.copy(alpha = 0.2f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) LeafGreen else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = title,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) LeafGreen else Color.White,
            fontSize = 14.sp
        )
    }
}

// =========================================================================
// 1. BRANCHES MANAGEMENT VIEW
// =========================================================================
@Composable
fun BranchesView(branches: List<Branch>, repository: MenuRepository) {
    var showDialog by remember { mutableStateOf(false) }
    var editingBranch by remember { mutableStateOf<Branch?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Branch Locations", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { editingBranch = null; showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("+ Add Branch", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(branches) { branch ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(branch.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LeafGreen)
                            Text("ID: ${branch.id} | Address: ${branch.address.ifEmpty { "N/A" }}", color = TextMuted, fontSize = 12.sp)
                        }
                        Row {
                            TextButton(onClick = { editingBranch = branch; showDialog = true }) {
                                Text("Edit", color = LeafGreen)
                            }
                            TextButton(onClick = { scope.launch { repository.deleteBranch(branch.id) } }) {
                                Text("Delete", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        BranchDialog(
            branch = editingBranch,
            existingBranches = branches,
            onDismiss = { showDialog = false },
            onSave = { branch ->
                scope.launch {
                    if (editingBranch == null) repository.addBranch(branch) else repository.updateBranch(branch)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun BranchDialog(
    branch: Branch?,
    existingBranches: List<Branch>,
    onDismiss: () -> Unit,
    onSave: (Branch) -> Unit
) {
    var name by remember { mutableStateOf(branch?.name ?: "") }
    var address by remember { mutableStateOf(branch?.address ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (branch == null) "Create Branch" else "Edit Branch", color = LeafGreen) },
        text = {
            Column {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Branch Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = ValidationEngine.validateBranch(name, existingBranches, branch?.id)
                    if (!validation.isValid) {
                        errorMsg = validation.errorMessage
                    } else {
                        val id = branch?.id ?: "branch_${System.currentTimeMillis() % 10000}"
                        onSave(Branch(id, name.trim(), address.trim()))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        },
        containerColor = CardNavySurface
    )
}

// =========================================================================
// 2. CATEGORIES MANAGEMENT VIEW (WITH SEARCH FILTER)
// =========================================================================
@Composable
fun CategoriesView(categories: List<Category>, repository: MenuRepository) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filteredCategories = remember(categories, searchQuery) {
        if (searchQuery.isBlank()) categories
        else categories.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Menu Categories", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { editingCategory = null; showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("+ Add Category", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Categories (e.g. Shake, Burger, Mojito...)", color = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("✕", color = LeafGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredCategories) { category ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(category.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = LeafGreen)
                            Text("ID: ${category.id} | Order: ${category.displayOrder}", color = TextMuted, fontSize = 12.sp)
                        }
                        Row {
                            TextButton(onClick = { editingCategory = category; showDialog = true }) {
                                Text("Edit", color = LeafGreen)
                            }
                            TextButton(onClick = { scope.launch { repository.deleteCategory(category.id) } }) {
                                Text("Delete", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CategoryDialog(
            category = editingCategory,
            existingCategories = categories,
            onDismiss = { showDialog = false },
            onSave = { category ->
                scope.launch {
                    if (editingCategory == null) repository.addCategory(category) else repository.updateCategory(category)
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun CategoryDialog(
    category: Category?,
    existingCategories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var orderText by remember { mutableStateOf(category?.displayOrder?.toString() ?: "1") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Create Category" else "Edit Category", color = LeafGreen) },
        text = {
            Column {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = orderText,
                    onValueChange = { orderText = it },
                    label = { Text("Display Order Index") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val validation = ValidationEngine.validateCategory(name, existingCategories, category?.id)
                    if (!validation.isValid) {
                        errorMsg = validation.errorMessage
                    } else {
                        val id = category?.id ?: name.trim().lowercase().replace(" ", "_")
                        val order = orderText.toIntOrNull() ?: 999
                        onSave(Category(id, name.trim(), order))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        },
        containerColor = CardNavySurface
    )
}

// =========================================================================
// 3. MENU ITEMS VIEW (WITH ITEM SEARCH & SEARCHABLE CATEGORY SELECTOR)
// =========================================================================
@Composable
fun ItemsView(
    items: List<MenuItem>,
    categories: List<Category>,
    branches: List<Branch>,
    repository: MenuRepository
) {
    var showEditor by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.categoryName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Menu Catalog Items (${filteredItems.size} shown)", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { editingItem = null; showEditor = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("+ Add New Item", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Item Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Items by Name or Category (e.g. Burger, Frappe, Pasta...)", color = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("✕", color = LeafGreen, fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredItems) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                    modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                            Text("Category: ${item.categoryName} | Base Price: ₹${item.defaultPrice}", color = LeafGreen, fontSize = 13.sp)
                            
                            val branchSummary = branches.mapNotNull { b ->
                                val config = item.branches[b.id]
                                if (config != null) "${b.name}: ₹${config.price}" else null
                            }.joinToString(" | ")

                            Text("Branch Pricing: ${branchSummary.ifEmpty { "None Selected" }}", color = TextMuted, fontSize = 12.sp)
                        }

                        Row {
                            TextButton(onClick = { editingItem = item; showEditor = true }) {
                                Text("Edit", color = LeafGreen)
                            }
                            TextButton(onClick = { scope.launch { repository.deleteMenuItem(item.id) } }) {
                                Text("Delete", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        FullScreenMenuItemEditor(
            item = editingItem,
            categories = categories,
            branches = branches,
            existingItems = items,
            onDismiss = { showEditor = false },
            onSave = { newItem ->
                scope.launch {
                    if (editingItem == null) repository.addMenuItem(newItem) else repository.updateMenuItem(newItem)
                    showEditor = false
                }
            }
        )
    }
}

// FULL-SCREEN ITEM EDITOR WITH SEARCHABLE CATEGORY SELECTOR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenMenuItemEditor(
    item: MenuItem?,
    categories: List<Category>,
    branches: List<Branch>,
    existingItems: List<MenuItem>,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var selectedCategoryId by remember { mutableStateOf(item?.categoryId ?: (categories.firstOrNull()?.id ?: "")) }
    var defaultPriceText by remember { mutableStateOf(item?.defaultPrice?.toString() ?: "149") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Category search query inside editor
    var categorySearchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(categories, categorySearchQuery) {
        if (categorySearchQuery.isBlank()) categories
        else categories.filter { it.name.contains(categorySearchQuery, ignoreCase = true) }
    }

    val selectedBranchesMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            branches.forEach { b ->
                put(b.id, item?.branches?.containsKey(b.id) ?: true)
            }
        }
    }

    val branchPricesMap = remember {
        mutableStateMapOf<String, String>().apply {
            branches.forEach { b ->
                val bPrice = item?.branches?.get(b.id)?.price?.toString() ?: defaultPriceText
                put(b.id, bPrice)
            }
        }
    }

    val branchAvailabilityMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            branches.forEach { b ->
                val bAvail = item?.branches?.get(b.id)?.available ?: true
                put(b.id, bAvail)
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavyBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxSize(0.95f)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                // --- TOP HEADER BAR ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (item == null) "Create Menu Item" else "Edit Menu Item: ${item.name}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = LeafGreen
                        )
                        Text(
                            text = "Set item details on the right and multi-branch pricing matrix on the left.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Row {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.padding(end = 12.dp)) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                val activeBranches = selectedBranchesMap.filterValues { it }.keys
                                val validation = ValidationEngine.validateMenuItem(
                                    name = name,
                                    categoryId = selectedCategoryId,
                                    defaultPriceText = defaultPriceText,
                                    selectedBranchIds = activeBranches,
                                    branchPricesMap = branchPricesMap,
                                    existingItems = existingItems,
                                    currentId = item?.id
                                )

                                if (!validation.isValid) {
                                    errorMsg = validation.errorMessage
                                } else {
                                    val itemId = item?.id ?: "item_${System.currentTimeMillis()}"
                                    val categoryName = categories.find { it.id == selectedCategoryId }?.name ?: ""
                                    val defaultPrice = defaultPriceText.toDoubleOrNull() ?: 0.0

                                    val branchConfigs = activeBranches.associateWith { bId ->
                                        val bPrice = branchPricesMap[bId]?.toDoubleOrNull() ?: defaultPrice
                                        val bAvail = branchAvailabilityMap[bId] ?: true
                                        BranchPriceConfig(price = bPrice, available = bAvail)
                                    }

                                    onSave(
                                        MenuItem(
                                            id = itemId,
                                            name = name.trim(),
                                            categoryId = selectedCategoryId,
                                            categoryName = categoryName,
                                            defaultPrice = defaultPrice,
                                            branches = branchConfigs
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
                        ) {
                            Text("Save Menu Item", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- SPLIT BODY CONTENT ---
                Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    
                    // =====================================================
                    // LEFT COLUMN (60% WIDTH): MULTI-BRANCH PRICE MATRIX
                    // =====================================================
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxHeight()
                            .border(1.dp, BorderGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Multi-Branch Price & Availability Matrix (${branches.size} Branches)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = LeafGreen
                                )

                                Row {
                                    TextButton(onClick = {
                                        branches.forEach { selectedBranchesMap[it.id] = true }
                                    }) {
                                        Text("Select All", fontSize = 12.sp, color = LeafGreen)
                                    }
                                    TextButton(onClick = {
                                        branches.forEach { selectedBranchesMap[it.id] = false }
                                    }) {
                                        Text("Deselect All", fontSize = 12.sp, color = TextMuted)
                                    }
                                    TextButton(onClick = {
                                        branches.forEach { branchPricesMap[it.id] = defaultPriceText }
                                    }) {
                                        Text("Sync Base Price", fontSize = 12.sp, color = LeafGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(branches) { branch ->
                                    val isSelected = selectedBranchesMap[branch.id] ?: false
                                    val isAvailable = branchAvailabilityMap[branch.id] ?: true

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) DarkNavyBg else CardNavySurface.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) LeafGreen.copy(alpha = 0.6f) else BorderGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Checkbox(
                                                    checked = isSelected,
                                                    onCheckedChange = { selectedBranchesMap[branch.id] = it },
                                                    colors = CheckboxDefaults.colors(checkedColor = LeafGreen)
                                                )
                                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                                    Text(branch.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                    Text("ID: ${branch.id}", color = TextMuted, fontSize = 11.sp)
                                                }
                                            }

                                            if (isSelected) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("Status: ", fontSize = 11.sp, color = TextMuted)
                                                        Switch(
                                                            checked = isAvailable,
                                                            onCheckedChange = { branchAvailabilityMap[branch.id] = it },
                                                            colors = SwitchDefaults.colors(checkedThumbColor = LeafGreen)
                                                        )
                                                    }

                                                    OutlinedTextField(
                                                        value = branchPricesMap[branch.id] ?: defaultPriceText,
                                                        onValueChange = { branchPricesMap[branch.id] = it },
                                                        label = { Text("Price (₹)") },
                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                        singleLine = true,
                                                        modifier = Modifier.width(130.dp)
                                                    )
                                                }
                                            } else {
                                                Text("Disabled at this Branch", color = TextMuted, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =====================================================
                    // RIGHT COLUMN (40% WIDTH): CORE PROPERTIES & SEARCHABLE CATEGORY SELECTOR
                    // =====================================================
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardNavySurface),
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .border(1.dp, BorderGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
                            Text("Item Core Properties", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = LeafGreen)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (errorMsg != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                ) {
                                    Text(errorMsg!!, color = Color(0xFFEF4444), fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                                }
                            }

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it; errorMsg = null },
                                label = { Text("Item Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // SEARCHABLE CATEGORY SELECTOR
                            Text("Search & Select Category:", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = categorySearchQuery,
                                onValueChange = { categorySearchQuery = it },
                                placeholder = { Text("Type to search categories...", color = TextMuted) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val selectedCat = categories.find { it.id == selectedCategoryId }
                            Text("Selected: ${selectedCat?.name ?: "None"}", color = LeafGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Scrollable list of filtered categories inside editor
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkNavyBg),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .border(1.dp, BorderGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(filteredCategories) { cat ->
                                        val isChosen = cat.id == selectedCategoryId
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedCategoryId = cat.id; errorMsg = null }
                                                .background(
                                                    if (isChosen) LeafGreen.copy(alpha = 0.2f) else Color.Transparent,
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = cat.name,
                                                color = if (isChosen) LeafGreen else Color.White,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                            if (isChosen) {
                                                Text("✓ Selected", color = LeafGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = defaultPriceText,
                                onValueChange = { defaultPriceText = it },
                                label = { Text("Default Base Price (₹)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // LIVE PREVIEW CARD
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkNavyBg),
                                modifier = Modifier.fillMaxWidth().border(1.dp, BorderGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Live Item Summary", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LeafGreen)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(name.ifEmpty { "[Item Name]" }, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    Text("Category: ${selectedCat?.name ?: "None"}", fontSize = 12.sp, color = TextMuted)
                                    Text("Base Price: ₹${defaultPriceText.ifEmpty { "0" }}", fontSize = 12.sp, color = LeafGreen)

                                    val activeCount = selectedBranchesMap.values.count { it }
                                    Text("Selected Active Branches: $activeCount / ${branches.size}", fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
