package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AnimationClipTest {

  private static AnimationClip clip(final boolean loops) {
    return new AnimationClip(
        "bend",
        2.0,
        loops,
        Map.of(
            "body",
            new AnimationClip.Track(
                new ClipChannel(
                    List.of(
                        new ClipChannel.Key(0.0, Vec3d.ZERO, false),
                        new ClipChannel.Key(1.0, new Vec3d(40, 0, 0), false),
                        new ClipChannel.Key(2.0, Vec3d.ZERO, false))),
                ClipChannel.EMPTY)));
  }

  @ParameterizedTest(name = "looping {0}, {1} s: {2}")
  @CsvSource({
    "true, 0.5, 20", "true, 2.5, 20", "true, -0.5, 20",
    "false, 0.5, 20", "false, 2.5, 0", "false, -1, 0"
  })
  @DisplayName("a looping clip wraps and a one-shot clip holds its ends")
  void samples(final boolean loops, final double seconds, final double bend) {
    assertEquals(bend, clip(loops).sample(seconds).bone("body").rotation().x, 1.0E-9);
  }

  @ParameterizedTest(name = "looping {0} at {1} s: finished {2}")
  @CsvSource({"false, 1.9, false", "false, 2.0, true", "true, 99, false"})
  @DisplayName("only a one-shot clip finishes")
  void finishes(final boolean loops, final double seconds, final boolean finished) {
    assertEquals(finished, clip(loops).finishedAt(seconds));
  }

  @Test
  @DisplayName("peaks are looked up on the named bone, and an unknown bone has none")
  void peaks() {
    assertEquals(List.of(1.0), clip(false).peaksOf("body", 30));
    assertEquals(List.of(), clip(false).peaksOf("tail", 30));
  }
}
