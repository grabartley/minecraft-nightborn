package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MountainCastleTest {

  static final int HOME_RADIUS = 16;
  static final int ROOM_RADIUS = 6;
  static final Pos HEART =
      new Pos(MountainCastle.MID, MountainCastle.HEIGHT / 2, MountainCastle.MID);
  static final Pos EYRIE = new Pos(MountainCastle.MID, Donjon.EYRIE + 1, MountainCastle.MID);

  static Stream<MountainCastle.Layout> layouts() {
    return MountainCastle.LAYOUTS.stream();
  }

  static Set<Pos> walk(final Template t) {
    return StructureWalk.from(
        t, new Pos(MountainCastle.MID, MountainCastle.LOW + 1, Barbican.SOUTH));
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

  static double distance(final Pos one, final Pos other) {
    return Math.sqrt(
        Math.pow(one.x() - other.x(), 2)
            + Math.pow(one.y() - other.y(), 2)
            + Math.pow(one.z() - other.z(), 2));
  }

  static boolean sheltered(final Template t, final Pos feet) {
    for (int y = feet.y() + 2; y < MountainCastle.HEIGHT; y++) {
      final String above = t.get(feet.x(), y, feet.z());
      if (above != null && !StructureWalk.open(above)) {
        return true;
      }
    }
    return false;
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void sleepsTwentyAndTheEldestInGothicStoneAndDarkOak(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    final List<Coffins.Coffin> coffins = Coffins.in(t);
    assertEquals(21, coffins.size());
    Coffins.assertSound(t, List.of(MountainCastle.GOTHIC_COFFIN, MountainCastle.DARK_OAK_COFFIN));
    assertTrue(coffins.stream().anyMatch(c -> c.block().equals(MountainCastle.DARK_OAK_COFFIN)));
    assertTrue(coffins.stream().anyMatch(c -> c.block().equals(MountainCastle.GOTHIC_COFFIN)));
    Coffins.assertHung(t);
  }

  @ParameterizedTest(name = "{0}: one coffin is the Eldest's own, out of the household's reach")
  @MethodSource("layouts")
  void keepsTheEldestsCoffinApart(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    final List<Pos> near = new ArrayList<>();
    final List<Pos> eldest = new ArrayList<>();
    for (final Coffins.Coffin coffin : Coffins.in(t)) {
      final Pos head = Coffins.head(coffin);
      if (distance(head, HEART) <= HOME_RADIUS) {
        near.add(head);
      }
      if (distance(head, EYRIE) <= ROOM_RADIUS) {
        eldest.add(head);
      }
    }
    assertEquals(20, near.size());
    assertEquals(1, eldest.size());
    assertTrue(
        !near.contains(eldest.getFirst()), "the Eldest's coffin is in the household's reach");
  }

  @ParameterizedTest(name = "{0}: the highest sheltered floor on the centre line is the eyrie")
  @MethodSource("layouts")
  void seatsTheEldestInTheHighestRoom(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    Pos highest = null;
    for (int y = 1; y < MountainCastle.HEIGHT - 2; y++) {
      final Pos feet = new Pos(MountainCastle.MID, y, MountainCastle.MID);
      if (StructureWalk.stands(t, feet.x(), y, feet.z()) && sheltered(t, feet)) {
        highest = feet;
      }
    }
    assertEquals(EYRIE, highest);
    assertEquals(null, t.get(MountainCastle.MID, MountainCastle.HEIGHT - 1, MountainCastle.MID));
  }

  @ParameterizedTest(name = "{0}: every coffin, chest and lectern can be walked to from the gate")
  @MethodSource("layouts")
  void everyRoomIsReachedFromTheGate(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    final Set<Pos> reached = walk(t);
    for (final String thing : List.of("_coffin", ":chest", ":lectern", "blood_fountain")) {
      for (final Pos at : find(t, thing)) {
        assertTrue(StructureWalk.beside(reached, at.x(), at.y(), at.z()), thing + " " + at);
      }
    }
  }

  static Stream<Arguments> places() {
    final int mid = MountainCastle.MID;
    return layouts()
        .flatMap(
            layout ->
                Stream.concat(
                    Stream.of(
                        Arguments.of(layout, "the lower ward", 20, MountainCastle.LOW + 1, 50),
                        Arguments.of(layout, "the upper ward", mid, MountainCastle.UP + 1, 41),
                        Arguments.of(layout, "the north court", mid, MountainCastle.UP + 1, 16),
                        Arguments.of(layout, "the feast hall", 17, HallWing.GROUND + 1, 25),
                        Arguments.of(layout, "the library loft", 17, HallWing.LOFT + 1, 25),
                        Arguments.of(layout, "the gate room", mid, Barbican.ROOM + 1, 57),
                        Arguments.of(layout, "the upper wall walk", 30, Ramparts.TOP + 1, 5),
                        Arguments.of(layout, "the lower wall walk", 20, Ramparts.LOW_TOP + 1, 57),
                        Arguments.of(layout, "the eyrie", mid, Donjon.EYRIE + 1, mid),
                        Arguments.of(
                            layout, "the keep's roof walk", mid - 2, Donjon.TERRACE + 1, mid)),
                    Donjon.floors().stream()
                        .map(
                            floor ->
                                Arguments.of(
                                    layout, "keep floor " + floor, mid, floor + 1, mid - 1))));
  }

  @ParameterizedTest(name = "{0}: {1} can be walked to")
  @MethodSource("places")
  void reachesItsPlaces(
      final MountainCastle.Layout layout,
      final String place,
      final int x,
      final int y,
      final int z) {
    assertTrue(walk(MountainCastle.build(layout)).contains(new Pos(x, y, z)), place);
  }

  @ParameterizedTest(name = "{0}: the summit anchor sits at the foot of the keep's centre")
  @MethodSource("layouts")
  void anchorsAtTheKeep(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    final List<Pos> anchors =
        find(t, ":jigsaw").stream()
            .filter(at -> MountainCastle.ANCHOR.equals(t.blockData().get(at).get("name")))
            .toList();
    assertEquals(
        List.of(new Pos(MountainCastle.MID, MountainCastle.UP, MountainCastle.MID)), anchors);
  }

  @ParameterizedTest(name = "{0}: every lantern hangs from or stands on something solid")
  @MethodSource("layouts")
  void lanternsAreHeld(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    for (final Pos lantern : find(t, ":lantern")) {
      final boolean hanging = t.get(lantern.x(), lantern.y(), lantern.z()).contains("hanging=true");
      final String held = t.get(lantern.x(), lantern.y() + (hanging ? 1 : -1), lantern.z());
      assertTrue(
          held != null && !StructureWalk.open(held) && !held.contains("type=bottom"),
          "lantern " + lantern + " held by " + held);
    }
  }

  @ParameterizedTest(name = "{0}: the yards grow no trees or grass")
  @MethodSource("layouts")
  void yardsHoldNoSoil(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    assertEquals(List.of(), find(t, ":grass_block"));
    assertEquals(List.of(), find(t, ":dirt"));
  }

  @ParameterizedTest(name = "{0}: the chapel stands only where the layout has one")
  @MethodSource("layouts")
  void buildsTheChapelOrAGraveyard(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    assertEquals(layout.chapel(), find(t, ":skeleton_skull").isEmpty());
    assertEquals(layout.chapel(), !find(t, "blood_pool").isEmpty());
  }

  @ParameterizedTest(
      name = "{0}: every stretch of wall walk and every doorway onto it is two blocks clear")
  @MethodSource("layouts")
  void nothingHangsOverAWallWalk(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    for (int x = 0; x < MountainCastle.SIZE; x++) {
      for (int z = 0; z < MountainCastle.SIZE; z++) {
        final int walk = Ramparts.top(x, z) + 1;
        if (Crag.level(x, z) == Crag.NONE
            || !Ramparts.onWalk(x, z)
            || !"minecraft:air".equals(t.get(x, walk, z))) {
          continue;
        }
        assertEquals("minecraft:air", t.get(x, walk + 1, z), "over the walk at " + x + "," + z);
      }
    }
  }

  @ParameterizedTest(name = "{0}: the east walk runs from tower to tower")
  @MethodSource("layouts")
  void theEastWalkIsOnePath(final MountainCastle.Layout layout) {
    final Template t = MountainCastle.build(layout);
    final int x = Crag.UPPER_EAST - Ramparts.THICK + 1;
    for (int z = Chapel.NORTH - 5; z <= Chapel.SOUTH + 2; z++) {
      for (int up = 1; up <= Ramparts.WALK_HEADROOM; up++) {
        assertEquals("minecraft:air", t.get(x, Ramparts.TOP + up, z), "walk at z " + z);
      }
    }
  }

  static boolean backedBySolidWall(final Template t, final Pos hung) {
    final String state = t.get(hung.x(), hung.y(), hung.z());
    final String facing = state.replaceAll(".*facing=(\\w+).*", "$1");
    final Pos step = Facing.STEP.get(facing);
    final String back = t.get(hung.x() - step.x(), hung.y(), hung.z() - step.z());
    return Turret.solid(back) && !StructureWalk.open(back);
  }

  @ParameterizedTest(name = "{0}: every {1} hangs on a solid wall, never a window or a doorway")
  @MethodSource("hungThings")
  void hungThingsSitOnSolidWall(final MountainCastle.Layout layout, final String block) {
    final Template t = MountainCastle.build(layout);
    final List<Pos> loose =
        find(t, block).stream().filter(hung -> !backedBySolidWall(t, hung)).toList();
    assertEquals(List.of(), loose);
  }

  static Stream<Arguments> hungThings() {
    return layouts()
        .flatMap(
            layout ->
                Stream.of(Arguments.of(layout, ":portrait"), Arguments.of(layout, ":ladder")));
  }
}
