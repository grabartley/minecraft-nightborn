package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Identifier;

public enum CombatMove {
  LEAP(
      "entity.goat.long_jump",
      s -> new Tuning(s.leapMinRange(), s.leapMaxRange(), s.leapPush(), s.leapLift())),
  DASH(
      "entity.breeze.wind_burst",
      s -> new Tuning(s.dashMinRange(), s.dashMaxRange(), s.dashPush(), s.dashLift())),
  DODGE("entity.breeze.slide", s -> new Tuning(0, 0, s.dodgePush(), s.dodgeLift()));

  private static final double HUNDREDTHS = 100.0;

  private final Identifier sound;
  private final Function<CombatSettings, Tuning> tuning;

  CombatMove(final String sound, final Function<CombatSettings, Tuning> tuning) {
    this.sound = Identifier.ofVanilla(sound);
    this.tuning = tuning;
  }

  public record Tuning(int minRange, int maxRange, int push, int lift) {

    public boolean reaches(final double distance) {
      return distance >= minRange && distance <= maxRange;
    }

    public double pushBlocks() {
      return push / HUNDREDTHS;
    }

    public double liftBlocks() {
      return lift / HUNDREDTHS;
    }
  }

  public record Threat(boolean swinging, boolean aiming) {

    public boolean at(final double distance, final CombatSettings settings) {
      return (swinging && distance <= settings.swingThreatRange())
          || (aiming && distance >= settings.aimThreatRange());
    }
  }

  public static boolean dodgesWithPower(
      final boolean threatened,
      final boolean wasThreatened,
      final int roll,
      final StageCombat combat) {
    return threatened && !wasThreatened && roll < combat.powerDodgeChance();
  }

  public static Optional<CombatMove> decide(
      final double distance,
      final boolean onGround,
      final Threat threat,
      final int roll,
      final StageCombat combat,
      final CombatSettings settings) {
    if (!onGround) {
      return Optional.empty();
    }
    if (threat.at(distance, settings) && roll < combat.dodgeChance()) {
      return Optional.of(DODGE);
    }
    if (LEAP.tuning(settings).reaches(distance) && roll < combat.leapChance()) {
      return Optional.of(LEAP);
    }
    if (DASH.tuning(settings).reaches(distance) && roll < combat.dashChance()) {
      return Optional.of(DASH);
    }
    return Optional.empty();
  }

  public Tuning tuning(final CombatSettings settings) {
    return tuning.apply(settings);
  }

  public Identifier sound() {
    return sound;
  }

  public String clip() {
    return name().toLowerCase(Locale.ROOT);
  }
}
