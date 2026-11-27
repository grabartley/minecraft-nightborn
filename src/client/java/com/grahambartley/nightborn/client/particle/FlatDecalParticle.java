package com.grahambartley.nightborn.client.particle;

import com.grahambartley.nightborn.particle.DecalFootprint;
import com.grahambartley.nightborn.particle.DecalRecheck;
import java.util.OptionalDouble;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public abstract class FlatDecalParticle extends SpriteBillboardParticle {

  private final Vec3d contact;
  private final Direction face;
  private final float spin;
  private final int recheckPhase;
  private final Quaternionf facing;

  protected FlatDecalParticle(
      final ClientWorld world, final Vec3d contact, final Direction face, final double lift) {
    super(
        world,
        contact.x + face.getOffsetX() * lift,
        contact.y + face.getOffsetY() * lift,
        contact.z + face.getOffsetZ() * lift);
    this.contact = contact;
    this.face = face;
    this.gravityStrength = 0.0F;
    this.velocityX = 0.0;
    this.velocityY = 0.0;
    this.velocityZ = 0.0;
    this.spin = random.nextFloat() * (float) Math.PI * 2.0F;
    this.recheckPhase = DecalRecheck.phase(random.nextInt());
    this.facing =
        new Quaternionf()
            .rotationTo(0.0F, 0.0F, 1.0F, face.getOffsetX(), face.getOffsetY(), face.getOffsetZ())
            .rotateZ(spin);
  }

  protected abstract void aged();

  public static void place(final ParticleManager particles, final FlatDecalParticle decal) {
    if (decal.settle()) {
      particles.addParticle(decal);
    }
  }

  private boolean settle() {
    final OptionalDouble fitted = DecalFootprint.fittedHalfSize(world, contact, face, scale, spin);
    fitted.ifPresent(halfSize -> scale = (float) halfSize);
    return fitted.isPresent();
  }

  @Override
  public void tick() {
    prevPosX = x;
    prevPosY = y;
    prevPosZ = z;
    if (age++ >= maxAge || !stillHeld()) {
      markDead();
      return;
    }
    aged();
  }

  private boolean stillHeld() {
    return !DecalRecheck.due(age, recheckPhase)
        || DecalFootprint.holds(world, contact, face, DecalFootprint.halfExtent(scale, spin));
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
