# ADR 0017: Hunter camps are generated templates and contracts are items

## Context

Hunter camps generate rarely in the overworld with a few hunters and a Guild Master (GUILD-1). The Guild Master sells gear and gives contracts that pay emeralds, silver, gear and hunter points (GUILD-3, GUILD-4, HUNT-2), and a vampire player cannot trade or take contracts there (GUILD-5). A data pack must be able to change the camp, the offers and the contracts (COMPAT-5), and the operator sets the camp rate and the points a contract pays (CFG-5). Trades and contracts must not create or lose items (SAFE-1).

## Decision

**A camp is one generated template on vanilla's jigsaw structure.** The template is built in Java by the `tools/structures` generator, taken from More Doors, and written to `data/nightborn/structure/`. The generator is the only thing that writes that folder, and `./gradlew check` fails when a shipped template differs from what the generator builds. The structure, structure set, template pool and biome tag are plain data. Camps generate in open biomes, plains, sunflower plains, meadows and savannas, because trees placed after structures would grow through the tents. The template is anchored at its centre with a jigsaw marker, so vanilla measures the ground height under the campfire rather than at a corner, and it clears only the two layers above the ground so the land shapes around the camp. The hunters and the Guild Master are entities in the template, placed by vanilla with the blocks, so no code spawns them. Camps use the `nightborn:rare_spread` placement with `rarity: hunter_camps`, read from the `campRarity` setting (ADR 0011).

**The Guild Master is a vanilla merchant.** He uses vanilla's trading screen, so trades move items the way villager trades do. His offers come from the `nightborn:guild_offer` registry and renew each in-game day. The server checks the faction on every interaction and closes the screen if a trading player turns, so a vampire never trades.

**A contract is an item.** The `nightborn:hunter_contract` registry holds what each contract asks for and the loot table it pays. The item stores the contract id and how many are done, so progress follows the item, survives a restart and needs no player data. When a human stakes a vampire, the first open contract in their inventory that the stake meets moves on by one, so one stake never fills two contracts. Clearing a household counts when the staked vampire has no living housemate within reach of its home. A fulfilled contract is handed to a Guild Master, who takes it and pays its loot table and the `contractPoints` setting, so each contract pays once.

## Consequences

- A data pack adds an offer or a contract with one JSON file, and a new kind of camp with a template and structure data.
- A housemate out feeding in an unloaded chunk is not seen, so a household can count as cleared while one member is away.
- Hunters spawn only in camps or from spawn eggs, and never despawn. Each one keeps to the spot it first stood on, so camp residents stay at their camp across saves, and hunters cannot be leashed away. A hunter takes its stake and its look on that first settle, so a camp placed by any means comes out armed.
- A vampire player has no stage, so it counts as a fledgling against a contract that asks for an older vampire.
- Hunter and Guild Master rigs are placeholders until their art lands, at the paths the art issue lists.
