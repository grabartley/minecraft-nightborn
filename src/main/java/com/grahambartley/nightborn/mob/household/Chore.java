package com.grahambartley.nightborn.mob.household;

import java.util.Optional;

public enum Chore {
  LIGHT,
  DOUSE,
  OPEN_DRAPES,
  CLOSE_DRAPES;

  public static Optional<Chore> forLight(final NightPhase phase, final boolean lit) {
    if (!phase.choresDue() || lit == phase.lightsWanted()) {
      return Optional.empty();
    }
    return Optional.of(lit ? DOUSE : LIGHT);
  }

  public static Optional<Chore> forDrape(final NightPhase phase, final boolean open) {
    if (!phase.choresDue() || open == phase.lightsWanted()) {
      return Optional.empty();
    }
    return Optional.of(open ? CLOSE_DRAPES : OPEN_DRAPES);
  }

  public boolean lights() {
    return this == LIGHT;
  }

  public boolean opens() {
    return this == OPEN_DRAPES;
  }
}
