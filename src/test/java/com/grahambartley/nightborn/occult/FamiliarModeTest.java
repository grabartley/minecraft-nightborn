package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarModeTest {

  @ParameterizedTest(name = "{0} -> {1}, {2}")
  @CsvSource({
    "FIGHT, nightborn.familiar.order.fight, nightborn:wheel/fight",
    "CARRY, nightborn.familiar.order.carry, nightborn:wheel/carry",
    "SCOUT, nightborn.familiar.order.scout, nightborn:wheel/scout"
  })
  @DisplayName("each task has its own label and icon on the command wheel")
  void wheel(final FamiliarMode mode, final String label, final String icon) {
    assertEquals(label, mode.labelKey());
    assertEquals(Identifier.of(icon), mode.icon());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"FIGHT, false", "CARRY, false", "SCOUT, true"})
  @DisplayName("a familiar passes through blocks only while scouting")
  void passesThroughBlocks(final FamiliarMode mode, final boolean expected) {
    assertEquals(expected, mode.passesThroughBlocks());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({
    "FIGHT, nightborn.familiar.mode.fight",
    "CARRY, nightborn.familiar.mode.carry",
    "SCOUT, nightborn.familiar.mode.scout"
  })
  @DisplayName("each task has its own message")
  void translationKey(final FamiliarMode mode, final String expected) {
    assertEquals(expected, mode.translationKey());
  }

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"0, FIGHT", "1, CARRY", "2, SCOUT", "-1, FIGHT", "9, FIGHT"})
  @DisplayName("a synced index outside the tasks falls back to fighting")
  void byIndex(final int index, final FamiliarMode expected) {
    assertEquals(expected, FamiliarMode.byIndex(index));
  }

  @ParameterizedTest(name = "''{0}'' -> {1}")
  @CsvSource({"CARRY, CARRY", "SCOUT, SCOUT", "'', FIGHT", "carry, FIGHT"})
  @DisplayName("a saved name outside the tasks falls back to fighting")
  void byName(final String name, final FamiliarMode expected) {
    assertEquals(expected, FamiliarMode.byName(name));
  }
}
