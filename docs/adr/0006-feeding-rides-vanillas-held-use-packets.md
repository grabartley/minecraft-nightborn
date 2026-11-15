# ADR 0006: Feeding rides vanilla's held-use packets

## Context

Feeding is sneak and hold use on a creature, letting go stops it (FEED-1 to FEED-8). The server must check reach, the target and the faction, and a modified client is assumed (section 7 of the PRD).

## Decision

There is no feeding packet. While the use key is held on an entity, the vanilla client sends an entity-use packet every 4 ticks, and the server already checks its reach. A `UseEntityCallback` turns each one into "still holding" on a server-side `FeedingSession`. When none arrives for 8 ticks, the vampire has let go.

The session drinks once a second. It measures the health the victim actually lost after the bite damage, and that measured amount is the only blood the vampire or a Blood Bottle can get. Bite damage ignores armour, effects and the hurt cooldown, so the measure is the truth and two vampires on one victim can never take more than it has (CS-1, CS-4).

## Consequences

- A modified client gains nothing by sending extra packets: the session drinks on the server's clock, not on packet count.
- Two vampires feeding on one victim, a vampire draining a victim another player kills, and bottling all come down to the victim's real health, so no blood or item is created.
- Feeding uses vanilla's existing reach check. A vampire who looks away or walks off stops feeding within half a second.
