package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.sun.DirectSun;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public final class Situations {

  private Situations() {}

  public static Situation read(final MobEntity mob, final Intent intent) {
    final LivingEntity target = mob.getTarget();
    return new Situation(
        intent,
        new Situation.Self(
            health(mob),
            BloodAttachment.get(mob),
            BatForm.isBat(mob) || WingedForm.on(mob),
            DirectSun.reaches(mob),
            Downed.state(mob) != null,
            mob.getActiveStatusEffects().keySet()),
        target == null
            ? null
            : new Situation.Target(mob.distanceTo(target), mob.canSee(target), health(target)));
  }

  private static float health(final LivingEntity entity) {
    return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
  }
}
