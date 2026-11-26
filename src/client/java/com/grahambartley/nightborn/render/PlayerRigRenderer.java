package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.client.vomit.VomitFx;
import com.grahambartley.nightborn.gear.StakedFall;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

public final class PlayerRigRenderer
    extends GeoReplacedEntityRenderer<AbstractClientPlayerEntity, PlayerRigAnimatable> {

  static final float SHADOW_RADIUS = 0.5F;
  static final float VANILLA_PLAYER_SCALE = 0.9375F;
  static final String MOUTH = "mouth";

  public PlayerRigRenderer(final EntityRendererFactory.Context context, final boolean slim) {
    super(context, new PlayerRigModel(slim), PlayerRigAnimatable.INSTANCE);
    this.shadowRadius = SHADOW_RADIUS;
    withScale(VANILLA_PLAYER_SCALE);
    addRenderLayer(new VampirePlayerEyesLayer(this));
    addRenderLayer(new PlayerRigHeldItemsLayer(this));
    addRenderLayer(new RigArmorLayer<>(this, this::getCurrentEntity));
  }

  @Override
  public Identifier getTextureLocation(final PlayerRigAnimatable animatable) {
    return getCurrentEntity() == null
        ? super.getTextureLocation(animatable)
        : getCurrentEntity().getSkinTextures().texture();
  }

  @Override
  protected float getDeathMaxRotation(final PlayerRigAnimatable animatable) {
    return StakedFall.deathRoll(
        getCurrentEntity() != null && StakedFall.staked(getCurrentEntity()),
        super.getDeathMaxRotation(animatable));
  }

  @Override
  public void postRender(
      final MatrixStack poseStack,
      final PlayerRigAnimatable animatable,
      final BakedGeoModel model,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final boolean isReRender,
      final float partialTick,
      final int packedLight,
      final int packedOverlay,
      final int colour) {
    if (!isReRender && getCurrentEntity() != null) {
      model
          .getBone(MOUTH)
          .ifPresent(
              mouth ->
                  VomitFx.mouth(
                      getCurrentEntity(),
                      new Vec3d(
                          mouth
                              .getWorldSpaceMatrix()
                              .transformPosition(
                                  new Vector3f(
                                      mouth.getPivotX() / 16.0F,
                                      mouth.getPivotY() / 16.0F,
                                      mouth.getPivotZ() / 16.0F)))));
    }
    super.postRender(
        poseStack,
        animatable,
        model,
        bufferSource,
        buffer,
        isReRender,
        partialTick,
        packedLight,
        packedOverlay,
        colour);
  }
}
