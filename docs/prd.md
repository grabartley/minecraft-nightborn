# Nightborn: Product Requirements

## How To Read This Document

This document says what Nightborn is: what a player can do with it, who may do what, which side decides each rule, and what the mod deliberately does not do. Every requirement has an identifier such as `FEED-3`, so issues, commits and reviews can cite it.

It holds no status. Nothing here says whether a requirement is built, in progress or waiting. Progress lives on the project board.

It changes only when a requirement changes or turns out to be unclear. The reasoning behind architectural choices lives in the decision records in [`docs/adr/`](adr/). The engineering baseline lives in [`docs/standards.md`](standards.md). Numbers in this document are starting values, to be tuned in play and then held as requirements.

## 1. Product Summary

### The Problem

Minecraft has no vampires, and nothing that hunts them. Its undead are mindless mobs that burn at dawn. There is no way to become something that fears the sun, lives on blood and grows stronger with age, and no reason to fear the dark beyond a creeper. A player who wants a night that feels dangerous, or a server where players choose sides, has nothing to choose from.

### What The Mod Is

Nightborn adds vampires and the people who hunt them. A bite from a vampire mob or a vampire bat may start a sickness, and a vampire player chooses whether their bite does. If it is not cured in time you turn. As a vampire you live on blood, sleep through the day in a coffin, and nothing but direct sun or a stake can finish you. Anything else only brings you down. You grow through a skill tree and pick two of three bloodlines: Noble, Beast and Occult. Each gives powers the others do not, such as bat form, mesmerising, thralls and ghostly familiars. Humans can fight back. Garlic, crucifixes, holy water, silver and the stake all hurt vampires, and every human earns hunter skills by killing them. Vampires are rare. They live in households hidden in fog, in castles, crypts, ruined manors, deep lairs and, rarest of all, castles on the tops of mountains, and hunters keep camps of their own and send out parties that wander the land, camping a night or so before they move on. Each vampire is someone: a name and an epithet, a look, a voice, a stage of age and powers of its own. Hunters, familiars and thralls have names too. A hunter who gathers a relic from a castle, a crypt, a ruined manor and an underground lair can build an altar and summon the Eldest, a vampire with every power, again and again, for a chalice of blood each time. Each mountain castle holds an Eldest of its own, met only once. Vampires have a match of their own: kill enough hunters and the Grand Inquisitor, the head of their guild, comes for you by night, and a vampire who wants the fight can ring a bell to call him.

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
| Vampires live somewhere | Households with personalities in five kinds of structure, rare and hidden in fog. Vampires live there, go out to feed and come home by dawn (HOME-1 to HOME-23) |
| Vampires fight like players | Wild vampires grow in stages and use every power a player can, more often and more cleverly the older they are (HOME-15 to HOME-17) |
| The day belongs to vampires too | Coffins skip the day the way beds skip the night (COFFIN-1 to COFFIN-14) |
| An endgame for hunters | A repeatable ritual summons the Eldest, a boss with every vampire power, fought in three phases inside a sealed arena, and one waits once at the top of every mountain castle (RITE-1 to RITE-16, HOME-23) |
| An endgame for vampires | The Grand Inquisitor hunts down a vampire who has killed too many hunters, or comes when a bell is rung: a boss fought in three phases inside a sealed arena, with dawn as the clock (INQ-1 to INQ-16) |
| Help in the daylight | A human familiar runs errands and guards your coffin, and might betray you (FAM-1 to FAM-8) |
| Everyone has a name | Vampires, hunters, familiars and thralls each carry a name, shown above them and in chat. A vampire's ends in an epithet and takes the colour of its age (NAME-1 to NAME-10) |

### Non-Goals

- **Not a werewolf or general monster mod.** Vampires and hunters only.
- **No cure after turning.** A cure works only during the sickness.
- **No vampire that feeds on anything but blood.**
- **Not a claims or protection mod.** Garlic weakens vampires. It does not stop them breaking blocks. The one protected block is an assigned coffin (COFFIN-13).
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
| How do you turn? | A bite from a vampire mob or a vampire bat, or a vampire player's turning bite | TURN-1, TURN-8 |
| Can you be cured? | Only during the sickness | CURE-1 to CURE-3 |
| How bad is the sun? | Lethal. Direct sun sets a vampire on fire | SUN-1 |
| What replaces hunger? | Blood | FEED-1 |
| How do powers grow? | A skill tree with three bloodlines, of which you invest in two | LINE-1 to LINE-3 |
| Who is a hunter? | Any human who kills vampires | HUNT-1 |
| How does a vampire die? | Only to direct sun or a stake. Anything else brings it down, greatly weakened, until it recovers and escapes | CORE-7, CORE-8 |
| What happens on death? | Respawn at your assigned coffin, which death by the sun or a stake unassigns, sending you to world spawn. A stake costs skill progress. A server can make a vampire's death a full reset instead | COFFIN-4, COFFIN-5, COFFIN-13, SKILL-6, CORE-10 |
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
| Bite sickness | The state between a bite that starts it and turning (TURN-3) |
| Feeding bite | A vampire player's bite that only drinks (TURN-8) |
| Turning bite | A vampire player's bite that drinks and may start bite sickness (TURN-8) |
| Blood | What a vampire lives on, shown as the blood bar in place of hunger |
| Fresh blood | Blood drunk straight from a living creature |
| Bottled blood | Blood stored in a Blood Bottle, filled by a vampire as it feeds or by any player with a lancet (GEAR-10). It restores less than fresh blood |
| Blood quality | How much a creature's blood is worth: animal, then villager or illager, then player |
| Sip | Feeding that stops before the victim dies |
| Drain | Feeding until the victim dies |
| Resist | A victim breaking free of a bite |
| Weakened | The vampire debuff from garlic, crucifixes, holy water, starving, being downed and escaping: slower, weaker blows and no powers. It is not the vanilla Weakness effect |
| Skill point | Earned by a vampire through feeding and by ending the Grand Inquisitor, or by a hunter through killing vampires. Spent on a skill tree |
| Core | The vampire skills every vampire can take |
| Bloodline | One of three branches of the vampire tree: Noble, Beast or Occult |
| Hunter tree | The one skill tree every human can earn |
| Direct sun | Daylight bright enough to burn the undead on a vampire with open sky above it, not in rain, water or shade |
| Garlic String | A block of hung garlic that weakens vampires near it |
| Crucifix shape | A cross of crucifix-material blocks built in the world (see below) |
| Downed | A vampire brought to its last health by anything but direct sun or a stake. It lies helpless and greatly weakened until it recovers |
| Escaping | The minute after a downed vampire gets up, when only direct sun and a stake can hurt it and it stays weakened, so it can get away |
| Staked | Killed by a stake: a wooden or silver-tipped stake, or a stake bolt, hitting a downed vampire, or striking the blow that would bring a vampire down |
| Coffin | A block, lying flat or standing upright, that is a vampire's bed. A vampire's assigned coffin, assigned by sleeping in it (COFFIN-4), is their respawn point, and only its owner, a creative player or an operator can break it |
| Thrall | A villager a Noble vampire has bound to follow and fight for them |
| Ghostly familiar | A spirit an Occult vampire summons to fight and do tasks |
| Human familiar | A villager hired as a vampire's daytime servant |
| Loyalty | How a human familiar feels about its owner, from 0 to 100 |
| Household | The vampires who live in one vampire structure |
| Stage | How old and strong a vampire mob is: fledgling, adult, elder or ancient |
| Hungry | A vampire mob whose blood has fallen below its hunger threshold |
| Name pool | A list of given names a mob's name is drawn from. There are six: men's and women's for vampires, for hunters and for familiars |
| Epithet | The title after a vampire mob's given name, such as "the Relentless" |
| Relic | A rare item found in a castle, crypt, ruined manor or underground lair. One of each crafts the ritual altar |
| The Eldest | A boss vampire with every vampire skill (RITE-3, HOME-23) |
| The Grand Inquisitor | A boss hunter, the head of the guild, who hunts vampires down (INQ-5) |
| Notoriety | A hidden count of how much a vampire player has done against the hunters. Enough of it starts a hunt (INQ-1) |
| Guild Master's seal | Dropped by a Guild Master a player kills. Four craft the hunter's bell and one is used up at each ring (INQ-4) |
| Hunter's bell | A handheld bell a vampire rings to call the Grand Inquisitor (INQ-4) |
| Lancet | A tool any player uses to bottle a creature's blood, and a human their own (GEAR-10) |
| Vampire structure | A castle, mountain castle, crypt, ruined manor or underground lair, home to a household |
| Hunter camp | A generated camp of hunter mobs, home to a Guild Master |
| Hunter party | A Guild Master and 4 to 8 hunter mobs who travel together and keep no hunter camp |
| Temporary camp | The campfire and tents a hunter party pitches for one to three nights |
| Faction | Vampire or human. Every player is in exactly one |

### Who Can Be Fed On

| Creature | Can be fed on | Why not |
|---|---|---|
| Passive animals with blood (cows, pigs, sheep, horses and the like) | Yes, animal quality | |
| Villagers, wandering traders, illagers, witches | Yes, villager quality | |
| Human players and hunter mobs | Yes, player quality | |
| The Grand Inquisitor | Only once he is downed (INQ-13), player quality | |
| Vampire players and vampire mobs | No | Their blood is already dead |
| Undead, golems, slimes, blazes, arthropods, fish | No | No warm blood to drink |

The list is a tag, so a pack can change it.

### Crucifix Shape

A crucifix shape is a vertical column of at least 3 blocks with a single block on each side, one below the top, all from the `nightborn:crucifix_materials` tag. The tag starts with every plank, log and gold block, and any block in `c:storage_blocks/silver`. The shape can face any horizontal direction. The arms and the top are single blocks: another tag block beyond an arm, above or below an arm, or above the top means it is not a crucifix.

## 5. Use Cases

### 5.1 Get Turned

**Actor:** Human, sick player.

A vampire mob, a vampire bat or a vampire player bites a human. A mob's or a bat's bite may start the sickness. A vampire player chooses whether their bite can turn. A sick player sees a clear sign that something is wrong, which grows as the days pass. When the sickness runs out, the player collapses where they stand and wakes up a vampire.

| ID | Requirement |
|---|---|
| TURN-1 | A bite from a vampire mob, a vampire bat or a vampire player's turning bite (TURN-8) starts bite sickness on a human, with a chance set per biter. By default a turning bite starts it every time, a vampire mob's bite one time in four and a vampire bat's one time in ten, less against a human with bite resistance (HUNT-4). A vampire player's feeding bite never starts it |
| TURN-2 | A vampire player can bite a human player only where the server allows PvP |
| TURN-3 | Bite sickness lasts 3 in-game days by default, and gets worse in three equal stages over its length: hunger, then the vanilla Weakness effect, then slowness in daylight. The time already run counts against the current sickness length, so a changed length applies at once to a player already sick, and a player whose sickness has already run longer than the new length turns at once. Time skipped by sleeping counts toward it, whether beds skip the night or coffins skip the day (COFFIN-2) |
| TURN-4 | A sick player is shown the sickness and the days left, as a status effect and a sign on screen |
| TURN-5 | When the sickness runs out, the player turns. They keep their inventory, start with a full blood bar and get the vampire core |
| TURN-6 | A player who turns stops earning hunter skills, and their hunter tree is set aside. It comes back if an operator makes them human again |
| TURN-7 | Another bite during the sickness does not restart or lengthen it |
| TURN-8 | A vampire player's bite feeds by default. They switch it between feeding and turning with a key they can rebind. Each switch is confirmed on screen, and a sign with its own icon stays on screen while the turning bite is on (ACC-1). A turning bite feeds like any other bite (FEED-4 to FEED-12), and the bite's mode is fixed when it first draws blood. Where the server stops players turning players (CFG-5), a turning bite on a player feeds only. A vampire made human loses the choice, so a player who turns again starts with a feeding bite. Vampire mobs and bats do not choose |

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
| Sneak and hold use on a creature, empty hand | Bite and drink, as a feeding or turning bite (TURN-8) |
| Let go | Stop. The victim lives |
| Keep holding past the victim's last health | Drain. The victim dies |
| Hold an empty Glass Bottle in the off hand while feeding | Fill Blood Bottles instead of drinking |
| Drink a Blood Bottle | Restore bottled blood |

| ID | Requirement |
|---|---|
| FEED-1 | A vampire has a blood bar in place of hunger. It drains over time, by default one point every 5 minutes at rest, so a full bar lasts several in-game days and a vampire does not have to feed every day, and faster when the vampire runs, fights or uses powers |
| FEED-2 | An empty blood bar starves the vampire (FEED-14). While the bar is above half, the vampire heals slowly over time, one health every 10 seconds, and healing costs no blood |
| FEED-3 | Normal food, potions and milk give a vampire nothing. A vampire who eats or drinks one is hurt and vomits it back up, with a short spell of Nausea, seen and heard by everyone near. Anything eaten or drunk counts, including other mods' food and drink, as does anything in the `nightborn:vampire_rejects` tag, which takes in the shared `c:foods` and `c:potions` tags. Blood Bottles, holy water and items in the `nightborn:vampire_consumables` tag are exempt |
| FEED-4 | Feeding drinks blood from the victim each second, and hurts it by the same amount. The victim is held in place for as long as the bite lasts: it cannot walk, be pushed away, ride away or leave, and it is free again a moment after the bite ends. Creatures that fly or swim also keep their height, listed in the `nightborn:holds_height` tag. The vampire plays a drinking animation for as long as the bite lasts, seen by everyone near and by the vampire in first person |
| FEED-5 | Letting go stops the bite and leaves the victim alive, with Weakness for a short time |
| FEED-6 | Holding past the victim's last health drains it, and it dies |
| FEED-7 | A victim resists with a chance that grows with its maximum health and falls as its health drops. A sleeping or mesmerised victim never resists |
| FEED-8 | A resisting victim breaks free of the hold, pushes the vampire back and, if it can attack, fights back |
| FEED-9 | Blood restores more by quality: animal least, then villager and illager, then player |
| FEED-10 | Fresh blood restores more than the same blood bottled |
| FEED-11 | A Blood Bottle keeps the quality of the creature it came from, and shows it |
| FEED-12 | Feeding earns vampire skill points from the blood drunk, weighed by its quality, with a bonus for draining. Filling Blood Bottles while feeding counts as feeding. Filling them with a lancet does not (GEAR-10) |
| FEED-13 | A villager that sees a vampire feed panics and flees, rings the village bell, and the village treats the vampire as it treats a player who hurt a villager, through vanilla gossip. A villager that sees nothing acts as normal |
| FEED-14 | A starving vampire grows more weakened over time, up to the cap (WEAK-9). Its powers and levitation stop, then its night speed and jump fade, and its bare-hand damage shrinks. After a while it loses health slowly, down to half a heart on any difficulty but Peaceful. Starving never downs or kills a vampire. Any blood stops it growing, and the weakness fades as the bar refills

**Not supported:** feeding on the list's excluded creatures; feeding through blocks; a human drinking blood.

**Enforcement:** server. The client sends the bite request; the server checks reach, the target and the faction.

### 5.4 Survive The Day

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| SUN-1 | A vampire in direct sun catches fire, as a zombie does, and burns while it stays there. Sun fire kills, downed or not. Fire that keeps burning after the vampire reaches shade only downs it, and cannot hurt it while it is escaping |
| SUN-2 | No armour, helmet or effect, Fire Resistance included, stops the burn. Shade, rain, water and a closed coffin that is not burning (COFFIN-7, COFFIN-14) do |
| SUN-3 | A burning vampire gives off dark, ashy smoke and a sizzling sound, on top of the fire |
| SUN-4 | Vampire mobs follow the same rules. As dawn nears, and whenever the sun reaches them, they head for the nearest shade they can walk to and keep going until they are under cover |

**Not supported:** a way to walk in direct sun.

**Enforcement:** server.

### 5.5 Feel Your Weaknesses

**Actor:** Vampire, hunter.

How weakened a vampire is scales with how strong the source is. By default each Garlic String in range weakens by 15%, up to 45% for three or more; touching or holding garlic weakens by 80%; a held crucifix weakens by 60%, and by 25% more of that for each point of the holder's faith; and a crucifix shape or a hung crucifix weakens by 40%. A crucifix held by a vampire wards no one. A vampire mob's only attack is its bite, so it cannot hurt a crucifix holder at all.

| ID | Requirement |
|---|---|
| WEAK-1 | Each Garlic String weakens vampires within 8 blocks. More strings in range weaken more, up to a cap |
| WEAK-2 | Touching garlic, by walking through garlic crops or brushing a Garlic String, or holding garlic, weakens a vampire greatly, far more than a string in range |
| WEAK-3 | Holding a Crucifix in either hand weakens and slows vampires within 6 blocks who can see it, players and mobs alike, and stops them biting the holder |
| WEAK-4 | A crucifix shape in the world, or a crucifix hung on a wall (DECOR-4), weakens vampires within 8 blocks who can see it |
| WEAK-5 | Holy water hurts vampires and weakens them for a short time, whether splashed, lingering, fired as a bolt or carried as a coating on a weapon or ammo (GEAR-9). It does nothing to anyone else |
| WEAK-6 | Silver weapons deal extra damage to vampires, and only to vampires |
| WEAK-7 | A stake deals extra damage to vampires, and stakes a vampire it downs or hits while downed, whoever holds it. It also hurts a vampire that is escaping |
| WEAK-8 | A weakened vampire shows it to everyone with a sign that is not colour alone, and stoops and staggers on foot |
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
| CORE-8 | A vampire brought to its last health by anything else is downed: it collapses, cannot move, attack, feed or use items or powers, and is weakened at the cap (WEAK-9). It takes no damage from anything but sun and a stake while downed. After 30 seconds by default it gets up with 20% of its health, wherever it lies. More damage does not reset the time. Everyone near can see and hear that it is downed. No mob or boss targets a downed vampire, and one already after it lets it go, except hunters, Guild Masters and the Grand Inquisitor, who still stake it. Players can stake it as before. It then escapes for 1 minute by default: it takes no damage but sun and a stake, stays weakened at the cap and can move freely. Vampire mobs flee while escaping |
| CORE-9 | Phantoms never spawn for a vampire player and never target a vampire, and time a vampire spends awake never counts toward them |
| CORE-10 | With the `hardcoreVampireDeath` server setting on, off by default, a vampire player who dies is reset like a hardcore death: they come back as a new human at world spawn with an empty inventory and ender chest, no experience, no skills, bloodlines or hunter tree, their coffin unassigned (COFFIN-13), and their thralls and familiars freed. Advancements are kept |
| CORE-11 | Every vampire, player or mob, can levitate. Players toggle it with a key they can rebind. A levitating vampire floats one block above the ground with its feet together, follows the ground up steps and slopes and over water, gliding up a rise of one block without catching while a fence or a higher wall still stops it, and sinks slowly over a drop without fall damage. A player holding sneak sinks over a drop about as fast as sneak lowers a creative flyer, keeping the float pose, and holding it over the ground lands them. It starts a sprint as it would on foot, and leans forward to move and further to sprint, never fully horizontal, with arms spreading as it speeds up, and lands with its own animation. It drains blood while active. It can still attack and use items while it floats, and others see the swing or the raised hand over the float. A weakened or downed vampire, or one in bat form, riding, sleeping, gliding or flying in creative, cannot levitate and lands at once. The server lands a player who stays well above that height for more than a moment, and ignores a toggle pressed again within half a second. Every landing the vampire did not choose sets it down gently, never with fall damage. Vampire mobs rise in a fight and now and then as they move about, as often as the server's options allow, and still find their way while levitating |

**Not supported:** a vampire hiding its eyes while feeding; levitating higher than one block.

**Enforcement:** server decides state. The client draws eyes and effects.

### 5.7 Grow Your Bloodlines

**Actor:** Vampire.

The vampire tree has the core and three bloodlines. A vampire player spends points in at most two bloodlines, so each vampire is a different kind of vampire. Vampire mobs are not bound by this (HOME-15).

| Bloodline | What it gives |
|---|---|
| **Noble** | Mesmerise, thralls and charm |
| **Beast** | Bat form, stronger bare hands, speed and a long leap |
| **Occult** | Ghostly familiars, Grave Fog, Shadow Step and Shadow Veil |

The core starts with Thick Blood (more health), Grave Strength (harder blows), Blood Rush, an active power that burns blood for a short burst of speed and strength, and Vanish, an active power that takes the vampire out of sight in a puff of smoke and brings it back a moment later a few blocks away. A player vanishes the way their movement keys point, or behind them when none is held. The vampire only ever lands on solid floor with room to stand, never in fluid or fire, and never through a wall. If the full distance is not safe it lands short, and if nowhere is safe the power is refused and costs nothing. Vanish uses the smoke and sound of bat form (BAT-1) where the vampire leaves and where it returns. The first skill of each bloodline chooses it and gives a small lasting gift: tougher skin for Noble, safer falls for Beast and longer reach for Occult.

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
| SKILL-1 | Vampire points and hunter points are separate, and each is spent only in its own tree. Vampire points come from FEED-12 and INQ-15, and hunter points from HUNT-1, HUNT-2 and RITE-6 |
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
| BAT-1 | A Beast vampire can turn into a bat and fly, and uses the same power again to turn back. A vampire mob takes bat form to close on a distant target and turns back beside it. A vampire, player or mob, that turns back in the air, by choice or forced, falls with normal gravity and takes fall damage counted from where it turned back (CORE-7, CORE-8) |
| BAT-2 | Turning into a bat, and back, gives a poof of the dark, ashy vampire smoke (SUN-3) and a sound, seen and heard by everyone near |
| BAT-3 | A bat cannot attack, break blocks, use items or other powers, open containers or feed, and turning into one ends a feed. It drains blood while flying, faster than levitating and faster still at a sprint, and an empty blood bar forces it back. A landed or hanging bat drains blood only as a resting vampire does |
| BAT-4 | Direct sun forces a vampire out of bat form, and no vampire can take bat form in it |
| BAT-5 | Turning back needs room to stand. Without it, the bat stays a bat |
| BAT-6 | A single heavy blow, as heavy as the server sets, being downed, holy water or garlic in range forces a vampire out of bat form |
| BAT-7 | A bat player flies with weight, not as in creative. They steer with the camera and fly where they look while holding forward, climbing as they look up and diving as they look down. Speed eases in and out, turning slows the bat at speed, and letting go of forward lets it coast to a slow hover. Sprint flies faster. A bat never takes fall damage. Third person shows the bat in every pose |
| BAT-8 | A bat player that flies into the ground lands on it, crawls slowly on its wings and takes off with jump. Using the underside of a block from beneath it hangs the bat upside down from that block, and the player's first-person view turns upside down with it. Jump or forward lets go. A hanging bat turning back drops as in BAT-1 |

**Enforcement:** server decides the form, landing, hanging and blood drain, and checks bat flight as it checks any player's movement. The client moves and draws the bat.

### 5.9 Bend Minds

**Actor:** Noble vampire.

| ID | Requirement |
|---|---|
| MIND-1 | Mesmerise is a power: the vampire stares at a mob, villager or player in sight and in range for a moment, and if the stare holds, the target is mesmerised. The blood and cooldown are spent when the stare begins, even if it breaks. A mesmerised mob stands still and cannot attack |
| MIND-2 | A mesmerised villager can be charmed by talking to it: for a while it trades with that vampire as with a hero of the village. Silver Tongue gives the charm, as the `nightborn:charm` attribute |
| MIND-3 | A mesmerised player is slowed and cannot hurt the vampire who mesmerised them for a short time. They are never frozen |
| MIND-4 | Bosses cannot be mesmerised. Hunter skills resist it (HUNT-4) |
| MIND-5 | Sipping from a mesmerised villager can make it a thrall. A thrall follows its owner and fights for them. Binding one opens the naming dialogue (NAME-6) |
| MIND-6 | A thrall can be told to follow, stay or guard a place. Its owner gives the order by using it with an empty hand, and a guard fights monsters near its post |
| MIND-7 | A vampire has a limit on thralls, raised by Noble skills: Blood Bond allows one and Dark Court two more, as the `nightborn:thrall_limit` attribute |
| MIND-8 | A thrall whose owner dies stays where it is until they return |

**Not supported:** mesmerising through walls.

**Enforcement:** server.

### 5.10 Keep Familiars

**Actor:** Owner.

| ID | Requirement |
|---|---|
| FAM-1 | An Occult vampire can summon ghostly familiars that fight for them, carry items and scout ahead. Summon Familiar allows one at a time and Spirit Pact one more, as the `nightborn:familiar_limit` attribute; summoning past the limit sends the oldest away. Each takes a name as it appears (NAME-7). The owner changes its task by using it with an empty hand, hands it items while it carries and takes them back by sneaking. A scout flies the way its owner looks and makes the creatures and people it passes glow for a while |
| FAM-2 | Ghostly familiars last a set time, cost blood to summon and pass through blocks only while scouting. One that fades or dies drops what it carried |
| FAM-3 | Any vampire can hire one grown villager as a human familiar by using a Familiar's Contract on it. The contract is crafted from paper, a feather and a Blood Bottle, and is used up. Hiring opens the naming dialogue (NAME-6). The familiar keeps the villager's biome look in a servant's livery but leaves its trade behind |
| FAM-4 | A human familiar works in daylight: it carries items between chests the owner marks, trades with villagers for the owner and guards the owner's assigned coffin. The owner sneaks and uses a chest or barrel they can open with an empty hand: the first one marked is the fetch chest, the next is the deliver chest, and using a marked chest again clears it. Which blocks count is the `nightborn:familiar_errand_chests` block tag. On each errand the familiar takes a stack from the fetch chest, buys what it can afford from the nearest villager, and puts everything into the deliver chest. Its trades use up the villager's offers but give no experience to anyone. It drives off monsters near the coffin, other than creepers and vampires, and at night it stays by the coffin |
| FAM-5 | A human familiar has loyalty. Good treatment raises it. Being fed on, hurt or left without pay lowers it. The owner pays wages by using emeralds on it, a set number of days per emerald, up to a set number of days ahead. Each paid day raises loyalty and each unpaid day lowers it |
| FAM-6 | A human familiar with low loyalty may secretly turn to the hunters, then attack its owner while they sleep. Its loyalty is never shown as a number: using it with an empty hand tells the owner whether it seems devoted, content, uneasy or resentful, and a turned familiar always seems content and still takes wages. A turned familiar stops running errands |
| FAM-7 | A familiar obeys only its owner |
| FAM-8 | A human familiar killed or turned against its owner frees the contract slot. A hired villager brings what it was carrying and puts it in the deliver chest on its first errand. A familiar drops whatever it carries when it dies or is released, and a released familiar becomes a villager again. An operator can release a familiar, or free a player's slot when the familiar itself is lost |

**Enforcement:** server.

### 5.11 Hunt Vampires

**Actor:** Human, hunter. Any player for the lancet (GEAR-10).

Every human is a hunter in waiting. Killing vampires earns hunter points.

The hunter tree starts with Tracking, an active power that points toward the nearest vampire for a short time and costs no blood. Below it, three paths: Bite Ward and Steeled Blood lower the chance a bite starts the sickness, Iron Will and Clear Mind resist mesmerise, and Faith and Devotion make crucifixes and holy water stronger. Each of these gifts is a player attribute (`nightborn:bite_resistance`, `nightborn:mesmerise_resistance` and `nightborn:faith`) that the skills raise, so the features that use them read one number and never the skills themselves. Faith and mesmerise resistance take effect once crucifixes, holy water and mesmerise exist. A hunter must be online when the sun finishes the vampire to be paid.

| ID | Requirement |
|---|---|
| HUNT-1 | Any human player earns hunter points by staking vampire mobs and vampire players, or by downing one that the sun finishes before its escape ends. Downing alone earns nothing. A vampire mob pays extra points for each stage it has above fledgling (HOME-15). No pledge is needed. The Eldest also pays hunters, as RITE-6 says |
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
| GEAR-6 | Holy water is brewed from a water bottle and a silver nugget, or a cleric villager blesses a water bottle for an emerald as a trade. It comes as a drink, a splash and a lingering potion, made from the drink with gunpowder and dragon's breath as vanilla potions are. Holy water cannot tip arrows as a potion does: the holy water bolt is its ammo, and arrows carry it only as a coating (GEAR-9) |
| GEAR-7 | A Crucifix is crafted from gold or silver and held in either hand |
| GEAR-8 | Garlic grows wild in plains and around villages, can be farmed, and is crafted into Garlic Strings |
| GEAR-9 | Holy water or garlic can be applied to any weapon or ammo in a crafting grid. The coating gives extra damage against vampires and weakens them briefly, always less than silver does, and does nothing to anyone else. On a weapon it lasts a set number of hits on vampires or a set time, whichever runs out first; on ammo it lasts until fired, and Infinity never saves a coated arrow. A coated item shows its own sheen, holy water or garlic, wherever it is drawn, and the sheen goes when the coating wears off |
| GEAR-10 | A lancet is crafted from an iron ingot and a stick. Any player, human or vampire, uses it on a living creature that can be fed on (see Who Can Be Fed On) while carrying an empty Glass Bottle. It hurts the creature, fills one Blood Bottle of that creature's quality (FEED-11) and wears a little. It never takes a creature's last health, so it cannot kill, and never draws blood the creature does not have (CS-4). The creature takes it as it takes any other blow. A human who uses it while sneaking, aimed at no creature, bleeds themself the same way for a bottle of player quality. It does not work on another player. Bottling with a lancet earns no skill points (FEED-12). How much it hurts is a server setting (CFG-5) |

**Enforcement:** server.

### 5.12 Visit A Hunter Camp

**Actor:** Human, vampire.

| ID | Requirement |
|---|---|
| GUILD-1 | Hunter camps generate rarely in the open country of the overworld (plains, meadows and savannas), each with a few hunter mobs and a Guild Master. Hunters and the Guild Master are villagers, not humans: the villager head with its long nose and heavy brow, in villager skin tones and proportions. Hunters are weathered men and women in long coats, wide hats and leather, in at least 4 looks each, and a woman hunter has a bust sized the same way as a vampire woman's (HOME-18). The Guild Master is an older, better dressed villager who wears a mark of rank, in at least 2 looks. At ease they all stand and walk with their arms folded as villagers do, and unfold them to trade or to fight |
| GUILD-2 | Hunter mobs attack vampire players and vampire mobs on sight with stakes, so a vampire they bring down is finished. Each hunter carries one kit: a stake in the hand, or a crossbow that fires stake bolts from a distance. A hunter whose kit has a crucifix holds it up at a vampire within its reach (WEAK-3) and puts it away after, and one whose kit has holy water throws a flask at a vampire too far to strike, then waits before throwing another. A hunter keeps its hands empty until it has a vampire to hunt, then draws its weapon, and puts it away a few seconds after the hunt ends. Hunters and the Guild Master speak with villager voices, lower for a man and higher for a woman: they mutter at rest, call out when they sight a vampire, grunt at each strike and cheer when one is staked. They leave humans alone and do not fight back when a human hits them, except as INQ-6 says. Hunters and the Guild Master of a hunter camp stay near it, and those of a hunter party stay with their party (GUILD-7) |
| GUILD-3 | The Guild Master sells hunter gear and writes contracts on paper, such as staking vampires, staking an elder, or clearing a vampire structure by staking the last of its household. His offers and contracts are data, he trades through the vanilla trading screen, and his stock renews each day |
| GUILD-4 | Contracts pay in emeralds, silver and gear, on top of the hunter points HUNT-2 gives. A contract is an item that keeps its own count. One stake marks one open contract. A vampire player counts as a fledgling against a contract that asks for an older vampire, and a vampire who lives alone is the last of its household. A fulfilled contract, in either hand, is handed to a Guild Master, who takes it and pays once |
| GUILD-5 | A vampire player cannot trade or take contracts with any Guild Master |
| GUILD-6 | Hunters also travel. A hunter party of 4 to 8 hunter mobs and one Guild Master appears now and then on the surface of the overworld near a player, the way a vanilla pillager patrol does. They are the same mobs as a hunter camp's, so they fight, trade, give contracts and are named as GUILD-2 to GUILD-5 and NAME-1 say. Only how they appear, and that they travel, is different. The biomes parties appear in are data |
| GUILD-7 | A party wanders one area looking for vampires, then moves on to a new area. It keeps together around its Guild Master. A party on the move that no player has been near for some time leaves the world, as a patrol does. A party in camp never does |
| GUILD-8 | As evening falls a party pitches a temporary camp where it is: a lit campfire and tents, each with a bed inside. It pitches only where every block of the site is natural ground, a tree or a plant, and may clear the trees and plants. Any other block on or beside the site rules it out, so a party never breaks, replaces or moves anything a player built. With no such ground in reach it walks on and goes without a camp that night. The tents, the layout of the camp, and the blocks that count as natural ground or as trees and plants are data |
| GUILD-9 | A party stays in one area for one night as a rule, never fewer than one and never more than three. By night some of the party sleep in the tents and the rest stay awake and search the ground around the camp. By day they wander the area and come back to the camp at evening. Nights are counted by the world's clock, so a party whose stay ran out while its chunks were unloaded breaks camp when they load |
| GUILD-10 | On its last morning the party puts out the fire, takes down its tents and beds and moves on. Only the unlit campfire stays behind. The tents and beds of a temporary camp drop nothing, whoever removes them (SAFE-1). A player whose spawn was a tent's bed loses it as with any broken bed. A camp whose party is all dead stands until someone breaks it |
| GUILD-11 | Now and then, at night, vampire bats fall on a pitched temporary camp and bite the hunters (HOME-10). The hunters awake fight them, the sleepers wake and join, and the fight lasts until the bats are dead or gone |

**Enforcement:** server.

### 5.13 Find A Vampire Household

**Actor:** Any player.

Vampires are rare. They live in households hidden in five kinds of structure, each placed to fit the land around it. Each structure has a heart, the room its household gathers in, and its fog is thickest there.

| Structure | Where | Household |
|---|---|---|
| Castle | Dark forests, old growth taiga, taiga, birch forests, swamps and mangrove swamps, never on mountains | A vampire lord and a court of four to six vampires behind a battlemented curtain wall with corner towers and a gatehouse. The lord's throne stands in the great hall of the central keep, the castle's heart, with a crypt, a bedchamber of coffins and a library behind it. Coffins, a blood fountain or a graveyard in the courtyard, and rich loot |
| Mountain castle | The highest ground of windswept hills, windswept gravelly hills, windswept forests, jagged peaks, stony peaks, frozen peaks and snowy slopes, by far the rarest | Twenty elder and ancient vampires and the Eldest in the highest room, and the best loot |
| Crypt | Under a parish church in its walled, raised churchyard, standing just outside a village on dry, level plains | A few vampires below a working church, reached only by the vestry stair |
| Ruined manor | Dark forests | A vampire family in an overgrown brick house with a corner turret or tower, behind railings and a carriage sweep. A stair hall rises two storeys to a gallery, with a drawing room, library, dining room and kitchen below, bedchambers with canopied coffins above, a study at the top of the turret, and a glasshouse falling to ruin at its side |
| Underground lair | Deep caves, between y -40 and -16, sealed in rock where no sun reaches | A den round a blood pool or a grotto with a spring and a coffin ledge, with more elders and ancients than a crypt. Its household cannot leave to hunt, and a player digs in to find it |

| ID | Requirement |
|---|---|
| HOME-1 | Each structure type generates rarely, in biomes that suit it, and blends into the land around it |
| HOME-2 | Each structure holds several vampires. A mountain castle holds twenty-one by default (HOME-22) |
| HOME-3 | Each type has several layouts, chosen at random |
| HOME-4 | Fog hangs around and inside every vampire structure, thicker near its heart |
| HOME-5 | A household feels lived in: its vampires have their own personality and hobbies, talk to and argue with each other, react to each other's deaths, follow a nightly routine and use the furniture of their home: chairs, candles, drapes, books and gramophones. They open wooden doors and close them behind them as villagers do, and leave iron doors shut. At dusk a household gathers at its heart, lights its candles and opens its drapes. Through the night each vampire sits, reads, talks with a housemate or wanders the house as its temperament leans, and a hot temper on either side turns talk into an argument. Before dawn they put out the candles and draw the drapes. A vampire whose housemate dies within reach mourns it for a while, even one asleep in its coffin |
| HOME-6 | Household vampires settle under a roof, never in the open sun, sleep in coffins by day and wake at dusk. Each structure holds the coffins (DECOR-5) that suit it, by default: stone brick and gothic stone brick coffins in crypts, deepslate brick and polished blackstone brick in underground lairs, dark oak and spruce in ruined manors, dark oak in a castle with gothic stone brick around its heart, and gothic stone brick and dark oak in a mountain castle. A data pack can change these (HOME-12) |
| HOME-7 | A castle's vampire lord is a boss with a health bar and its own powers: it calls every vampire of its household nearby onto its foe, and calls a swarm of vampire bats to its side. These powers are skills only vampire mobs can hold, never shown on a player's tree. The lord has a body of its own, settles in front of its throne and takes a stage above ancient that no other vampire rolls. It is downed and escapes like any vampire, its bar turns white while it is downed and purple while it escapes, and only a stake or the sun ends the fight |
| HOME-8 | Structures hold loot chests with gear, Blood Bottles, decor, gothic music discs (DECOR-8), relics (RITE-1) and rare items found nowhere else. A mountain castle holds no relic |
| HOME-9 | Vampire mobs live only in vampire structures, as members of a household. Each remembers its structure and its own coffin, which starts assigned to it (COFFIN-4), so it cannot be broken until that vampire dies to the sun or a stake. They never spawn at random anywhere else, and never despawn |
| HOME-10 | Vampire bats spawn rarely, and only underground where a vanilla bat could spawn, inside a vampire structure, or about a temporary camp they attack (GUILD-11). Under cover, or at night in the open, they swoop at warm-blooded creatures and bite them (TURN-1). A vampire bat is bigger and darker than a vanilla bat, with soot fur, burgundy wings and red eyes that always glow. It hangs wrapped in its wings, flies, swoops on its prey and bites |
| HOME-11 | Vampire mobs attack only when hungry or provoked. Being hurt, or a housemate being hurt nearby, provokes them, and a provoked vampire fights its attacker by day or night, fed or hungry, until it must escape the sun, falls back at low health, is downed, or the attacker leaves. A fed vampire may watch, follow or warn off a stranger, but does not attack. They never fight another vampire, whether a mob or a player, even one that hits them, and on Peaceful they never hunt players |
| HOME-12 | Structure layouts, the coffins each structure holds, loot, relic chances, structure spawn rates, the biomes each structure generates in, household sizes and the vampire that heads a household, vampire stages and their odds per structure type, the skills vampire mobs pick, their temperaments and hobbies, the blocks they read at and the lights they tend, their looks, sizes, builds and outfits, the name pools and epithets, the name colour of each stage and of the Eldest, and the biomes and structures vampire bats spawn in are data, changed by a data pack |
| HOME-13 | Vampire mobs have blood that drains over time. A hungry vampire may leave its structure at night to hunt humans, villagers and animals within a range of home. Not every vampire leaves every night, so a household is rarely empty, and one setting out sometimes brings one or two vampire bats. Each heads back once it has fed, or in time to reach its coffin by dawn. One caught out at dawn seeks shade (SUN-4). Their bites can start bite sickness (TURN-1). A lair household is sealed in rock and cannot leave (see the underground lair row) |
| HOME-14 | A provoked vampire gives up the chase past a range from its structure and goes home |
| HOME-15 | Each vampire mob has a stage, fledgling, adult, elder or ancient, which sets its health, speed, damage, how many skills it has and how much blood it can spend on powers. Its skills come from the vampire tree and, unlike a player's, may come from all three bloodlines. The older a vampire and the more skills it has, the rarer it is, the harder it fights, and the more hunter points (HUNT-1) and the better loot it gives. Only the Eldest has every skill (RITE-3) |
| HOME-16 | A vampire mob uses every power a player with the same skills could use, with the same blood costs, cooldowns and weaknesses, through the same code. It fights like a skilled player: closing fast, leaping, dodging, mesmerising and taking bat form, and pulling back to recover. One that knows Vanish may vanish to one side, or away, when its target swings at it or draws a ranged weapon on it. How often each stage does so is stage data (HOME-12). Older stages use more powers, and use them more cleverly |
| HOME-17 | Every power a vampire mob uses has a clear tell in sight and sound just before it lands, never colour alone (ACC-1). A power used as a dodge has no tell before it, since it answers the attacker's own move: its smoke and sound mark it |
| HOME-18 | Vampire mobs are men and women in at least 10 looks, from 1.5 to 2 blocks tall in slim, average and broad builds. Women have a bust whose size is rolled once when they spawn, between a smallest and a largest. Their hitbox, eye height and reach follow their size, except that the hitbox is never wider than 0.6 blocks, so every build fits through an open door. Outfits are chosen by era (LOOK-4). A vampire keeps its look and size for life, the same on every client, and an older stage reads as older |
| HOME-19 | Vampire mobs bare their fangs and hiss, with animation and sound, when they notice a threat, are provoked, face garlic or a crucifix, or are about to strike. They have their own idle, hurt, death, talk and argue voices, male and female, pitched by size |
| HOME-20 | Vampire structures hold gramophones loaded with gothic music discs (DECOR-8). Each starts its disc now and then while a player is near, and its disc can be taken. A gramophone's music never plays over other music the mod or the game plays for the same player |
| HOME-21 | A mountain castle is a vast stone castle that grows out of the summit of a large mountain. Sheer pale walls rise straight from the rock, with round and square towers under steep red-brown conical and hipped roofs, battlements, small deep windows and timbered upper floors. Courtyards, stairs and walkways climb from its gate to one tall keep, and in cold biomes snow lies on its roofs and ledges. It generates only on the highest ground of the biomes the table above lists |
| HOME-22 | A mountain castle's household is, by default, twenty elder and ancient vampires, about three elders to every ancient, and the Eldest (HOME-12) |
| HOME-23 | The highest room of a mountain castle's keep belongs to the Eldest of RITE-3, who fights as RITE-4 and RITE-9 to RITE-16 say and treats players as RITE-7 says. It sleeps by day in its own coffin in that room, keeps to that room at night, takes no part in the household's routine (HOME-5) and never leaves the castle to feed. Its arena is that room (RITE-9). Ending it needs no ritual and no relic, and gives what RITE-6 says. Once a stake or the sun ends it, that castle never has an Eldest again |

**Not supported:** a household rebuilding after it is cleared; vampire mobs spawning outside their structures.

**Enforcement:** server.

### 5.14 Sleep In A Coffin

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| COFFIN-1 | A coffin opens and closes, and a vampire can lie in it by day, from dawn to dusk whatever the weather, as a human lies in a bed by night. Using it lies a vampire down; a human, or a vampire who sneaks with an empty hand, opens or shuts the lid instead. The lid shuts over a vampire who lies down, opens when they rise, and stays shut while someone is inside unless a player opens it (COFFIN-9). It softens a fall like a bed, but nothing bounces off its lid. Two cells long, or two cells tall when standing (COFFIN-10), it is crafted like a bed from the planks or bricks it is made of (DECOR-5), with red wool for the lining and a gold nugget for the fittings |
| COFFIN-2 | Enough vampires sleeping in coffins skips the day to dusk, as beds skip the night to dawn |
| COFFIN-3 | The share of vampire players who must sleep is a server option, 100% by default. Only vampire players online in that dimension count. With none online, nothing skips the day |
| COFFIN-4 | A vampire, player or mob, has at most one assigned coffin. It is a player's respawn point, as a bed is, and a vampire mob's home coffin (HOME-9). Sleeping in an unassigned coffin assigns it to that vampire and frees the one they had before. A vampire can sleep in any coffin no one is lying in, including another vampire's assigned coffin, but that assigns nothing and leaves their respawn point where it was |
| COFFIN-5 | A vampire player with no assigned coffin, or whose assigned coffin is missing, respawns at world spawn, as with a missing bed. Since dying to the sun or a stake unassigns the coffin (COFFIN-13), the coffin respawn applies only after the void or an operator's `/kill` (CORE-7) |
| COFFIN-6 | A human can open a coffin but not sleep in it |
| COFFIN-7 | A closed coffin shades the vampire inside from the sun, smothers any flames they carry in, and hides them from view, unless the coffin itself is burning (COFFIN-14). An open one does none of these |
| COFFIN-8 | Vampire players never count toward the bed share, and humans never count toward the coffin share, so neither side blocks the other's sleep |
| COFFIN-9 | Any player can sneak and use an occupied coffin with an empty hand to open or shut its lid. An open lid shows the vampire inside and no longer shades them, so direct sun reaches them (SUN-1). The vampire, player or mob, sleeps on until hurt. Using it without sneaking does not open it (COFFIN-1), and a second vampire cannot lie down in it (CS-2) |
| COFFIN-10 | A coffin placed while sneaking stands upright, two cells tall, its back to the block behind and its lid facing the player who placed it. A standing coffin follows every coffin rule, a vampire sleeps in it standing, and one who respawns at it stands on the floor in front of it. Where a flat coffin needs room above it, a standing one needs the two cells in front of its lid clear |
| COFFIN-11 | A vampire asleep in a coffin, player or mob, lies or stands with arms crossed over the chest |
| COFFIN-12 | A vampire waking in a flat coffin rises rigid, pivoting up from the heels until upright in about a second, then steps off, a second and a half in all. One waking in a standing coffin steps forward out of it in a second. The lid opens as the rise starts. It cannot move until it stands clear, and damage, a second wake-up, leaving the coffin by hand and dusk do not cut the rise short. A vampire already on fire when woken, by the sun through an open lid or a burning coffin (COFFIN-14), skips the rise and wakes at once, as does a player teleported away. It always ends on a safe free cell next to the coffin, facing the way it rose |
| COFFIN-13 | Only the owner can break an assigned coffin. Other players, mobs, explosions and pistons leave it whole, though creative players and operators can still break it. A coffin is unassigned when its owner dies to the sun or a stake, is made human again (CFG-8), is reset by a hardcore death (CORE-10), or breaks it themselves |
| COFFIN-14 | A coffin made of a flammable material catches fire as that material does. While it burns it is drawn ablaze with the crackle of fire, and it wakes anyone inside and sets them on fire, open or shut. No vampire can lie down in a burning coffin. The fire burns out on its own after a while, and rain, water or a player putting it out ends it sooner. Fire never destroys a coffin, of any style, assigned or not |

**Enforcement:** server.

### 5.15 Furnish And Dress

**Actor:** Any player.

| ID | Requirement |
|---|---|
| DECOR-1 | Gothic furniture: candelabras, chandeliers, velvet drapes, stained glass, ornate chairs and tables, gilt portraits that hang on the side of a block and show one of three sitters set by where they hang, and stone gargoyles that face the player who placed them. Drapes open and close as a whole column, a chair seats one player at a time, and stained glass needs Silk Touch like vanilla glass |
| DECOR-2 | Gothic building blocks in stone and dark wood: gothic stone bricks in plain, cracked and mossy forms with stairs, slabs and walls, carved gothic stone, and ebony planks with stairs, slabs and a fence. Castle building blocks: pale limewashed bricks in plain and worn forms with stairs, slabs and walls, plaster, and red-brown clay roof tiles with stairs and slabs. Every one is crafted: limewashed bricks from stone bricks and bone meal, worn ones in a furnace, plaster from sand, clay and bone meal, and roof tiles from terracotta |
| DECOR-3 | Blood decor: blood fountains, blood pools, placeable splatter decals and chalices. A blood fountain flows until it gets a redstone signal and starts again when the signal ends, and every start and stop plays its own animation. A blood pool lies flat on a floor. Blood splatter hangs on any face of a block in one of four shapes, set by where it is placed. A gold chalice is crafted from gold ingots and holds up to 7 Blood Bottles of one quality. Pouring a Blood Bottle into it adds one, a bottle of another quality is refused, and a glass bottle takes one back out, keeping its quality (FEED-11). A vampire can drink one bottle's worth straight from a chalice with an empty hand, as from a bottle. Breaking a chalice spills what it holds, as breaking a cauldron does. Placed splatter follows the gore option (GORE-3): fresh on Full, dried and dark on Reduced, and not drawn on Off |
| DECOR-4 | Hunter decor: weapon racks, wanted posters, trophy mounts and crucifixes, each hung on the side of a block and dropped when that block goes. A weapon rack holds two weapons, one above the other, from the `nightborn:weapon_rack_holds` tag, which starts with swords, axes, bows, crossbows, tridents, maces and stakes. Using a weapon on a free hook hangs it there, using an empty hand on a hook takes back what hangs on it, and breaking the rack drops all it holds. A wanted poster shows one of three faces, set by where it hangs. A trophy mount shows a fanged vampire skull. A gold or silver Crucifix used on the side of a block hangs there and breaks back into the same Crucifix, and a hung crucifix wards like a crucifix shape (WEAK-4) |
| DECOR-5 | Coffins in two styles. Wooden coffins are narrow at the head and foot and widest at the shoulders, made from oak, spruce, dark oak, mangrove, cherry or crimson planks and drawn with that wood's planks. Their hinged lid creaks open and shut, and they sound and break like their wood, fastest with an axe, and catch fire as it does (COFFIN-14), so a crimson coffin never catches fire. Stone coffins are plain rectangular boxes made from stone bricks, deepslate bricks, polished blackstone bricks or gothic stone bricks (DECOR-2) and drawn with that stone. Their heavy lid slides open and shut with a grind of stone, and they sound and break like those bricks, dropping only when mined with a pickaxe. Both styles lie flat or stand (COFFIN-10) and follow every coffin rule in 5.14 |
| DECOR-6 | Candles, candelabras and chandeliers light the room. Flint and steel or a fire charge lights them and an empty hand puts them out, and every light, douse, opening and closing plays its own animation |
| DECOR-7 | A gramophone, crafted from a jukebox, gold ingots and ebony planks and facing the player who placed it, plays any music disc as a jukebox does, with the same hopper, redstone and comparator behaviour, and animates while it plays |
| DECOR-8 | Gothic music discs, one per track, play in a gramophone or a jukebox. They are found only in vampire structures: in their loot and in their gramophones (HOME-20) |
| WEAR-1 | Capes and gothic clothing from several centuries, with at least two pieces for each armour slot, enough to dress at least 10 distinct looks. Each piece has a male and a female cut, and is drawn in the cut that fits its wearer: the female cut on a slim-armed player model, the male cut on a wide one. Female chest pieces follow the bust at its size: the corset cups it, the frock coat opens over a cravat and the doublet's stomacher runs down over it. A vanilla chestplate flattens it |
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
| GORE-2 | A drained victim and a staked vampire have their own death effects. A staked vampire also gives off the dark, ashy smoke of a burning one (SUN-3) |
| GORE-3 | A client option sets gore, including vomit particles (FEED-3), blood spray and its splatter (GORE-1), and placed blood splatter (DECOR-3), to Full, Reduced or Off. Full is the default. Reduced draws fewer drops, and blood spray and splatter dried and dark. Vomit particles and blood spray also follow the vanilla Particles setting: fewer on Decreased, and none on Minimal |
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
| CFG-5 | Options cover turn chances, sickness length, sun damage, blood drain and values, garlic and crucifix ranges, skill point rates, the staking penalty, stake and silver damage, the damage, hits, time and weakening of each weapon coating, the strength, time, radius and cloud time of a garlic bomb, the downed and escape times, the damage from vomiting food or potions, the blood drain while levitating and in bat form, flying and at a sprint, how fast starving weakens and hurts and how much it takes from night gifts and bare hands, the coffin sleep share, the chance a sip binds a thrall and how long a charm lasts, hardcore vampire deaths, spawn rates, vampire structure and hunter camp rates, how often hunter parties appear, how many hunters they hold, how many nights they camp and how often vampire bats attack a camp, hunter points per contract, vampire mob hunger threshold and blood drain, how often hungry vampire mobs go out to feed, how far they roam about home and how far a hurt one calls its housemates, how often vampire mobs hiss, how often vampire mobs rise to levitate and land, how far and how hard vampire mobs leap, dash and dodge and when they dodge, how long and how far they fall back and when they come again, a human familiar's starting loyalty, wages, loyalty gains and losses, betrayal threshold and chance, and guard and errand ranges, feeding range and chase range, how often structure gramophones play and how near a player must be, how much a lancet hurts, the rewards for older vampires, the Eldest and the Grand Inquisitor, how long the ritual builds, how far it reaches and how much health each extra player gives the Eldest, the size of the Eldest's arenas, how long an empty arena takes to reset the fight, how long hunters have to stake it and how much health it rises with, how much life its blade and its full set steal and how much health the set adds, the notoriety that starts a hunt and how fast it fades, how many nights the Grand Inquisitor warns, how late in the night he may come, how long the bell's build-up lasts, how much health each extra vampire gives him, the size of his arena, how long vampires have to drain him, how long the drain takes and how much health he rises with, how much notoriety a vampire who dies in his fight loses and how long he waits in an empty arena, how much blood his greatsword and his full set give back, how much blood the set adds and how much it slows the drain, and whether players can turn players, whether Nocturne works and how strong Batwing, Gliding Step and Hush are |
| CFG-6 | A settings screen through Mod Menu shows the same server options and shares their validation with the commands |
| CFG-7 | An option that only changes what one player sees, such as gore, is a client setting saved on that player's device. Every player can change it on the settings screen, whatever their operator level |
| CFG-8 | Operators can turn, cure, set points and reset the bloodlines of any player, read and set any vampire's notoriety (INQ-1), and release any human familiar |

**Enforcement:** server.

### 5.19 Work Through The Advancements

**Actor:** Any player.

The mod has its own tab, lined with tufted burgundy leather and brass studs. It branches early into three paths, Vampire, Hunter and World, and each can be followed in any order.

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
| ADV-25 | An advancement earned in steps shows its progress: Strings Attached counts the strings weakening you at once, and Old Blood counts bloodlines chosen |
| ADV-26 | Icons use the mod's own items where one fits. A vampire lord counts for Lord Of The Manner by being in the `nightborn:vampire_lords` entity tag |
| ADV-27 | Names may be tuned, but each stays short, clever and in plain English |

**Enforcement:** server.

### 5.20 Summon The Eldest

**Actor:** Human, hunter, vampire.

The Eldest is among the oldest vampires there are, with every power of every bloodline. It appears when the ritual calls it, and once at the top of every mountain castle (HOME-23). A hunter who has raided a castle, a crypt, a ruined manor and an underground lair builds a ritual altar from one relic of each. A chalice of blood set on it at night calls the Eldest, as often as the hunter can fill it.

| ID | Requirement |
|---|---|
| RITE-1 | Each vampire structure type but the mountain castle has a relic of its own: a castle's signet, a crypt's reliquary, a ruined manor's locket and an underground lair's fang. It has a chance to be found in that structure's loot or dropped by the elders, ancients and lord who live there |
| RITE-2 | A ritual altar is crafted from one of each relic (RITE-1), which the recipe uses up, so one set of relics makes an altar that lasts. Each ritual costs blood. A gold chalice (DECOR-3) used on the altar is set on it, where it is filled and emptied as anywhere else, and sneaking with an empty hand takes it back. Using the altar empty-handed at night while its chalice holds all 7 bottles, of any one quality, starts the ritual, and at any other time a vampire who uses it drinks from the chalice (DECOR-3). The ritual starts with a build-up everyone near can see and hear: fog closes in, a bell tolls lower and lower, smoke gathers on the altar and the candles around it gutter one by one. How long the build-up lasts and how far it reaches are server settings (CFG-5) |
| RITE-3 | The ritual summons the Eldest beside the altar: a vampire, always a man, crowned and robed, with every skill in the vampire tree, all three bloodlines at once. It does not fight, move or live as other vampires do (HOME-15 to HOME-17): it fights only as RITE-9 to RITE-16 say. It is a boss for MIND-4. Its health grows with the number of players near when it appears, by a share for each player after the first that is a server setting (CFG-5) |
| RITE-4 | Only a stake or the sun ends the Eldest (CORE-7, CORE-8). It never escapes from its arena (RITE-9). When its last phase (RITE-10) is beaten it is downed, and its bar says so. Hunters then have a short time to stake it. If nobody does, it rises again with a share of its last phase's health. The time and the share are server settings (CFG-5) |
| RITE-5 | The ritual drinks the chalice dry when the Eldest appears and leaves the chalice on the altar. Breaking the altar during the build-up stops the ritual and drops the altar and its chalice, and the blood spills as from any broken chalice (DECOR-3). The altar can be used again each time its chalice is filled, so the fight can be repeated. An altar from an older world, crafted without relics, works the same way, and drops any relics left on it as items when its chunk first loads |
| RITE-6 | Ending the Eldest gives a large number of hunter points to every human who took part in the fight by striking it at least once, and loot found nowhere else (RITE-12). A summoned Eldest and a mountain castle's Eldest give the same points and roll one shared loot table |
| RITE-7 | The Eldest attacks humans on sight, hungry or not, and ignores vampire players unless they strike it (HOME-11). Vampire players can fight on either side as the PvP setting allows (FACT-2). A vampire can start the ritual too, and the Eldest still comes, but it turns on the vampire who called it: it says a line of outrage, shown in chat under its name to everyone near, and treats that vampire as it treats a human. Its lines are data and are translated (ACC-5) |
| RITE-8 | The Eldest is not the castle's vampire lord (HOME-7). It appears only through the ritual and in mountain castles (HOME-23) |
| RITE-9 | The Eldest fights in an arena. The ritual seals a circle round the altar with a wall of dark red fog that is never still: it ripples, beats like a heart and runs slowly through blood red, violet and near black, and it stirs harder while a fight is on. It changes smoothly and never flashes. A mountain castle's Eldest has the room it keeps to. The Eldest never leaves its arena, targets nobody outside it and cannot be hurt from outside it, nor by fire, lava, falls and the like, which a damage type tag lists. Its boss bar and its music (RITE-13) reach only players inside. The wall lets anyone walk in and lets nobody out: a player inside who crosses it, on foot, mounted, thrown or by any other means, is put back where they last stood inside. A player leaves only by dying or when the Eldest is ended. The wall holds only those the Eldest attacks on sight (RITE-7), so not vampires it ignores or players in creative mode, and it holds nobody while the Eldest sleeps. When no living player has been inside for a short time, however far away they went, the Eldest returns to its first phase at full health and forgets who fought it. The wall falls when the Eldest is ended. The size of each arena and the empty time are server settings (CFG-5) |
| RITE-10 | The fight has three phases, each with its own share of the Eldest's health, and the bar names the phase and shows what is left of it. No blow carries past the end of a phase, not even a stake. Only the sun does. At the end of a phase the Eldest steps back to where the fight began, roars under a bolt of lightning, throws back everyone close and cannot be hurt while it changes. It comes out of each change stronger: better armoured, harder hitting, faster and visibly larger. The Court is a duel on foot: chains of blows a shield stops, a lunge along a marked line, two lesser vampires called to its side for a while, and a gaze that roots one hunter to the spot for a few seconds unless they break its line of sight first. The Beast takes to the air and circles the arena out of a blade's reach, so hunters need bows and bolts, or something that weakens a vampire, which keeps it on the ground. It dives onto a marked spot, sends a swarm down a marked line, screeches a mark under every hunter, and after each dive stays on the ground for a moment, open to a blade, before it rises again. The Blood is the Eldest enraged: larger again, its eyes blazing, growling. It steps through shadow to strike a marked spot, leaves pools of blood that hurt to stand in, uses the gaze and the dive, and drains a hunter to heal itself until enough blows break the drain. In the Beast and the Blood it can also call a ward of vampire bats, each outlined so it can be found: while one lives the Eldest takes no harm. Keepers and called vampires go when their time is up, when the fight ends and when the world is loaded again. Each phase also has vampire powers among its attacks, announced like any other: the Court vanishes and quickens itself, the Beast quickens itself and looses bats, and the Blood vanishes, raises grave fog and summons a familiar. Phases, attacks and their numbers are data |
| RITE-11 | Every attack and every state of RITE-10 can be read at once. Each attack has a pose and a sound before it lands, and one that hits the ground has a mark on the floor that shows exactly where. The mark is drawn as a model, not as particles, so it shows on any particle setting (ACC-1). An attack hurts only those still in its mark, in its reach or in its line of sight when it lands. The bar turns blue and says the Eldest is warded while its keepers live, and green and says it is feeding while it drains. A guarded blow rings like a shield. Each phase also looks different at a glance. The Court stands and walks like a king with empty hands and its wings folded on its back. The Beast hunches, opens its wings as it changes, spreads them wide for that phase only and beats them when it flies. The Blood folds them away again as it changes, stands tall with eyes and veins that blaze, and draws the Blade of the Eldest, the same blade and the same crown, mantle, greaves and boots it can drop (RITE-12). It rises from the floor when the ritual summons it, roars and changes shape between phases, and has its own pose for a guard, a break away, taking off, landing and the moment its ward falls. Which clip, form and weapon each phase uses is data |
| RITE-12 | The Eldest's loot is one loot table. Every kill gives treasure and the ash of the Eldest, which turns a silver sword, axe or stake into ashen silver at a smithing table: tougher and sharper, and still silver to a vampire. Very rarely it drops the Blade of the Eldest, which gives back a share of each blow as health, or one of four pieces of its armour, as strong as netherite. Wearing all four with the blade in hand steals more life and adds health. Rarely it drops one of two armour trim templates. Now and then it drops its skull, a trophy block, or the music disc of its last phase. Once in a great while it drops a tame vampire bat, which circles its owner, cannot be hurt and returns to their pack when they use it while sneaking. The shares and the health are server settings (CFG-5) and the chances are loot table data |
| RITE-13 | The fight has its own music: one piece for the first two phases and another for the last. It plays only for players inside the arena while a fight is on, in place of any other music, and stops when they leave or the fight ends. While a fight is on, players inside also hear something now and then from a list each phase carries: a tolling bell, an organ, a scream, laughter, or a bolt of harmless lightning on the wall, whose thunder carries beyond it. These come well apart, so they unsettle without wearing thin, and how often is data |
| RITE-14 | Every rule of RITE-9 to RITE-13, RITE-15 and RITE-16 holds for both the summoned Eldest and a mountain castle's Eldest |
| RITE-15 | The Eldest defends itself. No single blow takes more than a set amount of its health, and that amount falls each phase. After a few blows in quick succession it guards the next one, takes nothing and throws the attacker back if they stand close. When it has taken enough damage in a short time it breaks away to open ground inside the arena. After a guard or a break away it answers at once with one of that phase's counter attacks that can reach its target. A blow from a hunter it is not facing may turn it on that hunter. It will not ride in a boat or a minecart. Every number is data |
| RITE-16 | The Eldest moves as a boss, not as a vampire. Each phase gives it a stance: a distance it keeps from its target. It closes on a target that is too far, backs off one that is too near and circles one in between, changing direction as it goes, and it stands still while an attack is announced or lands. The Court keeps to a blade's length, the Beast on the ground runs its target down, and the Blood keeps a few paces off, so hunters must come through its attacks to reach it. It never flees, retreats to heal, wanders, feeds or bites outside its attacks. Stances are data |

**Not supported:** the Eldest appearing any other way.

**Enforcement:** server.

### 5.21 Face The Grand Inquisitor

**Actor:** Vampire, human.

The Grand Inquisitor is the head of the hunters' guild: an old villager, a zealot, and the vampires' match for the Eldest. He comes for a vampire who has killed too many of his hunters, and a vampire who wants the fight can ring for him.

| ID | Requirement |
|---|---|
| INQ-1 | Every vampire player has a notoriety, which is never shown. It rises when they, their thrall or their familiar kill a hunter mob or a Guild Master, and rises more for the last of a hunter camp or a hunter party. Killing a player never raises it. It fades by a set amount at each dawn the vampire is online for. Once it passes a threshold the guild hunts that vampire, and the hunt is called off if it fades back below. What each kill adds is data, and the threshold and how fast it fades are server settings (CFG-5) |
| INQ-2 | A hunted vampire is warned for a few nights before he comes, 3 by default. Each dusk of the warning a bell tolls far off, nearer each night, and a line on screen tells the vampire the guild is coming, so the warning never rests on sound alone (ACC-2). A dusk the vampire is offline for does not count. How many nights is a server setting (CFG-5) |
| INQ-3 | At nightfall after the last warning he arrives alone on the surface of the overworld, a short way from the vampire, and walks them down. When he comes close the arena seals round where the vampire stands (INQ-7). He comes only for a vampire who is on the surface with open sky above them, so never underground and never under a roof, and only early in the night, before midnight by default, so there is a night to fight in (INQ-8). On a night the vampire gives him no such chance, or is offline, he waits and tries again the next nightfall while the hunt lasts. How late he may come is a server setting (CFG-5) |
| INQ-4 | A vampire can also ring for him. A Guild Master a player kills drops a Guild Master's seal. A hunter's bell is crafted once from a vanilla bell and four seals, and is never used up. Ringing it by hand early in the night (INQ-3), on the surface under open sky, uses up one more seal and starts a build-up everyone near can see and hear: bells answer from far off, nearer and nearer, and then he arrives for whoever rang as INQ-3 says, with no nights of warning. Without a seal, or rung at any other time or place, the bell only rings and nothing is used up. How long the build-up lasts is a server setting (CFG-5) |
| INQ-5 | The Grand Inquisitor is a boss for MIND-4: an old man, a villager as hunters are (GUILD-1), dressed and armed as his phase says (INQ-11) and speaking with a villager's voice (GUILD-2). Each hunt and each ring of the bell brings a new man with a new name (NAME-1, INQ-14). He cannot be bitten or bled with a lancet (GEAR-10) until he is downed (INQ-13). His health grows with the number of vampire players near when he appears, by a share for each after the first that is a server setting (CFG-5) |
| INQ-6 | He attacks every vampire player in his arena on sight, and ignores humans unless they strike him. Humans can fight on either side as the PvP setting allows (FACT-2). A human can ring the bell too, and he still comes, but he turns on the human who rang: he says a line of outrage, shown in chat under his name to everyone near, and treats that human as he treats a vampire. Hunters he calls against that human fight them too, GUILD-2 notwithstanding. He and his hunters attack a vampire mob inside the arena as any hunter does (GUILD-2), but the wall holds no mob and a mob does not keep the fight going. His lines are data and are translated (ACC-5) |
| INQ-7 | He fights in an arena. A circle seals where he catches his quarry, with a wall of pale holy light: white and pale gold, steady as a candle flame, rising and falling slowly as if it breathed and brighter while a fight is on. It changes smoothly and never flashes. The rules RITE-9 gives the Eldest's arena hold for his, with vampires in place of humans, except its rule for an empty arena, which INQ-14 replaces: he never leaves it while a fight is on, targets nobody outside it and cannot be hurt from outside it or by the damage types the tag lists, his boss bar and music (INQ-16) reach only players inside, and the wall lets anyone in and holds every player he attacks on sight (INQ-6) but those in creative mode. The wall gives no shade. It falls when he is ended or leaves (INQ-14). The size of the arena is a server setting (CFG-5) |
| INQ-8 | Dawn is the clock. At sunrise the sun reaches the arena as it reaches anywhere else and burns the vampires inside (SUN-1), and the wall still holds them until the fight ends (INQ-14) |
| INQ-9 | The fight has three phases, each with its own share of his health, and the bar names the phase and shows what is left of it. No blow carries past the end of a phase. At the end of a phase he steps back to where the fight began, raises his crucifix to a peal of bells and a burst of light that throws back everyone close, and cannot be hurt while he changes. He comes out of each change stronger: better armoured, harder hitting and faster. The Hunt is fought at a distance: volleys of stake bolts down marked lines, snares on marked spots that root a vampire for a few seconds unless they step off in time, and a garlic bomb onto a marked spot (GEAR-5). The Faith stands its ground: he holds up a great crucifix that weakens every vampire who can see it until they break its line of sight (WEAK-3), throws holy water onto marked spots, consecrates marked ground that goes on hurting and weakening those who stand on it, and kneels to pray, healing himself until enough blows break the prayer. The Pyre is the Inquisitor in wrath, with a silver greatsword: chains of blows, fire swept down a marked line that leaves the ground burning, and the false dawn, shafts of holy light on marked spots and lines that are direct sun to a vampire standing in them (SUN-1) while the rest of the arena stays dark. Phases, attacks and their numbers are data |
| INQ-10 | Through the Hunt and the Faith he calls hunters to his side at regular intervals, a few at a time, up to a limit at once. They are hunter mobs with the kits of GUILD-2, they fight beside him inside the arena, and they can be fed on as any hunter can. They drop nothing, add nothing to notoriety (INQ-1), and go when the fight ends and when the world is loaded again. He calls none in the Pyre, and those still standing fight on. How many, how often and the limit are data |
| INQ-11 | Every attack and every state can be read at once, as RITE-11 says for the Eldest: a pose and a sound before each attack, a mark drawn as a model for each one that hits the ground, and harm only to those still in its mark, reach or line of sight when it lands. The bar turns green and says he is at prayer while he heals. Each phase also looks different at a glance. The Hunt wears a long coat and a wide hat and carries a crossbow. The Faith bares his head and carries the great crucifix and a flask. The Pyre casts the crucifix aside and draws the greatsword, the same sword, hat, coat, greaves and boots he can drop (INQ-15). He has his own pose for arriving, each change of phase, a guard, a break away, kneeling in prayer, being downed and rising again. Which clip and weapon each phase uses is data |
| INQ-12 | He defends himself as RITE-15 says and moves as RITE-16 says, with his own numbers and stances. The Hunt keeps well back from his target, the Faith holds a few paces off, and the Pyre closes to a blade's length. Stances are data |
| INQ-13 | He dies like no hunter. When his last phase is beaten he is downed: he falls to his knees and his bar says so. Vampires then have a short time to feed on him, and a bite held until he is drained ends him. It is a feeding bite whatever the vampire chose (TURN-8), his blood is of player quality, and he never resists. If nobody drains him he rises again with a share of his last phase's health. A human he turned on (INQ-6) ends him with any blow while he is downed, which gives the loot of INQ-15, pays the vampires who struck him as INQ-15 says and the human nothing, and changes nobody's notoriety. The time, how long the drain takes and the share are server settings (CFG-5) |
| INQ-14 | Draining him sets the notoriety of every vampire who struck him back to zero and ends the hunt, so the fight can be earned again. A vampire who dies in the fight, staked or burned by the sun, loses a share of their notoriety, half by default, and a staked one pays the usual penalty as well (SKILL-6). The fight also ends when every player he attacks on sight (INQ-6) is dead. He then leaves, the wall falls and the hunt he came on is over: a vampire whose notoriety is still past the threshold is hunted afresh, with new nights of warning and a new man (INQ-5). If instead the arena empties while such a player still lives, such as his quarry leaving the server, he leaves after a short time. On a hunt, the hunt stays on and the same man comes again the next nightfall, at full health in his first phase. After a ring of the bell he does not come back and the seal is spent. The share and the time are server settings (CFG-5) |
| INQ-15 | Ending him gives a large number of vampire skill points to every vampire player who struck him at least once, and loot found nowhere else, from one loot table. Every kill gives treasure and the Inquisitor's mail, which lines a piece of gothic clothing (WEAR-1) at a smithing table: as strong as diamond, and keeping its look, dye and enchantments. Very rarely he drops the Inquisitor's Greatsword, which gives a vampire who wields it a share of each blow back as blood, or one of four pieces of his armour, the hat, coat, greaves and boots, as strong as netherite. A vampire wearing all four with the greatsword in hand gets more blood from each blow, holds more blood and loses it more slowly. Rarely he drops one of two armour trim templates. Now and then he drops his broken crucifix, a trophy block that wards no one, or the music disc of his last phase. Once in a great while he drops a tame hawk, which behaves as the Eldest's tame vampire bat does (RITE-12). The shares, the blood and the drain are server settings (CFG-5) and the chances are loot table data |
| INQ-16 | The fight has its own music: one piece for the Hunt and the Faith and another for the Pyre. It plays as RITE-13 says for the Eldest's, and players inside also hear something now and then from a list each phase carries: a tolling bell, a choir, a hunting horn, a hammer on a stake, or a prayer muttered close by. How often is data |

**Not supported:** a hunt for a vampire mob; a Grand Inquisitor in a hunter camp or a hunter party; turning him, binding him as a thrall or hiring him; seeing a notoriety as a number.

**Enforcement:** server.

### 5.22 Know Them By Name

**Actor:** Any player, owner.

Vampire mobs, hunters, Guild Masters, the Grand Inquisitor, familiars and thralls each have a name. Vampire bats and vanilla mobs do not.

| ID | Requirement |
|---|---|
| NAME-1 | Every vampire mob, hunter mob and Guild Master has a given name, drawn at random when it spawns. It keeps its name, and a vampire its epithet, until someone renames it (NAME-9). A man's comes from the men's pool for his kind and a woman's from the women's pool (HOME-18, GUILD-1, RITE-3). Vampires have 250 men's and 250 women's names and hunters have 250 of each, so 500 different vampire names and 500 different hunter names, each fitting its group and the mod's tone (LOOK-1 to LOOK-4). A Guild Master draws from the hunter pools, and a Grand Inquisitor from the hunter men's pool. No two members of one household, one camp or one hunter party share a given name while the pool allows it. A vampire, hunter or Guild Master from an older world that has no name is given one when it loads |
| NAME-2 | Every vampire mob but the Eldest has a name that ends in an epithet, drawn at random from a pool of 500 when it spawns, so it reads as a given name and a title. No two members of one household share an epithet while the pool allows it. The Eldest's epithet is always "the Eldest", which is not one of the 500. A Grand Inquisitor's name ends in his title, "the Grand Inquisitor", in the same way. Hunters, Guild Masters, familiars and thralls have no epithet |
| NAME-3 | A name is drawn above its bearer as a vanilla name tag is, without the player having to look straight at it, whenever the player has a clear line of sight to the bearer within vanilla's range. It is never drawn through blocks, so a name does not give away a vampire behind a wall or in a closed coffin, and it is hidden when vanilla would hide it, such as while the bearer is invisible. A vampire mob's name takes the colour of its stage, by default grey for a fledgling, white for an adult, gold for an elder, red for an ancient, dark purple for a vampire lord and dark red for the Eldest (HOME-12). A hunter's and a Guild Master's name is blue, and the Grand Inquisitor's is dark blue. A familiar's and a thrall's name is the normal name colour. The colour comes from what the bearer is and is not stored in the name, so a renamed mob keeps it. A boss bar shows its boss's name. Colour is never the only sign of a stage: an older stage also looks older (HOME-18, ACC-1) |
| NAME-4 | A name is stored as the entity's vanilla custom name, epithet included, so entity selectors such as `@e[name=...]`, `/data`, other commands and other mods read it as they read any named mob. A selector matches the English name |
| NAME-5 | Chat lines that refer to a named mob use its name, in its colour, as vanilla does for any named mob. A player's death message names the vampire or hunter that killed them, and a familiar's or thrall's death is told to its owner as a pet's is |
| NAME-6 | Hiring a human familiar (FAM-3) or binding a thrall (MIND-5) opens a naming dialogue for its owner, styled as the vampire skill screen is (LINE-7) and never a plain menu. Its name field starts filled with a name drawn at random from the familiar pools, 250 men's and 250 women's names, all different, picked without regard to gender. The owner keeps it or types any other name and confirms. Neither is ever without a name: it holds the suggested one from the moment it is hired or bound, so closing the dialogue or leaving the server keeps it |
| NAME-7 | A ghostly familiar takes a name at random from the familiar pools each time it is summoned, with no dialogue, since it is summoned often and fades (FAM-2) |
| NAME-8 | A name a player enters, in the dialogue or on a name tag, is trimmed of spaces at both ends, has control and formatting characters removed, and must then be 1 to 32 characters long. The dialogue will not confirm a name that fails and says why. The server applies the same rules again, and takes a name from the dialogue only from the owner, for their own familiar or thrall (SAFE-4, PERM-1). A name that fails on the server changes nothing |
| NAME-9 | A vanilla name tag renames any vampire mob, hunter, Guild Master, familiar or thrall, used by any player as on any vanilla mob. A tag whose name fails NAME-8 does nothing and is not used up. The new name replaces the whole name, epithet included. A released familiar keeps its name as a villager (FAM-8), and a released thrall keeps its name too |
| NAME-10 | Given names are proper names and are the same in every language. Each epithet is translated into every shipped language |

**Not supported:** a naming dialogue for a vampire mob, hunter or Guild Master; an epithet for anyone but a vampire mob, beyond the Grand Inquisitor's title; a filter on the words a player may use in a name.

**Enforcement:** server. The client draws the name tags and the dialogue, and sends the name the owner chose.

## 6. Permissions

A modified client is assumed. Hiding a control is never the enforcement of a rule. Decor and structure blocks have no owner, as a placed bed has none. A coffin has an owner only while it is assigned (COFFIN-13). Thralls and familiars have one owner.

### Capability Matrix

| Capability | Human | Vampire | Owner of a thrall or familiar | Operator |
|---|---|---|---|---|
| Bite a creature | No | Yes | Yes | Yes, as a vampire |
| Bite a player | No | Only with PvP on | Only with PvP on | Only with PvP on |
| Earn and spend hunter points | Yes | No | No | Through CFG-8 |
| Earn and spend vampire points | No | Yes | Yes | Through CFG-8 |
| Sleep in a coffin | No | Yes | Yes | Yes, as a vampire |
| Break an unassigned coffin | Yes | Yes | Yes | Yes |
| Break an assigned coffin | No | Only their own | Only their own | Yes |
| Set a flammable coffin alight | Yes | Yes | Yes | Yes |
| Open an empty coffin | Yes | Yes | Yes | Yes |
| Open an occupied coffin | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand | Yes, sneaking with an empty hand |
| Start the ritual | Yes | Yes, and the Eldest turns on them (RITE-7) | Yes, and the Eldest turns on them (RITE-7) | Yes, as a human or a vampire |
| Ring the hunter's bell for the Grand Inquisitor | Yes, and he turns on them (INQ-6) | Yes | Yes | Yes, as a human or a vampire |
| Bottle blood with a lancet | Yes | Yes | Yes | Yes |
| Command a thrall or familiar | No | Only their own | Only their own | No |
| Name a thrall or familiar in the naming dialogue | No | Only their own | Only their own | No |
| Rename a vampire mob, hunter, Guild Master, familiar or thrall with a name tag | Yes | Yes | Yes | Yes |
| Hurt a thrall or familiar | Yes | Yes | Yes | Yes |
| Trade or take contracts with a Guild Master | Yes | No | No | Yes, as a human |
| Turn, cure or reset a player | No | No | No | Yes |
| Change server settings | No | No | No | Yes |

### Requirements

| ID | Requirement |
|---|---|
| PERM-1 | Every action in the matrix is checked on the server |
| PERM-2 | A skill purchase is checked on the server for points, order and bloodline limit |
| PERM-3 | A thrall or familiar whose owner leaves the server keeps its owner. An operator can release it, with `/nightborn thrall release` for thralls |
| PERM-4 | Operator actions on another player are logged |

## 7. Client And Server

There is no singleplayer mode in this mod. A single-player world is an integrated server with one client, on the same code path as a dedicated server. No requirement here depends on player count.

### The Boundary

| Server owns | Client does |
|---|---|
| Faction, sickness, blood, skills and bloodlines | Shows the blood bar, power bar and skill screen |
| The turning bite choice | Sends the switch and draws the sign |
| Feeding, resisting and draining | Sends the bite request and plays the effects |
| Sun, weakness and their sources | Draws smoke, fire and the weakness sign |
| Bat form, landing and hanging | Moves the bat, draws it and the poof, and turns the view upside down while hanging |
| Thralls, familiars and loyalty | Draws them |
| Names, epithets, their colours and who may set one | Draws the name tags and the naming dialogue, and sends the name the owner chose |
| Household behaviour and structures | Draws fog and plays ambience |
| Who is on which map | Draws the map it is sent |
| Gore events | Decides how much gore to draw (GORE-3) |
| Coffin lids, ownership, burning and who sleeps inside, levitation, vomiting, vampire mob powers, the ritual and the Eldest, notoriety, the hunt and the Grand Inquisitor | Draws the lids, burning coffins, sleepers, poses, tells, vomit, the ritual and bell build-ups, the hunt's warnings and both arenas, with vomit scaled by GORE-3 |

### Concurrency

| ID | Requirement |
|---|---|
| CS-1 | Two vampires feeding on one victim share its blood. Neither can take more than it has |
| CS-2 | Two players using one coffin: the first lies down, the second is refused |
| CS-8 | An owner breaking their assigned coffin while another vampire sleeps in it wakes the sleeper and frees the coffin once |
| CS-9 | A coffin catching fire as a vampire lies down in it ends with the vampire awake and burning, never asleep in a burning coffin |
| CS-3 | A victim drained and killed in the same tick by a vampire and another attacker dies once and drops loot once |
| CS-4 | Two players filling Blood Bottles from one victim, by feeding or with a lancet, never create blood it did not have |
| CS-5 | A player cured and turned in the same tick ends in one defined state: the cure wins if it landed before the sickness ran out |
| CS-10 | Two vampires draining a downed Grand Inquisitor, or a drain and a blow that would each end him in the same tick, end him once: one loot roll, and one payment of points to each vampire who struck him |

### Per-Player State

| State | Scope |
|---|---|
| Faction, sickness, blood, skills, bloodlines, cooldowns and the turning bite choice | That player, synced only to them, except what others can see (CORE-4, CORE-8, WEAK-8, FACT-3) |
| Notoriety and the hunt | That player, held on the server and never sent to any client. The vampire is sent only the warnings (INQ-2) |
| Assigned coffin and respawn point | That player, with one server-wide record of every owner's coffin, player or mob, so a coffin is freed even when its chunk is not loaded |
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
| SAFE-2 | Blood is never created except by feeding (FEED-4), turning (TURN-5), respawning as the persistence grid says, a human bleeding themself with a lancet (GEAR-10), and the blows of the Inquisitor's Greatsword (INQ-15) |
| SAFE-3 | A bad config file never stops a server starting (CFG-3, CFG-4) |
| SAFE-4 | Every payload is size-limited and checked, including skill purchases, familiar orders and the names owners give (NAME-8) |
| SAFE-5 | Skill points are never created except by FEED-12, HUNT-1, HUNT-2, RITE-6, INQ-15 and CFG-8 |
| SAFE-6 | A hunter party never changes a block a player built (GUILD-8), and the tents and beds of its temporary camp drop nothing (GUILD-10) |

### Performance

| ID | Requirement |
|---|---|
| PERF-1 | A human with no sickness, holding no crucifix and not tracking costs nothing per tick beyond vanilla |
| PERF-2 | Garlic and crucifix checks look only near vampires, never across the world |
| PERF-3 | Crucifix shapes are found when blocks change, not every tick |
| PERF-4 | Fog and ambience cost nothing outside the range of a vampire structure |
| PERF-5 | A household's routine costs nothing while its chunks are unloaded |
| PERF-6 | Structure placement never loads extra chunks |
| PERF-7 | A hunter party costs nothing while its chunks are unloaded, and pitching or taking down a temporary camp never loads extra chunks |
| PERF-8 | A vampire with no notoriety costs nothing per tick for the hunt, and a hunt never loads extra chunks |

### Persistence

| What | Chunk unload | World reload | Death | Disconnect | Power loss |
|---|---|---|---|---|---|
| Faction | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Bite sickness and days left | not applicable | kept | kept | kept, paused while offline | kept to the last save |
| Turning bite choice | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Downed or escaping, and the time left | kept, for a mob | kept | cleared | kept, so leaving does not escape it | kept to the last save |
| Blood | not applicable | kept | refilled, as hunger is on respawn | kept | kept to the last save |
| Skills and bloodlines | not applicable | kept | kept, except SKILL-6 and CORE-10 | kept | kept to the last save |
| Hunter tree set aside on turning | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Assigned coffin and respawn | kept | kept | cleared by sun or a stake (COFFIN-13), otherwise kept, except CORE-10 | kept | kept to the last save |
| Thralls and familiars | kept with their chunk | kept | kept, waiting (MIND-8), except CORE-10 | kept | kept to the last save |
| Human familiar loyalty | kept | kept | kept | kept | kept to the last save |
| Household members and state, including members out feeding | kept | kept | not applicable | not applicable | kept to the last save |
| A vampire mob's stage, skills, look, size, blood and hunger | kept | kept | not applicable | not applicable | kept to the last save |
| The name of a vampire mob, hunter or Guild Master, and a vampire's epithet | kept | kept | not applicable | not applicable | kept to the last save |
| The name of a familiar or thrall | kept with its chunk | kept | kept | kept | kept to the last save |
| The chalice on a ritual altar and the blood in it | kept | kept | not applicable | not applicable | kept to the last save |
| An Eldest, summoned or in a mountain castle, and its fight | kept with its chunk | kept | not applicable | not applicable | kept to the last save |
| Whether a mountain castle's Eldest has been ended | kept | kept | not applicable | not applicable | kept to the last save |
| Notoriety, the hunt and the warning nights left | not applicable | kept | kept, except INQ-14 and CORE-10 | kept, and it does not fade while offline | kept to the last save |
| A Grand Inquisitor and his fight | kept with its chunk | kept | not applicable | not applicable | kept to the last save |
| A hunter party, its temporary camp and the nights it has left | kept, except a party on the move that leaves the world (GUILD-7) | kept | not applicable | not applicable | kept to the last save |
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
| LOOK-6 | Music is baroque and gothic: harpsichord, organ, choir and brooding strings. It plays in and near vampire structures, cutting off the game's music, and in place of the game's music at night |
| LOOK-7 | Vampire structures sound like old houses: creaks, settling, drips, guttering candles, wind in the halls and distant bells, growing louder towards the heart |
| LOOK-8 | Blocks and items look like vanilla ones wherever they are held or shown: GUI, first and third person, item frames and the ground |
| LOOK-9 | Sounds use vanilla first and real recordings next. Creature voices are never synthesised |

### Accessibility

| ID | Requirement |
|---|---|
| ACC-1 | No state relies on colour alone: weakness, sickness, blood quality and the turning bite each have a shape or icon as well |
| ACC-2 | No meaning relies on particles alone. Sun damage, bat form, weakness, vomiting and a burning coffin all have sound and an on-screen sign |
| ACC-3 | Gore can be reduced or turned off (GORE-3) |
| ACC-4 | Fog never hides a threat completely within 8 blocks |
| ACC-5 | Every string is translated into every shipped language, except given names (NAME-10) |

### Compatibility

| ID | Requirement |
|---|---|
| COMPAT-1 | Minecraft 1.21.1, Java 21, Fabric Loader 0.16.5 or later, Fabric API 0.116.12+1.21.1 or later, GeckoLib 4.7 or later |
| COMPAT-2 | Mod Menu, JEI and EMI are optional. Without them, the mod works fully |
| COMPAT-3 | Nightborn needs no other mod in the family |
| COMPAT-4 | Vanilla undead behave as they do without the mod, except CORE-9, and beds and night sleeping work as normal for humans |
| COMPAT-5 | Feeding lists, crucifix materials, the foods and drinks a vampire can keep down, structures, temporary camp layouts, loot, relic chances, trades, what adds to notoriety, the phases, attacks and lines of the Eldest and the Grand Inquisitor, name pools and epithets, vampire mob stages, skills, looks, sizes, builds and outfits, and hunter looks and kits can be changed by data packs |
| COMPAT-6 | Shipped languages: English, German, Spanish, French, Brazilian Portuguese, Russian and Simplified Chinese |
| COMPAT-7 | Every declared dependency is used |

## 9. Release Criteria

- Every use case's requirements hold on a dedicated server with two clients and on an integrated server with one client, through the same code.
- A human can be bitten, get sick, and either cure it or turn, without outside help.
- A vampire can feed, survive a full day in a coffin and buy skills in two bloodlines.
- A human can find a vampire structure, down a vampire with hunter gear and stake it.
- A vampire who kills enough hunters is hunted, fights the Grand Inquisitor and can drain him.
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
