package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.trap.NetEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class NetRenderer extends GeoEntityRenderer<NetEntity> {

  static final Identifier RIG = Identifier.of(Nightborn.MOD_ID, "net");
  static final float TILT = 55.0F;

  public NetRenderer(final EntityRendererFactory.Context context) {
    super(context, new DefaultedEntityGeoModel<>(RIG, false));
    this.shadowRadius = 0.0F;
  }

  @Override
  protected void applyRotations(
      final NetEntity net,
      final MatrixStack poseStack,
      final float ageInTicks,
      final float rotationYaw,
      final float partialTick,
      final float nativeScale) {
    super.applyRotations(net, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
    poseStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(TILT));
  }
}
