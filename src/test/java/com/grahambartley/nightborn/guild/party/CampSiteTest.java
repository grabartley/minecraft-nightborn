package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.guild.party.CampSite.Ground;
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

  private static Ground meadow(final int dy) {
    return dy < 0 ? Ground.NATURAL : Ground.AIR;
  }
}
