package com.grahambartley.nightborn.clothing;

import java.util.Locale;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.constant.DataTickets;

public enum ClothingMotion {
  IDLE,
  WALK;

  static final float WALKING_LIMB_SPEED = 0.1F;
  static final int TRANSITION_TICKS = 4;

  private final RawAnimation animation = RawAnimation.begin().thenLoop(clip());

  public static ClothingMotion of(final float limbSpeed) {
    return limbSpeed > WALKING_LIMB_SPEED ? WALK : IDLE;
  }

  public static <T extends GeoAnimatable> AnimationController<T> controller(final T animatable) {
    return new AnimationController<>(
        animatable, "motion", TRANSITION_TICKS, ClothingMotion::motion);
  }

  public String clip() {
    return name().toLowerCase(Locale.ROOT);
  }

  private static <T extends GeoAnimatable> PlayState motion(final AnimationState<T> state) {
    final Entity wearer = state.getData(DataTickets.ENTITY);
    final float limbSpeed =
        wearer instanceof LivingEntity living ? living.limbAnimator.getSpeed() : 0;
    return state.setAndContinue(of(limbSpeed).animation);
  }
}
