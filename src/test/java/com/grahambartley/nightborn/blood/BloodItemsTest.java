package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.item.Items;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BloodItemsTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(BloodQuality.class)
  void craftingWithABloodBottleHandsBackItsGlassBottle(final BloodQuality quality) {
    assertEquals(Items.GLASS_BOTTLE, BloodBottleItem.of(quality).getRecipeRemainder().getItem());
    assertEquals(16, BloodBottleItem.of(quality).getMaxCount());
  }

  @ParameterizedTest(name = "uses {0}")
  @ValueSource(ints = {64})
  void aLancetIsASingleToolThatWears(final int uses) {
    assertEquals(1, BloodItems.LANCET.getDefaultStack().getMaxCount());
    assertEquals(uses, BloodItems.LANCET.getDefaultStack().getMaxDamage());
  }
}
