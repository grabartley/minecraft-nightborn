package com.grahambartley.nightborn.client.anim;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PlayerPoser {

  private PlayerPoser() {}

  public static void pose(
      final BipedEntityModel<?> model, final RigPose pose, final RigGeometry rig) {
    if (pose.isRest()) {
      return;
    }
    final Map<String, ModelPart> parts = new LinkedHashMap<>();
    parts.put("body", model.body);
    parts.put("head", model.head);
    parts.put("right_arm", model.rightArm);
    parts.put("left_arm", model.leftArm);
    parts.put("right_leg", model.rightLeg);
    parts.put("left_leg", model.leftLeg);
    final Map<ModelPart, RigSpace.PartState> posed = new LinkedHashMap<>();
    parts.forEach(
        (bone, part) ->
            posed.put(
                part,
                RigSpace.apply(
                    RigSpace.chain(rig, pose, bone),
                    new Vector3f(part.pivotX, part.pivotY, part.pivotZ),
                    new Quaternionf().rotationZYX(part.roll, part.yaw, part.pitch))));
    posed.forEach(PlayerPoser::set);
    model.hat.copyTransform(model.head);
  }

  public static void tilt(final MatrixStack matrices, final RigPose pose, final RigGeometry rig) {
    final BonePose root = pose.bone(RigSpace.ROOT);
    if (root.equals(BonePose.REST)) {
      return;
    }
    matrices.multiplyPositionMatrix(RigSpace.rootFrame(rig, root));
  }

  private static void set(final ModelPart part, final RigSpace.PartState state) {
    part.setPivot(state.pivot().x, state.pivot().y, state.pivot().z);
    final Vector3f angles = state.rotation().getEulerAnglesZYX(new Vector3f());
    part.setAngles(angles.x, angles.y, angles.z);
  }
}
