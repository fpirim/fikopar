# 9. UI stack: Tailwind, shadcn/ui, AG Grid Community, light theme

Status: Accepted · 2026-09-29

## Context
The approved screens are dense operator forms and grids with one accent colour. v2 used MUI and AG Grid
Enterprise; v3 has Tailwind and a dark-mode toggle nobody asked for.

## Decision
Tailwind plus shadcn/ui (Radix) primitives copied into `src/components/ui`, themed from
`src/styles/tokens.css`. Catalog search uses shadcn Command (cmdk). Grids are AG Grid Community with the
alpine theme mapped to the same tokens (`src/styles/ag-grid-theme.css`). Light theme only. No MUI outside
`legacy/`.

## Consequences
No licence keys and no component library lock-in. Primitives are added when a screen needs them. Dark
theme returns as one more variable block over the same tokens; components already read variables only.
