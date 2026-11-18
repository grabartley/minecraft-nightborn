package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingItems;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class AccessoryItems {

  public static final List<String> IDS =
      List.of(
          "mourning_veil",
          "high_collared_cape",
          "onyx_ring",
          "velvet_choker",
          "cameo_brooch",
          "lace_gloves",
          "brass_buckled_belt");

  private static final Map<String, AccessoryItem> ITEMS =
      IDS.stream()
          .collect(Collectors.toUnmodifiableMap(Function.identity(), AccessoryItems::registerItem));
  private static final List<AccessoryItem> ALL = IDS.stream().map(ITEMS::get).toList();

  private AccessoryItems() {}

  public static AccessoryItem of(final String id) {
    final AccessoryItem item = ITEMS.get(id);
    if (item == null) {
      throw new IllegalArgumentException("No accessory " + id);
    }
    return item;
  }

  public static List<AccessoryItem> all() {
    return ALL;
  }

  public static void register() {
    final List<? extends Item> clothing = ClothingItems.all();
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(entries -> entries.addAfter(clothing.getLast(), all().toArray(new Item[0])));
    all()
        .forEach(
            item -> {
              TrinketsApi.registerTrinket(item, AccessoryTrinket.INSTANCE);
              CauldronBehavior.WATER_CAULDRON_BEHAVIOR
                  .map()
                  .put(item, CauldronBehavior.CLEAN_DYEABLE_ITEM);
            });
  }

  private static AccessoryItem registerItem(final String id) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(Nightborn.MOD_ID, id),
        new AccessoryItem(id, new Item.Settings().maxCount(1)));
  }
}
