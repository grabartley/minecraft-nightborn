package com.grahambartley.nightborn.client.vomit;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import org.joml.Quaternionf;

public class VomitSplashParticle extends SpriteBillboardParticle {

  static final int MIN_AGE = 70;
  static final int FADE_TICKS = 25;
  static final double LIFT = 0.012;

  private final Quaternionf flat;

  VomitSplashParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final SpriteProvider sprites) {
    super(world, x, y + LIFT, z);
    this.maxAge = MIN_AGE + random.nextInt(40);
    this.gravityStrength = 0.0F;
    this.scale *= 0.8F + random.nextFloat() * 0.6F;
    this.flat =
        new Quaternionf()
            .rotationY(random.nextFloat() * (float) Math.PI * 2.0F)
            .rotateX((float) -Math.PI / 2.0F);
    setSprite(sprites.getSprite(random));
  }

  @Override
  public void tick() {
    super.tick();
    final int left = maxAge - age;
    alpha = left < FADE_TICKS ? Math.max(0.0F, left / (float) FADE_TICKS) : 1.0F;
  }

  @Override
  public void buildGeometry(
      final VertexConsumer vertexConsumer, final Camera camera, final float tickDelta) {
    method_60373(vertexConsumer, camera, flat, tickDelta);
    method_60373(vertexConsumer, camera, new Quaternionf(flat).rotateY((float) Math.PI), tickDelta);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
  }
}
