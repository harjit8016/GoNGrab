package com.gongrab.menu.presentation.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.gongrab.menu.domain.model.BranchPriceConfig
import com.gongrab.menu.domain.model.Category
import com.gongrab.menu.domain.model.MenuItem
import com.gongrab.menu.domain.repository.MenuRepository
import com.gongrab.menu.presentation.theme.BorderGreen
import com.gongrab.menu.presentation.theme.CardNavySurface
import com.gongrab.menu.presentation.theme.DarkNavyBg
import com.gongrab.menu.presentation.theme.LeafGreen
import com.gongrab.menu.presentation.theme.TextMuted
import com.gongrab.menu.presentation.ui.components.AnimatedSvgPickerModal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MobileAppScreen(repository: MenuRepository) {
    val coroutineScope = rememberCoroutineScope()

    val branches by repository.branches.collectAsState()
    val categories by repository.categories.collectAsState()
    val items by repository.items.collectAsState()
    val animatedSvgPack by repository.animatedSvgPack.collectAsState()

    var selectedBranchId by remember { mutableStateOf("branch_1") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var showBranchDrawer by remember { mutableStateOf(false) }
    var showAddItemSheet by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItem?>(null) }
    var showSvgPickerForItem by remember { mutableStateOf<MenuItem?>(null) }

    // Resolve current branch name for top app bar header
    val currentBranchName = when (selectedBranchId) {
        "all" -> "🌐 All Branches"
        else -> branches.find { it.id == selectedBranchId }?.name ?: "📍 Branch 1"
    }

    // Filter items based on category and search query (SHOW ALL ITEMS in management view)
    val filteredItems = items.filter { item ->
        val matchesCategory = selectedCategoryId == null || item.categoryId == selectedCategoryId
        val matchesSearch = searchQuery.isBlank() || item.name.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.sortedBy { it.displayOrder }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardNavySurface)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Hamburger Button
                    IconButton(onClick = { showBranchDrawer = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Select Branch Hamburger Menu",
                            tint = LeafGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Center Top Header: Selected Branch Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { showBranchDrawer = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(LeafGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = currentBranchName,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = TextMuted
                        )
                    }

                    // Right Search Button
                    IconButton(onClick = { isSearchExpanded = !isSearchExpanded }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Items",
                            tint = LeafGreen
                        )
                    }
                }

                if (isSearchExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search items...", color = TextMuted) },
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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardNavySurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${filteredItems.size} items",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentBranchName,
                            color = LeafGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    FloatingActionButton(
                        onClick = { showAddItemSheet = true },
                        containerColor = LeafGreen,
                        contentColor = Color(0xFF0A1017)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ADD ITEM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        },
        containerColor = DarkNavyBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Horizontal Category Pill Bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val isAllSelected = selectedCategoryId == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedCategoryId = null },
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
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text("${cat.name} ($count)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LeafGreen,
                            selectedLabelColor = Color(0xFF0A1017),
                            containerColor = CardNavySurface,
                            labelColor = Color.White
                        )
                    )
                }
            }

            // Mobile Single-Column Item Feed
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading items from database...",
                        color = TextMuted,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems, key = { it.id }) { item ->
                        MobileMenuItemCard(
                            item = item,
                            selectedBranchId = selectedBranchId,
                            categories = categories,
                            onToggleAvailability = { available ->
                                coroutineScope.launch {
                                    val updatedBranches = item.branches.toMutableMap()
                                    val currentConfig = updatedBranches[selectedBranchId] ?: BranchPriceConfig(price = item.defaultPrice)
                                    updatedBranches[selectedBranchId] = currentConfig.copy(available = available)
                                    repository.updateMenuItem(item.copy(branches = updatedBranches))
                                }
                            },
                            onEditClick = { editingItem = item },
                            onSvgClick = { showSvgPickerForItem = item },
                            onDeleteClick = {
                                coroutineScope.launch { repository.deleteMenuItem(item.id) }
                            }
                        )
                    }
                }
            }
        }
    }

    // Hamburger Branch Selection Modal Sheet
    if (showBranchDrawer) {
        AlertDialog(
            onDismissRequest = { showBranchDrawer = false },
            containerColor = CardNavySurface,
            title = {
                Text(
                    text = "🏬 Select Restaurant Branch",
                    color = LeafGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select branch to manage prices and menu availability:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val allBranchesList = branches.ifEmpty {
                        listOf(
                            com.gongrab.menu.domain.model.Branch("branch_1", "Branch 1", "Main Branch Location"),
                            com.gongrab.menu.domain.model.Branch("branch_2", "Branch 2", "Secondary Branch Location")
                        )
                    }

                    allBranchesList.forEach { b ->
                        val isSelected = selectedBranchId == b.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedBranchId = b.id
                                    showBranchDrawer = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) LeafGreen.copy(alpha = 0.2f) else DarkNavyBg,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) LeafGreen else BorderGreen
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "📍 ${b.name}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    if (b.address.isNotBlank()) {
                                        Text(text = b.address, color = TextMuted, fontSize = 12.sp)
                                    }
                                }

                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Selected", tint = LeafGreen)
                                }
                            }
                        }
                    }

                    // All branches option
                    val isAllSelected = selectedBranchId == "all"
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedBranchId = "all"
                                showBranchDrawer = false
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAllSelected) LeafGreen.copy(alpha = 0.2f) else DarkNavyBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isAllSelected) LeafGreen else BorderGreen
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🌐 All Branches",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            if (isAllSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = LeafGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBranchDrawer = false }) {
                    Text("CLOSE", color = TextMuted)
                }
            }
        )
    }

    // Dialog for Add/Edit
    if (showAddItemSheet || editingItem != null) {
        MobileItemFormDialog(
            item = editingItem,
            categories = categories,
            branches = branches,
            onDismiss = {
                showAddItemSheet = false
                editingItem = null
            },
            onSave = { newItem ->
                coroutineScope.launch {
                    if (editingItem != null) {
                        repository.updateMenuItem(newItem)
                    } else {
                        repository.addMenuItem(newItem)
                    }
                    showAddItemSheet = false
                    editingItem = null
                }
            }
        )
    }

    // SVG Picker Modal
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
                coroutineScope.launch {
                    repository.saveAnimatedSvgToPack(newItem)
                }
            },
            onDismiss = { showSvgPickerForItem = null }
        )
    }
}

@Composable
fun MobileMenuItemCard(
    item: MenuItem,
    selectedBranchId: String,
    categories: List<Category>,
    onToggleAvailability: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onSvgClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val branchConfig = item.branches[selectedBranchId]
    val isAvailable = branchConfig?.available ?: true
    val currentPrice = branchConfig?.price ?: item.defaultPrice
    val categoryName = categories.find { it.id == item.categoryId }?.name ?: "General"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardNavySurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isAvailable) BorderGreen else Color(0xFF334155)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Item Name & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LeafGreen.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LeafGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "₹${currentPrice}",
                        color = LeafGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-info Row: Category & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📂 $categoryName",
                    color = TextMuted,
                    fontSize = 13.sp
                )

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
                        onCheckedChange = onToggleAvailability,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LeafGreen,
                            checkedTrackColor = LeafGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color(0xFF64748B),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFF1E293B)
            )

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actions",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onSvgClick) {
                        Icon(Icons.Default.Star, contentDescription = "SVG Icon", tint = LeafGreen)
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = Color(0xFF38BDF8))
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
    }
}

@Composable
fun MobileItemFormDialog(
    item: MenuItem?,
    categories: List<Category>,
    branches: List<com.gongrab.menu.domain.model.Branch>,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var defaultPriceText by remember { mutableStateOf(item?.defaultPrice?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == item?.categoryId } ?: categories.firstOrNull()) }
    var expandedCatDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardNavySurface,
        title = {
            Text(
                text = if (item != null) "Edit Menu Item" else "Add New Item",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreen,
                        unfocusedBorderColor = BorderGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = defaultPriceText,
                    onValueChange = { defaultPriceText = it },
                    label = { Text("Default Price (₹)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LeafGreen,
                        unfocusedBorderColor = BorderGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                // Category Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCatDropdown = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderGreen)
                    ) {
                        Text(
                            text = "Category: ${selectedCategory?.name ?: "Select Category"}",
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = defaultPriceText.toDoubleOrNull() ?: 0.0
                    val catId = selectedCategory?.id ?: "general"
                    val catName = selectedCategory?.name ?: "General"

                    val newItem = (item ?: MenuItem(
                        id = "item_${kotlin.random.Random.nextLong(100000, 999999)}",
                        name = name,
                        categoryId = catId,
                        categoryName = catName,
                        defaultPrice = price
                    )).copy(
                        name = name,
                        categoryId = catId,
                        categoryName = catName,
                        defaultPrice = price
                    )

                    onSave(newItem)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color(0xFF0A1017))
            ) {
                Text("SAVE ITEM", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextMuted)
            }
        }
    )
}
