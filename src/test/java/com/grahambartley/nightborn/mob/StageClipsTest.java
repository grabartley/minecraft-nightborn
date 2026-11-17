package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.stage.StageLook;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class StageClipsTest {

  private static final StageLook ELDER =
      new StageLook(Optional.of("idle_elder"), Optional.empty(), Optional.empty());

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampirePose.class)
  @DisplayName(
      "a stage's own idle replaces only the plain idle, and every other pose keeps its clip")
  void clip(final VampirePose pose) {
    assertEquals(
        pose == VampirePose.IDLE ? ClipCache.loop("idle_elder") : pose.clip(),
        StageClips.clip(pose, ELDER));
    assertEquals(pose.clip(), StageClips.clip(pose, StageLook.PLAIN));
  }
}
