# Skill Lists

This is the locked list of every skill in the vampire tree and the hunter tree. The rules they follow are in `docs/prd.md` (SKILL-1 to SKILL-6, LINE-1 to LINE-8, AGE-5, HUNT-3 to HUNT-6, RANK-3). This document says what each skill is.

Every number here is the default, and lives in data or a server setting. Nobody building a skill chooses a name, a number or an effect of their own. If something is missing, ask.

## How To Read The Tables

- **Kind:** a passive is always on. A power sits on the power bar and is used with a key.
- **Ranks and cost:** a passive with ranks lists the points each rank costs, in order. A power has one cost.
- **Needs:** the skill that must be owned first.
- **Gate:** the stage a vampire, or the rank a hunter, must have reached.
- **Blood:** what a vampire power costs, in blood points. Hunter powers cost no blood.
- **Cooldown:** in seconds.
- **Radius:** every area is given as a radius in blocks from its centre.
- **Staggered:** a staggered target cannot move, attack or use a power for the time given, and can still be hit. A boss cannot be staggered.
- **Pinned:** a pinned target cannot move or use a power that moves it. It can still attack and use its other powers. A boss cannot be pinned.
- After a stagger or a pin ends, the same target cannot be staggered or pinned again for 3 seconds.
- **Weakness I:** the vanilla effect, not a vampire's own weakening.
- **Healing prayers:** Mend, Sanctuary and Judgement.

## Rules Every Skill Follows

- A vampire or a hunter should feel stronger with every point. A rank that cannot be felt in play is wrong.
- Every power has a pose and a sound when it is used, and a cue on screen. A power used on a player gives that player a cue too.
- Powers are used again and again, so each sound is satisfying to hear many times and never loud or grating. A power's sound, its volume and its pitch are data, and its pitch shifts a little each time.
- Every power has a counter on the other side. No skill removes a vampire weakness or lets a vampire walk in the sun.
- A boss cannot be pinned, pulled, mesmerised, commanded, downed by a skill or marked through its arena wall. Skills that would do so deal their damage and nothing more.
- A power that moves the user lands only on solid floor with room to stand, never in fluid or fire and never through a wall, as Vanish does today.
- No resistance or reduction passes 80% in all, whatever is learned and worn together. Bite resistance and mesmerise resistance stop at 0.8.
- A power hurts, moves, holds or curses another player only when PvP is on (FACT-2). Healing and help given to allies always work. Where a row says human or vampire, it means mobs always and players as PvP allows.
- Damage that a row says burns as the sun does is not the sun. It stops at a vampire's last health, as any other blow does, and never finishes a downed vampire.
- Skills are data. No skill has code written for one bloodline or one path.
- On the skill screen each branch is a band read left to right. A skill sits one column right of the deepest skill it needs, and its line never crosses or touches a line it is not joined to. Where each skill sits is data, and the server checks these rules when it starts (LINE-7).

## Vampire Tree

A vampire has the core and picks two of the three bloodlines. Each bloodline ends in a capstone: an ultimate power that needs ancient, then one last passive.

### Core

| Skill | Kind | Ranks and cost | Needs | Gate | Blood | Cooldown | What it does |
|---|---|---|---|---|---|---|---|
| Thick Blood | Passive | 3 ranks: 1, 2, 3 | None | None | | | 10% more health each rank |
| Grave Strength | Passive | 3 ranks: 2, 2, 3 | Thick Blood | None | | | 1 more attack damage each rank |
| Vanish | Power | 2 | Thick Blood | None | 1 | 5 | Gone in a puff of smoke, back a moment later 4 blocks away the way the player is moving |
| Blood Rush | Power | 2 | Thick Blood | None | 3 | 30 | Speed II and Strength I for 8 seconds |
| Night Eyes | Passive | 3 ranks: 1, 1, 2 | Thick Blood | None | | | Rank 1 sees in the dark at any hour and in any place, not only at night. Rank 2 halves the time blindness and darkness last on the vampire. Rank 3 sees through Grave Fog |
| Blood Healing | Power | 2 | Thick Blood | None | 2 | 20 | Heals 6 health over 3 seconds |
| Iron Grip | Passive | 3 ranks: 1, 2, 2 | Grave Strength | None |  |  | Each rank lowers a victim's chance to break free of a bite by 10% of what it is, feeds 10% faster and breaks a snare or a net 20% sooner. It is applied after the victim's bite resistance |
| Deep Veins | Passive | 3 ranks: 2, 3, 3 | Blood Healing | Adult | | | 2 more blood points on the bar each rank |
| Unbroken | Passive | 3 | Iron Grip | Elder | | | Downed for 25% less time, and rises with 30% of health |
| Sanguine Mastery | Passive | 3 | Deep Veins | Elder | | | Every power's cooldown is 10% shorter |

### Beast

The predator. Fast, hard-hitting and always on the move.

| Skill | Kind | Ranks and cost | Needs | Gate | Blood | Cooldown | What it does |
|---|---|---|---|---|---|---|---|
| Beast Blood | Passive | 1 | None | None | | | Falls 3 blocks further before taking damage |
| Beast Claws | Passive | 3 ranks: 1, 2, 2 | Beast Blood | None | | | Bare hands deal 2 more damage at rank 1, and 1 more with each rank after, so 4 more at rank 3 |
| Swift Prowl | Passive | 3 ranks: 1, 2, 2 | Beast Blood | None | | | 10% more speed each rank |
| Long Leap | Power | 2 | Swift Prowl | None | 1 | 5 | A long leap forward and up |
| Bat Form | Power | 2 | Beast Blood | None | 2 | 5 | Turns into a bat and back (BAT-1 to BAT-8) |
| Hear Heartbeats | Power | 2 | Beast Blood | Adult | 1 | 30 | For 8 seconds the living within 24 blocks show through walls, redder the more blood they hold |
| Pounce | Power | 2 | Long Leap | Adult | 2 | 20 | Leaps onto a target within 10 blocks and pins it for 2 seconds, 1 second for a player. A pinned target can be bitten without sneaking |
| Blood Frenzy | Power | 3 | Beast Claws | Elder | 3 | 90 | For 10 seconds: 30% more speed, 3 more damage, and blood drains twice as fast. Each kill adds 2 seconds, up to 20. While it is on the power bar it also starts by itself when health falls below 25%, at the same blood cost and on the same cooldown, and not while the vampire is weakened or short of blood |
| Night Terror | Power | 4 | Pounce and Blood Frenzy | Ancient | 6 | 300 | Capstone. For 20 seconds the vampire is a great winged beast, half bat and half man, about three blocks tall with grey-black hide, long claws and wings four blocks across. It roars as it changes, claws as it strikes and flinches when hit. In this form it flies as a bat does, its claws deal 6 more damage, it takes half damage, and its roar on changing staggers every villager, hunter mob and human player within a radius of 8 blocks for 1.5 seconds. It costs no blood beyond its price. It cannot feed or use other powers. Whatever forces a bat out of bat form forces it out |
| Apex Predator | Passive | 3 | Night Terror | Ancient | | | Beast powers cost 1 less blood, never less than 1, and their cooldowns are 20% shorter |

### Noble

The ruler. Wins with other people's hands.

| Skill | Kind | Ranks and cost | Needs | Gate | Blood | Cooldown | What it does |
|---|---|---|---|---|---|---|---|
| Noble Blood | Passive | 3 ranks: 1, 2, 2 | None | None | | | 2 more armour each rank |
| Mesmerise | Power | 2 | Noble Blood | None | 2 | 15 | Holds a target that meets the vampire's stare (MIND-1 to MIND-8) |
| Silver Tongue | Passive | 3 ranks: 1, 1, 2 | Mesmerise | None | | | 1 more charm each rank |
| Blood Bond | Passive | 2 | Mesmerise | None | | | 1 more thrall |
| Command | Power | 2 | Mesmerise | None | 2 | 30 | Orders a mesmerised mob or villager for 30 seconds: attack what the vampire looks at, follow the vampire, or open the door the vampire looks at. Players cannot be commanded |
| Dark Court | Passive | 3 | Blood Bond | Adult | | | 2 more thralls |
| Dread Presence | Power | 3 | Noble Blood | Adult | 3 | 60 | For 12 seconds, within a radius of 8 blocks: weak mobs flee, villagers cower and cannot ring the bell, and hunter mobs deal 20% less damage. A human player in reach has Slowness I unless their mesmerise resistance stops it, checked as Mesmerise checks it |
| Blood Tithe | Passive | 3 ranks: 2, 2, 3 | Blood Bond | Adult | | | While a thrall or a member of the vampire's house is within 12 blocks, the nearest takes 10%, 20%, then 30% of the damage dealt to the vampire, and gives 1 blood point every 30, 20, then 10 seconds |
| Glamour | Power | 3 | Silver Tongue | Elder | 5 | 180 | For 30 seconds the vampire passes as human to everyone, vampires included: no flinch, a full face in a hand mirror, a human's name colour, no glow in the eyes and no stoop when weakened, and villagers see no vampire signs. It ends on feeding, using another power or being hit. The sun still burns |
| Sovereign's Gaze | Power | 4 | Command and Dread Presence | Ancient | 6 | 300 | Capstone. Mesmerises every mob and villager the vampire has a clear line of sight to within a radius of 12 blocks for 10 seconds, and every human player there for 3, whether they look back or not. Mesmerise resistance works against it as it does against Mesmerise |
| Born To Rule | Passive | 3 | Sovereign's Gaze | Ancient | | | Thralls and NPC members of the vampire's house have 25% more health and damage, and Noble cooldowns are 20% shorter |

### Occult

The sorcerer. Shadow, fog and the dead.

| Skill | Kind | Ranks and cost | Needs | Gate | Blood | Cooldown | What it does |
|---|---|---|---|---|---|---|---|
| Occult Blood | Passive | 3 ranks: 1, 1, 2 | None | None | | | Half a block more reach each rank |
| Grave Fog | Power | 2 | Occult Blood | None | 3 | 30 | A cloud with a radius of 4 blocks for 8 seconds that blinds and slows those in it. A bank of grave mist rises over the ground it covers |
| Summon Familiar | Power | 2 | Occult Blood | None | 4 | 20 | Calls a ghostly familiar (FAM-1 to FAM-9) |
| Shadow Step | Power | 2 | Grave Fog | None | 2 | 10 | Steps through shadow to stand behind a target within 16 blocks |
| Spirit Pact | Passive | 3 ranks: 2, 2, 3 | Summon Familiar | None | | | Rank 1 allows 1 more familiar. Ranks 2 and 3 each give familiars 25% more health |
| Hex | Power | 2 | Occult Blood | None | 2 | 30 | Curses a target within 16 blocks for 20 seconds, 10 for a player: a turning violet sigil hangs over its head, the vampire sees it through walls, it has Weakness I, and its healing is halved |
| Shadow Veil | Power | 3 | Shadow Step | Adult | 4 | 45 | Unseen for 10 seconds |
| Blood Ward | Power | 3 | Grave Fog | Adult | 4 | 90 | Draws a circle of blood and runes with a radius of 4 blocks on the ground for 30 seconds. A human who stands in it has Slowness I and takes 1 damage each second. Inside it a Blessing cannot be cast and healing prayers do not heal. A weapon already blessed or alight still works, and so do garlic, crucifixes, holy water and consecrated ground. Where it overlaps a Consecrate circle, both work |
| Mist Form | Power | 3 | Shadow Veil | Elder | 4 | 60 | For 8 seconds the vampire is mist, a low swirling cloud with a dull red glow at its heart and two red eyes: it drifts through doors, bars, fences, trapdoors and gaps, and blades and bolts pass through it. It cannot attack, feed or use items. Sun, fire and holy water hurt it twice as much |
| Raise The Dead | Power | 3 | Summon Familiar | Elder | 5 | 120 | Three restless dead rise from the ground and fight for 30 seconds. Each has 20 health and deals 4 damage. They attack whatever the vampire attacks and whatever attacks the vampire |
| Black Mass | Power | 4 | Mist Form and Raise The Dead | Ancient | 6 | 300 | Capstone. For 15 seconds, within a radius of 12 blocks: fog fills the air under a tall bank of grave mist, a ring of red-eyed bats circles the vampire and deals 1 damage each second to hunter mobs and to anything that has attacked the vampire or been attacked by it in the last 10 seconds, and the vampire's Shadow Step costs nothing and has no cooldown |
| Beyond The Veil | Passive | 3 | Black Mass | Ancient | | | Familiars last 50% longer, Occult powers cost 1 less blood, never less than 1, and their cooldowns are 20% shorter |

## Hunter Tree

A hunter has the core and picks two of the four paths. Each path ends in a capstone: an ultimate power that needs Master, then one last passive. Hunter powers cost no blood.

### Core

What any hunter learns on the road, whichever paths they walk.

| Skill | Kind | Ranks and cost | Needs | Gate | Cooldown | What it does |
|---|---|---|---|---|---|---|
| Tracking | Power | 1 | None | None | 60 | Points toward the nearest vampire within 64 blocks for 10 seconds (HUNT-5). Every path needs it first |
| Weathered | Passive | 3 ranks: 1, 2, 3 | Tracking | None | | 1 more armour each rank |
| Second Wind | Power | 2 | Tracking | None | 45 | Speed I and Resistance I for 8 seconds |
| Clear Eyes | Passive | 2 ranks: 1, 2 | Tracking | None | | Blindness and Darkness on the hunter last 25% less each rank |
| Stakewright | Passive | 3 ranks: 1, 2, 2 | Weathered | None | | A stake's blow deals 10% more damage each rank |
| Bind Wounds | Power | 2 | Second Wind | None | 30 | Heals 6 health over 4 seconds |
| Sure Footing | Passive | 2 ranks: 1, 1 | Clear Eyes | None | | Falls 2 blocks further before taking damage each rank |
| Unyielding | Passive | 2 ranks: 2, 2 | Stakewright | Hunter | | 20% knockback resistance each rank |
| Old Hand | Passive | 3 | Bind Wounds | Master | | Every hunter power's cooldown is 10% shorter |

### Trapper

The fight is won before it starts.

| Skill | Kind | Ranks and cost | Needs | Gate | Cooldown | What it does |
|---|---|---|---|---|---|---|
| Field Craft | Passive | 3 ranks: 1, 1, 2 | Tracking | None | | Sets traps 25% faster each rank, and a vampire cannot see the hunter's traps from more than 6, 4, then 2 blocks |
| Snare | Power | 2 | Field Craft | None | 20 | Sets a hidden trap on the block looked at within 4 blocks. It lasts 5 minutes and pins a vampire that steps on it for 3 seconds, 2 for a player. A bat or a vampire in Mist Form is not caught. Two can be set at once |
| Bait | Power | 2 | Field Craft | None | 60 | Sets out blood on the block looked at within 4 blocks for 60 seconds. Hungry vampire mobs within 24 blocks come to it, and a vampire player within 8 blocks is told they smell blood |
| Net Shot | Power | 2 | Snare | None | 25 | Fires a weighted rope net up to 24 blocks, spinning open as it flies. It drags a bat, a levitating vampire or one in a leap to the ground, forces it out of Bat Form or Night Terror, and keeps it on the ground for 5 seconds, with the net weighted down over it |
| Quick Hands | Passive | 3 ranks: 1, 2, 2 | Snare | None | | 1 more snare can be set at once each rank |
| Trap Sense | Passive | 2 | Snare | None | | The hunter sees their own and their allies' traps outlined, and never sets off an ally's |
| Silver Edge | Passive | 3 ranks: 2, 2, 3 | Net Shot | Hunter |  | A snare or a net deals 2 silver damage each rank to the vampire it catches, and a flash trap 2 more each rank |
| Flash Trap | Power | 3 | Snare | Hunter | 60 | Sets a trap on the block looked at within 4 blocks. It lasts 5 minutes and bursts when a vampire comes within 3 blocks: 6 damage that burns as the sun does, and 3 seconds of blindness. One can be set at once |
| Killing Ground | Power | 4 | Flash Trap and Net Shot | Master | 300 | Capstone. Marks a circle with a radius of 10 blocks, reaching 10 blocks up, for 20 seconds. Traps inside reset the moment they spring, and a vampire inside cannot use Vanish, Shadow Step, Bat Form or Mist Form |
| Master Trapper | Passive | 3 | Killing Ground | Master | | Trapper cooldowns are 20% shorter, and a pinned vampire takes 25% more damage |

### Faithful

Faith as a weapon, and the one who keeps the others alive.

| Skill | Kind | Ranks and cost | Needs | Gate | Cooldown | What it does |
|---|---|---|---|---|---|---|
| Faith | Passive | 3 ranks: 2, 2, 3 | Tracking | None | | 1 more faith each rank, which makes crucifixes and holy water stronger |
| Mend | Power | 2 | Faith | None | 45 | Heals the hunter and every human within a radius of 6 blocks for 6 health |
| Rebuke | Power | 2 | Faith | None | 30 | With a crucifix in hand, or a Crucifix On A Chain worn, throws back every vampire within 8 blocks in a quarter circle in front of the hunter, and stops their powers for 2 seconds |
| Blessing | Power | 2 | Faith | None | 90 | Blesses the hunter's weapon, or the weapon of an ally they look at, for 60 seconds. A blessed weapon works as one coated with holy water (GEAR-9), with no limit on its hits until the time ends. The ally must be in sight within 8 blocks, and with no armed ally in sight the hunter's own weapon is blessed. A blessing replaces any coating already on the weapon. It is refused when the hunter, or the one holding the weapon, stands inside a Blood Ward |
| Grace | Passive | 3 ranks: 1, 2, 2 | Mend | None | | The hunter's healing prayers heal 25% more each rank |
| Blood Fire | Power | 3 | Blessing | Hunter | 120 | The hunter cuts themself for 4 health and lights their weapon. For 90 seconds crimson flames lick along the blade and rise off it, it burns bright and glows in the dark, deals 3 more damage as fire and sets a vampire alight. A vampire does not heal while it burns. A weapon can be blessed and alight at once. Each blow sets a vampire alight for 5 seconds, and a target that cannot burn takes no extra damage. A hunter with 4 health or less cannot use it |
| Sanctuary | Power | 3 | Mend | Hunter | 120 | A prayer of 10 seconds. Humans within a radius of 8 blocks heal 1 health each second, are freed from mesmerise and cannot be mesmerised or cowed by Dread Presence. The prayer follows the hunter and ends if they die |
| Consecrate | Power | 3 | Rebuke | Veteran | 120 | Blesses the ground in a circle with a radius of 5 blocks for 30 seconds. A vampire inside is weakened as by a crucifix, is forced out of Bat Form and Night Terror, and cannot levitate or use Vanish or Bat Form. The weakening is a crucifix's, raised by the hunter's faith. Night Terror is not barred, but a vampire in it over the ground is cast out within half a second. The ground reaches 8 blocks up. A boss on it is weakened but never cast out of the air or barred from a power |
| Judgement | Power | 4 | Consecrate and Sanctuary | Master | 300 | Capstone. Calls a pillar of holy light with a radius of 2 blocks on a spot within 16 blocks for 5 seconds. It deals 3 damage each second to a vampire inside, burning as the sun does, and heals a human inside 2 health each second, from the moment it lands. The pillar reaches 8 blocks up as a slowly turning shaft of pale gold light over a bright disc, and ends if the hunter dies |
| Unshaken Faith | Passive | 3 | Judgement | Master | | Faithful cooldowns are 20% shorter, and the hunter's healing also clears Weakness |

### Slayer

The duellist. Reads a vampire's move and punishes it.

| Skill | Kind | Ranks and cost | Needs | Gate | Cooldown | What it does |
|---|---|---|---|---|---|---|
| Bite Ward | Passive | 3 ranks: 2, 2, 3 | Tracking | None |  | 0.35 more bite resistance at ranks 1 and 2, and 0.1 more at rank 3, 0.8 in all (HUNT-4) |
| Sidestep | Power | 2 | Tracking | None | 6 | A quick dash of 4 blocks the way the hunter is moving, or back when they stand still. Nothing can touch them for 0.3 seconds. If a vampire's attack would have landed within 0.25 seconds of the dash starting, the vampire is staggered for 1.5 seconds. That is a vampire mid swing at the hunter and in reach, or leaping or dashing at them, as the dash starts, or one whose blow reaches them in those 0.25 seconds |
| Stake Throw | Power | 2 | Tracking | None | 10 | Throws a stake from the inventory up to 20 blocks for 4 damage. It staggers a vampire for 1 second, knocks it out of the air, and can be picked up again. A thrown stake never stakes: it deals damage only. The stake in hand is thrown first, then one from the pack, and it drops where it lands. It is refused with no stake to throw |
| Vampire Slayer | Passive | 3 ranks: 1, 2, 2 | Bite Ward | None | | 1 more damage against vampires each rank, on every blow the hunter strikes and every shot they land |
| Riposte | Power | 2 | Sidestep | None | 12 | A guard held for half a second. A Long Leap, a Pounce, a Shadow Step or a vampire mob's leap, dash or lunge that meets it is turned back, and the vampire is staggered for 2 seconds. A charge meets the guard when the vampire comes within 2.5 blocks of the hunter within half a second of starting it, and it is thrown 3 blocks back |
| Quick Shot | Power | 2 | Stake Throw | None | 15 | Fires a loaded crossbow at once, with no time to raise it. The crossbow can be in either hand, and the power is refused with none loaded |
| Tough As Oak | Passive | 3 ranks: 1, 2, 2 | Bite Ward | None | | 2 more health each rank |
| Heartstrike | Power | 3 | Riposte | Hunter | 30 | A lunge of 5 blocks with a stake in hand. A vampire up to an elder that is snared, netted, pinned or staggered by a Riposte or a dodge, and below 40% health, is downed at once, not staked. Any other vampire takes a stake's blow with half as much damage again. The lunge needs a creature in sight within 5 blocks and room to stand beside it, and a downed vampire it strikes is staked as by any stake |
| Last Stand | Passive | 3 | Tough As Oak | Veteran | 180 | When health falls below 25%, for 8 seconds the hunter cannot be bitten or mesmerised and deals 3 more damage. The 3 is added to the hunter's blows. A bite already under way breaks off, a vampire mob's bite is refused, and a vampire player that tries to bite is told the hunter stands fast |
| Onslaught | Power | 4 | Heartstrike and Last Stand | Master | 300 | Capstone. For 6 seconds the hunter strikes twice as fast and deals half as much damage again to vampires. A blow on a vampire below 30% health downs it, up to an elder, and it must still be staked. An ancient is never downed by it. |
| Slayer's Calm | Passive | 3 | Onslaught | Master | | Slayer cooldowns are 20% shorter, Last Stand's among them, and a dodge that staggers gives Sidestep back at once |

### Seeker

Nothing hides from a Seeker.

| Skill | Kind | Ranks and cost | Needs | Gate | Cooldown | What it does |
|---|---|---|---|---|---|---|
| Keen Tracking | Passive | 3 ranks: 1, 1, 2 | Tracking | None | | Tracking reaches 32 blocks further and lasts 5 seconds longer each rank. At rank 3 fresh blood trails show: the vampire Tracking points to drips blood where it stands, seen by the hunter alone |
| Iron Will | Passive | 3 ranks: 2, 2, 3 | Tracking | None |  | 0.35 more mesmerise resistance at ranks 1 and 2, and 0.1 more at rank 3, 0.8 in all |
| Sixth Sense | Passive | 2 | Tracking | None |  | The hunter is warned, with the direction, when a vampire mob within 48 blocks watches, stalks or targets them. A vampire player never sets it off |
| Soft Step | Passive | 3 ranks: 1, 2, 2 | Sixth Sense | None | | 25% less noise in a vampire's house each rank (HUNT-8). With a Stalker's Cloak worn as well, no step is quieter than a fifth of its noise |
| Read The Scene | Power | 2 | Keen Tracking | None | 20 | Used on a victim, a coffin or a kill, it tells the vampire's stage, its bloodlines and the way it went. The hunter looks at it within 8 blocks: a creature a vampire has bitten, a vampire's coffin, or the spot within 4 blocks of where a vampire killed. Signs stay fresh for one day. The way it went points to the vampire now, or says its trail is cold when it is dead or out of reach in another place. A claimed coffin whose keeper is away tells only that they are far from here. With nothing to read it is refused |
| Mark Quarry | Power | 2 | Keen Tracking | None | 60 | Marks one creature or player in sight within 24 blocks, vampire or not. For 60 seconds a gold hunter's brand turns under its feet, it shows through walls, and a marked vampire takes 15% more damage from the hunter. Its trail stays for Tracking until dawn or its death. Shadow Veil, Mist Form and Glamour hide the outline while they last. One mark at a time. A marked player is told. A boss's outline shows only while the hunter can see it |
| Grave Listener | Passive | 2 | Sixth Sense | None | | While the hunter sneaks, sleeping vampires within 12 blocks are heard and faintly outlined through walls. Each sleeper's slow heartbeat comes from where it lies, every two seconds |
| True Sight | Power | 3 | Sixth Sense | Hunter | 60 | For 6 seconds the hunter sees every vampire within 24 blocks outlined, through walls and whether it is hidden by Vanish, Shadow Veil, Mist Form or Glamour. A vampire player who is seen is told. A hidden vampire seen this way counts for the Seen Through trial |
| Revelation | Power | 4 | True Sight and Mark Quarry | Master | 300 | Capstone. For 20 seconds every vampire within 48 blocks is marked for the hunter and for every human within 16 blocks of them, and Shadow Veil, Mist Form and Glamour fail for those vampires. A vampire player who is marked is told. The 16 blocks are from the hunter. A veil, mist or glamour already up ends, and a marked vampire that tries one is told it is laid bare. A vampire player loses them only when PvP is on |
| Hunter's Patience | Passive | 3 | Revelation | Master |  | Seeker cooldowns are 20% shorter, and the hunter's allies also deal 10% more damage to a vampire the hunter has marked, by Mark Quarry or Revelation. Allies are other sworn hunters and guild hunters, and teammates |

## Skills The Mod Already Has

Nothing built is lost. Every skill a player can learn today keeps its name and effect as rank 1 or as the power above, except these, which become ranks of another skill with the same effect. Their costs change to the ones in the tables above:

| Old skill | Becomes |
|---|---|
| Steeled Blood | Rank 2 of Bite Ward |
| Devotion | Rank 2 of Faith |
| Clear Mind | Rank 2 of Iron Will |

Hunter skills bought in an older world are reset and their points given back (HUNT-3).

The skills only vampire mobs have, Bat Swarm and Call To Arms, stay as they are and cannot be learned by a player.

## New Art These Skills Need

- A net, a snare and a flash trap.
- An icon for every skill, at `textures/gui/sprites/skills/icon/<id>.png`. The id is the skill's name in lower case with spaces as underscores and apostrophes dropped, such as `sovereigns_gaze`.

## Art And Feel

- It must be fun. Being a vampire or a hunter is a power fantasy, and everything here should make the player feel strong, feared and clever.
- Keep it balanced: every power and gift has a cost or a counter on the other side.
- Everything looks and sounds like it belongs with the rest of the mod. The LOOK rows of `docs/prd.md` are the whole style brief: dark gothic, played straight.
- The art bar is AAA Minecraft studio quality. Every model, clip and texture is made in a Blockbench project through the `bbmodel` MCP and is a GeckoLib model or clip. Flat item sprites are made through the `item-sprite` skill. Nothing is posed or animated in code, and no art file is edited by hand.
