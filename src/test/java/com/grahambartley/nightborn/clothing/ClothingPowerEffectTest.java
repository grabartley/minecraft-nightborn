package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingPowerEffectTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPower.class)
  @DisplayName("an enchantment file names a clothing power by its id")
  void readsFromEnchantmentData(final ClothingPower power) {
    final EnchantmentLocationBasedEffect effect =
        EnchantmentLocationBasedEffect.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"type\":\"nightborn:clothing_power\",\"power\":\""
                        + power.asString()
                        + "\"}"))
            .getOrThrow();
    assertEquals(new ClothingPowerEffect(power), effect);
    assertEquals(
        ClothingPowerEffect.ID,
        Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE.getId(effect.getCodec()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(ClothingPower.class)
  @DisplayName("each slot keeps its own modifier, so two pieces never overwrite each other")
  void modifierPerSlot(final ClothingPower power) {
    final ClothingPowerEffect effect = new ClothingPowerEffect(power);
    assertNotEquals(effect.modifierId(EquipmentSlot.CHEST), effect.modifierId(EquipmentSlot.FEET));
    assertEquals(
        "nightborn:clothing/" + power.asString() + "/feet",
        effect.modifierId(EquipmentSlot.FEET).toString());
  }
}
