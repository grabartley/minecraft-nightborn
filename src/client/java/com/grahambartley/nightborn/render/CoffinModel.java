package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.Coffins;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class CoffinModel extends GeoModel<CoffinBlockEntity> {

  private final Map<Block, Identifier> woods = new HashMap<>();

  @Override
  public Identifier getModelResource(final CoffinBlockEntity coffin) {
    return CoffinArt.GEO;
  }

  @Override
  public Identifier getTextureResource(final CoffinBlockEntity coffin) {
    return this.woods.computeIfAbsent(
        coffin.getCachedState().getBlock(),
        block -> CoffinArt.planks(Registries.BLOCK.getId(Coffins.wood(block).planks())));
  }

  @Override
  public Identifier getAnimationResource(final CoffinBlockEntity coffin) {
    return CoffinArt.ANIMATION;
  }
}
