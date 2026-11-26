package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClipTrackTest {

  static final List<ClipTrack.Segment> RAISE_THEN_DROP =
      List.of(
          new ClipTrack.Segment(0.0, 0.0, 1.0),
          new ClipTrack.Segment(4.0, 1.0, 2.0),
          new ClipTrack.Segment(6.0, 2.0, 0.0));

  @ParameterizedTest(name = "tick {0}: {1}")
  @CsvSource({"0, 1.0", "2, 1.5", "4, 2.0", "7, 1.0", "10, 0.0", "30, 0.0", "-3, 1.0"})
  @DisplayName("a track blends each key into the next and holds its last value after the end")
  void samples(final double ticks, final double expected) {
    assertEquals(expected, ClipTrack.at(RAISE_THEN_DROP, ticks), 1e-9);
  }

  @Test
  @DisplayName("an empty track rests at zero")
  void empty() {
    assertEquals(0.0, ClipTrack.at(List.of(), 5.0));
  }
}
