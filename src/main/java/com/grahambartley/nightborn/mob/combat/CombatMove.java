package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.stage.StageCombat;
import java.util.Locale;
import java.util.Optional;

public enum CombatMove {
  LEAP(3.0, 7.0, 0.9, 0.45),
  DASH(7.0, 16.0, 1.3, 0.12),
  DODGE(0.0, 0.0, 0.85, 0.25);

  public static final double SWING_THREAT_RANGE = 4.0;
  public static final double AIM_THREAT_RANGE = 5.0;
  public static final int COOLDOWN_THINKS = 3;

  private final double minRange;
  private final double maxRange;
  private final double push;
  private final double lift;

  CombatMove(final double minRange, final double maxRange, final double push, final double lift) {
    this.minRange = minRange;
    this.maxRange = maxRange;
    this.push = push;
    this.lift = lift;
  }

  public record Threat(boolean swinging, boolean aiming) {

    public boolean at(final double distance) {
      return (swinging && distance <= SWING_THREAT_RANGE)
          || (aiming && distance >= AIM_THREAT_RANGE);
    }
  }

  public static Optional<CombatMove> decide(
      final double distance,
      final boolean onGround,
      final Threat threat,
      final int roll,
      final StageCombat combat) {
    if (!onGround) {
      return Optional.empty();
    }
    if (threat.at(distance) && roll < combat.dodgeChance()) {
      return Optional.of(DODGE);
    }
    if (LEAP.reaches(distance) && roll < combat.leapChance()) {
      return Optional.of(LEAP);
    }
    if (DASH.reaches(distance) && roll < combat.dashChance()) {
      return Optional.of(DASH);
    }
    return Optional.empty();
  }

  public boolean reaches(final double distance) {
    return distance >= minRange && distance <= maxRange;
  }

  public double push() {
    return push;
  }

  public double lift() {
    return lift;
  }

  public String clip() {
    return name().toLowerCase(Locale.ROOT);
  }
}
