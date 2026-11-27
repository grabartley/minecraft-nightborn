package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.sun.DirectSun;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public final class Situations {

  private Situations() {}

  public static Situation read(final MobEntity mob, final Intent intent, final Set<Fact> needed) {
    final LivingEntity target = mob.getTarget();
    return new Situation(
        intent,
        new Situation.Self(
            health(mob),
            BloodAttachment.get(mob),
            BatForm.isBat(mob) || WingedForm.on(mob),
            needed.contains(Fact.SUN_ON_ME) && DirectSun.reaches(mob),
            Downed.state(mob) != null),
        target == null
            ? null
            : new Situation.Target(
                mob.distanceTo(target),
                needed.contains(Fact.TARGET_IN_SIGHT) && mob.canSee(target),
                health(target)));
  }

  private static float health(final LivingEntity entity) {
    return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
  }
}
