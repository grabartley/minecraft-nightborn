package com.grahambartley.nightborn.client.familiar;

import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class HumanFamiliarRenderer
    extends MobEntityRenderer<HumanFamiliarEntity, VillagerResemblingModel<HumanFamiliarEntity>> {

  static final Identifier BASE = Identifier.ofVanilla("textures/entity/villager/villager.png");
  static final float SCALE = 0.9375F;
  static final float SHADOW = 0.5F;

  public HumanFamiliarRenderer(final EntityRendererFactory.Context context) {
    super(
        context,
        new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)),
        SHADOW);
    addFeature(
        new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
    addFeature(
        new VillagerClothingFeatureRenderer<>(this, context.getResourceManager(), "villager"));
    addFeature(new FamiliarLiveryFeatureRenderer(this));
  }

  @Override
  public Identifier getTexture(final HumanFamiliarEntity familiar) {
    return BASE;
  }

  @Override
  protected void scale(
      final HumanFamiliarEntity familiar, final MatrixStack matrices, final float tickDelta) {
    matrices.scale(SCALE, SCALE, SCALE);
  }
}
