package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import com.grahambartley.nightborn.mob.stage.StageCombat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;
import net.minecraft.util.Identifier;

public enum CombatMove {
  LEAP(
      "entity.goat.long_jump",
      s -> new Tuning(s.leapPush(), s.leapLift()),
      StageCombat::leapChance),
  DASH(
      "entity.breeze.wind_burst",
      s -> new Tuning(s.dashPush(), s.dashLift()),
      StageCombat::dashChance),
  DODGE(
      "entity.breeze.slide",
      s -> new Tuning(s.dodgePush(), s.dodgeLift()),
      StageCombat::dodgeChance);

  private static final double HUNDREDTHS = 100.0;

  private final Identifier id;
  private final Identifier sound;
  private final Function<CombatSettings, Tuning> tuning;
  private final ToIntFunction<StageCombat> chance;

  CombatMove(
      final String sound,
      final Function<CombatSettings, Tuning> tuning,
      final ToIntFunction<StageCombat> chance) {
    this.id = Identifier.of(Nightborn.MOD_ID, name().toLowerCase(Locale.ROOT));
    this.sound = Identifier.ofVanilla(sound);
    this.tuning = tuning;
    this.chance = chance;
  }

  public record Tuning(int push, int lift) {

    public double pushBlocks() {
      return push / HUNDREDTHS;
    }

    public double liftBlocks() {
      return lift / HUNDREDTHS;
    }
  }

  public static Optional<CombatMove> byId(final Identifier id) {
    return Arrays.stream(values()).filter(move -> move.id.equals(id)).findFirst();
  }

  public static boolean reacts(
      final boolean threatened,
      final boolean wasThreatened,
      final IntSupplier roll,
      final StageCombat combat) {
    return threatened && !wasThreatened && roll.getAsInt() < combat.powerDodgeChance();
  }

  public ActionReadiness readiness(
      final boolean free, final IntSupplier roll, final StageCombat combat, final int restTicks) {
    return ActionReadiness.of(id, free && roll.getAsInt() < chance.applyAsInt(combat), restTicks);
  }

  public Identifier id() {
    return id;
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
