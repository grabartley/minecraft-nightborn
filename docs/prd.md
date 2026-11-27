# Nightborn: Product Requirements

## How To Read This Document

This document says what Nightborn is: what a player can do with it, who may do what, which side decides each rule, and what the mod deliberately does not do. Every requirement has an identifier such as `FEED-3`, so issues, commits and reviews can cite it.

It holds no status. Nothing here says whether a requirement is built, in progress or waiting. Progress lives on the project board.

It changes only when a requirement changes or turns out to be unclear. The reasoning behind architectural choices lives in the decision records in [`docs/adr/`](adr/). The engineering baseline lives in [`docs/standards.md`](standards.md). Numbers in this document are starting values, to be tuned in play and then held as requirements.

## 1. Product Summary

### The Problem

Minecraft has no vampires, and nothing that hunts them. Its undead are mindless mobs that burn at dawn. There is no way to become something that fears the sun, lives on blood and grows stronger with age, and no reason to fear the dark beyond a creeper. A player who wants a night that feels dangerous, or a server where players choose sides, has nothing to choose from.

### What The Mod Is

Nightborn adds vampires and the people who hunt them. A human turns by choice, by drinking the old blood at the heart of a vampire structure or by a vampire player's turning bite, and once in a great while a vampire bat's bite does it. If the sickness is not cured in time you turn. As a vampire you live on blood, sleep through the day in a coffin, and nothing but direct sun or a stake can finish you. Anything else only brings you down. You grow through a skill tree, picking two of three bloodlines, Noble, Beast and Occult, and you grow older, from fledgling to ancient, until you lead a household of your own. Humans can fight back. Garlic, crucifixes, holy water, silver and the stake all hurt vampires, and a human who proves themselves and joins the guild earns hunter skills by killing them, picks two of four paths and rises through its ranks. Vampires are rare. They live in households hidden in fog, in castles, crypts, ruined manors, deep lairs and, rarest of all, castles on the tops of mountains, and a cleared house does not stay empty for ever. Hunters keep camps of their own and send out parties that wander the land. Each vampire is someone: a name and an epithet, a look, a voice, a stage of age and powers of its own, and one that gets away from you remembers. Villages notice what feeds on them. Each side has a boss. Hunters summon the Eldest, a vampire with every power, and each mountain castle holds one of its own, met only once. The Grand Inquisitor, the head of the guild, comes for a vampire who has killed too many hunters.

### Experience It Aims For

- **The night is dangerous again.** Vampires are rare, so meeting one matters.
- **Every vampire is someone.** Wild vampires have a home, a routine, a look and powers of their own. An old one is a fast, frightening fight that pays well.
- **Being a vampire changes how you play.** Sun, blood, garlic and the coffin shape every day.
- **Two sides, one server.** Vampires and humans are real factions, and players choose which to be.
- **Choices stick.** A bloodline, once chosen, is yours. A turned player stays turned.
- **Lived-in gothic.** Places feel old, rich and inhabited. Candlelight, decay and clutter, never clean or plastic.
- **Premium look and sound.** Every model, animation and sound is hand-made and judged against the best Minecraft art.
- **Vanilla at heart.** Recipes and rules echo what vanilla already teaches: a cleric blesses, a golden apple cures, a bed skips the night.

### What Makes It Different

| Claim | Why it holds |
|---|---|
| Both sides are playable | Turning, feeding and bloodlines for vampires, gear and a skill tree for hunters (TURN, FEED, LINE, HUNT) |
| Feeding is a real choice | Sip or drain, fresh or bottled, animal or person, and the victim can fight back (FEED-1 to FEED-16) |
| Builds that fit your playstyle | Pick two of three bloodlines, each with its own powers (LINE-1 to LINE-8) |
| Vampires are hard to kill | Only direct sun or a stake finishes one. Anything else only brings it down (CORE-7, CORE-8) |
| Folklore that works | Garlic, crucifixes built from blocks, holy water, silver and stakes each have real rules (WEAK-1 to WEAK-10) |
| Vampires live somewhere | Households with personalities in five kinds of structure, rare and hidden in fog. Vampires live there, go out to feed and come home by dawn (HOME-1 to HOME-26) |
| Vampires fight like players | Wild vampires grow in stages and use every power a player can, more often and more cleverly the older they are (HOME-15 to HOME-17) |
| Hunters fight like players | Hunter mobs hold a rank, the hunter core and two paths, keep learning skills as they live and stake, and use their powers in a fight (GUILD-15 to GUILD-17) |
| The day belongs to vampires too | Coffins skip the day the way beds skip the night (COFFIN-1 to COFFIN-15) |
| An endgame for hunters | A repeatable ritual summons the Eldest, a boss with every vampire power, fought in three phases inside a sealed arena, and one waits once at the top of every mountain castle (RITE-1 to RITE-16, HOME-23) |
| An endgame for vampires | The Grand Inquisitor hunts down a vampire who has killed too many hunters, or comes when a bell is rung: a boss fought in three phases inside a sealed arena, with dawn as the clock (INQ-1 to INQ-16) |
| Help in the daylight | A human familiar runs errands and guards your coffin, and might betray you (FAM-1 to FAM-9) |
| Everyone has a name | Vampires, hunters, familiars and thralls each carry a name, shown above them and in chat. A vampire's ends in an epithet and takes the colour of its age (NAME-1 to NAME-12) |
| A long game on both sides | Vampires age through four stages by nights, blood and deeds, and hunters rise through four guild ranks (AGE-1 to AGE-7, RANK-1 to RANK-4) |
| A house of your own | A vampire founds a household, invites younger vampires, names it and has to defend it by day (HOUSE-1 to HOUSE-10) |
| A world that answers back | Villages grow suspicious, survivors become nemeses, the guild goes after the notorious and cleared houses fill again (SUSP-1 to SUSP-4, NEM-1 to NEM-7, GUILD-12 to GUILD-14, HOME-24 to HOME-26) |
| Things worth finding | About 80 accessories in eight slots, new enchantments, weekly trials and mastery feats (WORN-1 to WORN-6, CHAL-1 to CHAL-3) |

### Non-Goals

- **Not a werewolf or general monster mod.** Vampires and hunters only.
- **No cure after turning.** A cure works only during the sickness.
- **No vampire that feeds on anything but blood.**
- **Not a claims or protection mod.** Garlic weakens vampires. It does not stop them breaking blocks. The only protected blocks are an assigned coffin (COFFIN-13) and an old blood font (TURN-9).
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
| A true reflection in a placed mirror | The hand mirror covers the test (FACT-6), and drawn reflections fight shader mods |
| Vampires needing an invitation to enter a home | Only a vampire's house has an owner (HOUSE-1). A villager's or a human player's home has none |
| Changing bloodlines once chosen | Choices are meant to stick. A reset can come later if play shows the need |
| Hiding vampire markers on map mods | Vanilla maps come first (CORE-5) |

### Product Decisions

| Question | Decision | Requirement |
|---|---|---|
| Which side is the mod about? | Both. Vampires and hunters are both playable | TURN, HUNT |
| How do you turn? | Old blood drunk at the heart of a vampire structure, a vampire player's turning bite or, very rarely, a vampire bat's bite | TURN-1, TURN-8, TURN-9 |
| Can you be cured? | Only during the sickness | CURE-1 to CURE-3 |
| How bad is the sun? | Lethal. Direct sun sets a vampire on fire | SUN-1 |
| What replaces hunger? | Blood | FEED-1 |
| How do powers grow? | A skill tree with three bloodlines, of which you invest in two, bought with experience that takes about a hundred nights to fill, and four stages of age | LINE-1 to LINE-3, SKILL-1, AGE-1 |
| Who is a hunter? | A human who has proved themselves and been sworn into the guild. They share a core of skills, pick two of four paths and rise through four ranks | HUNT-10, HUNT-3, RANK-1 |
| How does a vampire die? | Only to direct sun or a stake. Anything else brings it down, greatly weakened, until it recovers and escapes | CORE-7, CORE-8 |
| What happens on death? | Respawn at your assigned coffin, which death by the sun or a stake unassigns, sending you to world spawn. A stake costs skill progress. A server can make a vampire's death a full reset instead | COFFIN-4, COFFIN-5, COFFIN-13, SKILL-6, CORE-10 |
| PvP? | Vampires and humans are factions, and PvP follows the server's own setting, all but a duel for a house | FACT-1 to FACT-6, HOUSE-8 |

## 3. Actors

| Actor | What they need |
|---|---|
| **Vampire** | To feed, survive the sun, grow their bloodlines, grow older and keep a coffin |
| **Lord of a house** | The vampire player who leads a house, not a castle's vampire lord (HOME-7). To fill it, keep it fed and defend it |
| **Human** | To stay human, or cure a bite in time, and to fight back |
| **Hunter** | A human who has joined the guild (HUNT-10). To track, ward against and kill vampires, and earn skills and rank for it |
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
| Skill experience | What feeding, staking and other acts pay. Enough of it gives a skill point (SKILL-1) |
| Skill point | Bought with skill experience and spent on a skill tree |
| Core | The vampire skills every vampire can take |
| Bloodline | One of three branches of the vampire tree: Noble, Beast or Occult |
| Hunter tree | The humans' skill tree: four paths, of which a hunter picks two |
| Rank | A hunter's standing in the guild: Initiate, Hunter, Veteran or Master |
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
| Household | The vampires who live together under one head, in a vampire structure or in a player's house |
| House | A household a vampire player leads, at their coffin or in a structure they claimed |
| NPC member | A vampire mob that belongs to a household |
| Wild vampire | A vampire mob with no household |
| Old blood | The blood kept at the heart of a vampire structure. A human who drinks it starts bite sickness |
| Stage | How old and strong a vampire is, mob or player: fledgling, adult, elder or ancient |
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
| Raiding party | Hunter mobs the guild sends against one vampire player, with no Guild Master (GUILD-13) |
| Temporary camp | The campfire and tents a hunter party pitches for one to three nights |
| Veteran hunter | A named hunter mob who leads the raiding parties the guild sends against older vampires (GUILD-12) |
| Nemesis | A vampire or hunter mob that holds a grudge against one player and comes back for them |
| Suspicion | How sure a village is that a vampire is at work in it |
| Known face | A vampire player a village has seen for what they are |
| Trial | A feat on offer for a week that pays skill experience |
| Accessory | An item worn in one of the eight accessory slots |
| Journal | One of the two guide books, the hunter's and the vampire's |
| Command wheel | The ring of actions that opens when a player uses someone they lead or deal with |
| Faction | Vampire or human. Every player is in exactly one |
| Hunter | A human player sworn into the guild, or a hunter mob |

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

A human turns by drinking old blood at the heart of a vampire structure, by a vampire player's turning bite or, very rarely, by a vampire bat's bite. A sick player sees a clear sign that something is wrong, which grows as the day passes. When the sickness runs out, the player collapses where they stand and wakes up a vampire.

| ID | Requirement |
|---|---|
| TURN-1 | Bite sickness starts on a human in three ways: drinking old blood (TURN-9), a vampire player's turning bite (TURN-8), and a vampire bat's bite. By default a turning bite starts it every time and a bat's bite one time in a thousand, less against a human with bite resistance (HUNT-4). A vampire mob's bite never starts it and only feeds (FEED-4, FEED-5), and nor does a vampire player's feeding bite |
| TURN-2 | A vampire player can bite a human player only where the server allows PvP |
| TURN-3 | Bite sickness lasts 1 in-game day by default, and gets worse in three equal stages over its length: hunger, then the vanilla Weakness effect, then slowness in daylight. The time already run counts against the current sickness length, so a changed length applies at once to a player already sick, and a player whose sickness has already run longer than the new length turns at once. Time skipped by sleeping counts toward it, whether beds skip the night or coffins skip the day (COFFIN-2) |
| TURN-4 | A sick player is shown the sickness and the time left, as a status effect and a sign on screen |
| TURN-5 | When the sickness runs out, the player turns, which is a moment (FACT-7). They keep their inventory, start with a full blood bar and get the vampire core |
| TURN-6 | A player who turns stops earning hunter skills, and their hunter tree is set aside. It comes back if an operator makes them human again |
| TURN-7 | Another bite during the sickness does not restart or lengthen it |
| TURN-8 | A vampire player's bite feeds by default. They switch it between feeding and turning with a key they can rebind. Each switch is confirmed on screen, and a sign with its own icon stays on screen while the turning bite is on (ACC-1). A turning bite feeds like any other bite (FEED-4 to FEED-12), and the bite's mode is fixed when it first draws blood. One that starts the sickness costs the vampire a large share of their blood, half a full bar by default, and a vampire with less cannot turn anyone. Where the server stops players turning players (CFG-5), a turning bite on a player feeds only. A vampire made human loses the choice, so a player who turns again starts with a feeding bite. Vampire mobs and bats do not choose |
| TURN-9 | Every vampire structure keeps old blood in a font at its heart. The font is found only there and cannot be crafted, moved or broken. A human who uses it with an empty hand drinks, which starts the sickness and empties it. Nothing can carry the blood away, and a vampire gets nothing from it but a lost journal back (GUIDE-2). It refills over some days, 7 by default while its household lives and twice as long without one, and the blood in it rises as it does. A household that is fed lets a human it has no quarrel with walk in and drink (HOME-11). |

**Not supported:** turning by any means other than these and an operator command; turning a villager into a vampire; carrying old blood away.

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
| Use on a badly hurt enemy in a fight, empty hand | Finisher bite (FEED-16) |

| ID | Requirement |
|---|---|
| FEED-1 | A vampire has a blood bar in place of hunger. It drains over time, by default one point every 5 minutes at rest, so a full bar lasts several in-game days and a vampire does not have to feed every day, and faster when the vampire runs, fights or uses powers |
| FEED-2 | An empty blood bar starves the vampire (FEED-14). While the bar is above half, the vampire heals slowly over time, one health every 10 seconds, and healing costs no blood |
| FEED-3 | Normal food, potions and milk give a vampire nothing. A vampire who eats or drinks one is hurt and vomits it back up, with a short spell of Nausea, seen and heard by everyone near. Anything eaten or drunk counts, including other mods' food and drink, as does anything in the `nightborn:vampire_rejects` tag, which takes in the shared `c:foods` and `c:potions` tags. Blood Bottles, holy water and items in the `nightborn:vampire_consumables` tag are exempt. Garlic is the worst thing a vampire can swallow: on top of the vomiting it takes far more damage, a much longer spell of Nausea, the weakening of touching garlic (WEAK-2) for a long time, and it brings up blood. Like any vomiting, it brings a vampire down and never kills |
| FEED-4 | Feeding drinks blood from the victim each second, and hurts it by the same amount. The victim is held in place for as long as the bite lasts: it cannot walk, be pushed away, ride away or leave, and it is free again a moment after the bite ends. Creatures that fly or swim also keep their height, listed in the `nightborn:holds_height` tag. The vampire plays a drinking animation for as long as the bite lasts, seen by everyone near and by the vampire in first person |
| FEED-5 | Letting go stops the bite and leaves the victim alive, with Weakness for a short time |
| FEED-6 | Holding past the victim's last health drains it, and it dies |
| FEED-7 | A victim resists with a chance that grows with its maximum health and falls as its health drops. A sleeping or mesmerised victim never resists |
| FEED-8 | A resisting victim breaks free of the hold, pushes the vampire back and, if it can attack, fights back |
| FEED-9 | Blood restores more by quality: animal least, then villager and illager, then player |
| FEED-10 | Fresh blood restores more than the same blood bottled |
| FEED-11 | A Blood Bottle keeps the quality of the creature it came from, and shows it |
| FEED-12 | Feeding earns vampire skill experience (SKILL-1) from the blood drunk, weighed by its quality, with a bonus for draining. Filling Blood Bottles while feeding counts as feeding. Filling them with a lancet does not (GEAR-10) |
| FEED-13 | A villager that sees a vampire feed panics and flees, rings the village bell, and the village treats the vampire as it treats a player who hurt a villager, through vanilla gossip, and the village's suspicion rises (SUSP-1). A villager that sees nothing acts as normal |
| FEED-14 | A starving vampire grows more weakened over time, up to the cap (WEAK-9). Its powers and levitation stop, then its night speed and jump fade, and its bare-hand damage shrinks. After a while it loses health slowly, down to half a heart on any difficulty but Peaceful. Starving never downs or kills a vampire. Any blood stops it growing, and the weakness fades as the bar refills |
| FEED-15 | Fresh blood also gives a short gift by the kind of prey it came from, on top of its quality. By default a horse gives speed, a wolf makes nearby prey stand out, a villager quickens healing, an illager gives strength and a witch resistance to potions. A Blood Bottle keeps the gift at half strength and shows it, and a chalice keeps the blood but not the gift. A new gift replaces the last. Which prey gives what, how strong and for how long is data |
| FEED-16 | A vampire can bite an enemy that is below a share of its health, a quarter by default, in the middle of a fight, without sneaking. It is a fast drain: the enemy dies, the vampire is healed a little, and it gains blood and experience as from any drain (FEED-6, FEED-12). The vampire cannot move or strike while it drinks, and a blow breaks the bite. It works only on creatures that can be fed on, and on a player only with PvP on (TURN-2) |

**Not supported:** feeding on the list's excluded creatures; feeding through blocks; a human drinking blood.

**Enforcement:** server. The client sends the bite request; the server checks reach, the target and the faction.

### 5.4 Survive The Day

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| SUN-1 | A vampire in direct sun catches fire, as a zombie does, and burns while it stays there. Sun fire kills, downed or not. Fire that keeps burning after the vampire reaches shade only downs it, and cannot hurt it while it is escaping |
| SUN-2 | No armour, helmet or effect, Fire Resistance included, stops the burn. Shade, rain, water, a closed coffin that is not burning (COFFIN-7, COFFIN-14) and an open parasol (SUN-5) do |
| SUN-3 | A burning vampire gives off dark, ashy smoke and a sizzling sound, on top of the fire |
| SUN-4 | Vampire mobs follow the same rules. As dawn nears, and whenever the sun reaches them, they head for the nearest shade they can walk to and keep going until they are under cover |
| SUN-5 | A parasol is crafted from sticks and wool and held open in either hand, where it shades its holder alone from direct sun. Using it opens or closes it, and it closes when put away, dropped or held while lying down. It wears only while the sun is on it and lasts about 3 minutes of sun in all by default. It shows what is left and its cloth scorches as it wears. It is mended with wool and takes no enchantment. It opens and closes with a motion of its own, so it reads as open or closed from across a street. When it breaks its holder burns. Sprinting, levitating, taking bat form or taking a blow closes it, and it stays closed until its holder opens it again |

**Not supported:** any other way to walk in direct sun.

**Enforcement:** server.

### 5.5 Feel Your Weaknesses

**Actor:** Vampire, human.

How weakened a vampire is scales with how strong the source is. By default each Garlic String in range weakens by 15%, up to 45% for three or more; touching or holding garlic weakens by 80%; a held crucifix weakens by 60%, and by 25% more of that for each point of the holder's faith; and a crucifix shape or a hung crucifix weakens by 40%. A crucifix held by a vampire wards no one. A vampire mob's only attack is its bite, so it cannot hurt a crucifix holder at all.

| ID | Requirement |
|---|---|
| WEAK-1 | Each Garlic String weakens vampires within 8 blocks. More strings in range weaken more, up to a cap |
| WEAK-2 | Touching garlic, by walking through garlic crops or brushing a Garlic String, or holding garlic, weakens a vampire greatly, far more than a string in range. Eating garlic weakens just as much and for far longer (FEED-3) |
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
| CORE-10 | With the `hardcoreVampireDeath` server setting on, off by default, a vampire player who dies is reset like a hardcore death: they come back as a new human at world spawn with an empty inventory and ender chest, no experience, no skills, bloodlines, notoriety (INQ-1) or hunter tree, their coffin unassigned (COFFIN-13), and their thralls and familiars freed. Advancements are kept |
| CORE-11 | Every vampire, player or mob, can levitate. Players toggle it with a key they can rebind. A levitating vampire floats one block above the ground with its feet together, follows the ground up steps and slopes and over water, gliding up a rise of one block without catching while a fence or a higher wall still stops it, and sinks slowly over a drop without fall damage. A player holding sneak sinks over a drop about as fast as sneak lowers a creative flyer, keeping the float pose, and holding it over the ground lands them. It starts a sprint as it would on foot, and leans forward to move and further to sprint, never fully horizontal, with arms spreading as it speeds up, holding one float pose until its movement really changes, and lands with its own animation. It drains blood while active. It can still attack and use items while it floats, and others see the swing or the raised hand over the float. A weakened or downed vampire, or one in bat form, riding, sleeping, gliding or flying in creative, cannot levitate and lands at once. The server lands a player who stays well above that height for more than a moment, and ignores a toggle pressed again within half a second. Every landing the vampire did not choose sets it down gently, never with fall damage. Vampire mobs rise in a fight and now and then as they move about, as often as the server's options allow, and still find their way while levitating. A floating vampire mob knows its own height: where the way a few paces ahead is too low to float through, such as a doorway, a low passage or an overhang, it sinks and goes through on foot, opening doors as it does on foot, and rises again on the far side. In a room or passage too low to float in it fights on foot |

**Not supported:** a vampire hiding its eyes while feeding; levitating higher than one block.

**Enforcement:** server decides state. The client draws eyes and effects.

### 5.7 Grow Older

**Actor:** Vampire.

A vampire player ages as a vampire mob does. Time opens each stage and a deed reaches it.

| Stage | Opens after, by default | Reached by |
|---|---|---|
| Fledgling | Turning | |
| Adult | 10 nights | Draining a villager or an illager |
| Elder | 40 nights | Ending a Guild Master |
| Ancient | 100 nights | Draining the Grand Inquisitor (INQ-13) |

| ID | Requirement |
|---|---|
| AGE-1 | Every vampire player has a stage, one of the four a vampire mob has (HOME-15). A newly turned player is a fledgling, and so is a vampire from an older world, who keeps every skill they hold |
| AGE-2 | A stage opens once the player has lived a number of nights as a vampire and drunk a total of blood. A night counts when the player is online as a vampire for most of it, and a night that sleepers skip counts whole |
| AGE-3 | An open stage is reached by its deed, and a deed done before the stage opened counts when it opens. The ancient's deed counts for every vampire who struck the Grand Inquisitor in the fight that ended him (INQ-15), as do Inquire Within (ADV-45) and the server first (CHAL-3) |
| AGE-4 | Each stage gives more health, harder blows, a larger blood bar and slower blood drain, about what that stage gives a vampire mob. Age adds no weakness |
| AGE-5 | A stage also opens the deepest skills of the tree (SKILL-2), a larger household (HOUSE-3) and a name colour (NAME-3) |
| AGE-6 | Age never goes down. Death and staking leave it alone. Only a hardcore death (CORE-10) and an operator (CFG-8) change it |
| AGE-7 | The skill screen shows the player's stage, the nights and blood still needed for the next, and its deed |

**Not supported:** a change of look with age, since a player keeps their own skin.

**Enforcement:** server.

### 5.8 Grow Your Bloodlines

**Actor:** Vampire.

The vampire tree has the core and three bloodlines. A vampire player spends points in at most two bloodlines, so each vampire is a different kind of vampire. Vampire mobs are not bound by this (HOME-15).

| Bloodline | What it gives |
|---|---|
| **Noble** | The ruler: minds bent, servants kept and a human face worn |
| **Beast** | The predator: bat form, claws, speed and the leap |
| **Occult** | The sorcerer: familiars, fog, shadow and the dead |

The core starts with Thick Blood (more health), Grave Strength (harder blows), Blood Rush, an active power that burns blood for a short burst of speed and strength, and Vanish, an active power that takes the vampire out of sight in a puff of smoke and brings it back a moment later a few blocks away. A player vanishes the way their movement keys point, or behind them when none is held. The vampire only ever lands on solid floor with room to stand, never in fluid or fire, and never through a wall. If the full distance is not safe it lands short, and if nowhere is safe the power is refused and costs nothing. Vanish uses the smoke and sound of bat form (BAT-1) where the vampire leaves and where it returns. The first skill of each bloodline chooses it and gives a small lasting gift: tougher skin for Noble, safer falls for Beast and longer reach for Occult.

| ID | Requirement |
|---|---|
| LINE-1 | The vampire tree has the core and the three bloodlines |
| LINE-2 | Spending a first point in a bloodline chooses it. Once two are chosen, the third is locked |
| LINE-3 | A chosen bloodline cannot be swapped, except by an operator (CFG-8) |
| LINE-4 | Every bloodline has skills a vampire without it cannot get |
| LINE-5 | Each skill shows what it does, what it costs and what it needs first, before it is bought |
| LINE-6 | The skill screen opens with a key once the player has a side: turned (TURN-5) or sworn into the guild (HUNT-10). Before that the key only says they have no skills to learn yet, and the server refuses any skill or power bar change they send. The screen shows free points with a skill point graphic, readable at a glance, points earned and spent, and a bar of experience toward the next point (SKILL-1) |
| LINE-7 | The vampire and hunter skill screens are styled, each with its own look: background, frames, lines, headers, tooltips and point graphic. Neither is a plain menu. They are titled Vampire Skills and Hunter Skills, and each shows its shared branch as Core. Each branch is a band read left to right, and the bands stack top to bottom. Every skill sits one column right of the deepest skill it needs. Lines are solid with a darker edge and light up once the skill is owned. No line passes behind a skill or touches a line it is not joined to. The trees scroll under a fixed points header and power bar, by mouse wheel, scrollbar or keys, and the gaps between skills never shrink. The panel fits the screen at every GUI scale with a clear gap above and below it, centred, its stage lines (AGE-7) under the panel and never over it, and zooms out as one piece only on a screen too small for its fixed parts |
| LINE-8 | The core and each bloodline hold about ten skills. Skills, their ranks, costs and the stage each needs are data, and `docs/skills.md` lists them all |

| ID | Requirement |
|---|---|
| SKILL-1 | Acts pay skill experience, not points. Experience fills a bar, a full bar gives one point, and each point needs more experience than the last. Wherever this document says an act earns or pays points, it pays experience toward them. Vampire and hunter experience and points are separate, and each is spent only in its own tree. By default, filling two bloodlines or two hunter paths takes about as long as reaching ancient or Master, at the pace set under Skill pacing below |
| SKILL-2 | A skill costs points and may need another skill first. A passive skill comes in up to three ranks, each bought in turn. The deepest skills need a stage (AGE-5) or a rank (RANK-3) |
| SKILL-3 | Bought skills are kept across death, reconnect and world reload |
| SKILL-4 | Active skills are used from a power bar of five slots, with keys the player can rebind. The skill screen shows the bar: the player drags any active skill they own onto a slot, swaps two by dragging one onto the other and clears a slot by dragging its skill off. A power that is on no slot cannot be used. Hunters have the same bar (HUNT-6) |
| SKILL-5 | Each active skill has a blood cost and a cooldown, shown on the bar with its icon and key. A downed or weakened vampire cannot use powers |
| SKILL-6 | A staked vampire player loses 10% of their earned points, rounded up, taken from unspent points first and then from their most recent skills. A bloodline left with none of its skills is freed for a new choice. With CORE-10 on, the full reset applies instead |

**Skill pacing.** By default the first skill point costs 200 experience and each point after it costs 1% more than the one before. Staking a vampire pays 100 experience and 150 more for each stage above fledgling, so a fledgling pays 100, an adult 250, an elder 400 and an ancient 550. A contract pays 250. The Eldest and the Grand Inquisitor pay 2,500 each, and a nemesis five times what staking it would. Fresh blood pays 8 experience for each point of its worth and a drain 100 more. Trials pay a share of the next point, as CHAL-1 says.

An ordinary hunting week is three fledglings, three adults and two elders staked, two contracts handed in, and one small and one medium trial. An ordinary feeding night is 30 points of fresh blood at villager quality with one drain, and the same two trials each week. At those rates:

| Milestone | Hunter | Vampire |
|---|---|---|
| First point | Two fledglings, or one adult | The first night |
| Five fledglings staked | Two points | Not applicable |
| One staking | Half a point for a fledgling, one point for an adult or an elder, two for an ancient | Not applicable |
| The core, about 45 to 50 points | About five weeks: around 40 stakings, 10 contracts and 10 trials | About five weeks |
| The core and one path or bloodline, about 80 points | About ten weeks | About ten weeks |
| The full build, about 110 points | About fifteen weeks | About a hundred nights, as long as reaching ancient |
| Guild rank | Hunter at 10 points, in the first week. Veteran at 45, with the core. Master at 100, near the full build | Not applicable |

**Enforcement:** server. The client asks to buy a skill; the server checks points and order.

### 5.9 Take Bat Form

**Actor:** Beast vampire.

| ID | Requirement |
|---|---|
| BAT-1 | A Beast vampire can turn into a bat and fly, and uses the same power again to turn back. A vampire mob takes bat form to close on a distant target and turns back beside it. A vampire, player or mob, that turns back in the air, by choice or forced, falls with normal gravity and takes fall damage counted from where it turned back (CORE-7, CORE-8) |
| BAT-2 | Turning into a bat, and back, gives a poof of the dark, ashy vampire smoke (SUN-3) and a sound, seen and heard by everyone near |
| BAT-3 | A bat cannot attack, break blocks, use items or other powers, open containers or feed, and turning into one ends a feed. It drains blood while flying, faster than levitating and faster still at a sprint, and an empty blood bar forces it back. A landed or hanging bat drains blood only as a resting vampire does |
| BAT-4 | Direct sun forces a vampire out of bat form, and no vampire can take bat form in it |
| BAT-5 | Turning back needs room to stand. A bat close under a ceiling with open air below turns back lower, by no more than the height it gains, and falls as BAT-1 says. Without room, the bat stays a bat |
| BAT-6 | A single heavy blow, as heavy as the server sets, being downed, holy water or garlic in range forces a vampire out of bat form |
| BAT-7 | A bat player flies with weight, not as in creative. They steer with the camera and fly where they look while holding forward, climbing as they look up and diving as they look down. Speed eases in and out, turning slows the bat at speed, and letting go of forward lets it coast to a slow hover. Sprint flies faster. A bat never takes fall damage. Third person shows the bat in every pose |
| BAT-8 | A bat player that flies into the ground lands on it, crawls slowly on its wings and takes off with jump. Using the underside of a block from beneath it hangs the bat upside down from that block, and the player's first-person view turns upside down with it. Jump or forward lets go. A hanging bat turning back drops as in BAT-1. A bat player sees by sound, not sight: the world goes dark and shows only as black and white outlines, lit by ripples that spread from every sound nearby, louder sounds stronger and further, and from the bat's own calls, so a silent cave is never fully blind. A short distance around the bat is always dimly outlined so it can steer, land and hang, and creatures show a little brighter than blocks. The view fades in and out with the change of shape, is drawn under the HUD for that player alone and follows the form views option (CFG-7) |

**Enforcement:** server decides the form, landing, hanging and blood drain, and checks bat flight as it checks any player's movement. The client moves and draws the bat.

### 5.10 Bend Minds

**Actor:** Noble vampire.

| ID | Requirement |
|---|---|
| MIND-1 | Mesmerise is a power: the vampire stares at a mob, villager or player in sight and in range for a moment, and if the stare holds, the target is mesmerised. The blood and cooldown are spent when the stare begins, even if it breaks. A mesmerised mob stands still and cannot attack |
| MIND-2 | A mesmerised villager can be charmed by talking to it: for a while it trades with that vampire as with a hero of the village. Silver Tongue gives the charm, as the `nightborn:charm` attribute |
| MIND-3 | A mesmerised player is slowed and cannot hurt the vampire who mesmerised them for a short time. They are never frozen |
| MIND-4 | Bosses cannot be mesmerised. Hunter skills resist it (HUNT-4) |
| MIND-5 | Sipping from a mesmerised villager can make it a thrall. A thrall follows its owner and fights for them. Binding one opens the naming dialogue (NAME-6) |
| MIND-6 | A thrall can be told to follow, stay or guard a place. Its owner gives the order from the command wheel (WHEEL-2), and a guard fights monsters near its post |
| MIND-7 | A vampire has a limit on thralls, raised by Noble skills: Blood Bond allows one and Dark Court two more, as the `nightborn:thrall_limit` attribute |
| MIND-8 | A thrall whose owner dies stays where it is until they return |

**Not supported:** mesmerising through walls.

**Enforcement:** server.

### 5.11 Keep Familiars

**Actor:** Owner.

| ID | Requirement |
|---|---|
| FAM-1 | An Occult vampire can summon ghostly familiars that fight for them, carry items and scout ahead. Summon Familiar allows one at a time and Spirit Pact one more, as the `nightborn:familiar_limit` attribute; summoning past the limit sends the oldest away. Each takes a name as it appears (NAME-7). The owner changes its task from the command wheel (WHEEL-2), hands it items while it carries and takes them back by sneaking. A scout flies the way its owner looks and makes the creatures and people it passes glow for a while |
| FAM-2 | Ghostly familiars last a set time, cost blood to summon and pass through blocks only while scouting. One that fades or dies drops what it carried |
| FAM-3 | Any vampire can hire one grown villager as a human familiar by using a Familiar's Contract on it. The contract is crafted from paper, a feather and a Blood Bottle, and is used up. Hiring opens the naming dialogue (NAME-6). The familiar keeps the villager's biome look in a servant's livery but leaves its trade behind |
| FAM-4 | A human familiar works in daylight: it carries items between chests the owner marks, trades with villagers for the owner and guards the owner's assigned coffin. A player who leads a familiar or a thrall sneaks and uses a chest or barrel they can open with an empty hand: the first one marked is the fetch chest, the next is the deliver chest, and using a marked chest again clears it. The marks belong to the owner, so every servant they lead works to the same chests. Which blocks count is the `nightborn:familiar_errand_chests` block tag. On each errand the familiar takes a stack from the fetch chest, buys what it can afford from the nearest villager, and puts everything into the deliver chest. Its trades use up the villager's offers but give no experience to anyone. It drives off monsters near the coffin, other than creepers and vampires, and at night it stays by the coffin |
| FAM-5 | A human familiar has loyalty. Good treatment raises it. Being fed on, hurt or left without pay lowers it. The owner pays wages by using emeralds on it, a set number of days per emerald, up to a set number of days ahead. Each paid day raises loyalty and each unpaid day lowers it |
| FAM-6 | A human familiar with low loyalty may secretly turn to the hunters, then attack its owner while they sleep. Its loyalty is never shown as a number: asking after it on the command wheel (WHEEL-2) tells the owner whether it seems devoted, content, uneasy or resentful, and a turned familiar always seems content and still takes wages. A turned familiar stops running errands |
| FAM-7 | A familiar obeys only its owner |
| FAM-8 | A human familiar killed or turned against its owner frees the contract slot. A hired villager brings what it was carrying and puts it in the deliver chest on its first errand. A familiar drops whatever it carries when it dies or is released, and a released familiar becomes a villager again. An operator can release a familiar, or free a player's slot when the familiar itself is lost |
| FAM-9 | A human familiar and a thrall can also be set to work by day from the command wheel: gather and farm, taking crops, wood or ore from a place the owner marks to a chest; fetch blood, bleeding animals with a lancet (GEAR-10) and bringing the bottles home; scout, walking to a village or structure the owner names and reporting who is there, how many hunters and how suspicious it is (SUSP-2); and stand watch, guarding the house and waking its household when hunters come (HUNT-9). The place a servant gathers or fetches blood is the ground around where it stands when told. Each round it goes to the owner's deliver chest, or their only marked chest (FAM-4), puts down what it carries and takes up the tools for the job, then works until it has no room, nothing left to do or no tools, and brings it all back, tools too. So both jobs are offered only while such a chest is in errand range. A gatherer takes ripe crops and sows them again, fells a tree from its lowest log, a tree being logs under living leaves, and mines ore only with a pickaxe from the chest that could mine it, wearing the pickaxe as a player does. Beyond what grows or lies in that place, it changes no block a player built. A servant fetching blood takes a lancet and empty Glass Bottles from the chest and bleeds the animals there, one draw at a time, as GEAR-10 says. It never bleeds a tame animal or one in the `nightborn:servants_spare` entity tag, which holds pets, mounts and animals that fight back. The owner names a place to scout by picking it on the wheel from the nearest in reach, each shown with how far it lies and which way, and the scout tells the owner its report when it is home, or that it could not find a way. Until there are houses, a servant on watch stands by its owner's assigned coffin and warns the owner when hunters come, and the job is offered only to an owner with an assigned coffin. A job on a familiar takes the place of its errands until the owner sends it back to them. What can be gathered is the `nightborn:gather_crops`, `nightborn:gather_wood` and `nightborn:gather_ores` block tags, the pickaxes it may take are the `nightborn:gather_tools` item tag, and where a scout can go is the `nightborn:scout_targets` structure tag |

**Enforcement:** server.

### 5.12 Lead A Household

**Actor:** Vampire, lord.

A vampire who has come of age can found a house: a household of their own, of vampire players and NPC vampires younger than themselves.

| Lord's stage | Members the house can hold, by default |
|---|---|
| Fledgling | 0 |
| Adult | 2 |
| Elder | 4 |
| Ancient | 6 |

| ID | Requirement |
|---|---|
| HOUSE-1 | A vampire who is an adult or older founds a house in one of two places: at their own assigned coffin, by lying down in it while they lead no house, which the house then follows, waiting where it is while its lord has none, or in a vampire structure whose household is all gone, by sleeping in the coffin of its old head. A claimed structure's fog, music, old blood and coffins then serve the new house. A mountain castle can be claimed once its Eldest is ended (HOME-23). A vampire leads one house and holds one structure at a time, and founding again moves the house |
| HOUSE-2 | The founder is the lord. The lord invites a vampire player of a lower stage from the command wheel (WHEEL-2), who accepts or declines from the invitation in chat, belongs to one house at a time and can leave at any time, from the wheel on the lord or with `/house leave`. `/house` tells any vampire which house they belong to and where it stands. The lord invites an NPC vampire of a lower stage by using a full gold chalice on it, which the invitation empties. It leaves the household it had, if any, and walks to the house, where it needs a free coffin. The head of a household cannot be invited |
| HOUSE-3 | A house holds as many members as its lord's stage allows, players and NPC members together, as the table says |
| HOUSE-4 | NPC members live at the house as a household lives in its structure (HOME-5, HOME-6, HOME-13): they keep its routine, sleep in its coffins and go out to feed. They bring Blood Bottles home to a chalice or chest the lord marks by sneaking and using a Blood Bottle on it, and they defend the house, its lord and its members against any human or hunter who attacks them, and against a vampire only as HOME-11 allows |
| HOUSE-5 | The lord orders an NPC member from the command wheel: follow, stay, guard a place, where it turns on any human or hunter who comes near, go out to feed that night, bring blood home every night, or leave the house and go wild |
| HOUSE-6 | The lord names the house in the naming dialogue (NAME-6, NAME-8) and gives it a crest: a charge and two colours chosen from a set (`docs/challenges.md`). The dialogue opens when the house is founded and again with `/house name`. A crest banner, crafted from any banner and an iron ingot, hangs on a wall and shows the crest of the house of whoever hangs it. A member who uses a banner showing an old crest gives it the current one. The house name follows each member's name (NAME-11) |
| HOUSE-7 | Members can open, sleep in and be assigned any coffin of the house (COFFIN-4) |
| HOUSE-8 | A member of the lord's stage or older can challenge for the house by declaring it to the lord from the command wheel (WHEEL-2). When both next stand together, a circle drawn between them seals them in a duel nobody else can enter or affect, whatever the PvP setting. The first one downed loses, is not staked and gets up at once. The winner is the lord and the loser stays a member. A lord who does not answer within three nights loses the house, counting only nights the challenger is online to be met. A fighter who leaves the circle, by logging out or otherwise, loses, and a challenger cannot leave the house mid-duel. While a challenge is pending the lord cannot hand the house over or disband it, though an operator can. A lost challenge cannot be repeated for seven nights |
| HOUSE-9 | When the lord is ended by a stake or the sun, made human again (CFG-8) or reset by a hardcore death (CORE-10), the oldest member player who is an adult or older inherits, the longest in the house if two are of an age. NPC members beyond what the heir's stage allows become wild vampires, the youngest first (HOUSE-3). With no such member the house falls: its NPC members become wild vampires and its structure is unclaimed. A lord can also hand the house to an adult player member from the command wheel, where the former lord stays a member, or disband it with `/house disband` |
| HOUSE-10 | A house is a target. A raiding party or a passing hunter party may raid it by day (GUILD-13, GUILD-14), and a human player can at any time |

**Not supported:** leading more than one house; inviting a vampire as old as the lord or older; turning a villager into a member.

**Enforcement:** server.

### 5.13 Hunt Vampires

**Actor:** Human, hunter. Any player for the lancet (GEAR-10).

Every human can fight vampires with stake, silver, garlic and holy water. One who proves themselves and joins the guild is a hunter, and only a hunter grows: hunter experience, the tree, ranks, contracts and trials all begin with the oath.

The hunter tree has a core that every hunter shares, and four paths, of which a hunter picks two. The core starts with Tracking, an active power that points toward the nearest vampire for a short time, which every path needs first. The rest of the core is what any hunter learns on the road: tougher hide, a harder stake, surer feet, eyes that clear faster, Second Wind, a short burst of speed and grit, and Bind Wounds, which dresses the hunter's own wounds. The gifts that last are player attributes (`nightborn:bite_resistance`, `nightborn:mesmerise_resistance` and `nightborn:faith`) that skills raise, so the features that use them read one number and never the skills themselves. A hunter must be online when the sun finishes the vampire to be paid.

| Path | What it gives |
|---|---|
| **Trapper** | Traps, nets and bait. The fight is won before it starts |
| **Faithful** | Faith as a weapon: stronger holy things, prayers that heal, and a blade blessed or set alight |
| **Slayer** | The duellist: dodges, counters, stakes thrown and driven home, and bite resistance |
| **Seeker** | Senses: longer tracking, warnings, a mark on a quarry, and sight that sees through a disguise, which a vampire player is told of |

| ID | Requirement |
|---|---|
| HUNT-1 | A hunter earns hunter points by staking vampire mobs and vampire players, or by downing one that the sun finishes before its escape ends. Downing alone earns nothing. A vampire, mob or player, pays more for each stage it has above fledgling (HOME-15, AGE-1). The Eldest also pays hunters, as RITE-6 says |
| HUNT-2 | Contracts from the Guild Master earn hunter points as well as their reward |
| HUNT-3 | The hunter tree has the core and four paths: Trapper, Faithful, Slayer and Seeker. The core and each path hold about ten skills, and every hunter has the core whichever paths they pick. Spending a first point in a path chooses it, and once two are chosen the others are locked, as bloodlines are (LINE-2, LINE-3). Hunter skills bought in an older world are reset and their points given back |
| HUNT-4 | The paths give what the table says. Bite resistance lets a human break free of a feeding bite sooner (FEED-7), shortens the Weakness after it (FEED-5) and lowers the chance of the sickness (TURN-1). Bite resistance and mesmerise resistance never pass 0.8, whatever is learned and worn. Skills, their ranks, costs and the rank each needs are data, and `docs/skills.md` lists them all |
| HUNT-5 | Tracking shows the direction of the nearest vampire within range for a short time |
| HUNT-6 | A hunter's active skills sit on the power bar (SKILL-4). They cost no blood and each has a cooldown |
| HUNT-7 | A vampire mob a hunter has wounded leaves a trail that the hunter's Tracking follows until dawn, back to its coffin. A contract for a vampire or a household comes with a rough map of the region its house lies in (GUILD-3) |
| HUNT-8 | Noise wakes a sleeping house. Running, jumping, breaking or placing blocks, opening a coffin lid and fighting each make noise, and enough of it wakes the vampires asleep within its reach, who rise and fight (COFFIN-12). Sneaking steps make none, though a sneaking player still makes noise lifting a lid or fighting. A vampire makes none to a sleeping house. How loud each act is and how far it carries are data |
| HUNT-9 | A house is guarded by day. An NPC household keeps a thrall or two of its own and, some days, one member awake, and a player's house has whoever its lord set to stand watch (FAM-9). A guard that sees an intruder wakes the house. A guard sees as any mob does, so a sneaking intruder is seen only closer, and a household's thralls never turn on vampires, nor its vampires feed on them |
| HUNT-10 | A human joins the guild in two steps. They prove themselves by staking a vampire, then ask a Guild Master, of a camp or a party, to swear them in from the command wheel (WHEEL-2). The oath makes them a hunter of the lowest rank (RANK-1), and is a moment (FACT-7). A human who has not joined earns no hunter experience, and a vampire can never join. Hunter points alone never make a human a hunter. Every player who chose a side before journals existed is given theirs when they next join |

| Rank | Deed |
|---|---|
| Initiate | Joining the guild (HUNT-10) |
| Hunter | Staking an adult or older |
| Veteran | Clearing a household by staking the last of it |
| Master | Ending the Eldest |

| ID | Requirement |
|---|---|
| RANK-1 | Every hunter has a rank in the guild. A rank is reached by earning a total of hunter points, none for Initiate, and doing its deed, and a deed done early counts |
| RANK-2 | Each rank gives more health and more damage against vampires, and nothing against anyone else |
| RANK-3 | A rank also opens the deepest hunter skills (SKILL-2), better stock and harder contracts from Guild Masters (GUILD-3) |
| RANK-4 | A rank is never lost, and is set aside with the hunter tree if its holder turns (TURN-6). A mark of rank, a shape as well as a colour, is drawn beside a hunter's name for everyone, hunter mobs and Guild Masters included (GUILD-15). A human who has not joined has none. A vampire player shows the rank that was set aside when they turned, or none, so the mark never gives them away (FACT-4) |

| ID | Requirement |
|---|---|
| GEAR-1 | A wooden stake is crafted from two sticks and used as a melee weapon. It hits lighter and faster than a sword and never sweeps |
| GEAR-2 | Silver ore generates underground, with a deepslate form, rarer than iron and needing an iron pickaxe. It drops raw silver, which smelts to silver ingots |
| GEAR-3 | Silver gives a sword, an axe and a stake tip. Silver wears faster than iron and hits as hard. A silver-tipped stake counts as both a stake and silver |
| GEAR-4 | A crossbow can fire stake bolts, silver bolts, holy water bolts and garlic bombs |
| GEAR-5 | A garlic bomb bursts into a cloud that weakens vampires inside it for a short time |
| GEAR-6 | Holy water is brewed from a water bottle and a silver nugget, or a cleric villager blesses a water bottle for an emerald as a trade. It comes as a drink, a splash and a lingering potion, made from the drink with gunpowder and dragon's breath as vanilla potions are. Holy water cannot tip arrows as a potion does: the holy water bolt is its ammo, and arrows carry it only as a coating (GEAR-9) |
| GEAR-7 | A Crucifix is crafted from gold or silver and held in either hand |
| GEAR-8 | Garlic grows wild in plains and around villages, can be farmed, and is crafted into Garlic Strings. A human can eat it raw for a little hunger; a vampire who eats it pays dearly (FEED-3) |
| GEAR-9 | Holy water or garlic can be applied to any weapon or ammo in a crafting grid. The coating gives extra damage against vampires and weakens them briefly, always less than silver does, and does nothing to anyone else. On a weapon it lasts a set number of hits on vampires or a set time, whichever runs out first; on ammo it lasts until fired, and Infinity never saves a coated arrow. A coated item shows its own sheen, holy water or garlic, wherever it is drawn, and the sheen goes when the coating wears off |
| GEAR-10 | A lancet is crafted from an iron ingot and a stick. Any player, human or vampire, uses it on a living creature that can be fed on (see Who Can Be Fed On) while carrying an empty Glass Bottle. It hurts the creature, fills one Blood Bottle of that creature's quality (FEED-11) and wears a little. It never takes a creature's last health, so it cannot kill, and never draws blood the creature does not have (CS-4). The creature takes it as it takes any other blow. A human who uses it while sneaking, aimed at no creature, bleeds themself the same way for a bottle of player quality. It does not work on another player. Bottling with a lancet earns no skill points (FEED-12). A servant set to fetch blood uses one the same way (FAM-9). How much it hurts is a server setting (CFG-5) |
| GEAR-11 | Weapons take six new enchantments, found as any vanilla enchantment that is not a treasure is. Three work only in a vampire's hand: Bloodletting gives a little blood with each blow, Exsanguinate opens the finisher bite at higher health (FEED-16), and Nightfall hits harder at night. Three work only against vampires: Consecration makes a coating last longer (GEAR-9), Heartseeker makes a stake hit harder (WEAK-7), and Daybreak hits them harder by day |

**Enforcement:** server.

### 5.14 Visit A Hunter Camp

**Actor:** Human, vampire.

| ID | Requirement |
|---|---|
| GUILD-1 | Hunter camps generate rarely in the open country of the overworld (plains, meadows and savannas), each with a few hunter mobs and a Guild Master. Hunters and the Guild Master are villagers, not humans: the villager head with its long nose and heavy brow, in villager skin tones and proportions. Hunters are weathered men and women in long coats, wide hats and leather, in at least 4 looks each, and a woman hunter has a bust sized the same way as a vampire woman's (HOME-18). The Guild Master is an older, better dressed villager who wears a mark of rank, in at least 2 looks. At ease they all stand and walk with their arms folded as villagers do, and unfold them to trade or to fight. Only the Guild Master trades and talks business. A human who right-clicks a hunter is told by name which Guild Master to speak to and which way he stands: their party's, or else the nearest within 32 blocks, or else to seek one at a camp. A hunter answers at most once every two seconds, and not while it is fighting that player. A vampire gets no answer, and the Grand Inquisitor never gives directions. A hunter or Guild Master hit by anything, a player or a vanilla monster included, fights back until it dies or gets away, and the hunters and Guild Master near it join in. Nothing holds this back, not a trade, an oath or what it was doing. Hunting a vampire comes first, and holy water and the crucifix are kept for the guild's foes. Hunters and Guild Masters never turn on a hunter or a Guild Master this way |
| GUILD-2 | Hunter mobs attack vampire players and vampire mobs on sight with stakes, so a vampire they bring down is finished. One that can see a vampire within 8 blocks goes for it at once, and one further off a moment later. Nothing puts it off: not what it is doing, whom its party guards, a trade or the time of day. The Guild Master fights the same foes in the same way as his hunters, with a kit drawn from his own list in data, silver to suit his rank, and the powers of his skills (GUILD-16). A fight closes his counter, and one that finds him swearing a player in breaks off the oath, which is sworn all the same. Each hunter carries one kit: a stake in the hand, or a crossbow that fires stake bolts from a distance. A hunter whose kit has a crucifix holds it up at a vampire within its reach (WEAK-3) and puts it away after, and one whose kit has holy water throws a flask at a vampire too far to strike, then waits before throwing another. A hunter keeps its hands empty until it has a vampire to hunt, then draws its weapon, and puts it away a few seconds after the hunt ends. Hunters and the Guild Master speak with villager voices, lower for a man and higher for a woman: they mutter at rest, call out when they sight a vampire, grunt at each strike and cheer when one is staked. They leave humans alone unless a human strikes them (GUILD-1) or INQ-6 says otherwise. Hunters and the Guild Master of a hunter camp stay near it, and those of a hunter party stay with their party (GUILD-7) |
| GUILD-3 | The Guild Master sells hunter gear and writes contracts on paper, such as staking vampires, staking an elder, or clearing a vampire structure by staking the last of its household. He trades with any human, tells them what kind of vampire house is rumoured to lie nearest, how far, which way and the coordinates of a spot at its entrance where a player can stand, depth included, without stalling the game while he thinks, and swears in one who has proved themselves (HUNT-10). For a tithe of 16 emeralds he sends a sworn hunter of the rank of Hunter or above to that house by a holy rite (GUILD-18), and not the same hunter again for 20 minutes. To a hunter he also gives contracts and trials (CHAL-1), and tells them their rank and what the next needs (RANK-1). His offers and contracts are data, grow with the hunter's rank (RANK-3), are reached from the command wheel (WHEEL-2), gear and contracts each from its own entry, trade through the vanilla trading screen, and renew each day. The houses he can have heard of are the `nightborn:vampire_houses` structure tag |
| GUILD-4 | Contracts pay in emeralds, silver and gear, on top of the hunter points HUNT-2 gives. A contract is an item that keeps its own count. One stake marks one open contract. A vampire player counts at their stage (AGE-1), and a vampire who lives alone is the last of its household. A fulfilled contract, in either hand, is handed to a Guild Master, who takes it and pays once |
| GUILD-5 | A vampire player cannot trade, take contracts or trials, or hold a rank with any Guild Master |
| GUILD-6 | Hunters also travel. A hunter party of 4 to 8 hunter mobs and one Guild Master appears now and then on the surface of the overworld near a player, the way a vanilla pillager patrol does. They are the same mobs as a hunter camp's, so they fight, trade, give contracts and are named as GUILD-2 to GUILD-5 and NAME-1 say. Only how they appear, and that they travel, is different. The biomes parties appear in are data |
| GUILD-7 | A party wanders one area looking for vampires, then moves on to a new area. It keeps together around its Guild Master. A party on the move that no player has been near for some time leaves the world, as a patrol does. A party in camp never does |
| GUILD-8 | As evening falls a party pitches a temporary camp where it is: a lit campfire and tents, each a small canvas ridge tent over a bed, open at the end facing the fire so the bed can be seen and reached, that goes up with a short pitching motion. The tents stand with a walkable gap between them and all round the fire, and nothing stands across a tent's open end. It pitches only where every block of the site is natural ground, a tree or a plant, and may clear the trees and plants. Any other block on or beside the site rules it out, so a party never breaks, replaces or moves anything a player built. With no such ground in reach it walks on and goes without a camp that night. The tents, the layout of the camp, and the blocks that count as natural ground or as trees and plants are data, and a layout that breaks the gap rule is refused when it loads |
| GUILD-9 | A party stays in one area for one night as a rule, never fewer than one and never more than three, whatever it is doing, a guard party included (the Hunted step of 5.16). By night some of the party sleep in the tents and the rest stay awake and search the ground around the camp. By day they wander the area and come back to the camp at evening. Any mob, hunter or not, finds its way round the canvas and goes into a tent only by its open end, and a hunter or Guild Master never picks a spot inside or on top of a tent to walk to while there is clear ground to pick. Outside a fight, one that is walking somewhere but stays on the same spot for three seconds gives up and picks somewhere else. Nights are counted by the world's day count and checked against the game's own running time, so a party whose stay ran out while its chunks were unloaded breaks camp when they load, a clock set back or stopped never keeps a camp standing, and a camp whose count makes no sense breaks camp at once. A passing raid (GUILD-14) only starts on the march, so it never holds a camp |
| GUILD-10 | On its last morning the party puts out the fire, takes down its tents and beds and moves on. Each tent folds flat to the ground before it goes. Only the unlit campfire stays behind. The tents and beds of a temporary camp drop nothing, whoever removes them (SAFE-1), and breaking any part of a tent takes the whole tent down. A player whose spawn was a tent's bed loses it as with any broken bed. A party never pitches again on the spot it just left. A camp whose Guild Master is dead or gone is taken down the same way once its nights run out |
| GUILD-11 | Now and then, at night, vampire bats fall on a pitched temporary camp and bite the hunters (HOME-10). The hunters awake fight them, the sleepers wake and join, and the fight lasts until the bats are dead or gone |
| GUILD-12 | A veteran hunter is a named hunter mob (NAME-1) of Veteran rank (GUILD-15), stronger than a hunter and armed with silver, with attacks of its own that each have a pose and a sound before they land. One leads every raiding party the guild sends against an elder or an ancient (GUILD-13). Veterans, their kits and their attacks are data, and `docs/challenges.md` lists the attacks |
| GUILD-13 | The guild sends a raiding party against a vampire player whose notoriety passes the first threshold (INQ-1), or who is a known face in a hunted village (SUSP-3). It is made of hunter mobs that fight as GUILD-2 says, but it is not a hunter party: it has no Guild Master, trades with nobody, pitches no camp and follows none of GUILD-6 to GUILD-11. It is sized and armed to its quarry's stage: up to twelve hunters, with more silver, crucifixes and holy water for an older one. It goes to the vampire's house by day and stakes whoever sleeps there, or comes by night for a vampire with no house. It opens doors and coffin lids, and breaks, places and takes nothing (SAFE-6). It leaves when its quarry is staked, when it is beaten, or at the next dusk |
| GUILD-14 | A hunter party on the move (GUILD-7) that comes within sight of a vampire player's house by day raids it as a raiding party does, then goes on its way |
| GUILD-15 | Every hunter mob has a guild rank, Initiate, Hunter, Veteran or Master, with the health and the damage against vampires that a player of that rank gets (RANK-2). Rank is rolled when the hunter first appears: most are Initiates and Hunters and few are Veterans or Masters. A raiding party sent after an older vampire rolls higher ranks (GUILD-13), a veteran hunter is always a Veteran and a Guild Master is always a Master. The rank mark shows beside the hunter's name (RANK-4). A hunter from an older world is given a rank and a build when it loads. The rank odds, and the raiding party's for each stage, are data |
| GUILD-16 | A hunter mob holds the hunter core and two of the four paths, picked at random, and never more than two. It starts with the hunter points of its rank, at least the points that rank needs and fewer than the next rank's, and spends them through the player's own purchase rules: each skill's cost, the skills it needs first, rank gates and the ranks of a passive bought in turn (SKILL-2), so it never holds a set of skills a player could not have bought. What it buys first leans toward its kit and its paths, so two hunters of one rank and the same paths can still differ. A Guild Master holds every skill of the core and his two paths. Each hunter keeps earning hunter experience while it is loaded, a little at a steady interval and more each time it stakes a vampire, on the same curve as a player's (HUNT-1), and buys a skill as soon as it can afford one. It rises a rank when it holds that rank's points, as a player does (RANK-1), and a hunter mob's deeds are waived. Its rank, paths, points, experience and skills are saved with it. The leanings and the rates are data |
| GUILD-17 | A hunter mob uses each fighting power it holds through the same code as a player, with the same cooldowns and costs, as a vampire mob does (HOME-16). Each has a clear pose and sound before it lands (HOME-17), drawn from the hunter's own clips: the crucifix raised for the Faithful's prayers, the stake drawn back for Heartstrike and Riposte, the crossbow for Quick Shot, Net Shot and Mark Quarry, a throw for Stake Throw, Snare and Flash Trap, a quick step for Sidestep, and a rallying shout for Second Wind, Onslaught, Bind Wounds and Killing Ground. It picks them as a vampire mob does (HOME-16): a net for a vampire that runs, a snare or a flash trap laid at its own feet as a vampire comes at it, a net or a thrown stake for one in the air, Rebuke, Riposte, Sidestep or a thrown stake against one winding up, and Heartstrike or Onslaught for one nearly down or held. Older ranks think about their powers more often, and hold more of them. Its passives apply as a player's do. Powers that only inform a player (Tracking, Read The Scene, True Sight and Revelation) and Bait are not used by hunter mobs. A hunter with no skill for an item it carries still uses the item as GUILD-2 says. The Grand Inquisitor keeps his own fight (INQ-1 to INQ-16) |
| GUILD-18 | A Guild Master can send a hunter to the nearest vampire structure, from the command wheel (WHEEL-2). Who may ask, the tithe and the wait are in GUILD-3, and he never casts it for a vampire. A server setting opens the rite to every human, and one who has not sworn then needs no rank. The tithe is shown on the wheel and taken only as the hunter leaves, so a rite that is broken or finds no way costs nothing. Without the rank or the tithe he says so and casts nothing. He turns to the hunter and casts a short holy rite with his hand raised, a bell sounds and pale gold light gathers about the hunter. The hunter then rises and is drawn up into the light, and arrives at the entrance of the nearest house he has heard of (GUILD-3), stepping out of the same light. Each step has its own layered sound, a tolling bell and a hum under chimes that rise as the light gathers and fall as it settles, and both ends have their own light, which is white and pale gold, steady and never flashing, and both are poses on the Guild Master and the hunter that every player near sees, so nothing rests on particles alone. For 3 seconds after arriving the hunter cannot be harmed, shown by the Sanctuary effect with its icon and a glow about them, and it ends at once if they strike anything. The hunter always arrives on solid floor with two blocks of air, on the step outside the entrance or the nearest safe spot to it that is still outside, never in the doorway or beyond it, never in a wall, over a drop, in lava or water, on a block that harms, or within reach of the household. The house is found without stalling the game, and the ground at the entrance is loaded before the hunter is moved. While that takes longer than one casting, he casts the rite again, round after round, and the hunter leaves at the end of a round. If he knows of no house, or no safe spot is found there, or the way will not open in time, nobody is moved and he says which. The hunter steps out facing the house. The rite is broken if the hunter walks away or is hurt, or the Guild Master is hurt or starts a fight, before the hunter leaves, and he says so. He tells a hunter who must still wait how long is left. The timings, sounds, light and reach of the rite are data, and who may ask, the rank, the tithe, the wait and the length of the shelter are server settings (CFG-5). Vampires never use the rite, and it never sends anyone back or to a hunter camp |

**Enforcement:** server.

### 5.15 Find A Vampire Household

**Actor:** Any player.

Vampires are rare. They live in households hidden in five kinds of structure, each placed to fit the land around it. Each structure has a heart, the room its household gathers in, and its fog is thickest there. Every room, yard and wall walk of a structure can be reached on foot, by stairs rather than ladders, so its vampires go everywhere a player can. Every doorway, passage and stair has room for the tallest vampire of its household, on foot or floating. The game's pathfinding gives a mob its height plus one, rounded down to whole blocks, and a floating vampire rides a block above the floor, so the shipped builds (up to 2 blocks) need three blocks of headroom, kept over the whole climb of every stair. A castle's vampire lord stands taller, so his way from the throne out through the gate is four blocks high. Every coffin has a way out to open ground and back. That way is two lanes wide, apart from doorways no deeper than a thick wall, and no cobweb, drape, low beam, chandelier or furniture stands in its walking space. A data pack that adds a taller build must give it rooms to match. No floor is left sealed off, and every coffin, chair and lectern a household uses can be walked to. Old blood stains lie in clusters on the floors and low on the walls of every vampire structure, where its vampires have fed.

| Structure | Where | Household |
|---|---|---|
| Castle | Dark forests, old growth taiga, taiga, birch forests, swamps and mangrove swamps, never on mountains | A vampire lord and a court of four to six vampires behind a battlemented curtain wall with corner towers and a gatehouse. The lord's throne stands in the great hall of the central keep, the castle's heart, with a crypt, a bedchamber of coffins and a library behind it. Coffins, a blood fountain or a graveyard in the courtyard, and rich loot |
| Mountain castle | The highest ground of windswept hills, windswept gravelly hills, windswept forests, jagged peaks, stony peaks, frozen peaks and snowy slopes, by far the rarest | Twenty elder and ancient vampires and the Eldest in the highest room, and the best loot |
| Crypt | Under a parish church in its walled, raised churchyard, standing just outside a village on dry, level plains | A few vampires below a working church, reached only by the vestry stair |
| Ruined manor | Dark forests | A vampire family in an overgrown brick house with a corner turret or tower, behind railings and a carriage sweep. A stair hall rises two storeys to a gallery, with a drawing room, library, dining room and kitchen below, bedchambers with canopied coffins above, a study at the top of the turret, and a glasshouse falling to ruin at its side |
| Underground lair | Deep caves, about y -47 to -4, cut into the rock beside a natural cave where no sun reaches | A den round a blood pool or a grotto with a spring and a coffin ledge, with more elders and ancients than a crypt. Its way in is a rough passage, two wide, with a cobweb strung across the top of its mouth and a smear of blood, easy to miss, that opens onto the cave. Its household goes out through the caves to hunt like any other (HOME-13) |

| ID | Requirement |
|---|---|
| HOME-1 | Each structure type generates rarely, in biomes that suit it, and blends into the land around it. A structure on the surface sets its height by its way in: the floor of its gate or door is level with the ground just outside it, on flat land and on a slope, and a mountain castle stands so its gate opens onto the very top of its mountain, so a player can always walk in. An underground lair is moved and lowered or raised until its passage opens onto a natural cave a player can walk to, on a floor that is not over a drop and away from water and lava, and a short path of rock is laid from its passage into the cave where the cave floor has fallen away. Where no such cave is near, no lair is placed |
| HOME-2 | Each structure holds several vampires. A mountain castle holds twenty-one by default (HOME-22) |
| HOME-3 | Each type has several layouts, chosen at random |
| HOME-4 | Fog hangs around and inside every vampire structure, thicker near its heart |
| HOME-5 | A household feels lived in: its vampires have their own personality and hobbies, talk to and argue with each other, react to each other's deaths, follow a nightly routine and use the furniture of their home: chairs, candles, drapes, books and gramophones. They open wooden doors and close them behind them as villagers do, and leave iron doors shut. At dusk a household gathers at its heart, lights its candles and opens its drapes. Through the night each vampire sits, reads, talks with a housemate or wanders the house as its temperament leans, and a hot temper on either side turns talk into an argument. Before dawn they put out the candles and draw the drapes. A vampire whose housemate dies within reach mourns it for a while, even one asleep in its coffin |
| HOME-6 | Household vampires settle under a roof, never in the open sun, sleep in coffins by day, all but one who may keep watch (HUNT-9), and wake at dusk. Each structure holds the coffins (DECOR-5) that suit it, by default: stone brick and gothic stone brick coffins in crypts, deepslate brick and polished blackstone brick in underground lairs, dark oak and spruce in ruined manors, dark oak in a castle with gothic stone brick around its heart, and gothic stone brick and dark oak in a mountain castle. A data pack can change these (HOME-12) |
| HOME-7 | A castle's vampire lord is a boss with a health bar and its own powers: it calls every vampire of its household nearby onto its foe, and calls a swarm of vampire bats to its side. These powers are skills only vampire mobs can hold, never shown on a player's tree. The lord has a body of its own and always stands taller than any vampire of its household, at a height its look names in data. It settles in front of its throne and takes a stage above ancient that no other vampire rolls. It is downed and escapes like any vampire, its bar turns white while it is downed and purple while it escapes, and only a stake or the sun ends the fight |
| HOME-8 | Structures hold loot chests with Blood Bottles, clothing, jewellery, decor, gold and gems, gothic music discs (DECOR-8), relics (RITE-1) and rare items found nowhere else. A mountain castle holds no relic. They never hold what is made to kill or ward off a vampire: stakes, silver weapons, ingots and nuggets, garlic, holy water, crucifixes, bolts and hunter accessories, which is the `nightborn:anti_vampire` item tag. Hunter camps keep those. A vampire lord or the Eldest may still drop a silver weapon, a trophy taken from a hunter |
| HOME-9 | Vampire mobs live in vampire structures and in players' houses (HOUSE-2), as members of a household, all but the few wild ones (HOME-25). Each remembers its structure and its own coffin, which starts assigned to it (COFFIN-4), so it cannot be broken until that vampire dies to the sun or a stake. A member never spawns at random anywhere else and never despawns |
| HOME-10 | Vampire bats spawn rarely, and only underground where a vanilla bat could spawn, inside a vampire structure, or about a temporary camp they attack (GUILD-11). Under cover, or at night in the open, they swoop at warm-blooded creatures and bite them (TURN-1). A vampire bat is bigger and darker than a vanilla bat, with soot fur, burgundy wings and red eyes that always glow. It hangs wrapped in its wings, flies, swoops on its prey and bites |
| HOME-11 | Vampire mobs attack only when hungry or provoked. Being hurt, or a housemate being hurt nearby, provokes them, and a provoked vampire fights its attacker by day or night, fed or hungry, whatever its kind or stage, a watcher, a sleeper woken by the blow and a household member included, until it must escape the sun, falls back at low health, is downed, or the attacker leaves. Only a downed vampire, or one fleeing to shade or to its escape, does not turn on whatever strikes it. A fed vampire may watch, follow or warn off a stranger, but does not attack. They never start a fight with another vampire, whether a mob or a player, with two exceptions: a nemesis fights its quarry (NEM-4) and a house duel is fought (HOUSE-8). One struck by any vampire, of any stage, fights back (RITE-7). On Peaceful they never hunt players |
| HOME-12 | Structure layouts, the coffins each structure holds, loot, relic chances, structure spawn rates, the biomes each structure generates in, household sizes and the vampire that heads a household, vampire stages and their odds per structure type, the skills vampire mobs pick, their temperaments and hobbies, the blocks they read at and the lights they tend, their looks, sizes, builds and outfits, the name pools and epithets, the name colour of each stage and of the Eldest, and the biomes and structures vampire bats spawn in are data, changed by a data pack |
| HOME-13 | Vampire mobs have blood that drains over time. A hungry vampire may leave its structure at night to hunt humans, villagers and animals within a range of home. Not every vampire leaves every night, so a household is rarely empty, and one setting out sometimes brings one or two vampire bats. Each heads back once it has fed, or in time to reach its coffin by dawn. One caught out at dawn seeks shade (SUN-4). A lair household goes out through the cave its passage opens onto, where the lair cuts the cave open to the same room as its passage, and hunts what it finds below ground, or above it where the caves reach the surface |
| HOME-14 | A provoked vampire gives up the chase past a range from its structure and goes home |
| HOME-15 | Each vampire mob has a stage, fledgling, adult, elder or ancient, which sets its health, speed, damage, how many skills it has and how much blood it can spend on powers. Its skills come from the vampire tree and, unlike a player's, may come from all three bloodlines. The older a vampire and the more skills it has, the rarer it is, the harder it fights, and the more hunter points (HUNT-1) and the better loot it gives. Only the Eldest has every skill (RITE-3) |
| HOME-16 | A vampire mob uses every power a player with the same skills could use, with the same blood costs, cooldowns and weaknesses, through the same code. It fights like a skilled player: closing fast, leaping, dodging, mesmerising and taking bat form, and pulling back to recover. One that knows Vanish may vanish to one side, or away, when its target swings at it or draws a ranged weapon on it. How often each stage does so is stage data (HOME-12). Older stages use more powers, and use them more cleverly. Vampire and hunter mobs choose powers through one shared brain: each power's data lists the moments it is for and what makes it a good choice, and the mob casts the best one it can afford for what it is doing (ADR 0018). In a fight a mob reads its foe: it closes on one at range, nets or pins one that runs, stuns one winding up a blow, finishes one nearly down and wards against the kind of foe in front of it. It never slows a foe already slowed or held, never casts a buff it already has running, never leaps at a foe already in reach, and never spends a costly power on a foe one blow from down. A healthy mob saves its blood and its long cooldowns, and one that is losing spends them freely. When two powers suit the moment about equally, chance picks between them, so fights do not all look the same. A vampire pulling back, or one caught by the sun, uses Vanish, Shadow Step or Long Leap to get away toward where it is already heading, which for the sun is the nearest shade. A vampire that neither gains on its foe nor travels for a moment is stuck, while one that keeps moving after a fleeing foe is not: it lands and stays on foot a while, uses Long Leap, bat form or Vanish to get there if it knows one, then steps back and comes at it again, and after a few tries leaves that foe alone for a while unless the foe strikes it or its household is roused against it. A step back never keeps a hurt vampire from falling back. How long each stage waits before it counts itself stuck, and how it tries again, is stage data, and none waits more than two seconds |
| HOME-17 | Every power a vampire mob uses has a clear tell in sight and sound just before it lands, never colour alone (ACC-1), whether it is cast to fight or to get away. A power used as a dodge has no tell before it, since it answers the attacker's own move: its smoke and sound mark it |
| HOME-18 | Vampire mobs are men and women in at least 10 looks, from 1.5 to 2 blocks tall in slim, average and broad builds. Women have a bust whose size is rolled once when they spawn, between a smallest and a largest. Their hitbox, eye height and reach follow their size, except that the hitbox is never wider than 0.6 blocks, so every build fits through an open door. Outfits are chosen by era (LOOK-4). A vampire keeps its look and size for life, the same on every client, and an older stage reads as older |
| HOME-19 | Vampire mobs bare their fangs and hiss, with animation and sound, when they notice a threat, are provoked, face garlic or a crucifix, or are about to strike. They have their own idle, hurt, death, talk and argue voices, male and female, pitched by size |
| HOME-20 | Vampire structures hold gramophones loaded with gothic music discs (DECOR-8). Each starts its disc now and then while a player is near, and its disc can be taken. A gramophone's music never plays over other music the mod or the game plays for the same player |
| HOME-21 | A mountain castle is a vast stone castle that grows out of the summit of a large mountain. Sheer pale walls rise straight from the rock, with round and square towers under steep red-brown conical and hipped roofs, battlements, small deep windows and timbered upper floors. Courtyards, stairs and walkways climb from its gate to one tall keep, and in cold biomes snow lies on its roofs and ledges. It generates only on the highest ground of the biomes the table above lists |
| HOME-22 | A mountain castle's household is, by default, twenty elder and ancient vampires, about three elders to every ancient, and the Eldest (HOME-12) |
| HOME-23 | The highest room of a mountain castle's keep belongs to the Eldest of RITE-3, who fights as RITE-4 and RITE-9 to RITE-16 say and treats players as RITE-7 says. It sleeps by day in its own coffin in that room, keeps to that room at night, takes no part in the household's routine (HOME-5) and never leaves the castle to feed. Its arena is that room (RITE-9). Ending it needs no ritual and no relic, and gives what RITE-6 says. Once a stake or the sun ends it, that castle never has an Eldest again |
| HOME-24 | A vampire structure whose household is all gone, and that no player has claimed (HOUSE-1), does not stay empty. After a time drawn between 15 and 40 days by default, counted from when it was emptied or let go by a house, a whole new household moves in at once, as when the structure was generated. Turning the world's clock back never delays it past the longest time. A mountain castle's Eldest never returns (HOME-23) |
| HOME-25 | Very rarely a wild vampire appears at night, far from any player, and travels alone. It never stands idle for long: by night it roams far, by day it keeps to shade and wanders from shade to shade, and it pauses between legs to look about. Some appear hungry, half by default, so one may be met hunting on its first night. One that comes upon an empty, unclaimed vampire structure always claims it, and a new household arrives with it (HOME-24). One that no player has been near for some time leaves the world, as a hunter party does (GUILD-7) |
| HOME-26 | A new player meets signs of vampires in their first days. A wild vampire watches them from a distance on one of their first nights, leaves when approached or at dawn and never starts a fight, hungry or not, HOME-25 notwithstanding. If anything strikes it, it stops watching and fights back as HOME-11 says, then lives on as any wild vampire (HOME-25). Drained animals lie in the country round a vampire structure, and a vampire bat may watch from a branch. Villagers near a structure mutter about it, and a cartographer or a cleric sells a rumour map marking the nearest one's entrance. A hunter party crosses their path early, trades, tells them how to join the guild (HUNT-10) and warns of the nearest house (GUILD-6), unless hunter parties are turned off. Turning the world's clock back moves a player's first days back with it. The signs round a structure follow the mob spawning rules, and a drained animal still falls after a restart. How soon and how often is data |
| HOME-27 | Every lectern in a vampire structure holds a written book when the structure generates: a ledger, diary, letter or guide written by the vampires who live there, with a title, a vampire author and 2 to 6 pages. Each kind of structure draws at random from its own shelf of books, so two houses of a kind rarely hold the same one, and one building does not show a book twice while its shelf has books to spare. At least 20 books ship. The books are proud, old and petty, and are the one place the mod allows itself dry humour: they never break the world or use modern phrasing. They may hint at real things, such as the heart of the house, the Eldest, old blood fonts and what hunters carry. Titles and pages are shown in the reader's language. The book can be taken like any other. Books and shelves are data (COMPAT-5). Structures that generated before this are not changed |

**Not supported:** a household replacing its losses one by one; vampire mobs appearing outside a household, other than wild vampires.

**Enforcement:** server.

### 5.16 Keep A Village Quiet

**Actor:** Vampire, villager.

A village notices what feeds on it. A careful vampire keeps one for years. A careless one finds garlic on every door and hunters at the well.

| Step | What the village does |
|---|---|
| Wary | Doors shut at dusk, villagers stay in at night and charge strangers more after dark |
| Warded | Villagers hang garlic strings by their doors and raise a crucifix at the bell |
| Hunted | The village sends for the guild. A hunter party comes, camps beside it and guards it for as long as it is hunted. Its camp keeps to GUILD-9: when its nights run out it breaks camp and pitches a fresh one nearby, on a new spot |

| ID | Requirement |
|---|---|
| SUSP-1 | Every village has a suspicion. It rises most when a villager sees a vampire feed and reaches the bell (FEED-13). It rises a little for each victim found: a villager who wakes weakened from a bite, a drained animal, a villager gone missing. It rises when a villager sees a vampire levitate, take bat form, use a power, burn in the sun or bring up food, and slowly while a thrall or a human familiar taken from the village lives among its neighbours. What each adds is data |
| SUSP-2 | As suspicion rises a village becomes wary, then warded, then hunted, as the table says, and steps back down as it falls. Villagers place their garlic and crucifix only on free blocks and take them down again, and never change a block a player placed (SAFE-6). A village's suspicion is never shown as a number: its doors, its wards and its guards show it |
| SUSP-3 | A vampire player a villager saw for what they are is a known face in that village. Its villagers flee them and will not trade with them, except one they have charmed (MIND-2), the hunters who guard it go for them first, and once it is hunted the guild raids their house (GUILD-13). A vampire nobody saw feels only what the whole village does |
| SUSP-4 | Suspicion fades with each night that passes without incident, and a night when a thrall or human familiar taken from the village still serves is not one. A villager charmed (MIND-2), and gifts and fair trades from a known face, lower it sooner and can clear the face, since a village whose suspicion falls to nothing forgets every face. A villager who sees anything SUSP-1 lists runs for the bell as FEED-13 says. The sighting counts, and the vampire becomes a known face, only when the bell rings, so a witness stopped first tells nobody |

**Not supported:** suspicion of a human player; a village attacking a vampire's house itself.

**Enforcement:** server.

### 5.17 Make A Nemesis

**Actor:** Any player.

Some of those who survive a player do not forget them.

| ID | Requirement |
|---|---|
| NEM-1 | A named mob can become one player's nemesis: a vampire mob that a human downed and that escaped (CORE-8), a hunter mob that lived through a fight with a vampire player, and a member of an NPC household that a vampire player struck or drew a housemate away from (HOUSE-2). No boss becomes one, since it never leaves home (HOME-7, HOME-23), and no mob a boss called to its fight (RITE-10, INQ-10) |
| NEM-2 | A player has at most three nemeses at once by default, and no more are made while they have that many. A nemesis belongs to one player and is an ordinary mob to everyone else |
| NEM-3 | A nemesis grows. Each time it survives a fight with its quarry it heals, and gains a stage or a skill if it is a vampire or a better kit if it is a hunter. It may take a new epithet that tells the story, such as the Unbowed (NAME-2) |
| NEM-4 | A nemesis comes for its quarry on a night it chooses, wherever they are on the surface, alone or with one or two of its own. It goes home after, or keeps to the country round its quarry if it has no home. A nemesis never leaves the world and a party hunter who becomes one leaves its party, whatever GUILD-2, GUILD-7 and HOME-25 say. Nights between its visits are a server setting |
| NEM-5 | A nemesis speaks to its quarry in chat, under its name, when it sights them, when it downs them and when it flees. Its lines are data and are translated (ACC-5) |
| NEM-6 | Ending a nemesis pays its quarry a large, set amount of skill experience of their own side, vampire or hunter, and nothing to a human who has not joined the guild (HUNT-10). It drops a keepsake that bears its name for any quarry |
| NEM-7 | A player's nemeses are listed in their journal, whichever they have (GUIDE-3). A human who has not joined the guild has no journal and knows a nemesis only by its taunts. A nemesis forgets a quarry who changes faction |

**Enforcement:** server.

### 5.18 Sleep In A Coffin

**Actor:** Vampire.

| ID | Requirement |
|---|---|
| COFFIN-1 | A coffin opens and closes, and a vampire can lie in it by day, from dawn to dusk whatever the weather, as a human lies in a bed by night. Using it lies a vampire down; a human, or a vampire who sneaks with an empty hand, opens or shuts the lid instead. The lid shuts over a vampire who lies down, opens when they rise, and stays shut while someone is inside unless a player opens it (COFFIN-9). It softens a fall like a bed, but nothing bounces off its lid. Two cells long, or two cells tall when standing (COFFIN-10), it is crafted like a bed from the planks or bricks it is made of (DECOR-5), with red wool for the lining and a gold nugget for the fittings |
| COFFIN-2 | Enough vampires sleeping in coffins skips the day to dusk, as beds skip the night to dawn |
| COFFIN-3 | The share of vampire players who must sleep is a server option, 50% by default. Only vampire players online in that dimension count. With none online, nothing skips the day |
| COFFIN-4 | A vampire, player or mob, has at most one assigned coffin. It is a player's respawn point, as a bed is, and a vampire mob's home coffin (HOME-9). Sleeping in an unassigned coffin assigns it to that vampire and frees the one they had before. A vampire can sleep in any coffin no one is lying in, including another vampire's assigned coffin, but that assigns nothing and leaves their respawn point where it was |
| COFFIN-5 | A vampire player with no assigned coffin, or whose assigned coffin is missing, respawns at world spawn, as with a missing bed. Since dying to the sun or a stake unassigns the coffin (COFFIN-13), the coffin respawn applies only after the void or an operator's `/kill` (CORE-7) |
| COFFIN-6 | A human can open a coffin but not sleep in it |
| COFFIN-7 | A closed coffin shades the vampire inside from the sun, smothers any flames they carry in, and hides them from view, unless the coffin itself is burning (COFFIN-14). An open one does none of these |
| COFFIN-8 | Vampire players never count toward the bed share, and humans never count toward the coffin share, so neither side blocks the other's sleep |
| COFFIN-9 | Any player can sneak and use an occupied coffin with an empty hand to open or shut its lid. An open lid shows the vampire inside and no longer shades them, so direct sun reaches them (SUN-1). The vampire, player or mob, sleeps on until hurt or woken by noise (HUNT-8). Using it without sneaking does not open it (COFFIN-1), and a second vampire cannot lie down in it (CS-2) |
| COFFIN-10 | A coffin placed while sneaking stands upright, two cells tall, its back to the block behind and its lid facing the player who placed it. A standing coffin follows every coffin rule, a vampire sleeps in it standing, and one who respawns at it stands on the floor in front of it. Where a flat coffin needs room above it, a standing one needs the two cells in front of its lid clear |
| COFFIN-11 | A vampire asleep in a coffin, player or mob, lies or stands with arms crossed over the chest |
| COFFIN-12 | A vampire waking in a flat coffin rises rigid, pivoting up from the heels until upright in about a second, then steps off, a second and a half in all. One waking in a standing coffin steps forward out of it in a second. The lid opens as the rise starts. It cannot move until it stands clear, and damage, a second wake-up, leaving the coffin by hand and dusk do not cut the rise short. A vampire already on fire when woken, by the sun through an open lid or a burning coffin (COFFIN-14), skips the rise and wakes at once, as does a player teleported away. It always ends on a safe free cell next to the coffin, facing the way it rose |
| COFFIN-13 | Only the owner can break an assigned coffin. Other players, mobs, explosions and pistons leave it whole, though creative players and operators can still break it. A coffin is unassigned when its owner dies to the sun or a stake, is made human again (CFG-8), is reset by a hardcore death (CORE-10), or breaks it themselves |
| COFFIN-14 | A coffin made of a flammable material catches fire as that material does. While it burns it is drawn ablaze with the crackle of fire, and it wakes anyone inside and sets them on fire, open or shut. No vampire can lie down in a burning coffin. The fire burns out on its own after a while, and rain, water or a player putting it out ends it sooner. Fire never destroys a coffin, of any style, assigned or not |
| COFFIN-15 | A vampire player who sleeps a whole day in a coffin, skipped or not, which means asleep in it when the day is skipped, or from within a set time after dawn, the first hour by default, until dusk, heals fully, loses no blood while asleep and wakes rested: a little faster and stronger for the night that follows, and more so in a coffin of their own house, one assigned to its lord or a member (HOUSE-7) |

**Enforcement:** server.

### 5.19 Furnish And Dress

**Actor:** Any player.

| ID | Requirement |
|---|---|
| DECOR-1 | Gothic furniture: candelabras, chandeliers, velvet drapes, stained glass, ornate chairs and tables, gilt portraits that hang on the side of a block and show one of three sitters set by where they hang, and stone gargoyles that face the player who placed them. Drapes open and close as a whole column, a chair seats one player at a time, and stained glass needs Silk Touch like vanilla glass |
| DECOR-2 | Gothic building blocks in stone and dark wood: gothic stone bricks in plain, cracked and mossy forms with stairs, slabs and walls, carved gothic stone, and ebony planks with stairs, slabs and a fence. Castle building blocks: pale limewashed bricks in plain and worn forms with stairs, slabs and walls, plaster, and red-brown clay roof tiles with stairs and slabs. Every one is crafted: limewashed bricks from stone bricks and bone meal, worn ones in a furnace, plaster from sand, clay and bone meal, and roof tiles from terracotta |
| DECOR-3 | Blood decor: blood fountains, blood pools, placeable splatter decals and chalices. A blood fountain flows until it gets a redstone signal and starts again when the signal ends, and every start and stop plays its own animation. A blood pool lies flat on a floor. Blood splatter hangs on any face of a block in one of four shapes, set by where it is placed. A gold chalice is crafted from gold ingots and holds up to 7 Blood Bottles of one quality. Pouring a Blood Bottle into it adds one, a bottle of another quality is refused, and a glass bottle takes one back out, keeping its quality (FEED-11). A vampire can drink one bottle's worth straight from a chalice with an empty hand, as from a bottle. Breaking a chalice spills what it holds, as breaking a cauldron does. Placed splatter follows the gore option (GORE-3): fresh on Full, dried and dark on Reduced, and not drawn on Off |
| DECOR-4 | Hunter decor: weapon racks, wanted posters, trophy mounts and crucifixes, each hung on the side of a block and dropped when that block goes. A weapon rack holds two weapons, one above the other, from the `nightborn:weapon_rack_holds` tag, which starts with swords, axes, bows, crossbows, tridents, maces and stakes. Using a weapon on a free hook hangs it there, using an empty hand on a hook takes back what hangs on it, and breaking the rack drops all it holds. A wanted poster shows one of three faces, set by where it hangs. A trophy mount shows a fanged vampire skull. A gold or silver Crucifix used on the side of a block hangs there and breaks back into the same Crucifix, and a hung crucifix wards like a crucifix shape (WEAK-4) |
| DECOR-5 | Coffins in two styles. Wooden coffins are narrow at the head and foot and widest at the shoulders, made from oak, spruce, dark oak, mangrove, cherry or crimson planks and drawn with that wood's planks. Their hinged lid creaks open and shut, and they sound and break like their wood, fastest with an axe, and catch fire as it does (COFFIN-14), so a crimson coffin never catches fire. Stone coffins are plain rectangular boxes made from stone bricks, deepslate bricks, polished blackstone bricks or gothic stone bricks (DECOR-2) and drawn with that stone. Their heavy lid tips up on its long edge with a grind of stone and stays over the coffin, so it never passes through what stands beside it, and they sound and break like those bricks, dropping only when mined with a pickaxe. Both styles lie flat or stand (COFFIN-10) and follow every coffin rule (COFFIN-1 to COFFIN-15) |
| DECOR-6 | Candles, candelabras and chandeliers light the room. Flint and steel or a fire charge lights them and an empty hand puts them out, and every light, douse, opening and closing plays its own animation |
| DECOR-7 | A gramophone, crafted from a jukebox, gold ingots and ebony planks and facing the player who placed it, plays any music disc as a jukebox does, with the same hopper, redstone and comparator behaviour, and animates while it plays. Its music is loudest beside it and fades with distance until it is silent. A disc shows on the platter the moment it is put in and is gone the moment it is taken out, so it is always plain whether a gramophone holds one |
| DECOR-8 | Gothic music discs, one per track, play in a gramophone or a jukebox. They are found only in vampire structures: in their loot and in their gramophones (HOME-20) |
| WEAR-1 | Gothic clothing from several centuries, with at least two pieces for each armour slot, enough to dress at least 10 distinct looks. Each piece has a male and a female cut, and is drawn in the cut that fits its wearer: the female cut on a slim-armed player model, the male cut on a wide one. Female chest pieces follow the bust at its size: the corset cups it, the frock coat opens over a cravat and the doublet's stomacher runs down over it. A vanilla chestplate flattens it |
| WEAR-2 | Clothing is worn in the armour slots in place of armour, protects and wears out like leather, and is mended with wool, or with leather for shoes and boots. It is drawn as an outer layer over the body, so a coat hangs over any armour worn in the other slots, and coat tails sway as the wearer walks |
| WEAR-3 | Clothing takes the vanilla armour enchantments for its slot and eight of the mod's own, which come from the enchanting table, books on an anvil, librarians, loot and the gear mobs spawn with, like any vanilla enchantment that is not a treasure: Nocturne on headwear sees in the dark at night, Batwing on chest pieces falls further before taking damage, Gliding Step on legwear steps up a full block, Hush on shoes and boots sneaks faster, Shroud on headwear is noticed by hunters and villagers from less far, Mistcloak on chest pieces carries Vanish further, Warding on legwear dulls garlic and crucifixes a little, and Thrift on chest pieces slows blood drain. A cape or cloak takes the mod's own chest enchantments and a face piece its own headwear ones, and the same enchantment on two worn pieces counts once, at the higher level |
| WEAR-4 | Clothing whose colour is part of the look is dyed like leather, in a crafting grid with dyes, and a water cauldron washes the dye out. Only the cloth takes the dye: trims, buttons and buckles keep their own colours |
| WEAR-5 | Every outfit a vampire mob wears is made of clothing a player can craft |
| WEAR-6 | Three curses are found only on loot, on clothing, weapons and accessories: Thirst drains blood faster, Sunmarked takes more damage from the sun, and Beacon lets hunters find the wearer from further off |

**Enforcement:** server.

### 5.20 Wear Accessories

**Actor:** Any player.

| ID | Requirement |
|---|---|
| WORN-1 | A player has eight accessory slots beside their armour: two rings, a necklace, a back, gloves, a belt, a face and a brooch |
| WORN-2 | Capes and cloaks are worn on the back, so they hang over whatever is worn on the chest and sway as the wearer walks. Veils, masks and spectacles are worn on the face. Every accessory but a ring is drawn on its wearer |
| WORN-3 | The mod has about 80 accessories. The plain ones are for looks and are crafted. The rest each carry one gift and are found, never crafted: in the loot of vampire structures and hunter camps, from the Eldest and the Grand Inquisitor (RITE-12, INQ-15), and as the reward of some trials and mastery feats (CHAL-1, CHAL-2) |
| WORN-4 | A gift is small and particular to this mod: a ring that slows blood drain, a pendant that holds one bottle of blood for a sip in need, a crucifix on a chain that wards at short range with no hand, a bandolier that carries bolts and flasks, and a few for fun, such as smoked spectacles that turn daylight to dusk for the wearer. Some work only for a vampire, some only for a human, and each boss drops rings of its own |
| WORN-5 | Gifts stay fair. An accessory carries one gift, two of the same accessory give it once, and no gift removes a weakness (WEAK-1 to WEAK-10) or lets a vampire walk in the sun |
| WORN-6 | Accessories, their slots, gifts, numbers and where each is found are data, and `docs/accessories.md` lists them all |

**Enforcement:** server.

### 5.21 See Blood

**Actor:** Any player.

| ID | Requirement |
|---|---|
| GORE-1 | Bites, stakes and silver hits spray blood. Blood leaves splatter on nearby blocks, drawn by the client, that fades with time. It lands only on the full, flat top or side of a block it touches, never in or under liquid and never past the edge of its face, and it goes as soon as its block is broken, moved or flooded |
| GORE-2 | A drained victim and a staked vampire have their own death effects. A staked vampire also gives off the dark, ashy smoke of a burning one (SUN-3) |
| GORE-3 | A client option sets gore, including vomit particles (FEED-3), blood spray and its splatter (GORE-1), and placed blood splatter (DECOR-3), to Full, Reduced or Off. Full is the default. Reduced draws fewer drops, and blood spray and splatter dried and dark. Vomit particles and blood spray also follow the vanilla Particles setting: fewer on Decreased, and none on Minimal. The form views option (CFG-7) sits beside it |
| GORE-4 | Gore is drawn by the client only. The setting never changes what happens in the world, including which parts a body has lost (GORE-5) |
| GORE-5 | A blow can cut a part off a vampire mob, a hunter mob or a player, when dismemberment is on. A player can be maimed only while "players can be maimed" is on, and a player's blow on another player also needs PvP. Which mobs can be maimed is data. The Eldest and the Grand Inquisitor keep their bodies |
| GORE-6 | A blow can sever when it is a direct melee blow with a severing weapon (swords, axes, the silver sword and axe and their ashen forms, the Blade of the Eldest and the Inquisitor's Greatsword, as data), a power or boss attack marked as severing in data, or any blow that takes at least the massive blow share of the victim's max health (default 50%). It must also take at least the strong blow share (default 15%) after armour, and leave the victim alive and open: staggered, or below the badly hurt share of health (default 35%). Then a roll under the chance to sever (default 15%) cuts. Stakes, bolts, bites, a player's bare fist, fire, falls and the sun never sever, whatever the damage, and neither does a blocked blow. Which damage and which shots never sever is data |
| GORE-7 | A player's melee blow takes the part their line of sight meets, by height and side of the victim's body: the top quarter is the head, below the hip a leg, and outside the trunk between them an arm. A shot takes the part at its point of impact. A mob's melee blow picks a part by weights in data, limbs far more often than the head. A blow with no direction, such as a blast, picks a limb at random. A blow on the trunk, on the head or on a part already gone severs nothing. Zones are data |
| GORE-8 | A body that lost a part in the last 10 seconds loses nothing more, so two blows in one tick take one part at most |
| GORE-9 | Everyone near hears a heavy wet blow when a part comes off, and every client stops drawing that part on the body, with its armour and anything held in that hand. An operator can cut a named limb off any target and give every part back with `/nightborn body` |

**Enforcement:** server decides what happens. The client decides how much blood to draw.

### 5.22 Pick A Side

**Actor:** Vampire, human.

| ID | Requirement |
|---|---|
| FACT-1 | Every player is a vampire or a human. A human who joins the guild is a hunter and earns hunter skills (HUNT-10). Vampires earn bloodline skills |
| FACT-2 | Combat between players follows the server's PvP setting. With PvP on, vampires and humans can fight and bite. A duel for a house is fought whatever the setting (HOUSE-8) |
| FACT-3 | A player's faction is shown in the player list only if the server turns it on. It is off by default |
| FACT-4 | Otherwise, vampires, humans and sick players are told apart only by what other players can see: eyes, being weakened, effects, a flinch (FACT-5) and a mirror (FACT-6). No name, mark, list, screen or chat message gives a vampire player away to a human (NAME-3, RANK-4): a vampire's advancements from the mod's tab are announced in chat to vampires only. What they do, and those who know them, still can: a hunter's Tracking and a Seeker's Grave Listener, True Sight and Revelation (HUNT-4, `docs/skills.md`), a hunter mob that sights them (GUILD-2) and a village that knows their face (SUSP-3) |
| FACT-5 | A vampire, player or mob, flinches for a moment when it first comes to the edge of the reach of garlic or a crucifix (WEAK-1 to WEAK-4), before it is weakened enough to stoop (WEAK-8). Garlic and a crucifix each draw a recoil of their own, and the crucifix wins when both reach it at once. A recoil never cuts into feeding, a swing, a power warning or being downed. A vampire under Glamour, a Noble power, does not flinch and shows in a mirror until the power ends (`docs/skills.md`) |
| FACT-6 | A hand mirror is crafted from glass and a gold ingot. Used on a player or a mob nearby, it opens a small framed glass for its user that shows the one it was raised at, or an empty frame if they are a vampire. The one it was raised at sees and hears it raised. Used while sneaking with nobody in sight, it shows its user |
| FACT-7 | Choosing a side is a moment: turning into a vampire (TURN-5) and being sworn into the guild (HUNT-10). A short ceremony plays where the player stands. For a turning, their heartbeat stops, the light about them sinks to the colours of night and a bell tolls. For an oath, the Guild Master raises a crucifix, the hunters present salute and a horn sounds. The player sees it as a cutscene, in third person, the view circling them for a few seconds, and it ends on a card that names what they have become. They cannot act or be hurt while it plays, it never flashes, and a client option skips the camera. Players near see and hear the ceremony, but a human sees no more of a turning than TURN-5 shows. Each gives its journal (GUIDE-2) and an advancement (ADV-3, ADV-48) |

**Enforcement:** server.

### 5.23 Configure The Mod

**Actor:** Server operator, pack author.

| ID | Requirement |
|---|---|
| CFG-1 | Every server option can be set with `/nightborn`, by an operator of level 2 or above |
| CFG-2 | Settings are saved per world as JSON in the world save directory |
| CFG-3 | A missing file gives defaults. A broken file gives defaults, is logged and is kept |
| CFG-4 | Out-of-range values are clamped on load |
| CFG-5 | Options cover every area of the mod, as the table below lists |
| CFG-6 | A settings screen through Mod Menu shows the same server options and shares their validation with the commands |
| CFG-7 | An option that only changes what one player sees, such as gore (GORE-3) or form views, is a client setting saved on that player's device. Every player can change it on the settings screen, whatever their operator level, and it takes effect without a restart. Form views set how the world looks in Bat Form (BAT-8), Night Terror and Mist Form to Full, Reduced or Off. Full is the default. Reduced keeps each view but removes the tremble and softens the dark and the tint, and Off draws the world as normal. The view is never the only sign of a form (ACC-2) |
| CFG-8 | Operators can turn, cure, swear in, set the points, stage and rank and reset the bloodlines and paths of any player, read and set any vampire's notoriety (INQ-1), disband any house, release any human familiar, and cut parts off or restore any body (GORE-9) |

Every number this document calls a default, a setting or tunable is an option here.

| Area | Options |
|---|---|
| Turning and sickness | The turn chance of a turning bite and of a bat's bite, the blood a turning bite costs, sickness length, how fast old blood refills, and whether players can turn players |
| Sun and weaknesses | Sun damage, how long a parasol lasts, garlic and crucifix ranges, the damage, weakened time and blood lost from eating garlic, stake and silver damage, the damage, hits, time and weakening of each weapon coating, and the strength, time, radius and cloud time of a garlic bomb |
| Blood and feeding | Blood drain and blood values, the drain while levitating and in bat form, flying and at a sprint, how fast starving weakens and hurts and how much it takes from night gifts and bare hands, the damage from bringing up food or potions, how much a lancet hurts, feeding range, and the health below which a finisher bite opens |
| Being a vampire | The downed and escape times, hardcore vampire deaths, and the nights, blood and gifts of each stage |
| Dismemberment | Whether blows can cut parts off, whether players can be maimed, the chance to sever, and the strong blow, massive blow and badly hurt shares of health |
| Skills | How much experience each act pays, how fast a point's cost rises, the staking penalty, hunter experience per contract, the hunter points each guild rank needs, and the health and damage against vampires each rank gives |
| Coffins and the day | The coffin sleep share, how soon after dawn a whole day's sleep must start, and what being rested gives |
| Minds and servants | The chance a sip binds a thrall, how long a charm lasts, a human familiar's starting loyalty, wages, loyalty gains and losses, betrayal threshold and chance, and guard and errand ranges, and how far servants gather, scout, look about them on arrival and keep watch, which is also how far a household's guards see |
| Households | How many members each stage allows, how long a lord has to answer a challenge, how long before it can be repeated, how close the two must stand for the duel to begin, and the radius of its circle |
| Vampire mobs | Spawn rates, hunger threshold and blood drain, how often hungry ones go out to feed, how far they roam about home, how far a hurt one calls its housemates, their chase range, how often they hiss, how often they rise to levitate and land, how far and how hard they leap, dash and dodge and when, how long and how far they fall back and when they come again, and the rewards for older ones |
| The world | Vampire structure and hunter camp rates, how long a cleared structure stays empty, how often wild vampires appear, how far from every player and how often they appear hungry, how often structure gramophones play and how near a player must be, how far a Guild Master's rumours reach |
| First nights | How many nights a player counts as new, how often the watcher comes on one of those nights, how far it keeps from the player and how near the player comes before it leaves, how often a player near a vampire structure comes on its signs, and how far round a structure those signs lie and villagers mutter about it |
| Hunters and the guild | How often hunter parties appear, how many hunters they hold, how many nights they camp, how long a party on the move or a wild vampire goes with no player near before it leaves, how often vampire bats attack a camp, how much each stage of quarry adds to a party sent against it, how close a party on the move must come to a vampire's house to raid it, how far a raiding party follows a vampire with no house, how much noise wakes a sleeping house and how fast it fades, how often a household keeps a member awake by day, and how far a contract's map may lie from the house, and whether a Guild Master's rite needs the oath, the rank and the emeralds it asks, how long a hunter waits between rites and how long the shelter after one lasts |
| Villages and nemeses | How fast suspicion fades, the suspicion at which each step begins, how much a charm and each gift or trade take off and how many gifts count a night, how long a witness must live before the village hears, how much dearer a wary village is after dark, how far a village reaches from its bell, the fewest and most nights between a nemesis's visits, and how many nemeses a player may have at once |
| The Eldest | Its reward, how long the ritual builds and how far it reaches, how much health each extra player gives it, the size of its arenas, how long an empty arena takes to reset the fight, how long hunters have to stake it and how much health it rises with, how much life its blade and its full set steal and how much health the set adds |
| The Grand Inquisitor | His reward, the notoriety at which the guild sends a party and at which he hunts, how fast notoriety fades, the quiet nights after a visit, how many nights he warns, how late in the night he may come, how long the bell's build-up lasts, how much health each extra vampire gives him, the size of his arena, how long vampires have to drain him, how long the drain takes and how much health he rises with, how much notoriety a vampire who dies in his fight loses, how long he waits in an empty arena, how much blood his greatsword and his full set give back, and how much blood the set adds and how much it slows the drain |
| Clothing | Whether Nocturne works, and how strong Batwing, Gliding Step, Hush, Shroud, Mistcloak, Warding and Thrift are |
| Challenges | How many trials are on offer and how often they change |
| Players | Whether the player list shows factions (FACT-3), and how long the camera and the card of a ceremony last and how far away it is seen and heard (FACT-7) |

**Enforcement:** server.

### 5.24 Work Through The Advancements

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
| ADV-28 | Something In The Blood | Drinking old blood |
| ADV-29 | Coming Of Age | Reaching adult |
| ADV-30 | Elder Statesman | Reaching elder |
| ADV-31 | Older Than Dirt | Reaching ancient |
| ADV-32 | House Rules | Founding a house |
| ADV-33 | Full House | Leading a house of six |
| ADV-34 | Regime Change | Winning a challenge for a house |
| ADV-35 | Last Bite | Ending an enemy with a finisher bite |
| ADV-36 | Made In The Shade | Walking in direct sun under a parasol |
| ADV-37 | Nothing To See Here | Feeding unseen in a warded village |
| ADV-38 | Nothing Personal | Ending a nemesis |
| ADV-39 | Rank And File | Reaching the rank of Hunter |
| ADV-40 | Master Of The Hunt | Reaching the rank of Master |
| ADV-41 | Rude Awakening | Staking a vampire asleep in its coffin |
| ADV-42 | Smoke And Mirrors | Raising a hand mirror at a vampire |
| ADV-43 | Dressed To The Nines | Wearing an accessory in every slot |
| ADV-44 | Last Of The Line | Ending the Eldest |
| ADV-45 | Inquire Within | Draining the Grand Inquisitor |
| ADV-46 | Tried And Tested | Completing a trial |
| ADV-48 | Sworn In | Joining the guild |
| ADV-49 | Blood Type | Tasting every kind of prey that gives a blood gift (FEED-15) |
| ADV-50 | Property Ladder | Entering every kind of vampire structure: a crypt, a ruined manor, an underground lair, a castle and a mountain castle |
| ADV-51 | Ring Leader | Owning all three rings of one boss at once |
| ADV-52 | Stage Fright | Staking a fledgling, an adult and an elder |
| ADV-53 | Thicker Than Water | Learning every skill of one bloodline |
| ADV-54 | By The Book | Learning every skill of one hunter path |
| ADV-55 | Cover To Cover | Filling every page of a journal that is not behind a set challenge or a hidden advancement |
| ADV-56 | Staff Meeting | Keeping a thrall, a ghostly familiar and a human familiar at the same time |
| ADV-57 | The Full Treatment | Hitting one vampire with garlic, holy water, silver and a stake before it falls |
| ADV-58 | Well Tried | Completing 15 different trials of one side |
| ADV-59 | Mirror, Mirror | Raising a hand mirror at yourself as a vampire |
| ADV-60 | Light Lunch | Bringing up food as a vampire |
| ADV-61 | Hang In There | Hanging upside down as a bat from dusk to dawn |
| ADV-62 | Careful What You Wish For | Calling a boss as the wrong side: starting the ritual as a vampire, or ringing the hunter's bell as a human |
| ADV-64 | Garlic Bread | Eating bread as a human with garlic in the other hand |
| ADV-65 | Cross Purposes | Holding a crucifix in each hand as a human |

| ID | Requirement |
|---|---|
| ADV-21 | The tab is a tree, not a line. Love Bites, the hunter advancements (ADV-13 to ADV-16), House Call and Dressed To Kill each branch from the root and can be earned in any order |
| ADV-22 | Close Call and No Reflection On You branch from Love Bites. The other vampire advancements (ADV-4 to ADV-12) branch from No Reflection On You, in any order. Lord Of The Manner branches from House Call |
| ADV-23 | Bad Taste and Good Help Is Hard To Find stay hidden until earned |
| ADV-24 | Lord Of The Manner and Strings Attached are challenges. No Reflection On You, Old Blood and Close Call are goals. Goals give 50 experience and challenges give 100 |
| ADV-25 | An advancement earned in steps shows its progress: Strings Attached counts the strings weakening you at once, and Old Blood counts bloodlines chosen |
| ADV-26 | Icons use the mod's own items where one fits. A vampire lord counts for Lord Of The Manner by being in the `nightborn:vampire_lords` entity tag |
| ADV-27 | Names may be tuned, but each stays short, clever and in plain English |
| ADV-47 | The vampire advancements ADV-28 to ADV-37 and ADV-45 branch from No Reflection On You, except Something In The Blood, which branches from House Call. Sworn In branches from Stake Holder and is a goal, and the hunter advancements ADV-39 to ADV-41 and ADV-44 branch from it. Nothing Personal, Smoke And Mirrors, Dressed To The Nines and Tried And Tested branch from the root. Older Than Dirt, Full House, Master Of The Hunt, Last Of The Line and Inquire Within are challenges, and Nothing To See Here and Smoke And Mirrors stay hidden until earned |
| ADV-63 | ADV-49 to ADV-58 are set challenges, earned by doing every one of a set, as vanilla's are. Each is a challenge, pays as one (ADV-24), shows its progress as a count and lists what is still missing. A set with no fixed list counts in quarters: Thicker Than Water and By The Book count quarters of one line learned, and Cover To Cover quarters of one journal's pages, counting only the pages behind an advancement that is neither hidden nor a set challenge (a challenge earned in more than one step). Ring Leader counts the most rings of one boss owned at once, and Staff Meeting, The Full Treatment and Well Tried count what is held or done. Blood Type, Thicker Than Water and Staff Meeting branch from No Reflection On You, Stage Fright, By The Book and The Full Treatment from Sworn In, and Property Ladder, Ring Leader, Cover To Cover and Well Tried from the root. ADV-59 to ADV-62, ADV-64 and ADV-65 stay hidden until earned and branch from the root. No advancement asks for a deed that a mastery feat already asks for (CHAL-2) |

**Enforcement:** server.

### 5.25 Take On Trials And Feats

**Actor:** Any player.

| ID | Requirement |
|---|---|
| CHAL-1 | Each player has three trials on offer at a time, drawn from their side's list and changed every 7 in-game days by default. A vampire reads theirs in their journal (GUIDE-3) and a hunter takes theirs from a Guild Master (GUILD-3). A trial is a feat with a condition, such as feeding on three villagers in one night unseen or clearing a house by day without waking it, and pays skill experience once, and some pay an accessory as well (WORN-3). Trials are data, and `docs/challenges.md` lists the 20 each side draws from |
| CHAL-2 | A mastery feat is a hard deed done once, such as ending a boss in no armour, reaching ancient without ever being staked or clearing a mountain castle alone. Each gives a title the player may show after their name (NAME-12) and a trophy block of its own, and some an accessory as well (WORN-3). Feats are data, and `docs/challenges.md` lists all 12 |
| CHAL-3 | The first player on a server to reach ancient, to reach Master, to end the Eldest and to drain the Grand Inquisitor is recorded, and the deed is announced to everyone. A vampire's is announced without their name unless the server shows factions (FACT-3) |

**Enforcement:** server.

### 5.26 Summon The Eldest

**Actor:** Human, hunter, vampire.

The Eldest is among the oldest vampires there are, with every power of every bloodline. It appears when the ritual calls it, and once at the top of every mountain castle (HOME-23). A human who has raided a castle, a crypt, a ruined manor and an underground lair builds a ritual altar from one relic of each. A chalice of blood set on it at night calls the Eldest, as often as they can fill it.

| ID | Requirement |
|---|---|
| RITE-1 | Each vampire structure type but the mountain castle has a relic of its own: a castle's signet, a crypt's reliquary, a ruined manor's locket and an underground lair's fang. It has a chance to be found in that structure's loot or dropped by the elders, ancients and lord who live there |
| RITE-2 | A ritual altar is crafted from one of each relic (RITE-1), which the recipe uses up, so one set of relics makes an altar that lasts. Each ritual costs blood. A gold chalice (DECOR-3) used on the altar is set on it, where it is filled and emptied as anywhere else, and sneaking with an empty hand takes it back. Using the altar empty-handed at night while its chalice holds all 7 bottles, of any one quality, starts the ritual, and at any other time a vampire who uses it drinks from the chalice (DECOR-3). The ritual starts with a build-up everyone near can see and hear: fog closes in, a bell tolls lower and lower, smoke gathers on the altar and the candles around it gutter one by one. How long the build-up lasts and how far it reaches are server settings (CFG-5) |
| RITE-3 | The ritual summons the Eldest beside the altar: a vampire, always a man, crowned and robed, with every skill in the vampire tree, all three bloodlines at once. It does not fight, move or live as other vampires do (HOME-15 to HOME-17): it fights only as RITE-9 to RITE-16 say. It is a boss for MIND-4. Its health grows with the number of players near when it appears, by a share for each player after the first that is a server setting (CFG-5) |
| RITE-4 | Only a stake or the sun ends the Eldest (CORE-7, CORE-8). It never escapes from its arena (RITE-9). When its last phase (RITE-10) is beaten it is downed, and its bar says so. Humans then have a short time to stake it. If nobody does, it rises again with a share of its last phase's health. The time and the share are server settings (CFG-5) |
| RITE-5 | The ritual drinks the chalice dry when the Eldest appears and leaves the chalice on the altar. Breaking the altar during the build-up stops the ritual and drops the altar and its chalice, and the blood spills as from any broken chalice (DECOR-3). The altar can be used again each time its chalice is filled, so the fight can be repeated. An altar from an older world, crafted without relics, works the same way, and drops any relics left on it as items when its chunk first loads |
| RITE-6 | Ending the Eldest gives a large number of hunter points to every hunter who took part in the fight by striking it at least once, and loot found nowhere else (RITE-12). A summoned Eldest and a mountain castle's Eldest give the same points and roll one shared loot table |
| RITE-7 | The Eldest attacks humans on sight, hungry or not, and ignores vampire players unless they strike it (HOME-11). Vampire players can fight on either side as the PvP setting allows (FACT-2). A vampire can start the ritual too, and the Eldest still comes, but it turns on the vampire who called it: it says a line of outrage, shown in chat under its name to everyone near, and treats that vampire as it treats a human. Its lines are data and are translated (ACC-5) |
| RITE-8 | The Eldest is not the castle's vampire lord (HOME-7). It appears only through the ritual and in mountain castles (HOME-23) |
| RITE-9 | The Eldest fights in an arena. The ritual seals a circle round the altar with a wall of dark red fog that is never still: it ripples, beats like a heart and runs slowly through blood red, violet and near black, and it stirs harder while a fight is on. It changes smoothly and never flashes. A mountain castle's Eldest has the room it keeps to. The Eldest never leaves its arena, targets nobody outside it and cannot be hurt from outside it, nor by fire, lava, falls and the like, which a damage type tag lists. Its boss bar and its music (RITE-13) reach only players inside. The wall lets anyone walk in and lets nobody out: a player inside who crosses it, on foot, mounted, thrown or by any other means, is put back where they last stood inside. A player leaves only by dying or when the Eldest is ended. The wall holds only those the Eldest attacks on sight (RITE-7), so not vampires it ignores or players in creative mode, and it holds nobody while the Eldest sleeps. When no living player has been inside for a short time, however far away they went, the Eldest returns to its first phase at full health and forgets who fought it. The wall falls when the Eldest is ended. The size of each arena and the empty time are server settings (CFG-5) |
| RITE-10 | The fight has three phases, each with its own share of the Eldest's health, and the bar names the phase and shows what is left of it. No blow carries past the end of a phase, not even a stake. Only the sun does. At the end of a phase the Eldest steps back to where the fight began, roars under a bolt of lightning, throws back everyone close and cannot be hurt while it changes. It comes out of each change stronger: better armoured, harder hitting, faster and visibly larger. The Court is a duel on foot: chains of blows a shield stops, a lunge along a marked line, two lesser vampires called to its side for a while, and a gaze that roots one human to the spot for a few seconds unless they break its line of sight first. The Beast takes to the air and circles the arena out of a blade's reach, so humans need bows and bolts, or something that weakens a vampire, which keeps it on the ground. It dives onto a marked spot, sends a swarm down a marked line, screeches a mark under every human, and after each dive stays on the ground for a moment, open to a blade, before it rises again. The Blood is the Eldest enraged: larger again, its eyes blazing, growling. It steps through shadow to strike a marked spot, leaves pools of blood that hurt to stand in, uses the gaze and the dive, and drains a human to heal itself until enough blows break the drain. In the Beast and the Blood it can also call a ward of vampire bats, each outlined so it can be found: while one lives the Eldest takes no harm. Keepers and called vampires go when their time is up, when the fight ends and when the world is loaded again. Each phase also has vampire powers among its attacks, announced like any other: the Court vanishes and quickens itself, the Beast quickens itself and looses bats, and the Blood vanishes, raises grave fog and summons a familiar. Phases, attacks and their numbers are data |
| RITE-11 | Every attack and every state of RITE-10 can be read at once. Each attack has a pose and a sound before it lands, and one that hits the ground has a mark on the floor that shows exactly where. The mark is drawn as a model, not as particles, so it shows on any particle setting (ACC-1). An attack hurts only those still in its mark, in its reach or in its line of sight when it lands. The bar turns blue and says the Eldest is warded while its keepers live, and green and says it is feeding while it drains. A guarded blow rings like a shield. Each phase also looks different at a glance. The Court stands and walks like a king with empty hands and its wings folded on its back. The Beast hunches, opens its wings as it changes, spreads them wide for that phase only and beats them when it flies. The Blood folds them away again as it changes, stands tall with eyes and veins that blaze, and draws the Blade of the Eldest, the same blade and the same crown, mantle, greaves and boots it can drop (RITE-12). It rises from the floor when the ritual summons it, roars and changes shape between phases, and has its own pose for a guard, a break away, taking off, landing and the moment its ward falls. Which clip, form and weapon each phase uses is data |
| RITE-12 | The Eldest's loot is one loot table. Every kill gives treasure and the ash of the Eldest, which turns a silver sword, axe or stake into ashen silver at a smithing table: tougher and sharper, and still silver to a vampire. Very rarely it drops the Blade of the Eldest, which gives back a share of each blow as health, or one of four pieces of its armour, as strong as netherite. Wearing all four with the blade in hand steals more life and adds health. Rarely it drops one of two armour trim templates or one of its rings (WORN-4). Now and then it drops its skull, a trophy block, or the music disc of its last phase. Once in a great while it drops a tame vampire bat, which circles its owner, cannot be hurt and returns to their pack when they use it while sneaking. The shares and the health are server settings (CFG-5) and the chances are loot table data |
| RITE-13 | The fight has its own music: one piece for the first two phases and another for the last. It plays only for players inside the arena while a fight is on, in place of any other music, and stops when they leave or the fight ends. While a fight is on, players inside also hear something now and then from a list each phase carries: a tolling bell, an organ, a scream, laughter, or a bolt of harmless lightning on the wall, whose thunder carries beyond it. These come well apart, so they unsettle without wearing thin, and how often is data |
| RITE-14 | Every rule of RITE-9 to RITE-13, RITE-15 and RITE-16 holds for both the summoned Eldest and a mountain castle's Eldest |
| RITE-15 | The Eldest defends itself. No single blow takes more than a set amount of its health, and that amount falls each phase. After a few blows in quick succession it guards the next one, takes nothing and throws the attacker back if they stand close. When it has taken enough damage in a short time it breaks away to open ground inside the arena. After a guard or a break away it answers at once with one of that phase's counter attacks that can reach its target. A blow from a human it is not facing may turn it on that human. It will not ride in a boat or a minecart. Every number is data |
| RITE-16 | The Eldest moves as a boss, not as a vampire. Each phase gives it a stance: a distance it keeps from its target. It closes on a target that is too far, backs off one that is too near and circles one in between, changing direction as it goes, and it stands still while an attack is announced or lands. The Court keeps to a blade's length, the Beast on the ground runs its target down, and the Blood keeps a few paces off, so humans must come through its attacks to reach it. It never flees, retreats to heal, wanders, feeds or bites outside its attacks. Stances are data |

**Not supported:** the Eldest appearing any other way.

**Enforcement:** server.

### 5.27 Face The Grand Inquisitor

**Actor:** Vampire, human.

The Grand Inquisitor is the head of the hunters' guild: an old villager, a zealot, and the vampires' match for the Eldest. He comes for a vampire who has killed too many of his hunters, and a vampire who wants the fight can ring for him.

| ID | Requirement |
|---|---|
| INQ-1 | Every vampire player has a notoriety, which is never shown. It rises when they, their thrall, their familiar or an NPC member of their house kill a hunter mob or a Guild Master, more for the last of a hunter camp or a hunter party, and more the older the vampire is (AGE-1). Killing a player never raises it. It fades by a set amount at each dawn the vampire is online for. Past a first threshold the guild sends a raiding party against them (GUILD-13), and past a second it hunts them and the Grand Inquisitor comes. Each is called off if notoriety fades back below. After any raid, hunt or fight with him ends, however it ends, the vampire has quiet nights, 10 by default, in which the guild sends nothing new and notoriety cannot pass a threshold it is under. A vampire made human again (CFG-8) or reset by a hardcore death (CORE-10) loses their notoriety and any hunt. What each kill adds is data, and the thresholds, the fade and the quiet nights are server settings (CFG-5) |
| INQ-2 | A hunted vampire is warned for a few nights before he comes, 3 by default. Each dusk of the warning a bell tolls far off, nearer each night, and a line on screen tells the vampire the guild is coming, so the warning never rests on sound alone (ACC-2). A dusk the vampire is offline for does not count. How many nights is a server setting (CFG-5) |
| INQ-3 | At nightfall after the last warning he arrives alone on the surface of the overworld, a short way from the vampire, and walks them down. When he comes close the arena seals round where his quarry stands (INQ-7). He comes only for a vampire who is on the surface with open sky above them, so never underground and never under a roof, and only early in the night, before midnight by default, so there is a night to fight in (INQ-8). On a night the vampire gives him no such chance, or is offline, he waits and tries again the next nightfall while the hunt lasts. How late he may come is a server setting (CFG-5) |
| INQ-4 | A vampire can also ring for him. A Guild Master a player kills drops a Guild Master's seal. A hunter's bell is crafted once from a vanilla bell and four seals, and is never used up. Ringing it by hand early in the night (INQ-3), on the surface under open sky, uses up one more seal and starts a build-up everyone near can see and hear: bells answer from far off, nearer and nearer, and then he arrives for whoever rang as INQ-3 says, with no nights of warning. Without a seal, or rung at any other time or place, the bell only rings and nothing is used up. If the ringer leaves the surface or the server before he arrives, he does not come and the seal is spent. A hunted vampire who rings brings the man of their hunt at once, with no more nights of warning. How long the build-up lasts, ten seconds by default, is a server setting (CFG-5) |
| INQ-5 | The Grand Inquisitor is a boss for MIND-4: an old man, a villager as hunters are (GUILD-1), dressed and armed as his phase says (INQ-11) and speaking with a villager's voice (GUILD-2). Each hunt and each ring of the bell brings a new man with a new name (NAME-1, INQ-14). He cannot be bitten or bled with a lancet (GEAR-10) until he is downed (INQ-13). His health grows with the number of vampire players near when he appears, by a share for each after the first that is a server setting (CFG-5) |
| INQ-6 | He attacks every vampire player in his arena on sight, and ignores humans unless they strike him. Humans can fight on either side as the PvP setting allows (FACT-2). A human can ring the bell too, and he still comes, but he turns on the human who rang: he says a line of outrage, shown in chat under his name to everyone near, and treats that human as he treats a vampire. Hunters he calls against that human fight them too, GUILD-2 notwithstanding. He and his hunters attack a vampire mob inside the arena as any hunter does (GUILD-2), but the wall holds no mob and a mob does not keep the fight going. His lines are data and are translated (ACC-5) |
| INQ-7 | He fights in an arena. A circle seals where he catches his quarry, with a wall of pale holy light: white and pale gold, steady as a candle flame, rising and falling slowly as if it breathed and brighter while a fight is on. It changes smoothly and never flashes. The rules RITE-9 gives the Eldest's arena hold for his, with vampires in place of humans, except its rule for an empty arena, which INQ-14 replaces: he never leaves it while a fight is on, targets nobody outside it and cannot be hurt from outside it or by the damage types the tag lists, his boss bar and music (INQ-16) reach only players inside, and the wall lets anyone in and holds every player he attacks on sight (INQ-6) but those in creative mode. The wall gives no shade. It falls when he is ended or leaves (INQ-14). The size of the arena is a server setting (CFG-5) |
| INQ-8 | Dawn is the clock. At sunrise the sun reaches the arena as it reaches anywhere else and burns the vampires inside (SUN-1), and the wall still holds them until the fight ends (INQ-14) |
| INQ-9 | The fight has three phases, each with its own share of his health, and the bar names the phase and shows what is left of it. No blow carries past the end of a phase. At the end of a phase he steps back to where the fight began, raises his crucifix to a peal of bells and a burst of light that throws back everyone close, and cannot be hurt while he changes. He comes out of each change stronger: better armoured, harder hitting and faster. The Hunt is fought at a distance: volleys of stake bolts down marked lines, snares on marked spots that root a vampire for a few seconds unless they step off in time, and a garlic bomb onto a marked spot (GEAR-5). The Faith stands its ground: he holds up a great crucifix that weakens every vampire who can see it until they break its line of sight (WEAK-3), throws holy water onto marked spots, consecrates marked ground that goes on hurting and weakening those who stand on it, and kneels to pray, healing himself until enough blows break the prayer. The Pyre is the Inquisitor in wrath, with a silver greatsword: chains of blows, fire swept down a marked line that leaves the ground burning for a while, setting alight whoever stands on it but placing no fire (SAFE-6), and the false dawn, shafts of holy light on marked spots and lines that are direct sun to a vampire standing in them (SUN-1) while the rest of the arena stays dark. Phases, attacks and their numbers are data |
| INQ-10 | Through the Hunt and the Faith he calls hunters to his side at regular intervals, a few at a time, up to a limit at once. They are hunter mobs with the kits of GUILD-2, they fight beside him inside the arena, and they can be fed on as any hunter can. They drop nothing, add nothing to notoriety (INQ-1), and go when the fight ends and when the world is loaded again. He calls none in the Pyre, and those still standing fight on. How many, how often and the limit are data |
| INQ-11 | Every attack and every state can be read at once, as RITE-11 says for the Eldest: a pose and a sound before each attack, a mark drawn as a model for each one that hits the ground, and harm only to those still in its mark, reach or line of sight when it lands. The bar turns green and says he is at prayer while he heals. Each phase also looks different at a glance. The Hunt wears a long coat and a wide hat and carries a crossbow. The Faith bares his head and carries the great crucifix and a flask. The Pyre casts the crucifix aside and draws the greatsword, the same sword, hat, coat, greaves and boots he can drop (INQ-15). He has his own pose for arriving, each change of phase, a guard, a break away, kneeling in prayer, being downed and rising again. Which clip and weapon each phase uses is data |
| INQ-12 | He defends himself as RITE-15 says and moves as RITE-16 says, with his own numbers and stances. The Hunt keeps well back from his target, the Faith holds a few paces off, and the Pyre closes to a blade's length. Stances are data |
| INQ-13 | He dies like no hunter. When his last phase is beaten he is downed: he falls to his knees and his bar says so. Vampires then have a short time to feed on him, and a bite held until he is drained ends him. It is a feeding bite whatever the vampire chose (TURN-8), his blood is of player quality, and he never resists. If nobody drains him he rises again with a share of his last phase's health, and strikes no blow until he is back on his feet. A human he turned on (INQ-6) ends him with any blow while he is downed, which gives the loot of INQ-15, pays the vampires who struck him as INQ-15 says and the human nothing, and changes nobody's notoriety. The time, how long the drain takes and the share are server settings (CFG-5) |
| INQ-14 | Draining him sets the notoriety of every vampire who struck him back to zero and ends the hunt, so the fight can be earned again. A vampire who dies in the fight, staked or burned by the sun, loses a share of their notoriety, half by default, and a staked one pays the usual penalty as well (SKILL-6). The fight also ends when every player he attacks on sight (INQ-6) is dead. He then leaves, the wall falls and the hunt he came on is over: a vampire whose notoriety is still past the second threshold (INQ-1) is hunted afresh, with new nights of warning and a new man (INQ-5). If instead the arena empties while such a player still lives, such as his quarry leaving the server, he leaves after a short time. On a hunt, the hunt stays on and the same man comes again the next nightfall, at full health in his first phase. After a ring of the bell he does not come back and the seal is spent. The share and the time are server settings (CFG-5) |
| INQ-15 | Ending him gives a large number of vampire skill points to every vampire player who struck him at least once, and loot found nowhere else, from one loot table. Every kill gives treasure and the Inquisitor's mail, which lines a piece of gothic clothing (WEAR-1) at a smithing table: as strong as diamond, and keeping its look, dye and enchantments, with a faint silver mail shimmer over it, worn or in the inventory, to show it is lined. Very rarely he drops the Inquisitor's Greatsword, which gives a vampire who wields it a share of each blow back as blood, or one of four pieces of his armour, the hat, coat, greaves and boots, as strong as netherite. A vampire wearing all four with the greatsword in hand gets more blood from each blow, holds more blood and loses it more slowly. Rarely he drops one of two armour trim templates or one of his rings (WORN-4). Now and then he drops his broken crucifix, a trophy block that wards no one, or the music disc of his last phase. Once in a great while he drops a tame hawk, which behaves as the Eldest's tame vampire bat does (RITE-12). The shares, the blood and the drain are server settings (CFG-5) and the chances are loot table data |
| INQ-16 | The fight has its own music: one piece for the Hunt and the Faith and another for the Pyre. It plays as RITE-13 says for the Eldest's, and players inside also hear something now and then from a list each phase carries: a tolling bell, a choir, a hunting horn, a hammer on a stake, or a prayer muttered close by. How often is data |

**Not supported:** a hunt for a vampire mob; a Grand Inquisitor in a hunter camp or a hunter party; turning him, binding him as a thrall or hiring him; seeing a notoriety as a number.

**Enforcement:** server.

### 5.28 Know Them By Name

**Actor:** Any player, owner.

Vampire mobs, hunters, Guild Masters, the Grand Inquisitor, familiars and thralls each have a name, and a vampire player's name shows their age and house to their own kind. Vampire bats and vanilla mobs do not.

| ID | Requirement |
|---|---|
| NAME-1 | Every vampire mob, hunter mob and Guild Master has a given name, drawn at random when it spawns. It keeps its name, and a vampire its epithet, until someone renames it (NAME-9). A man's comes from the men's pool for his kind and a woman's from the women's pool (HOME-18, GUILD-1, RITE-3). Vampires have 250 men's and 250 women's names and hunters have 250 of each, so 500 different vampire names and 500 different hunter names, each fitting its group and the mod's tone (LOOK-1 to LOOK-4). A Guild Master draws from the hunter pools, and a Grand Inquisitor from the hunter men's pool. No two members of one household, one camp or one hunter party share a given name while the pool allows it. A vampire, hunter or Guild Master from an older world that has no name is given one when it loads |
| NAME-2 | Every vampire mob but the Eldest has a name that ends in an epithet, drawn at random from a pool of 500 when it spawns, so it reads as a given name and a title. No two members of one household share an epithet while the pool allows it. The Eldest's epithet is always "the Eldest", which is not one of the 500. A Grand Inquisitor's name ends in his title, "the Grand Inquisitor", in the same way. A nemesis, vampire or hunter, may take an epithet from a pool of its own (NEM-3). Hunters other than a nemesis, Guild Masters, familiars and thralls have no epithet |
| NAME-3 | A name is drawn above its bearer as a vanilla name tag is, without the player having to look straight at it, whenever the player has a clear line of sight to the bearer within vanilla's range. It is never drawn through blocks, so a name does not give away a vampire behind a wall or in a closed coffin, and it is hidden when vanilla would hide it, such as while the bearer is invisible. A vampire mob's name takes the colour of its stage, by default grey for a fledgling, white for an adult, gold for an elder, red for an ancient, dark purple for a vampire lord and dark red for the Eldest (HOME-12). A hunter's and a Guild Master's name is blue, and the Grand Inquisitor's is dark blue. A vampire player's name takes the colour of their stage (AGE-1) only for other vampires, and for everyone where the server shows factions (FACT-3). A human sees it as any player's name. A familiar's and a thrall's name is the normal name colour. The colour comes from what the bearer is and is not stored in the name, so a renamed mob keeps it. A boss bar shows its boss's name. Colour is never the only sign of a stage: an older stage also looks older (HOME-18, ACC-1) |
| NAME-4 | A name is stored as the entity's vanilla custom name, epithet included, so entity selectors such as `@e[name=...]`, `/data`, other commands and other mods read it as they read any named mob. A selector matches the English name |
| NAME-5 | Chat lines that refer to a named mob use its name, in its colour, as vanilla does for any named mob. A player's death message names the vampire or hunter that killed them, and a familiar's or thrall's death is told to its owner as a pet's is |
| NAME-6 | Hiring a human familiar (FAM-3) or binding a thrall (MIND-5) opens a naming dialogue for its owner, styled as the vampire skill screen is (LINE-7) and never a plain menu. Its name field starts filled with a name drawn at random from the familiar pools, 250 men's and 250 women's names, all different, picked without regard to gender. The owner keeps it or types any other name and confirms. Neither is ever without a name: it holds the suggested one from the moment it is hired or bound, so closing the dialogue or leaving the server keeps it |
| NAME-7 | A ghostly familiar takes a name at random from the familiar pools each time it is summoned, with no dialogue, since it is summoned often and fades (FAM-2) |
| NAME-8 | A name a player enters, in the dialogue or on a name tag, is trimmed of spaces at both ends, has control and formatting characters removed, and must then be 1 to 32 characters long. The dialogue will not confirm a name that fails and says why. The server applies the same rules again, and takes a name from the dialogue only from the owner, for their own familiar or thrall (SAFE-4, PERM-1). A name that fails on the server changes nothing |
| NAME-9 | A vanilla name tag renames any vampire mob, hunter, Guild Master, familiar or thrall, used by any player as on any vanilla mob. A tag whose name fails NAME-8 does nothing and is not used up. The new name replaces the whole name, epithet included. A released familiar keeps its name as a villager (FAM-8), and a released thrall keeps its name too |
| NAME-10 | Given names are proper names and are the same in every language. Each epithet is translated into every shipped language |
| NAME-11 | The name of a house (HOUSE-6) follows the name of each of its members, players and NPC members alike. A player member's is drawn only for those who see their stage colour (NAME-3) |
| NAME-12 | A player who has earned titles (CHAL-2) chooses one to show after their name, or none. A title earned as a vampire is drawn only for those who see their stage colour (NAME-3) |

**Not supported:** a naming dialogue for a vampire mob, hunter or Guild Master; an epithet for anyone but a vampire mob, beyond the Grand Inquisitor's title and a nemesis's; a filter on the words a player may use in a name.

**Enforcement:** server. The client draws the name tags and the dialogue, and sends the name the owner chose.

### 5.29 Read The Journals

**Actor:** Any player.

| ID | Requirement |
|---|---|
| GUIDE-1 | The mod's guide is two journals, built on Patchouli, which the mod requires (COMPAT-1): the hunter's journal and the vampire's journal. Each has its own cover and pages in the mod's style (LINE-7) |
| GUIDE-2 | A journal is first given when a player chooses a side (FACT-7): the hunter's journal when a human is sworn into the guild, and the vampire's journal when a player turns. Neither is found or crafted. A player who carries none of their own is given another: a hunter from a Guild Master's command wheel (WHEEL-2), and a vampire by using an old blood font with an empty hand (TURN-9). A journal has no worth, so SAFE-1 does not count it |
| GUIDE-3 | A journal fills in as its reader discovers things: an entry stays hidden until the advancement behind it is earned. It also shows what changes: its owner's nemeses (NEM-7), a hunter's contracts in hand, and a vampire's trials on offer (CHAL-1) |
| GUIDE-4 | Every page is translated into every shipped language (ACC-5) |
| GUIDE-5 | Each journal is written by someone who lives in the world, and still teaches every rule. The vampire's journal is the ledger of an old vampire, handed to the newly turned: private, dry and weary. The hunter's journal is a guild field book added to by several hands: blunt, practical and superstitious, with notes in the margin and lines struck out. Neither speaks of players, settings, points, mobs, health, screens or blocks. A distance is given in paces, a key is shown in brackets as the reader has bound it, and hunter points are the guild's reckoning, which is also what a Guild Master calls them when asked for a rank (RANK-1) |
| GUIDE-6 | Most entries carry an ink and wash plate on the page after their first, drawn from the mod's own models in oxblood for the vampire's journal and sepia for the hunter's. Only the vampire's entry on feeding well has none |

**Enforcement:** the client draws the journals. The server decides what is unlocked.

### 5.30 Give Orders With The Wheel

**Actor:** Owner, lord, human.

| ID | Requirement |
|---|---|
| WHEEL-1 | Using someone a player leads or deals with opens the command wheel: a ring of that person's actions, each an icon with a label. The player points at one and lets go to choose it, or clicks it. An item in the hand that has a use of its own on them, such as a lancet, a name tag, emeralds for wages or a chalice for an invitation, does that instead. Items with a use of their own on anyone are the `nightborn:keeps_own_use` item tag |
| WHEEL-2 | The wheel opens for a player's own thralls and familiars (MIND-6, FAM-1, FAM-4, FAM-6, FAM-9), for the NPC members and player members of their house and a younger vampire player a lord may invite (HOUSE-2, HOUSE-5, HOUSE-8, HOUSE-9), and for Guild Masters and hunters, where a human trades, joins the guild, takes contracts and trials, asks their rank, replaces a lost journal, asks for news of the nearest house and, once sworn, asks to be sent to it (GUILD-3, GUILD-18, HUNT-10, GUIDE-2). It shows only the actions that player may take |
| WHEEL-3 | The wheel is styled as the skill screens are (LINE-7). The server checks every action chosen from it (PERM-1) |

**Enforcement:** server. The client draws the wheel and sends the choice.

## 6. Permissions

A modified client is assumed. Hiding a control is never the enforcement of a rule. Decor and structure blocks have no owner, as a placed bed has none. A coffin has an owner only while it is assigned (COFFIN-13). Thralls and familiars have one owner.

### Capability Matrix

| Capability | Human | Vampire | Owner of a thrall or familiar | Operator |
|---|---|---|---|---|
| Bite a creature | No | Yes | Yes | Yes, as a vampire |
| Bite a player | No | Only with PvP on | Only with PvP on | Only with PvP on |
| Earn and spend hunter points | Once sworn into the guild | No | No | Through CFG-8 |
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
| Join the guild | Once they have staked a vampire | No | No | Through CFG-8 |
| Trade with a Guild Master | Yes | No | No | Yes, as a human |
| Take contracts and trials, or hold a rank | Once sworn into the guild | No | No | Yes, as a hunter |
| Found a house, and invite, order or release its members | No | Only as its lord | Only as its lord | Only to disband one (CFG-8) |
| Challenge for a house | No | Only a member of the lord's stage or older | Only a member of the lord's stage or older | No |
| Raise a hand mirror at someone | Yes | Yes | Yes | Yes |
| Wear accessories | Yes | Yes | Yes | Yes |
| Turn, cure or reset a player | No | No | No | Yes |
| Change server settings | No | No | No | Yes |

### Requirements

| ID | Requirement |
|---|---|
| PERM-1 | Every action in the matrix is checked on the server |
| PERM-2 | A skill purchase is checked on the server for points, order, stage or rank, and the limit of two bloodlines or two paths |
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
| Stage, rank, experience and the power bar | Draws the skill screen and the bar, and sends what the player dragged |
| Houses, their members, crests and duels | Draws the crest and the duel's circle |
| Village suspicion, known faces and nemeses | Draws nothing but what the village and the nemesis do |
| Trials, feats, titles and what a journal has unlocked | Draws the journals |
| What the command wheel may offer and what a choice does | Draws the wheel and sends the choice |
| What a hand mirror shows | Draws the framed glass |
| Names, epithets, their colours and who may set one | Draws the name tags and the naming dialogue, and sends the name the owner chose |
| Household behaviour and structures | Draws fog and plays ambience |
| Who is on which map | Draws the map it is sent |
| Gore events, and which parts a body has lost | Decides how much gore to draw (GORE-3), and hides lost parts |
| Coffin lids, ownership, burning and who sleeps inside, levitation, vomiting, vampire mob powers, the ritual and the Eldest, notoriety, the hunt and the Grand Inquisitor | Draws the lids, burning coffins, sleepers, poses, tells, vomit, the ritual and bell build-ups, the hunt's warnings and both arenas, with vomit scaled by GORE-3 |

### Concurrency

| ID | Requirement |
|---|---|
| CS-1 | Two vampires feeding on one victim share its blood. Neither can take more than it has |
| CS-2 | Two players using one coffin: the first lies down, the second is refused |
| CS-3 | A victim drained and killed in the same tick by a vampire and another attacker dies once and drops loot once |
| CS-4 | Two players filling Blood Bottles from one victim, by feeding or with a lancet, never create blood it did not have |
| CS-5 | A player cured and turned in the same tick ends in one defined state: the cure wins if it landed before the sickness ran out |
| CS-8 | An owner breaking their assigned coffin while another vampire sleeps in it wakes the sleeper and frees the coffin once |
| CS-9 | A coffin catching fire as a vampire lies down in it ends with the vampire awake and burning, never asleep in a burning coffin |
| CS-10 | Two vampires draining a downed Grand Inquisitor, or a drain and a blow that would each end him in the same tick, end him once: one loot roll, and one payment of points to each vampire who struck him |

### Per-Player State

| State | Scope |
|---|---|
| Faction, sickness, blood, skills, bloodlines, cooldowns and the turning bite choice | That player, synced only to them, except what others can see (CORE-4, CORE-8, WEAK-8, FACT-3) |
| Notoriety and the hunt | That player, held on the server and never sent to any client. The vampire is sent only the warnings (INQ-2) |
| Stage, rank, experience, power bar, titles and trials | That player, synced only to them, except what NAME-3, NAME-12 and RANK-4 show others |
| Nemeses and the villages that know a player's face | That player, held on the server. The player is sent their nemeses for the journal |
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
| SAFE-2 | Blood is never created except by feeding (FEED-4), turning (TURN-5), respawning as the persistence grid says, a human bleeding themself with a lancet (GEAR-10), and the blows of the Inquisitor's Greatsword (INQ-15) and of a weapon with Bloodletting (GEAR-11) |
| SAFE-3 | A bad config file never stops a server starting (CFG-3, CFG-4) |
| SAFE-4 | Every payload is size-limited and checked, including skill purchases, familiar orders and the names owners give (NAME-8) |
| SAFE-5 | Skill experience and points are never created except by FEED-12, HUNT-1, HUNT-2, RITE-6, INQ-15, NEM-6, CHAL-1 and CFG-8 |
| SAFE-6 | No mob changes a block a player built: not a hunter party pitching camp (GUILD-8), not a raiding party or a hunter party raiding a house (GUILD-13, GUILD-14), and not a village warding itself (SUSP-2). The tents and beds of a temporary camp drop nothing (GUILD-10) |

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
| PERF-9 | Village suspicion, nemeses, raiding parties and the refilling of empty structures cost nothing per tick while their chunks are unloaded, and are settled by the world's clock when they load |

### Persistence

| What | Chunk unload | World reload | Death | Disconnect | Power loss |
|---|---|---|---|---|---|
| Faction | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Bite sickness and the time left | not applicable | kept | kept | kept, paused while offline | kept to the last save |
| Turning bite choice | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Downed or escaping, and the time left | kept, for a mob | kept | cleared | kept, so leaving does not escape it | kept to the last save |
| Lost and replaced parts of a body | kept, for a mob | kept | cleared, except replaced limbs | kept | kept to the last save |
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
| Stage, nights lived, blood drunk and deeds done | not applicable | kept | kept, except CORE-10 | kept, and nights do not count while offline | kept to the last save |
| Whether a human has joined the guild, their rank and deeds done | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| Hunter paths and skill experience | not applicable | kept | kept, except SKILL-6 and CORE-10 | kept | kept to the last save |
| The power bar's slots, titles and feats | not applicable | kept | kept, except CORE-10 | kept | kept to the last save |
| A house, its lord, members, name, crest and structure | kept | kept | passes on when the lord is ended (HOUSE-9) | kept | kept to the last save |
| A raiding party and its quarry | kept with its chunk, and gone at the next dusk by the world's clock | kept | not applicable | not applicable | kept to the last save |
| Old blood and how full it is | kept | kept | not applicable | not applicable | kept to the last save |
| An empty structure's time until it refills | kept, and counted by the world's clock | kept | not applicable | not applicable | kept to the last save |
| A village's suspicion and known faces | kept, and faded by the world's clock | kept | kept | kept | kept to the last save |
| A nemesis, its quarry and what it has gained | kept | kept | kept | kept | kept to the last save |
| Trials on offer and those done | not applicable | kept | kept | kept | kept to the last save |
| Worn accessories | not applicable | kept | dropped or kept as armour is | kept | kept to the last save |
| Settings | not applicable | kept | not applicable | not applicable | written atomically |

### Look And Sound

| ID | Requirement |
|---|---|
| LOOK-1 | Interiors are candlelit: warm amber light against deep shadow |
| LOOK-2 | Places feel old, rich and lived in: decaying opulence, layered with portraits, velvet, dusty books, taxidermy and tarnished gold |
| LOOK-3 | Colours are muted jewel tones: burgundy, emerald, black and gold |
| LOOK-4 | Each vampire dresses in the era they were turned in, so a household mixes centuries |
| LOOK-5 | Every model and animation is hand-made to a premium bar and judged against the best Minecraft art. Every mob is animated at rest, moving and attacking. Every vampire, player or mob, is also animated while feeding, burning in the sun, falling to a stake (kneeling and slumping, not rolled over), stretching after it steps out of its coffin, and lighting or putting out a candle or starting a gramophone. Every player, vampire, hunter or human, plays a short clip on their model each time they use a power, and the clip never delays the power's effect. A vampire mob using the same power plays the same clip. A vampire mob at rest now and then sets its clothes straight, but never mid-fight |
| LOOK-6 | Music is baroque and gothic: harpsichord, organ, choir and brooding strings. It plays in and near vampire structures, cutting off the game's music, and in place of the game's music at night |
| LOOK-7 | Vampire structures sound like old houses: creaks, settling, drips, guttering candles, wind in the halls and distant bells, growing louder towards the heart |
| LOOK-8 | Blocks and items look like vanilla ones wherever they are held or shown: GUI, first and third person, item frames and the ground. Stakes are the one exception in the hand: they are held point down in a reverse grip, and attacking with one drives it forward in a thrust in place of the vanilla swing. They look vanilla in the GUI, item frames and on the ground |
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
| COMPAT-1 | Minecraft 1.21.1, Java 21, Fabric Loader 0.16.5 or later, Fabric API 0.116.12+1.21.1 or later, GeckoLib 4.7 or later, Patchouli for the journals (GUIDE-1) and Trinkets for the accessory slots (WORN-1) |
| COMPAT-2 | Mod Menu, JEI and EMI are optional. Without them, the mod works fully |
| COMPAT-3 | Nightborn needs no other mod in the family |
| COMPAT-4 | Vanilla undead behave as they do without the mod, except CORE-9, and beds and night sleeping work as normal for humans |
| COMPAT-5 | Feeding lists, crucifix materials, the foods and drinks a vampire can keep down, structures, temporary camp layouts, loot, relic chances, trades, what adds to notoriety and to suspicion, the phases, attacks and lines of the Eldest and the Grand Inquisitor, the gifts of blood, accessories and where they are found, trials and feats, veteran hunters and their attacks, the raiding party sent against each stage, hunter mob rank odds, leanings and growth, a nemesis's lines and epithets, the journals' pages, the lore books and the shelf each vampire structure draws from, name pools and epithets, vampire mob stages, skills, looks, sizes, builds and outfits, and hunter looks and kits can be changed by data packs |
| COMPAT-6 | Shipped languages: English, German, Spanish, French, Brazilian Portuguese, Russian and Simplified Chinese |
| COMPAT-7 | Every declared dependency is used |

## 9. Release Criteria

- Every use case's requirements hold on a dedicated server with two clients and on an integrated server with one client, through the same code.
- A human can drink old blood, get sick, and either cure it or turn, without outside help.
- A vampire can feed, survive a full day in a coffin, buy skills in two bloodlines, come of age and found a house.
- A human can stake a vampire, be sworn into the guild, choose two paths and rise a rank.
- A human can find a vampire structure, down a vampire with hunter gear and stake it.
- A vampire who kills enough hunters is hunted, fights the Grand Inquisitor and can drain him.
- A vampire dies in the sun, and gets up and escapes after being downed by anything else.
- No item, blood or skill point can be duplicated or lost by any mechanic the mod adds (SAFE-1, SAFE-2).
- A dedicated server starts with no client classes loaded (CS-6).
- Every string is translated in every shipped language.
- Every server setting is reachable from the command line.
- A world from the previous release opens with every player's faction, blood and vampire skills intact.

## 10. References

- **Progress:** the project board. This document holds no status.
- **Acceptance criteria:** each requirement's owning issue. This document does not repeat them.
- **Reasoning:** the decision records in [`docs/adr/`](adr/).
- **Engineering standards:** [`docs/standards.md`](standards.md).
- **Player-facing summary:** the [README](../README.md).
