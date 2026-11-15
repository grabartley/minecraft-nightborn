package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.particle.LandingDropParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VomitDropParticle extends LandingDropParticle {

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
    super(world, new Vec3d(x, y, z), new Vec3d(velocityX, velocityY, velocityZ), GRAVITY, MAX_AGE);
    this.splashes = splashes;
    this.velocityMultiplier = 0.96F;
    this.scale *= 0.55F + random.nextFloat() * 0.5F;
    setSprite(sprites.getSprite(random));
  }

  @Override
  protected boolean sticksTo(final Direction face) {
    return face == Direction.UP;
  }

  @Override
  protected void land(final Direction face, final Vec3d contact) {
    if (splashes) {
      VomitParticleFactories.spawnSplash(world, contact.x, contact.y, contact.z);
    }
  }
}
