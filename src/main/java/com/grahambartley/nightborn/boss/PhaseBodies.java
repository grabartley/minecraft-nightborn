package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vampire.Modifiers;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class PhaseBodies {

  private PhaseBodies() {}

  public static Set<Identifier> named(final BossFightPlan plan) {
    final Set<Identifier> named = new TreeSet<>();
    plan.phases().forEach(phase -> named.addAll(phase.body().attributes().keySet()));
    return named;
  }

  public static double value(
      final BossFightPlan plan,
      final Identifier attribute,
      final int index,
      final double progress) {
    final double now = plan.phase(index).body().bonus(attribute);
    final double before = index > 0 ? plan.phase(index - 1).body().bonus(attribute) : now;
    return PhaseBody.blend(before, now, progress);
  }

  public static double progress(final long changeUntil, final long now, final int changeTicks) {
    return now >= changeUntil || changeTicks <= 0
        ? 1.0
        : Math.max(0.0, 1.0 - (double) (changeUntil - now) / changeTicks);
  }

  public static Identifier modifier(final Identifier attribute) {
    return Identifier.of(Nightborn.MOD_ID, "boss/phase/" + attribute.getPath());
  }

  public static void apply(
      final LivingEntity boss, final BossFightPlan plan, final int index, final double progress) {
    for (final Identifier attribute : named(plan)) {
      Registries.ATTRIBUTE
          .getEntry(attribute)
          .ifPresent(
              entry ->
                  Modifiers.set(
                      boss,
                      entry,
                      modifier(attribute),
                      value(plan, attribute, index, progress),
                      EntityAttributeModifier.Operation.ADD_VALUE));
    }
  }
}
