package com.grahambartley.nightborn.client.vomit;

import com.grahambartley.nightborn.client.particle.DecalShapes;
import com.grahambartley.nightborn.client.particle.DecalTiming;
import com.grahambartley.nightborn.client.particle.FlatDecalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VomitSplashParticle extends FlatDecalParticle {

  static final DecalShapes SHAPES = new DecalShapes(3);

  VomitSplashParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final SpriteProvider sprites) {
    super(world, new Vec3d(x, y, z), Direction.UP, sprites, SHAPES, DecalTiming.VOMIT_FADE_TICKS);
    this.maxAge = DecalTiming.vomitLife(random.nextFloat());
    this.scale *= 0.8F + random.nextFloat() * 0.6F;
    refresh();
  }
}
