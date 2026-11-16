package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
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

  static Set<Pos> walk(final Template t) {
    return ManorWalk.from(t, new Pos(Manor.MID_X, 1, Manor.SIZE_Z - 1));
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
        assertTrue(ManorWalk.beside(reached, at.x(), at.y(), at.z()), thing + " " + at);
      }
    }
  }

  static Stream<Arguments> places() {
    return layouts()
        .flatMap(
            layout ->
                Stream.of(
                    Arguments.of(layout, "the stair landing", 27, Manor.F0 + 4, 10),
                    Arguments.of(layout, "the west gallery", 24, Manor.F1 + 1, 18),
                    Arguments.of(layout, "the east gallery", 30, Manor.F1 + 1, 18),
                    Arguments.of(
                        layout, "the turret study", Turret.CX, Turret.STUDY + 1, Turret.CZ - 1),
                    Arguments.of(layout, "the glasshouse", 6, Manor.F0 + 1, 13),
                    Arguments.of(layout, "the glasshouse aisle", 7, Manor.F0 + 1, 11),
                    Arguments.of(layout, "the glasshouse by the library", 11, Manor.F0 + 1, 13),
                    Arguments.of(layout, "the kitchen yard", 36, 1, 4)));
  }

  @ParameterizedTest(name = "{0}: without its ladder the study is out of reach")
  @MethodSource("layouts")
  void theStudyIsOnlyReachedByItsLadder(final Manor.Layout layout) {
    final Template t = Manor.build(layout);
    final Pos study = new Pos(Turret.CX, Turret.STUDY + 1, Turret.CZ - 1);
    for (final Pos ladder : find(t, ":ladder")) {
      if (ladder.y() > Manor.F1) {
        t.set(ladder.x(), ladder.y(), ladder.z(), "minecraft:air");
      }
    }
    assertTrue(!walk(t).contains(study));
  }

  @ParameterizedTest(name = "{0}: {1} can be walked to")
  @MethodSource("places")
  void reachesItsPlaces(
      final Manor.Layout layout, final String place, final int x, final int y, final int z) {
    assertTrue(walk(Manor.build(layout)).contains(new Pos(x, y, z)), place);
  }
}
