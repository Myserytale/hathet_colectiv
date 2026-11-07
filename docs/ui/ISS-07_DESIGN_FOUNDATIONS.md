# PulseVenue — ISS-07: Color Palette & Typography Tokens

**Project:** PulseVenue  
**Issue:** ISS-07 — Sprint 1  
**Area:** UI/UX Design Foundations + Frontend  
**Design file:** [PulseVenue — Figma](https://www.figma.com/design/nfiQ2dH2JfvhQDNbdMKteN/PulseVenue)

## 1. Goal and visual direction

Create a consistent, reusable foundation for PulseVenue's three product interfaces: the attendee mobile wallet, bartender tablet POS, and organizer desktop control room. The visual direction is a **low-glare festival dark theme**, designed for use in changing lighting conditions.

The palette uses deep petrol surfaces, readable off-white text, and a restrained warm-orange action accent. The **60/30/10** proportion is a visual composition guideline, not a WCAG requirement. Critical states must never be communicated by color alone.

## 2. Color tokens

The 13 core palette tokens are defined as Figma color variables and CSS custom properties.

| Role | CSS variable | Hex | Intended use |
|---|---|---|---|
| Base background | `--color-background-base` | `#06171B` | App canvas |
| Panel surface | `--color-surface-panel` | `#0D2224` | Cards and navigation surfaces |
| Elevated surface | `--color-surface-elevated` | `#142D31` | Hover and raised surfaces |
| Primary text | `--color-text-primary` | `#F3F7FA` | Headings and primary labels |
| Secondary text | `--color-text-secondary` | `#C6D8DB` | Supporting content |
| On-accent text | `--color-text-on-accent` | `#101820` | Text on orange buttons |
| Primary accent | `--color-accent-primary` | `#FB9031` | Primary actions, selection |
| Interactive border | `--color-border-interactive` | `#93ADB7` | Inputs and essential control boundaries |
| Focus ring | `--color-focus-ring` | `#FFE08B` | Visible keyboard focus outline |
| Normal | `--color-status-normal` | `#22C55E` | Normal state |
| Elevated | `--color-status-elevated` | `#FACC15` | Warning/elevated state |
| Bottleneck | `--color-status-bottleneck` | `#EF4444` | Critical crowding state |
| Offline | `--color-status-offline` | `#3B82F6` | Connection/offline state |

**Additional CSS-only helper:** `--color-border-subtle: #315158` is used for decorative divisions and card edges. It is **not** one of the 13 core Figma palette tokens. Do not rely on it as the only essential visible boundary of a control; its contrast against dark surfaces is below 3:1.

**Compatibility aliases:** `--bg-primary`, `--bg-card`, `--text-primary`, and `--accent` point to the new named variables so the current React skeleton continues to work.

## 3. Typography

**Font:** Inter, locally imported into React via `@fontsource/inter` (weights **400, 500, 600, 700**). Fallback: system UI sans-serif. The CSS implements 11 reusable type styles:

| Style | Size / line height | Weight | Usage |
|---|---|---|---|
| Display | 40 / 48 px | 700 | Prominent display text |
| H1 | 32 / 40 px | 700 | Main screen headings |
| H2 | 24 / 32 px | 600 | Section headings |
| H3 | 20 / 28 px | 600 | Card/subsection headings |
| Body Large | 18 / 28 px | 400 | Lead paragraphs |
| Body | 16 / 24 px | 400 | Main content |
| Body Small | 14 / 20 px | 400 | Supporting content |
| Label | 14 / 20 px | 500 | Labels |
| Button | 16 / 24 px | 600 | Button captions |
| Caption | 14 / 20 px | 400 | Metadata |
| KPI | 36 / 44 px | 700 | Dashboard numerical metrics |

Typography is represented in `frontend/src/styles.css` by font-size, line-height, weight variables and reusable `.type-*` classes.

## 4. Verified contrast pairs (WCAG calculation)

Contrast is measured from the hex values above, with a WCAG relative-luminance contrast formula. **WCAG AA requires at least 4.5:1 for normal-size text and 3:1 for large text and essential non-text graphical objects.** Passing selected pairs does not mean the entire application has passed an accessibility audit.

| Foreground / background | Contrast | Normal text AA? |
|---|---:|---|
| Primary text `#F3F7FA` / base `#06171B` | **17.01:1** | Pass |
| Secondary text `#C6D8DB` / panel `#0D2224` | **11.21:1** | Pass |
| On-accent text `#101820` / orange `#FB9031` | **7.78:1** | Pass |
| Normal green `#22C55E` / panel `#0D2224` | **7.25:1** | Pass |
| Elevated yellow `#FACC15` / panel `#0D2224` | **10.79:1** | Pass |
| Bottleneck red `#EF4444` / panel `#0D2224` | **4.39:1** | **Fail** for normal-size text |
| Offline blue `#3B82F6` / panel `#0D2224` | **4.49:1** | **Fail** for normal-size text |
| Interactive border `#93ADB7` / panel `#0D2224` | **7.00:1** | Pass even at normal-text threshold |
| Subtle border `#315158` / panel `#0D2224` | **1.93:1** | Not suitable for essential non-text boundaries |

**Implementation rule:** Keep red and blue as status accents for icons, dots, or badges; use compliant high-contrast text for the status label. Add text or iconography so normal/elevated/bottleneck/offline is not conveyed by color alone. Use the interactive border token where a control boundary must be perceptible.

## 5. Interaction and responsiveness

- Minimum button height is set via `--touch-target-min: 3rem` (**48 px**). This supports tablet POS interaction; individual controls still need checking.
- Keyboard focus is indicated with a **3 px** `--color-focus-ring` outline and **3 px** offset for common focusable elements.
- The current application skeleton has a responsive breakpoint at **767 px**. The compact navigation stacks on narrow screens.
- The logo is an external SVG asset used in `frontend/src/App.jsx` and sized in `frontend/src/styles.css`.
- The `NORMAL`, `ELEVATED`, `BOTTLENECK`, and `OFFLINE` states have CSS indicator classes. Full status components are designed in Figma; visual QA across all product screens remains necessary.

## 6. Figma deliverables

In the [PulseVenue Figma file](https://www.figma.com/design/nfiQ2dH2JfvhQDNbdMKteN/PulseVenue):

- **01 – Design Foundations:** 13 core color variables and 11 typography text styles.
- **02 – Brand Identity:** logo variants and brand work. **Check that the newest approved SVG versions are reflected here**; local frontend logos were updated more recently.
- **03 – Components:** reusable Button, Status Badge, Input Field, KPI Card, Alert Banner, Navigation Tab, and Connection State families.

