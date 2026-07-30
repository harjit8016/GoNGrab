---
name: mobile_ui_ux
description: Enforce world-class, native mobile UI/UX design standards for Android and iOS applications built with Compose Multiplatform or Flutter/React Native.
---

# Mobile UI/UX Design Standards & Guidelines

When building mobile applications for Android and iOS, desktop or web layouts MUST NOT be reused directly. Mobile interfaces require deliberate, touch-first mobile ergonomics, thumb-zone optimization, and gesture-driven interaction patterns.

## 1. Ergonomics & Thumb Zone Navigation
- **Bottom Navigation Bar**: Primary section switching (e.g. Menu, Branches, Analytics, Settings) must live in a sticky bottom navigation bar or bottom dock within reach of one-handed thumb use.
- **Top App Bar**: Keep compact (Title, Quick Search, Profile avatar). Never crowd the top bar with complex controls.
- **Primary Actions (FAB)**: Major create/add actions (e.g., "+ Add New Item") must use a Floating Action Button or bottom-anchored CTA button.

## 2. Card & List Architecture
- **Single-Column Vertical Feed**: On mobile screens, list items in a single full-width vertical card list with clean padding (16dp edge padding, 12dp card spacing).
- **Large Touch Targets**: All interactive elements (toggles, buttons, icon buttons) must have a minimum tap target of **48dp x 48dp**.
- **Visual Status Badges**: Use clear visual chips for item availability (e.g., Green pill for "In Stock", Muted Red for "Out of Stock").

## 3. Mobile Bottom Sheets for Editing & Creation
- Use Modal Bottom Sheets (`ModalBottomSheet`) for adding/editing menu items instead of center dialog boxes.
- Bottom sheets naturally drag down to dismiss and provide ample vertical scroll area for mobile keyboards.

## 4. Search & Filter Ergonomics
- Sticky or expandable search bar with clear button (`X`).
- Horizontal scrollable pill chips (`LazyRow`) for fast category switching.
