# Accessory List

This is the locked list of all 80 accessories. The rules they follow are in `docs/prd.md` (WORN-1 to WORN-6). This document says what each one is.

Every number here is the default and lives in data. Nobody building or drawing an accessory chooses a name, a gift or a number of their own. If something is missing, ask.

## The Set At A Glance

| Slot | Plain | With a gift | Total |
|---|---|---|---|
| Ring (two slots) | 4 | 16 | 20 |
| Necklace | 3 | 7 | 10 |
| Brooch | 3 | 5 | 8 |
| Back | 5 | 7 | 12 |
| Gloves | 3 | 5 | 8 |
| Belt | 2 | 6 | 8 |
| Face | 4 | 10 | 14 |
| **All** | **24** | **56** | **80** |

Of the 56 with a gift, 19 work only for a vampire, 23 only for a human, 8 for anyone, and 6 are boss rings.

## Rules Every Accessory Follows

- Plain pieces are for looks, carry no gift and are crafted. Every other piece carries one gift and is found, never crafted.
- A gift is small. It makes its wearer a little better at what they already do, and never does the job of a skill.
- Two of the same piece give the gift once. Different pieces with the same kind of gift add up, but no resistance or reduction passes 80% in all, whatever is learned and worn together.
- No gift, alone or with others, removes a vampire weakness or lets a vampire walk in the sun.
- A piece that works only for a vampire does nothing on a human, and the other way round. Its tooltip says who it works for.
- A piece that holds items is filled and emptied as a bundle is. It keeps what it holds when taken off, and drops it on death as an inventory does.
- A stage or a rank never gives an accessory.
- Every piece but a ring is drawn on the wearer, on both player models (WORN-2).
- Materials and motifs: tarnished gold, silver, jet, garnet, emerald, bone, velvet and lace. Old, worn and a little sinister, never clean or bright.

## Clothing The Mod Already Has

The mod's clothing stays, and two pieces move to the slot that now fits them. Nothing a player owns is lost: an item in an old world is the same item and goes in its new slot.

| Piece | Slot today | Slot from now on |
|---|---|---|
| Tricorne, Top Hat | Head | Head, unchanged |
| Mourning Veil | Head | Face. It is one of the 80 accessories, a plain one |
| High-Collared Cape | Chest | Back. It is one of the 80 accessories, a plain one |
| Frock Coat, Corset, Doublet | Chest | Chest, unchanged |
| Breeches, Trousers, Long Skirt | Legs | Legs, unchanged |
| Buckled Shoes, Riding Boots | Feet | Feet, unchanged |
| Crown, Mantle, Greaves and Boots of the Eldest | Armour | Armour, unchanged |

- A piece that moves keeps its name, look, recipe, dye and cuts. It no longer gives armour, as no accessory does, and it no longer wears out.
- A vanilla enchantment already on a piece that moves stays on the item and does nothing.
- When an older world loads, a veil or a cape still worn in its old slot moves to its new slot if that is free, or to the inventory, or is dropped at the player's feet if the inventory is full.
- A back piece takes the mod's chest enchantments (Batwing, Mistcloak, Thrift) and a face piece takes its headwear ones (Nocturne, Shroud), and no other accessory takes any, except the three curses, which are found on any of them (WEAR-6). The same enchantment on two worn pieces counts once, at the higher level.
- With the veil gone from the head, the head needs a second piece (WEAR-1). A new Wide-Brimmed Hat joins the Top Hat: a dark felt hat with a low crown, crafted from 4 wool and 1 leather, dyed like the rest. It is clothing, worn on the head, and its model is made through the `bbmodel` MCP in both cuts like every other piece.
- Vampire and hunter mobs keep wearing the veil and the cape as they do today, and still wear only what a player can craft (WEAR-5).
- Dressed To Kill still asks for clothing in all four armour slots, and Dressed To The Nines for an accessory in every slot.

## Where Pieces Are Found

| Source | What it gives |
|---|---|
| Crypt, ruined manor, underground lair | The common pieces, in their chests |
| Castle | The rarer pieces |
| Mountain castle | The rarest pieces that are not boss rings |
| The Eldest | The three Eldest rings. It drops one rarely (RITE-12), and never one a player who struck it already owns while they lack another |
| The Grand Inquisitor | The three Inquisitor rings, the same way (INQ-15) |
| Vampire trial, hunter trial | Named in `docs/challenges.md` |
| Mastery feat | Named in `docs/challenges.md` |

Vampire structures hold hunter pieces too: they are what is left of hunters who did not come back.

## Rings

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Gold Band | A plain worn gold ring | Anyone | None | Cut at a stonecutter from 1 gold ingot |
| Iron Signet | A dull iron ring with a blank seal | Anyone | None | Cut at a stonecutter from 1 iron ingot |
| Onyx Ring | A black stone in tarnished silver | Anyone | None | Crafted: 1 gold nugget and 1 coal |
| Pearl Ring | A small pearl in gold | Anyone | None | Crafted: 1 gold nugget and 1 nautilus shell |
| Garnet Signet | A deep red stone cut with a bat | Vampire | Blood drains 10% slower | Castle |
| Bloodstone Ring | A dark green stone flecked with red | Vampire | A finisher bite gives 2 more blood points | Ruined manor |
| Jet Band | A band of polished jet | Vampire | Power cooldowns are 8% shorter | Underground lair |
| Thrall-Keeper's Ring | Gold, with a tiny chain hanging from it | Vampire | Thralls have 20% more health | Vampire trial |
| Blessed Ring | Plain silver, cut with a cross | Human | A tenth more bite resistance | Crypt |
| Saint's Ring | Gold, set with a tiny bone | Human | A blessing on a weapon lasts 25% longer | Hunter trial |
| Tracker's Ring | Brass, cut with a compass rose | Human | Tracking's cooldown is 20% shorter | Ruined manor |
| Ring Of Vigil | Iron, set with a pale blue stone | Human | A chill, shown and heard, when an awake vampire mob is within 12 blocks. A vampire player never sets it off | Castle |
| Widow's Ring | Black enamel and a lock of hair under glass | Anyone | 1 more luck when opening a chest | Crypt |
| Moonstone Ring | A milky stone that catches the light | Human | Takes 10% less damage at night | Underground lair |
| Ring Of The Court | Gold, crowned, set with a ruby | Human | Mesmerise and dread last half as long on the wearer | The Eldest |
| Ring Of The Beast | Bone, carved as a bat's skull | Human | Fall damage is halved | The Eldest |
| Ring Of The Blood | A glass ring with dark blood sealed in it | Human | A blow on a vampire heals the wearer 1 health, once every 2 seconds | The Eldest |
| Ring Of The Hunt | Blackened iron, cut with a crossbow | Vampire | Bolts and thrown stakes deal 25% less damage | The Grand Inquisitor |
| Ring Of The Faith | Silver gone grey, its cross worn smooth | Vampire | Holy water weakens for 30% less time | The Grand Inquisitor |
| Ring Of The Pyre | Scorched gold, set with an ember-red stone | Vampire | Fire not lit by the sun deals half damage. The sun's burn is unchanged | The Grand Inquisitor |

## Necklaces

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Pearl Strand | A long rope of pearls | Anyone | None | Crafted: 3 nautilus shells and 1 string |
| Velvet Choker | A black velvet band | Anyone | None | Crafted: 1 black wool and 1 string |
| Gold Chain | A heavy tarnished chain | Anyone | None | Crafted: 3 gold nuggets |
| Blood Vial Pendant | A stoppered glass vial on a chain | Vampire | Holds one Blood Bottle, drunk by itself when the blood bar runs dry | Castle |
| Raven Locket | A locket shaped as a raven's head | Vampire | Familiars last 25% longer | Underground lair |
| Choker Of Thorns | Black iron thorns on a ribbon | Vampire | A feeding bite gives 10% more blood | Vampire trial |
| Crucifix On A Chain | A small silver crucifix | Human | Weakens and slows a vampire as a held crucifix does, at half the range, with no hand. It does not stop a bite | Castle |
| Garlic Sachet | A linen pouch on a cord | Human | A vampire mob's bite is 15% more likely to be broken. A mob's bite cannot otherwise be broken, so it breaks 15% of the time | Crypt |
| Saint's Medal | A worn bronze medal | Human | Healing prayers heal 20% more | Hunter trial |
| Keepsake Locket | A gold locket with a painted face inside | Anyone | Villagers' prices are 5% lower | Ruined manor |

## Brooches

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Cameo Brooch | A pale profile on a dark oval | Anyone | None | Crafted: 1 quartz and 1 gold nugget |
| Jet Brooch | A cluster of black beads | Anyone | None | Crafted: 2 coal and 1 gold nugget |
| Silver Filigree Brooch | Fine silver wire in a knot | Anyone | None | Crafted: 2 iron nuggets |
| Bat Brooch | A gold bat with garnet eyes | Vampire | Bat Form costs 1 less blood, never less than 1 | Ruined manor |
| Rose Brooch | An enamel rose, half wilted | Vampire | Being seen feeding raises a village's suspicion 20% less | Castle |
| Guild Badge | A brass badge of the guild | Human | A Guild Master's prices are 10% lower | Hunter trial |
| Sunburst Brooch | A gold sun with long rays | Human | Holy water splashes over a radius 1 block greater. Its strength fades over the wider radius, so it lands a little stronger at each distance | Crypt |
| Spider Brooch | A black spider with a red stone | Anyone | Cobwebs do not slow the wearer | Underground lair |

## Back

Capes and cloaks sway as the wearer moves and are drawn over armour and coats.

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Velvet Cape | A short cape that can be dyed | Anyone | None | Crafted: 3 wool and 1 string |
| High-Collared Cape | The cape the mod already has: black outside, red inside, with a high collar, and it can be dyed | Anyone | None | Crafted, with the recipe it has today |
| Tattered Shroud | Grey, torn and trailing | Anyone | None | Crafted: 3 string and 2 cobwebs |
| Fur-Trimmed Mantle | Heavy wool with a fur collar | Anyone | None | Crafted: 3 wool and 2 rabbit hide |
| Half Cape | Worn over one shoulder, can be dyed | Anyone | None | Crafted: 2 wool and 1 string |
| Cloak Of Dusk | Deep violet that fades to black at the hem | Vampire | Vanish carries 1 block further | Castle |
| Nightglider's Cloak | Thin and ribbed like a wing | Vampire | Levitating drains 15% less blood | Mountain castle |
| Lord's Mantle | Burgundy velvet and ermine | Vampire | NPC members of the wearer's house within 16 blocks deal 10% more damage | Mastery feat |
| Pilgrim's Cloak | Rough brown wool with a scallop shell | Human | Heals 1 health every 10 seconds in daylight | Crypt |
| Stalker's Cloak | Mottled grey and green | Human | 10% less noise in a vampire's house | Ruined manor |
| Warden's Cape | Dark blue with a silver clasp | Human | A tenth more mesmerise resistance | Hunter trial |
| Traveller's Cloak | Oiled canvas, hooded | Anyone | Never freezes and does not sink in powder snow | Underground lair |

## Gloves

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Lace Gloves | Black lace to the wrist | Anyone | None | Crafted: 2 string |
| Riding Gloves | Brown leather, worn at the knuckles | Anyone | None | Crafted: 2 leather |
| Fingerless Gloves | Grey wool with the fingers cut off | Anyone | None | Crafted: 2 wool |
| Clawed Gauntlets | Black leather with iron talons | Vampire | Bare hands deal 1 more damage | Underground lair |
| Silk Gloves | White silk, spotless | Vampire | Feeding is noticed from 30% less far | Ruined manor |
| Stakewright's Gloves | Thick hide, scarred | Human | A stake's blow deals 10% more damage | Crypt |
| Silver-Threaded Gloves | Grey leather stitched with silver | Human | Breaks free of a bite 15% sooner, applied after bite resistance | Castle |
| Gravedigger's Mitts | Heavy, earth-stained | Anyone | Digs dirt, gravel and sand 20% faster | Crypt |

## Belts

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Brass-Buckled Belt | Dark leather and a square buckle | Anyone | None | Crafted: 2 leather and 1 gold nugget |
| Silk Sash | A wide sash that can be dyed | Anyone | None | Crafted: 2 wool |
| Cellarer's Belt | Leather with loops for bottles | Vampire | A Blood Bottle gives 10% more blood when drunk | Ruined manor |
| Bandolier | A strap across the chest with loops and pouches | Human | Holds 2 stacks of bolts and 4 splash holy water potions, used from the belt before the inventory | Castle |
| Stake Loops | A belt hung with wooden stakes | Human | Holds 6 stakes. A new one comes to hand when the held one breaks | Crypt |
| Flask Belt | A belt of small padded pockets for bottles | Human | Holds 6 splash or lingering holy water potions, thrown from the belt before the inventory | Underground lair |
| Trapper's Pouch | A hide pouch of wire and springs | Human | Snares and flash traps can be set again 15% sooner | Hunter trial |
| Sturdy Girdle | Thick leather and iron plates | Anyone | A tenth more resistance to being knocked back | Ruined manor |

## Face

| Name | Look | Works for | Gift | Found |
|---|---|---|---|---|
| Mourning Veil | The veil the mod already has: black lace over the face, and it can be dyed | Anyone | None | Crafted, with the recipe it has today |
| Domino Mask | A plain black half mask | Anyone | None | Crafted: 1 leather and 1 black dye |
| Wire Spectacles | Round lenses in thin wire | Anyone | None | Crafted: 2 glass panes and 1 iron nugget |
| Plain Monocle | One lens on a cord | Anyone | None | Crafted: 1 glass pane and 1 gold nugget |
| Widow's Veil | A long, heavy black veil to the chin | Vampire | The flinch from garlic and crucifixes is half as large | Castle |
| Smoked Spectacles | Dark round lenses | Vampire | The glow of the wearer's eyes shows from half as far | Ruined manor |
| Porcelain Mask | A white face with no expression | Vampire | A village that sees the wearer feed is half as likely to learn their face | Mountain castle |
| Death Mask | A grey cast of a sleeping face | Vampire | Rises from being downed 10% sooner | Vampire trial |
| Hunter's Monocle | Brass, with a cross-hair lens | Human | Shows the health and stage of a vampire mob the wearer looks at | Castle |
| Confessor's Veil | White linen across the mouth | Human | A tenth more mesmerise resistance | Crypt |
| Garlic-Soaked Scarf | A stained scarf across the nose | Human | The Weakness after a bite lasts 25% less time | Underground lair |
| Lantern-Lens Goggles | Thick amber lenses in leather | Human | Sees through Grave Fog. Its slowness still applies | Hunter trial |
| Plague Mask | A long leather beak | Anyone | Lingering potion clouds affect the wearer for half as long | Crypt |
| Opera Mask | A gilded half mask on a ribbon | Anyone | The wearer's name does not show from more than 8 blocks | Mastery feat |

## Art And Feel

- It must be fun. Being a vampire or a hunter is a power fantasy, and everything here should make the player feel strong, feared and clever.
- Keep it balanced: every power and gift has a cost or a counter on the other side.
- Everything looks and sounds like it belongs with the rest of the mod. The LOOK rows of `docs/prd.md` are the whole style brief: dark gothic, played straight.
- The art bar is AAA Minecraft studio quality. Every model, clip and texture is made in a Blockbench project through the `bbmodel` MCP and is a GeckoLib model or clip. Flat item sprites are made through the `item-sprite` skill. Nothing is posed or animated in code, and no art file is edited by hand.
