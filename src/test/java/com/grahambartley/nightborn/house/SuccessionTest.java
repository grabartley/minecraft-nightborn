package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class SuccessionTest {

  static final UUID FIRST = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  static final UUID SECOND = UUID.fromString("00000000-0000-0000-0000-00000000000b");
  static final UUID THIRD = UUID.fromString("00000000-0000-0000-0000-00000000000c");
  static final UUID NPC_OLD = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
  static final UUID NPC_YOUNG = UUID.fromString("00000000-0000-0000-0000-0000000000a2");
  static final UUID NPC_LATE = UUID.fromString("00000000-0000-0000-0000-0000000000a3");

  static House players(final int first, final int second, final int third) {
    return House.founded(LORD, at(0, 64, 0))
        .withPlayer(FIRST)
        .withPlayer(SECOND)
        .withPlayer(THIRD)
        .ranked(FIRST, first)
        .ranked(SECOND, second)
        .ranked(THIRD, third);
  }

  static Stream<Arguments> heirs() {
    return Stream.of(
        Arguments.of(players(1, 2, 3), Optional.of(THIRD)),
        Arguments.of(players(2, 2, 1), Optional.of(FIRST)),
        Arguments.of(players(0, 1, 1), Optional.of(SECOND)),
        Arguments.of(players(0, 0, 0), Optional.empty()),
        Arguments.of(House.founded(LORD, at(0, 64, 0)), Optional.empty()));
  }

  @ParameterizedTest(name = "{index}: heir {1}")
  @MethodSource("heirs")
  @DisplayName("the oldest adult member inherits, the longest in the house if two are of an age")
  void heir(final House house, final Optional<UUID> heir) {
    assertEquals(heir, Succession.heir(house));
  }

  static House npcs() {
    return House.founded(LORD, at(0, 64, 0))
        .withPlayer(FIRST)
        .withNpc(NPC_OLD)
        .withNpc(NPC_YOUNG)
        .withNpc(NPC_LATE)
        .ranked(NPC_OLD, 2)
        .ranked(NPC_YOUNG, 0)
        .ranked(NPC_LATE, 0);
  }

  static Stream<Arguments> overflows() {
    return Stream.of(
        Arguments.of(4, List.of()),
        Arguments.of(9, List.of()),
        Arguments.of(3, List.of(NPC_LATE)),
        Arguments.of(2, List.of(NPC_LATE, NPC_YOUNG)),
        Arguments.of(0, List.of(NPC_LATE, NPC_YOUNG, NPC_OLD)),
        Arguments.of(-2, List.of(NPC_LATE, NPC_YOUNG, NPC_OLD)));
  }

  @ParameterizedTest(name = "room for {0} -> wild {1}")
  @MethodSource("overflows")
  @DisplayName("members beyond the heir's room go wild, youngest first, players never")
  void overflow(final int capacity, final List<UUID> wild) {
    assertEquals(wild, Succession.overflow(npcs(), capacity));
    final House kept = Succession.trimmed(npcs(), capacity);
    assertEquals(List.of(FIRST), kept.players());
    wild.forEach(gone -> assertEquals(false, kept.hasMember(gone)));
  }
}
