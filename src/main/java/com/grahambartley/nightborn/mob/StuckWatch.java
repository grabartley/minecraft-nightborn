package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.mob.stage.StageStuck;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record StuckWatch(double best, @Nullable Vec3d anchor, int stalled, int reroutes) {

  public static final StuckWatch FRESH = new StuckWatch(Double.NaN, null, 0, 0);

  public enum Step {
    NONE,
    LAND,
    STEP_BACK,
    GIVE_UP
  }

  public StuckWatch next(
      final double gap, final Vec3d at, final boolean pursuing, final StageStuck habits) {
    if (!pursuing || Double.isNaN(gap) || gap <= habits.reach()) {
      return FRESH;
    }
    if (Double.isNaN(best) || gap < best - habits.progress()) {
      return new StuckWatch(gap, at, 0, 0);
    }
    if (anchor == null || at.distanceTo(anchor) > habits.travel()) {
      return new StuckWatch(Math.min(best, gap), at, 0, reroutes);
    }
    return new StuckWatch(best, anchor, stalled + 1, reroutes);
  }

  public boolean stuck(final StageStuck habits) {
    return stalled >= habits.ticks();
  }

  public Step step(final StageStuck habits) {
    if (stalled == habits.ticks()) {
      return Step.LAND;
    }
    if (stalled == habits.ticks() * 2) {
      return reroutes + 1 >= habits.tries() ? Step.GIVE_UP : Step.STEP_BACK;
    }
    return Step.NONE;
  }

  public StuckWatch rerouted() {
    return new StuckWatch(best, anchor, 0, reroutes + 1);
  }
}
