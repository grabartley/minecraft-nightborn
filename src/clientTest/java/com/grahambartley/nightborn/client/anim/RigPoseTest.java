package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RigPoseTest {

  private static final RigPose LEAN =
      new RigPose(Map.of("root", new BonePose(new Vec3d(30, 0, 0), Vec3d.ZERO)));
  private static final RigPose ARMS =
      new RigPose(Map.of("right_arm", new BonePose(new Vec3d(0, 0, -40), Vec3d.ZERO)));

  @Test
  @DisplayName("a bone the pose does not move rests")
  void missingBoneRests() {
    assertEquals(BonePose.REST, LEAN.bone("head"));
  }

  @ParameterizedTest(name = "weight {0}: lean {1}")
  @CsvSource({"-1, 0", "0, 0", "0.5, 15", "1, 30", "2, 30"})
  @DisplayName("a weight outside zero to one is held at its end")
  void weights(final double weight, final double lean) {
    assertEquals(lean, LEAN.times(weight).bone("root").rotation().x, 1.0E-9);
  }

  @ParameterizedTest(name = "towards {0}")
  @CsvSource({"0.5, 15, -20"})
  @DisplayName("blending covers bones that only one pose moves")
  void blendCoversBoth(final double weight, final double lean, final double spread) {
    final RigPose blended = LEAN.towards(ARMS, weight);
    assertEquals(lean, blended.bone("root").rotation().x, 1.0E-9);
    assertEquals(spread, blended.bone("right_arm").rotation().z, 1.0E-9);
  }

  @Test
  @DisplayName("adding layers keeps every bone and adding rest changes nothing")
  void layers() {
    final RigPose both = LEAN.plus(ARMS);
    assertEquals(30, both.bone("root").rotation().x, 1.0E-9);
    assertEquals(-40, both.bone("right_arm").rotation().z, 1.0E-9);
    assertSame(LEAN, LEAN.plus(RigPose.REST));
    assertTrue(RigPose.REST.isRest());
  }
}
