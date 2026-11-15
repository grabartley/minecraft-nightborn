package com.grahambartley.nightborn.client.anim;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record RigPose(Map<String, BonePose> bones) {

  public static final RigPose REST = new RigPose(Map.of());

  public RigPose {
    bones = Map.copyOf(bones);
  }

  public BonePose bone(final String name) {
    return bones.getOrDefault(name, BonePose.REST);
  }

  public boolean isRest() {
    return bones.isEmpty();
  }

  public RigPose plus(final RigPose other) {
    if (other.isRest()) {
      return this;
    }
    final Map<String, BonePose> sum = new HashMap<>(bones);
    other.bones.forEach((name, pose) -> sum.merge(name, pose, BonePose::plus));
    return new RigPose(sum);
  }

  public RigPose times(final double weight) {
    if (weight >= 1.0) {
      return this;
    }
    if (weight <= 0.0) {
      return REST;
    }
    final Map<String, BonePose> scaled = new HashMap<>();
    bones.forEach((name, pose) -> scaled.put(name, pose.times(weight)));
    return new RigPose(scaled);
  }

  public RigPose towards(final RigPose other, final double weight) {
    if (weight <= 0.0) {
      return this;
    }
    if (weight >= 1.0) {
      return other;
    }
    final Set<String> names = new HashSet<>(bones.keySet());
    names.addAll(other.bones.keySet());
    final Map<String, BonePose> blended = new HashMap<>();
    names.forEach(name -> blended.put(name, bone(name).towards(other.bone(name), weight)));
    return new RigPose(blended);
  }
}
