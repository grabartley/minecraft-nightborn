package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.VampirePose;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireAnimationAssetsTest {

  private static Map<String, AnimationClip> clips() throws IOException {
    return ClipFile.read(
        JsonParser.parseString(
                Files.readString(
                    Path.of(
                        "src/client/resources/assets/nightborn/animations/entity/vampire.animation.json")))
            .getAsJsonObject());
  }

  @ParameterizedTest(name = "{0}: loops {1}")
  @CsvSource({
    "LEVITATE, true",
    "LEVITATE_MOVE, true",
    "LEVITATE_SPRINT, true",
    "LAND, false",
    "VOMIT, false"
  })
  @DisplayName("the shipped clips read cleanly and loop only where they should")
  void clipsRead(final VampirePose pose, final boolean loops) throws IOException {
    final AnimationClip clip = clips().get(pose.animation());
    assertEquals(loops, clip.loops());
    assertTrue(clip.length() > 0.0);
  }

  @Test
  @DisplayName("the vomit clip heaves more than once")
  void vomitHeaves() throws IOException {
    assertTrue(clips().get("vomit").peaksOf("body", 30).size() >= 2);
  }

  @Test
  @DisplayName("levitating never tips fully horizontal, even at a sprint")
  void neverHorizontal() throws IOException {
    final AnimationClip sprint = clips().get("levitate_sprint");
    for (double t = 0; t <= sprint.length(); t += 0.05) {
      final RigPose pose = sprint.sample(t);
      final double lean = pose.bone("root").rotation().x + pose.bone("body").rotation().x;
      assertTrue(Math.abs(lean) < 60.0, "lean " + lean + " at " + t);
    }
  }

  @Test
  @DisplayName("landing starts where the float leaves off, so the pose never jumps")
  void landingStartsFromTheFloat() throws IOException {
    final RigPose landing = clips().get("land").sample(0.0);
    final RigPose floating = clips().get("levitate").sample(0.0);
    for (final String bone : new String[] {"head", "right_arm", "left_arm", "right_leg"}) {
      assertEquals(floating.bone(bone).rotation(), landing.bone(bone).rotation(), bone);
    }
    assertFalse(landing.isRest());
  }
}
