package com.grahambartley.nightborn.client.thrall;

import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.ThrallLook;
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

public class ThrallMarkFeatureRenderer
    extends FeatureRenderer<ThrallEntity, VillagerResemblingModel<ThrallEntity>> {

  private final Map<VillagerType, Identifier> textures = new HashMap<>();

  public ThrallMarkFeatureRenderer(
      final FeatureRendererContext<ThrallEntity, VillagerResemblingModel<ThrallEntity>> context) {
    super(context);
  }

  private Identifier mark(final VillagerType type) {
    return textures.computeIfAbsent(
        type,
        key -> {
          final Identifier texture = ThrallLook.texture(key);
          return MinecraftClient.getInstance().getResourceManager().getResource(texture).isPresent()
              ? texture
              : ThrallLook.texture(VillagerType.PLAINS);
        });
  }

  @Override
  public void render(
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final ThrallEntity thrall,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch) {
    if (thrall.isInvisible()) {
      return;
    }
    renderModel(
        getContextModel(),
        mark(thrall.getVillagerData().getType()),
        matrices,
        vertexConsumers,
        light,
        thrall,
        -1);
  }
}
