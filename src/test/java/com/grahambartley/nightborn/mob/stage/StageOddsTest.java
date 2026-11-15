package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StageOddsTest {

  private static final Identifier ADULT = Identifier.of("nightborn", "adult");
  private static final Identifier ANCIENT = Identifier.of("nightborn", "ancient");
  private static final Identifier FLEDGLING = Identifier.of("nightborn", "fledgling");

  private static final Map<Identifier, VampireStage> STAGES =
      Map.of(
          ADULT, StageFixtures.stage(1, 30),
          ANCIENT, StageFixtures.stage(3, 10),
          FLEDGLING, StageFixtures.stage(0, 60));

  static Stream<Arguments> picks() {
    return Stream.of(
        Arguments.of(Map.of(), 0.0, ADULT),
        Arguments.of(Map.of(), 0.29, ADULT),
        Arguments.of(Map.of(), 0.31, ANCIENT),
        Arguments.of(Map.of(), 0.41, FLEDGLING),
        Arguments.of(Map.of(), 0.999, FLEDGLING),
        Arguments.of(Map.of(ANCIENT, 1), 0.0, ANCIENT),
        Arguments.of(Map.of(ANCIENT, 1), 0.999, ANCIENT),
        Arguments.of(Map.of(ADULT, 1, FLEDGLING, 1), 0.49, ADULT),
        Arguments.of(Map.of(ADULT, 1, FLEDGLING, 1), 0.51, FLEDGLING));
  }

  @ParameterizedTest(name = "odds {0}, roll {1}: {2}")
  @MethodSource("picks")
  @DisplayName("a structure's own odds replace the stages' weights")
  void picks(final Map<Identifier, Integer> odds, final double roll, final Identifier stage) {
    assertEquals(Optional.of(stage), StageOdds.pick(STAGES, odds, roll));
  }

  @Test
  @DisplayName("odds that name no known stage pick nothing")
  void unknownOnly() {
    assertEquals(
        Optional.empty(),
        StageOdds.pick(STAGES, Map.of(Identifier.of("nightborn", "lord"), 5), 0.5));
  }
}
