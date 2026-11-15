package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingMotionTest {

  @ParameterizedTest(name = "limb speed {0}: {1}")
  @CsvSource({"0.0, idle", "0.1, idle", "0.11, walk", "0.6, walk", "1.5, walk"})
  @DisplayName("clothing sways while its wearer walks and hangs still otherwise")
  void picksTheClip(final float limbSpeed, final String clip) {
    assertEquals(clip, ClothingMotion.clip(limbSpeed));
  }
}
