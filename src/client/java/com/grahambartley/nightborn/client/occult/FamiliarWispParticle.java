package com.grahambartley.nightborn.client.occult;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public final class FamiliarWispParticle extends SpriteBillboardParticle {

  static final int MIN_AGE = 20;
  static final int AGE_SPREAD = 14;
  static final float SIZE = 0.16F;
  static final float DRAG = 0.96F;

  private final SpriteProvider sprites;

  FamiliarWispParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final double velocityX,
      final double velocityY,
      final double velocityZ,
      final SpriteProvider sprites) {
    super(world, x, y, z);
    this.sprites = sprites;
    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.velocityZ = velocityZ;
    this.gravityStrength = 0.0F;
    this.velocityMultiplier = DRAG;
    this.maxAge = MIN_AGE + random.nextInt(AGE_SPREAD);
    this.alpha = WispLife.alpha(0, maxAge);
    setSpriteForAge(sprites);
  }

  @Override
  public void tick() {
    super.tick();
    if (!dead) {
      setSpriteForAge(sprites);
      alpha = WispLife.alpha(age, maxAge);
    }
  }

  @Override
  public float getSize(final float tickDelta) {
    return SIZE * WispLife.scale(age + tickDelta, maxAge);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
  }
}
