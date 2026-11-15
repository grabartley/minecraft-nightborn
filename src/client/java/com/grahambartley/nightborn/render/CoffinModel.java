package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.Coffins;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class CoffinModel extends GeoModel<CoffinBlockEntity> {

  private final Map<Block, Identifier> woods =
      Coffins.all().stream()
          .collect(
              Collectors.toUnmodifiableMap(
                  Function.identity(),
                  block -> CoffinArt.planks(Registries.BLOCK.getId(Coffins.wood(block).planks()))));

  @Override
  public Identifier getModelResource(final CoffinBlockEntity coffin) {
    return CoffinArt.GEO;
  }

  @Override
  public Identifier getTextureResource(final CoffinBlockEntity coffin) {
    return this.woods.get(coffin.getCachedState().getBlock());
  }

  @Override
  public Identifier getAnimationResource(final CoffinBlockEntity coffin) {
    return CoffinArt.ANIMATION;
  }
}
