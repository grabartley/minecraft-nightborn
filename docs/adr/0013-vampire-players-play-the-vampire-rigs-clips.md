# ADR 0013: Vampire players play the vampire rig's clips

## Context

Levitation (CORE-11) and vomiting (FEED-3) need the same poses on a vampire player as on a vampire mob: a float with the feet together, a lean that grows with speed, a landing, and doubling over to retch. Vampire mobs are GeckoLib entities that play clips from `animations/entity/vampire.animation.json`. Players are drawn by vanilla's player model, which has no clip player. Hand-coding the player poses in Java would make a second copy of every pose that could drift from the mob's.

## Decision

The vampire rig's animation file is the only place a vampire pose is defined. The client reads the same file for players and plays it on the vanilla player model:

- `ClipFile` reads the exported Bedrock animation, and `RigGeometry` reads the bone pivots, parents and the `mouth` locator from the male rig's geometry.
- `RigSpace` turns a clip's values into vanilla model angles and pivots, following the conventions GeckoLib uses for the same file, so a clip looks the same on a player as on a mob. The root bone becomes a transform in `PlayerEntityRenderer.setupTransforms`, so armour, held items and the cape tilt with the body.
- The body bone pivots at the hips, so the head and arms ride a forward bend, as on the mob rig.
- Clip names come from `VampirePose`, which both the mob and the player read.
- The vomit bursts land on the heaves of the vomit clip, found as the body's strongest forward bends, because the Bedrock export does not carry particle keyframes.

## Consequences

- A clip retimed or repainted in the Blockbench project changes players and mobs together, with no code change.
- A clip may use only plain numbers. An expression is refused when the file loads.
- The first-person arm is left to vanilla, so a held item stays where players expect it.
- When the male and female rigs gain different proportions (#11), players keep following the male rig's pivots until a player rig of their own is needed.
