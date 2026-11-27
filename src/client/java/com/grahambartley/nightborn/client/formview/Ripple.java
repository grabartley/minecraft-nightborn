package com.grahambartley.nightborn.client.formview;

import net.minecraft.util.math.Vec3d;

public record Ripple(Vec3d origin, double bornSeconds, float range, float strength) {

  public float radius(final double nowSeconds, final float speed) {
    return (float) Math.min(range, Math.max(0.0, nowSeconds - bornSeconds) * speed);
  }

  public float alpha(final double nowSeconds, final float lifeSeconds) {
    final double age = Math.max(0.0, nowSeconds - bornSeconds);
    return (float) Math.max(0.0, strength * (1.0 - age / lifeSeconds));
  }

  public boolean over(final double nowSeconds, final float lifeSeconds) {
    return nowSeconds - bornSeconds >= lifeSeconds;
  }
}
