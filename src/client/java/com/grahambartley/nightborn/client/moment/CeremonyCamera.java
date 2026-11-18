package com.grahambartley.nightborn.client.moment;

import net.minecraft.util.math.MathHelper;

public final class CeremonyCamera {

  public record Shot(float yaw, float pitch, float distance) {}

  static final float FULL_TURN = 360.0F;
  static final float FARTHEST = 4.0F;
  static final float HIGHEST_PITCH = 20.0F;

  private CeremonyCamera() {}

  public static Shot at(final float startYaw, final float progress) {
    final float t = MathHelper.clamp(progress, 0.0F, 1.0F);
    final float eased = t * t * (3.0F - 2.0F * t);
    final float away = MathHelper.sin(MathHelper.PI * t);
    return new Shot(startYaw + FULL_TURN * eased, HIGHEST_PITCH * away, FARTHEST * away);
  }
}
