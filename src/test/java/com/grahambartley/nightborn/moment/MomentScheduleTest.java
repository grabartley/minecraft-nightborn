package com.grahambartley.nightborn.moment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class MomentScheduleTest {

  static Stream<Arguments> beats() {
    return Stream.of(
        Arguments.of(Moment.TURNING, 0, List.of(MomentCue.HEARTBEAT)),
        Arguments.of(Moment.TURNING, 20, List.of(MomentCue.HEARTBEAT)),
        Arguments.of(Moment.TURNING, 45, List.of(MomentCue.HEARTBEAT)),
        Arguments.of(Moment.TURNING, 46, List.of()),
        Arguments.of(Moment.TURNING, 75, List.of(MomentCue.BELL)),
        Arguments.of(Moment.TURNING, 100, List.of(MomentCue.BELL)),
        Arguments.of(Moment.OATH, 0, List.of()),
        Arguments.of(Moment.OATH, 50, List.of(MomentCue.HORN)),
        Arguments.of(Moment.OATH, 100, List.of(MomentCue.HORN)));
  }

  @ParameterizedTest(name = "{0} at tick {1} -> {2}")
  @MethodSource("beats")
  @DisplayName("a turning's heart beats and stops before the bell, and an oath ends on the horn")
  void sounding(final Moment moment, final int tick, final List<MomentCue> cues) {
    assertEquals(cues, MomentSchedule.sounding(moment, tick, 100));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Moment.class)
  @DisplayName("every beat falls inside the camera, however long the server makes it")
  void beatsFitTheCamera(final Moment moment) {
    for (final int camera : new int[] {20, 100, 400}) {
      for (final MomentSchedule.Beat beat : MomentSchedule.of(moment)) {
        final int tick = beat.tick(camera);
        assertEquals(true, tick >= 0 && tick <= camera, beat + " at " + camera);
      }
    }
  }
}
