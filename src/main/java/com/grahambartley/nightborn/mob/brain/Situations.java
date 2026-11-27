package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.sun.DirectSun;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public final class Situations {

  static final Set<Fact> SIGNS =
      Collections.unmodifiableSet(
          EnumSet.of(
              Fact.TARGET_WINDING_UP,
              Fact.TARGET_RUNNING,
              Fact.TARGET_SLOWED,
              Fact.TARGET_AIRBORNE,
              Fact.TARGET_VAMPIRE,
              Fact.TARGET_HUNTER));

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
            : TargetSigns.read(
                mob,
                target,
                needed.contains(Fact.TARGET_IN_SIGHT) && mob.canSee(target),
                health(target),
                readsSigns(needed)),
        needed.contains(Fact.SPOT_DISTANCE)
            ? new Situation.Place(RouteEnd.distance(mob.getPos(), RouteEnd.of(mob)))
            : Situation.Place.UNKNOWN);
  }

  static boolean readsSigns(final Set<Fact> needed) {
    return !Collections.disjoint(needed, SIGNS);
  }

  private static float health(final LivingEntity entity) {
    return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
  }
}
