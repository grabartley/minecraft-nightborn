package com.grahambartley.nightborn.ammo;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.holy.HolyWater;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.DispenserBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class AmmoItems {

  public static final Item STAKE_BOLT = item("stake_bolt", BoltItem::new);
  public static final Item SILVER_BOLT = item("silver_bolt", BoltItem::new);
  public static final Item HOLY_WATER_BOLT =
      item(
          "holy_water_bolt",
          settings ->
              new BoltItem(
                  settings.component(
                      DataComponentTypes.POTION_CONTENTS,
                      new PotionContentsComponent(HolyWater.POTION))));
  public static final Item GARLIC_BOMB = item("garlic_bomb", GarlicBombItem::new);

  public static final List<Item> ALL =
      List.of(STAKE_BOLT, SILVER_BOLT, HOLY_WATER_BOLT, GARLIC_BOMB);

  private AmmoItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(entries -> entries.addAfter(Items.CROSSBOW, ALL.toArray(Item[]::new)));
    ALL.forEach(DispenserBlock::registerProjectileBehavior);
  }

  private static Item item(final String name, final Function<Item.Settings, Item> factory) {
    return Registry.register(
        Registries.ITEM, Identifier.of(Nightborn.MOD_ID, name), factory.apply(new Item.Settings()));
  }
}
