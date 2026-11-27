package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class PowerChoice {

  public record Option(Identifier power, PowerUse.Verdict verdict, int cost, MobPowerUse use) {}

  public record Scored(Option option, MobUse use, double score, boolean affordable) {}

  private PowerChoice() {}

  public static List<Scored> rank(
      final List<Option> options,
      final Situation situation,
      final IntentBudget budget,
      final int mobFloor) {
    final List<Scored> ranked = new ArrayList<>(options.size());
    for (final Option option : options) {
      if (option.verdict() != PowerUse.Verdict.READY) {
        continue;
      }
      MobUse best = null;
      double bestScore = 0.0;
      for (final MobUse use : option.use().uses()) {
        final double score = use.score(situation);
        if (best == null || score > bestScore) {
          best = use;
          bestScore = score;
        }
      }
      if (best != null) {
        ranked.add(
            new Scored(
                option,
                best,
                bestScore,
                budget.affords(situation.self().blood(), option.cost(), mobFloor)));
      }
    }
    ranked.sort(Comparator.comparingDouble(Scored::score).reversed());
    return ranked;
  }

  public static Optional<Scored> choose(final List<Scored> ranked, final IntentBudget budget) {
    return ranked.stream()
        .filter(scored -> scored.affordable() && budget.passes(scored.score()))
        .findFirst();
  }
}
