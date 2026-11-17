package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.ritual.RitualAltarBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class RitualAltarModel extends GeoModel<RitualAltarBlockEntity> {

  static final String ART = "ritual_altar";

  @Override
  public Identifier getModelResource(final RitualAltarBlockEntity altar) {
    return FurnitureArt.geo(ART);
  }

  @Override
  public Identifier getTextureResource(final RitualAltarBlockEntity altar) {
    return FurnitureArt.texture(Registries.BLOCK.getId(altar.getCachedState().getBlock()));
  }

  @Override
  public Identifier getAnimationResource(final RitualAltarBlockEntity altar) {
    return FurnitureArt.animation(ART);
  }
}
