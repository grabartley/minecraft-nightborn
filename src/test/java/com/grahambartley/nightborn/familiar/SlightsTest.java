package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.FamiliarSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlightsTest {

  private static final FamiliarSettings SETTINGS =
      new FamiliarSettings(60, 1, 7, 5, 15, 25, 10, 25, 30, 8, 32);

  @ParameterizedTest(name = "by owner {0}, bite {1}, {2} ticks since the last: loses {3}")
  @CsvSource({
    "true, true, 100, 25",
    "true, false, 100, 10",
    "true, true, 99, 0",
    "true, false, 0, 0",
    "false, true, 5000, 0",
    "false, false, 5000, 0",
    "true, true, 4294967296, 25"
  })
  @DisplayName("only the owner's bites and blows cost loyalty, at most once per cooling time")
  void loyaltyLost(final boolean byOwner, final boolean bite, final long since, final int lost) {
    assertEquals(lost, Slights.loyaltyLost(byOwner, bite, since, SETTINGS));
  }

  @ParameterizedTest(name = "struck by its owner {0}: strikes back {1}")
  @CsvSource({"true, false", "false, true"})
  @DisplayName("a familiar never strikes back at its owner")
  void retaliates(final boolean byOwner, final boolean strikes) {
    assertEquals(strikes, Slights.retaliates(byOwner));
  }
}
