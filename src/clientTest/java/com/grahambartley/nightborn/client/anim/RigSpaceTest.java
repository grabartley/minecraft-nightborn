package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Map;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RigSpaceTest {

  private static final float EPSILON = 1.0E-4F;
  private static final float PLAYER_EYE_HEIGHT = 1.62F;
  private static final float PLAYER_MODEL_SCALE = 0.9375F;

  @Test
  @DisplayName("an arm the clip swings forward points forward like a zombie's, pitch -90")
  void forwardIsAZombieArm() {
    final Vector3f angles =
        RigSpace.partRotation(new Vec3d(-90, 0, 0)).getEulerAnglesZYX(new Vector3f());
    assertEquals((float) Math.toRadians(-90), angles.x, EPSILON);
  }

  @Test
  @DisplayName("a right arm the clip spreads outward rolls the way vanilla sways it outward")
  void spreadIsVanillaOutward() {
    final Vector3f hand = new Vector3f(0, 12, 0);
    RigSpace.partRotation(new Vec3d(0, 0, -30)).transform(hand);
    assertTrue(
        hand.x < 0, "vanilla's right arm hangs at negative x, so out means further negative");
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "head, head",
    "right_arm, rightArm",
    "left_arm, leftArm",
    "right_leg, rightLeg",
    "left_leg, leftLeg"
  })
  @DisplayName("the rig's pivots land on the vanilla player model's own pivots")
  void pivotsMatchVanilla(final String bone, final String part) throws Exception {
    final ModelPart vanilla = vanillaPlayer().getChild(childName(part));
    final Vector3f pivot =
        RigSpace.modelPoint(RigGeometryTest.rig("male").bone(bone).orElseThrow().pivot());
    assertEquals(vanilla.pivotX, pivot.x, 0.11F);
    assertEquals(vanilla.pivotY, pivot.y, EPSILON);
    assertEquals(vanilla.pivotZ, pivot.z, EPSILON);
  }

  @Test
  @DisplayName("a rest pose leaves every part where vanilla put it")
  void restChangesNothing() throws IOException {
    final Matrix4f chain = RigSpace.chain(RigGeometryTest.rig("male"), RigPose.REST, "head");
    final Vector3f pivot = chain.transformPosition(new Vector3f(1, 2, 3));
    assertEquals(new Vector3f(1, 2, 3), pivot);
  }

  @Test
  @DisplayName("a forward lean of the root tips the top of the body towards the front")
  void rootLeansForward() throws IOException {
    final Matrix4f root =
        RigSpace.rootFrame(
            RigGeometryTest.rig("male"), new BonePose(new Vec3d(36, 0, 0), Vec3d.ZERO));
    final Vector3f head = root.transformPosition(new Vector3f(0, 1.5F, 0));
    assertTrue(head.z < -0.5F, "front is -z, so the head moves to negative z: " + head.z);
    assertTrue(head.y > 0.9F, "and stays well above the feet: " + head.y);
  }

  @Test
  @DisplayName("at rest the mouth sits just under a player's eye height, at the front of the face")
  void mouthAtRest() throws IOException {
    final Vec3d mouth =
        RigSpace.locate(RigGeometryTest.rig("male"), RigPose.REST, "mouth", PLAYER_MODEL_SCALE)
            .orElseThrow();
    assertTrue(mouth.y < PLAYER_EYE_HEIGHT && mouth.y > PLAYER_EYE_HEIGHT - 0.25, "y " + mouth.y);
    assertTrue(mouth.z < -0.2, "in front of the face, z " + mouth.z);
    assertEquals(0.0, mouth.x, EPSILON);
  }

  @Test
  @DisplayName("doubling over at the hips brings the mouth down and forward")
  void mouthFollowsTheBend() throws IOException {
    final RigGeometry rig = RigGeometryTest.rig("male");
    final Vec3d upright = RigSpace.locate(rig, RigPose.REST, "mouth", 1.0F).orElseThrow();
    final Vec3d bent =
        RigSpace.locate(
                rig,
                new RigPose(Map.of("body", new BonePose(new Vec3d(45, 0, 0), Vec3d.ZERO))),
                "mouth",
                1.0F)
            .orElseThrow();
    assertTrue(bent.y < upright.y - 0.2, "lower");
    assertTrue(bent.z < upright.z - 0.2, "further forward");
  }

  static ModelPart vanillaPlayer() {
    return TexturedModelData.of(
            PlayerEntityModel.getTexturedModelData(Dilation.NONE, false), 64, 64)
        .createModel();
  }

  private static String childName(final String field) {
    return switch (field) {
      case "rightArm" -> "right_arm";
      case "leftArm" -> "left_arm";
      case "rightLeg" -> "right_leg";
      case "leftLeg" -> "left_leg";
      default -> field;
    };
  }
}
