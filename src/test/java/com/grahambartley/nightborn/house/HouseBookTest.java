package com.grahambartley.nightborn.house;

import static com.grahambartley.nightborn.house.HouseFixtures.LORD;
import static com.grahambartley.nightborn.house.HouseFixtures.NPC;
import static com.grahambartley.nightborn.house.HouseFixtures.PLAYER;
import static com.grahambartley.nightborn.house.HouseFixtures.STRANGER;
import static com.grahambartley.nightborn.house.HouseFixtures.at;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HouseBookTest {

  private static final GlobalPos SEAT = at(100, 70, 100);

  private static HouseBook book() {
    final HouseBook book = new HouseBook();
    book.put(
        House.founded(LORD, at(0, 64, 0))
            .withPlayer(PLAYER)
            .withNpc(NPC)
            .seatedAt(Optional.of(SEAT)));
    return book;
  }

  @ParameterizedTest(name = "{0}: leads {1}, belongs {2}")
  @CsvSource({
    "00000000-0000-0000-0000-000000000001, true, true",
    "00000000-0000-0000-0000-000000000002, false, true",
    "00000000-0000-0000-0000-000000000003, false, true",
    "00000000-0000-0000-0000-000000000004, false, false"
  })
  @DisplayName("a vampire's house is found whether they lead it or belong to it")
  void lookUp(final UUID who, final boolean leads, final boolean belongs) {
    assertEquals(leads, book().ledBy(who).isPresent());
    assertEquals(belongs, book().of(who).isPresent());
  }

  @ParameterizedTest(name = "seat at {0}, {1}, {2} -> held {3}")
  @CsvSource({"100, 70, 100, true", "100, 70, 101, false", "0, 64, 0, false"})
  @DisplayName("a structure is held only by the house seated in it")
  void holding(final int x, final int y, final int z, final boolean held) {
    assertEquals(held, book().holding(at(x, y, z)).isPresent());
  }

  @ParameterizedTest(name = "{0}, {1}, {2} within {3} -> {4}")
  @CsvSource({
    "100, 70, 100, 1, true",
    "130, 70, 100, 64, true",
    "300, 70, 100, 64, false",
    "164, 70, 100, 64, false",
    "163, 70, 100, 64, true"
  })
  @DisplayName("a place near a held structure is served by its house")
  void seatNear(final int x, final int y, final int z, final int reach, final boolean near) {
    assertEquals(near, book().seatNear(at(x, y, z), reach));
    assertEquals(false, book().seatNear(at(World.NETHER, x, y, z), reach));
  }

  @ParameterizedTest(name = "{index}")
  @CsvSource({"00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000003"})
  @DisplayName("a lord's house is replaced whole when it changes")
  void put(final UUID member) {
    final HouseBook book = book();
    book.put(book.ledBy(LORD).orElseThrow().without(member));
    assertEquals(false, book.of(member).isPresent());
    assertEquals(1, book.all().size());
    assertEquals(false, book.of(STRANGER).isPresent());
  }

  @ParameterizedTest(name = "remove {0}")
  @CsvSource({"00000000-0000-0000-0000-000000000001, 0", "00000000-0000-0000-0000-000000000004, 1"})
  @DisplayName("removing a house by its lord frees its members, and a stranger removes nothing")
  void remove(final UUID lord, final int left) {
    final HouseBook book = book();
    book.remove(lord);
    assertEquals(left, book.all().size());
    assertEquals(left == 1, book.of(PLAYER).isPresent());
  }
}
