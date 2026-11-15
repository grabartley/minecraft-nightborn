package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodValueTest {

  @ParameterizedTest(name = "{0} taken at {1}% kept {2}% = {3}")
  @CsvSource({
    "2, 50, 100, 1.0",
    "2, 100, 100, 2.0",
    "2, 150, 100, 3.0",
    "4, 50, 60, 1.2",
    "4, 150, 60, 3.6",
    "0, 150, 100, 0.0",
    "-3, 150, 100, 0.0",
    "3, 0, 100, 0.0"
  })
  void worth(final float taken, final int quality, final int kept, final float expected) {
    assertEquals(expected, BloodValue.worth(taken, quality, kept), 0.0001F);
  }

  @ParameterizedTest(name = "{0} = {1} + {2}")
  @CsvSource({"0.0, 0, 0.0", "0.75, 0, 0.75", "1.0, 1, 0.0", "3.6, 3, 0.6", "-1.0, 0, 0.0"})
  void splitsWholeBloodFromTheRemainder(
      final float pending, final int whole, final float remainder) {
    final BloodValue.Split split = BloodValue.split(pending);
    assertEquals(whole, split.whole());
    assertEquals(remainder, split.remainder(), 0.0001F);
  }
}
