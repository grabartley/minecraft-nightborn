package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClipChannelTest {

  private static ClipChannel linear() {
    return new ClipChannel(
        List.of(
            new ClipChannel.Key(1.0, new Vec3d(10, 0, 0), false),
            new ClipChannel.Key(0.0, new Vec3d(0, 0, 0), false),
            new ClipChannel.Key(2.0, new Vec3d(10, 20, 0), false)));
  }

  @ParameterizedTest(name = "t={0}: x {1}, y {2}")
  @CsvSource({
    "-1, 0, 0",
    "0, 0, 0",
    "0.5, 5, 0",
    "1, 10, 0",
    "1.5, 10, 10",
    "2, 10, 20",
    "9, 10, 20"
  })
  @DisplayName("keys are sorted, held before the first and after the last, and blended between")
  void linearSampling(final double time, final double x, final double y) {
    final Vec3d value = linear().sample(time);
    assertEquals(x, value.x, 1.0E-9);
    assertEquals(y, value.y, 1.0E-9);
  }

  @Test
  @DisplayName("an empty channel rests at zero")
  void emptyRests() {
    assertEquals(Vec3d.ZERO, ClipChannel.EMPTY.sample(3.0));
  }

  @ParameterizedTest(name = "t={0}")
  @CsvSource({"0.0, 0", "1.0, 10", "0.5, 5"})
  @DisplayName("a smooth curve still passes through every key")
  void smoothHitsKeys(final double time, final double x) {
    final ClipChannel smooth =
        new ClipChannel(
            List.of(
                new ClipChannel.Key(0.0, new Vec3d(0, 0, 0), true),
                new ClipChannel.Key(1.0, new Vec3d(10, 0, 0), true)));
    assertEquals(x, smooth.sample(time).x, 1.0E-9);
  }

  @Test
  @DisplayName("a smooth curve eases through a peak rather than turning sharply")
  void smoothEases() {
    final ClipChannel smooth =
        new ClipChannel(
            List.of(
                new ClipChannel.Key(0.0, new Vec3d(0, 0, 0), true),
                new ClipChannel.Key(1.0, new Vec3d(10, 0, 0), true),
                new ClipChannel.Key(2.0, new Vec3d(0, 0, 0), true)));
    final double justBefore = smooth.sample(0.9).x;
    final double justAfter = smooth.sample(1.1).x;
    assertEquals(justBefore, justAfter, 1.0E-9);
  }

  @ParameterizedTest(name = "at least {0}: {1} peaks")
  @CsvSource({"30, 2", "45, 1", "60, 0"})
  @DisplayName("the strongest bends are found as peaks, ignoring the ends")
  void peaks(final double atLeast, final int count) {
    final ClipChannel bend =
        new ClipChannel(
            List.of(
                new ClipChannel.Key(0.0, new Vec3d(10, 0, 0), false),
                new ClipChannel.Key(0.5, new Vec3d(40, 0, 0), false),
                new ClipChannel.Key(0.7, new Vec3d(20, 0, 0), false),
                new ClipChannel.Key(1.0, new Vec3d(-50, 0, 0), false),
                new ClipChannel.Key(1.5, new Vec3d(0, 0, 0), false)));
    assertEquals(count, bend.peakTimesOfX(atLeast).size());
  }
}
