package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerSourcesTest {

  private static final double EPSILON = 1.0E-9;

  @ParameterizedTest(name = "chest {0} and back {1}: {2}")
  @CsvSource({
    "1.0, 3.0, 3.0",
    "3.0, 1.0, 3.0",
    "2.0, 2.0, 2.0",
    "-0.1, -0.3, -0.3",
    "0.0, 0.0, 0.0"
  })
  @DisplayName("the same power worn twice counts once, at the strongest piece")
  void strongestWins(final double chest, final double back, final double strongest) {
    final PowerSources sources =
        PowerSources.NONE
            .with(ClothingPower.BATWING, "slot/chest", chest)
            .with(ClothingPower.BATWING, "accessory/chest/back/0", back);
    assertEquals(strongest, sources.strongest(ClothingPower.BATWING), EPSILON);
  }

  @Test
  @DisplayName("taking one piece off leaves the other's power in place")
  void takingOneOff() {
    final PowerSources sources =
        PowerSources.NONE
            .with(ClothingPower.BATWING, "slot/chest", 1.0)
            .with(ClothingPower.BATWING, "accessory/chest/back/0", 3.0)
            .with(ClothingPower.THRIFT, "accessory/chest/back/0", -0.05);
    final PowerSources off = sources.without("accessory/chest/back/0");
    assertEquals(1.0, off.strongest(ClothingPower.BATWING), EPSILON);
    assertEquals(0.0, off.strongest(ClothingPower.THRIFT), EPSILON);
  }

  @Test
  @DisplayName("one power can be taken from a piece without touching its others")
  void takingOnePower() {
    final PowerSources sources =
        PowerSources.NONE
            .with(ClothingPower.BATWING, "slot/chest", 2.0)
            .with(ClothingPower.THIRST, "slot/chest", 0.25)
            .without(ClothingPower.BATWING, "slot/chest");
    assertEquals(0.0, sources.strongest(ClothingPower.BATWING), EPSILON);
    assertEquals(0.25, sources.strongest(ClothingPower.THIRST), EPSILON);
  }

  @Test
  @DisplayName("wearing a piece again replaces its amount rather than adding it")
  void replaces() {
    final PowerSources sources =
        PowerSources.NONE
            .with(ClothingPower.HUSH, "slot/feet", 0.3)
            .with(ClothingPower.HUSH, "slot/feet", 0.1);
    assertEquals(0.1, sources.strongest(ClothingPower.HUSH), EPSILON);
  }
}
