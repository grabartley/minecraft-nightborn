package com.grahambartley.nightborn.ammo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CrossbowAmmoTest {

  static Stream<Arguments> stacks() {
    return Stream.of(
        Arguments.of(AmmoItems.STAKE_BOLT, true),
        Arguments.of(AmmoItems.SILVER_BOLT, true),
        Arguments.of(AmmoItems.HOLY_WATER_BOLT, true),
        Arguments.of(AmmoItems.GARLIC_BOMB, true),
        Arguments.of(Items.ARROW, false),
        Arguments.of(Items.FIREWORK_ROCKET, false),
        Arguments.of(Items.SPLASH_POTION, false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("stacks")
  @DisplayName("only hunter ammo is loaded by the crossbow through this path")
  void is(final Item item, final boolean ammo) {
    assertEquals(ammo, CrossbowAmmo.is(new ItemStack(item)));
  }
}
