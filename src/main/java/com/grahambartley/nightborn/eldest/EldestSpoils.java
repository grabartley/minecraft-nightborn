package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.StakeItem;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.sound.NightbornSounds;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
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

public final class EldestSpoils {

  public static final TagKey<Item> SET_ARMOR =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "eldest_set_armor"));
  public static final TagKey<Item> SET_BLADES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "eldest_set_blades"));
  public static final RegistryKey<JukeboxSong> SONG =
      RegistryKey.of(RegistryKeys.JUKEBOX_SONG, Identifier.of(Nightborn.MOD_ID, "eldest"));

  public static final SoundEvent SONG_SOUND = NightbornSounds.register("music_disc.eldest");

  public static final Item ASH = item("eldest_ash", Item::new);

  public static final Item ASHEN_SILVER_SWORD =
      item(
          "ashen_silver_sword",
          settings ->
              new SwordItem(
                  AshenSilver.ASHEN_SILVER,
                  settings.attributeModifiers(
                      SwordItem.createAttributeModifiers(AshenSilver.ASHEN_SILVER, 3, -2.4F))));
  public static final Item ASHEN_SILVER_AXE =
      item(
          "ashen_silver_axe",
          settings ->
              new AxeItem(
                  AshenSilver.ASHEN_SILVER,
                  settings.attributeModifiers(
                      AxeItem.createAttributeModifiers(AshenSilver.ASHEN_SILVER, 5.0F, -3.0F))));
  public static final Item ASHEN_SILVER_STAKE =
      item("ashen_silver_stake", settings -> new StakeItem(AshenSilver.ASHEN_SILVER, settings));

  public static final Item BLADE =
      item(
          "eldest_blade",
          settings ->
              new SwordItem(
                  ToolMaterials.NETHERITE,
                  settings
                      .fireproof()
                      .rarity(Rarity.EPIC)
                      .attributeModifiers(
                          SwordItem.createAttributeModifiers(ToolMaterials.NETHERITE, 4, -2.2F))));

  public static final Item CROWN = armor("eldest_crown", ArmorItem.Type.HELMET);
  public static final Item MANTLE = armor("eldest_mantle", ArmorItem.Type.CHESTPLATE);
  public static final Item GREAVES = armor("eldest_greaves", ArmorItem.Type.LEGGINGS);
  public static final Item BOOTS = armor("eldest_boots", ArmorItem.Type.BOOTS);

  public static final Item FANG_TRIM =
      item(
          "fang_armor_trim_smithing_template",
          settings -> SmithingTemplateItem.of(EldestTrims.FANG));
  public static final Item SIGIL_TRIM =
      item(
          "sigil_armor_trim_smithing_template",
          settings -> SmithingTemplateItem.of(EldestTrims.SIGIL));

  public static final Item DISC =
      item(
          "music_disc_eldest",
          settings -> new Item(settings.maxCount(1).rarity(Rarity.RARE).jukeboxPlayable(SONG)));

  public static final Item TAME_BAT =
      item(
          "tame_bat",
          settings ->
              new PetBatItem(
                  () -> NightbornMobs.PET_BAT, settings.maxCount(1).rarity(Rarity.EPIC)));

  private EldestSpoils() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(
            entries -> {
              entries.add(ASHEN_SILVER_SWORD);
              entries.add(ASHEN_SILVER_AXE);
              entries.add(ASHEN_SILVER_STAKE);
              entries.add(BLADE);
              entries.add(CROWN);
              entries.add(MANTLE);
              entries.add(GREAVES);
              entries.add(BOOTS);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(
            entries -> {
              entries.add(ASH);
              entries.add(FANG_TRIM);
              entries.add(SIGIL_TRIM);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(
            entries -> {
              entries.add(DISC);
              entries.add(TAME_BAT);
            });
  }

  private static Item armor(final String name, final ArmorItem.Type type) {
    return item(
        name,
        settings ->
            new ArmorItem(
                EldestArmor.MATERIAL,
                type,
                settings
                    .fireproof()
                    .rarity(Rarity.EPIC)
                    .maxDamage(type.getMaxDamage(EldestArmor.DURABILITY))));
  }

  private static Item item(final String name, final Function<Item.Settings, Item> factory) {
    return Registry.register(
        Registries.ITEM, Identifier.of(Nightborn.MOD_ID, name), factory.apply(new Item.Settings()));
  }
}
