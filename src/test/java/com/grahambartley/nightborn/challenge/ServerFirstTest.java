package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ServerFirstTest {

  @ParameterizedTest(name = "{0} with top {1} marks {2}")
  @CsvSource({
    "nightborn:stage_reached, true, ANCIENT",
    "nightborn:stage_reached, false, ",
    "nightborn:rank_reached, true, MASTER",
    "nightborn:rank_reached, false, ",
    "nightborn:eldest_ended, false, ELDEST",
    "nightborn:inquisitor_drained, false, INQUISITOR",
    "nightborn:fed, true, "
  })
  void eachFirstIsMarkedByItsOwnDeed(
      final String kind, final boolean top, final ServerFirst marked) {
    final Deed deed = Deed.of(Identifier.of(kind)).with(DeedKinds.TOP, top);
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
