# Nightborn: Product Requirements

## How To Read This Document

This document says what Nightborn is: what a player can do with it, who may do what, which side decides each rule, and what the mod deliberately does not do. Every requirement has an identifier such as `FEED-3`, so issues, commits and reviews can cite it.

It holds no status. Nothing here says whether a requirement is built, in progress or waiting. Progress lives on the project board.

It changes only when a requirement changes or turns out to be unclear. The reasoning behind architectural choices lives in the decision records in [`docs/adr/`](adr/). The engineering baseline lives in [`docs/standards.md`](standards.md). Numbers in this document are starting values, to be tuned in play and then held as requirements.

## 1. Product Summary

### The Problem

Minecraft has no vampires, and nothing that hunts them. Its undead are mindless mobs that burn at dawn. There is no way to become something that fears the sun, lives on blood and grows stronger with age, and no reason to fear the dark beyond a creeper. A player who wants a night that feels dangerous, or a server where players choose sides, has nothing to choose from.

### What The Mod Is

Nightborn adds vampires and the people who hunt them. A bite from a vampire or a vampire bat starts a sickness, and if it is not cured in time you turn. As a vampire you live on blood, sleep through the day in a coffin, and nothing but direct sun or a stake can finish you. Anything else only brings you down. You grow through a skill tree and pick two of three bloodlines: Noble, Beast and Occult. Each gives powers the others do not, such as bat form, mesmerising, thralls and ghostly familiars. Humans can fight back. Garlic, crucifixes, holy water, silver and the stake all hurt vampires, and every human earns hunter skills by killing them. Vampires are rare. They live in households hidden in castles, crypts, ruined manors and deep lairs, wrapped in fog, and hunters keep camps of their own. Each vampire is someone: a look, a voice, a stage of age and powers of its own. A hunter who gathers a relic from every kind of vampire structure can summon the Eldest, a vampire with every power, again and again.

### Experience It Aims For

- **The night is dangerous again.** Vampires are rare, so meeting one matters.
- **Every vampire is someone.** Wild vampires have a home, a routine, a look and powers of their own. An old one is a fast, frightening fight that pays well.
- **Being a vampire changes how you play.** Sun, blood, garlic and the coffin shape every day.
- **Two sides, one server.** Vampires and hunters are real factions, and players choose which to be.
- **Choices stick.** A bloodline, once chosen, is yours. A turned player stays turned.
- **Lived-in gothic.** Places feel old, rich and inhabited. Candlelight, decay and clutter, never clean or plastic.
- **Premium look and sound.** Every model, animation and sound is hand-made and judged against the best Minecraft art.
- **Vanilla at heart.** Recipes and rules echo what vanilla already teaches: a cleric blesses, a golden apple cures, a bed skips the night.

### What Makes It Different

| Claim | Why it holds |
|---|---|
| Both sides are playable | Turning, feeding and bloodlines for vampires, gear and a skill tree for hunters (TURN, FEED, LINE, HUNT) |
| Feeding is a real choice | Sip or drain, fresh or bottled, animal or person, and the victim can fight back (FEED-1 to FEED-14) |
| Builds that fit your playstyle | Pick two of three bloodlines, each with its own powers (LINE-1 to LINE-7) |
| Vampires are hard to kill | Only direct sun or a stake finishes one. Anything else only brings it down (CORE-7, CORE-8) |
| Folklore that works | Garlic, crucifixes built from blocks, holy water, silver and stakes each have real rules (WEAK-1 to WEAK-10) |
| Vampires live somewhere | Households with personalities in four kinds of structure, rare and hidden in fog. Vampires live there, go out to feed and come home by dawn (HOME-1 to HOME-20) |
| Vampires fight like players | Wild vampires grow in stages and use every power a player can, more often and more cleverly the older they are (HOME-15 to HOME-17) |
| The day belongs to vampires too | Coffins skip the day the way beds skip the night (COFFIN-1 to COFFIN-12) |
| An endgame for hunters | A repeatable ritual summons the Eldest, a boss with every vampire power (RITE-1 to RITE-8) |
| Help in the daylight | A human familiar runs errands and guards your coffin, and might betray you (FAM-1 to FAM-8) |

### Non-Goals

- **Not a werewolf or general monster mod.** Vampires and hunters only.
- **No cure after turning.** A cure works only during the sickness.
- **No vampire that feeds on anything but blood.**
- **Not a claims or protection mod.** Garlic weakens vampires. It does not stop them breaking blocks.
- **No change to vanilla undead.** Zombies, skeletons and phantoms behave as they always have, except that phantoms never come for vampires (CORE-9).

## 2. Release Scope

This document specifies the Nightborn alpha line for Minecraft 1.21.1 on Fabric.

| An alpha release accepts | An alpha release refuses |
|---|---|
| Features that are thin, as long as what is there works | Any item or block that can be duplicated or lost by a game mechanic |
| Settings that need a world restart to apply, if the setting says so | A client being able to change anything the server decides |
| Missing optional integrations | A crash, or a server that will not start, from a bad config file |
| Art still being refined, if it reads clearly in game | A player's vampire state, blood or skills lost by a reload, death or disconnect |
| Fewer structures, decor or clothing than this document lists | Any untranslated text in a shipped language |

### Deferred

| Out | Why |
|---|---|
| Vampire raids on villages | Structures and their households carry the world first |
| Mirrors that show no reflection | Not chosen for the first set |
| Vampires needing an invitation to enter a home | Needs a notion of home ownership, which the mod does not have |
| Changing bloodlines once chosen | Choices are meant to stick. A reset can come later if play shows the need |
| Hunter orders with separate trees | One shared tree keeps hunters simple |
| Hiding vampire markers on map mods | Vanilla maps come first (CORE-5) |

### Product Decisions

| Question | Decision | Requirement |
|---|---|---|
| Which side is the mod about? | Both. Vampires and hunters are both playable | TURN, HUNT |
| How do you turn? | A bite from a vampire, a vampire bat or a vampire player | TURN-1 |
| Can you be cured? | Only during the sickness | CURE-1 to CURE-3 |
| How bad is the sun? | Lethal. Direct sun sets a vampire on fire | SUN-1 |
| What replaces hunger? | Blood | FEED-1 |
| How do powers grow? | A skill tree with three bloodlines, of which you invest in two | LINE-1 to LINE-3 |
| Who is a hunter? | Any human who kills vampires | HUNT-1 |
| How does a vampire die? | Only to direct sun or a stake. Anything else brings it down, greatly weakened, until it recovers and escapes | CORE-7, CORE-8 |
| What happens on death? | Respawn at your coffin. A stake costs skill progress. A server can make a vampire's death a full reset instead | COFFIN-4, SKILL-6, CORE-10 |
| PvP? | Vampires and hunters are factions, and PvP follows the server's own setting | FACT-1 to FACT-4 |

## 3. Actors

| Actor | What they need |
|---|---|
| **Vampire** | To feed, survive the sun, grow their bloodlines and keep a coffin |
| **Human** | To stay human, or cure a bite in time, and to fight back |
| **Hunter** | Any human who hunts. To track, ward against and kill vampires, and earn skills for it |
| **Sick player** | Bitten and not yet turned. To know how long they have and how to cure it |
| **Owner** | The vampire a thrall, ghostly familiar or human familiar serves |
| **Villager** | Fed on, mesmerised, turned into a thrall or hired as a familiar. Reacts only when it sees a bite |
| **Server operator** | To set every option from the command line, manage any player's state, and trust that clients cannot cheat |
| **Pack author** | To tune numbers, structures, loot and blood values with data packs and config |
| **A vanilla client** | Cannot join: the mod adds blocks, items and mobs, so the client must have it installed |
| **A modified client** | Assumed hostile. It can send any packet with any value |
| **Reduced settings** | A player on Minimal particles, with sound off or with gore reduced still knows what is happening |

## 4. Domain Model

| Term | Definition |
|---|---|
| Vampire | A player or mob that has turned. Lives on blood, and dies only to direct sun or a stake |
| Human | A player who is not a vampire |
| Bite sickness | The state between a turning bite and turning. It lasts 3 in-game days |
| Blood | What a vampire lives on, shown as the blood bar in place of hunger |
| Fresh blood | Blood drunk straight from a living creature |
| Bottled blood | Blood stored in a Blood Bottle. It restores less than fresh blood |
| Blood quality | How much a creature's blood is worth: animal, then villager or illager, then player |
| Sip | Feeding that stops before the victim dies |
| Drain | Feeding until the victim dies |
| Resist | A victim breaking free of a bite |
| Weakened | The vampire debuff from garlic, crucifixes, holy water, starving, being downed and escaping: slower, weaker blows and no powers. It is not the vanilla Weakness effect |
| Skill point | Earned by a vampire through feeding, or by a hunter through killing vampires. Spent on a skill tree |
| Core | The vampire skills every vampire can take |
| Bloodline | One of three branches of the vampire tree: Noble, Beast or Occult |
| Hunter tree | The one skill tree every human can earn |
| Direct sun | Daylight on a vampire with open sky above it, not in rain, water or shade |
| Garlic String | A block of hung garlic that weakens vampires near it |
| Crucifix shape | A cross of crucifix-material blocks built in the world (see below) |
| Downed | A vampire brought to its last health by anything but direct sun or a stake. It lies helpless and greatly weakened until it recovers |
| Escaping | The minute after a downed vampire gets up, when only direct sun and a stake can hurt it and it stays weakened, so it can get away |
| Staked | Killed by a stake: a wooden or silver-tipped stake, or a stake bolt, hitting a downed vampire, or striking the blow that would bring a vampire down |
| Coffin | A block, lying flat or standing upright, that is a vampire's bed and respawn point |
| Thrall | A villager a Noble vampire has bound to follow and fight for them |
| Ghostly familiar | A spirit an Occult vampire summons to fight and do tasks |
| Human familiar | A villager hired as a vampire's daytime servant |
| Loyalty | How a human familiar feels about its owner, from 0 to 100 |
| Household | The vampires who live in one vampire structure |
| Stage | How old and strong a vampire mob is: fledgling, adult, elder or ancient |
| Hungry | A vampire mob whose blood has fallen below its hunger threshold |
| Relic | A rare item found in one kind of vampire structure, used in the ritual |
| The Eldest | A boss vampire with every vampire skill, summoned only by the ritual |
| Vampire structure | A castle, crypt, ruined manor or underground lair, home to a household |
| Hunter camp | A generated camp of hunter mobs, home to a Guild Master |
| Faction | Vampire or human. Every player is in exactly one |

### Who Can Be Fed On

| Creature | Can be fed on | Why not |
|---|---|---|
| Passive animals with blood (cows, pigs, sheep, horses and the like) | Yes, animal quality | |
| Villagers, wandering traders, illagers, witches | Yes, villager quality | |
| Human players and hunter mobs | Yes, player quality | |
| Vampire players and vampire mobs | No | Their blood is already dead |
| Undead, golems, slimes, blazes, arthropods, fish | No | No warm blood to drink |

The list is a tag, so a pack can change it.

### Crucifix Shape

A crucifix shape is a vertical column of at least 3 blocks with a single block on each side, one below the top, all from the `nightborn:crucifix_materials` tag. The tag starts with every plank, log and gold block, and any block in `c:storage_blocks/silver`. The shape can face any horizontal direction. The arms and the top are single blocks: another tag block beyond an arm, above or below an arm, or above the top means it is not a crucifix.

## 5. Use Cases

### 5.1 Get Turned

**Actor:** Human, sick player.

A vampire mob, a vampire bat or a vampire player bites a human. The bite may start the sickness. A sick player sees a clear sign that something is wrong, which grows as the days pass. When the sickness runs out, the player collapses where they stand and wakes up a vampire.

| ID | Requirement |
|---|---|
| TURN-1 | A bite from a vampire mob, vampire bat or vampire player starts bite sickness on a human, with a chance set per biter. By default a vampire player's bite starts it every time, a vampire mob's one time in four and a vampire bat's one time in ten, less against a human with bite resistance (HUNT-4) |
| TURN-2 | A vampire player can bite a human player only where the server allows PvP |
| TURN-3 | Bite sickness lasts 3 in-game days, and gets worse each day: hunger, then the vanilla Weakness effect, then slowness in daylight |
| TURN-4 | A sick player is shown the sickness and the days left, as a status effect and a sign on screen |
| TURN-5 | When the sickness runs out, the player turns. They keep their inventory, start with a full blood bar and get the vampire core |
| TURN-6 | A player who turns stops earning hunter skills, and their hunter tree is set aside. It comes back if an operator makes them human again |
| TURN-7 | Another bite during the sickness does not restart or lengthen it |

**Not supported:** turning by any means other than a bite or an operator command; turning a villager into a vampire.

**Enforcement:** server.

### 5.2 Cure The Sickness

**Actor:** Sick player.

| ID | Requirement |
|---|---|
| CURE-1 | Eating a golden apple while under Weakness cures bite sickness, as it cures a zombie villager |
| CURE-2 | Drinking holy water while sick cures it |
| CURE-3 | A cure works only during the sickness. Nothing a player can do cures a vampire. Only an operator can make a vampire human again (CFG-8) |

**Not supported:** a cure after turning.

**Enforcement:** server.

### 5.3 Feed

**Actor:** Vampire.

Sneak and use on a creature to bite it, and keep holding to drink. Let go to stop. A creature that can fight back may break free. Drink long enough and it dies.

| Gesture | Effect |
|---|---|
| Sneak and hold use on a creature, empty hand | Bite and drink |
| Let go | Stop. The victim lives |
| Keep holding past the victim's last health | Drain. The victim dies |
| Hold an empty Glass Bottle in the off hand while feeding | Fill Blood Bottles instead of drinking |
| Drink a Blood Bottle | Restore bottled blood |

| ID | Requirement |
|---|---|
| FEED-1 | A vampire has a blood bar in place of hunger. It drains over time and faster when the vampire runs, fights or uses powers |
| FEED-2 | An empty blood bar starves the vampire (FEED-14), and a full one heals |
| FEED-3 | Normal food, potions and milk give a vampire nothing. A vampire who eats or drinks one is hurt and vomits it back up, with a short spell of Nausea, seen and heard by everyone near. Anything eaten or drunk counts, including other mods' food and drink, as does anything in the `nightborn:vampire_rejects` tag, which takes in the shared `c:foods` and `c:potions` tags. Blood Bottles, holy water and items in the `nightborn:vampire_consumables` tag are exempt |
| FEED-4 | Feeding drinks blood from the victim each second, and hurts it by the same amount |
| FEED-5 | Letting go stops the bite and leaves the victim alive, with Weakness for a short time |
| FEED-6 | Holding past the victim's last health drains it, and it dies |
| FEED-7 | A victim resists with a chance that grows with its maximum health and falls as its health drops. A sleeping or mesmerised victim never resists |
| FEED-8 | A resisting victim breaks free, pushes the vampire back and, if it can attack, fights back |
| FEED-9 | Blood restores more by quality: animal least, then villager and illager, then player |
| FEED-10 | Fresh blood restores more than the same blood bottled |
| FEED-11 | A Blood Bottle keeps the quality of the creature it came from, and shows it |
| FEED-12 | Feeding earns vampire skill points from the blood drunk, weighed by its quality, with a bonus for draining. Filling Blood Bottles counts as feeding |
| FEED-13 | A villager that sees a vampire feed panics and flees, rings the village bell, and the village treats the vampire as it treats a player who hurt a villager, through vanilla gossip. A villager that sees nothing acts as normal |
| FEED-14 | A starving vampire grows more weakened over time, up to the cap (WEAK-9). Its powers and levitation stop, then its night speed and jump fade, and its bare-hand damage shrinks. After a while it loses health slowly, down to half a heart on any difficulty but Peaceful. Starving never downs or kills a vampire. Any blood stops it growing, and the weakness fades as the bar refills

**Not supported:** feeding on the list's excluded creatures; feeding through blocks; a human drinking blood.

**Enforcement:** server. The client sends the bite request; the server checks reach, the target and the faction.

### 5.4 Survive The Day

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| SUN-1 | A vampire in direct sun catches fire, as a zombie does, and burns while it stays there. Sun fire kills, downed or not. Fire that keeps burning after the vampire reaches shade only downs it, and cannot hurt it while it is escaping |
| SUN-2 | No armour, helmet or effect, Fire Resistance included, stops the burn. Shade, rain, water and a closed coffin (COFFIN-7) do |
| SUN-3 | A burning vampire gives off smoke and a sizzling sound, on top of the fire |
| SUN-4 | Vampire mobs follow the same rules and seek shade at dawn |

**Not supported:** a way to walk in direct sun.

**Enforcement:** server.

### 5.5 Feel Your Weaknesses

**Actor:** Vampire, hunter.

How weakened a vampire is scales with how strong the source is. By default each Garlic String in range weakens by 15%, up to 45% for three or more; touching or holding garlic weakens by 80%; a held crucifix weakens by 60%, and by 25% more of that for each point of the holder's faith; and a crucifix shape weakens by 40%. A crucifix held by a vampire wards no one. A vampire mob's only attack is its bite, so it cannot hurt a crucifix holder at all.

| ID | Requirement |
|---|---|
| WEAK-1 | Each Garlic String weakens vampires within 8 blocks. More strings in range weaken more, up to a cap |
| WEAK-2 | Touching garlic, by walking through garlic crops or brushing a Garlic String, or holding garlic, weakens a vampire greatly, far more than a string in range |
| WEAK-3 | Holding a Crucifix in either hand weakens and slows vampires within 6 blocks who can see it, players and mobs alike, and stops them biting the holder |
| WEAK-4 | A crucifix shape in the world weakens vampires within 8 blocks who can see it |
| WEAK-5 | Holy water hurts vampires and weakens them for a short time, whether splashed, lingering or fired as a bolt. It does nothing to anyone else |
| WEAK-6 | Silver weapons deal extra damage to vampires, and only to vampires |
| WEAK-7 | A stake deals extra damage to vampires, and stakes a vampire it downs or hits while downed, whoever holds it. It also hurts a vampire that is escaping |
| WEAK-8 | A weakened vampire shows it to everyone with a sign that is not colour alone |
| WEAK-9 | Being weakened by different sources adds up, to a cap |
| WEAK-10 | Being weakened never damages a vampire on its own. Holy water does damage, as WEAK-5 says |

**Not supported:** garlic or crucifixes stopping a vampire break blocks or open doors.

**Enforcement:** server.

### 5.6 Be A Vampire

**Actor:** Vampire.

Every vampire has the core, whatever bloodlines they choose.

| ID | Requirement |
|---|---|
| CORE-1 | A vampire has double a human's health |
| CORE-2 | A vampire hits harder with bare hands than a human, more so as they grow |
| CORE-3 | At night a vampire has night vision, moves faster and jumps higher |
| CORE-4 | A vampire's eyes glow red while feeding, for 5 seconds after, and while their health is below 30%. At all other times they look normal |
| CORE-5 | A vampire player never shows as a marker on a vanilla map |
| CORE-6 | A vampire can sleep only in a coffin (COFFIN-1) |
| CORE-7 | Only direct sun and a stake can kill a vampire. Damage from anything else, mobs, players, fire, falls, drowning or holy water included, stops at its last health. Falling into the void and an operator's `/kill` are the only exceptions |
| CORE-8 | A vampire brought to its last health by anything else is downed: it collapses, cannot move, attack, feed or use items or powers, and is weakened at the cap (WEAK-9). It takes no damage from anything but sun and a stake while downed. After 30 seconds by default it gets up with 20% of its health, wherever it lies. More damage does not reset the time. Everyone near can see and hear that it is downed. It then escapes for 1 minute by default: it takes no damage but sun and a stake, stays weakened at the cap and can move freely. Vampire mobs flee while escaping |
| CORE-9 | Phantoms never spawn for a vampire player and never target a vampire, and time a vampire spends awake never counts toward them |
| CORE-10 | With the `hardcoreVampireDeath` server setting on, off by default, a vampire player who dies is reset like a hardcore death: they come back as a new human at world spawn with an empty inventory and ender chest, no experience, no skills, bloodlines or hunter tree, no coffin respawn point, and their thralls and familiars freed. Advancements are kept |
| CORE-11 | Every vampire, player or mob, can levitate. Players toggle it with a key they can rebind. A levitating vampire floats one block above the ground with its feet together, follows the ground up steps and slopes and over water, and sinks slowly over a drop without fall damage. It leans forward to move and further to sprint, never fully horizontal, with arms spreading as it speeds up, and lands with its own animation. It drains blood while active. It can still attack and use items while it floats, and others see the swing or the raised hand over the float. A weakened or downed vampire, or one in bat form, riding, sleeping, gliding or flying in creative, cannot levitate and lands at once. The server lands a player who stays well above that height for more than a moment, and ignores a toggle pressed again within half a second. Every landing the vampire did not choose sets it down gently, never with fall damage. Vampire mobs rise in a fight and now and then as they move about, as often as the server's options allow, and still find their way while levitating |

**Not supported:** a vampire hiding its eyes while feeding; levitating higher than one block.

**Enforcement:** server decides state. The client draws eyes and effects.

### 5.7 Grow Your Bloodlines

**Actor:** Vampire.

The vampire tree has the core and three bloodlines. A vampire player spends points in at most two bloodlines, so each vampire is a different kind of vampire. Vampire mobs are not bound by this (HOME-15).

| Bloodline | What it gives |
|---|---|
| **Noble** | Mesmerise, thralls and charm |
| **Beast** | Bat form, stronger bare hands, speed and a long leap |
| **Occult** | Ghostly familiars, fog and shadow tricks |

The core starts with Thick Blood (more health), Grave Strength (harder blows) and Blood Rush, an active power that burns blood for a short burst of speed and strength. The first skill of each bloodline chooses it and gives a small lasting gift: tougher skin for Noble, safer falls for Beast and longer reach for Occult.

| ID | Requirement |
|---|---|
| LINE-1 | The vampire tree has the core and the three bloodlines |
| LINE-2 | Spending a first point in a bloodline chooses it. Once two are chosen, the third is locked |
| LINE-3 | A chosen bloodline cannot be swapped, except by an operator (CFG-8) |
| LINE-4 | Every bloodline has skills a vampire without it cannot get |
| LINE-5 | Each skill shows what it does, what it costs and what it needs first, before it is bought |
| LINE-6 | The skill screen opens with a key, and shows free points with a skill point graphic, readable at a glance, and points earned and spent |
| LINE-7 | The vampire and hunter skill screens are styled, each with its own look: background, frames, lines, headers, tooltips and point graphic. Neither is a plain menu |

| ID | Requirement |
|---|---|
| SKILL-1 | Vampire points and hunter points are separate, and each is spent only in its own tree. Their sources are FEED-12, HUNT-1 and HUNT-2 |
| SKILL-2 | A skill costs points and may need another skill first |
| SKILL-3 | Bought skills are kept across death, reconnect and world reload |
| SKILL-4 | Active skills sit in a small power bar of up to five slots, in the order they were bought, and are used with keys the player can rebind |
| SKILL-5 | Each active skill has a blood cost and a cooldown, shown on the bar. A downed or weakened vampire cannot use powers |
| SKILL-6 | A staked vampire player loses 10% of their earned points, rounded up, taken from unspent points first and then from their most recent skills. A bloodline left with none of its skills is freed for a new choice. With CORE-10 on, the full reset applies instead |

**Enforcement:** server. The client asks to buy a skill; the server checks points and order.

### 5.8 Take Bat Form

**Actor:** Beast vampire.

| ID | Requirement |
|---|---|
| BAT-1 | A Beast vampire can turn into a bat and fly |
| BAT-2 | Turning into a bat, and back, gives a poof of smoke and a sound, seen and heard by everyone near |
| BAT-3 | A bat cannot use items, open containers or feed. It drains blood while flying |
| BAT-4 | Direct sun forces a vampire out of bat form |
| BAT-5 | Turning back needs room to stand. Without it, the bat stays a bat |
| BAT-6 | Heavy damage, being downed, holy water or garlic in range forces a vampire out of bat form |

**Enforcement:** server.

### 5.9 Bend Minds

**Actor:** Noble vampire.

| ID | Requirement |
|---|---|
| MIND-1 | Staring at a mob or villager for a moment mesmerises it: it stands still and cannot attack |
| MIND-2 | A mesmerised villager can be charmed: it trades as if the vampire were a hero of the village for a while |
| MIND-3 | A mesmerised player is slowed and cannot attack the vampire for a short time. They are never frozen |
| MIND-4 | Bosses cannot be mesmerised. Hunter skills resist it (HUNT-4) |
| MIND-5 | Sipping from a mesmerised villager can make it a thrall. A thrall follows its owner and fights for them |
| MIND-6 | A thrall can be told to follow, stay or guard a place |
| MIND-7 | A vampire has a limit on thralls, raised by Noble skills |
| MIND-8 | A thrall whose owner dies stays where it is until they return |

**Not supported:** mesmerising through walls.

**Enforcement:** server.

### 5.10 Keep Familiars

**Actor:** Owner.

| ID | Requirement |
|---|---|
| FAM-1 | An Occult vampire can summon ghostly familiars that fight for them, carry items and scout ahead |
| FAM-2 | Ghostly familiars last a set time, cost blood to summon and pass through blocks only while scouting |
| FAM-3 | Any vampire can hire one villager as a human familiar with a Familiar's Contract |
| FAM-4 | A human familiar works in daylight: it carries items between chests the owner marks, trades with villagers for the owner and guards the owner's coffin |
| FAM-5 | A human familiar has loyalty. Good treatment raises it. Being fed on, hurt or left without pay lowers it |
| FAM-6 | A human familiar with low loyalty may secretly turn to the hunters, then attack its owner while they sleep. Its loyalty is never shown as a number |
| FAM-7 | A familiar obeys only its owner |
| FAM-8 | A human familiar killed or turned against its owner frees the contract slot |

**Enforcement:** server.

### 5.11 Hunt Vampires

**Actor:** Human, hunter.

Every human is a hunter in waiting. Killing vampires earns hunter points.

The hunter tree starts with Tracking, an active power that points toward the nearest vampire for a short time and costs no blood. Below it, three paths: Bite Ward and Steeled Blood lower the chance a bite starts the sickness, Iron Will and Clear Mind resist mesmerise, and Faith and Devotion make crucifixes and holy water stronger. Each of these gifts is a player attribute (`nightborn:bite_resistance`, `nightborn:mesmerise_resistance` and `nightborn:faith`) that the skills raise, so the features that use them read one number and never the skills themselves. Faith and mesmerise resistance take effect once crucifixes, holy water and mesmerise exist. A hunter must be online when the sun finishes the vampire to be paid.

| ID | Requirement |
|---|---|
| HUNT-1 | Any human player earns hunter points by staking vampire mobs and vampire players, or by downing one that the sun finishes before its escape ends. Downing alone earns nothing. No pledge is needed. The Eldest also pays hunters, as RITE-6 says |
| HUNT-2 | Contracts from the Guild Master earn hunter points as well as their reward |
| HUNT-3 | The hunter tree is one shared tree for all humans |
| HUNT-4 | Hunter skills cover tracking vampires, resisting bites and mesmerise, and faith powers that make crucifixes and holy water stronger |
| HUNT-5 | Tracking shows the direction of the nearest vampire within range for a short time |

| ID | Requirement |
|---|---|
| GEAR-1 | A wooden stake is crafted from two sticks and used as a melee weapon. It hits lighter and faster than a sword and never sweeps |
| GEAR-2 | Silver ore generates underground, with a deepslate form, rarer than iron and needing an iron pickaxe. It drops raw silver, which smelts to silver ingots |
| GEAR-3 | Silver gives a sword, an axe and a stake tip. Silver wears faster than iron and hits as hard. A silver-tipped stake counts as both a stake and silver |
| GEAR-4 | A crossbow can fire stake bolts, silver bolts, holy water bolts and garlic bombs |
| GEAR-5 | A garlic bomb bursts into a cloud that weakens vampires inside it for a short time |
| GEAR-6 | Holy water is brewed from a water bottle and a silver nugget, or a cleric villager blesses a water bottle for an emerald as a trade. It comes as a drink, a splash and a lingering potion, made from the drink with gunpowder and dragon's breath as vanilla potions are. Holy water cannot tip arrows: the holy water bolt is its ammo |
| GEAR-7 | A Crucifix is crafted from gold or silver and held in either hand |
| GEAR-8 | Garlic grows wild in plains and around villages, can be farmed, and is crafted into Garlic Strings |
| GEAR-9 | Holy water or garlic can be applied to any weapon or ammo in a crafting grid. The coating gives extra damage against vampires for a short time, always less than silver does, and does nothing to anyone else |

**Enforcement:** server.

### 5.12 Visit A Hunter Camp

**Actor:** Human, vampire.

| ID | Requirement |
|---|---|
| GUILD-1 | Hunter camps generate rarely in the overworld, each with a few hunter mobs and a Guild Master |
| GUILD-2 | Hunter mobs attack vampire players and vampire mobs on sight, and leave humans alone |
| GUILD-3 | The Guild Master sells hunter gear and gives contracts, such as staking vampires or clearing a vampire structure |
| GUILD-4 | Contracts pay in emeralds, silver and gear, on top of the hunter points HUNT-2 gives |
| GUILD-5 | A vampire player cannot trade or take contracts at a camp |

**Enforcement:** server.

### 5.13 Find A Vampire Household

**Actor:** Any player.

Vampires are rare. They live in households hidden in four kinds of structure, each placed to fit the land around it. Each structure has a heart, the room its household gathers in, and its fog is thickest there.

| Structure | Where | Household |
|---|---|---|
| Castle | Mountains and dark forests, rarest | A vampire lord and their court, coffins, blood fountains and the best loot |
| Crypt | Under a church in or near a village, or in a graveyard | A few vampires below a working church |
| Ruined manor | Dark forests | A vampire family in an overgrown house |
| Underground lair | Deep caves, where no sun reaches | A den around a blood pool |

| ID | Requirement |
|---|---|
| HOME-1 | Each structure type generates rarely, in biomes that suit it, and blends into the land around it |
| HOME-2 | Each structure holds several vampires |
| HOME-3 | Each type has several layouts, chosen at random |
| HOME-4 | Fog hangs around and inside every vampire structure, thicker near its heart |
| HOME-5 | A household feels lived in: its vampires have their own personality and hobbies, talk to and argue with each other, react to each other's deaths, follow a nightly routine and use the furniture of their home: chairs, candles, drapes, books and gramophones |
| HOME-6 | Household vampires sleep in coffins by day and wake at dusk |
| HOME-7 | A castle's vampire lord is a boss with a health bar and its own powers. It is downed and escapes like any vampire, its bar shows when it is downed or escaping, and only a stake or the sun ends the fight |
| HOME-8 | Structures hold loot chests with gear, Blood Bottles, decor, gothic music discs (DECOR-8), relics (RITE-1) and rare items found nowhere else |
| HOME-9 | Vampire mobs live only in vampire structures, as members of a household. Each remembers its structure and its own coffin. They never spawn at random anywhere else, and never despawn |
| HOME-10 | Vampire bats spawn rarely in dark caves and at night. Under cover, or at night in the open, they swoop at warm-blooded creatures and bite them (TURN-1) |
| HOME-11 | Vampire mobs attack only when hungry or provoked. Being hurt, or a housemate being hurt nearby, provokes them. A fed vampire may watch, follow or warn off a stranger, but does not attack. They ignore vampire players unless provoked |
| HOME-12 | Structure layouts, loot, relic chances, structure spawn rates, vampire stages and their odds per structure type, the skills vampire mobs pick, their looks, sizes, builds and outfits, and the biomes vampire bats spawn in are data, changed by a data pack |
| HOME-13 | Vampire mobs have blood that drains over time. A hungry vampire may leave its structure at night to hunt humans, villagers and animals within a range of home. Not every vampire leaves every night, so a household is rarely empty. Each heads back in time to reach its coffin by dawn. One caught out at dawn seeks shade (SUN-4). Their bites can start bite sickness (TURN-1) |
| HOME-14 | A provoked vampire gives up the chase past a range from its structure and goes home |
| HOME-15 | Each vampire mob has a stage, fledgling, adult, elder or ancient, which sets its health, speed, damage, how many skills it has and how much blood it can spend on powers. Its skills come from the vampire tree and, unlike a player's, may come from all three bloodlines. The older a vampire and the more skills it has, the rarer it is, the harder it fights, and the more hunter points (HUNT-1) and the better loot it gives. Only the Eldest has every skill (RITE-3) |
| HOME-16 | A vampire mob uses every power a player with the same skills could use, with the same blood costs, cooldowns and weaknesses, through the same code. It fights like a skilled player: closing fast, leaping, dodging, mesmerising and taking bat form, and pulling back to recover. Older stages use more powers, and use them more cleverly |
| HOME-17 | Every power a vampire mob uses has a clear tell in sight and sound just before it lands, never colour alone (ACC-1) |
| HOME-18 | Vampire mobs are men and women in at least 10 looks, from 1.5 to 2 blocks tall in slim, average and broad builds. Their hitbox, eye height and reach follow their size. Outfits are chosen by era (LOOK-4). A vampire keeps its look and size for life, the same on every client, and an older stage reads as older |
| HOME-19 | Vampire mobs bare their fangs and hiss, with animation and sound, when they notice a threat, are provoked, face garlic or a crucifix, or are about to strike. They have their own idle, hurt, death, talk and argue voices, male and female, pitched by size |
| HOME-20 | Vampire structures hold gramophones loaded with gothic music discs (DECOR-8). Each starts its disc now and then while a player is near, and its disc can be taken. A gramophone's music never plays over other music the mod or the game plays for the same player |

**Not supported:** a household rebuilding after it is cleared; vampire mobs spawning outside their structures.

**Enforcement:** server.

### 5.14 Sleep In A Coffin

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| COFFIN-1 | A coffin opens and closes, and a vampire can lie in it by day, from dawn to dusk whatever the weather, as a human lies in a bed by night. Using it lies a vampire down; a human, or a vampire who sneaks with an empty hand, opens or shuts the lid instead. The lid shuts over a vampire who lies down, opens when they rise, and stays shut while someone is inside unless a player opens it (COFFIN-9). It softens a fall like a bed, but nothing bounces off its lid. Two cells long, or two cells tall when standing (COFFIN-10), it is crafted like a bed from planks with red wool for the lining and a gold nugget for the fittings |
| COFFIN-2 | Enough vampires sleeping in coffins skips the day to dusk, as beds skip the night to dawn |
| COFFIN-3 | The share of vampire players who must sleep is a server option, 100% by default. Only vampire players online in that dimension count. With none online, nothing skips the day |
| COFFIN-4 | Lying in a coffin sets the vampire's respawn point there, as a bed does |
| COFFIN-5 | A vampire whose coffin is gone respawns at world spawn, as with a missing bed |
| COFFIN-6 | A human can open a coffin but not sleep in it |
| COFFIN-7 | A closed coffin shades the vampire inside from the sun, smothers any flames they carry in, and hides them from view. An open one does none of these |
| COFFIN-8 | Vampire players never count toward the bed share, and humans never count toward the coffin share, so neither side blocks the other's sleep |
| COFFIN-9 | Any player can sneak and use an occupied coffin with an empty hand to open or shut its lid. An open lid shows the vampire inside and no longer shades them, so direct sun reaches them (SUN-1). The vampire, player or mob, sleeps on until hurt. Using it without sneaking does not open it (COFFIN-1), and a second vampire cannot lie down in it (CS-2) |
| COFFIN-10 | A coffin placed while sneaking stands upright, two cells tall, its back to the block behind and its lid facing the player who placed it. A standing coffin follows every coffin rule, a vampire sleeps in it standing, and one who respawns at it stands on the floor in front of it |
| COFFIN-11 | A vampire asleep in a coffin, player or mob, lies or stands with arms crossed over the chest |
| COFFIN-12 | A vampire waking in a flat coffin rises rigid, pivoting up from the heels until upright, then steps off. One waking in a standing coffin steps forward out of it. It cannot move until it stands clear, damage does not cancel the rise, and it always ends on a safe free cell next to the coffin |

**Enforcement:** server.

### 5.15 Furnish And Dress

**Actor:** Any player.

| ID | Requirement |
|---|---|
| DECOR-1 | Gothic furniture: candelabras, chandeliers, velvet drapes, stained glass, ornate chairs and tables. Drapes open and close as a whole column, a chair seats one player at a time, and stained glass needs Silk Touch like vanilla glass |
| DECOR-2 | Gothic building blocks in stone and dark wood: gothic stone bricks in plain, cracked and mossy forms with stairs, slabs and walls, carved gothic stone, and ebony planks with stairs, slabs and a fence |
| DECOR-3 | Blood decor: blood fountains, blood pools, placeable splatter decals and chalices |
| DECOR-4 | Hunter decor: weapon racks, wanted posters, trophy mounts and crucifixes |
| DECOR-5 | Coffins in several woods: oak, spruce, dark oak, mangrove, cherry and crimson, each drawn with that wood's own planks, and each able to lie flat or stand (COFFIN-10) |
| DECOR-6 | Candles, candelabras and chandeliers light the room. Flint and steel or a fire charge lights them and an empty hand puts them out, and every light, douse, opening and closing plays its own animation |
| DECOR-7 | A gramophone, crafted from a jukebox, gold ingots and ebony planks and facing the player who placed it, plays any music disc as a jukebox does, with the same hopper, redstone and comparator behaviour, and animates while it plays |
| DECOR-8 | Gothic music discs, one per track, play in a gramophone or a jukebox. They are found only in vampire structures: in their loot and in their gramophones (HOME-20) |
| WEAR-1 | Capes and gothic clothing from several centuries, with at least two pieces for each armour slot, enough to dress at least 10 distinct looks. Each piece has a male and a female cut, and is drawn in the cut that fits its wearer: the female cut on a slim-armed player model, the male cut on a wide one |
| WEAR-2 | Clothing is worn in the armour slots in place of armour, protects and wears out like leather, and is mended with wool, or with leather for shoes and boots. It is drawn as an outer layer over the body, so a coat or cape hangs over any armour worn in the other slots, and capes and coat tails sway as the wearer walks |
| WEAR-3 | Clothing takes the vanilla armour enchantments for its slot and four of the mod's own, which come from the enchanting table, books on an anvil, librarians, loot and the gear mobs spawn with, like any vanilla enchantment that is not a treasure: Nocturne on headwear sees in the dark at night, Batwing on chest pieces falls further before taking damage, Gliding Step on legwear steps up a full block, and Hush on shoes and boots sneaks faster |
| WEAR-4 | Clothing whose colour is part of the look is dyed like leather, in a crafting grid with dyes, and a water cauldron washes the dye out. Only the cloth takes the dye: trims, buttons and buckles keep their own colours |
| WEAR-5 | Every outfit a vampire mob wears is made of clothing a player can craft |

**Enforcement:** server.

### 5.16 See Blood

**Actor:** Any player.

| ID | Requirement |
|---|---|
| GORE-1 | Bites, stakes and silver hits spray blood. Blood leaves splatter on nearby blocks, drawn by the client, that fades with time |
| GORE-2 | A drained victim and a staked vampire have their own death effects |
| GORE-3 | A client option sets gore, including vomit particles (FEED-3), to Full, Reduced or Off. Full is the default. Vomit particles also follow the vanilla Particles setting, and none show on Minimal |
| GORE-4 | Gore is drawn by the client only. The setting never changes what happens in the world |

**Enforcement:** server decides what happens. The client decides how much blood to draw.

### 5.17 Pick A Side

**Actor:** Vampire, human.

| ID | Requirement |
|---|---|
| FACT-1 | Every player is a vampire or a human. Humans earn hunter skills; vampires earn bloodline skills |
| FACT-2 | Combat between players follows the server's PvP setting. With PvP on, vampires and humans can fight and bite |
| FACT-3 | A player's faction is shown in the player list only if the server turns it on. It is off by default |
| FACT-4 | Otherwise, vampires, humans and sick players are told apart only by what other players can see: eyes, being weakened and effects |

**Enforcement:** server.

### 5.18 Configure The Mod

**Actor:** Server operator, pack author.

| ID | Requirement |
|---|---|
| CFG-1 | Every server option can be set with `/nightborn`, by an operator of level 2 or above |
| CFG-2 | Settings are saved per world as JSON in the world save directory |
| CFG-3 | A missing file gives defaults. A broken file gives defaults, is logged and is kept |
| CFG-4 | Out-of-range values are clamped on load |
| CFG-5 | Options cover turn chances, sickness length, sun damage, blood drain and values, garlic and crucifix ranges, skill point rates, the staking penalty, stake and silver damage, the downed and escape times, the damage from vomiting food or potions, the blood drain while levitating, how fast starving weakens and hurts and how much it takes from night gifts and bare hands, the coffin sleep share, hardcore vampire deaths, spawn and structure rates, vampire mob hunger threshold and blood drain, how often vampire mobs rise to levitate and land, feeding range and chase range, how often structure gramophones play, the rewards for older vampires and the Eldest, and whether players can turn players., whether Nocturne works and how strong Batwing, Gliding Step and Hush are |
| CFG-6 | A settings screen through Mod Menu shows the same server options and shares their validation with the commands |
| CFG-7 | An option that only changes what one player sees, such as gore, is a client setting saved on that player's device. Every player can change it on the settings screen, whatever their operator level |
| CFG-8 | Operators can turn, cure, set points and reset the bloodlines of any player |

**Enforcement:** server.

### 5.19 Work Through The Advancements

**Actor:** Any player.

The mod has its own tab. It branches early into three paths, Vampire, Hunter and World, and each can be followed in any order.

| ID | Advancement | Earned by |
|---|---|---|
| ADV-1 | Nightborn | Granted on joining a world: the root of the tab |
| ADV-2 | Love Bites | Getting bitten |
| ADV-3 | No Reflection On You | Turning |
| ADV-4 | First Blood | Drinking from a living creature |
| ADV-5 | Fresh Off The Neck | Drinking from a villager |
| ADV-6 | Bad Taste | Feeding on a creature in front of a villager |
| ADV-7 | Sun Dried | Burning in the sun and living |
| ADV-8 | Fly By Night | Taking bat form |
| ADV-9 | Strings Attached | Being weakened by 3 Garlic Strings at once |
| ADV-10 | Day Shift | Skipping the day in a coffin |
| ADV-11 | Old Blood | Choosing a second bloodline |
| ADV-12 | Good Help Is Hard To Find | Hiring a human familiar |
| ADV-13 | Stake Holder | Staking a vampire |
| ADV-14 | Holy Moly | Hitting a vampire with holy water |
| ADV-15 | Silver Lining | Crafting a silver weapon |
| ADV-16 | Garlic Breath | Carrying garlic into a vampire structure |
| ADV-17 | Close Call | Curing bite sickness |
| ADV-18 | House Call | Entering a vampire structure |
| ADV-19 | Lord Of The Manner | Defeating a vampire lord |
| ADV-20 | Dressed To Kill | Wearing a full set of gothic clothing |

| ID | Requirement |
|---|---|
| ADV-21 | The tab is a tree, not a line. Love Bites, the hunter advancements (ADV-13 to ADV-16), House Call and Dressed To Kill each branch from the root and can be earned in any order |
| ADV-22 | Close Call and No Reflection On You branch from Love Bites. The other vampire advancements (ADV-4 to ADV-12) branch from No Reflection On You, in any order. Lord Of The Manner branches from House Call |
| ADV-23 | Bad Taste and Good Help Is Hard To Find stay hidden until earned |
| ADV-24 | Lord Of The Manner and Strings Attached are challenges. No Reflection On You, Old Blood and Close Call are goals. Goals give 50 experience and challenges give 100 |
| ADV-25 | An advancement earned in steps shows its progress |
| ADV-26 | Icons use the mod's own items where one fits |
| ADV-27 | Names may be tuned, but each stays short, clever and in plain English |

**Enforcement:** server.

### 5.20 Summon The Eldest

**Actor:** Human, hunter.

The Eldest is the oldest vampire there is, with every power of every bloodline. It appears only when the ritual calls it. A hunter who has raided every kind of vampire structure brings one relic from each to a ritual altar at night, and the Eldest comes.

| ID | Requirement |
|---|---|
| RITE-1 | Each vampire structure type has a relic of its own: a castle, a crypt, a ruined manor and an underground lair. It has a chance to be found in that structure's loot or dropped by its older vampires |
| RITE-2 | A ritual altar is crafted. Placing one relic of every type on it and using it at night starts the ritual, with a build-up everyone near can see and hear |
| RITE-3 | The ritual summons the Eldest beside the altar: a vampire with every skill in the vampire tree, all three bloodlines at once, using every power as HOME-16 says. It has a boss bar, and is a boss for MIND-4. Its health grows with the number of players near |
| RITE-4 | The Eldest is downed and escapes like any vampire, and only a stake or the sun ends it (CORE-7, CORE-8). Its bar shows when it is downed or escaping |
| RITE-5 | The ritual uses up the relics. The altar can be used again with a new set, so the fight can be repeated |
| RITE-6 | Ending the Eldest gives a large number of hunter points to every human who took part in the fight, and loot found nowhere else |
| RITE-7 | Only a human can start the ritual. The Eldest attacks humans, and ignores vampire players unless they attack it (HOME-11). Vampire players can fight on either side as the PvP setting allows (FACT-2) |
| RITE-8 | The Eldest is not the castle's vampire lord (HOME-7). It appears only through the ritual |

**Not supported:** summoning the Eldest any other way.

**Enforcement:** server.

## 6. Permissions

A modified client is assumed. Hiding a control is never the enforcement of a rule. Coffins, decor and structure blocks have no owner, as a placed bed has none. Thralls and familiars have one owner.

### Capability Matrix

| Capability | Human | Vampire | Owner of a thrall or familiar | Operator |
|---|---|---|---|---|
| Bite a creature | No | Yes | Yes | Yes, as a vampire |
| Bite a player | No | Only with PvP on | Only with PvP on | Only with PvP on |
| Earn and spend hunter points | Yes | No | No | Through CFG-8 |
| Earn and spend vampire points | No | Yes | Yes | Through CFG-8 |
| Sleep in a coffin | No | Yes | Yes | Yes, as a vampire |
| Break a coffin | Yes | Yes | Yes | Yes |
| Open an empty coffin | Yes | Yes | Yes | Yes |
| Open an occupied coffin | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand |
| Start the ritual | Yes | No | No | Yes, as a human |
| Command a thrall or familiar | No | Only their own | Only their own | No |
| Hurt a thrall or familiar | Yes | Yes | Yes | Yes |
| Trade or take contracts at a hunter camp | Yes | No | No | Yes, as a human |
| Turn, cure or reset a player | No | No | No | Yes |
| Change server settings | No | No | No | Yes |

### Requirements

| ID | Requirement |
|---|---|
| PERM-1 | Every action in the matrix is checked on the server |
| PERM-2 | A skill purchase is checked on the server for points, order and bloodline limit |
| PERM-3 | A thrall or familiar whose owner leaves the server keeps its owner. An operator can release it |
| PERM-4 | Operator actions on another player are logged |

## 7. Client And Server

There is no singleplayer mode in this mod. A single-player world is an integrated server with one client, on the same code path as a dedicated server. No requirement here depends on player count.

### The Boundary

| Server owns | Client does |
|---|---|
| Faction, sickness, blood, skills and bloodlines | Shows the blood bar, power bar and skill screen |
| Feeding, resisting and draining | Sends the bite request and plays the effects |
| Sun, weakness and their sources | Draws smoke, fire and the weakness sign |
| Bat form | Draws the bat and the poof |
| Thralls, familiars and loyalty | Draws them |
| Household behaviour and structures | Draws fog and plays ambience |
| Who is on which map | Draws the map it is sent |
| Gore events | Decides how much gore to draw (GORE-3) |
| Coffin lids and who sleeps inside, levitation, vomiting, vampire mob powers, the ritual and the Eldest | Draws the lids, sleepers, poses, tells, vomit and the ritual build-up, with vomit scaled by GORE-3 |

### Concurrency

| ID | Requirement |
|---|---|
| CS-1 | Two vampires feeding on one victim share its blood. Neither can take more than it has |
| CS-2 | Two players using one coffin: the first lies down, the second is refused |
| CS-3 | A victim drained and killed in the same tick by a vampire and another attacker dies once and drops loot once |
| CS-4 | Two players filling Blood Bottles from one victim never create blood it did not have |
| CS-5 | A player cured and turned in the same tick ends in one defined state: the cure wins if it landed before the sickness ran out |

### Per-Player State

| State | Scope |
|---|---|
| Faction, sickness, blood, skills, bloodlines and cooldowns | That player, synced only to them, except what others can see (CORE-4, CORE-8, WEAK-8, FACT-3) |
| Coffin respawn point | That player |
| Gore setting | That player's device |

### Side Separation

| ID | Requirement |
|---|---|
| CS-6 | A dedicated server starts without loading a client class |
| CS-7 | Optional integrations load only when the other mod is present |

## 8. Non-Functional Requirements

### Safety

| ID | Requirement |
|---|---|
| SAFE-1 | No game mechanic duplicates or deletes any item the mod handles. Bottling blood, contracts and trades conserve items |
| SAFE-2 | Blood is never created except by feeding (FEED-4), turning (TURN-5) and respawning, as the persistence grid says |
| SAFE-3 | A bad config file never stops a server starting (CFG-3, CFG-4) |
| SAFE-4 | Every payload is size-limited and checked, including skill purchases and familiar orders |
| SAFE-5 | Skill points are never created except by FEED-12, HUNT-1, HUNT-2, RITE-6 and CFG-8 |

### Performance

| ID | Requirement |
|---|---|
| PERF-1 | A human with no sickness, holding no crucifix and not tracking costs nothing per tick beyond vanilla |
| PERF-2 | Garlic and crucifix checks look only near vampires, never across the world |
| PERF-3 | Crucifix shapes are found when blocks change, not every tick |
| PERF-4 | Fog and ambience cost nothing outside the range of a vampire structure |
| PERF-5 | A household's routine costs nothing while its chunks are unloaded |
| PERF-6 | Structure placement never loads extra chunks |

### Persistence

| What | Chunk unload | World reload | Death | Disconnect | Power loss |
|---|---|---|---|---|---|
| Faction | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Bite sickness and days left | not applicable | kept | kept | kept, paused while offline | kept to the last save |
| Downed or escaping, and the time left | kept, for a mob | kept | cleared | kept, so leaving does not escape it | kept to the last save |
| Blood | not applicable | kept | refilled, as hunger is on respawn | kept | kept to the last save |
| Skills and bloodlines | not applicable | kept | kept, except SKILL-6 and CORE-10 | kept | kept to the last save |
| Hunter tree set aside on turning | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Coffin respawn | kept | kept | kept, except CORE-10 | kept | kept to the last save |
| Thralls and familiars | kept with their chunk | kept | kept, waiting (MIND-8), except CORE-10 | kept | kept to the last save |
| Human familiar loyalty | kept | kept | kept | kept | kept to the last save |
| Household members and state, including members out feeding | kept | kept | not applicable | not applicable | kept to the last save |
| A vampire mob's stage, skills, look, size, blood and hunger | kept | kept | not applicable | not applicable | kept to the last save |
| Relics placed on a ritual altar | kept | kept | not applicable | not applicable | kept to the last save |
| A summoned Eldest and its fight | kept with its chunk | kept | not applicable | not applicable | kept to the last save |
| Contracts in progress | not applicable | kept | kept | kept | kept to the last save |
| Settings | not applicable | kept | not applicable | not applicable | written atomically |

### Look And Sound

| ID | Requirement |
|---|---|
| LOOK-1 | Interiors are candlelit: warm amber light against deep shadow |
| LOOK-2 | Places feel old, rich and lived in: decaying opulence, layered with portraits, velvet, dusty books, taxidermy and tarnished gold |
| LOOK-3 | Colours are muted jewel tones: burgundy, emerald, black and gold |
| LOOK-4 | Each vampire dresses in the era they were turned in, so a household mixes centuries |
| LOOK-5 | Every model and animation is hand-made to a premium bar and judged against the best Minecraft art. Every mob is animated at rest, moving and attacking, and every vampire while feeding |
| LOOK-6 | Music is baroque and gothic: harpsichord, choir and brooding strings |
| LOOK-7 | Vampire structures sound like old houses: creaks, settling, drips, guttering candles, wind in the halls and distant bells |
| LOOK-8 | Blocks and items look like vanilla ones wherever they are held or shown: GUI, first and third person, item frames and the ground |
| LOOK-9 | Sounds use vanilla first and real recordings next. Creature voices are never synthesised |

### Accessibility

| ID | Requirement |
|---|---|
| ACC-1 | No state relies on colour alone: weakness, sickness and blood quality each have a shape or icon as well |
| ACC-2 | No meaning relies on particles alone. Sun damage, bat form, weakness and vomiting all have sound and an on-screen sign |
| ACC-3 | Gore can be reduced or turned off (GORE-3) |
| ACC-4 | Fog never hides a threat completely within 8 blocks |
| ACC-5 | Every string is translated into every shipped language |

### Compatibility

| ID | Requirement |
|---|---|
| COMPAT-1 | Minecraft 1.21.1, Java 21, Fabric Loader 0.16.5 or later, Fabric API 0.116.12+1.21.1 or later |
| COMPAT-2 | Mod Menu, JEI and EMI are optional. Without them, the mod works fully |
| COMPAT-3 | Nightborn needs no other mod in the family |
| COMPAT-4 | Vanilla undead behave as they do without the mod, except CORE-9, and beds and night sleeping work as normal for humans |
| COMPAT-5 | Feeding lists, crucifix materials, the foods and drinks a vampire can keep down, structures, loot, relic chances, trades, and vampire mob stages, skills, looks, sizes, builds and outfits can be changed by data packs |
| COMPAT-6 | Shipped languages: English, German, Spanish, French, Brazilian Portuguese, Russian and Simplified Chinese |
| COMPAT-7 | Every declared dependency is used |

## 9. Release Criteria

- Every use case's requirements hold on a dedicated server with two clients and on an integrated server with one client, through the same code.
- A human can be bitten, get sick, and either cure it or turn, without outside help.
- A vampire can feed, survive a full day in a coffin and buy skills in two bloodlines.
- A human can find a vampire structure, down a vampire with hunter gear and stake it.
- A vampire dies in the sun, and gets up and escapes after being downed by anything else.
- No item, blood or skill point can be duplicated or lost by any mechanic the mod adds (SAFE-1, SAFE-2).
- A dedicated server starts with no client classes loaded (CS-6).
- Every string is translated in every shipped language.
- Every server setting is reachable from the command line.
- A world from the previous release opens with every player's faction, blood and skills intact.

## 10. References

- **Progress:** the project board. This document holds no status.
- **Acceptance criteria:** each requirement's owning issue. This document does not repeat them.
- **Reasoning:** the decision records in [`docs/adr/`](adr/).
- **Engineering standards:** [`docs/standards.md`](standards.md).
- **Player-facing summary:** the [README](../README.md).
