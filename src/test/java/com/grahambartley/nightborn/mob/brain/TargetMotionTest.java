package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TargetMotionTest {

  @ParameterizedTest(name = "from {0} at {1} to x 10 z 4 at {2}, thinks every {3}: {4}, {5} a tick")
  @CsvSource({
    "true, 100, 120, 20, 0.5, 0.2",
    "true, 100, 101, 20, 10, 4",
    "true, 100, 200, 100, 0.1, 0.04",
    "true, 100, 300, 100, 0.05, 0.02",
    "true, 100, 170, 70, 0.1428571429, 0.0571428571",
    "true, 100, 241, 20, 0, 0",
    "true, 100, 100, 20, 0, 0",
    "false, 100, 120, 20, 0, 0"
  })
  @DisplayName(
      "a target's motion is its flat movement per tick since the last sample, and none when"
          + " there is no sample fresh for that mob's think interval")
  void perTick(
      final boolean sampled,
      final long fromTick,
      final long toTick,
      final int thinkTicks,
      final double x,
      final double z) {
    final Vec3d moved =
        TargetMotion.perTick(
            sampled ? new Vec3d(0, 64, 0) : null,
            fromTick,
            new Vec3d(10, 70, 4),
            toTick,
            TargetMotion.staleAfter(thinkTicks));
    assertEquals(x, moved.x, 1.0E-6);
    assertEquals(0.0, moved.y, 1.0E-6);
    assertEquals(z, moved.z, 1.0E-6);
  }

  @ParameterizedTest(name = "thinks every {0}: stale after {1}")
  @CsvSource({"12, 124", "20, 140", "70, 240", "100, 300"})
  @DisplayName("a sample goes stale after two thinks and the longest tell")
  void staleAfter(final int thinkTicks, final long stale) {
    assertEquals(stale, TargetMotion.staleAfter(thinkTicks));
  }

  @Test
  @DisplayName("motion is measured on the same target only, and starts again on a new one")
  void samplesOneTarget() {
    final LivingEntity first = target(UUID.randomUUID(), new Vec3d(0, 64, 0));
    final LivingEntity second = target(UUID.randomUUID(), new Vec3d(5, 64, 0));
    final TargetMotion motion = new TargetMotion();
    assertEquals(Vec3d.ZERO, motion.sample(first, 100, 20));
    when(first.getPos()).thenReturn(new Vec3d(4, 64, 0));
    assertEquals(new Vec3d(0.2, 0, 0), motion.sample(first, 120, 20));
    assertEquals(Vec3d.ZERO, motion.sample(second, 140, 20));
  }

  private static LivingEntity target(final UUID id, final Vec3d pos) {
    final LivingEntity entity = mock(LivingEntity.class);
    when(entity.getUuid()).thenReturn(id);
    when(entity.getPos()).thenReturn(pos);
    return entity;
  }
}
