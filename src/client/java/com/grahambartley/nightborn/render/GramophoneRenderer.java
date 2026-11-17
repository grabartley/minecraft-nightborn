package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.gramophone.GramophoneBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class GramophoneRenderer extends GeoBlockRenderer<GramophoneBlockEntity> {

  static final String ART = "gramophone";

  public GramophoneRenderer(final BlockEntityRendererFactory.Context context) {
    this();
  }

  public GramophoneRenderer() {
    super(new FixedArtModel<>(ART));
  }
}
