package com.grahambartley.nightborn.client.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.client.render.model.json.ModelTransformationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ParasolHeldModelTest {

  @ParameterizedTest(name = "{0}: 3D {1}")
  @CsvSource({
    "NONE, true",
    "THIRD_PERSON_LEFT_HAND, true",
    "THIRD_PERSON_RIGHT_HAND, true",
    "FIRST_PERSON_LEFT_HAND, true",
    "FIRST_PERSON_RIGHT_HAND, true",
    "HEAD, true",
    "GUI, false",
    "GROUND, false",
    "FIXED, false"
  })
  @DisplayName(
      "the parasol is a model when held and a flat sprite in slots, frames and on the ground")
  void drawsInHand(final ModelTransformationMode mode, final boolean model) {
    assertEquals(model, ParasolHeldModel.drawsInHand(mode));
  }
}
