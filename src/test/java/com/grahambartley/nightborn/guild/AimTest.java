package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AimTest {

  @ParameterizedTest(name = "aim {0} from {1}, {2} by {3} away: rise {4}")
  @CsvSource({
    "65.0, 65.0, 0.0, 0.0, 0.0",
    "65.0, 65.0, 10.0, 0.0, 2.0",
    "65.0, 65.0, 3.0, 4.0, 1.0",
    "65.0, 65.0, -3.0, -4.0, 1.0",
    "68.0, 65.0, 10.0, 0.0, 5.0",
    "62.0, 65.0, 0.0, 5.0, -2.0"
  })
  void aimsHigherTheFurtherTheTarget(
      final double aimY, final double fromY, final double dx, final double dz, final double rise) {
    assertEquals(rise, Aim.rise(aimY, fromY, dx, dz), 1.0e-9);
  }
}
