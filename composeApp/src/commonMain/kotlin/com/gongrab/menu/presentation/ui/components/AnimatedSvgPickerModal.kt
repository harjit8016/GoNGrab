package com.gongrab.menu.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gongrab.menu.domain.model.AnimatedSvgItem

private val LeafGreen = Color(0xFF9EC956)
private val DarkNavyBg = Color(0xFF0F172A)
private val CardNavySurface = Color(0xFF1E293B)
private val TextMuted = Color(0xFF94A3B8)
private val BorderGreen = Color(0x559EC956)

val DEFAULT_ANIMATED_PRESETS = listOf(
    AnimatedSvgItem(
        id = "anim_preset_burger",
        name = "Animated Stacking Burger 🍔",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } }
    @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
    .anim-burger { animation: burgerBounce 2s ease-in-out infinite; }
    .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }
  </style>
  <g class="anim-burger">
    <path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" />
    <ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" />
    <ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" />
    <path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" />
    <path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" />
    <rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" />
    <path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" />
  </g>
</svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_coffee",
        name = "Animated Steaming Coffee ☕",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } }
    .steam-line-1 { animation: steamRise 2s ease-in-out infinite; }
    .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }
  </style>
  <ellipse cx="40" cy="68" rx="24" ry="5" fill="#9ec956" opacity="0.4" />
  <path d="M22 44 L24 66 Q24 70 28 70 L52 70 Q56 70 56 66 L58 44 Z" fill="#9ec956" />
  <ellipse cx="40" cy="44" rx="18" ry="6" fill="#5a2800" />
  <path d="M58 50 Q70 50 70 58 Q70 66 58 66" stroke="#3a4a1a" stroke-width="3.5" fill="none" stroke-linecap="round" />
  <path class="steam-line-1" d="M34 38 Q32 30 34 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" />
  <path class="steam-line-2" d="M44 38 Q42 30 44 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" />
</svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_shake",
        name = "Animated Pulsing Shake 🥤",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes shakePulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.05); } }
    .anim-shake { animation: shakePulse 1.8s ease-in-out infinite; transform-origin: center; }
  </style>
  <g class="anim-shake">
    <path d="M24 28 L28 68 Q28 72 32 72 L48 72 Q52 72 52 68 L56 28 Z" fill="#9ec956" />
    <rect x="22" y="24" width="36" height="6" rx="3" fill="#3a4a1a" />
    <rect x="44" y="8" width="5" height="32" rx="2.5" fill="#3a4a1a" />
    <circle cx="44" cy="8" r="2.5" fill="#3a4a1a" />
    <ellipse cx="40" cy="24" rx="14" ry="5" fill="white" />
  </g>
</svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pizza",
        name = "Animated Sizzling Pizza 🍕",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg">
  <style>
    @keyframes pizzaRotate { 0%, 100% { transform: rotate(0deg); } 50% { transform: rotate(5deg); } }
    .anim-pizza { animation: pizzaRotate 2.5s ease-in-out infinite; transform-origin: center; }
  </style>
  <g class="anim-pizza">
    <path d="M40 10 L72 70 L8 70 Z" fill="#e8aa30" />
    <path d="M40 16 L68 66 L12 66 Z" fill="#e84030" opacity="0.8" />
    <path d="M40 20 L64 64 L16 64 Z" fill="#f0c040" opacity="0.7" />
    <ellipse cx="40" cy="70" rx="32" ry="8" fill="#e8aa30" />
    <circle cx="36" cy="38" r="5" fill="#e84030" />
    <circle cx="44" cy="50" r="5" fill="#e84030" />
  </g>
</svg>"""
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimatedSvgPickerModal(
    selectedAnimatedSvg: String,
    dbAnimatedSvgPack: List<AnimatedSvgItem>,
    onAnimatedSvgSelected: (String) -> Unit,
    onUploadToDbPack: (AnimatedSvgItem) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val fullPack = remember(dbAnimatedSvgPack) {
        (DEFAULT_ANIMATED_PRESETS + dbAnimatedSvgPack).distinctBy { it.id }
    }

    val filteredPack = remember(fullPack, searchQuery) {
        if (searchQuery.isBlank()) fullPack
        else fullPack.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkNavyBg),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .width(640.dp)
                .height(580.dp)
                .border(1.dp, BorderGreen, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🎬 Database Animated SVG Pack Gallery", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = LeafGreen)
                        Text("Select an existing animated SVG from the database or upload a new one.", fontSize = 11.sp, color = TextMuted)
                    }
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // SEARCH BAR
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("🔍 Search Animated SVG Pack by Name (e.g. Burger, Coffee, Shake...)", color = TextMuted) },
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

                Spacer(modifier = Modifier.height(10.dp))

                // UPLOAD NEW ANIMATED SVG TO DB PACK BUTTON
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Existing DB Pack Items (${filteredPack.size}):", fontSize = 12.sp, color = TextMuted)

                    Button(
                        onClick = {
                            try {
                                val fileDialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Animated SVG File", java.awt.FileDialog.LOAD)
                                fileDialog.setFilenameFilter { _, fileName -> fileName.lowercase().endsWith(".svg") }
                                fileDialog.isVisible = true
                                val dir = fileDialog.directory
                                val file = fileDialog.file
                                if (dir != null && file != null) {
                                    val selectedFile = java.io.File(dir, file)
                                    val content = selectedFile.readText()
                                    if (content.contains("<svg", ignoreCase = true)) {
                                        val itemName = file.replace(".svg", "", ignoreCase = true).replace("_", " ").replace("-", " ")
                                        val newItem = AnimatedSvgItem(
                                            id = "anim_${System.currentTimeMillis()}",
                                            name = itemName.replaceFirstChar { it.uppercase() } + " 🎬",
                                            svgContent = content
                                        )
                                        onUploadToDbPack(newItem)
                                        onAnimatedSvgSelected(content)
                                        onDismiss()
                                    }
                                }
                            } catch (e: Exception) {
                                println("SVG upload error: ${e.message}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LeafGreen, contentColor = Color.Black)
                    ) {
                        Text("📁 + Upload New Animated SVG to DB Pack", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // NONE / CLEAR SELECTION OPTION
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (selectedAnimatedSvg.isEmpty()) LeafGreen.copy(alpha = 0.25f) else CardNavySurface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAnimatedSvgSelected(""); onDismiss() }
                        .border(1.dp, if (selectedAnimatedSvg.isEmpty()) LeafGreen else BorderGreen, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🚫 (None - Clear Animated SVG)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        if (selectedAnimatedSvg.isEmpty()) {
                            Text("✓ Selected", color = LeafGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ANIMATED SVG PACK GRID
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 130.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    items(filteredPack) { item ->
                        val isSelected = selectedAnimatedSvg == item.svgContent

                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) LeafGreen.copy(alpha = 0.25f) else CardNavySurface),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAnimatedSvgSelected(item.svgContent)
                                    onDismiss()
                                }
                                .border(1.dp, if (isSelected) LeafGreen else BorderGreen, RoundedCornerShape(8.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) LeafGreen else Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Saved in DB Pack",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
