package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class BloodStainsTest {

  private static Template room() {
    final Template t = new Template("vampire/test_room", 11, 5, 11);
    t.fill(0, 0, 0, 10, 4, 10, "minecraft:stone_bricks");
    t.fill(1, 1, 1, 9, 3, 9, "minecraft:air");
    return t;
  }

  private static List<Pos> stains(final Template t) {
    return t.cells().entrySet().stream()
        .filter(cell -> cell.getValue().startsWith("nightborn:blood_"))
        .map(Map.Entry::getKey)
        .toList();
  }

  @ParameterizedTest(name = "{0} at 5,1,5 is a floor to stain: {1}")
  @CsvSource({
    "minecraft:stone_bricks, true",
    "minecraft:red_carpet, false",
    "'minecraft:stone_brick_stairs[facing=north,half=bottom]', false",
    "minecraft:grass_block, false",
    "minecraft:air, false"
  })
  void stainsOnlyBareFloor(final String ground, final boolean stained) {
    final Template t = room();
    t.set(5, 0, 5, ground);
    assertEquals(stained, BloodStains.floor(t, 5, 1, 5));
  }

  @ParameterizedTest(name = "a {0} beside the cell keeps it clear")
  @ValueSource(
      strings = {
        "minecraft:dark_oak_door[half=lower]",
        "minecraft:stone_brick_stairs[facing=north]",
        "minecraft:ladder[facing=north]",
        "nightborn:dark_oak_coffin[part=foot]"
      })
  void keepsOffRoutesAndCoffins(final String neighbour) {
    final Template t = room();
    t.set(6, 1, 5, neighbour);
    assertTrue(!BloodStains.floor(t, 5, 1, 5));
  }

  @Test
  void aFloorOpenToTheSkyIsNotStained() {
    final Template t = room();
    t.fill(1, 4, 1, 9, 4, 9, "minecraft:air");
    assertEquals(List.of(), BloodStains.floors(t));
  }

  @ParameterizedTest(name = "{0} clusters of radius {1} stain within reach of each other")
  @CsvSource({"1, 1", "1, 2", "2, 1"})
  void clustersStayTight(final int clusters, final int radius) {
    final Template t = room();
    BloodStains.scatter(t, new BloodStainRules(clusters, radius, 4, 1, 1, 1));
    final List<Pos> stains = stains(t);
    assertTrue(!stains.isEmpty());
    if (clusters == 1) {
      for (final Pos one : stains) {
        for (final Pos other : stains) {
          assertTrue(Math.abs(one.x() - other.x()) <= radius * 2, one + " " + other);
          assertTrue(Math.abs(one.z() - other.z()) <= radius * 2, one + " " + other);
        }
      }
    }
  }

  @Test
  void theSameTemplateIsStainedTheSameWay() {
    final Template one = room();
    final Template two = room();
    final BloodStainRules rules = new BloodStainRules(2, 2, 5, 1, 2, 1);
    BloodStains.scatter(one, rules);
    BloodStains.scatter(two, rules);
    assertEquals(one.cells(), two.cells());
  }

  @Test
  void wallStainsSitLowOnTheWall() {
    final Template t = room();
    BloodStains.scatter(t, new BloodStainRules(3, 2, 0, 0, 6, 1));
    for (final Pos stain : stains(t)) {
      assertTrue(t.get(stain.x(), stain.y(), stain.z()).contains("face=wall"));
      assertTrue(stain.y() <= 2, "stain at " + stain);
    }
  }
}
