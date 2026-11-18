package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.gear.GearItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornRefillTest {

  @ParameterizedTest(name = "holds {0} and {1}, used {2}: same kind {3}")
  @CsvSource({
    "SPLASH_HOLY, BOLT, SPLASH_HOLY, true",
    "LINGERING_HOLY, BOLT, SPLASH_HOLY, false",
    "BOLT, LINGERING_HOLY, LINGERING_HOLY, true",
    "BOLT, BOLT, SPLASH_HOLY, false"
  })
  @DisplayName(
      "the hand is refilled with the same kind when the belt has it, else what the pocket holds")
  void like(final String first, final String second, final String used, final boolean same) {
    final List<ItemStack> held =
        List.of(AccessoryHoldTest.stack(first), AccessoryHoldTest.stack(second));
    assertEquals(same, WornRefill.like(held, AccessoryHoldTest.stack(used)));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"MAINHAND, MAIN_HAND", "OFFHAND, OFF_HAND", "HEAD, ", "FEET, "})
  @DisplayName("only a tool broken in a hand is replaced from the belt")
  void hand(final EquipmentSlot slot, final Hand hand) {
    assertEquals(Optional.ofNullable(hand), WornRefill.hand(slot));
  }

  @ParameterizedTest(name = "held damage {0}, wooden {1}: same {2}")
  @CsvSource({"0, true, true", "30, true, true", "0, false, false"})
  @DisplayName("a worn stake still counts as the same kind as the stake that broke")
  void sameStake(final int damage, final boolean wooden, final boolean same) {
    final ItemStack held = new ItemStack(wooden ? GearItems.WOODEN_STAKE : GearItems.SILVER_STAKE);
    held.setDamage(damage);
    assertEquals(same, WornRefill.same(held, new ItemStack(GearItems.WOODEN_STAKE)));
  }
}
