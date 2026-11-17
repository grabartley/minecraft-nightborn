package com.grahambartley.nightborn.ritual;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class RitualItems {

  public static final TagKey<Item> RELICS =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "relics"));

  public static final Item CASTLE_SIGNET = relic("castle_signet");
  public static final Item CRYPT_RELIQUARY = relic("crypt_reliquary");
  public static final Item MANOR_LOCKET = relic("manor_locket");
  public static final Item LAIR_FANG = relic("lair_fang");

  static final List<Item> ALL = List.of(CASTLE_SIGNET, CRYPT_RELIQUARY, MANOR_LOCKET, LAIR_FANG);

  private RitualItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(entries -> ALL.forEach(entries::add));
  }

  public static int relicsNeeded() {
    return Registries.ITEM.getEntryList(RELICS).map(RegistryEntryList::size).orElse(0);
  }

  private static Item relic(final String name) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(Nightborn.MOD_ID, name),
        new Item(new Item.Settings().maxCount(1).rarity(Rarity.RARE)));
  }
}
