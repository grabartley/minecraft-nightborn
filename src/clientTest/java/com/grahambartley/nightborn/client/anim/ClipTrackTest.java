package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.math.value.Constant;

class ClipTrackTest {

  private static Keyframe<MathValue> key(
      final double ticks, final double start, final double end, final EasingType easing) {
    return new Keyframe<>(ticks, new Constant(start), new Constant(end), easing);
  }

  static final List<Keyframe<MathValue>> RAISE_THEN_DROP =
      List.of(
          key(0.0, 1.0, 1.0, EasingType.LINEAR),
          key(4.0, 1.0, 2.0, EasingType.LINEAR),
          key(6.0, 2.0, 0.0, EasingType.LINEAR));

  @ParameterizedTest(name = "tick {0}: {1}")
  @CsvSource({"0, 1.0", "2, 1.5", "4, 2.0", "7, 1.0", "10, 0.0", "30, 0.0", "-3, 1.0"})
  @DisplayName("a track blends each key into the next and holds its last value after the end")
  void samples(final double ticks, final double expected) {
    assertEquals(expected, ClipTrack.at(RAISE_THEN_DROP, ticks), 1e-9);
  }

  @ParameterizedTest(name = "tick {0}: {1}")
  @CsvSource({"0, 0.0", "2, 1.0", "4, 4.0"})
  @DisplayName("a key's own easing shapes the blend, as it does on the model")
  void eases(final double ticks, final double expected) {
    assertEquals(
        expected, ClipTrack.at(List.of(key(4.0, 0.0, 4.0, EasingType.EASE_IN_QUAD)), ticks), 1e-9);
  }

  @Test
  @DisplayName("an empty track rests at zero")
  void empty() {
    assertEquals(0.0, ClipTrack.at(List.of(), 5.0));
  }
}
