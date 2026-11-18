package com.grahambartley.nightborn.nemesis;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class NemesisItems {

  static final String NAMED = ".named";

  public static final Item LOCK_OF_HAIR =
      register("lock_of_hair", new Item(new Item.Settings().maxCount(1)));
  public static final Item BROKEN_STAKE =
      register("broken_stake", new Item(new Item.Settings().maxCount(1)));

  private NemesisItems() {}

  static void register() {
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(
            entries -> {
              entries.add(LOCK_OF_HAIR);
              entries.add(BROKEN_STAKE);
            });
  }

  public static ItemStack keepsake(final Identifier item, final Text bearer) {
    final ItemStack stack = new ItemStack(Registries.ITEM.get(item));
    if (!stack.isEmpty()) {
      stack.set(
          DataComponentTypes.ITEM_NAME,
          Text.translatable(stack.getItem().getTranslationKey() + NAMED, bearer));
    }
    return stack;
  }

  private static Item register(final String path, final Item item) {
    return Registry.register(Registries.ITEM, Identifier.of(Nightborn.MOD_ID, path), item);
  }
}
