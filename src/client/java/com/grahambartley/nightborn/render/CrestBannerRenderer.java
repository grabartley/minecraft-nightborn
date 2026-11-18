package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.house.crest.CrestBannerBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CrestBannerRenderer extends GeoBlockRenderer<CrestBannerBlockEntity> {

  static final String ART = "crest_banner";

  public CrestBannerRenderer(final BlockEntityRendererFactory.Context context) {
    this();
  }

  public CrestBannerRenderer() {
    super(new FixedArtModel<>(ART));
    addRenderLayer(new CrestDyeLayer(this));
  }

  @Override
  public boolean rendersOutsideBoundingBox(final CrestBannerBlockEntity banner) {
    return true;
  }
}
