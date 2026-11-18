package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.Dressed;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

public class ClothingModel<T extends Item & GeoItem & Dressed> extends GeoModel<T> {

  private ClothingCut cut = ClothingCut.MALE;

  void fit(final ClothingCut fitted) {
    this.cut = fitted;
  }

  @Override
  public Identifier getModelResource(final T item) {
    return ClothingArt.geo(item.artId(), this.cut);
  }

  @Override
  public Identifier getTextureResource(final T item) {
    return ClothingArt.texture(item.artId(), this.cut);
  }

  @Override
  public Identifier getAnimationResource(final T item) {
    return ClothingArt.animation(item.artId());
  }
}
