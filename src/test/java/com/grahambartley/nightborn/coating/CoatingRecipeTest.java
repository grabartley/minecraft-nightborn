package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.holy.HolyWater;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoatingRecipeTest {

  static Stream<Arguments> agents() {
    return Stream.of(
        Arguments.of(HolyWater.bottle(), Optional.of(Coating.HOLY_WATER), Items.GLASS_BOTTLE),
        Arguments.of(new ItemStack(GearItems.GARLIC), Optional.of(Coating.GARLIC), Items.AIR),
        Arguments.of(new ItemStack(Items.POTION), Optional.empty(), Items.AIR),
        Arguments.of(new ItemStack(Items.BREAD), Optional.empty(), Items.AIR));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("agents")
  @DisplayName("holy water and garlic coat, and a holy water bottle comes back empty")
  void agents(final ItemStack stack, final Optional<Coating> coating, final Item left) {
    assertEquals(coating, CoatingRecipe.agent(stack));
    final CraftingRecipeInput input = CraftingRecipeInput.create(1, 1, List.of(stack));
    assertEquals(
        left,
        new CoatingRecipe(CraftingRecipeCategory.EQUIPMENT).getRemainder(input).get(0).getItem());
  }
}
