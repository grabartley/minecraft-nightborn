package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.VampireLook;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class VampireClips {

  static final String BODY = "body";

  private VampireClips() {}

  public static double seconds(final VampirePose pose) {
    return clip(pose).map(clip -> clip.length() / PlayerClipState.TICKS_PER_SECOND).orElse(0.0);
  }

  public static List<Double> heaves(final double deeperThan) {
    final List<Double> times = new ArrayList<>();
    final List<Double> degrees = new ArrayList<>();
    clip(VampirePose.VOMIT)
        .flatMap(VampireClips::body)
        .ifPresent(
            body -> {
              double tick = 0.0;
              for (final Keyframe<MathValue> key : body.rotationKeyFrames().xKeyframes()) {
                tick += key.length();
                times.add(tick / PlayerClipState.TICKS_PER_SECOND);
                degrees.add(Math.toDegrees(key.endValue().get()));
              }
            });
    return Heaves.peaks(times, degrees, deeperThan);
  }

  private static Optional<Animation> clip(final VampirePose pose) {
    final BakedAnimations animations =
        GeckoLibCache.getBakedAnimations().get(VampireLook.animation());
    return animations == null
        ? Optional.empty()
        : Optional.ofNullable(animations.getAnimation(pose.animation()));
  }

  private static Optional<BoneAnimation> body(final Animation clip) {
    for (final BoneAnimation bone : clip.boneAnimations()) {
      if (BODY.equals(bone.boneName())) {
        return Optional.of(bone);
      }
    }
    return Optional.empty();
  }
}
