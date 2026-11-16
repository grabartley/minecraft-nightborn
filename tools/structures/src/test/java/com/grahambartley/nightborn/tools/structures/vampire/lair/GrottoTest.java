package com.grahambartley.nightborn.tools.structures.vampire.lair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Coffins;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class GrottoTest {

  private static final Template GROTTO = Grotto.build();

  static IntStream benches() {
    return Arrays.stream(Grotto.BENCHES);
  }

  @Test
  void holdsFiveLairCoffinsWhoseLidsCanSlideClear() {
    assertEquals(5, Coffins.in(GROTTO).size());
    Coffins.assertSound(
        GROTTO, List.of(LairFeatures.DEEPSLATE_COFFIN, LairFeatures.BLACKSTONE_COFFIN));
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
  void theSpringIsKerbedWithGapsToStepIn() {
    assertEquals(LairFeatures.POOL, GROTTO.get(14, 0, 7));
    assertTrue(GROTTO.get(10, 1, 5).startsWith("minecraft:polished_blackstone_brick_slab"));
    assertEquals(BlockState.AIR, GROTTO.get(14, 1, 5));
    assertTrue(GROTTO.get(20, 1, 7).startsWith("nightborn:blood_fountain"));
  }
}
