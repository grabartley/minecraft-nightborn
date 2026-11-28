# ADR 0021: Mobs help allies from one shared look around

## Context

The mob brain (ADR 0018) weighed each power for the mob alone and its target. In a group fight that left healers healing only themselves, wards drawn at the caster's feet, and area powers spent on a single foe while a fog could blind the caster's own household. The epic for the brain (#418) asks mobs of one group to fight for each other without a rule per skill, per side or per kind of group.

## Decision

**One rule for allies.** `faction/Sides` decides, from plain facts, who stands together: hunters with hunters and vampires with vampires, mob or player, or anyone on the same team, as long as neither is fighting the other. A hunter's sworn foes count as fighting. A foe is anyone not allied who is on the other side, fights the group, or is fought by it. `faction/Allies` reads those facts from the world. The same rule keeps Grave Fog, Black Mass's fog and Sovereign's Gaze off the caster's allies, for players and mobs alike. A boss, any mob in the `nightborn:unholdable` entity tag, keeps the reach its fog always had, so the Eldest's fight is unchanged.

**A group picture in the situation.** A mob that owns a ready power whose uses ask about its group reads a `Situation.Group`: the allies within 24 blocks with their distance, health, whether they are under attack, being healed, in a fight, or claimed by another mob, and the distances of its foes. Facts read it: `ally_distance`, `ally_health`, `ally_under_attack` and `ally_healing` about one ally, and `allies_in_radius` and `foes_in_radius`, which count within a radius set on the consideration. `allies_in_radius` counts only allies in the fight, so a housemate asleep in its coffin does not make a defender hold back.

**A use about one ally picks its ally.** A use that weighs an `ally_` fact is scored once for each ally not claimed by another mob and keeps its best, so the choice of who to help is the same scoring as the choice of what to cast. Before the tell, the power is checked as ready for that very ally, since readiness among the options is judged without one. The chosen ally rides through the tell, which keeps the tell alive while the mob has no target of its own. An `ally` aim casts at that ally, so a heal, a ward or a blessing lands on it, and a mob that chose an ally to bless never falls back to its own weapon. `allies` and `foes` cast as before and suit powers that spread around the caster. An `allies` use that weighs an ally fact still claims the ally it scored best, so two mobs praying over the same hurt ally do not both spend a prayer on it.

**Claims last the tell.** While a mob winds up to help an ally it claims that ally, and no other mob picks it for help until the tell lands. After that, `ally_healing` keeps a second heal over time off an ally that already has one. Claims are kept per world, dropped once they run out, and cleared when the server stops.

**One look around per half second.** A mob within 8 blocks of where another looked in the last 10 ticks shares that look (`SharedScans`), so a raid of ten scans the world once even though its members think on different ticks. The look keeps who was there; positions, health and who is alive are read fresh, and each mob still sorts what it saw into its own allies and foes.

**Standing by.** A mob with no target of its own, that is not getting away or dodging, still thinks on its own timer, but only when it owns a power that helps an ally, and casts only while an ally near it is fighting. It thinks as if fighting, and its tell is kept while it helps. A mob that takes a target thinks at once, as before.

**Lone fights stay as they were.** Area powers gain a `foes` use that counts heads, so in a group they wait for a crowd. Their old uses gain an `allies_in_radius` that is full only with no ally in the fight near, so a mob alone uses them on one foe exactly as before. Healing uses for an ally need the healer itself to be healthy, and its own healing use outbids them once it is hurt.

## Consequences

- A new heal, ward or buff for allies needs only its data: an `ally` or `allies` use with ally facts.
- The server reports an `ally` use that weighs no ally fact and a head count with no radius.
- The Eldest and the Grand Inquisitor do not use this brain. Of the powers whose code changed, the bosses cast only Grave Fog, through `BossPowers`, and its reach for a boss is unchanged.
- Out of scope: moving to help an ally, such as flanking or holding a line. A mob helps only those already in reach.
