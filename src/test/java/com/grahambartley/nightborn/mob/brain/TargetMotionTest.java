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

  @ParameterizedTest(name = "from {0} at {1} to x 10 z 4 at {2}: {3}, {4} a tick")
  @CsvSource({
    "true, 100, 120, 0.5, 0.2",
    "true, 100, 101, 10, 4",
    "true, 100, 160, 0.1666666667, 0.0666666667",
    "true, 100, 161, 0, 0",
    "true, 100, 100, 0, 0",
    "false, 100, 120, 0, 0"
  })
  @DisplayName(
      "a target's motion is its flat movement per tick since the last sample, and none when"
          + " there is no fresh sample")
  void perTick(
      final boolean sampled,
      final long fromTick,
      final long toTick,
      final double x,
      final double z) {
    final Vec3d moved =
        TargetMotion.perTick(
            sampled ? new Vec3d(0, 64, 0) : null, fromTick, new Vec3d(10, 70, 4), toTick);
    assertEquals(x, moved.x, 1.0E-6);
    assertEquals(0.0, moved.y, 1.0E-6);
    assertEquals(z, moved.z, 1.0E-6);
  }

  @Test
  @DisplayName("motion is measured on the same target only, and starts again on a new one")
  void samplesOneTarget() {
    final LivingEntity first = target(UUID.randomUUID(), new Vec3d(0, 64, 0));
    final LivingEntity second = target(UUID.randomUUID(), new Vec3d(5, 64, 0));
    final TargetMotion motion = new TargetMotion();
    assertEquals(Vec3d.ZERO, motion.sample(first, 100));
    when(first.getPos()).thenReturn(new Vec3d(4, 64, 0));
    assertEquals(new Vec3d(0.2, 0, 0), motion.sample(first, 120));
    assertEquals(Vec3d.ZERO, motion.sample(second, 140));
  }

  private static LivingEntity target(final UUID id, final Vec3d pos) {
    final LivingEntity entity = mock(LivingEntity.class);
    when(entity.getUuid()).thenReturn(id);
    when(entity.getPos()).thenReturn(pos);
    return entity;
  }
}
