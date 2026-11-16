package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireHissTest {

  @ParameterizedTest(name = "client {0}, alive {1}, downed {2}, rising {3}: hisses {4}")
  @CsvSource({
    "false, true, false, false, true",
    "true, true, false, false, false",
    "false, false, false, false, false",
    "false, true, true, false, false",
    "false, true, false, true, false"
  })
  @DisplayName("only a living, standing vampire on the server hisses")
  void mayHiss(
      final boolean client,
      final boolean alive,
      final boolean downed,
      final boolean rising,
      final boolean hisses) {
    assertEquals(hisses, VampireHiss.mayHiss(client, () -> alive, () -> downed, () -> rising));
  }

  @ParameterizedTest(name = "client {0}, alive {1}, downed {2}: checks {3}")
  @CsvSource({
    "true, true, false, ''",
    "false, false, false, alive",
    "false, true, true, alive downed",
    "false, true, false, alive downed rising"
  })
  @DisplayName("each check runs only once every check before it has passed")
  void mayHissStopsAtTheFirstFailedCheck(
      final boolean client, final boolean alive, final boolean downed, final String checks) {
    final List<String> asked = new ArrayList<>();
    VampireHiss.mayHiss(
        client,
        VampireHuntTest.asking(asked, "alive", alive),
        VampireHuntTest.asking(asked, "downed", downed),
        VampireHuntTest.asking(asked, "rising", false));
    assertEquals(checks, String.join(" ", asked));
  }

  @ParameterizedTest(name = "warded now {0}, before {1}: hisses {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a vampire hisses the moment garlic or a crucifix first wards it")
  void newlyWarded(final boolean now, final boolean before, final boolean hisses) {
    assertEquals(hisses, VampireHiss.newlyWarded(now, before));
  }
}
