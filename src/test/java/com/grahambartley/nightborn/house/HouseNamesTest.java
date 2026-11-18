package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static com.grahambartley.nightborn.house.HouseFixtures.STRANGER;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class HouseNamesTest {

  static House named(final String name) {
    return House.founded(LORD, at(0, 64, 0))
        .withPlayer(PLAYER)
        .withNpc(NPC)
        .withIdentity(new HouseIdentity(Optional.of(name), Optional.empty()));
  }

  static Stream<Arguments> namings() {
    return Stream.of(
        Arguments.of(
            List.of(named("Valcourt")),
            Map.of(LORD, "Valcourt", PLAYER, "Valcourt", NPC, "Valcourt")),
        Arguments.of(List.of(House.founded(LORD, at(0, 64, 0)).withPlayer(PLAYER)), Map.of()));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("namings")
  @DisplayName("every member of a named house, lord and NPCs too, bears its name")
  void names(final List<House> houses, final Map<UUID, String> names) {
    assertEquals(names, HouseNames.names(houses));
  }

  static Stream<Arguments> changes() {
    return Stream.of(
        Arguments.of(Map.of(LORD, "A"), Map.of(LORD, "A"), Set.of()),
        Arguments.of(Map.of(LORD, "A"), Map.of(LORD, "B"), Set.of(LORD)),
        Arguments.of(Map.of(LORD, "A", PLAYER, "A"), Map.of(LORD, "A"), Set.of(PLAYER)),
        Arguments.of(Map.of(), Map.of(STRANGER, "C"), Set.of(STRANGER)));
  }

  @ParameterizedTest(name = "{index}: {2}")
  @MethodSource("changes")
  @DisplayName("only members whose house name changed are told again")
  void changed(
      final Map<UUID, String> before, final Map<UUID, String> after, final Set<UUID> changed) {
    assertEquals(changed, HouseNames.changed(before, after));
  }
}
