package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FightBackTest {

  static Stream<Arguments> vampires() {
    return Stream.of(
        Arguments.of("a wild vampire struck by a vampire", true, false, false, true),
        Arguments.of("a watcher struck by a vampire", true, false, false, true),
        Arguments.of("a sated vampire by day struck by a vampire", true, false, false, true),
        Arguments.of("a sleeper woken by a vampire's blow", true, false, false, true),
        Arguments.of("a household member struck by a vampire", true, false, false, true),
        Arguments.of("an elder struck by a fledgling", true, false, false, true),
        Arguments.of("a vampire lord struck by a vampire", true, false, false, true),
        Arguments.of("a downed vampire struck by a vampire", true, true, false, false),
        Arguments.of("a vampire escaping when downed", true, false, true, false),
        Arguments.of("a vampire fleeing to shade", true, false, true, false),
        Arguments.of("a vampire nobody struck", false, false, false, false));
  }

  @ParameterizedTest(name = "{0}: fights back {4}")
  @MethodSource("vampires")
  @DisplayName("a vampire answers a vampire's blow unless it is downed or fleeing")
  void vampireAnswers(
      final String who,
      final boolean struckBy,
      final boolean helpless,
      final boolean fleeing,
      final boolean fights) {
    assertEquals(fights, FightBack.vampireAnswers(struckBy, helpless, fleeing));
  }

  static Stream<Arguments> guild() {
    return Stream.of(
        Arguments.of("a hunter struck by a human", true, false, true),
        Arguments.of("a veteran struck by a zombie", true, false, true),
        Arguments.of("a trading Guild Master struck by a human", true, false, true),
        Arguments.of("a Guild Master struck mid-oath", true, false, true),
        Arguments.of("a hunter struck by another hunter", true, true, false),
        Arguments.of("a hunter struck by its Guild Master", true, true, false),
        Arguments.of("a hunter nobody struck", false, false, false));
  }

  @ParameterizedTest(name = "{0}: fights back {3}")
  @MethodSource("guild")
  @DisplayName("a hunter or Guild Master answers any blow but the guild's own")
  void guildAnswers(
      final String who, final boolean struckBy, final boolean byGuild, final boolean fights) {
    assertEquals(fights, FightBack.guildAnswers(struckBy, byGuild));
  }
}
