package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.gramophone.GramophoneBlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class GramophoneModel extends GeoModel<GramophoneBlockEntity> {

  static final String ART = "gramophone";

  @Override
  public Identifier getModelResource(final GramophoneBlockEntity gramophone) {
    return FurnitureArt.geo(ART);
  }

  @Override
  public Identifier getTextureResource(final GramophoneBlockEntity gramophone) {
    return FurnitureArt.texture(Registries.BLOCK.getId(gramophone.getCachedState().getBlock()));
  }

  @Override
  public Identifier getAnimationResource(final GramophoneBlockEntity gramophone) {
    return FurnitureArt.animation(ART);
  }
}
