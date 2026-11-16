package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.household.HouseholdBonds;
import com.grahambartley.nightborn.skill.Powers;
import java.util.List;
import net.minecraft.entity.LivingEntity;

public final class Rally {

  private Rally() {}

  public static List<VampireEntity> call(final LivingEntity caster, final RallyPower power) {
    final LivingEntity foe = Powers.foe(caster);
    if (!(caster instanceof VampireEntity lord) || foe == null) {
      return List.of();
    }
    return HouseholdBonds.rally(lord, foe, power.range());
  }
}
