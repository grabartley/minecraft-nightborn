package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.clothing.ClothingCut;
import com.grahambartley.nightborn.clothing.ClothingItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ClothingModel extends GeoModel<ClothingItem> {

  private ClothingCut cut = ClothingCut.MALE;

  void fit(final ClothingCut fitted) {
    this.cut = fitted;
  }

  @Override
  public Identifier getModelResource(final ClothingItem item) {
    return ClothingArt.geo(item.piece(), this.cut);
  }

  @Override
  public Identifier getTextureResource(final ClothingItem item) {
    return ClothingArt.texture(item.piece(), this.cut);
  }

  @Override
  public Identifier getAnimationResource(final ClothingItem item) {
    return ClothingArt.animation(item.piece());
  }
}
