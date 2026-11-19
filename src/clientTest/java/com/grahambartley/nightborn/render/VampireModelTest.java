package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.mob.look.VampireRig;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class VampireModelTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(VampireRig.class)
  @DisplayName("a rig whose own file lacks a clip falls back to the shared vampire clips")
  void fallsBackToSharedClips(final VampireRig rig) {
    assertArrayEquals(
        new Identifier[] {VampireRig.sharedClips()},
        new VampireModel().getAnimationResourceFallbacks(null));
  }

  static Stream<Arguments> rigsAndPoses() {
    return Arrays.stream(VampireRig.values())
        .flatMap(rig -> Arrays.stream(VampirePose.values()).map(pose -> Arguments.of(rig, pose)));
  }

  @ParameterizedTest(name = "{0} {1}")
  @MethodSource("rigsAndPoses")
  @DisplayName("every rig finds every pose in its own clips or the shared ones, on bones it has")
  void everyRigPlaysEveryPose(final VampireRig rig, final VampirePose pose) throws IOException {
    final JsonObject own = GeoAssets.animations(GeoAssets.file(rig.clips()));
    final JsonObject shared = GeoAssets.animations(GeoAssets.file(VampireRig.sharedClips()));
    final Set<String> bones = GeoAssets.boneNames(GeoAssets.file(rig.geo()));
    for (final String name :
        pose.clip().getAnimationStages().stream().map(stage -> stage.animationName()).toList()) {
      final JsonObject clip =
          own.has(name) ? own.getAsJsonObject(name) : shared.getAsJsonObject(name);
      assertTrue(clip != null, name + " for " + rig);
      final JsonObject moved = clip.getAsJsonObject("bones");
      if (moved != null) {
        assertTrue(bones.containsAll(moved.keySet()), name + " on " + rig);
      }
    }
  }
}
