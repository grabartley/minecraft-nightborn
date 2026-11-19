package com.grahambartley.nightborn.client.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParasolScorchTest {

  @ParameterizedTest(name = "{0} of {1} worn: {2}")
  @CsvSource({
    "0, 180, fresh",
    "62, 180, fresh",
    "63, 180, singed",
    "116, 180, singed",
    "117, 180, scorched",
    "161, 180, scorched",
    "162, 180, charred",
    "179, 180, charred",
    "5, 0, fresh"
  })
  @DisplayName("the canopy looks more scorched the more of its sun it has used")
  void texture(final int damage, final int maxDamage, final String stage) {
    assertEquals(
        "nightborn:textures/entity/parasol/" + stage + ".png",
        ParasolScorch.texture(damage, maxDamage).toString());
  }
}
