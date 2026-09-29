# Brand

A back-office tool used all day: warm neutral ground, one dark navigation panel, one blue for actions
and links. Numbers are set in a monospace face with tabular figures so columns line up. Status labels
are small monospace chips. No gradients, no illustrations.

## Logo

- Wordmark `FIKOPAR`: Archivo, weight 800, width 125 (Expanded), letter-spacing 0.14em, uppercase.
  Set in CSS (`.fk-wordmark`), not shipped as outlines.
- Mark: an F built from three ledger bars on a 22 × 22 grid. Vertical bar 5 × 22 at x 0; top bar
  15 × 5 at (7, 0); middle bar 10 × 5 at (7, 8.5).
- On light: vertical `#1F5F8B`, bars `#16181D`. On the dark panel: vertical `#8FB7D6`, bars `#FFFFFF`.
- Mark at 18 px beside a 17 px wordmark in the sidebar, gap 10 px. File: `frontend/public/fikopar-mark.svg`.

## Tokens

Source of truth: `frontend/src/styles/tokens.css`.

| Token | Value | Use |
|---|---|---|
| `--fk-bg` | `#F6F5F1` | Page |
| `--fk-surface` / `--fk-surface-sunken` | `#FFFFFF` / `#FBFAF7` | Cards / grid header, pinned rows |
| `--fk-border` / `--fk-border-input` | `#E3E1DA` / `#D5D2C9` | Lines / inputs, buttons |
| `--fk-text` / `-secondary` / `-muted` / `-faint` | `#16181D` / `#474B54` / `#5B606B` / `#8A8F99` | Text |
| `--fk-accent` / `-hover` / `-light` | `#1F5F8B` / `#16476A` / `#8FB7D6` | Actions, links, mark |
| `--fk-negative` | `#8E3222` | Credit balances, errors |
| `--fk-row-selected` | `#EEF3F8` | Selected grid row |
| `--fk-nav-bg` / `-active` / `-text` | `#16181D` / `#23303D` / `#C9CCD3` | Sidebar |
| Chips | blue `#E6EEF5/#1F4E72`, grey `#EEECE6/#474B54`, amber `#F6EEDC/#6F4A00`, green `#E3F0EA/#1D5B43`, red `#F6E4E0/#8E3222` | Status |

Type: IBM Plex Sans 400/500/600 for text, IBM Plex Mono 400/500 for numbers, codes and chips.
Sizes 11, 12, 13, 13.5 (grid), 14, 16, 22, 24 px. Radius 4 (chips, grid inputs), 6 (controls), 8 (cards).
Controls 36 px high (30 px small); grid rows 34 px, header 36 px.
