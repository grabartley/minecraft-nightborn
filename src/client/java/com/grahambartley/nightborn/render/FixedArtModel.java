package com.grahambartley.nightborn.render;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

public class FixedArtModel<T extends BlockEntity & GeoAnimatable> extends GeoModel<T> {

  private final String art;

  public FixedArtModel(final String art) {
    this.art = art;
  }

  @Override
  public Identifier getModelResource(final T block) {
    return FurnitureArt.geo(art);
  }

  @Override
  public Identifier getTextureResource(final T block) {
    return FurnitureArt.texture(Registries.BLOCK.getId(block.getCachedState().getBlock()));
  }

  @Override
  public Identifier getAnimationResource(final T block) {
    return FurnitureArt.animation(art);
  }
}
