# ADR 0005: Blood replaces the hunger tick for vampires

## Context

A vampire has a blood bar in place of hunger (FEED-1). It drains over time and faster with effort, hurts when empty and heals slowly while above half (FEED-2), and food restores none of it (FEED-3). Blood must never be created except by feeding, turning and respawning (SAFE-2).

Storing blood in vanilla's hunger manager looks cheap, but many vanilla paths add to it: food, the Saturation effect, and peaceful difficulty refilling it. Each would need its own block, and a missed one creates blood.

## Decision

Blood is its own attachment, `nightborn:blood`, following ADR 0002: saved with the player, synced only to them, not copied on death, so it refills on respawn. A full bar stores nothing, so a human and a fed vampire cost nothing.

For a vampire, a mixin cancels `HungerManager.update` and runs `BloodTicker` in its place, so blood ticks exactly where hunger would. The rules live in the pure `BloodMetabolism`:

- Vanilla's exhaustion still collects from sprinting, jumping and fighting, and every 4 exhaustion costs one blood, as it costs one hunger.
- A resting drain adds exhaustion at the rate the `restingDrainSeconds` setting gives. Nothing drains in peaceful, as with hunger.
- While blood is above half, a hurt vampire heals one health every 200 ticks. Healing adds no exhaustion, so it costs no blood. Empty blood starves the vampire through `Starvation`, which weakens and then hurts it on the timers its settings give (FEED-14), and never in peaceful.

The vampire's hunger bar is pinned full, so vanilla code that reads it, such as sprinting and eating, behaves the same for every vampire. Vampires may always eat, for no gain.

## Consequences

- No vanilla path writes blood, so SAFE-2 holds by construction rather than by blocking each path.
- The blood bar is drawn by the client in place of the hunger bar, from the synced attachment.
- Blood changes sync only when the whole number changes. The fraction lives in vanilla's unsynced exhaustion field.
