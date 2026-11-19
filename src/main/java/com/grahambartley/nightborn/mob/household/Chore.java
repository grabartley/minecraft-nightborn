package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.decor.FlameGesture;
import com.grahambartley.nightborn.mob.VampirePose;
import java.util.Optional;

public enum Chore {
  LIGHT,
  DOUSE,
  OPEN_DRAPES,
  CLOSE_DRAPES;

  public static Optional<Chore> forLight(final NightPhase phase, final boolean lit) {
    if (!phase.choresDue() || lit == phase.awake()) {
      return Optional.empty();
    }
    return Optional.of(lit ? DOUSE : LIGHT);
  }

  public static Optional<Chore> forDrape(final NightPhase phase, final boolean open) {
    if (!phase.choresDue() || open == phase.awake()) {
      return Optional.empty();
    }
    return Optional.of(open ? CLOSE_DRAPES : OPEN_DRAPES);
  }

  public boolean lights() {
    return this == LIGHT;
  }

  public Optional<VampirePose> gesture() {
    return this == LIGHT || this == DOUSE
        ? Optional.of(FlameGesture.of(lights()))
        : Optional.empty();
  }

  public boolean opens() {
    return this == OPEN_DRAPES;
  }
}
