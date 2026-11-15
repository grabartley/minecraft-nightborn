package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IdleFidgetTest {

  @ParameterizedTest(name = "vampire {0}")
  @ValueSource(ints = {0, 1, 7, 42, -3, 9001})
  @DisplayName("a vampire at rest mostly stands idle and now and then looks round or listens")
  void mostlyIdle(final int seed) {
    final long cycles = 300;
    final long idle =
        LongStream.range(0, IdleFidget.CYCLE_TICKS * cycles)
            .filter(age -> IdleFidget.pose(seed, age) == VampirePose.IDLE)
            .count();
    assertTrue(idle > IdleFidget.CYCLE_TICKS * cycles / 2, "idle ticks: " + idle);
    final Set<VampirePose> seen = EnumSet.noneOf(VampirePose.class);
    LongStream.range(0, IdleFidget.CYCLE_TICKS * cycles)
        .forEach(age -> seen.add(IdleFidget.pose(seed, age)));
    assertEquals(
        EnumSet.of(VampirePose.IDLE, VampirePose.IDLE_LOOK, VampirePose.IDLE_LISTEN), seen);
  }

  @ParameterizedTest(name = "vampire {0}")
  @ValueSource(ints = {0, 5, 77})
  @DisplayName("a fidget runs no longer than its clip, so the vampire never freezes after it")
  void fidgetFitsItsClip(final int seed) {
    int run = 0;
    VampirePose last = VampirePose.IDLE;
    for (long age = 0; age < IdleFidget.CYCLE_TICKS * 200L; age++) {
      final VampirePose pose = IdleFidget.pose(seed, age);
      run = pose == last ? run + 1 : 1;
      last = pose;
      if (pose == VampirePose.IDLE_LOOK) {
        assertTrue(run <= IdleFidget.LOOK_TICKS, "look ran " + run);
      } else if (pose == VampirePose.IDLE_LISTEN) {
        assertTrue(run <= IdleFidget.LISTEN_TICKS, "listen ran " + run);
      }
    }
  }

  @ParameterizedTest(name = "age {0}")
  @ValueSource(longs = {0, 30, 500, 12_345})
  @DisplayName("vampires standing together do not all fidget in step")
  void neighboursDiffer(final long age) {
    final long distinct =
        IntStream.range(0, 12).mapToObj(seed -> IdleFidget.pose(seed, age)).distinct().count();
    assertTrue(distinct > 1, "all twelve vampires shared one pose at " + age);
  }
}
