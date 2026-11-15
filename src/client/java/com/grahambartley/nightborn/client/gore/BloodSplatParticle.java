package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.particle.FlatDecalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BloodSplatParticle extends FlatDecalParticle {

  static final int MIN_AGE = 600;
  static final int AGE_SPREAD = 300;
  static final double LIFT = 0.012;
  static final double LIFT_SPREAD = 0.006;

  private final SplatterLook look;

  BloodSplatParticle(
      final ClientWorld world,
      final Vec3d contact,
      final Direction face,
      final SpriteProvider sprites,
      final SplatterLook look,
      final boolean pool) {
    super(world, contact, face, LIFT + world.random.nextDouble() * LIFT_SPREAD);
    this.look = look;
    this.maxAge = MIN_AGE + random.nextInt(AGE_SPREAD);
    this.scale *= (pool ? 4.0F : 1.7F) * (0.8F + random.nextFloat() * 0.6F);
    aged();
    setSprite(sprites.getSprite(random));
  }

  @Override
  protected void aged() {
    final float[] rgb = look.rgb(SplatFade.shade(age, maxAge));
    setColor(rgb[0], rgb[1], rgb[2]);
    alpha = SplatFade.alpha(age, maxAge);
  }
}
