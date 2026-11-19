package com.grahambartley.nightborn.client.faith;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.world.ClientWorld;

public final class BloodFlameParticle extends SpriteBillboardParticle {

  static final int MIN_AGE = 8;
  static final int AGE_SPREAD = 6;
  static final float SIZE = 0.09F;
  static final float SIZE_SPREAD = 0.04F;
  static final double LIFT = 0.035;

  private final SpriteProvider sprites;
  private final float size;

  BloodFlameParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final SpriteProvider sprites) {
    super(world, x, y, z);
    this.sprites = sprites;
    this.velocityX = 0.0;
    this.velocityY = LIFT;
    this.velocityZ = 0.0;
    this.gravityStrength = 0.0F;
    this.velocityMultiplier = 1.0F;
    this.maxAge = MIN_AGE + random.nextInt(AGE_SPREAD);
    this.size = SIZE + SIZE_SPREAD * random.nextFloat();
    this.alpha = FlameLife.alpha(0, maxAge);
    setSpriteForAge(sprites);
  }

  @Override
  public void tick() {
    super.tick();
    if (!dead) {
      setSpriteForAge(sprites);
      alpha = FlameLife.alpha(age, maxAge);
    }
  }

  @Override
  public float getSize(final float tickDelta) {
    return size * FlameLife.scale(age + tickDelta, maxAge);
  }

  @Override
  protected int getBrightness(final float tickDelta) {
    return LightmapTextureManager.MAX_LIGHT_COORDINATE;
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
  }
}
