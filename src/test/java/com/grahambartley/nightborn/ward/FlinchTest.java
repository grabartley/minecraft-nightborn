package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FlinchTest {

  @ParameterizedTest(name = "was in reach {0}, in reach {1} -> {2}")
  @CsvSource({
    "false, true, true",
    "true, true, false",
    "true, false, false",
    "false, false, false"
  })
  @DisplayName("a vampire recoils once, on first coming into reach")
  void starts(final boolean was, final boolean now, final boolean flinches) {
    assertEquals(flinches, Flinch.starts(was, now));
  }

  @ParameterizedTest(name = "strengths {0} -> {1}")
  @CsvSource({"'', false", "0, false", "1, true", "0 0, false", "0 30, true", "12 0, true"})
  @DisplayName("a ward reaches a vampire when any of its sources has strength")
  void inReach(final String strengths, final boolean reached) {
    assertEquals(
        reached,
        Flinch.inReach(
            strengths.isBlank()
                ? new int[0]
                : Arrays.stream(strengths.split(" ")).mapToInt(Integer::parseInt).toArray()));
  }

  @ParameterizedTest(name = "garlic {0}, crucifix {1}, small {2} -> {3}")
  @CsvSource({
    "false, false, false, ",
    "false, false, true, ",
    "true, false, false, RECOIL_GARLIC",
    "false, true, false, RECOIL_CRUCIFIX",
    "true, true, false, RECOIL_CRUCIFIX",
    "true, false, true, SMALL_FLINCH",
    "false, true, true, SMALL_FLINCH"
  })
  @DisplayName(
      "garlic and a crucifix each have their own recoil, the crucifix wins when both reach at once,"
          + " and a smaller flinch replaces either")
  void recoil(
      final boolean garlic, final boolean crucifix, final boolean small, final VampirePose pose) {
    assertEquals(Optional.ofNullable(pose), Flinch.recoil(garlic, crucifix, small));
  }

  @ParameterizedTest(name = "small flinch {0}: small {1}")
  @CsvSource({"0.0, false", "1.0, true", "0.5, true"})
  @DisplayName("a vampire whose flinch is made smaller plays the small flinch")
  void small(final double smallFlinch, final boolean small) {
    assertEquals(small, Flinch.small(smallFlinch));
  }
}
