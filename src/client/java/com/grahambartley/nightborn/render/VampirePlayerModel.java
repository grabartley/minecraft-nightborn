package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.VampireLook;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public final class VampirePlayerModel extends GeoModel<VampirePlayerAnimatable> {

  static final Identifier STEVE = Identifier.ofVanilla("textures/entity/player/wide/steve.png");

  private final Identifier geo;

  public VampirePlayerModel(final boolean slim) {
    this.geo =
        Identifier.of(
            Nightborn.MOD_ID,
            "geo/entity/vampire_player_" + (slim ? "slim" : "wide") + ".geo.json");
  }

  @Override
  public Identifier getModelResource(final VampirePlayerAnimatable animatable) {
    return geo;
  }

  @Override
  public Identifier getTextureResource(final VampirePlayerAnimatable animatable) {
    return STEVE;
  }

  @Override
  public Identifier getAnimationResource(final VampirePlayerAnimatable animatable) {
    return VampireLook.animation();
  }

  @Override
  public void setCustomAnimations(
      final VampirePlayerAnimatable animatable,
      final long instanceId,
      final AnimationState<VampirePlayerAnimatable> animationState) {
    VampirePlayerArmorLayer.PIECES
        .keySet()
        .forEach(
            name -> {
              final GeoBone anchor = getAnimationProcessor().getBone(name);
              if (anchor != null) {
                anchor.setHidden(true);
              }
            });
    final GeoBone head = getAnimationProcessor().getBone("head");
    if (head == null) {
      return;
    }
    head.setTrackingMatrices(true);
    final EntityModelData data = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
    head.setRotX(head.getRotX() + data.headPitch() * MathHelper.RADIANS_PER_DEGREE);
    head.setRotY(head.getRotY() + data.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
  }
}
