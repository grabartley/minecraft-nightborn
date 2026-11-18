package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.ClothingSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ClothingPowerTest {

  private static final double EPSILON = 1.0E-9;

  @ParameterizedTest(name = "{0} {1} with settings {2}/{3}/{4}: +{5}")
  @CsvSource({
    "BATWING, 1, 1, 4, 10, 1.0",
    "BATWING, 3, 1, 4, 10, 3.0",
    "BATWING, 3, 2, 4, 10, 6.0",
    "BATWING, 2, 0, 4, 10, 0.0",
    "GLIDING_STEP, 1, 1, 4, 10, 0.4",
    "GLIDING_STEP, 1, 1, 10, 10, 1.0",
    "HUSH, 1, 1, 4, 10, 0.1",
    "HUSH, 3, 1, 4, 10, 0.3",
    "HUSH, 2, 1, 4, 25, 0.5"
  })
  @DisplayName("each power's strength follows its level and the server's setting")
  void amount(
      final ClothingPower power,
      final int level,
      final int batwingBlocks,
      final int glidingStepTenths,
      final int hushSpeed,
      final double amount) {
    assertEquals(
        amount,
        power
            .amount(
                level,
                new ClothingSettings(
                    true, batwingBlocks, glidingStepTenths, hushSpeed, 10, 1, 10, 5))
            .orElseThrow(),
        EPSILON);
  }

  @ParameterizedTest(name = "{0} level {1} with {2}% / {3} blocks: {4}")
  @CsvSource({
    "SHROUD, 1, 10, 1, -0.1",
    "SHROUD, 3, 10, 1, -0.3",
    "MISTCLOAK, 2, 10, 1, 2.0",
    "MISTCLOAK, 1, 10, 3, 3.0",
    "WARDING, 3, 10, 1, 0.3",
    "WARDING, 2, 20, 1, 0.4",
    "THRIFT, 1, 5, 1, -0.05",
    "THRIFT, 3, 5, 1, -0.15"
  })
  @DisplayName("the newer powers scale by level and their server setting")
  void newerAmounts(
      final ClothingPower power,
      final int level,
      final int percent,
      final int blocks,
      final double amount) {
    final ClothingSettings settings =
        new ClothingSettings(true, 1, 4, 10, percent, blocks, percent, percent);
    assertEquals(amount, power.amount(level, settings).orElseThrow(), EPSILON);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = ClothingPower.class,
      names = {"THIRST", "SUNMARKED", "BEACON"})
  @DisplayName("a curse's strength is not a setting, so it comes from data")
  void cursesHaveNoSetting(final ClothingPower power) {
    assertTrue(power.amount(1, new ClothingSettings(true, 1, 4, 10, 10, 1, 10, 5)).isEmpty());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "BATWING, batwing, minecraft:generic.safe_fall_distance",
    "GLIDING_STEP, gliding_step, minecraft:generic.step_height",
    "HUSH, hush, minecraft:player.sneaking_speed",
    "SHROUD, shroud, nightborn:notice_distance",
    "MISTCLOAK, mistcloak, nightborn:vanish_reach",
    "WARDING, warding, nightborn:ward_resistance",
    "THRIFT, thrift, nightborn:blood_drain",
    "THIRST, thirst, nightborn:blood_drain",
    "SUNMARKED, sunmarked, nightborn:sun_damage",
    "BEACON, beacon, nightborn:hunter_sight"
  })
  @DisplayName("each power is named in data by its id and raises its attribute")
  void namesAndAttributes(final ClothingPower power, final String id, final String attribute) {
    assertEquals(id, power.asString());
    assertEquals(attribute, power.attribute().getKey().orElseThrow().getValue().toString());
  }
}
