# ADR 0011: Vampire structures share one structure type

## Context

The PRD has four kinds of vampire structure: castles, crypts, ruined manors and underground lairs (section 5.13). Each must be rare, fit the land, hold several vampires and loot, and sit in fog that thickens towards its heart (HOME-1 to HOME-4, HOME-8). A data pack must be able to change layouts, loot, rates and biomes (HOME-12, COMPAT-5). Placement must not load extra chunks (PERF-6), fog must cost nothing away from a structure (PERF-4), and fog must never hide a threat within 8 blocks (ACC-4). The operator sets structure rates (CFG-5).

More Doors places its vaults with a custom jigsaw structure type and a custom random spread placement that reads its config each time it is asked (More Doors ADR 0065). Nightborn follows the same shape.

## Decision

**One structure type, `nightborn:vampire_structure`, serves every kind.** It takes the same fields as a vanilla jigsaw structure plus a `household`: the entity type that lives there and how many. Its `size` must be at least one, because vanilla's jigsaw generator places no pieces at all at a size of zero. Biomes, terrain adaptation, layouts (template pools), processors and loot all stay in data, so a new kind is data only. No code names a kind.

**The first piece placed is the heart.** Residents settle on open floor inside it, and fog is thickest at its centre. A layout puts its throne room, blood pool or crypt chamber in its start pool.

**Residents settle once, from the chunk that holds the heart's centre.** `postPlace` runs once per chunk, so only that chunk spawns the household, and only on floor inside both the heart and that chunk. Residents are persistent so they stay home. `HouseholdSpots` keeps them apart while there is room.

**Rarity is read as chunks generate.** The `nightborn:rare_spread` placement type has the fields of vanilla's random spread plus a `rarity` that names the setting it reads: Vampire structure sets name `vampire_structures`, which reads `structureRarity`, and hunter camps name `hunter_camps`, which reads `campRarity`. Its spacing and separation are the data's numbers scaled by that setting, from 25% to 400%. As in vanilla, the data's spacing must be larger than its separation. Changing the setting only affects chunks not yet generated.

**A structure can ask for good ground.** An optional `footing` samples the terrain across the whole footprint every `spacing` blocks, 8 by default, before the structure is placed. It refuses the spot if any sample is under water, when `dry` is set, which it is by default, or if the ground rises more than `max_rise` blocks across it. A refused spot gets no structure, the same as a biome that does not match.

**Reach is capped by the codec.** `max_distance_from_center` plus the padding terrain adaptation adds must stay within 128 blocks, the 8 chunks a structure start references. A layout that reaches further fails to load rather than loading extra chunks.

**The server finds fog and the client draws it.** Fog covers the structure start's box, which already includes vanilla's 12 block terrain adaptation padding, and a 24 block margin around that. It is full at the heart and fades with distance from it, and it fades to nothing across the margin, so it never starts abruptly. Once a second the server looks at the structure references already held by loaded chunks within two chunks of each player, which always covers the margin. It reads a start only from a loaded chunk, so it never loads one. When the zone a player is deepest in changes, it sends the heart and box. A player with no zone costs one map lookup per nearby chunk each second, and the client does nothing. The client eases towards the target and pulls vanilla's fog in towards 20 blocks at the heart. It never pushes back a nearer vanilla fog, such as blindness or water, so a threat 8 blocks away is at most 40% faded.

**Ambience rides on the fog zone.** A structure's optional `ambience` takes vanilla's biome effect fields: a looping `ambient_sound`, one-shot `additions_sound` with a `tick_chance`, and `music` with `min_delay`, `max_delay` and `replace_current_music` (true unless set). A kind's soundscape is data like everything else. Sounds live in the client's resource packs, so the server cannot check the ids, and a wrong one only shows up as an unknown sound warning in the client log when it would play. The server sends the ambience with the heart and box, so it costs nothing more. The client fades the loop in and out at a fortieth of full volume a tick and holds it at the player's closeness to the heart, plays one-shots only inside the zone and louder towards the heart, and gives the structure's music in place of the game's. Outside any zone, gothic music takes the place of the game's own music at night, letting a playing track finish. Menu, credits, underwater, Ender Dragon and End music are never replaced.

## Consequences

- Castles, crypts, manors and lairs (issues 70 and 71) are data files on this type. Household routines (issue 69) build on the residents settled here.
- A crypt is a church on the surface hung from the crypt below by a vertical jigsaw. The crypt is the start piece, so it is the heart. Any buried rigid piece makes vanilla shape the ground around it, which cut into the churchyard, so crypts use no terrain adaptation. Instead the crypt piece is a level bank of earth under the whole churchyard, faced with a rubble retaining wall, and its footing keeps it to dry ground that rises 4 blocks or less. Its placement has a `near` rule, `exclusion_zone` turned around: a crypt only starts within 8 chunks of a chunk where the village grid can start a village, and its exclusion zone keeps it more than 6 chunks away, so the church stands just beyond the edge of a village. The rule asks the village placement, not the biome, so a crypt can rarely stand by a grid spot where no village grew; crypts keep to the same plains villages favour, which keeps that rare.
- A heart whose start chunk is not loaded casts no fog until it loads. With a view distance of two or more this does not happen inside a structure.
- Fog changes distance only, not colour, so it reads as mist by day and darkness by night.
- Vanilla's structure locator caches misses per chunk until restart, so `/locate` can stay wrong for searched chunks after the rarity changes, as More Doors found.
- Changing the rarity in a world that is already explored moves the grid. A new start in fresh land can reach into chunks that already generated and be cut off there, and two structures can sit closer than their separation where new land meets old.
- The test server turns structure generation off, so natural placement is checked by hand. Gametests cover settling, the codec cap, the rarity scaling, the fog lookup, the ambience the zone carries and the loot table.
