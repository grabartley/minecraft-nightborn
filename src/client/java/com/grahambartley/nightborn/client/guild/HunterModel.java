package com.grahambartley.nightborn.client.guild;

import com.grahambartley.nightborn.guild.HunterLook;
import com.grahambartley.nightborn.guild.HunterLooking;
import com.grahambartley.nightborn.render.HeadLook;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

public class HunterModel<T extends LivingEntity & GeoEntity & HunterLooking> extends GeoModel<T> {

  static final String HEAD = "head";

  @Override
  public Identifier getModelResource(final T hunter) {
    return hunter.look().geo();
  }

  @Override
  public Identifier getTextureResource(final T hunter) {
    return hunter.look().texture();
  }

  @Override
  public Identifier getAnimationResource(final T hunter) {
    return HunterLook.ANIMATION;
  }

  @Override
  public void setCustomAnimations(
      final T hunter, final long instanceId, final AnimationState<T> animationState) {
    final GeoBone head = getAnimationProcessor().getBone(HEAD);
    if (head == null) {
      return;
    }
    HeadLook.apply(head, animationState.getData(DataTickets.ENTITY_MODEL_DATA));
  }
}
