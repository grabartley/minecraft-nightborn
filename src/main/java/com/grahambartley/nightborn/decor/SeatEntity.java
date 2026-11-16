package com.grahambartley.nightborn.decor;

import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SeatEntity extends Entity {

  public SeatEntity(final EntityType<? extends SeatEntity> type, final World world) {
    super(type, world);
    this.noClip = true;
    this.setNoGravity(true);
    this.setInvisible(true);
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient
        && !Seating.keepsSeat(
            this.hasPassengers(),
            this.getWorld().getBlockState(this.getBlockPos()).getBlock() instanceof ChairBlock)) {
      this.discard();
    }
  }

  @Override
  protected void updatePassengerPosition(
      final Entity passenger, final Entity.PositionUpdater positionUpdater) {
    super.updatePassengerPosition(passenger, positionUpdater);
    if (passenger instanceof MobEntity sitter) {
      sitter.setYaw(this.getYaw());
      sitter.setBodyYaw(this.getYaw());
      sitter.prevBodyYaw = this.getYaw();
      sitter.setHeadYaw(Seating.headYaw(this.getYaw(), sitter.getHeadYaw()));
    }
  }

  @Override
  public Vec3d updatePassengerForDismount(final LivingEntity passenger) {
    for (final Direction side : Seating.dismountSides(Direction.fromRotation(this.getYaw()))) {
      final Vec3d spot =
          Dismounting.findRespawnPos(
              passenger.getType(), this.getWorld(), this.getBlockPos().offset(side), false);
      if (spot != null) {
        return spot;
      }
    }
    return super.updatePassengerForDismount(passenger);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {}

  @Override
  protected void readCustomDataFromNbt(final NbtCompound nbt) {}

  @Override
  protected void writeCustomDataToNbt(final NbtCompound nbt) {}
}
