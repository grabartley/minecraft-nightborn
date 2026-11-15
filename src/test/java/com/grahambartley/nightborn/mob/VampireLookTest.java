package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireLookTest {

  @ParameterizedTest(name = "look {0}: {1} rig, costume {2}")
  @CsvSource({
    "0, MALE, 0, male, male_0",
    "2, MALE, 2, male, male_2",
    "3, FEMALE, 0, female, female_0",
    "5, FEMALE, 2, female, female_2",
    "6, MALE, 0, male, male_0",
    "-1, FEMALE, 2, female, female_2"
  })
  @DisplayName("each look picks a rig and a costume texture made for it")
  void look(
      final int index,
      final VampireLook.Rig rig,
      final int costume,
      final String geo,
      final String texture) {
    final VampireLook look = VampireLook.of(index);
    assertEquals(rig, look.rig());
    assertEquals(costume, look.costume());
    assertEquals("nightborn:geo/entity/vampire_" + geo + ".geo.json", look.geo().toString());
    assertEquals(
        "nightborn:textures/entity/vampire/" + texture + ".png", look.texture().toString());
    assertEquals("nightborn:textures/entity/vampire/" + geo + "_eyes.png", look.eyes().toString());
  }

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-4, 0", "0, 0", "4, 4", "5, 5", "99, 5"})
  @DisplayName("a saved look outside the range is clamped to a real one")
  void clamp(final int given, final int kept) {
    assertEquals(kept, VampireLook.clamp(given));
    assertEquals(kept, VampireLook.of(kept).index());
  }
}
