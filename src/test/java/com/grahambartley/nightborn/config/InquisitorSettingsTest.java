package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class InquisitorSettingsTest {

  static Stream<Setting<?>> settings() {
    return InquisitorSettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every fight setting sits in the Grand Inquisitor's section and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(HuntSettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "radius {0} kept as {1}, drain {2} kept as {3}, loss {4} kept as {5}")
  @CsvSource({"14, 14, 5, 5, 50, 50", "2, 8, 0, 2, -10, 0", "99, 48, 99, 30, 150, 100"})
  @DisplayName("every fight setting is clamped into its range on load")
  void clamps(
      final int radius,
      final int keptRadius,
      final int drain,
      final int keptDrain,
      final int loss,
      final int keptLoss) {
    final InquisitorSettings settings =
        new InquisitorSettings(4000, 50, radius, 20, drain, 30, loss, 30, 10, 20, 4, 25);
    assertEquals(keptRadius, settings.arenaRadius());
    assertEquals(keptDrain, settings.drainSeconds());
    assertEquals(keptLoss, settings.deathNotorietyLoss());
  }

  @ParameterizedTest(name = "sword {0}, set {1}, extra {2}, slower {3}")
  @CsvSource({
    "10, 20, 4, 25, 10, 20, 4, 25",
    "-5, -5, -5, -5, 0, 0, 0, 0",
    "150, 150, 99, 99, 100, 100, 20, 80"
  })
  @DisplayName("the greatsword and set amounts are clamped into their ranges on load")
  void clampsTheSet(
      final int sword,
      final int set,
      final int extra,
      final int slower,
      final int keptSword,
      final int keptSet,
      final int keptExtra,
      final int keptSlower) {
    final InquisitorSettings settings =
        new InquisitorSettings(4000, 50, 14, 20, 5, 30, 50, 30, sword, set, extra, slower);
    assertEquals(keptSword, settings.greatswordBlood());
    assertEquals(keptSet, settings.setBlood());
    assertEquals(keptExtra, settings.setExtraBlood());
    assertEquals(keptSlower, settings.setDrainSlower());
  }
}
