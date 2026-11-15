package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.holy.HolyWater;
import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class CoatingRecipe extends SpecialCraftingRecipe {

  public CoatingRecipe(final CraftingRecipeCategory category) {
    super(category);
  }

  @Override
  public boolean matches(final CraftingRecipeInput input, final World world) {
    return mix(input).isPresent();
  }

  @Override
  public ItemStack craft(
      final CraftingRecipeInput input, final RegistryWrapper.WrapperLookup lookup) {
    return mix(input)
        .map(
            mix -> {
              final ItemStack coated =
                  input.getStackInSlot(mix.target()).copyWithCount(mix.count());
              coated.set(Coatings.COAT, Coat.fresh(mix.coating()));
              return coated;
            })
        .orElse(ItemStack.EMPTY);
  }

  @Override
  public DefaultedList<ItemStack> getRemainder(final CraftingRecipeInput input) {
    final DefaultedList<ItemStack> remainder = super.getRemainder(input);
    for (int slot = 0; slot < input.getSize(); slot++) {
      if (agent(input.getStackInSlot(slot)).orElse(null) == Coating.HOLY_WATER) {
        remainder.set(slot, new ItemStack(Items.GLASS_BOTTLE));
      }
    }
    return remainder;
  }

  @Override
  public boolean fits(final int width, final int height) {
    return width * height >= 2;
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return Coatings.RECIPE;
  }

  static Optional<Coating> agent(final ItemStack stack) {
    if (stack.isOf(Items.POTION) && HolyWater.isHolyWater(stack)) {
      return Optional.of(Coating.HOLY_WATER);
    }
    return stack.isOf(GearItems.GARLIC) ? Optional.of(Coating.GARLIC) : Optional.empty();
  }

  static CoatingMix.Role role(final ItemStack stack) {
    if (stack.isEmpty()) {
      return new CoatingMix.Role.Empty();
    }
    final Optional<Coating> agent = agent(stack);
    if (agent.isPresent()) {
      return new CoatingMix.Role.Agent(agent.get());
    }
    if (stack.isIn(Coatings.WEAPONS)) {
      return new CoatingMix.Role.Weapon();
    }
    return stack.isIn(Coatings.AMMO) ? new CoatingMix.Role.Ammo() : new CoatingMix.Role.Other();
  }

  private static Optional<CoatingMix> mix(final CraftingRecipeInput input) {
    return CoatingMix.of(
        input.getStacks(), CoatingRecipe::role, ItemStack::areItemsAndComponentsEqual);
  }
}
