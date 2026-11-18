package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class VillageSettingsTest {

  static Stream<Setting<?>> settings() {
    return VillageSettings.SETTINGS.stream();
  }

  private static VillageSettings steps(final int wary, final int warded, final int hunted) {
    return new VillageSettings(wary, warded, hunted, 2, 5, 2, 3, 10, 25, 48);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(VillageSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "wary {0}, warded {1}, hunted {2} kept as {3}, {4}, {5}")
  @CsvSource({
    "25, 50, 75, 25, 50, 75",
    "60, 50, 75, 60, 60, 75",
    "25, 80, 75, 25, 80, 80",
    "0, 0, 0, 1, 1, 1",
    "200, 200, 200, 100, 100, 100"
  })
  @DisplayName("each step begins no lower than the one before it, and all stay in 1 to 100")
  void ordersSteps(
      final int wary,
      final int warded,
      final int hunted,
      final int keptWary,
      final int keptWarded,
      final int keptHunted) {
    final VillageSettings settings = steps(wary, warded, hunted);
    assertEquals(keptWary, settings.waryAt());
    assertEquals(keptWarded, settings.wardedAt());
    assertEquals(keptHunted, settings.huntedAt());
  }

  @ParameterizedTest(name = "reach {0} kept as {1}")
  @CsvSource({"0, 16", "48, 48", "500, 128"})
  @DisplayName("a village reaches between 16 and 128 blocks from its bell")
  void clampsReach(final int reach, final int kept) {
    assertEquals(kept, new VillageSettings(25, 50, 75, 2, 5, 2, 3, 10, 25, reach).villageReach());
  }
}
