# ADR 0002: Player state lives in Fabric attachments

## Context

Every player is a vampire or a human, and later features add more state per player: bite sickness, blood, the downed state, skills and bloodlines. The persistence grid in the PRD says what each piece does on death, disconnect and world reload. The PRD also says this state is synced only to its own player, apart from the few things other players can see, such as glowing eyes.

## Decision

Each piece of per-player state is its own Fabric data attachment on the player, starting with `nightborn:faction`.

- An attachment is saved inside the player's own data, so it survives disconnect and world reload with no extra save code.
- Its death rule is set on the attachment: `copyOnDeath` for state the grid keeps through death, such as faction, and no copy for state the grid clears or refills, such as the downed state and blood.
- Private state syncs with `targetOnly`, so no other client is ever sent it.
- State other players can see gets its own small attachment synced to every player tracking that player, added by the feature that makes it visible. Private and public state never share an attachment, so a public sync cannot leak a private value.
- A player at the default, a human, stores no data at all, and nothing in this state runs per tick.

This needs attachment sync, so the mod requires Fabric API 0.116.12 for 1.21.1, the same version Total Storage uses.

## Consequences

- A new piece of player state is one attachment and a death rule, not new save or network code.
- The death rule is tested through the real respawn path, because Fabric copies attachments in its respawn event, not in `ServerPlayerEntity.copyFrom`.
- Players on an older Fabric API must update it to run the mod.
