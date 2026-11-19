package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RevealingTest {

  private static final UUID REVEALED = new UUID(0L, 1L);
  private static final UUID MISSED = new UUID(0L, 2L);
  private static final Revealing REVEALING =
      new Revealing(
          Set.of(REVEALED),
          400L,
          new RevealPower(
              48.0F,
              16.0F,
              400,
              0,
              List.of(Identifier.of("nightborn", "occult/shadow_veil")),
              List.of(Veil.SHADOW_VEIL)));

  @ParameterizedTest(name = "now {0}: lasts {1}")
  @CsvSource({"0, true", "399, true", "400, false"})
  @DisplayName("a revelation lasts its time")
  void lasts(final long now, final boolean lasts) {
    assertEquals(lasts, REVEALING.lasts(now));
  }

  @ParameterizedTest(name = "revealed {0}, power {1} -> barred {2}")
  @CsvSource({
    "true, occult/shadow_veil, true",
    "true, occult/shadow_step, false",
    "false, occult/shadow_veil, false"
  })
  @DisplayName("a revealed vampire is barred from the listed powers only")
  void bars(final boolean revealed, final String power, final boolean barred) {
    assertEquals(
        barred, REVEALING.bars(revealed ? REVEALED : MISSED, Identifier.of("nightborn", power)));
  }

  @ParameterizedTest(name = "hunter {0}, human {1}, {2} blocks -> {3}")
  @CsvSource({
    "true, true, 0, true",
    "false, true, 16, true",
    "false, true, 16.5, false",
    "false, false, 2, false",
    "true, false, 100, true"
  })
  @DisplayName("the hunter and every human close to them see the marks")
  void shared(
      final boolean hunter, final boolean human, final double distance, final boolean shared) {
    assertEquals(shared, REVEALING.sharedWith(hunter, human, distance * distance));
  }
}
