package com.grahambartley.nightborn.client.familiar;

import com.grahambartley.nightborn.familiar.FamiliarLivery;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.VillagerResemblingModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.village.VillagerType;

public class FamiliarLiveryFeatureRenderer
    extends FeatureRenderer<HumanFamiliarEntity, VillagerResemblingModel<HumanFamiliarEntity>> {

  private final Map<VillagerType, Identifier> textures = new HashMap<>();

  public FamiliarLiveryFeatureRenderer(
      final FeatureRendererContext<
              HumanFamiliarEntity, VillagerResemblingModel<HumanFamiliarEntity>>
          context) {
    super(context);
  }

  private Identifier livery(final VillagerType type) {
    return textures.computeIfAbsent(
        type,
        key -> {
          final Identifier texture = FamiliarLivery.texture(key);
          return MinecraftClient.getInstance().getResourceManager().getResource(texture).isPresent()
              ? texture
              : FamiliarLivery.texture(VillagerType.PLAINS);
        });
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
        livery(familiar.getVillagerData().getType()),
        matrices,
        vertexConsumers,
        light,
        familiar,
        -1);
  }
}
