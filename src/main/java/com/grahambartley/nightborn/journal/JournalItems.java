package com.grahambartley.nightborn.journal;

import java.util.EnumMap;
import java.util.Map;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class JournalItems {

  private static final Map<Journal, Item> ITEMS = new EnumMap<>(Journal.class);

  public static final Item HUNTER_JOURNAL = register(Journal.HUNTER);
  public static final Item VAMPIRE_JOURNAL = register(Journal.VAMPIRE);

  private JournalItems() {}

  static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(
            entries -> entries.addAfter(Items.WRITABLE_BOOK, HUNTER_JOURNAL, VAMPIRE_JOURNAL));
  }

  public static Item of(final Journal journal) {
    return ITEMS.get(journal);
  }

  private static Item register(final Journal journal) {
    final Item item =
        Registry.register(
            Registries.ITEM,
            journal.id(),
            new JournalItem(journal, new Item.Settings().maxCount(1)));
    ITEMS.put(journal, item);
    return item;
  }
}
