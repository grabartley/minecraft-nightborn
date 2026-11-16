package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.VampireEntity;

public final class StandTask implements HouseholdTask {

  private final VampireEntity vampire;
  private final Countdown standing;

  public StandTask(final VampireEntity vampire, final int ticks) {
    this.vampire = vampire;
    this.standing = new Countdown(ticks);
  }

  @Override
  public boolean tick() {
    vampire.getNavigation().stop();
    return standing.running();
  }
}
