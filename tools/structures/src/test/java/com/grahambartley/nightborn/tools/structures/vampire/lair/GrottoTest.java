package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GrottoTest {

  private static final Template GROTTO = Grotto.build();

  static IntStream benches() {
    return Grotto.BENCHES.stream().mapToInt(Integer::intValue);
  }

  @Test
  void holdsFiveLairCoffinsWhoseLidsCanSlideClear() {
    assertEquals(5, Coffins.in(GROTTO).size());
    Coffins.assertSound(GROTTO, List.of(LairShrine.DEEPSLATE_COFFIN, LairShrine.BLACKSTONE_COFFIN));
  }

  @ParameterizedTest(name = "the bench coffin at z {0} lies on the ledge")
  @MethodSource("benches")
  void benchCoffinsLieOnTheLedge(final int z) {
    final String foot = GROTTO.get(24, Grotto.LEDGE_TOP + 1, z);
    assertTrue(foot.contains("_coffin") && foot.contains("part=foot"), foot);
    assertTrue(!GROTTO.get(24, Grotto.LEDGE_TOP, z).endsWith("air"));
  }

  @ParameterizedTest(name = "the ledge stair at z {0} climbs with headroom")
  @ValueSource(ints = {6, 7, 8})
  void theLedgeStairClimbs(final int z) {
    assertTrue(GROTTO.get(Grotto.LEDGE - 1, 1, z).contains("facing=east"));
    assertTrue(GROTTO.get(Grotto.LEDGE, 2, z).contains("facing=east"));
    for (int y = 2; y <= 4; y++) {
      assertEquals(BlockState.AIR, GROTTO.get(Grotto.LEDGE - 1, y + 1, z));
    }
    for (int y = 3; y <= 5; y++) {
      assertEquals(BlockState.AIR, GROTTO.get(Grotto.LEDGE + 1, y, z));
    }
  }

  @Test
  void aReadingCornerSitsBesideTheHearth() {
    assertTrue(GROTTO.get(4, 1, 7).startsWith("minecraft:lectern"));
    assertTrue(GROTTO.get(5, 1, 7).startsWith("nightborn:ornate_chair"));
  }

  @Test
  void theSpringIsAnOvalKerbedWithGapsToStepIn() {
    final int x = Grotto.SPRING_X;
    final int z = Grotto.SPRING_Z;
    assertEquals(LairFeatures.POOL, GROTTO.get(x, 1, z));
    assertEquals(LairFeatures.POOL, GROTTO.get(x + 5, 1, z));
    assertTrue(GROTTO.get(x - 4, 1, z - 2).startsWith("minecraft:polished_blackstone_brick_slab"));
    assertTrue(!GROTTO.get(x - 5, 1, z - 2).equals(LairFeatures.POOL));
    assertEquals(BlockState.AIR, GROTTO.get(x, 1, z - 3));
    assertEquals(BlockState.AIR, GROTTO.get(x, 1, z + 3));
    assertTrue(GROTTO.get(x + 6, 1, z).startsWith("nightborn:blood_fountain"));
  }

  static Stream<Arguments> furnishings() {
    final int y = Grotto.LEDGE_TOP + 1;
    return Stream.of(
        Arguments.of(11, 1, 11, "nightborn:ornate_table"),
        Arguments.of(10, 1, 11, "nightborn:ornate_chair"),
        Arguments.of(12, 1, 11, "nightborn:ornate_chair"),
        Arguments.of(27, y + 1, 7, "nightborn:chalice"),
        Arguments.of(27, y, 5, "nightborn:candelabra"),
        Arguments.of(27, y, 9, "nightborn:candelabra"),
        Arguments.of(27, y, 3, "minecraft:chest"));
  }

  @ParameterizedTest(name = "{3} stands at {0},{1},{2} with nothing buried over it")
  @MethodSource("furnishings")
  void theHearthAndLedgeAreFurnished(final int x, final int y, final int z, final String block) {
    assertTrue(GROTTO.get(x, y, z).startsWith(block), GROTTO.get(x, y, z));
    final String above = GROTTO.get(x, y + 1, z);
    assertTrue(!above.contains("deepslate") && !above.contains("tuff"), above);
  }

  @Test
  void everyHangingDripstoneIsWhole() {
    LairChecks.assertHangingWhole(GROTTO);
  }

  @Test
  void twoBlocksOfRockWallInTheWholeGrotto() {
    LairChecks.assertSealed(GROTTO);
  }
}
