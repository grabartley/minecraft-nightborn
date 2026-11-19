package com.grahambartley.nightborn.occult;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BatSwarmEntity extends Entity implements GeoEntity {

  static final RawAnimation CIRCLE = RawAnimation.begin().thenLoop("circle");
  static final RawAnimation FLAP = RawAnimation.begin().thenLoop("flap");

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public BatSwarmEntity(final EntityType<? extends BatSwarmEntity> type, final World world) {
    super(type, world);
    this.noClip = true;
    setNoGravity(true);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {}

  @Override
  public boolean shouldSave() {
    return false;
  }

  @Override
  protected void readCustomDataFromNbt(final NbtCompound nbt) {}

  @Override
  protected void writeCustomDataToNbt(final NbtCompound nbt) {}

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(this, "circle", 0, state -> state.setAndContinue(CIRCLE)));
    controllers.add(
        new AnimationController<>(this, "flap", 0, state -> state.setAndContinue(FLAP)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
