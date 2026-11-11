# UI Design Specification & Wireframes (Skeleton)

**Assigned Role:** UI Designer  
**Related Issues:** `ISS-07` (Sprint 1), `ISS-16` (Sprint 2), `ISS-26` (Sprint 4)

---

## 1. Color Palette & Typography Tokens (`✅ DONE: ISS-07`)
> **Completed (`ISS-07`):**
> High-contrast Festival Dark Theme color palette, semantic status tokens (`NORMAL`, `ELEVATED`, `BOTTLENECK`, `OFFLINE`), typography hierarchy, and WCAG contrast verification are documented in **[`docs/ui/ISS-07_DESIGN_FOUNDATIONS.md`](ISS-07_DESIGN_FOUNDATIONS.md)** and implemented in [`frontend/src/styles.css`](../../frontend/src/styles.css).

---

## 2. Screen Wireframes & Component Styling (`TODO: ISS-16`)
> **Task for UI Designer (`ISS-16` Pull Request):** Add component styles and wireframes / Figma exports for the 3 views:
> 1. **Attendee Mobile Wallet (`< 480px`):** Balance display, rotating QR card, countdown bar, and pulsing geofenced Flash Discount banner.
> 2. **Bartender Offline Tablet POS (`768px–1024px`):** Online/Offline status toggle, large one-handed order keypad, QR scan/verify panel, and `localStorage` pending batch queue list.
> 3. **Organizer H3 Crowd & Simulator Control Room (`Desktop`):** H3 zone occupancy cards, bottleneck warning badges, and simulator control bar.

---

## 3. Accessibility & Usability Audit (`TODO: ISS-26`)
> **Task for UI Designer (`ISS-26` Pull Request):** Document WCAG contrast checks for outdoor/night-club lighting and touch-target sizing (`>= 48px`) for bartender tablets.
