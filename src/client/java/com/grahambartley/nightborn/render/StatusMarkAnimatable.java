package com.grahambartley.nightborn.render;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

public final class StatusMarkAnimatable implements GeoAnimatable {

  static final StatusMarkAnimatable NET = new StatusMarkAnimatable("net_drape", "settle");
  static final StatusMarkAnimatable HEX = new StatusMarkAnimatable("hex_mark", "turn");
  static final StatusMarkAnimatable QUARRY = new StatusMarkAnimatable("quarry_mark", "pulse");

  private final String rig;
  private final RawAnimation clip;
  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  private StatusMarkAnimatable(final String rig, final String clip) {
    this.rig = rig;
    this.clip = RawAnimation.begin().thenLoop(clip);
  }

  String rig() {
    return rig;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(this, "loop", 0, state -> state.setAndContinue(clip)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }

  @Override
  public double getTick(final Object object) {
    return RenderUtil.getCurrentTick();
  }
}
