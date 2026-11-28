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
class WalkHomeGoalTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(Intent.class)
  @DisplayName("walking back to its home or camp tells the mob brain it is travelling")
  void reportsItsIntent(final Intent intent) {
    assertEquals(
        intent == Intent.TRAVEL,
        new WalkHomeGoal(mock(PathAwareEntity.class), 1.0).intent() == intent);
  }
}
