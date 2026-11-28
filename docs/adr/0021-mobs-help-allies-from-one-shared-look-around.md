# ADR 0021: Mobs help allies from one shared look around

## Context

The mob brain (ADR 0018) weighed each power for the mob alone and its target. In a group fight that left healers healing only themselves, wards drawn at the caster's feet, and area powers spent on a single foe while a fog could blind the caster's own household. The epic for the brain (#418) asks mobs of one group to fight for each other without a rule per skill, per side or per kind of group.

## Decision

**One rule for allies.** `faction/Sides` decides, from plain facts, who stands together: hunters with hunters and vampires with vampires, mob or player, or anyone on the same team, as long as neither is fighting the other. A hunter's sworn foes count as fighting. A foe is anyone not allied who is on the other side, fights the group, or is fought by it. `faction/Allies` reads those facts from the world. The same rule keeps Grave Fog, Black Mass's fog and Sovereign's Gaze off the caster's allies, for players and mobs alike.

**A group picture in the situation.** A mob that owns a ready power whose uses ask about its group reads a `Situation.Group`: the allies within 24 blocks with their distance, health, whether they are under attack, in a fight, or claimed by another mob, and the distances of its foes. Facts read it: `ally_distance`, `ally_health` and `ally_under_attack` about one ally, and `allies_in_radius` and `foes_in_radius`, which count within a radius set on the consideration.

**A use about one ally picks its ally.** A use that weighs an `ally_` fact is scored once for each ally not claimed by another mob and keeps its best, so the choice of who to help is the same scoring as the choice of what to cast. The chosen ally rides through the tell. An `ally` aim casts at that ally, so a heal, a ward or a blessing lands on it. `allies` and `foes` cast as before and suit powers that spread around the caster.

**Claims last the tell.** While a mob winds up to help an ally it claims that ally, and no other mob picks it for help until the tell lands. Claims are kept per world and cleared when the server stops.

**One look around per tick.** Mobs that think on the same tick within 8 blocks of a mob that already looked share that look (`SharedScans`), so a raid of ten scans the world once. Each mob still sorts what it saw into its own allies and foes.

**Standing by.** A mob with no target of its own, that is not getting away or dodging, still thinks on its own timer, but only when it owns a power that helps an ally, and casts only while an ally near it is fighting. It thinks as if fighting, and its tell is kept while it helps. A mob that takes a target thinks at once, as before.

**Lone fights stay as they were.** Area powers gain a `foes` use that counts heads, so in a group they wait for a crowd. Their old uses gain an `allies_in_radius` that is full only with no ally near, so a mob alone uses them on one foe exactly as before. Healing uses for an ally need the healer itself to be healthy, and its own healing use outbids them once it is hurt.

## Consequences

- A new heal, ward or buff for allies needs only its data: an `ally` or `allies` use with ally facts.
- The server reports an `ally` use that weighs no ally fact and a head count with no radius.
- The Eldest and the Grand Inquisitor do not use this brain and are unchanged.
- Out of scope: moving to help an ally, such as flanking or holding a line. A mob helps only those already in reach.
