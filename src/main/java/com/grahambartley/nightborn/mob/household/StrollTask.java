package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.util.math.Vec3d;

public final class StrollTask implements HouseholdTask {

  static final int HORIZONTAL = 8;
  static final int VERTICAL = 3;
  static final double PAUSE_REACH = 1.5;

  private final VampireEntity vampire;
  private final Walk walk;
  private final Countdown strolling;
  private final boolean endsOnArrival;
  private Vec3d spot;

  private StrollTask(
      final VampireEntity vampire, final Vec3d spot, final int ticks, final boolean endsOnArrival) {
    this.vampire = vampire;
    this.walk = new Walk(vampire);
    this.strolling = new Countdown(ticks);
    this.spot = spot;
    this.endsOnArrival = endsOnArrival;
  }

  public static StrollTask to(final VampireEntity vampire, final Vec3d spot, final int ticks) {
    return new StrollTask(vampire, spot, ticks, true);
  }

  public static StrollTask around(final VampireEntity vampire, final int ticks) {
    final Vec3d spot = NoPenaltyTargeting.find(vampire, HORIZONTAL, VERTICAL);
    return new StrollTask(vampire, spot == null ? vampire.getPos() : spot, ticks, false);
  }

  @Override
  public boolean tick() {
    if (walk.toward(spot, PAUSE_REACH)) {
      if (endsOnArrival) {
        return false;
      }
      final Vec3d next = NoPenaltyTargeting.find(vampire, HORIZONTAL, VERTICAL);
      if (next != null && vampire.getRandom().nextInt(4) == 0) {
        spot = next;
      }
    }
    return strolling.running();
  }
}
