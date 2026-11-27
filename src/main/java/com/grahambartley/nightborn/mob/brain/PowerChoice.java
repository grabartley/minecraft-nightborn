package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Identifier;

public final class PowerChoice {

  public record Option(
      Identifier power,
      PowerUse.Verdict verdict,
      int cost,
      int cooldownTicks,
      boolean running,
      MobPowerUse use,
      Set<Aim> readyAims) {

    public Option {
      readyAims = Set.copyOf(readyAims);
    }

    public Option(
        final Identifier power,
        final PowerUse.Verdict verdict,
        final int cost,
        final int cooldownTicks,
        final boolean running,
        final MobPowerUse use) {
      this(
          power,
          verdict,
          cost,
          cooldownTicks,
          running,
          use,
          verdict == PowerUse.Verdict.READY ? EnumSet.allOf(Aim.class) : EnumSet.noneOf(Aim.class));
    }

    public Option(
        final Identifier power,
        final PowerUse.Verdict verdict,
        final int cost,
        final MobPowerUse use) {
      this(power, verdict, cost, 0, false, use);
    }
  }

  public record Scored(Option option, MobUse use, double score, boolean affordable) {}

  private PowerChoice() {}

  public static List<Scored> rank(
      final List<Option> options,
      final Situation situation,
      final IntentBudget budget,
      final int mobFloor) {
    final List<Scored> ranked = new ArrayList<>(options.size());
    for (final Option option : options) {
      if (option.verdict() != PowerUse.Verdict.READY || option.running()) {
        continue;
      }
      MobUse best = null;
      double bestScore = 0.0;
      for (final MobUse use : option.use().effectiveUses()) {
        if (!option.readyAims().contains(use.aimed())) {
          continue;
        }
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
                bestScore
                    * budget.kept(option.cost(), option.cooldownTicks(), situation.self().health()),
                budget.affords(situation.self().blood(), option.cost(), mobFloor)));
      }
    }
    ranked.sort(Comparator.comparingDouble(Scored::score).reversed());
    return ranked;
  }

  public static Optional<Scored> choose(
      final List<Scored> ranked, final IntentBudget budget, final double roll) {
    final List<Scored> worthy =
        ranked.stream()
            .filter(scored -> scored.affordable() && budget.passes(scored.score()))
            .toList();
    if (worthy.isEmpty()) {
      return Optional.empty();
    }
    if (budget.spread() <= 0.0F) {
      return Optional.of(worthy.getFirst());
    }
    final double best = worthy.getFirst().score();
    final List<Scored> close =
        worthy.stream().filter(scored -> budget.close(scored.score(), best)).toList();
    return Optional.of(close.get(Math.min(close.size() - 1, (int) (roll * close.size()))));
  }
}
