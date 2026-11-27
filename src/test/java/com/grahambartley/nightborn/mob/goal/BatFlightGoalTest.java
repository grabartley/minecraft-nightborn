package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Intent;
import net.minecraft.entity.mob.MobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BatFlightGoalTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("the goal tells the mob brain it is close in, and nothing else")
  void reportsItsIntent(final Intent intent) {
    final MobEntity mob = mock(MobEntity.class);
    assertEquals(intent == Intent.CLOSE_IN, new BatFlightGoal(mob).intent() == intent);
  }
}
