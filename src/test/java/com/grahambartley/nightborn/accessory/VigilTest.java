package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VigilTest {

  private static final UUID ONE = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID TWO = UUID.fromString("00000000-0000-0000-0000-000000000002");

  @ParameterizedTest(name = "vampire {0}, player {1}, alive {2}, asleep {3}: {4}")
  @CsvSource({
    "true, false, true, false, true",
    "true, true, true, false, false",
    "true, false, true, true, false",
    "true, false, false, false, false",
    "false, false, true, false, false"
  })
  @DisplayName("only an awake vampire mob sets the ring off, never a vampire player")
  void awake(
      final boolean vampire,
      final boolean player,
      final boolean alive,
      final boolean sleeping,
      final boolean felt) {
    assertEquals(felt, Vigil.awakeVampireMob(vampire, player, alive, sleeping));
  }

  @ParameterizedTest(name = "near {0}, felt {1}: {2}")
  @CsvSource({"1, '', true", "1, 1, false", "'1,2', 1, true", "'', 1, false", "'', '', false"})
  @DisplayName("the chill comes when a vampire comes near, not every second it stays")
  void newcomer(final String near, final String felt, final boolean chills) {
    assertEquals(chills, Vigil.newcomer(ids(near), ids(felt)));
  }

  private static Set<UUID> ids(final String list) {
    if (list == null || list.isBlank()) {
      return Set.of();
    }
    return java.util.Arrays.stream(list.split(","))
        .map(id -> id.equals("1") ? ONE : TWO)
        .collect(java.util.stream.Collectors.toSet());
  }
}
