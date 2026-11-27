package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LairPassageTest {

  private static Template cave(final int sizeX, final int sizeZ) {
    final Template t = new Template("vampire/test", sizeX, 12, sizeZ);
    LairCave.carve(t);
    return t;
  }

  @ParameterizedTest(
      name = "a {2} passage at {3} in a {0} by {1} cave runs from the edge to open cave")
  @CsvSource({
    "25, 25, south, 12",
    "25, 25, north, 12",
    "25, 25, east, 12",
    "25, 25, west, 12",
    "31, 15, north, 17",
    "31, 15, south, 10",
    "15, 15, west, 7"
  })
  void thePassageRunsThroughTheShellIntoTheCave(
      final int sizeX, final int sizeZ, final String outward, final int along) {
    final Template t = cave(sizeX, sizeZ);
    final List<Pos> run = LairPassage.run(t, along, outward);
    assertTrue(run.size() >= LairCave.SHELL, "runs " + run.size());
    final Pos mouth = run.getFirst();
    final Pos step = Facing.STEP.get(outward);
    assertFalse(t.inside(mouth.x() + step.x(), 0, mouth.z() + step.z()), "starts at the edge");
    final Pos past = run.getLast().offset(Facing.STEP.get(Facing.OPPOSITE.get(outward)));
    assertTrue(LairCave.roof(t, past.x(), past.z()) > LairPassage.HEADROOM, "ends in the cave");
  }

  @ParameterizedTest(name = "a {2} passage in a {0} by {1} cave is walkable and marked")
  @CsvSource({"25, 25, south, 12", "31, 15, north, 17", "25, 25, east, 12", "15, 15, west, 7"})
  void aPersonWalksTheWholePassageUnderRock(
      final int sizeX, final int sizeZ, final String outward, final int along) {
    final Template t = cave(sizeX, sizeZ);
    LairPassage.cut(t, along, outward);
    final List<Pos> run = LairPassage.run(t, along, outward);
    for (final Pos cell : run) {
      assertTrue(StructureWalk.open(t.get(cell.x(), 1, cell.z())), "feet at " + cell);
      assertTrue(StructureWalk.open(t.get(cell.x(), 2, cell.z())), "head at " + cell);
      assertFalse(StructureWalk.open(t.get(cell.x(), 0, cell.z())), "floor at " + cell);
      assertFalse(StructureWalk.open(t.get(cell.x(), 3, cell.z())), "roof at " + cell);
    }
    final Pos mouth = run.getFirst();
    assertEquals(
        "minecraft:jigsaw[orientation=" + outward + "_up]", t.get(mouth.x(), 0, mouth.z()));
    assertEquals("minecraft:cobweb", t.get(mouth.x(), LairPassage.HEADROOM, mouth.z()));
    final Pos inner = run.getLast();
    assertTrue(t.get(inner.x(), 1, inner.z()).startsWith("nightborn:blood_splatter"));
  }

  static Stream<Arguments> lairs() {
    return Stream.of(
        Arguments.of(BloodPoolDen.build(), "south"), Arguments.of(Grotto.build(), "north"));
  }

  @ParameterizedTest(name = "{0} has one lair entrance facing {1} over a rough rock floor")
  @MethodSource("lairs")
  void eachLairMarksOneEntrance(final Template lair, final String outward) {
    final List<Pos> entrances =
        lair.blockData().entrySet().stream()
            .filter(data -> LairPassage.ENTRANCE.equals(data.getValue().get("name")))
            .map(Map.Entry::getKey)
            .toList();
    assertEquals(1, entrances.size());
    final Pos floor = entrances.getFirst();
    assertEquals(
        "minecraft:jigsaw[orientation=" + outward + "_up]", lair.get(floor.x(), 0, floor.z()));
    final String finalState = String.valueOf(lair.blockData().get(floor).get("final_state"));
    assertTrue(finalState.contains("deepslate") || finalState.contains("tuff"), finalState);
    assertFalse(finalState.contains(BlockState.AIR), finalState);
  }
}
