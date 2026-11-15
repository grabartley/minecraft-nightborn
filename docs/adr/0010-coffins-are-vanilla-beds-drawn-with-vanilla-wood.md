# ADR 0010: Coffins are vanilla beds drawn with vanilla wood

## Context

A coffin is a vampire's bed. It is two cells long, it is a respawn point, two players cannot share one, and it must break as one piece with one drop. Vanilla's bed already does all of this, and the game's own sleep, wake and respawn code checks for a bed by its class.

Coffins also come in several woods, and the wood should look like the planks a player already knows, including under a resource pack. ADR 0003 draws animated furniture with GeckoLib, but a GeckoLib model takes one painted texture sheet and cannot name a vanilla texture, so a GeckoLib coffin would carry painted copies of each wood.

## Decision

`CoffinBlock` extends vanilla's `BedBlock`. Pairing the two cells, placement, breaking with one drop, occupancy, the respawn point and the wake-up spot all come from vanilla. Mixins change only what differs:

- A vampire may rest only in a coffin and a human only in a bed.
- A coffin is for the day: a vampire is refused at night and rises at dusk.
- A sleeper lies low enough for the lid to close over them, and a client does not draw a sleeper under a shut lid.
- Vampires are left out of the bed share. A server tick counts the coffin share and passes the day to dusk.

Each cell is a plain block model, exported from Blockbench, that names the wood's own planks, the mod's burgundy velvet and vanilla's raw gold block for the fittings. The six woods are child models of one template. The lid is a separate block model drawn by a block entity on the head cell, which swings it on a hinge from the `open` block state the way a chest swings its lid. The block entity holds no saved data.

## Consequences

- Every coffin looks like its wood under any resource pack. A new wood is one line in `CoffinWood`, its three child models, item model and blockstate, a loot table, a recipe, the axe tag and a name in each language. Its fire behaviour is read from its planks.
- Coffins do not use GeckoLib, so the lid's motion is a hinge angle in code rather than an animation clip. ADR 0003 still applies to the candelabra, chandelier and drapes.
- Anything in vanilla or another mod that treats every `BedBlock` as a bed treats a coffin as one too. Villagers do not, because their home point lists vanilla's beds by name.
