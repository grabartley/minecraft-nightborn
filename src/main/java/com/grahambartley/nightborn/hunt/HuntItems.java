package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class HuntItems {

  public static final Item GUILD_MASTERS_SEAL =
      register("guild_masters_seal", new Item(new Item.Settings()));
  public static final Item HUNTERS_BELL =
      register("hunters_bell", new HuntersBellItem(new Item.Settings().maxCount(1)));

  private HuntItems() {}

  static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(entries -> entries.add(GUILD_MASTERS_SEAL));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.add(HUNTERS_BELL));
  }

  private static Item register(final String path, final Item item) {
    return Registry.register(Registries.ITEM, Identifier.of(Nightborn.MOD_ID, path), item);
  }
}
