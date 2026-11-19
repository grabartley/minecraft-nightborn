package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.coating.Coating;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class SheenTest {

  @ParameterizedTest(name = "{0} ms at speed {1} over {2}: {3}")
  @CsvSource({
    "0, 1.0, 1000, 0.0",
    "50, 1.0, 1000, 0.4",
    "125, 1.0, 1000, 0.0",
    "50, 0.5, 1000, 0.2",
    "50, 0.0, 1000, 0.0",
    "50, 1.0, 0, 0.0"
  })
  @DisplayName("the sheen drifts with time and the glint speed option, and wraps round")
  void drifts(final long millis, final double speed, final float period, final float drift) {
    assertEquals(drift, Sheen.drift(millis, speed, period), 0.0001F);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("each coating has its own sheen texture")
  void texture(final Coating coating) {
    assertEquals(
        "nightborn:textures/misc/" + coating.asString() + "_coating.png",
        Sheen.of(Optional.of(coating), false, false).get(0).texture().toString());
  }

  @ParameterizedTest(name = "coating {0}, burning {1}, lined {2}: {3}")
  @CsvSource(
      delimiter = ';',
      value = {
        "GARLIC; true; false; GARLIC BLOOD_FIRE",
        "HOLY_WATER; true; false; HOLY_WATER BLOOD_FIRE",
        "; true; false; BLOOD_FIRE",
        "HOLY_WATER; false; false; HOLY_WATER",
        "GARLIC; false; false; GARLIC",
        "; false; false; ",
        "; false; true; MAIL_LINING",
        "GARLIC; true; true; GARLIC BLOOD_FIRE MAIL_LINING"
      })
  @DisplayName(
      "a weapon alight shows its flame on top of any coating, lined clothing shows its mail,"
          + " and a bare piece shows nothing")
  void sheen(
      final Coating coating, final boolean burning, final boolean lined, final String sheens) {
    assertEquals(
        sheens == null ? List.of() : Arrays.stream(sheens.split(" ")).map(Sheen::valueOf).toList(),
        Sheen.of(Optional.ofNullable(coating), burning, lined));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({"HOLY_WATER, false", "GARLIC, false", "BLOOD_FIRE, false", "MAIL_LINING, true"})
  @DisplayName("only the mail lining is worn, so only it is drawn on worn clothing")
  void worn(final Sheen sheen, final boolean worn) {
    assertEquals(worn, sheen.worn());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Sheen.class)
  @DisplayName("every sheen ships its texture")
  void ships(final Sheen sheen) {
    assertTrue(Files.exists(GeoAssets.mainFile(sheen.texture())), sheen.texture().toString());
  }
}
