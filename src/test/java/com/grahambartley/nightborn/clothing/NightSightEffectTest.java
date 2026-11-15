package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class NightSightEffectTest {

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{\"type\":\"nightborn:night_sight\"}"})
  @DisplayName("an enchantment file names the night sight effect by its id")
  void readsFromEnchantmentData(final String json) {
    final EnchantmentEntityEffect effect =
        EnchantmentEntityEffect.CODEC
            .parse(JsonOps.INSTANCE, com.google.gson.JsonParser.parseString(json))
            .getOrThrow();
    assertSame(NightSightEffect.INSTANCE, effect);
    assertEquals(
        NightSightEffect.ID, Registries.ENCHANTMENT_ENTITY_EFFECT_TYPE.getId(effect.getCodec()));
  }
}
