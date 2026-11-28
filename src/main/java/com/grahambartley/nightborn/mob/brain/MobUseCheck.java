package com.grahambartley.nightborn.mob.brain;

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

  public static Report check(
      final Map<Identifier, Skill> skills,
      final Set<Identifier> budgets,
      final Map<Identifier, MobAction> actions) {
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
      problems.addAll(uses(id.toString(), power.mob().get().effectiveUses()));
    }
    for (final Map.Entry<Identifier, MobAction> entry : new TreeMap<>(actions).entrySet()) {
      if (entry.getValue().uses().isEmpty()) {
        problems.add("action " + entry.getKey() + " has no uses");
      }
      problems.addAll(uses("action " + entry.getKey(), entry.getValue().uses()));
    }
    Arrays.stream(Intent.values())
        .filter(intent -> !budgets.contains(MobIntents.id(intent)))
        .forEach(intent -> problems.add("intent " + intent.key() + " has no budget"));
    return new Report(unused, problems);
  }

  private static List<String> uses(final String id, final List<MobUse> uses) {
    final List<String> problems = new ArrayList<>();
    for (final MobUse use : uses) {
      if (Intent.byKey(use.intent()).isEmpty()) {
        problems.add(id + " has a use with unknown intent " + use.intent());
      }
      if (Aim.byKey(use.aim()).isEmpty()) {
        problems.add(id + " has a use with unknown aim " + use.aim());
      }
      if (Aim.SPOT.key().equals(use.aim())
          && use.considerations().stream()
              .noneMatch(consideration -> Fact.SPOT_DISTANCE.key().equals(consideration.fact()))) {
        problems.add(id + " has a use aimed at a spot that does not weigh spot_distance");
      }
      if (Aim.ALLY.key().equals(use.aim()) && !use.helpsAnAlly()) {
        problems.add(id + " has a use aimed at an ally that weighs no ally fact");
      }
      use.considerations().stream()
          .filter(
              consideration ->
                  consideration.radius() <= 0.0F
                      && Fact.byKey(consideration.fact()).filter(Fact.RADIUS::contains).isPresent())
          .forEach(
              consideration ->
                  problems.add(
                      id + " has a use that counts " + consideration.fact() + " with no radius"));
      use.considerations().stream()
          .filter(consideration -> Fact.byKey(consideration.fact()).isEmpty())
          .forEach(
              consideration ->
                  problems.add(id + " has a use with unknown fact " + consideration.fact()));
    }
    return problems;
  }
}
