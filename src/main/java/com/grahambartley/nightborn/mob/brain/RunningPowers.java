package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.skill.Boosts;
import com.grahambartley.nightborn.skill.SkillPower;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.Identifier;

public final class RunningPowers {

  private RunningPowers() {}

  public static boolean on(final LivingEntity caster, final Identifier id, final SkillPower power) {
    int present = 0;
    for (final StatusEffectInstance effect : power.effects()) {
      if (caster.hasStatusEffect(effect.getEffectType())) {
        present++;
      }
    }
    return running(
        power.boost().isPresent() && Boosts.active(caster, id), power.effects().size(), present);
  }

  public static boolean running(final boolean boosted, final int effects, final int present) {
    return boosted || effects > 0 && present == effects;
  }
}
