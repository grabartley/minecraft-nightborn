package com.grahambartley.nightborn.vomit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.WardSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GarlicMealTest {

  private static final WardSettings WARDS = NightbornConfig.DEFAULTS.wards();

  @ParameterizedTest(name = "other food with {0} blood costs only the vomiting")
  @CsvSource({"0", "10", "20"})
  @DisplayName("food that is not garlic takes nothing beyond what vomiting already does")
  void otherFoodAddsNothing(final int blood) {
    assertEquals(new GarlicMeal.Toll(4, 120, 0, 0, blood), GarlicMeal.Toll.plain(4, 120, blood));
  }

  @ParameterizedTest(name = "garlic with {0} blood leaves {1}")
  @CsvSource({"20, 14", "6, 0", "3, 0", "0, 0"})
  @DisplayName("garlic adds its damage, triples the nausea, weakens like a touch and costs blood")
  void garlicIsTheWorstMeal(final int blood, final int left) {
    assertEquals(
        new GarlicMeal.Toll(
            4 + WARDS.eatenDamage(), 360, WARDS.touchStrength(), WARDS.eatenSeconds() * 20, left),
        GarlicMeal.worsen(GarlicMeal.Toll.plain(4, 120, blood), WARDS));
  }
}
