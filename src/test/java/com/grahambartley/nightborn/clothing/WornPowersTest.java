package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class WornPowersTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPower.class)
  @DisplayName("each power has one modifier however many pieces carry it")
  void oneModifierPerPower(final ClothingPower power) {
    assertEquals("nightborn:clothing/" + power.asString(), WornPowers.modifierId(power).toString());
  }

  @ParameterizedTest(name = "{0} on {1}")
  @CsvSource({"BATWING, CHEST", "GLIDING_STEP, LEGS", "HUSH, FEET"})
  @DisplayName("the per-slot modifiers older worlds saved are the ones cleared")
  void legacyIds(final ClothingPower power, final EquipmentSlot slot) {
    assertEquals(
        "nightborn:clothing/" + power.asString() + "/" + slot.getName(),
        WornPowers.legacyModifierId(power, slot).toString());
  }
}
