package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.particle.DecalShapes;
import com.grahambartley.nightborn.client.particle.DecalTiming;
import com.grahambartley.nightborn.client.particle.FlatDecalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class BloodSplatParticle extends FlatDecalParticle {

  static final DecalShapes SHAPES = new DecalShapes(4);

  private final SplatterLook look;

  BloodSplatParticle(
      final ClientWorld world,
      final Vec3d contact,
      final Direction face,
      final SpriteProvider sprites,
      final SplatterLook look,
      final boolean pool) {
    super(world, contact, face, sprites, SHAPES, DecalTiming.BLOOD_FADE_TICKS);
    this.look = look;
    this.maxAge = DecalTiming.bloodLife(look.lifeShare(), pool, random.nextFloat());
    this.scale *= (pool ? 4.0F : 1.7F) * (0.8F + random.nextFloat() * 0.6F);
    refresh();
  }

  @Override
  protected void aged() {
    final float[] rgb = look.rgb(SplatFade.shade(age));
    setColor(rgb[0], rgb[1], rgb[2]);
  }
}
