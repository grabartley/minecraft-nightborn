package com.grahambartley.nightborn.tools.structures.camp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CampTentTest {

  private static final CampTent ALONG_Z = new CampTent(0, 3, 4, 2, false, "white_wool");
  private static final CampTent ALONG_X = new CampTent(0, 3, 4, 2, true, "white_wool");

  @ParameterizedTest(name = "along {0}, across {1}, y {2}: {3}")
  @CsvSource({
    "1, 2, 1, minecraft:white_wool",
    "1, 6, 1, minecraft:white_wool",
    "1, 3, 1, minecraft:air",
    "1, 3, 2, minecraft:white_wool",
    "1, 4, 3, minecraft:white_wool",
    "1, 4, 2, minecraft:air",
    "0, 4, 1, minecraft:white_wool",
    "0, 4, 2, minecraft:white_wool",
    "3, 4, 1, minecraft:air"
  })
  void buildsAnAFrameClosedAtTheBackAndOpenAtTheFront(
      final int along, final int across, final int y, final String state) {
    final Template alongZ = new Template("t", 9, 5, 9);
    ALONG_Z.build(alongZ);
    assertEquals(state, alongZ.get(across, y, along));
    final Template alongX = new Template("t", 9, 5, 9);
    ALONG_X.build(alongX);
    assertEquals(state, alongX.get(along, y, across));
  }
}
