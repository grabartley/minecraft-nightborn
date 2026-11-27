package com.grahambartley.nightborn.client.formview;

import com.grahambartley.nightborn.formview.FormView;
import java.util.Optional;
import net.minecraft.util.math.MathHelper;

public record FormViewFade(Optional<FormView> shown, float strength, float previous) {

  public static final FormViewFade NONE = new FormViewFade(Optional.empty(), 0.0F, 0.0F);

  static final float GONE = 1.0E-4F;

  public FormViewFade step(final Optional<FormView> target) {
    if (shown.isEmpty()) {
      return target.map(view -> new FormViewFade(target, rise(0.0F, view), 0.0F)).orElse(NONE);
    }
    final FormView current = shown.get();
    if (target.equals(shown)) {
      return new FormViewFade(shown, rise(strength, current), strength);
    }
    final float lower = Math.max(0.0F, strength - 1.0F / current.fadeOutTicks());
    if (lower > GONE) {
      return new FormViewFade(shown, lower, strength);
    }
    return target.map(view -> new FormViewFade(target, 0.0F, 0.0F)).orElse(NONE);
  }

  public float strength(final float tickDelta) {
    return MathHelper.lerp(tickDelta, previous, strength);
  }

  public boolean visible() {
    return shown.isPresent() && (strength > 0.0F || previous > 0.0F);
  }

  private static float rise(final float from, final FormView view) {
    return Math.min(1.0F, from + 1.0F / view.fadeInTicks());
  }
}
