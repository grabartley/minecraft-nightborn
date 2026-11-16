package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FollowRulesTest {

  @ParameterizedTest(name = "{0}, owner here {1}, {2} blocks -> starts {3}, keeps {4}")
  @CsvSource({
    "FOLLOW, true, 10.0, true, true",
    "FOLLOW, true, 5.0, false, true",
    "FOLLOW, true, 2.0, false, false",
    "FOLLOW, false, 10.0, false, false",
    "STAY, true, 10.0, false, false",
    "GUARD, true, 10.0, false, false"
  })
  @DisplayName("a following thrall closes in on its owner and waits when the owner is gone")
  void follows(
      final ThrallMode mode,
      final boolean here,
      final double distance,
      final boolean starts,
      final boolean keeps) {
    assertEquals(starts, FollowRules.follows(mode, here, distance));
    assertEquals(keeps, FollowRules.keepsFollowing(mode, here, distance));
  }

  @ParameterizedTest(name = "{0} blocks -> {1}")
  @CsvSource({"10.0, false", "24.0, false", "24.5, true", "100.0, true"})
  @DisplayName("a thrall left far behind catches up at once")
  void teleports(final double distance, final boolean expected) {
    assertEquals(expected, FollowRules.teleports(distance));
  }

  @ParameterizedTest(name = "{0}, {1} from post -> {2}")
  @CsvSource({
    "FOLLOW, 50.0, false",
    "STAY, 0.5, false",
    "STAY, 1.5, true",
    "GUARD, 8.0, false",
    "GUARD, 9.0, true"
  })
  @DisplayName("a staying thrall keeps to its spot and a guard to its ground")
  void returnsToPost(final ThrallMode mode, final double fromPost, final boolean expected) {
    assertEquals(expected, FollowRules.returnsToPost(mode, fromPost));
  }

  @ParameterizedTest(name = "{0}, target {1} from post -> {2}")
  @CsvSource({
    "GUARD, 4.0, true",
    "GUARD, 8.0, true",
    "GUARD, 8.5, false",
    "STAY, 1.0, false",
    "FOLLOW, 1.0, false"
  })
  @DisplayName("only a guard goes after monsters near its post")
  void guards(final ThrallMode mode, final double fromPost, final boolean expected) {
    assertEquals(expected, FollowRules.guards(mode, fromPost));
  }
}
