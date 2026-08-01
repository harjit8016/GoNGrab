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
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pulseGlow { 0%,100% { transform: scale(1); filter: drop-shadow(0 0 4px #9EC956); } 50% { transform: scale(1.06); filter: drop-shadow(0 0 10px #A3E635); } } @keyframes strawWiggle { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-5deg); } } .anim-shake-main { animation: pulseGlow 2s ease-in-out infinite; transform-origin: center; } .anim-straw { animation: strawWiggle 1.8s ease-in-out infinite; transform-origin: bottom center; }</style><g class="anim-shake-main"><path class="anim-straw" d="M54 10 L58 38" stroke="#EF4444" stroke-width="5" stroke-linecap="round" /><path d="M26 36 L32 84 Q32 88 38 88 L62 88 Q68 88 68 84 L74 36 Z" fill="#9EC956" /><ellipse cx="50" cy="36" rx="24" ry="8" fill="#F8FAFC" /><path d="M30 36 Q50 20 70 36" fill="#F472B6" /><circle cx="50" cy="22" r="5" fill="#EF4444" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_mojito",
        name = "Animated Mojito 🍹",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes riseBubbles { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 1; } 100% { opacity: 0; transform: translateY(-20px); } } @keyframes leafSway { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(8deg); } } .b-1 { animation: riseBubbles 1.6s ease-in-out infinite; } .b-2 { animation: riseBubbles 1.6s ease-in-out infinite 0.5s; } .anim-leaf { animation: leafSway 2.2s ease-in-out infinite; transform-origin: bottom left; }</style><path d="M28 28 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L72 28 Z" fill="#34D399" opacity="0.9" /><ellipse cx="50" cy="28" rx="22" ry="6" fill="#F8FAFC" opacity="0.6" /><circle class="b-1" cx="42" cy="64" r="3.5" fill="#FFFFFF" opacity="0.8" /><circle class="b-2" cx="56" cy="52" r="2.5" fill="#FFFFFF" opacity="0.8" /><g class="anim-leaf" transform="translate(48, 14)"><path d="M0 16 Q-12 0 0 -12 Q12 0 0 16 Z" fill="#10B981" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_smoothies",
        name = "Animated Smoothies 🍓",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes floatSmooth { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-smooth-main { animation: floatSmooth 2.2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-smooth-main"><path d="M30 38 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L70 38 Z" fill="#F472B6" /><path d="M26 38 Q50 18 74 38 Z" fill="#F8FAFC" /><path d="M34 32 Q50 22 66 32 Z" fill="#EC4899" /><circle cx="50" cy="20" r="6" fill="#EF4444" /><path d="M50 14 L53 8" stroke="#15803D" stroke-width="2.5" stroke-linecap="round" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_icetea",
        name = "Animated Ice Tea 🧊",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes floatIce { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(10deg); } } .anim-ice-cube { animation: floatIce 2.5s ease-in-out infinite; transform-origin: center; }</style><path d="M30 32 L34 84 Q34 88 40 88 L60 88 Q66 88 66 84 L70 32 Z" fill="#F59E0B" opacity="0.95" /><g class="anim-ice-cube"><rect x="38" y="44" width="10" height="10" rx="3" fill="#FFFFFF" opacity="0.8" /><rect x="50" y="56" width="10" height="10" rx="3" fill="#FFFFFF" opacity="0.8" /><path d="M60 18 A 14 14 0 0 1 60 42 Z" fill="#FACC15" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pasta",
        name = "Animated Pasta 🍝",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes twirlFork { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(14deg); } } .anim-fork-twirl { animation: twirlFork 2s ease-in-out infinite; transform-origin: 50px 18px; }</style><ellipse cx="50" cy="68" rx="34" ry="16" fill="#FACC15" /><ellipse cx="50" cy="62" rx="26" ry="12" fill="#EF4444" opacity="0.9" /><g class="anim-fork-twirl"><path d="M46 16 L46 42 M54 16 L54 42 M43 16 L57 16 M50 42 L50 60" stroke="#CBD5E1" stroke-width="3" stroke-linecap="round" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_maggie",
        name = "Animated Maggie 🍜",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes maggieSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-10px); } } .ms-line-1 { animation: maggieSteam 1.8s ease-in-out infinite; } .ms-line-2 { animation: maggieSteam 1.8s ease-in-out infinite 0.6s; }</style><path class="ms-line-1" d="M40 30 Q36 22 40 14" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="ms-line-2" d="M60 30 Q56 22 60 14" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path d="M20 44 Q20 80 50 80 Q80 80 80 44 Z" fill="#FDE047" /><path d="M26 48 Q38 56 50 48 Q62 56 74 48" stroke="#F59E0B" stroke-width="4" stroke-linecap="round" fill="none" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_dessert",
        name = "Animated Dessert 🍨",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes cherryDip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(4px); } } .anim-cherry-dip { animation: cherryDip 2s ease-in-out infinite; }</style><path d="M30 80 L70 80 L64 88 L36 88 Z" fill="#94A3B8" /><path d="M48 58 L48 80 M52 58 L52 80" stroke="#CBD5E1" stroke-width="4" /><circle cx="50" cy="42" r="20" fill="#F472B6" /><circle cx="36" cy="52" r="16" fill="#38BDF8" /><circle cx="64" cy="52" r="16" fill="#FACC15" /><circle class="anim-cherry-dip" cx="50" cy="22" r="6" fill="#EF4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_sandwich",
        name = "Animated Sandwich 🥪",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes bounceSandwich { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-sand-bounce { animation: bounceSandwich 1.8s ease-in-out infinite; transform-origin: center; }</style><g class="anim-sand-bounce"><path d="M16 60 L50 28 L84 60 Z" fill="#E8AA30" /><path d="M14 62 L86 62 L84 70 L16 70 Z" fill="#22C55E" /><rect x="16" y="70" width="68" height="8" fill="#EF4444" /><path d="M16 78 L50 90 L84 78 Z" fill="#E8AA30" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_subsandwich",
        name = "Animated Sub Sandwich 🥖",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes slideSub { 0%,100% { transform: translateX(0); } 50% { transform: translateX(4px); } } .anim-sub-slide { animation: slideSub 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-sub-slide"><rect x="14" y="38" width="72" height="20" rx="10" fill="#E8AA30" /><rect x="16" y="54" width="68" height="8" rx="3" fill="#22C55E" /><rect x="16" y="62" width="68" height="10" rx="4" fill="#EF4444" /><rect x="14" y="68" width="72" height="16" rx="8" fill="#E8AA30" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_garlicbread",
        name = "Animated Garlic Bread 🍞",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes garlicGlow { 0%,100% { opacity: 0.6; } 50% { opacity: 1; } } .anim-garlic { animation: garlicGlow 1.5s infinite; }</style><path d="M20 68 Q50 28 80 68 Z" fill="#E8AA30" /><path class="anim-garlic" d="M28 64 Q50 36 72 64 Z" fill="#FACC15" /><circle cx="42" cy="52" r="3" fill="#15803D" /><circle cx="58" cy="50" r="3" fill="#15803D" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_taco",
        name = "Animated Taco 🌮",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rockTaco { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-8deg); } } .anim-taco-rock { animation: rockTaco 2s ease-in-out infinite; transform-origin: bottom center; }</style><g class="anim-taco-rock"><path d="M16 68 Q50 20 84 68 Z" fill="#F59E0B" /><path d="M22 66 Q50 28 78 66 Z" fill="#EF4444" opacity="0.85" /><path d="M26 64 Q50 34 74 64 Z" fill="#22C55E" /><path d="M30 62 Q50 40 70 62 Z" fill="#FACC15" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_hotdog",
        name = "Animated Hot Dog 🌭",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes sizzleDog { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-3px); } } .anim-dog-sizzle { animation: sizzleDog 1.6s ease-in-out infinite; transform-origin: center; }</style><g class="anim-dog-sizzle"><rect x="14" y="46" width="72" height="24" rx="12" fill="#E8AA30" /><rect x="8" y="52" width="84" height="14" rx="7" fill="#B91C1C" /><path d="M18 58 Q28 50 38 60 Q48 50 58 60 Q68 50 78 58" stroke="#FACC15" stroke-width="4" stroke-linecap="round" fill="none" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_coffee",
        name = "Animated Coffee ☕",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes steamRise { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-10px); } } .steam-line-1 { animation: steamRise 2s ease-in-out infinite; } .steam-line-2 { animation: steamRise 2s ease-in-out infinite 0.6s; }</style><ellipse cx="50" cy="84" rx="30" ry="6" fill="#9EC956" opacity="0.4" /><path d="M26 52 L28 82 Q28 88 34 88 L66 88 Q72 88 72 82 L74 52 Z" fill="#9EC956" /><ellipse cx="50" cy="52" rx="24" ry="8" fill="#5A2800" /><path d="M74 60 Q88 60 88 70 Q88 80 74 80" stroke="#3A4A1A" stroke-width="4.5" stroke-linecap="round" fill="none" /><path class="steam-line-1" d="M42 44 Q40 34 42 24" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="steam-line-2" d="M56 44 Q54 34 56 24" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_coldcoffee",
        name = "Animated Cold Coffee 🧋",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes ccSwirl { 0%,100% { transform: scale(1); } 50% { transform: scale(1.05); } } .anim-cc-swirl { animation: ccSwirl 2s infinite; transform-origin: center; }</style><g class="anim-cc-swirl"><path d="M32 38 L36 84 Q36 88 42 88 L58 88 Q64 88 64 84 L68 38 Z" fill="#78350F" /><path d="M30 30 Q50 14 70 30 Z" fill="#FFFFFF" /><rect x="52" y="10" width="5" height="34" fill="#EF4444" rx="2.5" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_nonvegsnacks",
        name = "Animated Non-Veg Snacks 🍗",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes legSizzle { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-7deg); } } .anim-leg-sizzle { animation: legSizzle 1.8s ease-in-out infinite; transform-origin: bottom left; }</style><g class="anim-leg-sizzle"><path d="M34 64 L22 78 Q18 82 22 86 Q26 88 30 82 L42 70 Z" fill="#F8FAFC" /><path d="M34 60 C34 34 70 28 78 50 C82 68 52 78 34 60 Z" fill="#B45309" stroke="#78350F" stroke-width="2.5" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pastry",
        name = "Animated Pastry 🍰",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pastryCherry { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } .anim-pastry-cherry { animation: pastryCherry 2s ease-in-out infinite; }</style><path d="M18 78 L82 78 L82 48 L18 60 Z" fill="#78350F" /><path d="M18 60 L82 48 L82 56 L18 68 Z" fill="#F472B6" /><path d="M18 68 L82 56 L82 64 L18 76 Z" fill="#FFFFFF" /><circle class="anim-pastry-cherry" cx="48" cy="34" r="7" fill="#EF4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_gongrabspecial",
        name = "Animated Go N Grab Special 🌟",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes star3dRot { 0% { transform: rotateY(0deg); } 100% { transform: rotateY(360deg); } } .anim-star-3d { animation: star3dRot 4s linear infinite; transform-origin: center; }</style><g class="anim-star-3d"><polygon points="50,12 62,38 90,40 68,60 74 88 50 72 26 88 32 60 10 40 38 38" fill="#FACC15" stroke="#EAB308" stroke-width="2.5" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_burger",
        name = "Animated Stacking Burger 🍔",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes burgerBounce { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); } } @keyframes topBunFloat { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-6px); } } .anim-burger-main { animation: burgerBounce 2s ease-in-out infinite; } .anim-bun-top { animation: topBunFloat 2s ease-in-out infinite; }</style><g class="anim-burger-main"><path class="anim-bun-top" d="M18 46 Q18 26 50 26 Q82 26 82 46 L80 54 L20 54 Z" fill="#E8AA30" /><ellipse cx="40" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="50" cy="38" rx="3.5" ry="1.8" fill="#C47A00" /><ellipse cx="60" cy="42" rx="3.5" ry="1.8" fill="#C47A00" /><path d="M18 54 Q25 48 32 54 Q39 60 46 54 Q53 48 60 54 Q67 60 74 54 L78 62 L22 62 Z" fill="#22C55E" /><path d="M16 62 L84 62 L82 70 L18 70 Z" fill="#F0C040" /><rect x="18" y="76" width="64" height="10" rx="4" fill="#7A4010" /><path d="M20 86 L80 86 Q80 92 50 92 Q20 92 20 86 Z" fill="#E8AA30" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_fries",
        name = "Animated Fries 🍟",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes friesPop { 0%,100% { transform: translateY(0); } 50% { transform: translateY(-5px); } } .anim-fry-pop { animation: friesPop 1.6s ease-in-out infinite; }</style><path class="anim-fry-pop" d="M30 30 L35 62 L40 30 M42 22 L45 62 L50 22 M52 28 L55 62 L60 28 M62 34 L65 62 L70 34" stroke="#FACC15" stroke-width="5.5" stroke-linecap="round" /><path d="M24 54 L30 90 L70 90 L76 54 Z" fill="#EF4444" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_momos",
        name = "Animated Momos 🥟",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes momoSteam { 0% { opacity: 0; transform: translateY(0); } 50% { opacity: 0.8; } 100% { opacity: 0; transform: translateY(-8px); } } .ms-1 { animation: momoSteam 1.6s ease-in-out infinite; } .ms-2 { animation: momoSteam 1.6s ease-in-out infinite 0.5s; }</style><path class="ms-1" d="M42 32 Q40 24 42 16" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><path class="ms-2" d="M58 32 Q56 24 58 16" stroke="#FFFFFF" stroke-width="2.5" stroke-linecap="round" fill="none" /><ellipse cx="50" cy="70" rx="30" ry="16" fill="#F8FAFC" stroke="#CBD5E1" stroke-width="2.5" /><path d="M40 58 Q50 50 60 58" stroke="#94A3B8" stroke-width="2.5" stroke-linecap="round" fill="none" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_springroll",
        name = "Animated Spring Roll 🌯",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes springCrunch { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(7deg); } } .anim-spring-roll { animation: springCrunch 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-spring-roll"><rect x="22" y="42" width="56" height="20" rx="10" transform="rotate(-20 50 52)" fill="#D97706" stroke="#B45309" stroke-width="2.5" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_waffle",
        name = "Animated Waffle 🧇",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes syrupDrip { 0%,100% { transform: translateY(0); } 50% { transform: translateY(3px); } } .anim-syrup-drip { animation: syrupDrip 2s ease-in-out infinite; }</style><rect x="24" y="24" width="52" height="52" rx="8" fill="#F59E0B" stroke="#D97706" stroke-width="3.5" /><line x1="41" y1="24" x2="41" y2="76" stroke="#B45309" stroke-width="2.5" /><line x1="59" y1="24" x2="59" y2="76" stroke="#B45309" stroke-width="2.5" /><line x1="24" y1="41" x2="76" y2="41" stroke="#B45309" stroke-width="2.5" /><line x1="24" y1="59" x2="76" y2="59" stroke="#B45309" stroke-width="2.5" /><path class="anim-syrup-drip" d="M34 24 Q44 34 54 24 Q64 32 74 24 Z" fill="#78350F" opacity="0.9" /></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_pizza",
        name = "Animated Sizzling Pizza 🍕",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes pizzaTilt { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(7deg); } } .anim-pizza-tilt { animation: pizzaTilt 2.2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-pizza-tilt"><path d="M50 12 L90 86 L10 86 Z" fill="#E8AA30" /><path d="M50 20 L84 80 L16 80 Z" fill="#EF4444" opacity="0.88" /><ellipse cx="50" cy="86" rx="40" ry="10" fill="#E8AA30" /><circle cx="45" cy="46" r="5.5" fill="#EF4444" /><circle cx="55" cy="62" r="5.5" fill="#EF4444" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_wrap",
        name = "Animated Wrap 🌯",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes rollWrap { 0%,100% { transform: rotate(0deg); } 50% { transform: rotate(-6deg); } } .anim-wrap-roll { animation: rollWrap 2s ease-in-out infinite; transform-origin: center; }</style><g class="anim-wrap-roll"><path d="M24 24 L62 84 Q68 92 76 84 L82 74 Q84 66 76 62 L24 24 Z" fill="#FEF08A" stroke="#EAB308" stroke-width="2.5" /><path d="M30 30 L60 70 Z" fill="#22C55E" stroke="#15803D" stroke-width="4" stroke-linecap="round" /></g></svg>"""
    ),
    AnimatedSvgItem(
        id = "anim_preset_addons",
        name = "Animated Add-ons Plus ➕",
        svgContent = """<svg viewBox="0 0 100 100" fill="none" xmlns="http://www.w3.org/2000/svg"><style>@keyframes simplePlusZoom { 0%,100% { transform: scale(1); opacity: 0.85; } 50% { transform: scale(1.15); opacity: 1; } } .anim-simple-plus { animation: simplePlusZoom 2.2s ease-in-out infinite; transform-origin: center; }</style><path class="anim-simple-plus" d="M50 20 L50 80 M20 50 L80 50" stroke="#9EC956" stroke-width="12" stroke-linecap="round" /></svg>"""
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
