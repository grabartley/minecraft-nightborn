package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class GuildSettingsTest {

  static Stream<Setting<?>> settings() {
    return GuildSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(GuildSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "rank {0}, price {1}, wait {2}s, shelter {3}s -> {4}, {5}, {6}s, {7}s")
  @CsvSource({
    "1, 16, 1200, 3, 1, 16, 1200, 3",
    "-1, -5, -5, -1, 0, 0, 0, 0",
    "9, 999, 99999, 99, 3, 64, 7200, 30",
    "0, 0, 0, 30, 0, 0, 0, 30"
  })
  void clampsToItsBounds(
      final int rank,
      final int price,
      final int wait,
      final int shelter,
      final int keptRank,
      final int keptPrice,
      final int keptWait,
      final int keptShelter) {
    final GuildSettings settings = new GuildSettings(rank > 0, rank, price, wait, shelter);
    assertEquals(rank > 0, settings.riteNeedsOath());
    assertEquals(keptRank, settings.riteMinRank());
    assertEquals(keptPrice, settings.ritePrice());
    assertEquals(keptWait, settings.riteCooldownSeconds());
    assertEquals(keptShelter, settings.riteShieldSeconds());
  }
}
