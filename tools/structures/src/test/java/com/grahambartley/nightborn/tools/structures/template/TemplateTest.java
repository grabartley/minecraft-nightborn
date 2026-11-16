package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TemplateTest {

  private final Template t = new Template("hunter_camp/test/piece", 6, 6, 6);

  @ParameterizedTest
  @CsvSource({
    "0, 0, 0, true",
    "5, 5, 5, true",
    "6, 0, 0, false",
    "0, -1, 0, false",
    "0, 0, 6, false"
  })
  void knowsWhatIsInside(final int x, final int y, final int z, final boolean inside) {
    assertEquals(inside, t.inside(x, y, z));
  }

  @Test
  void setRefusesACellOutsideTheTemplate() {
    assertThrows(IllegalArgumentException.class, () -> t.set(6, 0, 0, "stone"));
  }

  @Test
  void setAddsTheNamespaceAndKeepsBlockData() {
    t.set(1, 1, 1, "barrel", Nbt.compound("id", "minecraft:barrel"));
    assertEquals("minecraft:barrel", t.get(1, 1, 1));
    assertEquals(Nbt.compound("id", "minecraft:barrel"), t.blockData().get(new Pos(1, 1, 1)));
  }

  @Test
  void settingWithoutDataOrWithEmptyDataDropsOldData() {
    t.set(1, 1, 1, "barrel", Nbt.compound("id", "minecraft:barrel"));
    t.set(1, 1, 1, "barrel", Nbt.compound());
    assertNull(t.blockData().get(new Pos(1, 1, 1)));
  }

  @Test
  void fillCoversTheBoxWhicheverWayRoundItsCornersAre() {
    t.fill(2, 0, 2, 0, 1, 0, "stone");
    assertEquals(18, t.cells().size());
    assertEquals("minecraft:stone", t.get(0, 1, 2));
  }

  @Test
  void entitySitsInTheMiddleOfItsBlock() {
    t.entity(1, 2, 3, Nbt.compound("id", "minecraft:minecart"));
    final Map<String, Object> entity = t.entities().get(0);
    assertEquals(NbtList.doubles(1.5, 2, 3.5), entity.get("pos"));
    assertEquals(NbtList.ints(1, 2, 3), entity.get("blockPos"));
  }

  @Test
  void itsCollectionsCannotBeChangedFromOutside() {
    assertThrows(UnsupportedOperationException.class, () -> t.cells().put(new Pos(0, 0, 0), "x"));
    assertThrows(UnsupportedOperationException.class, () -> t.blockData().clear());
  }

  @Test
  void embedCopiesCellsAndTheirDataAtAnOffset() {
    final Template inner = new Template("hunter_camp/test/inner", 2, 2, 2);
    inner.set(1, 1, 1, "barrel", Nbt.compound("id", "minecraft:barrel"));
    t.embed(inner, 2, 3, 1);
    assertEquals("minecraft:barrel", t.get(3, 4, 2));
    assertEquals(Nbt.compound("id", "minecraft:barrel"), t.blockData().get(new Pos(3, 4, 2)));
    assertNull(t.get(1, 1, 1));
  }

  @ParameterizedTest(name = "a {0} jigsaw towards {1} keeps its orientation, pool and joint")
  @CsvSource({"up_north, nightborn:pool_a, aligned", "west_up, nightborn:pool_b, rollable"})
  void jigsawsCarryTheirConnection(
      final String orientation, final String pool, final String joint) {
    t.jigsaw(1, 1, 1, orientation, "nightborn:here", "nightborn:there", pool, "stone", joint);
    assertEquals("minecraft:jigsaw[orientation=" + orientation + "]", t.get(1, 1, 1));
    final Map<String, Object> data = t.blockData().get(new Pos(1, 1, 1));
    assertEquals("nightborn:here", data.get("name"));
    assertEquals("nightborn:there", data.get("target"));
    assertEquals(pool, data.get("pool"));
    assertEquals(joint, data.get("joint"));
    assertEquals("minecraft:stone", data.get("final_state"));
  }

  @ParameterizedTest(name = "anchor {0} leaves {1}")
  @CsvSource({"nightborn:camp_heart, dirt_path", "nightborn:other, minecraft:stone"})
  void anAnchorIsAnUpwardJigsawThatLeavesItsFinalState(final String name, final String below) {
    t.anchor(2, 0, 3, name, below);
    assertEquals("minecraft:jigsaw[orientation=up_north]", t.get(2, 0, 3));
    final Map<String, Object> data = t.blockData().get(new Pos(2, 0, 3));
    assertEquals(name, data.get("name"));
    assertEquals(BlockState.full(below), data.get("final_state"));
    assertEquals("minecraft:empty", data.get("pool"));
  }
}
