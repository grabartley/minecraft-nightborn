package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.occult.RestlessDeadEntity;
import com.grahambartley.nightborn.occult.RestlessLook;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public class RestlessDeadModel extends GeoModel<RestlessDeadEntity> {

  static final Identifier CLIPS =
      Identifier.of(Nightborn.MOD_ID, "animations/entity/restless_dead.animation.json");

  @Override
  public Identifier getModelResource(final RestlessDeadEntity dead) {
    return dead.wornLook().map(RestlessLook::rig).orElse(VampireRig.MALE).geo();
  }

  @Override
  public Identifier getTextureResource(final RestlessDeadEntity dead) {
    return dead.wornLook().map(RestlessLook::texture).orElse(RestlessLook.MISSING);
  }

  @Override
  public Identifier getAnimationResource(final RestlessDeadEntity dead) {
    return CLIPS;
  }

  @Override
  public void setCustomAnimations(
      final RestlessDeadEntity dead,
      final long instanceId,
      final AnimationState<RestlessDeadEntity> animationState) {
    RigArmorLayer.hideAnchors(getAnimationProcessor());
    final GeoBone bust = getAnimationProcessor().getBone(BustShape.BONE);
    if (bust != null) {
      bust.setHidden(false);
    }
    final GeoBone head = getAnimationProcessor().getBone("head");
    if (head != null && !dead.held()) {
      HeadLook.apply(head, animationState.getData(DataTickets.ENTITY_MODEL_DATA));
    }
  }
}
