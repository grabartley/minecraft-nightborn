package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodBarLayoutTest {

  @ParameterizedTest(name = "a bar of {0}: {1} drops, {2} apart")
  @CsvSource({
    "20, 10, 8",
    "0, 10, 8",
    "21, 11, 7",
    "22, 11, 7",
    "24, 12, 6",
    "26, 13, 6",
    "60, 30, 2"
  })
  @DisplayName("a larger bar draws more drops, closer together")
  void lays(final int capacity, final int icons, final int step) {
    assertEquals(new BloodBarLayout(icons, step), BloodBarLayout.of(capacity));
  }

  @ParameterizedTest(name = "a bar of {0}")
  @CsvSource({"20", "22", "24", "26", "40", "60"})
  @DisplayName("the bar starts at the right edge and never grows wider than the hunger bar")
  void keepsItsWidth(final int capacity) {
    final BloodBarLayout layout = BloodBarLayout.of(capacity);
    final int right = 300;
    final int plainLeft = right - (BloodBar.ICONS - 1) * BloodBar.STEP - BloodBar.ICON_SIZE;
    assertEquals(right - BloodBar.ICON_SIZE, layout.left(right, 0));
    assertTrue(layout.left(right, layout.icons() - 1) >= plainLeft);
  }
}
