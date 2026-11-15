# ADR 0008: Vampire rules apply to any vampire entity

## Context

The sun, the downed state and the weakened state apply to vampire players and vampire mobs alike (CORE-7, CORE-8, SUN-4). Vampire mobs come later, and the PRD forbids per-mob code in shared features.

## Decision

`Vampires.isVampire` is true for a player whose faction is vampire, and for any entity whose type is in the `nightborn:vampires` tag. The tag starts empty. One mixin on `LivingEntity.tick` runs `VampireTicker` for any vampire on the server, which drives the downed timer, the sun, the core boosts, the weakened state and what others can see. The downed rules hook Fabric's damage and death events for every living entity, and the damage that may kill a vampire is the `nightborn:kills_vampires` damage type tag.

## Consequences

- A vampire mob added later gets the sun, the downed and escaping timers, the damage rules and the weakened state by joining the tag, with no new code in those features.
- What others see and what a downed vampire is stopped from doing are player-only for now: the glowing eyes, the weakened sign, the collapsed pose and the blocks on attacking and using items. Vampire mobs get their own forms of these with their models and AI, in the issue that adds them.
- Stakes join the kill list by adding their damage type to `nightborn:kills_vampires`.
- A human costs one attachment lookup per tick and nothing else (PERF-1).
