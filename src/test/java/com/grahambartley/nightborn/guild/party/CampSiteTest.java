package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.party.CampSite.Ground;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampSiteTest {

  private static final CampSite.Bounds SITE = new CampSite.Bounds(-2, 2, -2, 2, 2);

  @ParameterizedTest(name = "{0} at {1},{2},{3} lets the camp pitch: {4}")
  @CsvSource({
    "NATURAL, 0, -1, 0, true",
    "CLEARABLE, 0, 0, 0, true",
    "CLEARABLE, 3, 1, 3, true",
    "BUILT, 0, 0, 0, false",
    "BUILT, 3, 0, 0, false",
    "BUILT, 3, -1, 0, false",
    "AIR, 3, -1, 0, true",
    "AIR, 0, -1, 0, false",
    "CLEARABLE, 0, -1, 0, false",
    "NATURAL, 1, 1, 1, false",
    "BUILT, 4, 0, 0, true"
  })
  void pitchesOnlyOnNaturalGroundAwayFromBuiltBlocks(
      final Ground ground, final int x, final int y, final int z, final boolean fits) {
    final CampSite.Survey survey =
        (dx, dy, dz) -> dx == x && dy == y && dz == z ? ground : meadow(dy);
    assertEquals(fits, CampSite.fits(survey, SITE));
  }

  @ParameterizedTest(name = "air {0}, clearable {1}, natural {2}, dry {3}: {4}")
  @CsvSource({
    "true, false, false, true, AIR",
    "false, true, false, true, CLEARABLE",
    "false, false, true, true, NATURAL",
    "false, false, true, false, BUILT",
    "false, false, false, true, BUILT"
  })
  void sortsEachBlock(
      final boolean air,
      final boolean clearable,
      final boolean natural,
      final boolean dry,
      final Ground ground) {
    assertEquals(ground, CampSite.classify(air, clearable, natural, dry));
  }

  @Test
  void checksTheChunksAtEachCornerBesideTheSite() {
    assertEquals(
        List.of("-3,-3", "-3,3", "3,-3", "3,3"),
        CampSite.corners(SITE).stream().map(c -> c[0] + "," + c[1]).toList());
  }

  private static Ground meadow(final int dy) {
    return dy < 0 ? Ground.NATURAL : Ground.AIR;
  }

  @ParameterizedTest(name = "{0},{1} from the last camp is apart from it at {2}: {3}")
  @CsvSource({
    "0, 0, 16, false",
    "0, 0, 0, true",
    "15, 0, 16, false",
    "16, 0, 16, true",
    "-11, 11, 16, false",
    "-12, 12, 16, true",
    "3, -4, 5, true",
    "3, -3, 5, false"
  })
  void keepsANewCampOffTheLastOnesSpot(
      final int dx, final int dz, final int apart, final boolean far) {
    final BlockPos last = new BlockPos(100, 64, -40);
    assertEquals(far, CampSite.apart(Optional.of(last), last.getX() + dx, last.getZ() + dz, apart));
  }

  @ParameterizedTest(name = "with no last camp, {0},{1} is free")
  @CsvSource({"0, 0", "5, -5", "400, 12"})
  void anyPlaceIsFreeWithNoLastCamp(final int x, final int z) {
    assertEquals(true, CampSite.apart(Optional.empty(), x, z, PartyCamp.APART));
  }
}
