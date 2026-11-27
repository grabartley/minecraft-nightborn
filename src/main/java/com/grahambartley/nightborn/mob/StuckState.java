package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.mob.stage.StageStuck;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record StuckState(
    StuckWatch watch,
    StageStuck habits,
    long holdUntil,
    long backUntil,
    @Nullable Vec3d backTo,
    @Nullable UUID forgone,
    long forgoneUntil) {

  public static final StuckState FRESH =
      new StuckState(StuckWatch.FRESH, StageStuck.PLAIN, 0L, 0L, null, null, 0L);

  public StuckState observe(
      final double gap, final Vec3d at, final boolean pursuing, final StageStuck stage) {
    final StageStuck kept = watch.stalled() == 0 ? stage : habits;
    return new StuckState(
        watch.next(gap, at, pursuing, kept),
        kept,
        holdUntil,
        pursuing ? backUntil : 0L,
        pursuing ? backTo : null,
        forgone,
        forgoneUntil);
  }

  public StuckWatch.Step step() {
    return watch.step(habits);
  }

  public boolean stuck() {
    return watch.stuck(habits);
  }

  public StuckState landed(final long now) {
    return new StuckState(
        watch, habits, until(now, habits.holdWaits()), backUntil, backTo, forgone, forgoneUntil);
  }

  public StuckState steppingBack(@Nullable final Vec3d spot, final long now) {
    return new StuckState(
        watch.rerouted(),
        habits,
        until(now, habits.holdWaits()),
        spot == null ? 0L : until(now, habits.stepBackWaits()),
        spot,
        forgone,
        forgoneUntil);
  }

  public StuckState backedOff() {
    return new StuckState(watch, habits, holdUntil, 0L, null, forgone, forgoneUntil);
  }

  public StuckState gaveUp(final UUID target, final long now) {
    return new StuckState(
        StuckWatch.FRESH, habits, holdUntil, 0L, null, target, until(now, habits.forgoWaits()));
  }

  public boolean holdsDown(final long now) {
    return now < holdUntil;
  }

  @Nullable
  public Vec3d backingTo(final long now) {
    return now < backUntil ? backTo : null;
  }

  public boolean forgoes(final UUID target, final long now) {
    return now < forgoneUntil && target.equals(forgone);
  }

  private long until(final long now, final int waits) {
    return now + (long) habits.ticks() * waits;
  }
}
