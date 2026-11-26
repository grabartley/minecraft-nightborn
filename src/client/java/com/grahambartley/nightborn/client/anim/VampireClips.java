package com.grahambartley.nightborn.client.anim;

import com.grahambartley.nightborn.mob.VampirePose;
import com.grahambartley.nightborn.mob.look.VampireRig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Arm;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.keyframe.BoneAnimation;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.cache.GeckoLibCache;
import software.bernie.geckolib.loading.math.MathValue;
import software.bernie.geckolib.loading.object.BakedAnimations;

public final class VampireClips {

  static final String BODY = "body";
  static final String RIGHT_ARM = "right_arm";
  static final String LEFT_ARM = "left_arm";

  private VampireClips() {}

  public static double seconds(final String animation) {
    return clip(animation).map(clip -> clip.length() / OneShot.TICKS_PER_SECOND).orElse(0.0);
  }

  public static List<Double> heaves(final double deeperThan) {
    final List<Double> times = new ArrayList<>();
    final List<Double> degrees = new ArrayList<>();
    clip(VampirePose.VOMIT.animation())
        .flatMap(clip -> bone(clip, BODY))
        .ifPresent(
            body -> {
              double tick = 0.0;
              for (final Keyframe<MathValue> key : body.rotationKeyFrames().xKeyframes()) {
                tick += key.length();
                times.add(tick / OneShot.TICKS_PER_SECOND);
                degrees.add(Math.toDegrees(key.endValue().get()));
              }
            });
    return Heaves.peaks(times, degrees, deeperThan);
  }

  public static FirstPersonSway sway(final String animation, final Arm arm, final double seconds) {
    final double ticks = seconds * OneShot.TICKS_PER_SECOND;
    return clip(animation)
        .flatMap(clip -> bone(clip, arm == Arm.RIGHT ? RIGHT_ARM : LEFT_ARM))
        .map(
            bone ->
                FirstPersonSway.of(
                    ClipTrack.at(bone.rotationKeyFrames().xKeyframes(), ticks),
                    ClipTrack.at(bone.rotationKeyFrames().zKeyframes(), ticks)))
        .orElse(FirstPersonSway.NONE);
  }

  private static Optional<Animation> clip(final String animation) {
    final BakedAnimations animations =
        GeckoLibCache.getBakedAnimations().get(VampireRig.sharedClips());
    return animations == null
        ? Optional.empty()
        : Optional.ofNullable(animations.getAnimation(animation));
  }

  private static Optional<BoneAnimation> bone(final Animation clip, final String name) {
    for (final BoneAnimation bone : clip.boneAnimations()) {
      if (name.equals(bone.boneName())) {
        return Optional.of(bone);
      }
    }
    return Optional.empty();
  }
}
