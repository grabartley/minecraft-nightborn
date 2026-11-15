package com.grahambartley.nightborn.client.smoke;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public final class VampireSmokeParticle extends SpriteBillboardParticle {

  static final float BUOYANCY = -0.05F;
  static final float DRAG = 0.94F;
  static final double RISE = 0.02;

  private final SpriteProvider sprites;
  private final float fullScale;

  VampireSmokeParticle(
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
    this.velocityY = velocityY + RISE;
    this.velocityZ = velocityZ;
    this.gravityStrength = BUOYANCY;
    this.velocityMultiplier = DRAG;
    this.maxAge = SmokeRolls.maxAge(random.nextFloat());
    this.fullScale = SmokeRolls.fullSize(random.nextFloat());
    final float shade = SmokeRolls.shade(random.nextFloat());
    setColor(shade, shade, shade);
    this.alpha = SmokeLife.alpha(0, maxAge);
    setSpriteForAge(sprites);
  }

  @Override
  public void tick() {
    super.tick();
    if (!dead) {
      setSpriteForAge(sprites);
      alpha = SmokeLife.alpha(age, maxAge);
    }
  }

  @Override
  public float getSize(final float tickDelta) {
    return fullScale * SmokeLife.scale(age + tickDelta, maxAge);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
  }
}
