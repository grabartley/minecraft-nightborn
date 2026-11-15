package com.grahambartley.nightborn.client.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public abstract class LandingDropParticle extends SpriteBillboardParticle {

  protected LandingDropParticle(
      final ClientWorld world,
      final Vec3d at,
      final Vec3d velocity,
      final float gravity,
      final int maxAge) {
    super(world, at.x, at.y, at.z);
    this.velocityX = velocity.x;
    this.velocityY = velocity.y;
    this.velocityZ = velocity.z;
    this.gravityStrength = gravity;
    this.maxAge = maxAge;
  }

  protected abstract boolean sticksTo(Direction face);

  protected abstract void land(Direction face, Vec3d contact);

  @Override
  public void tick() {
    final double beforeX = velocityX;
    final double beforeZ = velocityZ;
    super.tick();
    if (dead) {
      return;
    }
    DropLanding.face(onGround, beforeX, velocityX, beforeZ, velocityZ)
        .filter(this::sticksTo)
        .ifPresent(
            face -> {
              land(
                  face,
                  DropLanding.contact(
                      new Vec3d(x, y, z), face, getBoundingBox().getLengthX() / 2.0));
              markDead();
            });
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
  }
}
