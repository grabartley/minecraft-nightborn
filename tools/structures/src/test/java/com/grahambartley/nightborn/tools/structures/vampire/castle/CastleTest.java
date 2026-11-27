package com.grahambartley.nightborn.tools.structures.vampire.castle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;
import com.grahambartley.nightborn.tools.structures.vampire.StructureAudit;
import com.grahambartley.nightborn.tools.structures.vampire.StructureWalk;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CastleTest {

  static Stream<Template> layouts() {
    return Castle.LAYOUTS.stream().map(Castle::build);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void theLordStandsBeforeTheThroneAtTheMiddle(final Template t) {
    assertEquals(Castle.SIZE, t.sizeX());
    assertEquals(Castle.SIZE, t.sizeZ());
    assertTrue(t.get(Castle.MID, Castle.FLOOR, Castle.MID).contains(Keep.DAIS));
    assertEquals(BlockState.AIR, t.get(Castle.MID, Castle.FLOOR + 1, Castle.MID));
    assertEquals(BlockState.AIR, t.get(Castle.MID, Castle.FLOOR + 2, Castle.MID));
    assertTrue(
        t.get(Castle.MID, Castle.FLOOR + 1, Keep.THRONE_Z)
            .startsWith("nightborn:ornate_chair[facing=south"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void theLordWalksToEveryPieceHisHouseholdUses(final Template t) {
    final StructureWalk.Walker lord =
        new StructureWalk.Walker(false, RouteRoom.onFoot(RouteRoom.TALLEST_LORD));
    final Pos gate = new Pos(Castle.MID, Castle.FLOOR, Gatehouse.SOUTH);
    assertEquals(
        List.of(), StructureAudit.unreachedHousehold(t, StructureWalk.from(t, lord, gate)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void theLordFloatsFromHisDaisOutOfTheGateAndBack(final Template t) {
    final Pos dais = new Pos(Castle.MID, Castle.FLOOR + 1, Castle.MID);
    final Pos gate = new Pos(Castle.MID, Castle.FLOOR, Gatehouse.SOUTH);
    assertTrue(StructureWalk.roomy(t, StructureWalk.LORD, dais).contains(gate), "out");
    assertTrue(StructureWalk.roomy(t, StructureWalk.LORD, gate).contains(dais), "back");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void sleepsTheCourtInDarkOakWithGothicStoneByTheHeart(final Template t) {
    final List<Coffins.Coffin> coffins = Coffins.in(t);
    assertTrue(coffins.size() >= 7, "coffins " + coffins.size());
    Coffins.assertSound(t, List.of(Castle.DARK_OAK_COFFIN, Castle.GOTHIC_COFFIN));
    for (final Coffins.Coffin coffin : coffins) {
      if (coffin.block().equals(Castle.GOTHIC_COFFIN)) {
        assertTrue(
            coffin.foot().z() < Keep.SOUTH && coffin.foot().y() == Castle.FLOOR
                || coffin.foot().y() == Castle.FLOOR + 1,
            coffin.toString());
      }
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void chestsHoldCastleLootAndChandeliersHang(final Template t) {
    final long chests =
        t.blockData().values().stream()
            .map(data -> data.get("LootTable"))
            .filter(Castle.LOOT::equals)
            .count();
    assertEquals(3, chests);
    Coffins.assertHung(t);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("layouts")
  void standsOnAPlinthAndClearsTheCourtyard(final Template t) {
    for (final Map.Entry<int[], String> probe :
        Map.of(
                new int[] {0, 0, 20}, "blackstone",
                new int[] {20, Castle.GROUND, 20}, "deepslate",
                new int[] {Castle.MID, Castle.FLOOR + 3, 33}, "air",
                new int[] {10, Castle.HEIGHT - 1, 20}, "air")
            .entrySet()) {
      final int[] at = probe.getKey();
      assertTrue(t.get(at[0], at[1], at[2]).contains(probe.getValue()), t.get(at[0], at[1], at[2]));
    }
  }
}
