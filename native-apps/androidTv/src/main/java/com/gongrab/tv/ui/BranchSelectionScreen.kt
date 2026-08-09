@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.gongrab.tv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.tv.material3.*
import com.gongrab.menu.domain.model.Branch
import com.gongrab.menu.domain.repository.MenuRepository

@Composable
fun BranchSelectionScreen(
    repository: MenuRepository,
    onBranchSelected: (Branch) -> Unit
) {
    val branches by repository.branches.collectAsState()

    // Auto-focus first item on launch per TV Accessibility Guidelines
    val firstItemFocusRequester = remember { FocusRequester() }
    LaunchedEffect(branches) {
        if (branches.isNotEmpty()) {
            firstItemFocusRequester.requestFocus()
        }
    }

    // Exact website tv.css color definitions
    val BgDark = Color(0xFF0A1017) // --bg-dark
    val LogoGreen = Color(0xFF9EC956) // --logo-green
    val TextMuted = Color(0xFF94A3B8) // --text-muted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .padding(vertical = 27.dp), // Overscan safe top/bottom margin
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo Header
        Text(
            text = "GO N GRAB 24 SEVEN",
            color = LogoGreen,
            fontSize = 44.sp,
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Subtitle Pill (Matches .branch-modal-pill from tv.css)
        Box(
            modifier = Modifier
                .background(LogoGreen.copy(alpha = 0.12f), RoundedCornerShape(50))
                .border(1.dp, LogoGreen.copy(alpha = 0.35f), RoundedCornerShape(50))
                .padding(horizontal = 24.dp, vertical = 6.dp)
        ) {
            Text(
                text = "SELECT RESTAURANT BRANCH",
                color = Color(0xFFE2E8F0),
                fontSize = 15.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        if (branches.isEmpty()) {
            Text(
                text = "Loading branches...", 
                color = TextMuted, 
                fontSize = 20.sp,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold
            )
        } else {
            // Horizontal scrolling row per TV Skill Guidelines
            LazyRow(
                contentPadding = PaddingValues(horizontal = 58.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(branches) { idx, branch ->
                    val icons = listOf("🏬", "🏪", "🏙️", "📍")
                    val iconEmoji = icons[idx % icons.size]

                    val cardModifier = Modifier
                        .width(320.dp)
                        .height(360.dp)
                        .then(if (idx == 0) Modifier.focusRequester(firstItemFocusRequester) else Modifier)

                    // Big branch card matching .branch-card-big from tv.css
                    Card(
                        onClick = { onBranchSelected(branch) },
                        modifier = cardModifier,
                        shape = CardDefaults.shape(shape = RoundedCornerShape(24.dp)),
                        colors = CardDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.04f), // Unfocused card bg
                            focusedContainerColor = LogoGreen.copy(alpha = 0.12f) // Focused card bg
                        ),
                        border = CardDefaults.border(
                            focusedBorder = Border(BorderStroke(3.dp, LogoGreen)), // Focused border
                            border = Border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.12f))) // Unfocused border
                        ),
                        scale = CardDefaults.scale(focusedScale = 1.08f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(28.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Circle Emoji Icon (Matches .branch-card-icon)
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(LogoGreen.copy(alpha = 0.15f), CircleShape)
                                    .border(2.dp, LogoGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = iconEmoji, fontSize = 36.sp)
                            }

                            // Branch Name & Subtitle
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = branch.name,
                                    fontSize = 28.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold, // 800 weight
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (branch.address.isNotBlank()) branch.address else "Select to display live menu board",
                                    fontSize = 14.sp,
                                    fontFamily = OutfitFontFamily,
                                    color = TextMuted
                                )
                            }

                            // Enter Menu Pill Button Indicator (Matches .branch-card-btn-indicator)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(LogoGreen, RoundedCornerShape(50))
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "SELECT BRANCH",
                                    color = BgDark, // #0a1017 text
                                    fontSize = 15.sp,
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
