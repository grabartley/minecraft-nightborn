package com.grahambartley.nightborn.client.anim;

import net.minecraft.util.math.Vec3d;

public record BonePose(Vec3d rotation, Vec3d position) {

  public static final BonePose REST = new BonePose(Vec3d.ZERO, Vec3d.ZERO);

  public BonePose plus(final BonePose other) {
    return new BonePose(rotation.add(other.rotation), position.add(other.position));
  }

  public BonePose times(final double weight) {
    return new BonePose(rotation.multiply(weight), position.multiply(weight));
  }

  public BonePose towards(final BonePose other, final double weight) {
    return new BonePose(
        rotation.lerp(other.rotation, weight), position.lerp(other.position, weight));
  }
}
