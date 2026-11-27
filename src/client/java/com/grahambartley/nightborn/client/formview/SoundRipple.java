package com.grahambartley.nightborn.client.formview;

import com.grahambartley.nightborn.formview.FormViewEcho;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;

public final class SoundRipple {

  static final float FAINTEST = 0.25F;

  private SoundRipple() {}

  public static Optional<Ripple> of(
      final FormViewEcho echo,
      final Vec3d source,
      final float volume,
      final Vec3d listener,
      final double nowSeconds) {
    final float range = echo.rippleRange(volume);
    if (range <= 0.0F || source.distanceTo(listener) > range + echo.maxRange()) {
      return Optional.empty();
    }
    final float strength = Math.clamp(volume, FAINTEST, 1.0F);
    return Optional.of(new Ripple(source, nowSeconds, range, strength));
  }
}
