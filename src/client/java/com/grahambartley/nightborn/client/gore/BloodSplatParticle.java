package com.grahambartley.nightborn.client.gore;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;

public class BloodSplatParticle extends SpriteBillboardParticle {

  static final int MIN_AGE = 600;
  static final int AGE_SPREAD = 300;
  static final double LIFT = 0.012;

  private final Quaternionf facing;
  private final SplatterLook look;

  BloodSplatParticle(
      final ClientWorld world,
      final double x,
      final double y,
      final double z,
      final Direction face,
      final SpriteProvider sprites,
      final SplatterLook look,
      final boolean pool) {
    super(
        world,
        x + face.getOffsetX() * LIFT,
        y + face.getOffsetY() * LIFT,
        z + face.getOffsetZ() * LIFT);
    this.look = look;
    this.maxAge = MIN_AGE + random.nextInt(AGE_SPREAD);
    this.gravityStrength = 0.0F;
    this.velocityX = 0.0;
    this.velocityY = 0.0;
    this.velocityZ = 0.0;
    this.scale *= (pool ? 3.2F : 1.0F) * (0.8F + random.nextFloat() * 0.6F);
    this.facing =
        new Quaternionf()
            .rotationTo(0.0F, 0.0F, 1.0F, face.getOffsetX(), face.getOffsetY(), face.getOffsetZ())
            .rotateZ(random.nextFloat() * (float) Math.PI * 2.0F);
    BloodDropParticle.tint(this, look, 1.0F);
    setSprite(sprites.getSprite(random));
  }

  @Override
  public void tick() {
    prevPosX = x;
    prevPosY = y;
    prevPosZ = z;
    if (age++ >= maxAge) {
      markDead();
      return;
    }
    BloodDropParticle.tint(this, look, SplatFade.shade(age, maxAge));
    alpha = SplatFade.alpha(age, maxAge);
  }

  @Override
  public void buildGeometry(
      final VertexConsumer vertexConsumer, final Camera camera, final float tickDelta) {
    method_60373(vertexConsumer, camera, facing, tickDelta);
    method_60373(
        vertexConsumer, camera, new Quaternionf(facing).rotateY((float) Math.PI), tickDelta);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
  }
}
