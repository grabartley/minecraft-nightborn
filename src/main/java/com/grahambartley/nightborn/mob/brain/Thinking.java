package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.skill.SkillAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

public final class Thinking {

  private Thinking() {}

  public static Optional<Think> think(
      final MobEntity mob, final boolean dodge, final int mobFloor) {
    final List<PowerChoice.Option> options = options(mob, dodge);
    if (options.isEmpty()) {
      return Optional.empty();
    }
    final Intent intent = Intents.read(mob);
    final Situation situation = Situations.read(mob, intent);
    final IntentBudget budget = MobIntents.budget(mob.getRegistryManager(), intent);
    return Optional.of(
        new Think(
            intent,
            mob.getWorld().getTime(),
            PowerChoice.rank(options, situation, budget, mobFloor),
            budget));
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
