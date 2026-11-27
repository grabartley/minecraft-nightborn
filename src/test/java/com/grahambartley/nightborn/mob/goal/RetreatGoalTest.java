package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Intent;
import net.minecraft.entity.mob.PathAwareEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class RetreatGoalTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("the goal tells the mob brain it is retreat, and nothing else")
  void reportsItsIntent(final Intent intent) {
    final PathAwareEntity mob = mock(PathAwareEntity.class);
    assertEquals(intent == Intent.RETREAT, new RetreatGoal(mob, () -> 0).intent() == intent);
  }
}
