package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.StructureSettings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StructureRarityTest {

  @ParameterizedTest(name = "{0} reads {3} from structures {1} and camps {2}")
  @CsvSource({
    "VAMPIRE_STRUCTURES, 150, 50, 150",
    "HUNTER_CAMPS, 150, 50, 50",
    "HUNTER_CAMPS, 100, 400, 400"
  })
  void eachKindReadsItsOwnSetting(
      final StructureRarity rarity, final int structures, final int camps, final int percent) {
    assertEquals(percent, rarity.percent(new StructureSettings(structures, camps)));
  }

  @ParameterizedTest(name = "{0} is written {1}")
  @CsvSource({"VAMPIRE_STRUCTURES, vampire_structures", "HUNTER_CAMPS, hunter_camps"})
  void writesItsDataName(final StructureRarity rarity, final String name) {
    assertEquals(name, rarity.asString());
  }
}
