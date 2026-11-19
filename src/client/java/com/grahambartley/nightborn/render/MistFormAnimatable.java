package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.NightbornMobs;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class MistFormAnimatable implements GeoReplacedEntity {

  static final RawAnimation DRIFT = RawAnimation.begin().thenLoop("drift");

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  @Override
  public EntityType<?> getReplacingEntityType() {
    return NightbornMobs.VAMPIRE;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(this, "drift", 0, state -> state.setAndContinue(DRIFT)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
