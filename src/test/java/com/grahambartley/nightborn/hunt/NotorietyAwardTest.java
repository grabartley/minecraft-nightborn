package com.grahambartley.nightborn.hunt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NotorietyAwardTest {

  static final UUID VAMPIRE = UUID.fromString("00000000-0000-0000-0000-00000000000a");
  static final UUID MASTER = UUID.fromString("00000000-0000-0000-0000-00000000000b");

  static Optional<UUID> of(final String who) {
    return switch (who) {
      case "vampire" -> Optional.of(VAMPIRE);
      case "master" -> Optional.of(MASTER);
      default -> Optional.empty();
    };
  }

  @ParameterizedTest(name = "killer {0}, killer's master {1} -> {2}")
  @CsvSource({
    "vampire, none, vampire",
    "none, master, master",
    "none, none, none",
    "vampire, master, vampire"
  })
  @DisplayName("a kill counts for the vampire who struck, or for the master of the servant who did")
  void credits(final String killer, final String master, final String credited) {
    assertEquals(of(credited), NotorietyAward.credited(of(killer), of(master)));
  }
}
