package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class PartySettingsTest {

  static Stream<Setting<?>> settings() {
    return PartySettings.SETTINGS.stream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("settings")
  @DisplayName("every setting in the section belongs to it and reads its default")
  void ownsItsSettings(final Setting<?> setting) {
    assertEquals(PartySettings.SECTION, setting.section());
    assertEquals(setting.defaultValue(), setting.get(NightbornConfig.DEFAULTS));
  }

  @ParameterizedTest(name = "sizes {0} to {1} are kept as {2} to {3}")
  @CsvSource({"4, 8, 4, 8", "6, 2, 6, 6", "0, 99, 1, 12", "12, 12, 12, 12"})
  @DisplayName("a party is never smaller than its minimum or larger than twelve")
  void clampsPartySize(final int min, final int max, final int keptMin, final int keptMax) {
    final PartySettings settings = new PartySettings(10, 20, min, max, 25, 3, 5, 50, 15, 5);
    assertEquals(keptMin, settings.partyMinSize());
    assertEquals(keptMax, settings.partyMaxSize());
  }

  @ParameterizedTest(name = "at most {0} nights is kept as {1}")
  @CsvSource({"0, 1", "1, 1", "3, 3", "9, 3"})
  @DisplayName("a party camps for one night at least and three at most")
  void clampsNights(final int nights, final int kept) {
    assertEquals(kept, new PartySettings(10, 20, 4, 8, 25, nights, 5, 50, 15, 5).partyMaxNights());
  }

  @ParameterizedTest(name = "a bat chance of {0} is kept as {1}")
  @CsvSource({"-1, 0", "0, 0", "100, 100", "150, 100"})
  @DisplayName("bats can be turned off with zero and set to always with one hundred")
  void clampsBatChance(final int chance, final int kept) {
    assertEquals(kept, new PartySettings(10, 20, 4, 8, 25, 3, 5, 50, chance, 5).campBatChance());
  }
}
