package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireHuntTest {

  @ParameterizedTest(
      name = "downed {0}, asleep {1}, prey {2}, spared {3}, drops pursuit {4}: hunts {5}")
  @CsvSource({
    "false, false, true, false, false, true",
    "true, false, true, false, false, false",
    "false, true, true, false, false, false",
    "false, false, false, false, false, false",
    "false, false, true, true, false, false",
    "false, false, true, false, true, false"
  })
  @DisplayName("a vampire hunts only standing, awake prey it is not sparing and would chase")
  void hunts(
      final boolean downed,
      final boolean sleeping,
      final boolean prey,
      final boolean spared,
      final boolean dropsPursuit,
      final boolean hunts) {
    assertEquals(
        hunts,
        VampireHunt.hunts(downed, () -> sleeping, () -> prey, () -> spared, () -> dropsPursuit));
  }

  @ParameterizedTest(name = "downed {0}, asleep {1}, prey {2}, spared {3}: checks {4}")
  @CsvSource({
    "true, false, true, false, ''",
    "false, true, true, false, sleeping",
    "false, false, false, false, sleeping prey",
    "false, false, true, true, sleeping prey spared",
    "false, false, true, false, sleeping prey spared pursuit"
  })
  @DisplayName("each check runs only once every check before it has passed")
  void huntsStopsAtTheFirstFailedCheck(
      final boolean downed,
      final boolean sleeping,
      final boolean prey,
      final boolean spared,
      final String checks) {
    final List<String> asked = new ArrayList<>();
    VampireHunt.hunts(
        downed,
        asking(asked, "sleeping", sleeping),
        asking(asked, "prey", prey),
        asking(asked, "spared", spared),
        asking(asked, "pursuit", false));
    assertEquals(checks, String.join(" ", asked));
  }

  @ParameterizedTest(name = "downed {0}, drops pursuit {1}: lets go {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vampire lets its target go when downed or when the pursuit is over")
  void letsGo(final boolean downed, final boolean dropsPursuit, final boolean letsGo) {
    assertEquals(letsGo, VampireHunt.letsGo(downed, () -> dropsPursuit));
  }

  @ParameterizedTest(name = "provoked {0}, attacks on sight {1}: pressed {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vampire presses a target that provoked it or that it attacks on sight")
  void pressed(final boolean provoked, final boolean onSight, final boolean pressed) {
    assertEquals(pressed, VampireHunt.pressed(provoked, onSight));
  }

  @ParameterizedTest(name = "downed {0}: weighs the pursuit {1}")
  @CsvSource({"true, false", "false, true"})
  @DisplayName("a downed vampire lets go without weighing the pursuit")
  void letsGoWeighsThePursuitOnlyWhenStanding(final boolean downed, final boolean weighed) {
    final List<String> asked = new ArrayList<>();
    VampireHunt.letsGo(downed, asking(asked, "pursuit", false));
    assertEquals(weighed, asked.contains("pursuit"));
  }

  static BooleanSupplier asking(final List<String> asked, final String name, final boolean answer) {
    return () -> {
      asked.add(name);
      return answer;
    };
  }
}
