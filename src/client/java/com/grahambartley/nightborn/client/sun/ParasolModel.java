package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.sun.ParasolItem;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ParasolModel extends GeoModel<ParasolItem> {

  @Override
  public Identifier getModelResource(final ParasolItem parasol) {
    return Identifier.of(Nightborn.MOD_ID, "geo/item/parasol.geo.json");
  }

  @Override
  public Identifier getTextureResource(final ParasolItem parasol) {
    return Identifier.of(Nightborn.MOD_ID, "textures/item/parasol.png");
  }

  @Override
  public Identifier getAnimationResource(final ParasolItem parasol) {
    return Identifier.of(Nightborn.MOD_ID, "animations/item/parasol.animation.json");
  }
}
