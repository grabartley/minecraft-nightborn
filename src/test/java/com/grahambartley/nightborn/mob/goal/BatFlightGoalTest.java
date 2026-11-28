package com.grahambartley.nightborn.mob.goal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.grahambartley.nightborn.mob.brain.Intent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class BatFlightGoalTest {

  @ParameterizedTest(name = "target {0}: {1}")
  @CsvSource({"true, CLOSE_IN", "false, TRAVEL"})
  @DisplayName("a bat closes in on its target, and travels when it has none")
  void reportsItsIntent(final boolean hasTarget, final Intent intent) {
    final MobEntity mob = mock(MobEntity.class);
    when(mob.getTarget()).thenReturn(hasTarget ? mock(LivingEntity.class) : null);
    assertEquals(intent, new BatFlightGoal(mob).intent());
  }
}
