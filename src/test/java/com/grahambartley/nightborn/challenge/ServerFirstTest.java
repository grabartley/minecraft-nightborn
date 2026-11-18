package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServerFirstTest {

  @ParameterizedTest(name = "{0} with top {1}, staker {2} marks {3}")
  @CsvSource({
    "nightborn:stage_reached, true, false, ANCIENT",
    "nightborn:stage_reached, false, false, ",
    "nightborn:rank_reached, true, false, MASTER",
    "nightborn:rank_reached, false, false, ",
    "nightborn:eldest_ended, false, true, ELDEST",
    "nightborn:eldest_ended, false, false, ",
    "nightborn:inquisitor_drained, false, false, INQUISITOR",
    "nightborn:fed, true, true, "
  })
  void eachFirstIsMarkedByItsOwnDeed(
      final String kind, final boolean top, final boolean staker, final ServerFirst marked) {
    final Deed deed =
        Deed.of(Identifier.of(kind)).with(DeedKinds.TOP, top).with(DeedKinds.STAKER, staker);
    for (final ServerFirst first : ServerFirst.values()) {
      assertEquals(first == marked, first.marks(deed), first.name());
    }
  }

  @ParameterizedTest(name = "{0} reads {1}")
  @CsvSource({"ANCIENT, nightborn.first.ancient", "INQUISITOR, nightborn.first.inquisitor"})
  void namesItsMessage(final ServerFirst first, final String key) {
    assertEquals(key, first.key());
  }
}
