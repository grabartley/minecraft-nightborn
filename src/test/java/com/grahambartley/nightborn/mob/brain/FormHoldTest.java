package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormHoldTest {

  @ParameterizedTest(name = "taken at 100 for {0}, now {1}: holds {2}")
  @CsvSource({"40, 100, true", "40, 139, true", "40, 140, false", "0, 100, false"})
  @DisplayName("a form is held from when it was taken until its shortest hold has passed")
  void holds(final int holdTicks, final long now, final boolean holds) {
    final FormHold hold = new FormHold();
    hold.take(100, holdTicks, null);
    assertEquals(holds, hold.holds(now));
  }

  @Test
  @DisplayName("nothing is held before a form is taken")
  void nothingHeld() {
    assertEquals(false, new FormHold().holds(0));
    assertEquals(Optional.empty(), new FormHold().course());
  }

  @Test
  @DisplayName("the course is kept until the mob arrives, and a new form takes a new course")
  void course() {
    final FormHold hold = new FormHold();
    final Vec3d far = new Vec3d(30, 64, 4);
    hold.take(0, 40, far);
    assertEquals(Optional.of(far), hold.course());
    hold.arrive();
    assertEquals(Optional.empty(), hold.course());
    final Vec3d home = new Vec3d(-8, 70, 2);
    hold.take(200, 40, home);
    assertEquals(Optional.of(home), hold.course());
  }
}
