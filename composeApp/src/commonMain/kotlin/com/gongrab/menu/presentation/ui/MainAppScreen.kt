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
import java.util.UUID

enum class NavTab { BRANCHES, CATEGORIES, ITEMS }

@Composable
fun MainAppScreen(repository: MenuRepository) {
    var selectedTab by remember { mutableStateOf(NavTab.ITEMS) }
    val branches by repository.branches.collectAsState()
    val categories by repository.categories.collectAsState()
    val items by repository.items.collectAsState()
    val scope = rememberCoroutineScope()

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
// 2. CATEGORIES MANAGEMENT VIEW
// =========================================================================
@Composable
fun CategoriesView(categories: List<Category>, repository: MenuRepository) {
    var showDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }
    val scope = rememberCoroutineScope()

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

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { category ->
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
// 3. MENU ITEMS VIEW (MULTI-BRANCH PRICING SELECTION & OVERRIDES)
// =========================================================================
@Composable
fun ItemsView(
    items: List<MenuItem>,
    categories: List<Category>,
    branches: List<Branch>,
    repository: MenuRepository
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Menu Catalog Items", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { editingItem = null; showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
            ) {
                Text("+ Add New Item", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
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
                            
                            // Render active branch prices
                            val branchSummary = branches.mapNotNull { b ->
                                val config = item.branches[b.id]
                                if (config != null) "${b.name}: ₹${config.price}" else null
                            }.joinToString(" | ")

                            Text("Branch Pricing: $branchSummary", color = TextMuted, fontSize = 12.sp)
                        }

                        Row {
                            TextButton(onClick = { editingItem = item; showDialog = true }) {
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

    if (showDialog) {
        MenuItemDialog(
            item = editingItem,
            categories = categories,
            branches = branches,
            existingItems = items,
            onDismiss = { showDialog = false },
            onSave = { newItem ->
                scope.launch {
                    if (editingItem == null) repository.addMenuItem(newItem) else repository.updateMenuItem(newItem)
                    showDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
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

    // Multi-branch selection map: BranchId -> Selected State (Boolean)
    val selectedBranchesMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            branches.forEach { b ->
                put(b.id, item?.branches?.containsKey(b.id) ?: true)
            }
        }
    }

    // Per-branch custom price overrides: BranchId -> Price (String)
    val branchPricesMap = remember {
        mutableStateMapOf<String, String>().apply {
            branches.forEach { b ->
                val bPrice = item?.branches?.get(b.id)?.price?.toString() ?: defaultPriceText
                put(b.id, bPrice)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Create Menu Item" else "Edit Menu Item", color = LeafGreen) },
        text = {
            Column(modifier = Modifier.width(440.dp).verticalScroll(rememberScrollState())) {
                if (errorMsg != null) {
                    Text(errorMsg!!, color = Color(0xFFEF4444), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMsg = null },
                    label = { Text("Item Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection Dropdown
                Text("Category Selection:", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                var categoryExpanded by remember { mutableStateOf(false) }
                val currentCategory = categories.find { it.id == selectedCategoryId }

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = currentCategory?.name ?: "Select Category",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = defaultPriceText,
                    onValueChange = { 
                        defaultPriceText = it
                        // Auto-fill branch overrides if default price changes
                        branches.forEach { b ->
                            if (branchPricesMap[b.id].isNullOrEmpty() || branchPricesMap[b.id] == defaultPriceText) {
                                branchPricesMap[b.id] = it
                            }
                        }
                    },
                    label = { Text("Default Base Price (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Multi-Branch Selection & Custom Branch Prices:", color = LeafGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                branches.forEach { b ->
                    val isChecked = selectedBranchesMap[b.id] ?: false
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { selectedBranchesMap[b.id] = it },
                                colors = CheckboxDefaults.colors(checkedColor = LeafGreen)
                            )
                            Text(b.name, color = Color.White, fontSize = 14.sp)
                        }

                        if (isChecked) {
                            OutlinedTextField(
                                value = branchPricesMap[b.id] ?: defaultPriceText,
                                onValueChange = { branchPricesMap[b.id] = it },
                                label = { Text("Branch ₹") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.width(120.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
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
                            BranchPriceConfig(price = bPrice, available = true)
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) }
        },
        containerColor = CardNavySurface
    )
}
