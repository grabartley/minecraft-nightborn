package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class MistFormRenderer
    extends GeoReplacedEntityRenderer<LivingEntity, MistFormAnimatable> {

  static final Identifier RIG = Identifier.of(Nightborn.MOD_ID, "mist_form");
  static final float SHADOW_RADIUS = 0.45F;
  static final float DRAWN_SCALE = 1.5F;

  public MistFormRenderer(final EntityRendererFactory.Context context) {
    super(context, new DefaultedEntityGeoModel<>(RIG, false), new MistFormAnimatable());
    this.shadowRadius = SHADOW_RADIUS;
    withScale(DRAWN_SCALE);
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }

  @Override
  public RenderLayer getRenderType(
      final MistFormAnimatable animatable,
      final Identifier texture,
      @Nullable final VertexConsumerProvider bufferSource,
      final float partialTick) {
    return RenderLayer.getEntityTranslucent(texture);
  }
}
