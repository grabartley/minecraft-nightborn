package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FootstepsTest {

  @ParameterizedTest(name = "walked {0}, sprinted {1}, jumps {2}: {3}")
  @CsvSource(
      delimiter = ';',
      value = {
        "0; 0; 0; ",
        "120; 0; 0; WALK",
        "0; 300; 0; RUN",
        "50; 300; 0; RUN",
        "0; 0; 2; JUMP JUMP",
        "80; 0; 1; WALK JUMP",
        "0; 200; 1; RUN JUMP"
      })
  @DisplayName(
      "walking and running each count once a second, every jump counts, and sneaking steps"
          + " are never counted")
  void acts(final int walked, final int sprinted, final int jumps, final String expected) {
    final List<NoiseAct> acts =
        expected == null
            ? List.of()
            : Arrays.stream(expected.split(" ")).map(NoiseAct::valueOf).toList();
    assertEquals(acts, Footsteps.of(walked, sprinted, jumps));
  }
}
