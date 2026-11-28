package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class PowerChoice {

  public enum Source {
    POWER,
    ACTION
  }

  public record Option(
      Identifier id,
      Source source,
      PowerUse.Verdict verdict,
      int cost,
      int cooldownTicks,
      boolean running,
      List<MobUse> uses,
      Set<Aim> readyAims) {

    public Option {
      uses = List.copyOf(uses);
      readyAims = Set.copyOf(readyAims);
    }

    public Option(
        final Identifier power,
        final PowerUse.Verdict verdict,
        final int cost,
        final int cooldownTicks,
        final boolean running,
        final MobPowerUse use,
        final Set<Aim> readyAims) {
      this(
          power,
          Source.POWER,
          verdict,
          cost,
          cooldownTicks,
          running,
          use.effectiveUses(),
          readyAims);
    }

    public Option(
        final Identifier power,
        final PowerUse.Verdict verdict,
        final int cost,
        final int cooldownTicks,
        final boolean running,
        final MobPowerUse use) {
      this(power, verdict, cost, cooldownTicks, running, use, allIfReady(verdict));
    }

    public Option(
        final Identifier power,
        final PowerUse.Verdict verdict,
        final int cost,
        final MobPowerUse use) {
      this(power, verdict, cost, 0, false, use);
    }

    public static Option action(final ActionReadiness readiness, final MobAction action) {
      return new Option(
          readiness.id(),
          Source.ACTION,
          readiness.verdict(),
          0,
          readiness.cooldownTicks(),
          readiness.running(),
          action.uses(),
          allIfReady(readiness.verdict()));
    }

    public boolean action() {
      return source == Source.ACTION;
    }

    private static Set<Aim> allIfReady(final PowerUse.Verdict verdict) {
      return verdict == PowerUse.Verdict.READY
          ? EnumSet.allOf(Aim.class)
          : EnumSet.noneOf(Aim.class);
    }
  }

  public record Scored(
      Option option,
      MobUse use,
      double score,
      boolean affordable,
      @Nullable Situation.Member ally) {

    public Scored(
        final Option option, final MobUse use, final double score, final boolean affordable) {
      this(option, use, score, affordable, null);
    }
  }

  private PowerChoice() {}

  public static List<Scored> rank(
      final List<Option> options,
      final Situation situation,
      final IntentBudget budget,
      final int mobFloor,
      final Mind mind,
      final DoubleSupplier chance) {
    final List<Scored> ranked = new ArrayList<>(options.size());
    for (final Option option : options) {
      if (option.verdict() != PowerUse.Verdict.READY || option.running()) {
        continue;
      }
      MobUse best = null;
      MobUse.Scoring bestScoring = null;
      for (final MobUse use : option.uses()) {
        if (!option.readyAims().contains(use.aimed())) {
          continue;
        }
        final MobUse.Scoring scoring = use.scoring(situation);
        if (best == null || scoring.score() > bestScoring.score()) {
          best = use;
          bestScoring = scoring;
        }
      }
      if (best != null) {
        ranked.add(
            new Scored(
                option,
                best,
                mind.judged(
                    bestScoring.score()
                        * budget.kept(
                            option.cost(), option.cooldownTicks(), situation.self().health()),
                    situation.intent(),
                    best.aimed(),
                    mind.sharpness().blurs() ? chance.getAsDouble() : 0.5),
                option.action()
                    || budget.affords(situation.self().blood(), option.cost(), mobFloor),
                bestScoring.ally()));
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
