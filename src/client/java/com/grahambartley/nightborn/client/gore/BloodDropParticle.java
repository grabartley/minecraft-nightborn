package com.grahambartley.nightborn.client.gore;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;

public class BloodDropParticle extends SpriteBillboardParticle {

  static final float GRAVITY = 1.0F;
  static final int MAX_AGE = 50;

  private final SplatterLook look;

  BloodDropParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final double velocityX,
      final double velocityY,
      final double velocityZ,
      final SpriteProvider sprites,
      final SplatterLook look) {
    super(world, x, y, z);
    this.look = look;
    this.velocityX = velocityX;
    this.velocityY = velocityY;
    this.velocityZ = velocityZ;
    this.gravityStrength = GRAVITY;
    this.velocityMultiplier = 0.97F;
    this.maxAge = MAX_AGE;
    this.scale *= 0.5F + random.nextFloat() * 0.5F;
    tint(this, look, 1.0F);
    setSprite(sprites.getSprite(random));
  }

  @Override
  public void tick() {
    final double beforeX = velocityX;
    final double beforeZ = velocityZ;
    super.tick();
    if (dead) {
      return;
    }
    if (onGround) {
      land(Direction.UP);
    } else if (beforeX != 0.0 && velocityX == 0.0) {
      land(beforeX > 0.0 ? Direction.WEST : Direction.EAST);
    } else if (beforeZ != 0.0 && velocityZ == 0.0) {
      land(beforeZ > 0.0 ? Direction.NORTH : Direction.SOUTH);
    }
  }

  private void land(final Direction face) {
    BloodParticleFactories.spawnSplat(world, x, y, z, face, look, false);
    markDead();
  }

  static void tint(
      final SpriteBillboardParticle particle, final SplatterLook look, final float shade) {
    final int argb = look.apply(0xFFFFFFFF);
    particle.setColor(
        ((argb >> 16) & 0xFF) / 255.0F * shade,
        ((argb >> 8) & 0xFF) / 255.0F * shade,
        (argb & 0xFF) / 255.0F * shade);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
  }
}
