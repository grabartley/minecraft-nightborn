package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.particle.FlatDecalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VomitSplashParticle extends FlatDecalParticle {

  static final int MIN_AGE = 70;
  static final int FADE_TICKS = 25;
  static final double LIFT = 0.012;

  VomitSplashParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final SpriteProvider sprites) {
    super(world, new Vec3d(x, y, z), Direction.UP, LIFT);
    this.maxAge = MIN_AGE + random.nextInt(40);
    this.scale *= 0.8F + random.nextFloat() * 0.6F;
    setSprite(sprites.getSprite(random));
  }

  @Override
  protected void aged() {
    final int left = maxAge - age;
    alpha = left < FADE_TICKS ? Math.max(0.0F, left / (float) FADE_TICKS) : 1.0F;
  }
}
