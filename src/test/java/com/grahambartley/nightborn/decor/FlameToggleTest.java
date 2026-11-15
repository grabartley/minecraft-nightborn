package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FlameToggleTest {

  static Stream<Arguments> cases() {
    return Stream.of(
        Arguments.of(false, new ItemStack(Items.FLINT_AND_STEEL), FlameToggle.LIGHT_WITH_FLINT),
        Arguments.of(false, new ItemStack(Items.FIRE_CHARGE), FlameToggle.LIGHT_WITH_CHARGE),
        Arguments.of(false, ItemStack.EMPTY, FlameToggle.PASS),
        Arguments.of(false, new ItemStack(Items.TORCH), FlameToggle.PASS),
        Arguments.of(true, ItemStack.EMPTY, FlameToggle.DOUSE),
        Arguments.of(true, new ItemStack(Items.FLINT_AND_STEEL), FlameToggle.PASS),
        Arguments.of(true, new ItemStack(Items.WATER_BUCKET), FlameToggle.PASS));
  }

  @ParameterizedTest(name = "lit={0} holding {1} -> {2}")
  @MethodSource("cases")
  void decidesFromFlameStateAndHeldItem(
      final boolean lit, final ItemStack held, final FlameToggle expected) {
    assertEquals(expected, FlameToggle.decide(lit, held));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(FlameToggle.class)
  void onlyTheIgnitersLight(final FlameToggle toggle) {
    assertEquals(
        toggle == FlameToggle.LIGHT_WITH_FLINT || toggle == FlameToggle.LIGHT_WITH_CHARGE,
        toggle.lights());
  }
}
