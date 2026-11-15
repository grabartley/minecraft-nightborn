package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.decor.FurnitureBlockEntity;
import java.util.Set;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class FurnitureModel extends GeoModel<FurnitureBlockEntity> {

  @Override
  public Identifier getModelResource(final FurnitureBlockEntity furniture) {
    return FurnitureArt.geo(furniture.furniture().artModel());
  }

  @Override
  public Identifier getTextureResource(final FurnitureBlockEntity furniture) {
    return FurnitureArt.texture(Registries.BLOCK.getId(furniture.getCachedState().getBlock()));
  }

  @Override
  public Identifier getAnimationResource(final FurnitureBlockEntity furniture) {
    return FurnitureArt.animation(furniture.furniture().artModel());
  }

  @Override
  public void setCustomAnimations(
      final FurnitureBlockEntity furniture,
      final long instanceId,
      final AnimationState<FurnitureBlockEntity> animationState) {
    final Set<String> hidden = furniture.furniture().hiddenBones(furniture.getCachedState());
    for (final GeoBone bone : this.getAnimationProcessor().getRegisteredBones()) {
      bone.setHidden(hidden.contains(bone.getName()));
    }
  }
}
