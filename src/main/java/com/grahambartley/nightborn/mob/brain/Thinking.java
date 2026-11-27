package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillPower;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class Thinking {

  private Thinking() {}

  public static Optional<Think> think(
      final MobEntity mob, final boolean dodge, final int mobFloor) {
    return think(mob, dodge, mobFloor, null);
  }

  public static Optional<Think> think(
      final MobEntity mob,
      final boolean dodge,
      final int mobFloor,
      @Nullable final TargetMotion motion) {
    final Vec3d moved =
        motion == null || mob.getTarget() == null
            ? Vec3d.ZERO
            : motion.sample(mob.getTarget(), mob.getWorld().getTime());
    final Intent intent = Intents.read(mob);
    final List<PowerChoice.Option> options = options(mob, dodge, intent);
    if (options.stream().noneMatch(option -> option.verdict() == PowerUse.Verdict.READY)) {
      return Optional.empty();
    }
    final Situation situation = Situations.read(mob, intent, needed(options), moved);
    final IntentBudget budget = MobIntents.budget(mob.getRegistryManager(), intent);
    return Optional.of(
        new Think(
            intent,
            mob.getWorld().getTime(),
            PowerChoice.rank(options, situation, budget, mobFloor),
            budget,
            mob.getRandom().nextDouble()));
  }

  static Set<Fact> needed(final List<PowerChoice.Option> options) {
    final Set<Fact> needed = EnumSet.noneOf(Fact.class);
    for (final PowerChoice.Option option : options) {
      if (option.verdict() != PowerUse.Verdict.READY) {
        continue;
      }
      for (final MobUse use : option.use().effectiveUses()) {
        for (final Consideration consideration : use.considerations()) {
          Fact.byKey(consideration.fact()).ifPresent(needed::add);
        }
      }
    }
    return needed;
  }

  static boolean offered(final MobPowerUse use, final boolean dodge, final Intent intent) {
    return use.dodge() == dodge || !dodge && !intent.fights();
  }

  static Set<Aim> aims(final MobPowerUse use, final Intent intent) {
    final Set<Aim> aims = EnumSet.noneOf(Aim.class);
    for (final MobUse each : use.effectiveUses()) {
      if (each.intent().equals(intent.key())) {
        aims.add(each.aimed());
      }
    }
    return aims;
  }

  static PowerUse.Verdict overall(final List<PowerUse.Verdict> verdicts) {
    return verdicts.contains(PowerUse.Verdict.READY)
        ? PowerUse.Verdict.READY
        : verdicts.stream().findFirst().orElse(PowerUse.Verdict.NOWHERE);
  }

  static List<PowerChoice.Option> options(
      final MobEntity mob, final boolean dodge, final Intent intent) {
    List<PowerChoice.Option> options = List.of();
    for (final Identifier id : SkillAttachment.owned(mob).ownedInOrder()) {
      final Optional<SkillPower> power = Powers.power(mob, id);
      final MobPowerUse use =
          power.flatMap(SkillPower::mob).filter(each -> offered(each, dodge, intent)).orElse(null);
      if (use == null) {
        continue;
      }
      final Set<Aim> aims = aims(use, intent);
      if (aims.isEmpty()) {
        continue;
      }
      final Set<Aim> ready = EnumSet.noneOf(Aim.class);
      final List<PowerUse.Verdict> verdicts = new ArrayList<>(aims.size());
      for (final Aim aim : aims) {
        final PowerUse.Verdict verdict =
            CastAims.resolve(mob, aim)
                .map(cast -> Powers.check(mob, id, power, cast))
                .orElse(PowerUse.Verdict.NOWHERE);
        verdicts.add(verdict);
        if (verdict == PowerUse.Verdict.READY) {
          ready.add(aim);
        }
      }
      if (options.isEmpty()) {
        options = new ArrayList<>();
      }
      final SkillPower owned = power.get();
      final PowerUse.Verdict verdict = overall(verdicts);
      options.add(
          new PowerChoice.Option(
              id,
              verdict,
              owned.blood(),
              Powers.cooldownTicks(mob, id, owned),
              verdict == PowerUse.Verdict.READY && RunningPowers.on(mob, id, owned),
              use,
              ready));
    }
    return options;
  }
}
