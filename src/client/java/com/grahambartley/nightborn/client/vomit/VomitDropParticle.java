package com.grahambartley.nightborn.client.vomit;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class VomitDropParticle extends SpriteBillboardParticle {

  static final float GRAVITY = 0.9F;
  static final int MAX_AGE = 40;

  private final boolean splashes;

  VomitDropParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final double velocityX,
      final double velocityY,
      final double velocityZ,
      final SpriteProvider sprites,
      final boolean splashes) {
    super(world, x, y, z);
    this.splashes = splashes;
    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.velocityZ = velocityZ;
    this.gravityStrength = GRAVITY;
    this.velocityMultiplier = 0.96F;
    this.maxAge = MAX_AGE;
    this.scale *= 0.55F + random.nextFloat() * 0.5F;
    setSprite(sprites.getSprite(random));
  }

  @Override
  public void tick() {
    super.tick();
    if (onGround && !dead) {
      if (splashes) {
        VomitParticleFactories.spawnSplash(world, x, y, z);
      }
      markDead();
    }
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
  }
}
