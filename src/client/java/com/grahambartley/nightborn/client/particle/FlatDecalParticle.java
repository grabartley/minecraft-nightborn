package com.grahambartley.nightborn.client.particle;

import com.grahambartley.nightborn.particle.DecalFootprint;
import com.grahambartley.nightborn.particle.DecalRecheck;
import java.util.OptionalDouble;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

public abstract class FlatDecalParticle extends SpriteBillboardParticle
    implements PlacedDecal<FlatDecalParticle> {

  private static final LiveDecals<FlatDecalParticle> LIVE = new LiveDecals<>(DecalTiming.LIMIT);

  private final Vec3d contact;
  private final Direction face;
  private final float spin;
  private final int recheckPhase;
  private final Quaternionf facing;
  private final Quaternionf backFacing;
  private final Sprite rimSprite;
  private final Sprite bodySprite;
  private final int fadeTicks;
  private long lastTicked;
  private boolean retiring;

  protected FlatDecalParticle(
      final ClientWorld world,
      final Vec3d contact,
      final Direction face,
      final SpriteProvider sprites,
      final DecalShapes shapes,
      final int fadeTicks) {
    super(
        world,
        contact.x + face.getOffsetX() * DecalLift.anchor(),
        contact.y + face.getOffsetY() * DecalLift.anchor(),
        contact.z + face.getOffsetZ() * DecalLift.anchor());
    this.contact = contact;
    this.face = face;
    this.fadeTicks = fadeTicks;
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
    this.backFacing = new Quaternionf(facing).rotateY((float) Math.PI);
    final int roll = random.nextInt();
    this.rimSprite = sprites.getSprite(shapes.rim(roll), shapes.lastSprite());
    this.bodySprite = sprites.getSprite(shapes.body(roll), shapes.lastSprite());
    this.lastTicked = world.getTime();
    setSprite(rimSprite);
  }

  protected void aged() {}

  public static void place(final ParticleManager particles, final FlatDecalParticle decal) {
    if (decal.settle() && LIVE.place(decal, decal.world.getTime())) {
      particles.addParticle(decal);
    }
  }

  private boolean settle() {
    final OptionalDouble fitted = DecalFootprint.fittedHalfSize(world, contact, face, scale, spin);
    fitted.ifPresent(halfSize -> scale = (float) halfSize);
    return fitted.isPresent();
  }

  @Override
  public boolean gone(final long now) {
    return !isAlive()
        || world != MinecraftClient.getInstance().world
        || Math.abs(now - lastTicked) > DecalTiming.STALE_TICKS;
  }

  @Override
  public boolean retiring() {
    return retiring;
  }

  @Override
  public void retire() {
    retiring = true;
    maxAge = Math.min(maxAge, age + fadeTicks);
  }

  @Override
  public boolean absorbs(final FlatDecalParticle incoming) {
    return incoming.getClass() == getClass()
        && DecalJoin.joins(face, contact, scale, incoming.face, incoming.contact);
  }

  @Override
  public void absorb(final FlatDecalParticle incoming) {
    DecalFootprint.fittedHalfSize(
            world, contact, face, DecalJoin.grown(scale, incoming.scale), spin)
        .ifPresent(halfSize -> scale = (float) Math.max(scale, halfSize));
    maxAge = Math.max(maxAge - age, incoming.maxAge);
    age = 0;
    refresh();
  }

  @Override
  public void tick() {
    prevPosX = x;
    prevPosY = y;
    prevPosZ = z;
    lastTicked = world.getTime();
    if (age++ >= maxAge || !stillHeld()) {
      markDead();
      return;
    }
    refresh();
  }

  protected final void refresh() {
    alpha = DecalFade.alpha(age, maxAge, fadeTicks);
    aged();
  }

  private boolean stillHeld() {
    return !DecalRecheck.due(age, recheckPhase)
        || DecalFootprint.holds(world, contact, face, DecalFootprint.halfExtent(scale, spin));
  }

  @Override
  public void buildGeometry(
      final VertexConsumer vertexConsumer, final Camera camera, final float tickDelta) {
    final Vec3d camera3d = camera.getPos();
    final float wetness = DecalFade.wetness(age);
    layer(vertexConsumer, camera3d, rimSprite, DecalLift.rim(wetness), tickDelta);
    layer(vertexConsumer, camera3d, bodySprite, DecalLift.body(wetness), tickDelta);
    setSprite(rimSprite);
  }

  private void layer(
      final VertexConsumer vertexConsumer,
      final Vec3d camera,
      final Sprite layerSprite,
      final double lift,
      final float tickDelta) {
    setSprite(layerSprite);
    final float relX = (float) (contact.x + face.getOffsetX() * lift - camera.x);
    final float relY = (float) (contact.y + face.getOffsetY() * lift - camera.y);
    final float relZ = (float) (contact.z + face.getOffsetZ() * lift - camera.z);
    method_60374(vertexConsumer, facing, relX, relY, relZ, tickDelta);
    method_60374(vertexConsumer, backFacing, relX, relY, relZ, tickDelta);
  }

  @Override
  public ParticleTextureSheet getType() {
    return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
  }
}
