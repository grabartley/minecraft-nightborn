package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.occult.RestlessDeadEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.util.Identifier;

public final class RestlessDeadRenderer
    extends BipedEntityRenderer<RestlessDeadEntity, BipedEntityModel<RestlessDeadEntity>> {

  static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/zombie/zombie.png");
  static final float SHADOW_RADIUS = 0.5F;

  public RestlessDeadRenderer(final EntityRendererFactory.Context context) {
    super(
        context, new BipedEntityModel<>(context.getPart(EntityModelLayers.ZOMBIE)), SHADOW_RADIUS);
  }

  @Override
  public Identifier getTexture(final RestlessDeadEntity entity) {
    return TEXTURE;
  }
}
