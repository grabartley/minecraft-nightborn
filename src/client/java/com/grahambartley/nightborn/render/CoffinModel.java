package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.Coffins;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class CoffinModel extends GeoModel<CoffinBlockEntity> {

  @Override
  public Identifier getModelResource(final CoffinBlockEntity coffin) {
    return CoffinArt.GEO;
  }

  @Override
  public Identifier getTextureResource(final CoffinBlockEntity coffin) {
    return CoffinArt.planks(
        Registries.BLOCK.getId(Coffins.wood(coffin.getCachedState().getBlock()).planks()));
  }

  @Override
  public Identifier getAnimationResource(final CoffinBlockEntity coffin) {
    return CoffinArt.ANIMATION;
  }
}
