package com.grahambartley.nightborn.hold;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HoldRulesTest {

  @ParameterizedTest(name = "held until {0}, immune until {1}, now {2}, boss {3}: takes {4}")
  @CsvSource({
    "0, 0, 100, false, true",
    "150, 210, 100, false, false",
    "90, 150, 100, false, false",
    "90, 150, 150, false, true",
    "0, 0, 100, true, false"
  })
  @DisplayName(
      "a target cannot be held twice at once, nor again during the grace after, and never a boss")
  void takes(
      final long until,
      final long immuneUntil,
      final long now,
      final boolean boss,
      final boolean takes) {
    final HoldState state =
        new HoldState(
            until > now ? Optional.of(HoldKind.PIN) : Optional.empty(), until, immuneUntil);
    assertEquals(takes, HoldRules.takes(state, now, boss, true, false));
  }

  @ParameterizedTest(name = "now {0}, {1} ticks, grace {2}: until {3}, immune until {4}")
  @CsvSource({"100, 40, 60, 140, 200", "100, 0, 60, 101, 161", "0, 20, 0, 20, 20"})
  @DisplayName("a hold lasts its time and the grace starts when it ends")
  void starts(
      final long now, final int ticks, final int grace, final long until, final long immune) {
    final HoldState state = HoldRules.start(HoldKind.STAGGER, now, ticks, grace);
    assertEquals(until, state.until());
    assertEquals(immune, state.immuneUntil());
    assertEquals(Optional.of(HoldKind.STAGGER), state.kind());
  }

  @ParameterizedTest(name = "until {0}, now {1}: ended {2}")
  @CsvSource({"140, 139, false", "140, 140, true", "140, 200, true"})
  @DisplayName("a hold has ended once its time is up")
  void ends(final long until, final long now, final boolean ended) {
    assertEquals(
        ended, HoldRules.ended(new HoldState(Optional.of(HoldKind.PIN), until, until), now));
  }

  @ParameterizedTest(name = "dead {0}: takes {1}")
  @CsvSource({"true, false", "false, true"})
  @DisplayName("the dead cannot be held")
  void dead(final boolean dead, final boolean takes) {
    assertEquals(takes, HoldRules.takes(HoldState.NONE, 0L, false, !dead, false));
  }
}
