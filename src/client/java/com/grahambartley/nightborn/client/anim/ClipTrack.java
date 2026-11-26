package com.grahambartley.nightborn.client.anim;

import java.util.List;
import software.bernie.geckolib.animation.EasingType;
import software.bernie.geckolib.animation.keyframe.AnimationPoint;
import software.bernie.geckolib.animation.keyframe.Keyframe;
import software.bernie.geckolib.loading.math.MathValue;

public final class ClipTrack {

  private ClipTrack() {}

  public static double at(final List<Keyframe<MathValue>> keys, final double ticks) {
    double elapsed = 0.0;
    for (final Keyframe<MathValue> key : keys) {
      if (ticks < elapsed + key.length()) {
        return EasingType.lerpWithOverride(
            new AnimationPoint(
                key,
                Math.max(0.0, ticks - elapsed),
                key.length(),
                key.startValue().get(),
                key.endValue().get()),
            null);
      }
      elapsed += key.length();
    }
    return keys.isEmpty() ? 0.0 : keys.getLast().endValue().get();
  }
}
