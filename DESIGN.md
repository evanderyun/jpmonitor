# JPM ERP Design System

> Based on Linear.app design system, adapted for JPM brand with RED primary accent.
> Supports both light mode and dark mode with natural color transitions.
> Single source of truth for all UI components.

## 1. Visual Theme & Atmosphere

Dark-mode-first canvas where content emerges through luminance gradations. Single brand accent: JPM Red replaces Linear indigo.

**Key Characteristics:**
- Dark: #08090a canvas, #0f1011 panels, #191a1b elevated surfaces
- Light: #f7f8f8 canvas, #ffffff cards, #e5e7eb borders
- Brand: JPM Red #dc2626 (replaces Linear indigo #5e6ad2)
- Font: Inter Variable with cv01, ss03 globally, weight 510 as signature
- Borders: semi-transparent (dark) or solid subtle (light)

## 2. Color Palette & Roles

### Dark Mode (Default)
| Token | Value | Usage |
|-------|-------|-------|
| --bg-page | #08090a | Page background |
| --bg-panel | #0f1011 | Sidebar, panels |
| --bg-surface | #191a1b | Cards, dropdowns |
| --bg-elevated | #28282c | Hover, elevated |
| --text-primary | #f7f8f8 | Headings, primary |
| --text-secondary | #d0d6e0 | Body text |
| --text-muted | #8a8f98 | Metadata, labels |
| --text-quaternary | #62666d | Disabled, timestamps |
| --border | rgba(255,255,255,0.08) | Card borders |
| --border-subtle | rgba(255,255,255,0.05) | Dividers |
| --jpm-red | #dc2626 | Primary CTA, accents |
| --jpm-red-hover | #ef4444 | Hover states |
| --jpm-red-subtle | rgba(220,38,38,0.15) | Error/brand surfaces |

### Light Mode
| Token | Value | Usage |
|-------|-------|-------|
| --bg-page | #f7f8f8 | Page background |
| --bg-panel | #ffffff | Sidebar, panels |
| --bg-surface | #ffffff | Cards, dropdowns |
| --bg-elevated | #f3f4f5 | Hover, elevated |
| --text-primary | #0f172a | Headings, primary |
| --text-secondary | #475569 | Body text |
| --text-muted | #94a3b8 | Metadata, labels |
| --text-quaternary | #cbd5e1 | Disabled, timestamps |
| --border | #e2e8f0 | Card borders |
| --border-subtle | #f1f5f9 | Dividers |
| --jpm-red | #dc2626 | Primary CTA, accents (same) |
| --jpm-red-hover | #ef4444 | Hover states (same) |
| --jpm-red-subtle | #fef2f2 | Error/brand surfaces |

### Status Colors
| Token | Dark | Light | Usage |
|-------|------|-------|-------|
| --success | #27a644 | #16a34a | Success, active |
| --warning | #f59e0b | #f59e0b | Warning, pending |
| --error | #ef4444 | #ef4444 | Error, danger |
| --info | #3b82f6 | #3b82f6 | Info, neutral |

## 3. Typography

| Role | Size | Weight | Line Height | Letter Spacing |
|------|------|--------|-------------|----------------|
| Display XL | 72px / 4.5rem | 510 | 1.00 | -1.584px |
| Display Large | 48px / 3rem | 510 | 1.00 | -1.056px |
| Heading 1 | 32px / 2rem | 400 | 1.13 | -0.704px |
| Heading 2 | 24px / 1.5rem | 510 | 1.33 | -0.288px |
| Heading 3 | 20px / 1.25rem | 590 | 1.33 | -0.24px |
| Body Large | 18px / 1.13rem | 400 | 1.60 | -0.165px |
| Body | 16px / 1rem | 400 | 1.50 | normal |
| Body Medium | 16px / 1rem | 510 | 1.50 | normal |
| Small | 14px / 0.88rem | 400 | 1.50 | -0.13px |
| Caption | 13px / 0.81rem | 510 | 1.50 | -0.13px |
| Label | 12px / 0.75rem | 510 | 1.40 | normal |
| Micro | 11px / 0.69rem | 510 | 1.40 | normal |

**Font:** Inter Variable, features cv01, ss03
**Mono:** Source Code Pro or SF Mono

## 4. Component Styles

### Buttons
| Type | Background | Text | Border | Radius |
|------|-----------|------|--------|--------|
| Ghost (dark) | rgba(255,255,255,0.02) | #e2e4e7 | 1px solid rgba(255,255,255,0.08) | 6px |
| Ghost (light) | transparent | #0f172a | 1px solid #e2e8f0 | 6px |
| Primary | var(--jpm-red) | #ffffff | none | 6px |
| Primary Hover | var(--jpm-red-hover) | #ffffff | none | 6px |

### Cards
| Token | Dark | Light |
|-------|------|-------|
| Background | rgba(255,255,255,0.02) | #ffffff |
| Border | 1px solid rgba(255,255,255,0.08) | 1px solid #e2e8f0 |
| Radius | 8px | 8px |
| Shadow (dark) | none (luminance-based) | 0 1px 3px rgba(0,0,0,0.08) |
| Shadow (hover) | bg rgba(255,255,255,0.04) | 0 4px 6px rgba(0,0,0,0.1) |

### Inputs
| Property | Dark | Light |
|----------|------|-------|
| Background | rgba(255,255,255,0.02) | #ffffff |
| Border | 1px solid rgba(255,255,255,0.08) | 1px solid #e2e8f0 |
| Focus Border | 1px solid var(--jpm-red) | 1px solid var(--jpm-red) |
| Radius | 6px | 6px |

### Badges / Pills
| Type | Background | Text | Border | Radius |
|------|-----------|------|--------|--------|
| Success | rgba(39,166,68,0.15) | #27a644 | 1px solid rgba(39,166,68,0.3) | 9999px |
| Neutral (dark) | rgba(255,255,255,0.05) | #d0d6e0 | 1px solid rgba(255,255,255,0.08) | 9999px |
| Neutral (light) | #f1f5f9 | #475569 | 1px solid #e2e8f0 | 9999px |

## 5. Spacing System

Base: 8px
Scale: 4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96

## 6. Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| Micro | 2px | Inline badges |
| Standard | 4px | Small containers |
| Comfortable | 6px | Buttons, inputs |
| Card | 8px | Cards, panels |
| Large | 12px | Featured panels |
| Pill | 9999px | Pills, tags |

## 7. Shadow System

### Dark Mode (luminance-based elevation)
| Level | Background | Border |
|-------|-----------|--------|
| Base | #08090a | none |
| Panel | #0f1011 | rgba(255,255,255,0.05) |
| Card | rgba(255,255,255,0.02) | rgba(255,255,255,0.08) |
| Elevated | rgba(255,255,255,0.04) | rgba(255,255,255,0.08) |

### Light Mode (shadow-based elevation)
| Level | Shadow | Border |
|-------|--------|--------|
| Base | none | none |
| Panel | none | #e2e8f0 |
| Card | 0 1px 3px rgba(0,0,0,0.08) | #e2e8f0 |
| Elevated | 0 4px 6px rgba(0,0,0,0.1) | #cbd5e1 |

## 8. Do and Dont

### Do
- Use var(--jpm-red) as primary brand accent
- Use Inter Variable with cv01, ss03 globally
- Use weight 510 as default emphasis
- Dark mode is native default, light mode is opt-in toggle
- Background luminance for elevation in dark mode
- Shadows for elevation in light mode

### Dont
- Dont use purple/indigo - JPM brand is RED
- Dont use pure white/black text
- Dont use warm colors in chrome
- Dont use positive letter-spacing at display sizes
- Dont skip cv01, ss03 OpenType features

## 9. Implementation

### CSS Custom Properties
All colors use CSS variables toggled via .dark class on html element

### Tailwind Config
References CSS variables, NOT hardcoded hex values

### Dark Mode Toggle
Toggle via localStorage + document.documentElement.classList
