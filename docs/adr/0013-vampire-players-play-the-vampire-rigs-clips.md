# ADR 0013: Vampire players play the vampire rig's clips

## Context

Levitation (CORE-11) and vomiting (FEED-3) need the same poses on a vampire player as on a vampire mob: a float with the feet together, a lean that grows with speed, a landing, and doubling over to retch. Vampire mobs are GeckoLib entities that play clips from `animations/entity/vampire.animation.json`. Players are drawn by vanilla's player model, which has no clip player. Copying the clips onto the vanilla model in code would mean a second, hand-written version of every pose that could drift from the mob's, and it did not hold together in game.

## Decision

Every model and animation is a GeckoLib model and clip made in Blockbench through the `bbmodel` MCP. Vampire players are drawn by GeckoLib while a vampire clip plays:

- Two player rigs, wide-armed and slim-armed, are built in Blockbench on the vanilla 64x64 skin layout and textured with the player's own skin. They use the vampire rig's bone names and pivots, so the vampire animation file drives them unchanged.
- `PlayerEntityRenderer` hands a vampire player to a GeckoLib `GeoReplacedEntityRenderer` while it levitates, lands or vomits, and draws it as vanilla does otherwise.
- The rigs carry GeckoLib's armour anchor bones and two hand bones, so armour, trims, held items and the glowing eyes follow the clip.
- Clip names come from `VampirePose`, which both the mob and the player read. Clip lengths and the heaves of the vomit clip are read from GeckoLib's loaded animation, because the Bedrock export does not carry particle keyframes. The mouth is found from the head bone where GeckoLib drew it.

## Consequences

- A clip retimed or repainted in the Blockbench project changes players and mobs together, with no code change.
- The first-person arm is left to vanilla, so a held item stays where players expect it.
- Vanilla's own poses, such as sneaking, swimming or drawing a bow, stay vanilla. A vampire version of one is a new clip in Blockbench, not code.
- Capes, elytra, shoulder parrots and stuck arrows are not drawn while a vampire clip plays.
