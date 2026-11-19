package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.coating.Coating;
import java.nio.file.Files;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CoatingSheenTest {

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
    assertEquals(drift, CoatingSheen.drift(millis, speed, period), 0.0001F);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Coating.class)
  @DisplayName("each coating has its own sheen texture")
  void texture(final Coating coating) {
    assertEquals(
        "nightborn:textures/misc/" + coating.asString() + "_coating.png",
        CoatingSheen.of(Optional.of(coating), false).orElseThrow().texture().toString());
  }

  @ParameterizedTest(name = "coating {0}, burning {1}: {2}")
  @CsvSource({
    "GARLIC, true, BLOOD_FIRE",
    ", true, BLOOD_FIRE",
    "HOLY_WATER, false, HOLY_WATER",
    "GARLIC, false, GARLIC",
    ", false, "
  })
  @DisplayName("a weapon alight shows its flame over any coating, and a bare one shows nothing")
  void sheen(final Coating coating, final boolean burning, final CoatingSheen sheen) {
    assertEquals(
        Optional.ofNullable(sheen), CoatingSheen.of(Optional.ofNullable(coating), burning));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoatingSheen.class)
  @DisplayName("every sheen ships its texture")
  void ships(final CoatingSheen sheen) {
    assertTrue(Files.exists(GeoAssets.mainFile(sheen.texture())), sheen.texture().toString());
  }
}
