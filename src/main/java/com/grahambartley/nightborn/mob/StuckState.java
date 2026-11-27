package com.grahambartley.nightborn.mob;

import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record StuckState(
    StuckWatch watch,
    int patience,
    long holdUntil,
    long backUntil,
    @Nullable Vec3d backTo,
    @Nullable UUID forgone,
    long forgoneUntil) {

  public static final StuckState FRESH =
      new StuckState(StuckWatch.FRESH, Integer.MAX_VALUE, 0L, 0L, null, null, 0L);

  static final int HOLD_PATIENCES = 3;
  static final int BACK_PATIENCES = 1;
  static final int FORGO_PATIENCES = 10;

  public StuckState observe(final double distance, final boolean pursuing, final int patience) {
    return new StuckState(
        watch.next(distance, pursuing),
        Math.max(1, patience),
        holdUntil,
        backUntil,
        backTo,
        forgone,
        forgoneUntil);
  }

  public StuckWatch.Step step() {
    return watch.step(patience);
  }

  public boolean stuck() {
    return watch.stuck(patience);
  }

  public StuckState landed(final long now) {
    return new StuckState(
        watch,
        patience,
        now + (long) patience * HOLD_PATIENCES,
        backUntil,
        backTo,
        forgone,
        forgoneUntil);
  }

  public StuckState steppingBack(@Nullable final Vec3d spot, final long now) {
    return new StuckState(
        watch.rerouted(),
        patience,
        now + (long) patience * HOLD_PATIENCES,
        spot == null ? backUntil : now + (long) patience * BACK_PATIENCES,
        spot == null ? backTo : spot,
        forgone,
        forgoneUntil);
  }

  public StuckState gaveUp(final UUID target, final long now) {
    return new StuckState(
        StuckWatch.FRESH,
        patience,
        holdUntil,
        0L,
        null,
        target,
        now + (long) patience * FORGO_PATIENCES);
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
}
