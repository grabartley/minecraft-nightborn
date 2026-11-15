package com.grahambartley.nightborn.decor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.nbt.NbtCompound;
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
        && (!this.hasPassengers()
            || !(this.getWorld().getBlockState(this.getBlockPos()).getBlock()
                instanceof ChairBlock))) {
      this.discard();
    }
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {}

  @Override
  protected void readCustomDataFromNbt(final NbtCompound nbt) {}

  @Override
  protected void writeCustomDataToNbt(final NbtCompound nbt) {}
}
