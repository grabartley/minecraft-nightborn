package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PartyMemberTest {

  @ParameterizedTest(name = "in a party {0}, leader near {1}: stays {2}")
  @CsvSource({"false, false, true", "false, true, true", "true, true, true", "true, false, false"})
  void aHunterWhoseLeaderIsGoneMayLeave(
      final boolean inParty, final boolean leaderNear, final boolean stays) {
    assertEquals(stays, PartyMember.staysInWorld(inParty, leaderNear));
  }

  @ParameterizedTest(name = "sleeping {0} at {1}: wakes {2}")
  @CsvSource({"true, 18000, false", "true, 23500, true", "true, 1000, true", "false, 1000, false"})
  void aSleeperWakesAtDawnOnItsOwn(final boolean sleeping, final long time, final boolean wakes) {
    assertEquals(wakes, PartyMember.wakes(sleeping, time));
  }

  @ParameterizedTest(name = "leader {0}: follows {1}")
  @CsvSource({"master, true", "other, false", "none, false"})
  void aHunterWhoLeftThePartyIsNoLongerItsMember(final String leader, final boolean follows) {
    final UUID master = new UUID(0L, 1L);
    final Optional<UUID> held =
        switch (leader) {
          case "master" -> Optional.of(master);
          case "other" -> Optional.of(new UUID(0L, 2L));
          default -> Optional.empty();
        };
    assertEquals(follows, PartyMember.follows(held, master));
  }

  @ParameterizedTest(name = "quarries {0}/{1}, leaders {2}/{3}: fellows {4}")
  @CsvSource({
    "q, q, '', '', true",
    "q, r, '', '', false",
    "'', '', '', '', false",
    "'', '', m, m, true",
    "'', '', m, n, false",
    "'', '', '', one, true",
    "'', '', other, '', true",
    "'', q, '', '', false"
  })
  @DisplayName(
      "two hunters are fellows when they raid the same quarry, follow the same leader, or one"
          + " leads the other")
  void fellows(
      final String oneQuarry,
      final String otherQuarry,
      final String oneLeader,
      final String otherLeader,
      final boolean fellows) {
    assertEquals(
        fellows,
        PartyMember.fellows(
            id("one"),
            maybe(oneQuarry),
            maybe(oneLeader),
            id("other"),
            maybe(otherQuarry),
            maybe(otherLeader)));
  }

  private static UUID id(final String name) {
    return UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  private static Optional<UUID> maybe(final String name) {
    return name.isEmpty() ? Optional.empty() : Optional.of(id(name));
  }
}
