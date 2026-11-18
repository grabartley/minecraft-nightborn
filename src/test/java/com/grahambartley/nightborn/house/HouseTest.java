package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static com.grahambartley.nightborn.house.HouseFixtures.STRANGER;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.house.crest.Crest;
import com.grahambartley.nightborn.house.crest.CrestCharge;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.GlobalPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HouseTest {

  static Stream<Arguments> houses() {
    return Stream.of(
        Arguments.of(House.founded(LORD, at(1, 64, 1))),
        Arguments.of(
            House.founded(LORD, at(1, 64, 1))
                .withPlayer(PLAYER)
                .withNpc(NPC)
                .seatedAt(Optional.of(at(500, 70, -20)))
                .storeToggled(at(3, 64, 3))),
        Arguments.of(
            House.founded(LORD, at(1, 64, 1))
                .withPlayer(PLAYER)
                .ranked(PLAYER, 2)
                .withIdentity(
                    new HouseIdentity(
                        Optional.of("Valcourt"),
                        Optional.of(
                            new Crest(CrestCharge.RAVEN, DyeColor.PURPLE, DyeColor.YELLOW))))
                .withChallenges(
                    HouseChallenges.NONE.lost(NPC, 12L).declared(Challenge.declared(PLAYER, 4L)))),
        Arguments.of(
            new House(
                LORD,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                HouseIdentity.NONE,
                HouseChallenges.NONE,
                Map.of())));
  }

  @ParameterizedTest(name = "{index}")
  @MethodSource("houses")
  @DisplayName("a house survives a save and load whole")
  void roundTrip(final House house) {
    final House loaded =
        House.CODEC
            .parse(JsonOps.INSTANCE, House.CODEC.encodeStart(JsonOps.INSTANCE, house).getOrThrow())
            .getOrThrow();
    assertEquals(house, loaded);
  }

  static Stream<Arguments> members() {
    final House house = House.founded(LORD, at(0, 64, 0)).withPlayer(PLAYER).withNpc(NPC);
    return Stream.of(
        Arguments.of(house, LORD, false, true),
        Arguments.of(house, PLAYER, true, true),
        Arguments.of(house, NPC, true, true),
        Arguments.of(house, STRANGER, false, false),
        Arguments.of(house.without(PLAYER), PLAYER, false, false),
        Arguments.of(house.without(NPC), NPC, false, false));
  }

  @ParameterizedTest(name = "{1}: member {2}, belongs {3}")
  @MethodSource("members")
  @DisplayName("the lord belongs to the house, and members come and go")
  void belongs(final House house, final UUID who, final boolean member, final boolean belongs) {
    assertEquals(member, house.hasMember(who));
    assertEquals(belongs, house.belongs(who));
  }

  static Stream<Arguments> sizes() {
    final House house = House.founded(LORD, at(0, 64, 0));
    return Stream.of(
        Arguments.of(house, 0),
        Arguments.of(house.withPlayer(PLAYER), 1),
        Arguments.of(house.withPlayer(PLAYER).withPlayer(PLAYER), 1),
        Arguments.of(house.withPlayer(PLAYER).withNpc(NPC), 2),
        Arguments.of(house.withNpc(NPC).withNpc(NPC).without(NPC), 0));
  }

  @ParameterizedTest(name = "{1} members")
  @MethodSource("sizes")
  @DisplayName("players and NPC members count together, once each, and never the lord")
  void size(final House house, final int size) {
    assertEquals(size, house.size());
    assertEquals(size + 1, house.everyone().count());
    assertEquals(size + 1, house.household());
  }

  static Stream<Arguments> places() {
    final GlobalPos anchor = at(1, 64, 1);
    final GlobalPos coffin = at(9, 64, 9);
    final GlobalPos seat = at(400, 70, 400);
    final House founded = House.founded(LORD, anchor);
    return Stream.of(
        Arguments.of(founded, Optional.of(coffin), Optional.of(coffin)),
        Arguments.of(founded, Optional.empty(), Optional.of(anchor)),
        Arguments.of(founded.seatedAt(Optional.of(seat)), Optional.of(coffin), Optional.of(seat)),
        Arguments.of(
            new House(
                LORD,
                Optional.empty(),
                Optional.empty(),
                List.of(),
                List.of(),
                Optional.empty(),
                HouseIdentity.NONE,
                HouseChallenges.NONE,
                Map.of()),
            Optional.empty(),
            Optional.empty()));
  }

  @ParameterizedTest(name = "{index}: -> {2}")
  @MethodSource("places")
  @DisplayName("a house stands in its structure, else follows its lord's coffin, else waits")
  void place(final House house, final Optional<GlobalPos> coffin, final Optional<GlobalPos> place) {
    assertEquals(place, house.place(coffin));
  }

  static Stream<Arguments> stores() {
    final GlobalPos chest = at(3, 64, 3);
    final GlobalPos chalice = at(4, 64, 3);
    final House house = House.founded(LORD, at(0, 64, 0));
    return Stream.of(
        Arguments.of(house, chest, Optional.of(chest)),
        Arguments.of(house.storeToggled(chest), chest, Optional.empty()),
        Arguments.of(house.storeToggled(chest), chalice, Optional.of(chalice)));
  }

  @ParameterizedTest(name = "{index}: -> {2}")
  @MethodSource("stores")
  @DisplayName("marking the store again clears it, and marking another moves it")
  void store(final House house, final GlobalPos marked, final Optional<GlobalPos> store) {
    assertEquals(store, house.storeToggled(marked).store());
  }

  static Stream<Arguments> handovers() {
    final House house =
        House.founded(LORD, at(0, 64, 0))
            .withPlayer(PLAYER)
            .withPlayer(STRANGER)
            .withNpc(NPC)
            .ranked(LORD, 3)
            .ranked(PLAYER, 2)
            .withChallenges(HouseChallenges.NONE.declared(Challenge.declared(PLAYER, 5L)));
    return Stream.of(
        Arguments.of(house, true, List.of(LORD, STRANGER), true),
        Arguments.of(house, false, List.of(STRANGER), false));
  }

  @ParameterizedTest(name = "former lord stays {1}")
  @MethodSource("handovers")
  @DisplayName("passing the house makes the heir lord and settles any challenge")
  void ledBy(
      final House house,
      final boolean stays,
      final List<UUID> players,
      final boolean formerRankKept) {
    final House passed = house.ledBy(PLAYER, stays);
    assertEquals(PLAYER, passed.lord());
    assertEquals(players, passed.players());
    assertEquals(List.of(NPC), passed.npcs());
    assertEquals(Optional.empty(), passed.challenges().pending());
    assertEquals(formerRankKept, passed.ranks().containsKey(LORD));
  }

  @ParameterizedTest(name = "{0} -> rank {1}")
  @CsvSource({"2, 2", "0, 0"})
  @DisplayName("a member's last known stage is kept until they leave")
  void ranked(final int rank, final int known) {
    final House house = House.founded(LORD, at(0, 64, 0)).withPlayer(PLAYER).ranked(PLAYER, rank);
    assertEquals(known, house.rank(PLAYER));
    assertEquals(0, house.without(PLAYER).rank(PLAYER));
    assertEquals(0, house.rank(STRANGER));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("leavers")
  @DisplayName("a member who leaves takes their challenge and their bar with them")
  void leaverForgotten(final UUID leaver, final boolean pendingKept) {
    final House house =
        House.founded(LORD, at(0, 64, 0))
            .withPlayer(PLAYER)
            .withPlayer(STRANGER)
            .withChallenges(
                HouseChallenges.NONE.lost(STRANGER, 20L).declared(Challenge.declared(PLAYER, 3L)));
    final House left = house.without(leaver);
    assertEquals(pendingKept, left.challenges().pending().isPresent());
    assertEquals(leaver.equals(STRANGER), !left.challenges().barred(STRANGER, 10L));
  }

  static Stream<Arguments> leavers() {
    return Stream.of(Arguments.of(PLAYER, false), Arguments.of(STRANGER, true));
  }
}
