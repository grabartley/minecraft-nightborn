package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.entity.EntityPose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeakenedStoopTest {

  @ParameterizedTest(name = "{0}, riding {1}, flying {2}, using {3}: stoops {4}")
  @CsvSource({
    "STANDING, false, false, false, true",
    "CROUCHING, false, false, false, false",
    "SWIMMING, false, false, false, false",
    "SLEEPING, false, false, false, false",
    "FALL_FLYING, false, false, false, false",
    "SPIN_ATTACK, false, false, false, false",
    "DYING, false, false, false, false",
    "STANDING, true, false, false, false",
    "STANDING, false, true, false, false",
    "STANDING, false, false, true, false"
  })
  @DisplayName("the weakened stoop plays only standing on foot with nothing in use")
  void onFoot(
      final EntityPose pose,
      final boolean riding,
      final boolean flying,
      final boolean using,
      final boolean stoops) {
    assertEquals(stoops, WeakenedStoop.onFoot(pose, riding, flying, using));
  }
}
