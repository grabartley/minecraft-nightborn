package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FightBackTest {

  static Stream<Arguments> struck() {
    return Stream.of(
        Arguments.of("a wild vampire hit by a player", true, false, false, false, false, true),
        Arguments.of("a watcher hit by a player", true, false, false, false, false, true),
        Arguments.of(
            "a sated vampire by day hit by a hunter", true, false, false, false, true, true),
        Arguments.of("a sleeper woken by a zombie's blow", true, false, false, false, false, true),
        Arguments.of("a household member hit by a hunter", true, false, false, false, true, true),
        Arguments.of("an elder hit by a fledgling vampire", true, false, false, false, false, true),
        Arguments.of("a vampire lord hit by a vampire", true, false, false, false, false, true),
        Arguments.of("the Eldest hit by a hunter", true, false, false, false, true, true),
        Arguments.of("a downed vampire hit by a hunter", true, true, false, false, true, false),
        Arguments.of("a vampire escaping when downed", true, false, true, false, true, false),
        Arguments.of("a vampire fleeing to shade", true, false, true, false, false, false),
        Arguments.of("a hunter hit by a player", true, false, false, true, false, true),
        Arguments.of("a hunter hit by a vampire", true, false, false, true, false, true),
        Arguments.of("a veteran hit by a zombie", true, false, false, true, false, true),
        Arguments.of(
            "a trading Guild Master hit by a player", true, false, false, true, false, true),
        Arguments.of("a Guild Master hit mid-oath", true, false, false, true, false, true),
        Arguments.of("a hunter hit by another hunter", true, false, false, true, true, false),
        Arguments.of("a hunter hit by its Guild Master", true, false, false, true, true, false),
        Arguments.of("a vampire nobody struck", false, false, false, false, false, false),
        Arguments.of("a hunter nobody struck", false, false, false, true, false, false));
  }

  @ParameterizedTest(name = "{0}: fights back {6}")
  @MethodSource("struck")
  @DisplayName(
      "anything struck fights back unless downed, fleeing, or a guild fighter struck by the guild")
  void answers(
      final String who,
      final boolean struckBy,
      final boolean helpless,
      final boolean fleeing,
      final boolean guild,
      final boolean byGuild,
      final boolean fights) {
    assertEquals(fights, FightBack.answers(struckBy, helpless, fleeing, guild, byGuild));
  }
}
