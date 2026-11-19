package com.grahambartley.nightborn.inquisitor;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.StatueBlock;
import com.grahambartley.nightborn.eldest.PetBatItem;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SmithingTemplateItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class InquisitorSpoils {

  public static final TagKey<Item> SET_ARMOR = tag("inquisitor_set_armor");
  public static final TagKey<Item> SET_BLADES = tag("inquisitor_set_blades");
  public static final RegistryKey<JukeboxSong> SONG =
      RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(Nightborn.MOD_ID, "inquisitor"));

  public static final SoundEvent SONG_SOUND = NightbornSounds.register("music_disc.inquisitor");

  public static final Item MAIL = item("inquisitor_mail", Item::new);

  public static final Item GREATSWORD =
      item(
          "inquisitor_greatsword",
          settings ->
              new SwordItem(
                  ToolMaterials.NETHERITE,
                  settings
                      .fireproof()
                      .rarity(Rarity.EPIC)
                      .attributeModifiers(
                          SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 5, -2.8F))));

  public static final Item HAT = armor("inquisitor_hat", ArmorItem.Type.HELMET);
  public static final Item COAT = armor("inquisitor_coat", ArmorItem.Type.CHESTPLATE);
  public static final Item GREAVES = armor("inquisitor_greaves", ArmorItem.Type.LEGGINGS);
  public static final Item BOOTS = armor("inquisitor_boots", ArmorItem.Type.BOOTS);

  public static final Item CROSS_TRIM =
      item(
          "cross_armor_trim_smithing_template",
          settings -> SmithingTemplateItem.of(InquisitorTrims.CROSS));
  public static final Item CHAIN_TRIM =
      item(
          "chain_armor_trim_smithing_template",
          settings -> SmithingTemplateItem.of(InquisitorTrims.CHAIN));

  public static final Item DISC =
      item(
          "music_disc_inquisitor",
          settings -> new Item(settings.maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(SONG)));

  public static final Item TAME_HAWK =
      item(
          "tame_hawk",
          settings ->
              new PetBatItem(
                  () -> InquisitorMobs.TAME_HAWK, settings.maxCount(1).rarity(Rarity.EPIC)));

  static final Identifier CRUCIFIX_ID = Identifier.of(Nightborn.MOD_ID, "broken_crucifix");

  public static final Block BROKEN_CRUCIFIX =
      Registry.register(
          Registries.BLOCK,
          CRUCIFIX_ID,
          new StatueBlock(
              AbstractBlock.Settings.copy(Blocks.GOLD_BLOCK).nonOpaque(),
              Block.createCuboidShape(3, 0, 5, 13, 14, 11)));

  public static final Item BROKEN_CRUCIFIX_ITEM =
      Registry.register(
          Registries.ITEM,
          CRUCIFIX_ID,
          new BlockItem(BROKEN_CRUCIFIX, new Item.Settings().rarity(Rarity.RARE)));

  private InquisitorSpoils() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(
            entries -> {
              entries.add(GREATSWORD);
              entries.add(HAT);
              entries.add(COAT);
              entries.add(GREAVES);
              entries.add(BOOTS);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(
            entries -> {
              entries.add(MAIL);
              entries.add(CROSS_TRIM);
              entries.add(CHAIN_TRIM);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(
            entries -> {
              entries.add(DISC);
              entries.add(TAME_HAWK);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(BROKEN_CRUCIFIX_ITEM));
  }

  private static Item armor(final String name, final ArmorItem.Type type) {
    return item(
        name,
        settings ->
            new ArmorItem(
                InquisitorArmor.MATERIAL,
                type,
                settings
                    .fireproof()
                    .rarity(Rarity.EPIC)
                    .maxDamage(type.getMaxDamage(InquisitorArmor.DURABILITY))));
  }

  private static Item item(final String name, final Function<Item.Settings, Item> factory) {
    return Registry.register(
        Registries.ITEM, Identifier.of(Nightborn.MOD_ID, name), factory.apply(new Item.Settings()));
  }

  private static TagKey<Item> tag(final String path) {
    return TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, path));
  }
}
