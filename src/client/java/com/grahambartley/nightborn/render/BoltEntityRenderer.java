package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.ammo.BoltEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ProjectileEntityRenderer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class BoltEntityRenderer extends ProjectileEntityRenderer<BoltEntity> {

  public BoltEntityRenderer(final EntityRendererFactory.Context context) {
    super(context);
  }

  @Override
  public Identifier getTexture(final BoltEntity bolt) {
    return texture(Registries.ITEM.getId(bolt.getItemStack().getItem()));
  }

  static Identifier texture(final Identifier ammo) {
    return Identifier.of(
        ammo.getNamespace(), "textures/entity/projectiles/" + ammo.getPath() + ".png");
  }
}
