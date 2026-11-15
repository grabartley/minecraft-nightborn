# Architecture Decision Records

These records capture **why** Nightborn is built the way it is. They are not specifications: what the mod does lives in [`../prd.md`](../prd.md), and the acceptance criteria for a specific piece of work live on the issue that owns it.

A record is historical. When a decision changes, add a new record superseding the old one rather than editing it, so the reasoning behind the original choice is not lost.

Shared engineering standards across all the mods in this family live in [`../standards.md`](../standards.md).

| Record | Decision |
|---|---|
| [0001](0001-a-linter-forbids-comments-rather-than-a-formatter-stripping-them.md) | A linter forbids comments rather than a formatter stripping them |
| [0002](0002-player-state-lives-in-fabric-attachments.md) | Player state lives in Fabric attachments |
| [0003](0003-animated-furniture-is-one-geckolib-block-entity-driven-by-a-block-state.md) | Animated furniture is one GeckoLib block entity driven by a block state |
