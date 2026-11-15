# ADR 0003: Animated furniture is one GeckoLib block entity driven by a block state

## Context

Every action in Nightborn plays an animation. Lighting a candelabra, putting out a chandelier and drawing a drape are actions, so each needs a motion, not a model swap. Vanilla block models cannot animate. The furniture also has to stay cheap: a hall can hold dozens of candelabras.

## Decision

Candelabras, chandeliers and drapes share one block entity type, `FurnitureBlockEntity`, rendered by GeckoLib. The block decides everything on the server and stores it in one boolean block state property: `lit` for lights and `open` for drapes. The block entity holds no saved data of its own.

On the client the block entity watches that property. The first time it sees the block it holds the matching pose, so loading a chunk never replays an animation. When the property changes it plays a transition clip and then holds the new pose. Every piece uses the same four clips: `activate`, `active`, `deactivate` and `inactive`. Each block declares its geometry and its texture is named after the block, so a new piece needs a model and its clips, not new rendering code.

Furniture that never animates, such as the chair, the table and the building blocks, stays a vanilla block model.

## Consequences

- The server owns the state and syncs it as a normal block update, so every player sees the same light and the same drape.
- GeckoLib becomes a required dependency, as it is for the family's other mods with animated blocks.
- A piece of furniture with more than two states would need a new property and clip names, not a change to this pattern.
