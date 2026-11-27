package com.grahambartley.nightborn.guild;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StallWatchTest {

  private static final BlockPos TARGET = new BlockPos(10, 64, 0);

  private static int ticksUntilGiveUp(final StallWatch watch, final double step) {
    double distance = 100.0;
    for (int tick = 0; distance > 0.0; tick++) {
      if (watch.gaveUp(TARGET, distance)) {
        return tick;
      }
      distance = Math.max(0.0, distance - step);
    }
    return -1;
  }

  @ParameterizedTest(name = "closing {0} a tick")
  @ValueSource(doubles = {0.0, 0.001, 0.003})
  @DisplayName("a mob that gets no closer gives up after three seconds")
  void givesUpWhenNoCloser(final double step) {
    assertEquals(StallWatch.GIVE_UP_TICKS, ticksUntilGiveUp(new StallWatch(), step));
  }

  @ParameterizedTest(name = "closing {0} a tick")
  @ValueSource(doubles = {0.05, 0.1, 0.2})
  @DisplayName("a mob that keeps closing never gives up")
  void keepsGoingWhileClosing(final double step) {
    assertEquals(-1, ticksUntilGiveUp(new StallWatch(), step));
  }

  @ParameterizedTest(name = "stalled {0} ticks, then new target")
  @CsvSource({"30", "59"})
  @DisplayName("a new target starts the count again")
  void newTargetRestartsCount(final int stalled) {
    final StallWatch watch = new StallWatch();
    for (int tick = 0; tick <= stalled; tick++) {
      assertFalse(watch.gaveUp(TARGET, 5.0));
    }
    for (int tick = 0; tick < StallWatch.GIVE_UP_TICKS; tick++) {
      assertFalse(watch.gaveUp(TARGET.east(), 5.0));
    }
    assertTrue(watch.gaveUp(TARGET.east(), 5.0));
  }

  @Test
  @DisplayName("standing idle never counts as stalled")
  void idleNeverStalls() {
    final StallWatch watch = new StallWatch();
    for (int tick = 0; tick < StallWatch.GIVE_UP_TICKS * 3; tick++) {
      assertFalse(watch.gaveUp(null, 0.0));
    }
  }

  private static PathAwareEntity mob(final EntityNavigation navigation, final boolean fighting) {
    final PathAwareEntity mob = mock(PathAwareEntity.class);
    when(mob.getNavigation()).thenReturn(navigation);
    when(mob.getPos()).thenReturn(new Vec3d(0.5, 64, 0.5));
    when(mob.getTarget()).thenReturn(fighting ? mock(LivingEntity.class) : null);
    return mob;
  }

  @ParameterizedTest(name = "fighting {0}: gives up {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("a stalled walk is dropped and the mob wanders on, but never mid fight")
  void watchStopsAStalledWalk(final boolean fighting, final boolean givesUp) {
    final EntityNavigation navigation = mock(EntityNavigation.class);
    when(navigation.isIdle()).thenReturn(false);
    when(navigation.getTargetPos()).thenReturn(TARGET);
    final WanderAroundGoal wander = mock(WanderAroundGoal.class);
    final PathAwareEntity mob = mob(navigation, fighting);
    final StallWatch watch = new StallWatch();
    for (int tick = 0; tick <= StallWatch.GIVE_UP_TICKS; tick++) {
      watch.watch(mob, wander);
    }
    if (givesUp) {
      verify(navigation).stop();
      verify(wander).ignoreChanceOnce();
    } else {
      verify(navigation, never()).stop();
      verify(wander, never()).ignoreChanceOnce();
    }
  }
}
