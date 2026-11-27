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

public final class Thinking {

  private Thinking() {}

  public static Optional<Think> think(
      final MobEntity mob, final boolean dodge, final int mobFloor) {
    final Intent intent = Intents.read(mob);
    final List<PowerChoice.Option> options = options(mob, dodge, intent);
    if (options.stream().noneMatch(option -> option.verdict() == PowerUse.Verdict.READY)) {
      return Optional.empty();
    }
    final Situation situation = Situations.read(mob, intent, needed(options));
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

  static List<PowerChoice.Option> options(
      final MobEntity mob, final boolean dodge, final Intent intent) {
    List<PowerChoice.Option> options = List.of();
    for (final Identifier id : SkillAttachment.owned(mob).ownedInOrder()) {
      final Optional<SkillPower> power = Powers.power(mob, id);
      if (power.flatMap(SkillPower::mob).filter(use -> offered(use, dodge, intent)).isEmpty()) {
        continue;
      }
      if (options.isEmpty()) {
        options = new ArrayList<>();
      }
      final SkillPower owned = power.get();
      final PowerUse.Verdict verdict = Powers.check(mob, id, power);
      options.add(
          new PowerChoice.Option(
              id,
              verdict,
              owned.blood(),
              owned.cooldownTicks(),
              verdict == PowerUse.Verdict.READY && RunningPowers.on(mob, id, owned),
              owned.mob().get()));
    }
    return options;
  }
}
