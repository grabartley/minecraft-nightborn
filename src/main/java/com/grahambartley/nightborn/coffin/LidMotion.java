package com.grahambartley.nightborn.coffin;

import net.minecraft.util.math.MathHelper;

public final class LidMotion {

  static final float STEP = 0.1F;

  private boolean seen;
  private float previous;
  private float current;

  public void tick(final boolean open) {
    final float target = open ? 1.0F : 0.0F;
    if (!this.seen) {
      this.seen = true;
      this.previous = target;
      this.current = target;
      return;
    }
    this.previous = this.current;
    this.current = open ? Math.min(1.0F, this.current + STEP) : Math.max(0.0F, this.current - STEP);
  }

  public float openness(final float tickDelta) {
    return MathHelper.lerp(tickDelta, this.previous, this.current);
  }
}
