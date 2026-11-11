---
name: RoozYar Life OS
colors:
  surface: '#0b1326'
  surface-dim: '#0b1326'
  surface-bright: '#31394d'
  surface-container-lowest: '#060e20'
  surface-container-low: '#131b2e'
  surface-container: '#171f33'
  surface-container-high: '#222a3d'
  surface-container-highest: '#2d3449'
  on-surface: '#dae2fd'
  on-surface-variant: '#bbcac6'
  inverse-surface: '#dae2fd'
  inverse-on-surface: '#283044'
  outline: '#859490'
  outline-variant: '#3c4947'
  surface-tint: '#4fdbc8'
  primary: '#4fdbc8'
  on-primary: '#003731'
  primary-container: '#14b8a6'
  on-primary-container: '#00423b'
  inverse-primary: '#006b5f'
  secondary: '#ffb95f'
  on-secondary: '#472a00'
  secondary-container: '#ee9800'
  on-secondary-container: '#5b3800'
  tertiary: '#c0c1ff'
  on-tertiary: '#1000a9'
  tertiary-container: '#989bff'
  on-tertiary-container: '#1f1ab3'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#71f8e4'
  primary-fixed-dim: '#4fdbc8'
  on-primary-fixed: '#00201c'
  on-primary-fixed-variant: '#005048'
  secondary-fixed: '#ffddb8'
  secondary-fixed-dim: '#ffb95f'
  on-secondary-fixed: '#2a1700'
  on-secondary-fixed-variant: '#653e00'
  tertiary-fixed: '#e1e0ff'
  tertiary-fixed-dim: '#c0c1ff'
  on-tertiary-fixed: '#07006c'
  on-tertiary-fixed-variant: '#2f2ebe'
  background: '#0b1326'
  on-background: '#dae2fd'
  surface-variant: '#2d3449'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 48px
    letterSpacing: -0.02em
  display-lg-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 30px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 28px
  title-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 24px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 26px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 22px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-compact: 0.75rem
  margin: 1rem
  margin-expanded: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.5rem
  space-xl: 2rem
---

## Brand & Style

This design system establishes a high-performance, cognitive-clarity interface tailored for executive daily management, personal growth tracking, and habit architecture within a native Android environment. 

### Brand Personality & Emotional Impact
- **Intelligent & Focused:** Zero cognitive clutter. Data visualization is crisp, prioritized, and actionable.
- **Calm & Grounded:** Utilizes balanced negative space, subtle surface elevations, and cool slate tones to reduce daily decision fatigue and digital anxiety.
- **Motivating & Rewarding:** Strategic, micro-dosed accents of warm amber celebrate completion metrics and Jalali milestone achievements without juvenile gamification tropes.
- **Bilingual & RTL-First Integrity:** Crafted from the ground up for right-to-left Persian script dynamics, respecting Persian typographic baseline rules, ligature breathing room, and Persian numeral clarity alongside universal Latin fallback.

### Design Movement
**Modern Functional Material (M3 Persian Adaptation):** Synthesizes Google Material Design 3 surface token logic with refined international editorial discipline. The system rejects hyper-glossy or heavy skeuomorphic ornamentation in favor of precision tonal containers, subtle hairline rim borders (1px with 8–12% opacity), deep layered elevation, and tactile physics-driven micro-interactions.

## Colors

The palette is tuned specifically for OLED mobile displays, defaulting to a deep, high-legibility dark theme that conserves battery life while minimizing eye strain throughout prolonged daily planning routines. A complementary light mode mirrors the token hierarchy using clean, cool porcelain and slate foundations.

### Palette Architecture

- **Primary (`#14B8A6` Emerald/Teal):** The core active brand tone, signifying focus, vitality, and life equilibrium. Used for primary CTAs, active status indicators, completed progress paths, and primary navigation states.
- **Secondary (`#F59E0B` Amber Gold):** Reserved for achievements, streak counters, XP rewards, and Jalali calendar priority highlights.
- **Tertiary (`#6366F1` Soft Indigo):** Dedicated to cognitive deep-work blocks, Pomodoro sessions, and time-audit tracking.
- **Error & Urgent (`#F43F5E` Soft Rose):** Highlights missed habits, overdue deadlines, and destructive confirm actions without harsh, panic-inducing reds.

### Surface Tiers & Container Strategy (Dark Mode Default)

- `surface-dim`: `#090D16` — Underlying viewport base for edge-to-edge Android system navigation.
- `surface`: `#0F172A` — Primary scaffold background.
- `surface-container-low`: `#1E293B` — Standard content cards, list row surfaces, and bottom sheets.
- `surface-container`: `#334155` — Elevated sub-cards, nested chips, active search bars, and floating toolbars.
- `surface-container-high`: `#475569` — Hover/pressed feedback states and high-priority chip highlights.
- `outline-variant`: `rgba(255, 255, 255, 0.08)` — Hairline structural separation borders.
- `text-primary`: `#F8FAFC` — High-emphasis body, Persian titles, and metric figures.
- `text-secondary`: `#94A3B8` — Metadata, Persian Jalali sub-dates, and unselected tab icons.
- `text-tertiary`: `#64748B` — Placeholder text and subtle divider hints.

## Typography

The type scale is engineered to gracefully handle bidirectional and pure Persian (Farsi) text. While Latin numerals and text map cleanly to modern geometric forms, the system mandates equivalent proportional scaling for standard Persian system typefaces (such as Vazirmatn or IRANSansX).

### Typographic Specifications & RTL Rules
- **Line Heights:** Persian scripts require 20–25% taller vertical leading compared to standard Latin types to accommodate ascenders, descenders, and accent marks (اعراب) without vertical overlap. All body and headline line-height tokens reflect this expanded box.
- **Numbers:** Persian/Eastern Arabic numerals (`۰, ۱, ۲, ۳, ۴, ۵, ۶, ۷, ۸, ۹`) are tabular by default in dashboard metrics, streak counters, and time displays to ensure vertical column alignment.
- **Letter Spacing:** Tracking is strictly set to `0` or slightly positive (`0.01em`) for body copy in Persian, as negative letter spacing breaks cursive script connectors.

## Layout & Spacing

### Layout Philosophy
The layout adheres strictly to an 8-point base spatial grid (with a 4-point micro-mesh for icon offsets, borders, and tight badge padding) engineered for a target viewport of 393 × 852 px (Android flagship standard).

### Viewport Metrics & Breakpoints
- **Mobile (360px – 480px):** 4-column layout, `16px` outer margin (`space-md`), `16px` gutter. Full fluid width within content boundaries.
- **Foldable & Tablet (600px+):** 8-column layout, `24px` outer margin (`margin-expanded`), with dual-pane split view (Navigation Rail on the right for RTL, content on the left).

### Safe Areas & Touch Target Ergonomics
- **Bottom Navigation Bar:** Height fixed at `80px` (including pill indicators), resting above the Android gesture navigation pill with a bottom inset padding of `16px`.
- **Touch Target Integrity:** Every interactive node (toggles, Jalali calendar day cells, menu icons, habit checkboxes) has an invisible bounding box of at least 48 × 48 px.
- **RTL Fluidity:** Structural directionality is mirrored automatically (`start` = right, `end` = left). Leading icons, back chevron vectors, progress direction, and list item swipe-to-complete actions travel from right to left.

## Elevation & Depth

This design system avoids heavy, muddy drop shadows in favor of **Tonal Surface Stacking** accented with soft, color-tinted ambient glows. This creates a calm, organized mental model where critical tasks float naturally over passive reference data.

### Elevation Hierarchy

1. **Level 0 (Canvas Base):** `#0F172A`. Deep canvas layer for primary background views.
2. **Level 1 (Card & Section Containers):** `#1E293B` overlaid with a 1px border of `rgba(255, 255, 255, 0.06)`. Ambient shadow: `0px 4px 20px -2px rgba(0, 0, 0, 0.35)`.
3. **Level 2 (Active Interactive Cards & Bottom Sheets):** `#243248` with a 1px border of `rgba(255, 255, 255, 0.1)`. Ambient shadow: `0px 12px 32px -4px rgba(0, 0, 0, 0.5)`.
4. **Level 3 (Modals, FABs, and Dropdowns):** `#334155` accented with primary teal ambient luminescence: `0px 8px 24px -2px rgba(20, 184, 166, 0.18)`.
5. **Level 4 (Gamification Streaks & Level-Up Celebrations):** Amber luminescence: `0px 8px 28px 0px rgba(245, 158, 11, 0.22)`.

## Shapes

The geometric identity expresses balance, approachability, and modern precision through consistent rounded forms that match the native Android Material 3 corner continuity.

### Shape Scale Rules
- **Micro Elements (Tags, Badges, Jalali Day Indicators):** `rounded` (8px / `0.5rem`).
- **Standard UI Containers (Cards, Input Fields, List Items):** `rounded-lg` (16px / `1rem`).
- **Surface Overlays (Bottom Sheets, Task Detail Modals, FABs):** `rounded-xl` (24px / `1.5rem`).
- **Continuous Pills (Status Chips, Floating Badges, Action Buttons):** Full pill (`9999px`) to denote interactive pressability.

## Components

### Buttons
- **Primary Action (CTA):** Full-height (48–56px) pill or 16px rounded rectangle. Solid teal background (`#14B8A6`) with deep slate text (`#0F172A`) at `label-lg` bold. Subtle inner highlight on the top border. Ripple feedback is pure white at 12% opacity.
- **Secondary / Ghost:** 16px rounded surface with `#1E293B` fill and 1px border (`rgba(255, 255, 255, 0.1)`). Teal or white label.
- **Floating Action Button (FAB):** 56 × 56 px container with 16px corner radius, floating `16px` above the bottom navigation rail on the bottom-left screen area (optimized for Persian RTL primary thumb zone).

### Cards & Habit Trackers
- **Standard Habit Card:** Background `#1E293B`, rounded at 16px, padding `16px`. Right side houses the task icon and Persian title; left side contains the habit streak badge (`#F59E0B` container) and custom check trigger.
- **Progress Card:** Contains a linear progress track (`#334155`, height 6px, rounded full) with active fill animated from right to left using a vibrant `#14B8A6` gradient.

### Form Inputs & Date Pickers
- **Input Fields:** Minimum height 56px, background `#1E293B`, radius 12px, border 1px `rgba(255, 255, 255, 0.08)`. Placeholder text aligned to the right. When focused, border transitions smoothly to `#14B8A6` with a soft 3px diffused outer halo.
- **Jalali Date Matrix:** Horizontal calendar strip with 7 visible days. Current day is framed with a glowing teal pill; selected active day is solid teal. Persian weekday labels (ش، ی، د، س، چ، پ، ج) centered above Persian day numbers.

### Lists & Feed Items
- **Swipe Actions:** Swiping from right-to-left marks complete (teal fill with checkmark icon); swiping from left-to-right triggers reschedule/postpone (amber fill with clock icon).
- **Separators:** Inset horizontal dividers (`16px` right/left inset) using `rgba(255, 255, 255, 0.04)`.

### Selection Controls
- **Checkboxes:** 24 × 24 px rounded box (6px radius). Unchecked: 2px border in `#64748B`. Checked: filled with `#14B8A6` and an animated check icon in `#0F172A`.
- **Radio Buttons & Switches:** Custom M3 toggle with a 28px height track and 24px thumb. Thumb transitions between muted slate and vibrant teal.

### Chips & Micro-Badges
- **Status Chips:** Height 32px, pill-shaped, padding 0 12px. Background matches status tone with 15% opacity; label text matches status tone at 100% saturation.
- **Streak & XP Counter:** Small pill housing a micro flame/star icon on the right, followed by Persian numerals (e.g. `۱۲ روز`) in bold amber.