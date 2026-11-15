package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.entity.EntityPose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeakenedStoopTest {

  @ParameterizedTest(name = "{0}, on ground {1}, riding {2}, using {3}: stoops {4}")
  @CsvSource({
    "STANDING, true, false, false, true",
    "CROUCHING, true, false, false, false",
    "SWIMMING, true, false, false, false",
    "SLEEPING, true, false, false, false",
    "FALL_FLYING, false, false, false, false",
    "SPIN_ATTACK, false, false, false, false",
    "DYING, true, false, false, false",
    "STANDING, false, false, false, false",
    "STANDING, true, true, false, false",
    "STANDING, true, false, true, false"
  })
  @DisplayName("the weakened stoop plays only standing on the ground with nothing in use")
  void onFoot(
      final EntityPose pose,
      final boolean onGround,
      final boolean riding,
      final boolean using,
      final boolean stoops) {
    assertEquals(stoops, WeakenedStoop.onFoot(pose, onGround, riding, using));
  }
}
