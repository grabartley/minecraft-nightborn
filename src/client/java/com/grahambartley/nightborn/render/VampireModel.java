package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.VampireLook;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class VampireModel extends GeoModel<VampireEntity> {

  @Override
  public Identifier getModelResource(final VampireEntity vampire) {
    return vampire.look().geo();
  }

  @Override
  public Identifier getTextureResource(final VampireEntity vampire) {
    return vampire.look().texture();
  }

  @Override
  public Identifier getAnimationResource(final VampireEntity vampire) {
    return VampireLook.animation();
  }
}
