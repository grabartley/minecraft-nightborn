package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodBarIconsTest {

  @ParameterizedTest(name = "icon {0} at blood {1}: {2}")
  @CsvSource({
    "0, 0, empty",
    "0, 1, half",
    "0, 2, full",
    "4, 9, half",
    "4, 10, full",
    "4, 8, empty",
    "9, 19, half",
    "9, 20, full",
    "9, 18, empty"
  })
  @DisplayName("each drop holds two blood, drawn like vanilla's hunger shanks")
  void fills(final int icon, final int blood, final String expected) {
    final Identifier fill = BloodBarIcons.fill(icon, blood);
    final String shown = fill == null ? "empty" : fill.getPath().replace("hud/blood_", "");
    assertEquals(expected, shown);
  }
}
