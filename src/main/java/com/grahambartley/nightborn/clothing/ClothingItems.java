package com.grahambartley.nightborn.clothing;

import com.grahambartley.nightborn.Nightborn;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ClothingItems {

  private static final Map<ClothingPiece, ClothingItem> ITEMS =
      Arrays.stream(ClothingPiece.values())
          .collect(Collectors.toUnmodifiableMap(Function.identity(), ClothingItems::registerItem));
  private static final List<ClothingItem> ALL =
      Arrays.stream(ClothingPiece.values()).map(ITEMS::get).toList();

  private ClothingItems() {}

  public static ClothingItem of(final ClothingPiece piece) {
    return ITEMS.get(piece);
  }

  public static List<ClothingItem> all() {
    return ALL;
  }

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(entries -> entries.addAfter(Items.LEATHER_BOOTS, all().toArray(new Item[0])));
    all().stream()
        .filter(item -> item.piece().dyeable())
        .forEach(
            item ->
                CauldronBehavior.WATER_CAULDRON_BEHAVIOR
                    .map()
                    .put(item, CauldronBehavior.CLEAN_DYEABLE_ITEM));
  }

  private static ClothingItem registerItem(final ClothingPiece piece) {
    return Registry.register(
        Registries.ITEM,
        Identifier.of(Nightborn.MOD_ID, piece.id()),
        new ClothingItem(piece, new Item.Settings().maxCount(1).maxDamage(piece.durability())));
  }
}
