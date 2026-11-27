package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ManorTest {

  static Stream<Manor.Layout> layouts() {
    return Manor.LAYOUTS.stream();
  }

  static Pos start() {
    return new Pos(Manor.MID_X, 1, Manor.SIZE_Z - 1);
  }

  static Set<Pos> walk(final Template t) {
    return StructureWalk.from(t, start());
  }

  static List<Pos> find(final Template t, final String block) {
    final List<Pos> found = new ArrayList<>();
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      if (cell.getValue().replaceAll("\\[.*", "").endsWith(block)) {
        found.add(cell.getKey());
      }
    }
    return found;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void fillsEveryCellOfItsBox(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    assertEquals(Manor.SIZE_X * Manor.SIZE_Y * Manor.SIZE_Z, t.cells().size());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void sleepsAFullHouseholdInDarkOakAndSpruce(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    final long darkOak = find(t, "dark_oak_coffin").size() / 2;
    final long spruce = find(t, "spruce_coffin").size() / 2;
    assertEquals(2, darkOak);
    assertEquals(3, spruce);
    assertEquals(List.of(), find(t, "stone_brick_coffin"));
  }

  @ParameterizedTest(name = "{0}: every door, coffin and chest can be walked to from the gate")
  @MethodSource("layouts")
  void everyRoomIsReachedFromTheGate(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    final Set<Pos> reached = walk(t);
    for (final Pos door : find(t, "dark_oak_door")) {
      if (t.get(door.x(), door.y(), door.z()).contains("half=lower")) {
        assertTrue(reached.contains(door), "door " + door);
      }
    }
    for (final String thing : List.of("_coffin", ":chest", ":lectern")) {
      for (final Pos at : find(t, thing)) {
        assertTrue(StructureWalk.beside(reached, at.x(), at.y(), at.z()), thing + " " + at);
      }
    }
  }

  static Stream<Arguments> places() {
    return layouts()
        .flatMap(
            layout ->
                Stream.of(
                    Arguments.of(layout, "the stair landing", 27, StairHall.LANDING + 1, 10),
                    Arguments.of(layout, "the west gallery", 24, Manor.F1 + 1, 18),
                    Arguments.of(layout, "the east gallery", 30, Manor.F1 + 1, 18),
                    Arguments.of(
                        layout, "the turret study", Turret.CX, Turret.STUDY + 1, Turret.CZ - 1),
                    Arguments.of(layout, "the glasshouse", 6, Manor.F0 + 1, 13),
                    Arguments.of(layout, "the glasshouse aisle", 7, Manor.F0 + 1, 11),
                    Arguments.of(layout, "the glasshouse by the library", 11, Manor.F0 + 1, 13),
                    Arguments.of(layout, "the kitchen yard", 36, 1, 4)));
  }

  static boolean open(final String state) {
    return state == null || state.equals("minecraft:air");
  }

  static boolean meant(
      final Template t, final Manor.Layout layout, final int x, final int y, final int z) {
    if (overDoor(t, x, y, z)) {
      return true;
    }
    if (new Turret(layout.roundTurret()).inside(x, z, Turret.RADIUS)) {
      return true;
    }
    if (z == Manor.WING_S && ManorFront.inBay(x, z)) {
      return true;
    }
    return Chimney.ALL.stream()
        .flatMap(chimney -> chimney.fires().stream())
        .anyMatch(
            fire -> fire.x() == x && fire.z() == z && y > fire.floor() && y <= fire.floor() + 2);
  }

  @ParameterizedTest(
      name = "{0}: the house walls have no holes but their doors, bay, fires and turret")
  @MethodSource("layouts")
  void wallsHaveNoStrayHoles(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    for (final ManorShell.Box box : List.of(ManorShell.WING, ManorShell.MAIN)) {
      for (int x = box.x0(); x <= box.x1(); x++) {
        for (int z = box.z0(); z <= box.z1(); z++) {
          if (!box.onWall(x, z)) {
            continue;
          }
          for (int y = Manor.F0; y <= Manor.TOP; y++) {
            assertTrue(
                !open(t.get(x, y, z)) || meant(t, layout, x, y, z),
                "hole at " + x + "," + y + "," + z);
          }
        }
      }
    }
  }

  static boolean overDoor(final Template t, final int x, final int y, final int z) {
    for (int down = 1; down <= RouteRoom.HOUSEHOLD - 2; down++) {
      final String below = t.get(x, y - down, z);
      if (below != null && below.contains("dark_oak_door") && below.contains("half=upper")) {
        return true;
      }
    }
    return false;
  }

  @ParameterizedTest(name = "{0}: every door has a fanlight a vampire fits under, then solid wall")
  @MethodSource("layouts")
  void doorsHaveAHeadAboveThem(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    for (final Pos door : find(t, "dark_oak_door")) {
      if (t.get(door.x(), door.y(), door.z()).contains("half=lower")) {
        for (int up = 2; up < RouteRoom.HOUSEHOLD; up++) {
          assertTrue(open(t.get(door.x(), door.y() + up, door.z())), "fanlight over " + door);
        }
        assertTrue(
            !open(t.get(door.x(), door.y() + RouteRoom.HOUSEHOLD, door.z())), "head over " + door);
      }
    }
  }

  @ParameterizedTest(name = "{0}: every finial stands on a full block")
  @MethodSource("layouts")
  void finialsSitOnFullBlocks(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    final List<Pos> rods = find(t, ManorRoofs.FINIAL);
    assertEquals(5, rods.size());
    for (final Pos rod : rods) {
      final String below = t.get(rod.x(), rod.y() - 1, rod.z());
      assertTrue(
          below.endsWith("tiles") || below.endsWith("bricks"),
          "under the finial " + rod + ": " + below);
    }
  }

  @ParameterizedTest(name = "{0}: every lantern hangs from or stands on something solid")
  @MethodSource("layouts")
  void lanternsAreHeld(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    for (final Pos lantern : find(t, ":lantern")) {
      final boolean hanging = t.get(lantern.x(), lantern.y(), lantern.z()).contains("hanging=true");
      final String held = t.get(lantern.x(), lantern.y() + (hanging ? 1 : -1), lantern.z());
      assertTrue(
          !open(held) && !held.contains("type=bottom") && !held.contains("lantern"),
          "lantern " + lantern + " held by " + held);
    }
  }

  @ParameterizedTest(name = "{0}: a vampire walks up to the study without a ladder")
  @MethodSource("layouts")
  void aVampireWalksUpToTheStudy(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    assertTrue(find(t, ":ladder").isEmpty());
    final Pos study = new Pos(Turret.CX, Turret.STUDY + 1, Turret.CZ - 1);
    assertTrue(StructureWalk.mob(t, start()).contains(study));
  }

  @ParameterizedTest(name = "{0}: {1} can be walked to")
  @MethodSource("places")
  void reachesItsPlaces(
      final Manor.Layout layout, final String place, final int x, final int y, final int z) {
    assertTrue(walk(Manor.build(layout)).contains(new Pos(x, y, z)), place);
  }
}
