package com.grahambartley.nightborn.coating;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.CoatingSettings;
import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoatingTermsTest {

  @ParameterizedTest(name = "{0}, silver {1}: {2}")
  @CsvSource({
    "HOLY_WATER, 4, '3, 20, 6000, 20, 60'",
    "GARLIC, 4, '2, 20, 6000, 15, 60'",
    "HOLY_WATER, 3, '2, 20, 6000, 20, 60'",
    "GARLIC, 1, '0, 20, 6000, 15, 60'",
    "HOLY_WATER, 0, '0, 20, 6000, 20, 60'"
  })
  @DisplayName("a coating never hits as hard as silver, and its times are in ticks")
  void staysBelowSilver(final Coating coating, final int silver, final String expected) {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS.with(GearSettings.SILVER_DAMAGE, silver);
    final String[] parts = expected.split(", ");
    assertEquals(
        new CoatingTerms(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2]),
            Integer.parseInt(parts[3]),
            Integer.parseInt(parts[4])),
        CoatingTerms.of(coating, config));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"HOLY_WATER", "GARLIC"})
  @DisplayName("each coating reads its own settings")
  void readsItsOwnSettings(final Coating coating) {
    final NightbornConfig config =
        NightbornConfig.DEFAULTS
            .with(CoatingSettings.HOLY_WATER_HITS, 7)
            .with(CoatingSettings.GARLIC_HITS, 9);
    assertEquals(coating == Coating.HOLY_WATER ? 7 : 9, CoatingTerms.of(coating, config).hits());
  }
}
