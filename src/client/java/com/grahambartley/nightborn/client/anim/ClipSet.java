package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Map;
import java.util.Optional;

public record ClipSet(Map<String, AnimationClip> clips, RigGeometry rig) {

  public static final ClipSet EMPTY = new ClipSet(Map.of(), RigGeometry.NONE);

  public ClipSet {
    clips = Map.copyOf(clips);
  }

  public Optional<AnimationClip> clip(final VampirePose pose) {
    return Optional.ofNullable(clips.get(pose.animation()));
  }

  public RigPose sample(final VampirePose pose, final double seconds) {
    return clip(pose).map(clip -> clip.sample(seconds)).orElse(RigPose.REST);
  }

  public double length(final VampirePose pose) {
    return clip(pose).map(AnimationClip::length).orElse(0.0);
  }
}
