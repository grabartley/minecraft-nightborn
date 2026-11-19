package com.grahambartley.nightborn.trap;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NetEntity extends ThrownItemEntity implements GeoEntity {

  static final double GRAVITY = 0.01;

  static final RawAnimation FLY = RawAnimation.begin().thenLoop("fly");

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  @Nullable private NetPower power;
  private float extraDamage;

  public NetEntity(final EntityType<? extends NetEntity> type, final World world) {
    super(type, world);
  }

  NetEntity(
      final World world, final LivingEntity owner, final NetPower power, final float extraDamage) {
    super(TrapEntities.NET, owner, world);
    this.power = power;
    this.extraDamage = extraDamage;
  }

  @Override
  protected Item getDefaultItem() {
    return Items.COBWEB;
  }

  @Override
  protected double getGravity() {
    return GRAVITY;
  }

  @Override
  public void tick() {
    super.tick();
    if (!getWorld().isClient() && (power == null || age > power.flightTicks())) {
      discard();
    }
  }

  @Override
  protected void onEntityHit(final EntityHitResult hit) {
    super.onEntityHit(hit);
    if (!getWorld().isClient()
        && power != null
        && getOwner() instanceof LivingEntity owner
        && hit.getEntity() instanceof LivingEntity target) {
      Nets.land(this, owner, target, power, extraDamage);
    }
  }

  @Override
  protected void onCollision(final HitResult hit) {
    super.onCollision(hit);
    if (!getWorld().isClient()) {
      discard();
    }
  }

  @Override
  public boolean shouldSave() {
    return false;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(new AnimationController<>(this, "fly", 0, state -> state.setAndContinue(FLY)));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
