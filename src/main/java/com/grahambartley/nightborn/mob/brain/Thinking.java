package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.mob.Unsticking;
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

public final class Thinking {

  static final double WATCHED_RANGE = 64.0;

  private Thinking() {}

  public static Optional<Think> dodge(final MobEntity mob, final int mobFloor) {
    return think(mob, true, mobFloor, Vec3d.ZERO);
  }

  public static Optional<Think> think(
      final MobEntity mob, final int mobFloor, final TargetMotion motion, final int thinkTicks) {
    return think(
        mob,
        false,
        mobFloor,
        mob.getTarget() == null
            ? Vec3d.ZERO
            : motion.sample(mob.getTarget(), mob.getWorld().getTime(), thinkTicks));
  }

  private static Optional<Think> think(
      final MobEntity mob, final boolean dodge, final int mobFloor, final Vec3d moved) {
    final Intent reported = Intents.read(mob);
    if (aids(dodge, mob.getTarget() != null, reported) && ownsHelp(mob)) {
      final Optional<Think> aid =
          weigh(
              mob,
              Intent.FIGHT,
              helping(options(mob, false, Intent.FIGHT), Intent.FIGHT),
              mobFloor,
              moved,
              true);
      if (aid.isPresent()) {
        return aid;
      }
    }
    if (!thinksAlone(dodge, mob.getTarget() != null, watched(mob))) {
      return Optional.empty();
    }
    return weigh(mob, reported, options(mob, dodge, reported), mobFloor, moved, false);
  }

  private static Optional<Think> weigh(
      final MobEntity mob,
      final Intent intent,
      final List<PowerChoice.Option> options,
      final int mobFloor,
      final Vec3d moved,
      final boolean forAllies) {
    if (options.stream().noneMatch(option -> option.verdict() == PowerUse.Verdict.READY)) {
      return Optional.empty();
    }
    final Set<Fact> needed = needed(options);
    final Situation.Group group =
        forAllies || Situations.readsGroup(needed) ? GroupViews.read(mob) : null;
    if (forAllies && !group.allyFighting()) {
      return Optional.empty();
    }
    final Situation situation = Situations.read(mob, intent, needed, moved, group);
    final IntentBudget budget = MobIntents.budget(mob.getRegistryManager(), intent);
    return Optional.of(
        new Think(
            intent,
            mob.getWorld().getTime(),
            PowerChoice.rank(options, situation, budget, mobFloor),
            budget,
            mob.getRandom().nextDouble()));
  }

  public static int idleTicks(
      final MobEntity mob, final Optional<Think> think, final int thinkTicks) {
    final Intent intent = think.map(Think::intent).orElseGet(() -> Intents.read(mob));
    return paced(
        ownsHelp(mob),
        intent,
        think
            .map(Think::budget)
            .orElseGet(() -> MobIntents.budget(mob.getRegistryManager(), intent)),
        thinkTicks);
  }

  static int paced(
      final boolean helps, final Intent intent, final IntentBudget budget, final int thinkTicks) {
    return helps || intent.fights() ? thinkTicks : budget.thinkTicks(thinkTicks);
  }

  static boolean thinksAlone(final boolean dodge, final boolean hasTarget, final boolean watched) {
    return dodge || hasTarget || watched;
  }

  private static boolean watched(final MobEntity mob) {
    return mob.getWorld().isPlayerInRange(mob.getX(), mob.getY(), mob.getZ(), WATCHED_RANGE);
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

  static boolean aids(final boolean dodge, final boolean hasTarget, final Intent reported) {
    return !dodge && !hasTarget && reported != Intent.RETREAT;
  }

  static List<PowerChoice.Option> helping(
      final List<PowerChoice.Option> options, final Intent intent) {
    return options.stream()
        .filter(
            option ->
                option.use().effectiveUses().stream()
                    .anyMatch(
                        use ->
                            use.intent().equals(intent.key())
                                && use.helpsAnAlly()
                                && option.readyAims().contains(use.aimed())))
        .toList();
  }

  static boolean helps(final MobPowerUse use) {
    return use.effectiveUses().stream().anyMatch(MobUse::helpsAnAlly);
  }

  private static boolean ownsHelp(final MobEntity mob) {
    for (final Identifier id : SkillAttachment.owned(mob).ownedInOrder()) {
      if (Powers.power(mob, id).flatMap(SkillPower::mob).filter(Thinking::helps).isPresent()) {
        return true;
      }
    }
    return false;
  }

  static boolean offered(
      final MobPowerUse use, final boolean dodge, final Intent intent, final boolean stuck) {
    return use.dodge() == dodge || !dodge && (!intent.fights() || stuck);
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
    final boolean stuck = Unsticking.stuck(mob);
    for (final Identifier id : SkillAttachment.owned(mob).ownedInOrder()) {
      final Optional<SkillPower> power = Powers.power(mob, id);
      final MobPowerUse use =
          power
              .flatMap(SkillPower::mob)
              .filter(each -> offered(each, dodge, intent, stuck))
              .orElse(null);
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
