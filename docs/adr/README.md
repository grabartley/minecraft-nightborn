# Architecture Decision Records

These records capture **why** Nightborn is built the way it is. They are not specifications: what the mod does lives in [`../prd.md`](../prd.md), and the acceptance criteria for a specific piece of work live on the issue that owns it.

A record is historical. When a decision changes, add a new record superseding the old one rather than editing it, so the reasoning behind the original choice is not lost.

Shared engineering standards across all the mods in this family live in [`../standards.md`](../standards.md).

| Record | Decision |
|---|---|
| [0001](0001-a-linter-forbids-comments-rather-than-a-formatter-stripping-them.md) | A linter forbids comments rather than a formatter stripping them |
| [0002](0002-player-state-lives-in-fabric-attachments.md) | Player state lives in Fabric attachments |
| [0003](0003-animated-furniture-is-one-geckolib-block-entity-driven-by-a-block-state.md) | Animated furniture is one GeckoLib block entity driven by a block state |
| [0004](0004-every-setting-is-declared-once.md) | Every setting is declared once |
| [0005](0005-blood-replaces-the-hunger-tick-for-vampires.md) | Blood replaces the hunger tick for vampires |
| [0006](0006-feeding-rides-vanillas-held-use-packets.md) | Feeding rides vanilla's held-use packets |
| [0007](0007-turning-happens-where-the-player-stands.md) | Turning happens where the player stands |
| [0008](0008-vampire-rules-apply-to-any-vampire-entity.md) | Vampire rules apply to any vampire entity |
| [0009](0009-skills-are-data-in-synced-registries.md) | Skills are data in synced registries |
| [0010](0010-coffins-are-vanilla-beds-drawn-with-vanilla-wood.md) | Coffins are vanilla beds drawn with vanilla wood |
| [0011](0011-vampire-structures-share-one-structure-type.md) | Vampire structures share one structure type |
| [0012](0012-wards-are-found-near-vampires-and-indexed-on-block-change.md) | Wards are found near vampires and indexed on block change |
