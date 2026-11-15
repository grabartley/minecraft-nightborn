# ADR 0012: Wards are found near vampires and indexed on block change

## Context

Garlic Strings, garlic crops, held crucifixes and crucifix shapes all weaken vampires (WEAK-1 to WEAK-4). The checks must look only near vampires (PERF-2), and crucifix shapes must be found when blocks change, not every tick (PERF-3). Shapes are built from a tag (`nightborn:crucifix_materials`) that holds every plank, log and gold block, so they cannot be told apart by block alone, and a data pack can change the tag (COMPAT-5). The ranges and strengths are server settings (CFG-5).

## Decision

**Each vampire looks around itself, every 10 ticks.** The vampire tick runs the ward scan on a beat set by the entity id, so vampires spread their scans across ticks. A human costs nothing. Each scanned source replaces its own entry in the vampire's weakened sources and lasts 15 ticks, so it holds while the vampire stays in range, drops when strings come down or a stronger holder leaves, and fades just after the vampire leaves. Touching garlic lasts 3 seconds, so a brush keeps weakening for a moment after.

**Garlic Strings are points of interest.** The string is registered as a vanilla point of interest type, so the world already tracks every string per chunk section and saves it. Counting strings near a vampire is one vanilla query.

**Touching garlic is a block collision.** The crop and the string add a strong source to a vampire whose box is in their cell. Holding garlic adds the same source during the scan.

**Crucifix shapes live in a per-world index updated on block change.** A mixin on `ServerWorld.onBlockChanged`, at the head because the method returns early for most blocks, re-checks every centre a changed block could belong to when the block goes into or out of the tag. Found centres are saved per world, grouped by chunk. A vampire's scan reads the index for nearby chunks and checks each shape again, which drops any shape the tag no longer allows. The check reads only loaded chunks.

**Seeing is a ray.** A held crucifix counts when the vampire can see its holder, the same test mobs use for targets. A shape counts when a ray from the vampire's eyes reaches the shape's centre or lands on one of its own blocks: the column or a matched arm. A crucifix held by a vampire wards no one.

**Faith makes a held crucifix stronger.** The holder's `nightborn:faith` attribute adds a share of the base strength for each point.

## Consequences

- A shape built before the mod was added, or placed by world generation, is not found until one of its blocks changes. A generated structure that needs a working crucifix, such as a hunter camp, has to add it to the index when it is placed.
- Strings are counted in a sphere around the vampire's block, as vanilla counts points of interest.
- A held crucifix on a mob counts as much as one held by a player, so future hunter mobs ward with no new code.
