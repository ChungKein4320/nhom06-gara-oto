# Garage UI Guidelines

## 1. Layout contract

Every object screen lives inside the same application shell:

```text
MainFrame
├── Sidebar
├── Header
└── ContentArea (CardLayout)
```

Do not create another `MainFrame` for an object. Do not recreate the sidebar or header inside object screens.

## 2. Object-oriented menu

The sidebar is a flat list of domain/business objects. Actor/role only filters which objects are visible.

```text
Actor → Allowed Objects → Object View → Action
```

Functions such as Create, Edit, Delete, Payment, Issuance, Confirm, Print and Process belong inside the relevant object view.

## 3. Object page patterns

### List

```text
Page header
Search / Filter / Primary action
Table
Footer / Pagination
```

### Detail

```text
Back / breadcrumb
Object identity
Information
Related information
Status / activity
Actions
```

### Form

```text
Dialog/page title
Logical field groups
Clear labels
Validation feedback
Cancel / Save
```

### Transaction / document

```text
Document identity
Line items
Totals / summary
Actions
```

## 4. Design tokens

Use `gui/DesignSystem.java` for every global visual decision.

Do not hard-code colors, typography or spacing in individual object screens unless the value is genuinely local to that component.

## 5. Brand palette

The project owner's six supplied colors are:

| Token | Hex |
|---|---|
| Ink | `#13272E` |
| Teal | `#487674` |
| Mint | `#83B4AE` |
| Sand | `#E8C69B` |
| Peach | `#D69369` |
| Orange | `#CB6330` |

## 6. Iconography

Use the SVG icons in `src/IMG/icons/` through `ComponentFactory.icon(...)`.

Do not use emoji or text characters as icons. Do not mix icon families.

## 7. Team rule

A developer implementing an object may change object-specific content, fields, columns and actions. They must not independently redesign the global shell or invent a new component style.
