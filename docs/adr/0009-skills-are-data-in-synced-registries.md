# ADR 0009: Skills are data in synced registries

## Context

The vampire tree has a core and three bloodlines, and the hunter tree comes later. Each bloodline issue adds its own skills. The PRD forbids per-bloodline code in shared features, and wants skills, costs and values to be data a pack can change. The client needs every skill's cost, place and requirements to draw the skill screen and the power bar, while the server alone decides every purchase (PERM-2).

## Decision

Skills and branches are two dynamic registries, `nightborn:skill` and `nightborn:skill_branch`, registered with Fabric's `DynamicRegistries.registerSynced`. Their files live in `data/<namespace>/nightborn/skill/` and `data/<namespace>/nightborn/skill_branch/`. A branch names its tree and whether it is a line, a bloodline in the vampire tree or a path in the hunter tree, of which a player chooses two. A skill names its branch, cost, required skills, icon sprite, place on the screen, attribute modifiers and, for an active skill, a power with its blood cost, cooldown, effects and sound.

A player's points and bought skills are one attachment, `nightborn:skills`, with a separate record per tree. It keeps each purchase and what was paid, in order, and the bloodlines chosen. Power cooldowns are a second attachment. Both are saved, kept through death and synced only to their player (ADR 0002).

The rules live in pure helpers, `SkillPurchase` and `PowerUse`, that both sides call. The client uses them only to grey out and explain a node. The server runs them again on every purchase and power use.

## Consequences

- A bloodline issue adds a skill by adding a file, a name, a description and an icon. A skill whose power needs new behaviour still needs code for that behaviour, keyed by the skill's data.
- A data pack can retune or add skills, but registry changes need a world restart, not `/reload`.
- Changing a skill's cost does not change what players already paid, so a refund always returns exactly what was spent.
- A skill removed from the data stays in a player's record until an operator resets that tree, and gives nothing while it is missing: its attribute modifiers are cleared the next time the player's skills are applied.
- Skill data is checked when the server starts. A skill in an unknown branch, a skill that needs itself directly or through a loop, a requirement that does not exist or sits in another tree, a skill whose row is not one more than the deepest skill it needs in its branch, a skill that sits on the lane of a long line, two lines that touch or cross when they share neither a parent nor a child, and two skills in one place on the screen are each logged as an error.
