package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.MobPowerUse;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillPower;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.util.Identifier;

public final class MobUseCheck {

  public record Report(List<String> unused, List<String> problems) {}

  private MobUseCheck() {}

  public static Report check(final Map<Identifier, Skill> skills, final Set<Identifier> budgets) {
    final List<String> unused = new ArrayList<>();
    final List<String> problems = new ArrayList<>();
    for (final Map.Entry<Identifier, Skill> entry : new TreeMap<>(skills).entrySet()) {
      final Identifier id = entry.getKey();
      final SkillPower power = entry.getValue().power().orElse(null);
      if (power == null) {
        continue;
      }
      if (power.mob().isEmpty()) {
        if (!power.mobsNeverUse()) {
          unused.add(id + " has a power that mobs will never use");
        }
        continue;
      }
      if (power.mobsNeverUse()) {
        problems.add(id + " has a mob block but says mobs never use it");
      }
      problems.addAll(uses(id, power.mob().get()));
    }
    Arrays.stream(Intent.values())
        .filter(intent -> !budgets.contains(MobIntents.id(intent)))
        .forEach(intent -> problems.add("intent " + intent.key() + " has no budget"));
    return new Report(unused, problems);
  }

  private static List<String> uses(final Identifier id, final MobPowerUse mob) {
    final List<String> problems = new ArrayList<>();
    for (final MobUse use : mob.effectiveUses()) {
      if (Intent.byKey(use.intent()).isEmpty()) {
        problems.add(id + " has a use with unknown intent " + use.intent());
      }
      if (Aim.byKey(use.aim()).isEmpty()) {
        problems.add(id + " has a use with unknown aim " + use.aim());
      }
      use.considerations().stream()
          .filter(consideration -> Fact.byKey(consideration.fact()).isEmpty())
          .forEach(
              consideration ->
                  problems.add(id + " has a use with unknown fact " + consideration.fact()));
    }
    return problems;
  }
}
