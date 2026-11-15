package com.grahambartley.nightborn.client.familiar;

import com.grahambartley.nightborn.familiar.FamiliarLivery;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;

public class FamiliarLiveryFeatureRenderer
    extends FeatureRenderer<HumanFamiliarEntity, VillagerResemblingModel<HumanFamiliarEntity>> {

  public FamiliarLiveryFeatureRenderer(
      final FeatureRendererContext<
              HumanFamiliarEntity, VillagerResemblingModel<HumanFamiliarEntity>>
          context) {
    super(context);
  }

  @Override
  public void render(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final HumanFamiliarEntity familiar,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch) {
    if (familiar.isInvisible()) {
      return;
    }
    renderModel(
        getContextModel(),
        FamiliarLivery.texture(familiar.getVillagerData().getType()),
        matrices,
        vertexConsumers,
        light,
        familiar,
        -1);
  }
}
