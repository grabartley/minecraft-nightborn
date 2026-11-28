package com.grahambartley.nightborn.balance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.balance.Roster.Kind;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RosterTest {

  static Stream<Arguments> rosters() {
    return Stream.of(
        Arguments.of(Roster.of(Kind.VAMPIRE, "nightborn:adult"), "nightborn:adult", true),
        Arguments.of(Roster.of(Kind.VAMPIRE), "mixed", true),
        Arguments.of(Roster.of(Kind.VAMPIRE_LORD), "vampire_lord", true),
        Arguments.of(Roster.of(Kind.HUNTER, "nightborn:master"), "nightborn:master", false),
        Arguments.of(Roster.of(Kind.HUNTER), "mixed", false),
        Arguments.of(Roster.of(Kind.VETERAN_HUNTER), "veteran_hunter", false),
        Arguments.of(Roster.of(Kind.GUILD_MASTER), "guild_master", false));
  }

  @ParameterizedTest(name = "{0} is labelled {1}, a vampire {2}")
  @MethodSource("rosters")
  void label(final Roster roster, final String label, final boolean vampire) {
    assertEquals(label, roster.label());
    assertEquals(vampire, roster.vampire());
  }
}
