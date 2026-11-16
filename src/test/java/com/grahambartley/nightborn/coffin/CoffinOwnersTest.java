package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CoffinOwnersTest {

  private static final UUID ANNA = new UUID(0, 1);
  private static final UUID BRAM = new UUID(0, 2);
  private static final Map<UUID, String> HELD = Map.of(ANNA, "crypt", BRAM, "lair");

  static Stream<Arguments> assignments() {
    return Stream.of(
        Arguments.of(
            "a free coffin goes to its sleeper",
            Map.of(),
            ANNA,
            "crypt",
            true,
            Map.of(ANNA, "crypt")),
        Arguments.of(
            "a new coffin frees the old one",
            HELD,
            ANNA,
            "manor",
            true,
            Map.of(ANNA, "manor", BRAM, "lair")),
        Arguments.of(
            "another's coffin is never taken",
            HELD,
            ANNA,
            "lair",
            false,
            Map.of(ANNA, "crypt", BRAM, "lair")),
        Arguments.of(
            "a vampire's own coffin changes nothing",
            HELD,
            ANNA,
            "crypt",
            false,
            Map.of(ANNA, "crypt", BRAM, "lair")),
        Arguments.of(
            "a second vampire takes another free coffin",
            HELD,
            BRAM,
            "manor",
            true,
            Map.of(ANNA, "crypt", BRAM, "manor")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("assignments")
  @DisplayName("a vampire holds at most one coffin and a coffin has at most one owner")
  void assignsOneCoffinPerVampire(
      final String name,
      final Map<UUID, String> before,
      final UUID owner,
      final String coffin,
      final boolean changed,
      final Map<UUID, String> after) {
    final CoffinOwners<String> owners = new CoffinOwners<>();
    before.forEach(owners::assign);
    assertEquals(changed, owners.assign(owner, coffin));
    assertEquals(after, owners.entries());
    after.forEach(
        (who, where) -> {
          assertEquals(Optional.of(where), owners.coffinOf(who));
          assertEquals(Optional.of(who), owners.ownerOf(where));
        });
  }

  @Test
  @DisplayName("releasing by owner or by coffin frees both sides, and twice frees nothing more")
  void releasesFromEitherSide() {
    final CoffinOwners<String> owners = new CoffinOwners<>();
    owners.assign(ANNA, "crypt");
    owners.assign(BRAM, "lair");
    assertEquals(Optional.of("crypt"), owners.release(ANNA));
    assertEquals(Optional.empty(), owners.release(ANNA));
    assertEquals(Optional.of(BRAM), owners.releaseCoffin("lair"));
    assertEquals(Optional.empty(), owners.releaseCoffin("lair"));
    assertEquals(Optional.empty(), owners.ownerOf("crypt"));
    assertEquals(Optional.empty(), owners.coffinOf(BRAM));
    assertEquals(Map.of(), owners.entries());
    assertEquals(true, owners.assign(BRAM, "crypt"));
  }
}
