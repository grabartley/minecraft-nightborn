# ADR 0020: Lost limbs are decided once on the server and kept in one attachment

## Context

Dismemberment lets a strong blow cut an arm or a leg off a vampire mob, a hunter mob or a player (GORE-5 to GORE-9). Later work builds on it: wounds and bone stubs, the cost of a lost limb, bleeding, beheading, regrowth, silver limbs, how maimed mobs behave and boss fights. Each of these needs the same answer to "what is this body missing?", and every player near the fight has to see the same body. Vanilla melee tells the server only which creature was hit, not where. Gore is already split this way: the server decides what happened and each client decides how much blood to draw (GORE-4, ADR 0015).

## Decision

- **One rule on the server.** `SeverRules` decides whether a blow cuts anything. It is a pure function of the settings, the blow and the victim, so it is tested without a world. `Dismemberment` only gathers those facts after damage is final and writes the result. Clients never decide a cut.
- **One reading of the blow.** Gore and dismemberment each listen for damage and read the damage source through the same `BlowReading.read`, so they never disagree about whether a hit was a stake, a bite, silver or a severing blade. Neither calls the other.
- **Where the blow lands.** For a player's melee blow the server traces the attacker's line of sight to the victim's box. For a shot it uses the point of impact. A mob's melee blow picks a part by weight, since a mob does not aim. A blast, or a blow with no direction, picks a limb at random. Turning a point on the box into a part is `HitLocation`, which reads the body zones.
- **Data, not code.** Body zones and mob weights are a data registry, `nightborn:body_zones`, checked when it loads. Severing weapons are the item tag `nightborn:severing_weapons`. A power or boss attack severs when its damage type is in the damage type tag `nightborn:severing`. Blows that never sever are the damage type tag `nightborn:never_severs` and the entity type tag `nightborn:never_severing_shots`. Who can lose a part, and on what terms, is the data registry `nightborn:body_rule`, one entry per entity type, checked when it loads: whether it can be maimed, whether only while staggered, a scale on the chance to sever, and how its own blows cut (with a severing weapon, with every melee blow, or only with marked attacks). A mob with no entry keeps its body and cuts only with a severing weapon. No class names a bloodline, mob or rig.
- **One saved state.** The `nightborn:body` attachment lists the missing parts, each with its condition (missing or replaced), when it was lost and room for the bleed and regrowth timers later work fills in. It is saved and synced to every watcher. A player's death clears it, except for replaced limbs. Nothing else keeps a record of a body's limbs.
- **Drawing reads the state.** Each client hides the bone named after a lost part on every rig, and the matching parts of the plain player model, its armour and the item held in that hand. The client gore setting changes only how wounds are drawn once there are any. It never changes what a body has lost.

## Consequences

- Every later issue in the epic reads or writes the one attachment instead of keeping its own copy.
- A data pack can change where the zones fall, which mobs can be maimed and which weapons and attacks cut, with no code.
- The same tick check sits in the state itself: a body cut within the last 10 seconds cannot be cut again, so two blows in one tick take one part at most and every client sees the same result.
- Rig bones must keep the part names `head`, `right_arm`, `left_arm`, `right_leg` and `left_leg`. A unit test checks every rig that can lose a part.
