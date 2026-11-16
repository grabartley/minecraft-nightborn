package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConnectionsTest {

  @Test
  void aFenceConnectsToFencesAndFullBlocks() {
    final Template t = new Template("x", 3, 1, 3);
    t.set(1, 0, 1, "oak_fence");
    t.set(0, 0, 1, "oak_fence");
    t.set(1, 0, 0, "stone");
    t.set(2, 0, 1, "oak_slab");
    Connections.connect(t);
    assertEquals(
        "minecraft:oak_fence[east=false,north=true,south=false,west=true]", t.get(1, 0, 1));
  }

  @Test
  void aWallConnectsLowAndStandsUp() {
    final Template t = new Template("x", 2, 1, 1);
    t.set(0, 0, 0, "cobblestone_wall");
    t.set(1, 0, 0, "cobblestone_wall");
    Connections.connect(t);
    assertEquals(
        "minecraft:cobblestone_wall[east=low,north=none,south=none,up=true,west=none]",
        t.get(0, 0, 0));
  }

  @Test
  void barsConnectToPanesButNotFences() {
    final Template t = new Template("x", 3, 1, 1);
    t.set(0, 0, 0, "glass_pane");
    t.set(1, 0, 0, "iron_bars");
    t.set(2, 0, 0, "oak_fence");
    Connections.connect(t);
    assertEquals(
        "minecraft:iron_bars[east=false,north=false,south=false,west=true]", t.get(1, 0, 0));
  }

  @Test
  void otherBlocksAreLeftAlone() {
    final Template t = new Template("x", 1, 1, 1);
    t.set(0, 0, 0, "oak_planks");
    Connections.connect(t);
    assertEquals("minecraft:oak_planks", t.get(0, 0, 0));
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:stone, true",
    "minecraft:oak_slab[type=bottom], false",
    "minecraft:air, false",
    "nightborn:burgundy_velvet_drape[facing=west], false",
    "nightborn:spruce_coffin[part=head], false",
    "nightborn:portrait[facing=east], false",
    "nightborn:gargoyle[facing=south], false",
    "nightborn:gothic_stone_bricks, true",
    "nightborn:weapon_rack[facing=north], false",
    "minecraft:snow, false",
    "minecraft:snow_block, true"
  })
  void knowsWhichBlocksAreFullCubes(final String state, final boolean full) {
    assertEquals(full, Connections.solidFull(state));
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:oak_fence, minecraft:oak_fence_gate[facing=north], true",
    "minecraft:oak_fence, minecraft:glass_pane, false",
    "minecraft:glass_pane, minecraft:oak_fence, false",
    "minecraft:glass_pane, minecraft:stone_brick_wall, true",
    "minecraft:stone_brick_wall, minecraft:glass_pane, false"
  })
  void connectsByNeighbourKind(final String block, final String neighbour, final boolean on) {
    assertEquals(on, Connections.connects(block, neighbour));
  }
}
