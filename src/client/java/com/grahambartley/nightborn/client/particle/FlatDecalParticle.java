package com.grahambartley.nightborn.client.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public abstract class FlatDecalParticle extends SpriteBillboardParticle {

  private final Quaternionf facing;

  protected FlatDecalParticle(
      final ClientWorld world, final Vec3d contact, final Direction face, final double lift) {
    super(
        world,
        contact.x + face.getOffsetX() * lift,
        contact.y + face.getOffsetY() * lift,
        contact.z + face.getOffsetZ() * lift);
    this.gravityStrength = 0.0F;
    this.velocityX = 0.0;
    this.velocityY = 0.0;
    this.velocityZ = 0.0;
    this.facing =
        new Quaternionf()
            .rotationTo(0.0F, 0.0F, 1.0F, face.getOffsetX(), face.getOffsetY(), face.getOffsetZ())
            .rotateZ(random.nextFloat() * (float) Math.PI * 2.0F);
  }

  protected abstract void aged();

  @Override
  public void tick() {
    prevPosX = x;
    prevPosY = y;
    prevPosZ = z;
    if (age++ >= maxAge) {
      markDead();
      return;
    }
    aged();
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
