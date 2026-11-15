package com.grahambartley.nightborn.client.anim;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class RigSpace {

  public static final String ROOT = "root";

  static final float MODEL_HEIGHT = 24.0F;
  static final float PIXEL = 1.0F / 16.0F;
  static final float MODEL_DROP = 1.501F;

  private RigSpace() {}

  public record PartState(Vector3f pivot, Quaternionf rotation) {}

  public static Quaternionf partRotation(final Vec3d degrees) {
    return new Quaternionf()
        .rotationZYX(
            (float) Math.toRadians(-degrees.z),
            (float) Math.toRadians(-degrees.y),
            (float) Math.toRadians(degrees.x));
  }

  public static Vector3f partOffset(final Vec3d position) {
    return new Vector3f((float) -position.x, (float) -position.y, (float) position.z);
  }

  public static Vector3f modelPoint(final Vec3d rigPoint) {
    return new Vector3f((float) -rigPoint.x, MODEL_HEIGHT - (float) rigPoint.y, (float) rigPoint.z);
  }

  public static Matrix4f boneMatrix(final Vec3d rigPivot, final BonePose pose) {
    final Vector3f pivot = modelPoint(rigPivot);
    return new Matrix4f()
        .translate(pivot.add(partOffset(pose.position()), new Vector3f()))
        .rotate(partRotation(pose.rotation()))
        .translate(pivot.negate(new Vector3f()));
  }

  public static Matrix4f chain(final RigGeometry rig, final RigPose pose, final String bone) {
    final Deque<String> lineage = new ArrayDeque<>();
    Optional<String> current = Optional.of(bone);
    while (current.isPresent() && !ROOT.equals(current.get())) {
      final String name = current.get();
      lineage.push(name);
      current = rig.bone(name).flatMap(RigGeometry.Bone::parent);
    }
    final Matrix4f chain = new Matrix4f();
    for (final String name : lineage) {
      rig.bone(name).ifPresent(found -> chain.mul(boneMatrix(found.pivot(), pose.bone(name))));
    }
    return chain;
  }

  public static PartState apply(
      final Matrix4f chain, final Vector3f vanillaPivot, final Quaternionf vanillaRotation) {
    return new PartState(
        chain.transformPosition(vanillaPivot, new Vector3f()),
        chain.getNormalizedRotation(new Quaternionf()).mul(vanillaRotation));
  }

  public static Matrix4f rootFrame(final RigGeometry rig, final BonePose root) {
    final Vec3d pivot = rig.bone(ROOT).map(RigGeometry.Bone::pivot).orElse(Vec3d.ZERO);
    final Vec3d rotation = root.rotation();
    return new Matrix4f()
        .translate(
            (float) (pivot.x + root.position().x) * PIXEL,
            (float) (pivot.y + root.position().y) * PIXEL,
            (float) (pivot.z + root.position().z) * PIXEL)
        .rotate(
            new Quaternionf()
                .rotationZYX(
                    (float) Math.toRadians(-rotation.z),
                    (float) Math.toRadians(rotation.y),
                    (float) Math.toRadians(-rotation.x)))
        .translate((float) -pivot.x * PIXEL, (float) -pivot.y * PIXEL, (float) -pivot.z * PIXEL);
  }

  public static Optional<Vec3d> locate(
      final RigGeometry rig, final RigPose pose, final String locator, final float modelScale) {
    return rig.locator(locator)
        .map(
            found -> {
              final Vector3f model =
                  chain(rig, pose, found.bone()).transformPosition(modelPoint(found.position()));
              final Vector3f frame =
                  new Vector3f(
                      -model.x * PIXEL * modelScale,
                      -(model.y * PIXEL - MODEL_DROP) * modelScale,
                      model.z * PIXEL * modelScale);
              rootFrame(rig, pose.bone(ROOT)).transformPosition(frame);
              return new Vec3d(frame.x, frame.y, frame.z);
            });
  }
}
