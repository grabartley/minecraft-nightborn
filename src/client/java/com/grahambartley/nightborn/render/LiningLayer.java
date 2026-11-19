package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.Lining;
import java.util.Optional;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class LiningLayer<T extends Item & GeoItem> extends GeoRenderLayer<T> {

  private final GeoArmorRenderer<T> armor;

  public LiningLayer(final GeoArmorRenderer<T> armor) {
    super(armor);
    this.armor = armor;
  }

  @Override
  public void render(
      final MatrixStack poseStack,
      final T animatable,
      final BakedGeoModel bakedModel,
      final RenderLayer renderType,
      final VertexConsumerProvider bufferSource,
      final VertexConsumer buffer,
      final float partialTick,
      final int packedLight,
      final int packedOverlay) {
    for (final Sheen sheen :
        Sheen.of(Optional.empty(), false, Lining.lined(this.armor.getCurrentStack()))) {
      final RenderLayer worn = SheenLayers.worn(sheen);
      this.armor.reRender(
          bakedModel,
          poseStack,
          bufferSource,
          animatable,
          worn,
          bufferSource.getBuffer(worn),
          partialTick,
          packedLight,
          OverlayTexture.DEFAULT_UV,
          -1);
    }
  }
}
