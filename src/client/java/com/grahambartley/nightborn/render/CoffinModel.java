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

  private final Map<Block, Identifier> textures =
      Coffins.all().stream()
          .collect(
              Collectors.toUnmodifiableMap(
                  Function.identity(),
                  block ->
                      CoffinArt.texture(Registries.BLOCK.getId(Coffins.material(block).source()))));

  @Override
  public Identifier getModelResource(final CoffinBlockEntity coffin) {
    return CoffinArt.geo(Coffins.material(coffin.getCachedState().getBlock()).style());
  }

  @Override
  public Identifier getTextureResource(final CoffinBlockEntity coffin) {
    return this.textures.get(coffin.getCachedState().getBlock());
  }

  @Override
  public Identifier getAnimationResource(final CoffinBlockEntity coffin) {
    return CoffinArt.animation(Coffins.material(coffin.getCachedState().getBlock()).style());
  }
}
