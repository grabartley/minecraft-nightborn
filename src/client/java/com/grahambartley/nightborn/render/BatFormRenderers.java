package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.batform.WingedState;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class BatFormRenderers {

  private final EntityRendererFactory.Context context;
  private final BatFormRenderer bat;
  private final Map<Identifier, WingedFormRenderer> winged = new HashMap<>();

  public BatFormRenderers(final EntityRendererFactory.Context context) {
    this.context = context;
    this.bat = new BatFormRenderer(context);
  }

  public void render(
      final LivingEntity entity,
      final float yaw,
      final float tickDelta,
      final MatrixStack matrices,
      final VertexConsumerProvider vertices,
      final int light) {
    WingedForm.state(entity)
        .flatMap(WingedState::rig)
        .<EntityRenderer<LivingEntity>>map(
            rig -> winged.computeIfAbsent(rig, id -> new WingedFormRenderer(context, id)))
        .orElse(bat)
        .render(entity, yaw, tickDelta, matrices, vertices, light);
  }
}
