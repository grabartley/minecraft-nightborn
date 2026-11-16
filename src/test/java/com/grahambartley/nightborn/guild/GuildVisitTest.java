package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuildVisitTest {

  @ParameterizedTest(name = "vampire {0}, finished contract {1}: {2}")
  @CsvSource({
    "true, false, REFUSE",
    "true, true, REFUSE",
    "false, true, CLAIM",
    "false, false, TRADE"
  })
  void vampiresAreRefusedAndHumansClaimOrTrade(
      final boolean vampire, final boolean finished, final GuildVisit visit) {
    assertEquals(visit, GuildVisit.of(vampire, finished));
  }
}
