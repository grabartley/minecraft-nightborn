# ADR 0008: Vampire rules apply to any vampire entity

## Context

The sun, the downed state and the weakened state apply to vampire players and vampire mobs alike (CORE-7, CORE-8, SUN-4). Vampire mobs come later, and the PRD forbids per-mob code in shared features.

## Decision

`Vampires.isVampire` is true for a player whose faction is vampire, and for any entity whose type is in the `nightborn:vampires` tag. The tag starts empty. One mixin on `LivingEntity.tick` runs `VampireTicker` for any vampire on the server, which drives the downed timer, the sun, the core boosts, the weakened state and what others can see. The downed rules hook Fabric's damage and death events for every living entity, and a vampire may die only to damage in the `nightborn:kills_vampires` damage type tag or to a stake blow: a direct melee hit with an item in the `nightborn:stakes` item tag.

## Consequences

- A vampire mob added later gets the sun, the downed and escaping timers, the damage rules and the weakened state by joining the tag, with no new code in those features.
- What others see and what a downed vampire is stopped from doing are player-only for now: the glowing eyes, the weakened sign, the collapsed pose and the blocks on attacking and using items. Vampire mobs get their own forms of these with their models and AI, in the issue that adds them.
- A stake blow on a downed vampire finishes it with `nightborn:stake` damage, which is in `nightborn:kills_vampires`. A stake blow that would down a vampire kills it instead. Stake bolts join by adding their damage type to `nightborn:kills_vampires` and `nightborn:stakes`, and other stake items join the item tag.
- A human costs one attachment lookup per tick and nothing else (PERF-1).
