package com.grahambartley.nightborn.tools.structures.camp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.StructureAudit;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class HunterCampTest {

  private static final Template CAMP = HunterCamp.build();

  static Stream<Arguments> residents() {
    return CAMP.entities().stream()
        .map(
            entity -> {
              final List<Object> pos = ((NbtList) entity.get("blockPos")).items();
              return Arguments.of(
                  ((Map<?, ?>) entity.get("nbt")).get("id"), pos.get(0), pos.get(1), pos.get(2));
            });
  }

  @Test
  void housesThreeHuntersAndOneGuildMasterWhoNeverDespawn() {
    final List<Object> ids =
        CAMP.entities().stream()
            .<Object>map(entity -> ((Map<?, ?>) entity.get("nbt")).get("id"))
            .toList();
    assertEquals(3, ids.stream().filter(HunterCamp.HUNTER::equals).count());
    assertEquals(1, ids.stream().filter(HunterCamp.GUILD_MASTER::equals).count());
    CAMP.entities()
        .forEach(
            entity ->
                assertEquals(
                    Nbt.flag(true), ((Map<?, ?>) entity.get("nbt")).get("PersistenceRequired")));
  }

  @ParameterizedTest(name = "{0} at {1},{2},{3} has room to stand")
  @MethodSource("residents")
  void everyResidentStandsInOpenAir(final String id, final int x, final int y, final int z) {
    assertFalse(CAMP.get(x, y - 1, z) == null || CAMP.get(x, y - 1, z).endsWith(":air"), id);
    assertTrue(standable(CAMP.get(x, y, z)), id + " feet in " + CAMP.get(x, y, z));
    assertEquals("minecraft:air", CAMP.get(x, y + 1, z), id + " head");
  }

  private static boolean standable(final String state) {
    return state.equals("minecraft:air") || state.endsWith("_carpet");
  }

  @Test
  void theCampIsAnchoredAtItsHeartOnATroddenPath() {
    assertEquals(
        "minecraft:jigsaw[orientation=up_north]",
        CAMP.get(HunterCamp.HEART_X, 0, HunterCamp.HEART_Z));
    final Map<String, Object> heart =
        CAMP.blockData().get(new Pos(HunterCamp.HEART_X, 0, HunterCamp.HEART_Z));
    assertEquals(HunterCamp.HEART, heart.get("name"));
    assertEquals("minecraft:dirt_path", heart.get("final_state"));
  }

  @Test
  void aLitCampfireBurnsAtTheHeartOfTheCamp() {
    assertEquals(
        "minecraft:campfire[facing=north,lit=true]",
        CAMP.get(HunterCamp.FIRE_X, 1, HunterCamp.FIRE_Z));
  }

  @ParameterizedTest(name = "the {3} at {0},{1},{2} holds camp loot")
  @CsvSource({
    "10, 1, 2, minecraft:chest",
    "10, 1, 1, minecraft:barrel",
    "1, 1, 11, minecraft:barrel",
    "15, 1, 2, minecraft:barrel"
  })
  void containersRollTheCampLootTable(final int x, final int y, final int z, final String id) {
    final Map<String, Object> data = CAMP.blockData().get(new Pos(x, y, z));
    assertEquals(id, data.get("id"));
    assertEquals(HunterCamp.LOOT, data.get("LootTable"));
    assertTrue(CAMP.get(x, y, z).startsWith(id));
  }

  @ParameterizedTest(name = "the rack at {0},{1},{2} holds {3} and {4}")
  @CsvSource({
    "1, 2, 11, nightborn:wooden_stake, nightborn:silver_sword",
    "14, 2, 9, nightborn:silver_stake, minecraft:crossbow"
  })
  void weaponRacksHoldHunterGear(
      final int x, final int y, final int z, final String first, final String second) {
    final List<Object> hooks =
        ((NbtList) CAMP.blockData().get(new Pos(x, y, z)).get("Items")).items();
    assertEquals(first, ((Map<?, ?>) hooks.get(0)).get("id"));
    assertEquals(second, ((Map<?, ?>) hooks.get(1)).get("id"));
  }

  @ParameterizedTest(name = "{3} hangs at {0},{1},{2} with support above")
  @CsvSource({
    "8, 4, 5, nightborn:garlic_string",
    "4, 3, 11, nightborn:garlic_string",
    "8, 4, 3, minecraft:lantern",
    "2, 3, 11, minecraft:lantern"
  })
  void hangingThingsHangFromCanvas(final int x, final int y, final int z, final String block) {
    assertTrue(CAMP.get(x, y, z).startsWith(block + "["));
    assertTrue(CAMP.get(x, y, z).contains("hanging=true"));
    assertTrue(CAMP.get(x, y + 1, z).endsWith("_wool"));
  }

  @ParameterizedTest(name = "wall piece at {0},{1},{2} facing {3} has a wall behind")
  @CsvSource({
    "8, 3, 1, south, 0, -1",
    "6, 2, 1, south, 0, -1",
    "10, 2, 1, south, 0, -1",
    "1, 2, 11, east, -1, 0",
    "14, 2, 9, west, 1, 0",
    "14, 1, 9, west, 1, 0"
  })
  void wallPiecesHangOnSolidBlocks(
      final int x, final int y, final int z, final String facing, final int dx, final int dz) {
    assertTrue(CAMP.get(x, y, z).contains("facing=" + facing));
    final String behind = CAMP.get(x + dx, y, z + dz);
    assertTrue(behind.endsWith("_wool") || behind.contains("spruce_log"), behind);
  }

  @Test
  void everyTentAndTheFireCanBeWalkedTo() {
    final Pos fire = new Pos(HunterCamp.HEART_X, 1, 0);
    final Set<Pos> reached = StructureWalk.from(CAMP, fire);
    assertEquals(List.of(), StructureAudit.sealedFloors(CAMP, reached));
    assertEquals(List.of(), StructureAudit.doorsLeadingNowhere(CAMP, reached));
    assertEquals(List.of(), StructureAudit.humanOnly(reached, StructureWalk.mob(CAMP, fire), CAMP));
  }

  @Test
  void everyDecorationIsHeld() {
    assertEquals(List.of(), StructureAudit.loose(CAMP));
  }
}
