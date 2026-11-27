package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationRulesTest {

  @ParameterizedTest(name = "vampire {0}, weakened {1}, downed {2}, occupied {3}: {4}")
  @CsvSource({
    "true, 0, false, false, true",
    "false, 0, false, false, false",
    "true, 1, false, false, false",
    "true, 100, false, false, false",
    "true, 0, true, false, false",
    "true, 0, false, true, false"
  })
  @DisplayName("only a free vampire that is neither weakened nor downed can levitate")
  void mayLevitate(
      final boolean vampire,
      final int weakened,
      final boolean downed,
      final boolean occupied,
      final boolean may) {
    assertEquals(may, LevitationRules.mayLevitate(vampire, weakened, downed, occupied));
  }

  @ParameterizedTest(name = "last {0}, now {1}: {2}")
  @CsvSource({
    ", 100, true",
    "100, 100, false",
    "100, 109, false",
    "100, 110, true",
    "100, 50, true"
  })
  @DisplayName("the toggle waits half a second between presses")
  void toggleReady(final Long last, final long now, final boolean ready) {
    assertEquals(ready, LevitationRules.toggleReady(last, now));
  }

  @ParameterizedTest(name = "on ground {0}, levitating {1}: {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName("a levitating vampire starts a sprint as it would standing on the ground")
  void movesAsGrounded(final boolean onGround, final boolean levitating, final boolean grounded) {
    assertEquals(grounded, LevitationRules.movesAsGrounded(onGround, levitating));
  }

  @ParameterizedTest(name = "levitating {0}, player {1}, sneaking {2}: {3}")
  @CsvSource({
    "true, true, true, true",
    "true, true, false, false",
    "true, false, true, false",
    "false, true, true, false",
    "false, false, false, false"
  })
  @DisplayName("only a levitating player holding sneak sinks fast, never a mob")
  void sinksFast(
      final boolean levitating,
      final boolean player,
      final boolean sneaking,
      final boolean sinksFast) {
    assertEquals(sinksFast, LevitationRules.sinksFast(levitating, player, sneaking));
  }

  @ParameterizedTest(name = "sinks fast {0}, on ground {1}: {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a fast sink that reaches the ground lands the vampire")
  void landsOnSneak(final boolean sinksFast, final boolean onGround, final boolean lands) {
    assertEquals(lands, LevitationRules.landsOnSneak(sinksFast, onGround));
  }

  @ParameterizedTest(name = "levitating {0}, room to stand {1}: {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a levitating vampire holds the float pose wherever it has room to stand")
  void holdsFloatPose(final boolean levitating, final boolean roomToStand, final boolean holds) {
    assertEquals(holds, LevitationRules.holdsFloatPose(levitating, roomToStand));
  }

  @ParameterizedTest(name = "crouching {0}, holds float pose {1}: {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("sneak never crouches a vampire that holds the float pose")
  void crouches(final boolean crouching, final boolean holdsFloatPose, final boolean crouches) {
    assertEquals(crouches, LevitationRules.crouches(crouching, holdsFloatPose));
  }

  @ParameterizedTest(name = "levitating {0}, on ground {1}, feet {2}, surface {3}: cushioned {4}")
  @CsvSource({
    "true, false, 65.0, 64.0, false",
    "true, false, 65.75, 64.0, false",
    "true, false, 65.8, 64.0, true",
    "true, false, 70.0, 60.0, true",
    "true, false, 65.0, , true",
    "true, true, 70.0, 60.0, false",
    "false, false, 70.0, 60.0, false"
  })
  @DisplayName("a vampire sinking to pass a low gap only floats down when the drop is long")
  void cushions(
      final boolean levitating,
      final boolean onGround,
      final double feet,
      final Double surface,
      final boolean cushions) {
    assertEquals(
        cushions,
        LevitationRules.cushions(
            levitating,
            onGround,
            feet,
            surface == null ? OptionalDouble.empty() : OptionalDouble.of(surface)));
  }
}
