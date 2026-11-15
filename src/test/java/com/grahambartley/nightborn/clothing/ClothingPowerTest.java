package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.config.ClothingSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
        power.amount(
            level, new ClothingSettings(true, batwingBlocks, glidingStepTenths, hushSpeed)),
        EPSILON);
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "BATWING, batwing, generic.safe_fall_distance",
    "GLIDING_STEP, gliding_step, generic.step_height",
    "HUSH, hush, player.sneaking_speed"
  })
  @DisplayName("each power is named in data by its id and raises its attribute")
  void namesAndAttributes(final ClothingPower power, final String id, final String attribute) {
    assertEquals(id, power.asString());
    assertEquals(
        "minecraft:" + attribute, power.attribute().getKey().orElseThrow().getValue().toString());
  }
}
