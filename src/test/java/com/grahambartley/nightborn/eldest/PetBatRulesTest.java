package com.grahambartley.nightborn.eldest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PetBatRulesTest {

  @ParameterizedTest(name = "age {0}")
  @ValueSource(ints = {0, 1, 17, 200, 100_000})
  @DisplayName("the bat circles just above its owner's head, never further than its orbit")
  void perch(final int age) {
    final Vec3d eyes = new Vec3d(4.0, 70.0, -9.0);
    final Vec3d perch = PetBatRules.perch(eyes, age);
    assertEquals(PetBatRules.ABOVE, perch.y - eyes.y, 1.0E-9);
    assertEquals(PetBatRules.ORBIT, Math.hypot(perch.x - eyes.x, perch.z - eyes.z), 1.0E-9);
    assertTrue(perch.distanceTo(PetBatRules.perch(eyes, age + 1)) < 0.2);
  }

  @ParameterizedTest(name = "{0} blocks squared away: lost {1}")
  @CsvSource({"0, false", "575.9, false", "576, false", "576.1, true", "10000, true"})
  @DisplayName("a bat left more than twenty four blocks behind goes straight to its owner")
  void lost(final double squared, final boolean lost) {
    assertEquals(lost, PetBatRules.lost(squared));
  }

  @ParameterizedTest(name = "owner {0}, sneaking {1}: comes home {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("only its owner, sneaking, can pick the bat back up")
  void comesHome(final boolean owner, final boolean sneaking, final boolean home) {
    assertEquals(home, PetBatRules.comesHome(owner, sneaking));
  }
}
