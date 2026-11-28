package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ManorTest {

  private static final Map<Manor.Layout, Template> BUILT =
      Map.of(Manor.TURRET, Manor.build(Manor.TURRET), Manor.TOWER, Manor.build(Manor.TOWER));

  static Stream<Manor.Layout> layouts() {
    return Manor.LAYOUTS.stream();
  }

  static Template built(final Manor.Layout layout) {
    return BUILT.get(layout);
  }

  static Pos start() {
    return new Pos(Manor.MID_X, 1, Manor.SIZE_Z - 1);
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

  static boolean open(final String state) {
    return state == null || state.equals("minecraft:air");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void fillsEveryCellOfItsBox(final Manor.Layout layout) {
    assertEquals(Manor.SIZE_X * Manor.SIZE_Y * Manor.SIZE_Z, built(layout).cells().size());
  }

  @ParameterizedTest(name = "{0}: no brick is left anywhere in the house")
  @MethodSource("layouts")
  void noBrickIsLeft(final Manor.Layout layout) {
    for (final String state : built(layout).cells().values()) {
      final String block = state.replaceAll("\\[.*", "");
      assertTrue(!block.matches("minecraft:brick(s|_stairs|_slab|_wall)"), block);
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void sleepsAFullHouseholdInDarkOakAndSpruce(final Manor.Layout layout) {
    final Template t = built(layout);
    assertEquals(2, find(t, "dark_oak_coffin").size() / 2);
    assertEquals(3, find(t, "spruce_coffin").size() / 2);
  }

  @ParameterizedTest(name = "{0}: loot, lecterns, the font and the gramophone are all kept")
  @MethodSource("layouts")
  void keepsItsHouseholdThings(final Manor.Layout layout) {
    final Template t = built(layout);
    assertTrue(find(t, ":chest").size() >= 8, "chests " + find(t, ":chest").size());
    assertEquals(3, find(t, ":lectern").size());
    assertEquals(1, find(t, ":old_blood_font").size());
    assertEquals(1, find(t, ":gramophone").size());
  }

  @ParameterizedTest(name = "{0}: every door, coffin, chest and lectern can be walked to")
  @MethodSource("layouts")
  void everyRoomIsReachedFromTheGate(final Manor.Layout layout) {
    final Template t = built(layout);
    final Set<Pos> reached = StructureWalk.from(t, start());
    for (final Pos door : find(t, "dark_oak_door")) {
      if (t.get(door.x(), door.y(), door.z()).contains("half=lower")) {
        assertTrue(reached.contains(door), "door " + door);
      }
    }
    for (final String thing : List.of("_coffin", ":chest", ":lectern", ":old_blood_font")) {
      for (final Pos at : find(t, thing)) {
        assertTrue(StructureWalk.beside(reached, at.x(), at.y(), at.z()), thing + " " + at);
      }
    }
  }

  @ParameterizedTest(name = "{0}: household vampires walk to every coffin, the font and the door")
  @MethodSource("layouts")
  void vampiresWalkToTheirCoffinsTheFontAndTheDoor(final Manor.Layout layout) {
    final Template t = built(layout);
    final Set<Pos> walked = StructureWalk.mob(t, start());
    for (final String thing : List.of("_coffin", ":old_blood_font")) {
      for (final Pos at : find(t, thing)) {
        assertTrue(StructureWalk.beside(walked, at.x(), at.y(), at.z()), thing + " " + at);
      }
    }
    assertTrue(walked.contains(new Pos(Manor.MID_X, ManorShell.GROUND, Manor.HOUSE_S)));
  }

  static Stream<Arguments> groundRooms() {
    final List<ManorShell.Box> rooms = new ArrayList<>(ManorShell.SIDE_ROOMS);
    rooms.add(ManorShell.HALL);
    return layouts().flatMap(layout -> rooms.stream().map(room -> Arguments.of(layout, room)));
  }

  @ParameterizedTest(name = "{0}: ground floor room {1} is at least 4 clear to its ceiling")
  @MethodSource("groundRooms")
  void groundFloorRoomsAreTall(final Manor.Layout layout, final ManorShell.Box room) {
    final Template t = built(layout);
    for (int x = room.x0(); x <= room.x1(); x++) {
      for (int z = room.z0(); z <= room.z1(); z++) {
        if (ceiling(t.get(x, Manor.F0 + 1, z))) {
          continue;
        }
        int y = Manor.F0 + 1;
        while (!ceiling(t.get(x, y, z))) {
          y++;
        }
        assertTrue(y - Manor.F0 - 1 >= 4, "ceiling at " + x + "," + y + "," + z);
      }
    }
  }

  private static boolean ceiling(final String state) {
    return state.startsWith(Manor.PLANKS)
        || state.startsWith("minecraft:" + Manor.BEAM)
        || state.contains("gothic_stone");
  }

  @ParameterizedTest(name = "{0}: the front door, steps, sweep and gate share one line")
  @MethodSource("layouts")
  void theApproachLinesUp(final Manor.Layout layout) {
    final Template t = built(layout);
    final int y = ManorShell.GROUND;
    assertEquals(3, run(t, Manor.HOUSE_S, y, StructureWalk::open));
    assertEquals(7, run(t, ManorFront.STEP_Z, Manor.F0, s -> s.contains("stairs")));
    assertTrue(open(t.get(Manor.MID_X, 1, Grounds.RAILING_Z)), "the gate stands open");
    assertTrue(
        StructureWalk.from(t, start()).contains(new Pos(Manor.MID_X, y, Manor.HOUSE_S - 2)),
        "the gate leads straight in through the door");
  }

  private static int run(final Template t, final int z, final int y, final Predicate<String> is) {
    int width = 0;
    for (int x = Manor.MID_X - 6; x <= Manor.MID_X + 6; x++) {
      final boolean centred = is.test(t.get(x, y, z));
      if (centred) {
        width++;
        assertTrue(Math.abs(x - Manor.MID_X) <= 3, "off the centre line at " + x);
      }
    }
    return width;
  }

  static Stream<Arguments> doorways() {
    return layouts()
        .flatMap(layout -> ManorShell.DOORWAYS.stream().map(door -> Arguments.of(layout, door)));
  }

  @ParameterizedTest(name = "{0}: {1} opens at least two wide")
  @MethodSource("doorways")
  void everyDoorwayOpensAtLeastTwoWide(final Manor.Layout layout, final Doorway doorway) {
    final Template t = built(layout);
    int open = 0;
    for (int a = doorway.from() - 1; a <= doorway.to() + 1; a++) {
      final int x = doorway.along() == Axis.X ? a : doorway.line();
      final int z = doorway.along() == Axis.X ? doorway.line() : a;
      if (StructureWalk.clear(t, x, doorway.floor(), z, RouteRoom.HOUSEHOLD)) {
        open++;
      }
    }
    assertTrue(open >= 2, "open across " + open);
  }

  @ParameterizedTest(name = "{0}: the main stair is three wide")
  @MethodSource("layouts")
  void theMainStairIsThreeWide(final Manor.Layout layout) {
    final Template t = built(layout);
    final int z = StairHall.LANDING_FRONT + 2;
    final int y = StairHall.LANDING - 1;
    int treads = 0;
    for (int x = StairHall.WEST; x <= StairHall.EAST; x++) {
      if (t.get(x, y, z).startsWith("minecraft:" + StairHall.STEP)) {
        treads++;
      }
    }
    assertEquals(3, treads);
  }

  @ParameterizedTest(name = "{0}: every window has a closed curtain that covers it")
  @MethodSource("layouts")
  void everyWindowIsCurtained(final Manor.Layout layout) {
    final Template t = built(layout);
    final Turret turret = new Turret(layout.roundTurret());
    int windows = 0;
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      final Pos at = cell.getKey();
      if (!cell.getValue().contains("glass_pane")) {
        continue;
      }
      final String inward;
      if (turret.ring(at.x(), at.z()) && !Turret.inRoom(at.x(), at.z())) {
        inward = Turret.inward(at.x(), at.z());
      } else if (ManorShell.HOUSE.onWall(at.x(), at.z())) {
        inward = houseInward(at);
      } else {
        continue;
      }
      final Pos step = Facing.STEP.get(inward);
      final String drape = t.get(at.x() + step.x(), at.y(), at.z() + step.z());
      assertTrue(
          drape.contains("_velvet_drape")
              && drape.contains("open=false")
              && drape.contains("facing=" + inward),
          "window " + at + " is behind " + drape);
      windows++;
    }
    assertTrue(windows > 100, "windows " + windows);
  }

  private static String houseInward(final Pos at) {
    if (at.x() == Manor.HOUSE_W) {
      return "east";
    }
    if (at.x() == Manor.HOUSE_E) {
      return "west";
    }
    return at.z() == Manor.HOUSE_N ? "south" : "north";
  }

  @ParameterizedTest(name = "{0}: the outer walls have no holes but their doorways and fires")
  @MethodSource("layouts")
  void wallsHaveNoStrayHoles(final Manor.Layout layout) {
    final Template t = built(layout);
    final Turret turret = new Turret(layout.roundTurret());
    for (int x = Manor.HOUSE_W; x <= Manor.HOUSE_E; x++) {
      for (int z = Manor.HOUSE_N; z <= Manor.HOUSE_S; z++) {
        if (!ManorShell.HOUSE.onWall(x, z) || turret.inside(x, z, Turret.RADIUS)) {
          continue;
        }
        for (int y = Manor.F0; y <= Manor.TOP; y++) {
          assertTrue(!open(t.get(x, y, z)) || meant(x, y, z), "hole at " + x + "," + y + "," + z);
        }
      }
    }
  }

  private static boolean meant(final int x, final int y, final int z) {
    for (final Doorway doorway : ManorShell.DOORWAYS) {
      final int a = doorway.along() == Axis.X ? x : z;
      final int c = doorway.along() == Axis.X ? z : x;
      if (a >= doorway.from()
          && a <= doorway.to()
          && c == doorway.line()
          && y >= doorway.floor()
          && y < doorway.floor() + doorway.height()) {
        return true;
      }
    }
    return Chimney.ALL.stream()
        .flatMap(chimney -> chimney.fires().stream())
        .anyMatch(
            fire ->
                fire.firebox().x() == x
                    && fire.firebox().z() == z
                    && y > fire.floor()
                    && y <= fire.floor() + 2);
  }

  @ParameterizedTest(name = "{0}: every door has a fanlight a vampire fits under, then a head")
  @MethodSource("layouts")
  void doorsHaveAHeadAboveThem(final Manor.Layout layout) {
    final Template t = built(layout);
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
    final Template t = built(layout);
    final List<Pos> rods = find(t, ManorRoofs.FINIAL);
    assertEquals(4, rods.size());
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
    final Template t = built(layout);
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
    final Template t = built(layout);
    assertTrue(find(t, ":ladder").isEmpty());
    final Pos study = new Pos(Turret.CX, Turret.STUDY + 1, Turret.CZ - 2);
    assertTrue(StructureWalk.mob(t, start()).contains(study));
  }

  static Stream<Arguments> places() {
    return layouts()
        .flatMap(
            layout ->
                Stream.of(
                    Arguments.of(layout, "the stair landing", 38, StairHall.LANDING + 1, 19),
                    Arguments.of(layout, "the west gallery", 33, Manor.F1 + 1, 30),
                    Arguments.of(layout, "the east gallery", 43, Manor.F1 + 1, 30),
                    Arguments.of(layout, "the front gallery", 38, Manor.F1 + 1, 40),
                    Arguments.of(layout, "the library", 25, ManorShell.GROUND, 26),
                    Arguments.of(layout, "the drawing room", 26, ManorShell.GROUND, 37),
                    Arguments.of(layout, "the dining room", 50, ManorShell.GROUND, 38),
                    Arguments.of(layout, "the kitchen", 52, ManorShell.GROUND, 26),
                    Arguments.of(layout, "the cellar", 54, Cellar.STAND, 25),
                    Arguments.of(layout, "the west chamber", 26, ManorShell.UPPER, 26),
                    Arguments.of(layout, "the master chamber", 25, ManorShell.UPPER, 36),
                    Arguments.of(layout, "the sitting room", 55, ManorShell.UPPER, 25),
                    Arguments.of(layout, "the east chamber", 50, ManorShell.UPPER, 37),
                    Arguments.of(layout, "the glasshouse", 14, ManorShell.GROUND, 20),
                    Arguments.of(layout, "the kitchen yard", 50, 1, 5),
                    Arguments.of(layout, "the back garden", 12, 1, 10)));
  }

  @ParameterizedTest(name = "{0}: {1} can be walked to")
  @MethodSource("places")
  void reachesItsPlaces(
      final Manor.Layout layout, final String place, final int x, final int y, final int z) {
    assertTrue(StructureWalk.from(built(layout), start()).contains(new Pos(x, y, z)), place);
  }

  @Test
  void theTwoVariantsDifferInMoreThanTheCornerTower() {
    final Template turret = built(Manor.TURRET);
    final Template tower = built(Manor.TOWER);
    final Turret corner = new Turret(false);
    int differ = 0;
    for (final Map.Entry<Pos, String> cell : turret.cells().entrySet()) {
      final Pos at = cell.getKey();
      if (!corner.inside(at.x(), at.z(), Turret.RADIUS + 1)
          && !cell.getValue().equals(tower.get(at.x(), at.y(), at.z()))) {
        differ++;
      }
    }
    assertTrue(differ > 200, "cells that differ away from the tower: " + differ);
  }
}
