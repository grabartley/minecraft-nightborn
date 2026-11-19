package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WingedCuesTest {

  @ParameterizedTest(name = "{0}: then since {1} at {2}, hurt {3}, swinging {4} -> {5}")
  @CsvSource({
    "fresh form, 100, 100, 0, false, ROAR",
    "form seen late, 100, 200, 0, false, ",
    "same form again, 0, 10, 0, false, ",
    "hit, 0, 10, 10, false, HURT",
    "nothing new, 0, 10, 0, false, ",
    "swing, 0, 10, 0, true, CLAW",
    "roar over a swing, 5, 10, 0, true, ROAR",
    "hit while swinging, 0, 10, 10, true, HURT"
  })
  @DisplayName("a winged form roars once as it begins, flinches when hit and claws when it swings")
  void observe(
      final String name,
      final long since,
      final long now,
      final int hurtTime,
      final boolean swinging,
      final WingedStrike expected) {
    final WingedCues cues = new WingedCues();
    cues.observe(0L, 0L, 0, false);
    assertEquals(Optional.ofNullable(expected), cues.observe(since, now, hurtTime, swinging));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"held swing, 0, true", "fading flinch, 8, false"})
  @DisplayName("a held swing or a fading flinch starts nothing new")
  void heldCuesStartNothing(final String name, final int hurtTime, final boolean swinging) {
    final WingedCues cues = new WingedCues();
    cues.observe(0L, 0L, 10, true);
    assertEquals(Optional.empty(), cues.observe(0L, 1L, hurtTime, swinging));
  }
}
