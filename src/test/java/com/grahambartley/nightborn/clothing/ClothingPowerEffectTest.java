package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.ClothingSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.registry.Registries;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingPowerEffectTest {

  private static final ClothingSettings SETTINGS = NightbornConfig.DEFAULTS.clothing();
  private static final double EPSILON = 1.0E-6;

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ClothingPower.class,
      names = {"BATWING", "GLIDING_STEP", "HUSH", "SHROUD", "MISTCLOAK", "WARDING", "THRIFT"})
  @DisplayName("a power the server sets is read from data by its id alone")
  void readsServerSetPowers(final ClothingPower power) {
    final EnchantmentLocationBasedEffect effect = parse("{\"power\":\"" + power.asString() + "\"}");
    assertEquals(new ClothingPowerEffect(power, Optional.empty()), effect);
    assertEquals(
        ClothingPowerEffect.ID,
        Registries.ENCHANTMENT_LOCATION_BASED_EFFECT_TYPE.getId(effect.getCodec()));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ClothingPower.class,
      names = {"THIRST", "SUNMARKED", "BEACON"})
  @DisplayName("a curse's power needs its amount in data and is refused without one")
  void cursesNeedAnAmount(final ClothingPower power) {
    assertTrue(
        EnchantmentLocationBasedEffect.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"type\":\"nightborn:clothing_power\",\"power\":\""
                        + power.asString()
                        + "\"}"))
            .isError());
    final ClothingPowerEffect effect =
        (ClothingPowerEffect) parse("{\"power\":\"" + power.asString() + "\",\"amount\":0.25}");
    assertEquals(0.25, effect.amount(1, SETTINGS), EPSILON);
  }

  @ParameterizedTest(name = "{0} at level {1}: {2}")
  @CsvSource({
    "BATWING, 2, 2.0",
    "SHROUD, 3, -0.3",
    "MISTCLOAK, 2, 2.0",
    "WARDING, 1, 0.1",
    "THRIFT, 3, -0.15"
  })
  @DisplayName("with no amount in data, a power takes its strength from the server setting")
  void amountFromSettings(final ClothingPower power, final int level, final double amount) {
    assertEquals(
        amount, new ClothingPowerEffect(power, Optional.empty()).amount(level, SETTINGS), EPSILON);
  }

  @ParameterizedTest(name = "level {0}: {1}")
  @CsvSource({"1, 0.5", "2, 1.0"})
  @DisplayName("an amount in data wins over the setting and follows the level")
  void amountFromData(final int level, final double amount) {
    assertEquals(
        amount,
        new ClothingPowerEffect(
                ClothingPower.BATWING, Optional.of(EnchantmentLevelBasedValue.linear(0.5F, 0.5F)))
            .amount(level, SETTINGS),
        EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"chest, slot/chest", "feet, slot/feet"})
  @DisplayName("each armour slot is its own source of a power")
  void sourcePerSlot(final String slot, final String source) {
    assertEquals(source, ClothingPowerEffect.source(slot));
  }

  private static EnchantmentLocationBasedEffect parse(final String fields) {
    return EnchantmentLocationBasedEffect.CODEC
        .parse(
            JsonOps.INSTANCE,
            JsonParser.parseString("{\"type\":\"nightborn:clothing_power\"," + fields.substring(1)))
        .getOrThrow();
  }
}
