package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import java.util.function.Function;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class GearItems {

  static final float COMPOST_CHANCE = 0.65F;

  public static final Item SILVER_INGOT = item("silver_ingot", Item::new);
  public static final Item SILVER_NUGGET = item("silver_nugget", Item::new);
  public static final Item RAW_SILVER = item("raw_silver", Item::new);

  public static final Item WOODEN_STAKE =
      item("wooden_stake", settings -> new StakeItem(ToolMaterials.WOOD, settings));
  public static final Item SILVER_STAKE =
      item("silver_stake", settings -> new StakeItem(SilverMaterial.SILVER, settings));
  public static final Item SILVER_SWORD =
      item(
          "silver_sword",
          settings ->
              new SwordItem(
                  SilverMaterial.SILVER,
                  settings.attributeModifiers(
                      SwordItem.createAttributeModifiers(SilverMaterial.SILVER, 3, -2.4F))));
  public static final Item SILVER_AXE =
      item(
          "silver_axe",
          settings ->
              new AxeItem(
                  SilverMaterial.SILVER,
                  settings.attributeModifiers(
                      AxeItem.createAttributeModifiers(SilverMaterial.SILVER, 6.0F, -3.1F))));

  public static final FoodComponent GARLIC_FOOD =
      new FoodComponent.Builder().nutrition(2).saturationModifier(0.3F).build();

  public static final Item GARLIC =
      item(
          "garlic",
          settings -> new AliasedBlockItem(GearBlocks.GARLIC, settings.food(GARLIC_FOOD)));
  public static final Item GOLD_CRUCIFIX =
      item(
          "gold_crucifix",
          settings -> new CrucifixItem(GearBlocks.GOLD_WALL_CRUCIFIX, settings.maxCount(1)));
  public static final Item SILVER_CRUCIFIX =
      item(
          "silver_crucifix",
          settings -> new CrucifixItem(GearBlocks.SILVER_WALL_CRUCIFIX, settings.maxCount(1)));

  private GearItems() {}

  public static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(
            entries -> {
              entries.addAfter(Items.IRON_SWORD, SILVER_SWORD);
              entries.addAfter(Items.IRON_AXE, SILVER_AXE);
              entries.addAfter(Items.NETHERITE_AXE, WOODEN_STAKE, SILVER_STAKE);
              entries.addAfter(SILVER_STAKE, GOLD_CRUCIFIX, SILVER_CRUCIFIX);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
        .register(entries -> entries.addAfter(Items.IRON_HOE, SILVER_AXE));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(
            entries -> {
              entries.addAfter(Items.RAW_GOLD, RAW_SILVER);
              entries.addAfter(Items.GOLD_INGOT, SILVER_INGOT);
              entries.addAfter(Items.GOLD_NUGGET, SILVER_NUGGET);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL)
        .register(
            entries -> {
              entries.addAfter(Items.GOLD_ORE, GearBlocks.SILVER_ORE);
              entries.addAfter(Items.DEEPSLATE_GOLD_ORE, GearBlocks.DEEPSLATE_SILVER_ORE);
              entries.addAfter(Items.BEETROOT_SEEDS, GARLIC);
            });
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
        .register(entries -> entries.add(GearBlocks.GARLIC_STRING));
    CompostingChanceRegistry.INSTANCE.add(GARLIC, COMPOST_CHANCE);
    CompostingChanceRegistry.INSTANCE.add(GearBlocks.GARLIC_STRING, COMPOST_CHANCE);
  }

  private static Item item(final String name, final Function<Item.Settings, Item> factory) {
    return Registry.register(
        Registries.ITEM, Identifier.of(Nightborn.MOD_ID, name), factory.apply(new Item.Settings()));
  }
}
