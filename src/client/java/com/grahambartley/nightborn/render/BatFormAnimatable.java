package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.batform.BatHang;
import com.grahambartley.nightborn.mob.NightbornMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.constant.DataTickets;
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
    controllers.add(flight(this));
  }

  static <T extends GeoAnimatable> AnimationController<T> flight(final T animatable) {
    return new AnimationController<>(
        animatable,
        "flight",
        BLEND_TICKS,
        state ->
            state.setAndContinue(
                BatFormClip.next(
                        pose(state.getData(DataTickets.ENTITY), state.getLimbSwingAmount()),
                        BatFormClip.playing(state.getController().getCurrentRawAnimation()))
                    .clip()));
  }

  private static BatFormPose pose(final Entity entity, final float limbSwingAmount) {
    return BatFormPose.of(
        BatHang.isHanging(entity), entity.isOnGround(), limbSwingAmount, entity.isSprinting());
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
