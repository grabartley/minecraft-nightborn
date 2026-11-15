# ADR 0014: Coffins are GeckoLib models with a texture per material

Supersedes the drawing half of [ADR 0010](0010-coffins-are-vanilla-beds-drawn-with-vanilla-wood.md). A coffin is still a vanilla bed underneath.

## Context

ADR 0010 drew each coffin cell as a plain block model and swung the lid with a hinge angle in code, because a GeckoLib model takes one painted sheet and could not name a wood's own planks. Two things have changed since:

- Every other model and animation in the mod is a GeckoLib model and clip made in Blockbench (ADR 0013). A lid angle in code was the one pose not made that way.
- A sleeper is 16 to 17 pixels across the arms, wider still in a flared sleeve, and 10 pixels across a skirt. The block model's inside was 12 pixels at the shoulders and 6 at the foot, so arms and legs showed through the walls whenever the lid was open. Fitting them meant angled walls, which a block model cannot draw without stair steps.

## Decision

The coffin is one GeckoLib model covering both cells, made in Blockbench through the `bbmodel` MCP, with `open` and `close` clips for the lid and `opened` and `closed` holds. The head cell's block entity plays them from the `open` block state the way furniture does (ADR 0003). The foot cell draws nothing.

The model's bones are grouped by material, named `_wood`, `_velvet` and `_gold`. Every face carries its own UV in 16 by 16 texture space, so the renderer binds a texture per bone:

- wood bones draw the wood's own vanilla planks, so each wood looks like its planks under any resource pack
- velvet bones draw the mod's burgundy velvet
- gold bones draw vanilla's raw gold block

The walls are angled cubes with mitred corners, wide enough at the arms and foot for every sleeper rig. Floors and lid boards are thin bands whose stepped edges sit under the walls and rims, and whose top faces are mapped in model space so the grain runs unbroken along the coffin.

A standing coffin is the same model turned upright about the head cell, as the blockstate turned the old cells. The item draws the same model through `builtin/entity`, with the display poses the block model had.

## Consequences

- A new wood still needs no art: one line in `CoffinWood`, its block model, item model and blockstate, which only name the planks for particles, a loot table, a recipe, the axe tag and a name in each language.
- The lid's motion and timing live in the clip. The lid's 10-tick swing in `LidMotion` still decides when a sleeper is hidden, and a unit test holds the clip lengths to it.
- The coffin is up to 22 pixels wide with its handles, so two coffins placed side by side overlap a little. Its hit box stays inside its two cells.
- Like a vanilla bed or chest, a coffin shows no cracks while it is being broken.
