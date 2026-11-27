package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.PowerUse;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
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
    final List<PowerChoice.Option> options = options(mob, dodge);
    if (options.stream().noneMatch(option -> option.verdict() == PowerUse.Verdict.READY)) {
      return Optional.empty();
    }
    final Intent intent = Intents.read(mob);
    final Situation situation = Situations.read(mob, intent, needed(options));
    final IntentBudget budget = MobIntents.budget(mob.getRegistryManager(), intent);
    return Optional.of(
        new Think(
            intent,
            mob.getWorld().getTime(),
            PowerChoice.rank(options, situation, budget, mobFloor),
            budget));
  }

  static Set<Fact> needed(final List<PowerChoice.Option> options) {
    final Set<Fact> needed = EnumSet.noneOf(Fact.class);
    for (final PowerChoice.Option option : options) {
      if (option.verdict() != PowerUse.Verdict.READY) {
        continue;
      }
      for (final MobUse use : option.use().uses()) {
        for (final Consideration consideration : use.considerations()) {
          Fact.byKey(consideration.fact()).ifPresent(needed::add);
        }
      }
    }
    return needed;
  }

  static List<PowerChoice.Option> options(final MobEntity mob, final boolean dodge) {
    List<PowerChoice.Option> options = List.of();
    for (final Identifier id : SkillAttachment.owned(mob).ownedInOrder()) {
      final var power = Powers.power(mob, id);
      if (power.flatMap(owned -> owned.mob()).filter(use -> use.dodge() == dodge).isEmpty()) {
        continue;
      }
      if (options.isEmpty()) {
        options = new ArrayList<>();
      }
      options.add(
          new PowerChoice.Option(
              id, Powers.check(mob, id, power), power.get().blood(), power.get().mob().get()));
    }
    return options;
  }
}
