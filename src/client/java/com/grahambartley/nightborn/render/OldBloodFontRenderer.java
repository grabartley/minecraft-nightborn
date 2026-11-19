package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.oldblood.OldBloodFontBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class OldBloodFontRenderer extends GeoBlockRenderer<OldBloodFontBlockEntity> {

  static final String ART = "old_blood_font";

  public OldBloodFontRenderer(final BlockEntityRendererFactory.Context context) {
    super(new FixedArtModel<>(ART));
    addRenderLayer(new AutoGlowingGeoLayer<>(this));
  }
}
