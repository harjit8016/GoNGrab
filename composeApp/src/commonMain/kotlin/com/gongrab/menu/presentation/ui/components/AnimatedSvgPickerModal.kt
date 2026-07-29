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
        id = "anim_preset_shake",
        name = "Animated Shake 🥤",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.06); } } .anim-shake { animation: pulse 1.8s infinite; transform-origin: center; }</style><g class="anim-shake"><path d="M24 28 L28 68 Q28 72 32 72 L48 72 Q52 72 52 68 L56 28 Z" fill="#9ec956" /><rect x="22" y="24" width="36" height="6" rx="3" fill="#3a4a1a" /><rect x="44" y="8" width="5" height="32" rx="2.5" fill="#3a4a1a" /><circle cx="44" cy="8" r="2.5" fill="#3a4a1a" /><ellipse cx="40" cy="24" rx="14" ry="5" fill="white" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_mojito",
        name = "Animated Mojito 🍹",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes bubble { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 1; } 100% { opacity: 0; transform: translateY(-12px); } } .anim-bub-1 { animation: bubble 1.6s infinite; } .anim-bub-2 { animation: bubble 1.6s infinite 0.6s; }</style><path d="M26 24 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 24 Z" fill="#9ec956" opacity="0.85" /><path d="M22 20 L58 20 L56 24 L24 24 Z" fill="#ffffff" /><circle class="anim-bub-1" cx="36" cy="50" r="3" fill="#ffffff" /><circle class="anim-bub-2" cx="44" cy="42" r="2.5" fill="#ffffff" /><path d="M46 12 L38 24 L52 24 Z" fill="#22c55e" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_smoothies",
        name = "Animated Smoothies 🍓",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes float { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-smooth { animation: float 2s infinite; transform-origin: center; }</style><g class="anim-smooth"><path d="M26 30 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 30 Z" fill="#ec4899" /><path d="M24 24 Q40 14 56 24 Z" fill="#ffffff" /><circle cx="40" cy="16" r="5" fill="#ef4444" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_icetea",
        name = "Animated Ice Tea 🧊",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes chill { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(8deg); } } .anim-ice { animation: chill 2.2s infinite; transform-origin: center; }</style><g class="anim-ice"><path d="M26 26 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 26 Z" fill="#f59e0b" opacity="0.9" /><rect x="32" y="38" width="8" height="8" rx="2" fill="#ffffff" opacity="0.8" /><rect x="42" y="48" width="8" height="8" rx="2" fill="#ffffff" opacity="0.8" /><path d="M48 14 A 12 12 0 0 1 48 34 Z" fill="#eab308" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pasta",
        name = "Animated Pasta 🍝",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes twirl { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(12deg); } } .anim-fork { animation: twirl 2s infinite; transform-origin: top center; }</style><ellipse cx="40" cy="54" rx="26" ry="14" fill="#eab308" /><ellipse cx="40" cy="50" rx="20" ry="10" fill="#ef4444" opacity="0.8" /><path class="anim-fork" d="M38 12 L38 34 M42 12 L42 34 M36 12 L44 12 M40 34 L40 46" stroke="#94a3b8" stroke-width="2.5" stroke-linecap="round" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_maggie",
        name = "Animated Maggie 🍜",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes steam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .st-1 { animation: steam 1.8s infinite; } .st-2 { animation: steam 1.8s infinite 0.5s; }</style><path class="st-1" d="M32 26 Q30 20 32 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" /><path class="st-2" d="M48 26 Q46 20 48 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" /><path d="M16 36 Q16 64 40 64 Q64 64 64 36 Z" fill="#eab308" /><path d="M22 40 Q31 46 40 40 Q49 46 58 40" stroke="#f59e0b" stroke-width="3" fill="none" stroke-linecap="round" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_dessert",
        name = "Animated Dessert 🍨",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes dip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(3px); } } .anim-dip { animation: dip 2s infinite; }</style><path d="M24 64 L56 64 L52 70 L28 70 Z" fill="#94a3b8" /><circle cx="40" cy="36" r="16" fill="#f472b6" /><circle cx="30" cy="44" r="14" fill="#38bdf8" /><circle cx="50" cy="44" r="14" fill="#facc15" /><circle class="anim-dip" cx="40" cy="20" r="4" fill="#ef4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_sandwich",
        name = "Animated Sandwich 🥪",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes press { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-sand { animation: press 1.8s infinite; transform-origin: center; }</style><g class="anim-sand"><path d="M14 50 L40 24 L66 50 Z" fill="#e8aa30" /><path d="M12 52 L68 52 L66 58 L14 58 Z" fill="#22c55e" /><rect x="14" y="58" width="52" height="6" fill="#ef4444" /><path d="M14 64 L40 74 L66 64 Z" fill="#e8aa30" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_subsandwich",
        name = "Animated Sub Sandwich 🥖",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes slide { 0%,100% { transform: translateX(0); } 50% { transform: translateX(3px); } } .anim-sub { animation: slide 2s infinite; transform-origin: center; }</style><g class="anim-sub"><rect x="12" y="32" width="56" height="16" rx="8" fill="#e8aa30" /><rect x="14" y="44" width="52" height="6" rx="2" fill="#22c55e" /><rect x="14" y="50" width="52" height="8" rx="3" fill="#ef4444" /><rect x="12" y="54" width="56" height="12" rx="6" fill="#e8aa30" opacity="0.9" opacity="0.95" opacity="1" opacity="1" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_garlicbread",
        name = "Animated Garlic Bread 🍞",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes glow { 0%,100% { opacity: 0.5; } 50% { opacity: 1; } } .anim-glow { animation: glow 1.5s infinite; }</style><path d="M16 54 Q40 22 64 54 Z" fill="#e8aa30" /><path class="anim-glow" d="M22 50 Q40 28 58 50 Z" fill="#facc15" /><circle cx="34" cy="42" r="2.5" fill="#15803d" /><circle cx="46" cy="40" r="2.5" fill="#15803d" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_taco",
        name = "Animated Taco 🌮",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rock { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-8deg); } } .anim-taco { animation: rock 2s infinite; transform-origin: bottom center; }</style><g class="anim-taco"><path d="M14 56 Q40 18 66 56 Z" fill="#f59e0b" /><path d="M18 54 Q40 24 62 54 Z" fill="#ef4444" opacity="0.85" /><path d="M22 52 Q40 28 58 52 Z" fill="#22c55e" /><path d="M24 50 Q40 32 56 50 Z" fill="#facc15" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_hotdog",
        name = "Animated Hot Dog 🌭",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes sizzle { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-2px); } } .anim-dog { animation: sizzle 1.6s infinite; transform-origin: center; }</style><g class="anim-dog"><rect x="12" y="38" width="56" height="20" rx="10" fill="#e8aa30" /><rect x="8" y="42" width="64" height="12" rx="6" fill="#b91c1c" /><path d="M16 46 Q24 40 32 48 Q40 40 48 48 Q56 40 64 46" stroke="#facc15" stroke-width="3" fill="none" stroke-linecap="round" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_coffee",
        name = "Animated Coffee ☕",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .steam-line-1 { animation: steamRise 2s ease-in-out infinite; } .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }</style><ellipse cx="40" cy="68" rx="24" ry="5" fill="#9ec956" opacity="0.4" /><path d="M22 44 L24 66 Q24 70 28 70 L52 70 Q56 70 56 66 L58 44 Z" fill="#9ec956" /><ellipse cx="40" cy="44" rx="18" ry="6" fill="#5a2800" /><path d="M58 50 Q70 50 70 58 Q70 66 58 66" stroke="#3a4a1a" stroke-width="3.5" fill="none" stroke-linecap="round" /><path class="steam-line-1" d="M34 38 Q32 30 34 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" /><path class="steam-line-2" d="M44 38 Q42 30 44 22" stroke="#ffffff" stroke-width="2" fill="none" stroke-linecap="round" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_coldcoffee",
        name = "Animated Cold Coffee 🧋",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes swirl { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } } .anim-cc { animation: swirl 2s infinite; transform-origin: center; }</style><g class="anim-cc"><path d="M26 30 L30 68 Q30 72 34 72 L46 72 Q50 72 50 68 L54 30 Z" fill="#78350f" /><path d="M24 24 Q40 12 56 24 Z" fill="#ffffff" /><rect x="42" y="8" width="4" height="28" fill="#ef4444" rx="2" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_nonvegsnacks",
        name = "Animated Non-Veg Snacks 🍗",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes sizzleLeg { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-6deg); } } .anim-leg { animation: sizzleLeg 1.8s infinite; transform-origin: bottom left; }</style><g class="anim-leg"><path d="M28 52 L18 64 Q14 68 18 72 Q22 74 26 68 L36 58 Z" fill="#f8fafc" /><path d="M28 48 C28 28 58 24 64 42 C68 56 42 64 28 48 Z" fill="#b45309" stroke="#78350f" stroke-width="2" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pastry",
        name = "Animated Pastry 🍰",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes cherryGlow { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-cherry { animation: cherryGlow 2s infinite; }</style><path d="M14 62 L66 62 L66 38 L14 48 Z" fill="#78350f" /><path d="M14 48 L66 38 L66 44 L14 54 Z" fill="#f472b6" /><path d="M14 54 L66 44 L66 50 L14 60 Z" fill="#ffffff" /><circle class="anim-cherry" cx="38" cy="28" r="6" fill="#ef4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_gongrabspecial",
        name = "Animated Go N Grab Special 🌟",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes star3d { 0% { transform: rotateY(0deg); } 100% { transform: rotateY(360deg); } } .anim-star3d { animation: star3d 4s linear infinite; transform-origin: center; }</style><g class="anim-star3d"><polygon points="40,10 50,30 72,32 55,47 60,70 40,57 20,70 25,47 8,32 30,30" fill="#facc15" stroke="#eab308" stroke-width="2" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_burger",
        name = "Animated Stacking Burger 🍔",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-burger { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style><g class="anim-burger"><path class="anim-bun-top" d="M14 38 Q14 22 40 22 Q66 22 66 38 L64 44 L16 44 Z" fill="#e8aa30" /><ellipse cx="32" cy="34" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="41" cy="31" rx="3" ry="1.5" fill="#c47a00" /><ellipse cx="50" cy="34" rx="3" ry="1.5" fill="#c47a00" /><path d="M14 44 Q20 40 26 44 Q32 48 38 44 Q44 40 50 44 Q56 48 62 44 L64 50 L16 50 Z" fill="#5a9e2f" /><path d="M12 50 L68 50 L66 56 L14 56 Z" fill="#f0c040" /><rect x="14" y="62" width="52" height="8" rx="3" fill="#7a4010" /><path d="M16 70 L64 70 Q64 74 40 74 Q16 74 16 70 Z" fill="#e8aa30" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_fries",
        name = "Animated Fries 🍟",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes popFries { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-fry { animation: popFries 1.6s infinite; }</style><path class="anim-fry" d="M24 24 L28 50 L32 24 M34 18 L36 50 L40 18 M42 22 L44 50 L48 22 M50 26 L52 50 L56 26" stroke="#facc15" stroke-width="4.5" stroke-linecap="round" /><path d="M20 44 L24 72 L56 72 L60 44 Z" fill="#ef4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_momos",
        name = "Animated Momos 🥟",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes momoSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-6px); } } .ms-1 { animation: momoSteam 1.6s infinite; } .ms-2 { animation: momoSteam 1.6s infinite 0.5s; }</style><path class="ms-1" d="M34 26 Q32 20 34 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" /><path class="ms-2" d="M46 26 Q44 20 46 14" stroke="#ffffff" stroke-width="2" stroke-linecap="round" fill="none" /><ellipse cx="40" cy="56" rx="24" ry="12" fill="#f8fafc" stroke="#cbd5e1" stroke-width="2" /><path d="M32 46 Q40 40 48 46" stroke="#94a3b8" stroke-width="2" fill="none" stroke-linecap="round" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_springroll",
        name = "Animated Spring Roll 🌯",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rollCrunch { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(6deg); } } .anim-roll { animation: rollCrunch 2s infinite; transform-origin: center; }</style><g class="anim-roll"><rect x="18" y="34" width="44" height="16" rx="8" transform="rotate(-20 40 42)" fill="#d97706" stroke="#b45309" stroke-width="2" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_waffle",
        name = "Animated Waffle 🧇",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes dripSyrup { 0%,100% { transform: translateY(0); } 50% { transform: translateY(2px); } } .anim-drip { animation: dripSyrup 2s infinite; }</style><rect x="20" y="20" width="40" height="40" rx="6" fill="#f59e0b" stroke="#d97706" stroke-width="3" /><line x1="33" y1="20" x2="33" y2="60" stroke="#b45309" stroke-width="2" /><line x1="47" y1="20" x2="47" y2="60" stroke="#b45309" stroke-width="2" /><line x1="20" y1="33" x2="60" y2="33" stroke="#b45309" stroke-width="2" /><line x1="20" y1="47" x2="60" y2="47" stroke="#b45309" stroke-width="2" /><path class="anim-drip" d="M28 20 Q36 28 44 20 Q52 26 60 20 Z" fill="#78350f" opacity="0.9" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pizza",
        name = "Animated Sizzling Pizza 🍕",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pizzaRotate { 0%, 100% { transform: rotate(0deg); } 50% { transform: rotate(5deg); } } .anim-pizza { animation: pizzaRotate 2.5s ease-in-out infinite; transform-origin: center; }</style><g class="anim-pizza"><path d="M40 10 L72 70 L8 70 Z" fill="#e8aa30" /><path d="M40 16 L68 66 L12 66 Z" fill="#e84030" opacity="0.8" /><path d="M40 20 L64 64 L16 64 Z" fill="#f0c040" opacity="0.7" /><ellipse cx="40" cy="70" rx="32" ry="8" fill="#e8aa30" /><circle cx="36" cy="38" r="5" fill="#e84030" /><circle cx="44" cy="50" r="5" fill="#e84030" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_wrap",
        name = "Animated Wrap 🌯",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes wrapRoll { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-5deg); } } .anim-wrap { animation: wrapRoll 2s infinite; transform-origin: center; }</style><g class="anim-wrap"><path d="M20 20 L50 68 Q54 74 60 68 L64 60 Q66 54 60 50 L20 20 Z" fill="#fef08a" stroke="#eab308" stroke-width="2" /><path d="M24 24 L48 56 Z" fill="#22c55e" stroke="#15803d" stroke-width="3" stroke-linecap="round" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_addons",
        name = "Animated Add-ons Badge ✨",
        svgContent = """<svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes addPulse { 0%,100% { transform: scale(1); } 50% { transform: scale(1.12); } } .anim-add { animation: addPulse 1.5s infinite; transform-origin: center; }</style><polygon class="anim-add" points="40,10 50,30 72,32 55,47 60,70 40,57 20,70 25,47 8,32 30,30" fill="#9ec956" stroke="#65a30d" stroke-width="2" /></svg>"""
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
