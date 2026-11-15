package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HeavesTest {

  static final List<Double> TIMES = List.of(0.0, 0.25, 0.5, 0.7, 1.0, 1.25, 1.6, 1.85, 2.25, 2.6);
  static final List<Double> BODY =
      List.of(0.0, -18.0, -44.0, -36.0, -50.0, -38.0, -47.0, -40.0, -14.0, 0.0);

  @ParameterizedTest(name = "deeper than {0}: {1}")
  @CsvSource({"30, '0.5,1.0,1.6'", "45, '1.0,1.6'", "49, '1.0'", "60, '0.0'"})
  @DisplayName("each deep bend of the body is a heave, and a clip with none still retches once")
  void peaks(final double degrees, final String expected) {
    assertEquals(
        List.of(expected.split(",")).stream().map(Double::parseDouble).toList(),
        Heaves.peaks(TIMES, BODY, degrees));
  }

  @ParameterizedTest(name = "from {0} to {1}: {2} heaves")
  @CsvSource({
    "NaN, 0.0, 0",
    "NaN, 0.5, 1",
    "NaN, 2.0, 3",
    "0.5, 0.9, 0",
    "0.49, 0.5, 1",
    "1.0, 2.6, 1",
    "0.0, NaN, 0"
  })
  @DisplayName("each heave fires once, on the frame the clip passes it")
  void crossed(final double before, final double now, final int count) {
    assertEquals(count, Heaves.crossed(List.of(0.5, 1.0, 1.6), before, now).size());
  }
}
