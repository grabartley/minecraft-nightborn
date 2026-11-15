package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;

public final class Consumables {

  public static final TagKey<Item> VAMPIRE_CONSUMABLES =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "vampire_consumables"));

  private Consumables() {}

  public static boolean rejectedBy(final ItemStack stack, final boolean vampire) {
    return vampire && swallowed(stack) && !stack.isIn(VAMPIRE_CONSUMABLES);
  }

  static boolean swallowed(final ItemStack stack) {
    final UseAction action = stack.getUseAction();
    return action == UseAction.EAT || action == UseAction.DRINK;
  }

  public static ItemStack leftBehind(final ItemStack stack) {
    final FoodComponent food = stack.get(DataComponentTypes.FOOD);
    if (food != null && food.usingConvertsTo().isPresent()) {
      return food.usingConvertsTo().get().copy();
    }
    final Item remainder = stack.getItem().getRecipeRemainder();
    if (remainder != null) {
      return new ItemStack(remainder);
    }
    if (stack.contains(DataComponentTypes.POTION_CONTENTS)) {
      return new ItemStack(Items.GLASS_BOTTLE);
    }
    return ItemStack.EMPTY;
  }
}
