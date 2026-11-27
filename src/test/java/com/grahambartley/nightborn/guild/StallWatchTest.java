package com.grahambartley.nightborn.guild;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StallWatchTest {

  @ParameterizedTest(name = "fighting {0}, idle {1}, wanders {2}: gives up {3}")
  @CsvSource({
    "false, false, true, true",
    "false, false, false, true",
    "true, false, true, false",
    "false, true, true, false"
  })
  @DisplayName("a stalled walk is dropped and the mob wanders on, never mid fight or when idle")
  void stalledWalkIsDropped(
      final boolean fighting, final boolean idle, final boolean wanders, final boolean givesUp) {
    final EntityNavigation navigation = mock(EntityNavigation.class);
    when(navigation.isIdle()).thenReturn(idle);
    when(navigation.getTargetPos()).thenReturn(new BlockPos(10, 64, 0));
    final PathAwareEntity mob = mock(PathAwareEntity.class);
    when(mob.getNavigation()).thenReturn(navigation);
    when(mob.getPos()).thenReturn(new Vec3d(0.5, 64, 0.5));
    when(mob.getTarget()).thenReturn(fighting ? mock(LivingEntity.class) : null);
    final WanderAroundGoal wander = mock(WanderAroundGoal.class);
    final StallWatch watch = new StallWatch();
    for (int tick = 0; tick <= StallCount.GIVE_UP_TICKS; tick++) {
      watch.watch(mob, wanders ? wander : null);
    }
    verify(navigation, times(givesUp ? 1 : 0)).stop();
    verify(wander, times(givesUp && wanders ? 1 : 0)).ignoreChanceOnce();
    if (!wanders) {
      verify(wander, never()).ignoreChanceOnce();
    }
  }
}
