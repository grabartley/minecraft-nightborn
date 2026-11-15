package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.particle.LandingDropParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BloodDropParticle extends LandingDropParticle {

  static final float GRAVITY = 1.0F;
  static final int MAX_AGE = 50;

  private final SplatterLook look;

  BloodDropParticle(
      final ClientWorld world,
      final Vec3d at,
      final Vec3d velocity,
      final SpriteProvider sprites,
      final SplatterLook look) {
    super(world, at, velocity, GRAVITY, MAX_AGE);
    this.look = look;
    this.velocityMultiplier = 0.97F;
    this.scale *= 0.5F + random.nextFloat() * 0.5F;
    final float[] rgb = look.rgb(1.0F);
    setColor(rgb[0], rgb[1], rgb[2]);
    setSprite(sprites.getSprite(random));
  }

  @Override
  protected boolean sticksTo(final Direction face) {
    return true;
  }

  @Override
  protected void land(final Direction face, final Vec3d contact) {
    BloodParticleFactories.spawnSplat(MinecraftClient.getInstance(), contact, face, look, false);
  }
}
