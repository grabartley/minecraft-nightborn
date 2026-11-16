package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatPose;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public final class BatFormAnimatable implements GeoReplacedEntity {

  public static final BatFormAnimatable INSTANCE = new BatFormAnimatable();

  static final int BLEND_TICKS = 3;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  private BatFormAnimatable() {}

  @Override
  public EntityType<?> getReplacingEntityType() {
    return NightbornMobs.VAMPIRE_BAT;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "flight",
            BLEND_TICKS,
            state ->
                state.setAndContinue(
                    VampireBatPose.of(false, false, BatFormPose.swoops(state.getLimbSwingAmount()))
                        .clip())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
