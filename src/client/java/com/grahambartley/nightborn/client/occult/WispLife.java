package com.grahambartley.nightborn.client.occult;

import com.grahambartley.nightborn.client.particle.ParticleLife;
import net.minecraft.client.render.LightmapTextureManager;

public final class WispLife {

  static final float OPACITY = 0.85F;
  static final float FADE_IN_SHARE = 0.2F;
  static final float START_SCALE = 1.0F;
  static final float END_SCALE = 0.35F;
  static final int MIN_BLOCK_LIGHT = 9;

  private WispLife() {}

  public static float alpha(final float age, final int maxAge) {
    final float life = ParticleLife.fraction(age, maxAge);
    if (life < FADE_IN_SHARE) {
      return OPACITY * life / FADE_IN_SHARE;
    }
    return OPACITY * (1.0F - life) / (1.0F - FADE_IN_SHARE);
  }

  public static float scale(final float age, final int maxAge) {
    return START_SCALE + (END_SCALE - START_SCALE) * ParticleLife.fraction(age, maxAge);
  }

  public static int brightness(final int worldLight) {
    final int block =
        Math.max(LightmapTextureManager.getBlockLightCoordinates(worldLight), MIN_BLOCK_LIGHT);
    return LightmapTextureManager.pack(
        block, LightmapTextureManager.getSkyLightCoordinates(worldLight));
  }
}
