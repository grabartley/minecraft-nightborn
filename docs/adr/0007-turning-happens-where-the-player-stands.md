# ADR 0007: Turning happens where the player stands

## Context

When bite sickness runs out, the player turns, keeps their inventory and starts with full blood (TURN-5). The PRD's story of turning said the player dies and respawns as a vampire.

A real death would end a hardcore world's game, drop and restore the inventory through death code that was never meant to keep it, and move the player to their spawn point.

## Decision

The player turns where they stand. Their faction changes, the sickness ends, their blood is full, and the moment is marked with darkness, a sound and a message. No death happens.

## Consequences

- Turning never risks the inventory, and works the same in hardcore worlds.
- The PRD's description of turning now says the player collapses and wakes as a vampire, matching TURN-5.
