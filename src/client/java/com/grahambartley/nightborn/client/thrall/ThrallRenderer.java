package com.grahambartley.nightborn.client.thrall;

import com.grahambartley.nightborn.thrall.ThrallEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.VillagerClothingFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class ThrallRenderer
    extends MobEntityRenderer<ThrallEntity, VillagerResemblingModel<ThrallEntity>> {

  static final Identifier BASE = Identifier.ofVanilla("textures/entity/villager/villager.png");
  static final float SCALE = 0.9375F;
  static final float SHADOW = 0.5F;

  public ThrallRenderer(final EntityRendererFactory.Context context) {
    super(
        context,
        new VillagerResemblingModel<>(context.getPart(EntityModelLayers.VILLAGER)),
        SHADOW);
    addFeature(
        new HeadFeatureRenderer<>(this, context.getModelLoader(), context.getHeldItemRenderer()));
    addFeature(
        new VillagerClothingFeatureRenderer<>(this, context.getResourceManager(), "villager"));
    addFeature(new ThrallMarkFeatureRenderer(this));
  }

  @Override
  public Identifier getTexture(final ThrallEntity thrall) {
    return BASE;
  }

  @Override
  protected void scale(
      final ThrallEntity thrall, final MatrixStack matrices, final float tickDelta) {
    matrices.scale(SCALE, SCALE, SCALE);
  }
}
