package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingItems;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.Arrays;
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

  private static final Map<AccessoryPiece, AccessoryItem> ITEMS =
      Arrays.stream(AccessoryPiece.values())
          .collect(Collectors.toUnmodifiableMap(Function.identity(), AccessoryItems::registerItem));
  private static final List<AccessoryItem> ALL =
      Arrays.stream(AccessoryPiece.values()).map(ITEMS::get).toList();

  private AccessoryItems() {}

  public static AccessoryItem of(final AccessoryPiece piece) {
    return ITEMS.get(piece);
  }

  public static List<AccessoryItem> all() {
    return ALL;
  }

  public static void register() {
    final List<? extends Item> clothing = ClothingItems.all();
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(entries -> entries.addAfter(clothing.getLast(), all().toArray(new Item[0])));
    all().forEach(item -> TrinketsApi.registerTrinket(item, AccessoryTrinket.INSTANCE));
    all().stream()
        .filter(item -> item.piece().dyeable())
        .forEach(
            item ->
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR
                    .map()
                    .put(item, CauldronBehavior.CLEAN_DYEABLE_ITEM));
  }

  private static AccessoryItem registerItem(final AccessoryPiece piece) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(Nightborn.MOD_ID, piece.id()),
        new AccessoryItem(piece, new Item.Settings().maxCount(1)));
  }
}
