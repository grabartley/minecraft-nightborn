package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class SitTask implements HouseholdTask {

  static final double SIT_REACH = 1.6;
  static final int GIVE_UP_TICKS = 400;

  private final VampireEntity vampire;
  private final ServerWorld world;
  private final BlockPos seat;
  private final Walk walk;
  private final Countdown sitting;
  private final Countdown approach = new Countdown(GIVE_UP_TICKS);

  public SitTask(
      final VampireEntity vampire, final ServerWorld world, final BlockPos seat, final int ticks) {
    this.vampire = vampire;
    this.world = world;
    this.seat = seat;
    this.walk = new Walk(vampire);
    this.sitting = new Countdown(ticks);
  }

  @Override
  public boolean tick() {
    if (vampire.getVehicle() instanceof SeatEntity) {
      return sitting.running();
    }
    if (!FurnitureUse.seatFree(world, seat) || !approach.running()) {
      return false;
    }
    if (walk.toward(Vec3d.ofBottomCenter(seat), SIT_REACH)) {
      FurnitureUse.sit(world, seat, vampire);
    }
    return true;
  }

  @Override
  public void stop() {
    if (vampire.getVehicle() instanceof SeatEntity) {
      vampire.stopRiding();
    }
  }
}
