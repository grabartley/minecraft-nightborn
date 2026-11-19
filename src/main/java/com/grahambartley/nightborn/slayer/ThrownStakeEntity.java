package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.gear.GearItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ThrownStakeEntity extends ThrownItemEntity {

  static final double GRAVITY = 0.01;

  @Nullable private StakeThrowPower power;
  private boolean returns;

  public ThrownStakeEntity(final EntityType<? extends ThrownStakeEntity> type, final World world) {
    super(type, world);
  }

  ThrownStakeEntity(
      final World world,
      final LivingEntity owner,
      final StakeThrowPower power,
      final boolean returns) {
    super(SlayerEntities.THROWN_STAKE, owner, world);
    this.power = power;
    this.returns = returns;
  }

  @Override
  protected Item getDefaultItem() {
    return GearItems.WOODEN_STAKE;
  }

  @Override
  protected double getGravity() {
    return GRAVITY;
  }

  @Override
  public void tick() {
    super.tick();
    if (!getWorld().isClient() && !isRemoved() && (power == null || age > power.flightTicks())) {
      drop(getPos());
    }
  }

  @Override
  protected void onEntityHit(final EntityHitResult hit) {
    super.onEntityHit(hit);
    if (!getWorld().isClient()
        && power != null
        && getOwner() instanceof LivingEntity owner
        && hit.getEntity() instanceof LivingEntity target) {
      StakeThrows.land(this, owner, target, power);
    }
  }

  @Override
  protected void onCollision(final HitResult hit) {
    super.onCollision(hit);
    if (!getWorld().isClient() && !isRemoved()) {
      drop(hit.getPos());
    }
  }

  private void drop(final Vec3d at) {
    if (!returns) {
      discard();
      return;
    }
    final ItemEntity stake = new ItemEntity(getWorld(), at.x, at.y, at.z, getStack().copy());
    stake.setToDefaultPickupDelay();
    getWorld().spawnEntity(stake);
    discard();
  }

  @Override
  public boolean shouldSave() {
    return false;
  }
}
