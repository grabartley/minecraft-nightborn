package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  @ParameterizedTest(name = "{0} at {1},{2},{3}: the camp fits around the old fire {4}")
  @CsvSource({
    "AIR, 0, 0, 0, true",
    "BUILT, 0, 0, 0, true",
    "BUILT, 0, 1, 0, false",
    "BUILT, 1, 0, 0, false",
    "AIR, 0, -1, 0, false",
    "BUILT, 3, 0, 0, false"
  })
  void setsUpAgainAroundAnOldFireOnlyWhileNothingIsBuiltOver(
      final Ground ground, final int x, final int y, final int z, final boolean fits) {
    final CampSite.Survey survey =
        (dx, dy, dz) -> dx == x && dy == y && dz == z ? ground : meadow(dy);
    assertEquals(fits, CampSite.fitsAround(survey, SITE, BlockPos.ORIGIN));
  }

  @ParameterizedTest(name = "searching {0} out in steps of {1} tries the nearest spot first")
  @CsvSource({"12, 3", "6, 2", "4, 4"})
  void triesTheNearestSpotsFirst(final int search, final int stride) {
    final List<BlockPos> offsets = CampSite.nearestFirst(search, stride);
    final int across = search * 2 / stride + 1;
    assertEquals(across * across, offsets.size());
    assertEquals(BlockPos.ORIGIN, offsets.get(0));
    for (int i = 1; i < offsets.size(); i++) {
      assertTrue(
          offsets.get(i - 1).getSquaredDistance(BlockPos.ORIGIN)
              <= offsets.get(i).getSquaredDistance(BlockPos.ORIGIN));
      assertEquals(0, offsets.get(i).getY());
    }
  }

  @ParameterizedTest(name = "far spot {0}, old fire close and reusable {1}, any spot {2}: {3}")
  @CsvSource({
    "true, true, true, FAR",
    "true, false, false, FAR",
    "false, true, true, OLD_FIRE",
    "false, true, false, OLD_FIRE",
    "false, false, true, NEAR",
    "false, false, false, NONE"
  })
  void fallsBackToTheOldFireOnlyWhenNoSpotIsFarEnoughAway(
      final boolean far, final boolean oldFire, final boolean any, final String expected) {
    final Optional<String> chosen =
        CampSite.choose(
            () -> far ? Optional.of("FAR") : Optional.empty(),
            () -> oldFire ? Optional.of("OLD_FIRE") : Optional.empty(),
            () -> any ? Optional.of("NEAR") : Optional.empty());
    assertEquals(expected, chosen.orElse("NONE"));
  }

  @ParameterizedTest(name = "{0},{1} from the party is within {2}: {3}")
  @CsvSource({
    "0, 0, 28, true",
    "28, 0, 28, true",
    "19, 20, 28, true",
    "20, 21, 28, false",
    "0, -29, 28, false"
  })
  void reachesOnlyAnOldFireCloseBy(final int dx, final int dz, final int reach, final boolean in) {
    final BlockPos from = new BlockPos(100, 64, -40);
    assertEquals(in, CampSite.inReach(from, from.add(dx, 30, dz), reach));
  }
}
