package com.grahambartley.nightborn.clothing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClothingMotionTest {

  @ParameterizedTest(name = "limb speed {0}: {1}")
  @CsvSource({
    "0.0, IDLE, idle",
    "0.1, IDLE, idle",
    "0.11, WALK, walk",
    "0.6, WALK, walk",
    "1.5, WALK, walk"
  })
  @DisplayName("clothing sways while its wearer walks and hangs still otherwise")
  void picksTheClip(final float limbSpeed, final ClothingMotion motion, final String clip) {
    assertEquals(motion, ClothingMotion.of(limbSpeed));
    assertEquals(clip, ClothingMotion.of(limbSpeed).clip());
  }
}
