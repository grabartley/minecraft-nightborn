# ADR 0015: Placed blood follows the gore option on each client

## Context

Blood splatter is a block a player can place on any face (DECOR-3). Gore is a client option with three levels: Full, Reduced and Off (GORE-3). Two players can stand in the same room with different gore levels, so the server cannot decide how a splatter looks. The block still has to exist for everyone, so that it drops, breaks and blocks placement the same way on every client.

## Decision

The splatter is a normal block on the server. Each client changes only how it draws the block:

- A model loading plugin wraps every baked model of the splatter's block states. The item model is left alone, so the item looks the same at every gore level.
- When the client draws a splatter, the wrapper reads the client's gore level. Full draws it as made. Reduced multiplies its colours toward a dark brown, so it reads as old, dried blood. Off draws nothing.
- Block models are baked into chunk meshes, so a change of gore level only shows after those meshes are built again. Saving the settings screen rebuilds them, but only when the new level draws splatter differently from the old one.

Blood pools, fountains and full chalices are furnishings, not gore, and look the same at every level.

## Consequences

- Hidden splatter still has its outline and can be broken, so a player with gore off can still find and remove it. Breaking it still throws the block's small red break particles, like any block's.
- A rebuild redraws every loaded chunk, which costs a moment, the same as changing vanilla's graphics settings.
- A future block that is gore can reuse the wrapper by adding its block to the plugin.
