package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.guild.party.HunterTentBlockEntity;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class HunterTentRenderer extends GeoBlockRenderer<HunterTentBlockEntity> {

  static final String ART = "hunter_tent";

  public HunterTentRenderer(final BlockEntityRendererFactory.Context context) {
    super(new FixedArtModel<>(ART));
  }

  @Override
  public boolean rendersOutsideBoundingBox(final HunterTentBlockEntity tent) {
    return true;
  }
}
