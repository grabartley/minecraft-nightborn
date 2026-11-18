package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VillageVictimsTest {

  @ParameterizedTest(name = "villager {0}, animal {1}, survived {2}: {3}")
  @CsvSource({
    "true, false, true, BITTEN_VILLAGER",
    "true, false, false, MISSING_VILLAGER",
    "false, true, false, DRAINED_ANIMAL",
    "false, true, true, ",
    "false, false, false, ",
    "false, false, true, "
  })
  @DisplayName("a bitten villager, a missing villager and a drained animal are victims found")
  void findsVictims(
      final boolean villager,
      final boolean animal,
      final boolean survived,
      final SuspicionCause cause) {
    assertEquals(Optional.ofNullable(cause), VillageVictims.found(villager, animal, survived));
  }
}
