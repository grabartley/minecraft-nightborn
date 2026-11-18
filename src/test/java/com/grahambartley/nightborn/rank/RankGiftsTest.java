package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.SkillSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankGiftsTest {

  static SkillSettings settings(final int health, final int damageTenths) {
    return new SkillSettings(100, 3, 5, 100, 300, 200, 10, 500, 5, 20, 45, health, damageTenths);
  }

  @ParameterizedTest(name = "rank {0}, {1} health, {2} tenths -> {3} health, {4} damage")
  @CsvSource({
    "0, 2, 5, 0.0, 0.0",
    "1, 2, 5, 2.0, 0.5",
    "2, 2, 5, 4.0, 1.0",
    "3, 2, 5, 6.0, 1.5",
    "3, 0, 0, 0.0, 0.0",
    "-1, 2, 5, 0.0, 0.0"
  })
  @DisplayName("each rank above Initiate gives more health and more damage against vampires")
  void perRank(
      final int rank,
      final int health,
      final int tenths,
      final double expectedHealth,
      final float expectedDamage) {
    final RankGifts gifts = RankGifts.of(rank, settings(health, tenths));
    assertEquals(expectedHealth, gifts.health());
    assertEquals(expectedDamage, gifts.against(true));
  }

  @ParameterizedTest(name = "rank {0}")
  @CsvSource({"0", "1", "3"})
  @DisplayName("a rank adds nothing against anyone but a vampire")
  void nothingAgainstOthers(final int rank) {
    assertEquals(0.0F, RankGifts.of(rank, settings(2, 5)).against(false));
  }
}
