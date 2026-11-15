package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClipSetTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampirePose.class)
  @DisplayName("a missing clip rests and lasts no time, so a broken pack never crashes a render")
  void missingClipRests(final VampirePose pose) {
    final ClipSet empty = new ClipSet(Map.of(), RigGeometry.NONE);
    assertTrue(empty.sample(pose, 1.0).isRest());
    assertEquals(0.0, empty.length(pose));
    assertTrue(empty.clip(pose).isEmpty());
  }
}
