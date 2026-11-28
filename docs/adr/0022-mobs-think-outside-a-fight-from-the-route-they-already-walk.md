# ADR 0022: Mobs think outside a fight from the route they already walk

## Context

The mob brain (ADR 0018) thought only in a fight, and a mob with no target thought only to help an ally (ADR 0021). Bat form was used only to chase a target, and a vampire walking home at dawn or wandering into a ravine had no way to use the powers it owns. The epic for the brain (#418) asks mobs to use powers to travel, to get ready, to hunt and to recover, without a new path search and without making idle mobs cost more.

## Decision

**Goals report, the brain decides.** The goals that move a mob stay as they are. Walking back to a home or camp (`WalkHomeGoal`, the vanilla walk target goal reporting itself), wandering the wild and flying in bat form with no target report `travel`; falling back and seeking shade report `retreat`; sleeping, or nothing at all, is `rest`. A mob with no target that cannot help an ally weighs the uses for the intent it is in.

**Thinking alone is slow and watched.** Outside a fight a mob thinks its intent's `think_scale` times less often than in one, and not at all while no player is within 64 blocks. A mob that owns a power that helps an ally keeps its fighting pace, so the help from ADR 0021 is never slower. A mob thinks only when a power is ready, as before, so an idle mob with nothing to cast builds nothing.

**Place facts come from the navigator's path.** `gap_ahead` is whether the path the mob is already walking reaches where it was asked to go, which catches a gap, a drop, water or a blocked way in one fact. A path that falls short of a goal beyond the mob's follow range says nothing, since the search was cut off rather than blocked. `detour` is that walk over the straight line, and `trip_length` the straight line. `home_distance` reads the mob's leash, which is its home or camp. `until_dawn`, `light` and `room_to_fly` are read where the mob stands. Each is read only when a ready power asks for it. A hunt is read from the target: `target_aware` is 0 for prey that has not turned on the mob, struck it or been struck by it, so stealth and charm uses wait for an unaware target without a separate hunt intent changing how every fight starts.

**A form is held, and flown to where the mob was going.** A power's `mob` block can set `hold_ticks`. When a power with a hold is cast, the mob notes the time and where it was going (`FormHold`), since the bat goal stopping the navigator would otherwise lose it. A mob with no foe walking back to its home notes the home itself, not the next hop its walk picked. In bat or winged form a mob chases its target; with none it flies on to that spot. It keeps the form for the hold, and changes back only over ground, so it never lands in the gap it set out to cross. `BatCourse` is the pure decision.

**Free powers ignore an intent's reserve.** An intent's blood reserve protects blood. A power that costs none answers only to the mob's own floor, so hunters, who have no blood, can pray and bind wounds while travelling or at rest.

## Consequences

- A new travel, boost or recovery use needs only its data.
- Summons already end on their own time, so nothing new drops them.
- Mist Form keeps only its fighting uses. Using it to pass a blocked way would need a path through what mist passes, which is a new path search.
- No beast form exists to give uses to.
- The Eldest and the Grand Inquisitor do not use this brain and are unchanged.
