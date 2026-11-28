package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.PowerUse;
import java.util.List;
import java.util.function.DoubleSupplier;
import net.minecraft.util.Identifier;

public final class ChoiceFixtures {

  private ChoiceFixtures() {}

  public static Identifier id(final String path) {
    return Identifier.of("nightborn", path);
  }

  public static List<PowerChoice.Option> strikeOrGuard(final int strikeCost) {
    return List.of(
        option(
            "strike",
            strikeCost,
            new MobUse(
                Intent.FIGHT.key(),
                Aim.TARGET.key(),
                List.of(new Consideration(Fact.TARGET_DISTANCE.key(), Curve.BETWEEN, 0, 8)),
                1.0F)),
        option(
            "guard",
            0,
            new MobUse(
                Intent.FIGHT.key(),
                Aim.SELF.key(),
                List.of(new Consideration(Fact.OWN_HEALTH.key(), Curve.BETWEEN, 0, 1)),
                0.8F)));
  }

  public static DoubleSupplier rolls(final double... rolls) {
    final int[] next = {0};
    return () -> rolls[next[0]++];
  }

  public static Identifier pick(
      final List<PowerChoice.Option> options,
      final Mind mind,
      final IntentBudget budget,
      final DoubleSupplier chance) {
    final Situation fight =
        new Situation(
            Intent.FIGHT, Situation.Self.of(1.0F, 20), new Situation.Target(5, true, 1.0F));
    final IntentBudget minded = mind.budget(budget);
    return PowerChoice.choose(
            PowerChoice.rank(options, fight, minded, 0, mind, chance), IntentBudget.FREE, 0.0)
        .orElseThrow()
        .option()
        .id();
  }

  private static PowerChoice.Option option(final String name, final int cost, final MobUse use) {
    return new PowerChoice.Option(
        id(name),
        PowerUse.Verdict.READY,
        cost,
        new MobPowerUse(
            0.0F, 0.0F, 10, Identifier.of("minecraft", "x"), "tell", false, List.of(use)));
  }
}
