package com.grahambartley.nightborn.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TitlesTest {

  static final Identifier FEAT = Identifier.of("nightborn", "the_cabinet");

  @ParameterizedTest(
      name = "earned as vampire {0}, bearer vampire {1}, viewer vampire {2}, shown {3}: {4}")
  @CsvSource({
    "false, false, false, false, true",
    "false, true, false, false, true",
    "true, true, true, false, true",
    "true, true, false, false, false",
    "true, true, false, true, true",
    "true, false, true, true, false"
  })
  void aVampireTitleIsSeenOnlyByThoseWhoSeeTheStageColour(
      final boolean asVampire,
      final boolean bearer,
      final boolean viewer,
      final boolean factions,
      final boolean seen) {
    final FeatLog log = FeatLog.EMPTY.earning(FEAT, asVampire).wearing(Optional.of(FEAT));
    assertEquals(
        seen ? Optional.of(FEAT) : Optional.empty(), Titles.shown(log, bearer, viewer, factions));
  }

  @ParameterizedTest(name = "nothing worn shows nothing ({0})")
  @CsvSource({"true", "false"})
  void nothingWornShowsNothing(final boolean viewer) {
    assertEquals(Optional.empty(), Titles.shown(FeatLog.EMPTY, true, viewer, true));
  }
}
