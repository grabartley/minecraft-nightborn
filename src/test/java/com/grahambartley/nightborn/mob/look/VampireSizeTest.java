package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.entity.EntityDimensions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireSizeTest {

  @ParameterizedTest(name = "{0} tall, {1} wide: hitbox {2} x {3}, eyes at {4}")
  @CsvSource({
    "1.95, 1.0, 0.6, 1.95, 1.7",
    "1.5, 1.0, 0.4615, 1.5, 1.3077",
    "2.0, 0.88, 0.5415, 2.0, 1.7436",
    "1.6, 1.14, 0.5612, 1.6, 1.3949",
    "9.0, 9.0, 1.3846, 3.0, 2.6154",
    "0.1, 0.1, 0.1538, 1.0, 0.8718"
  })
  @DisplayName("the hitbox and eye height follow the vampire's height and build, within limits")
  void dimensions(
      final float height,
      final float width,
      final float hitWidth,
      final float hitHeight,
      final float eyes) {
    final EntityDimensions base = EntityDimensions.changing(0.6F, 1.95F).withEyeHeight(1.7F);
    final EntityDimensions sized = VampireSize.dimensions(base, height, width);
    assertEquals(hitWidth, sized.width(), 1.0E-3F);
    assertEquals(hitHeight, sized.height(), 1.0E-3F);
    assertEquals(eyes, sized.eyeHeight(), 1.0E-3F);
  }

  @ParameterizedTest(name = "{0} tall draws at {1}")
  @CsvSource({"1.95, 1.0", "1.5, 0.7692", "2.0, 1.0256"})
  @DisplayName("the model is drawn at the same scale as the hitbox")
  void scale(final float height, final float scale) {
    assertEquals(scale, VampireSize.scale(height), 1.0E-3F);
  }
}
