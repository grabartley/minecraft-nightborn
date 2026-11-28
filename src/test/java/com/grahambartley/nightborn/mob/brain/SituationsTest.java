package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SituationsTest {

  @ParameterizedTest(name = "[{0}]: reads signs {1}")
  @CsvSource({
    "'', false",
    "TARGET_DISTANCE OWN_HEALTH SUN_ON_ME SPOT_DISTANCE, false",
    "TARGET_RUNNING, true",
    "TARGET_DISTANCE TARGET_HUNTER, true",
    "TARGET_WINDING_UP TARGET_SLOWED TARGET_AIRBORNE TARGET_VAMPIRE, true",
    "TARGET_AWARE, true",
    "TARGET_THREATENING, true",
    "GAP_AHEAD LIGHT UNTIL_DAWN, false"
  })
  @DisplayName("a mob reads its target's signs only when a ready power asks for one")
  void readsSigns(final String facts, final boolean reads) {
    final Set<Fact> needed =
        facts.isEmpty()
            ? EnumSet.noneOf(Fact.class)
            : Arrays.stream(facts.split(" ")).map(Fact::valueOf).collect(Collectors.toSet());
    assertEquals(reads, Situations.readsSigns(needed));
  }

  @ParameterizedTest(name = "[{0}]: reads the group {1}")
  @CsvSource({
    "'', false",
    "TARGET_DISTANCE OWN_HEALTH, false",
    "ALLY_HEALTH, true",
    "FOES_IN_RADIUS TARGET_DISTANCE, true",
    "ALLIES_IN_RADIUS, true"
  })
  @DisplayName("a mob looks around at its group only when a ready power asks about it")
  void readsGroup(final String facts, final boolean reads) {
    final Set<Fact> needed =
        facts.isEmpty()
            ? EnumSet.noneOf(Fact.class)
            : Arrays.stream(facts.split(" ")).map(Fact::valueOf).collect(Collectors.toSet());
    assertEquals(reads, Situations.readsGroup(needed));
  }

  @ParameterizedTest(name = "[{0}]: reads tactics {1}")
  @CsvSource({
    "'', false",
    "TARGET_DISTANCE TARGET_SLOWED, false",
    "TARGET_HEALING, true",
    "TARGET_RANGED OWN_HEALTH, true",
    "OWN_HELD, true",
    "TARGET_HELD, true"
  })
  @DisplayName("a mob studies its foe and itself only when a ready power asks a tactical question")
  void readsTactics(final String facts, final boolean reads) {
    final Set<Fact> needed =
        facts.isEmpty()
            ? EnumSet.noneOf(Fact.class)
            : Arrays.stream(facts.split(" ")).map(Fact::valueOf).collect(Collectors.toSet());
    assertEquals(reads, Situations.readsTactics(needed));
  }
}
