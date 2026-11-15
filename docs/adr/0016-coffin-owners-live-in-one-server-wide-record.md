# ADR 0016: Coffin owners live in one server-wide record

## Context

A vampire, player or mob, has at most one assigned coffin (COFFIN-4). Sleeping in a new coffin frees the old one, and dying to the sun or a stake, a cure or a hardcore reset frees it too (COFFIN-13). The old coffin and its owner are often far apart. A household vampire can die while its coffin sits in an unloaded chunk, and a player can claim a coffin in another dimension.

Coffins first kept their owner on the coffin's block entity (#94). Freeing a coffin then meant loading its chunk, or leaving a stale owner behind that nothing could clear. Keeping a copy on each owner as well would have given two records that drift apart.

## Decision

One `PersistentState` on the overworld, `nightborn_coffin_owners`, maps each owner's UUID to the dimension and position of their coffin's head, and back. It is the only record of who owns what. The coffin block entity holds no owner.

- Claiming a coffin replaces the owner's previous entry, so a vampire never holds two, and a coffin that already has an owner is never taken.
- Freeing by owner works with no chunk loaded. Freeing by position runs when the coffin's head is removed, from `onStateReplaced`, and only when the new state is not the same coffin.
- When a player's coffin is freed while they are online and their respawn point is that coffin, the respawn point is cleared, so they respawn at world spawn.
- The loader skips a damaged entry with a warning and keeps the rest.

Protection reads the same record: player breaks through Fabric's block break event, explosions through `onExploded`, pistons by refusing a push that would destroy the coffin, and other entities through `World.breakBlock` and the dragon's block removal.

## Consequences

- Freeing never depends on the coffin and its owner being loaded together.
- The client does not know who owns a coffin. A stranger who mines one sees it break and come back, with a message, as with spawn protection.
- A coffin removed without block callbacks, for example by a world editor, leaves its entry until its owner claims another coffin or is freed.
- Accepted drawback: owners whose coffins were set on the block entity before this change lose that link. Household vampires claim a free coffin again on their next check, and players claim theirs by resting in it.
