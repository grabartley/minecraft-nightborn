package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.accessory.AccessoryItems;
import com.grahambartley.nightborn.accessory.AccessorySlot;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.Color;

public final class AccessoryRenderer implements TrinketRenderer {

  static final AccessoryRenderer INSTANCE = new AccessoryRenderer();

  private AccessoryRenderer() {}

  public static void register() {
    AccessoryItems.all().stream()
        .filter(item -> item.piece().drawn())
        .forEach(item -> TrinketRendererRegistry.registerRenderer(item, INSTANCE));
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void render(
      final ItemStack stack,
      final SlotReference slot,
      final EntityModel<? extends LivingEntity> contextModel,
      final MatrixStack matrices,
      final VertexConsumerProvider vertexConsumers,
      final int light,
      final LivingEntity entity,
      final float limbAngle,
      final float limbDistance,
      final float tickDelta,
      final float animationProgress,
      final float headYaw,
      final float headPitch) {
    if (!(contextModel instanceof BipedEntityModel base)) {
      return;
    }
    final EquipmentSlot body = AccessorySlot.bodyOf(slot.inventory().getSlotType().getGroup());
    if (GeoRenderProvider.of(stack).getGeoArmorRenderer(entity, stack, body, base)
        instanceof GeoArmorRenderer<?> renderer) {
      base.copyBipedStateTo(renderer);
      renderer.prepForRender(
          entity,
          stack,
          body,
          base,
          vertexConsumers,
          tickDelta,
          limbAngle,
          limbDistance,
          headYaw,
          headPitch);
      renderer.render(matrices, null, light, OverlayTexture.DEFAULT_UV, Color.WHITE.argbInt());
    }
  }
}
