package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.batform.WingedForm;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.mob.Unsticking;
import com.grahambartley.nightborn.sun.DirectSun;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public final class Situations {

  static final Set<Fact> SIGNS =
      Collections.unmodifiableSet(
          EnumSet.of(
              Fact.TARGET_WINDING_UP,
              Fact.TARGET_RUNNING,
              Fact.TARGET_SLOWED,
              Fact.TARGET_AIRBORNE,
              Fact.TARGET_VAMPIRE,
              Fact.TARGET_HUNTER,
              Fact.TARGET_AWARE,
              Fact.TARGET_THREATENING));

  private Situations() {}

  public static Situation read(
      final MobEntity mob, final Intent intent, final Set<Fact> needed, final Vec3d moved) {
    return read(mob, intent, needed, moved, readsGroup(needed) ? GroupViews.read(mob) : null);
  }

  public static Situation read(
      final MobEntity mob,
      final Intent intent,
      final Set<Fact> needed,
      final Vec3d moved,
      @Nullable final Situation.Group group) {
    final LivingEntity target = mob.getTarget();
    return new Situation(
        intent,
        new Situation.Self(
            health(mob),
            BloodAttachment.get(mob),
            BatForm.isBat(mob) || WingedForm.on(mob),
            needed.contains(Fact.SUN_ON_ME) && DirectSun.reaches(mob),
            Downed.state(mob) != null,
            Unsticking.stuck(mob),
            needed.contains(Fact.OWN_HELD)
                && TargetSigns.held(
                    Holds.held(mob).isPresent() || mob.hasStatusEffect(StatusEffects.SLOWNESS),
                    MindTies.mesmerised(mob))),
        target == null
            ? null
            : TargetSigns.read(
                mob,
                target,
                needed.contains(Fact.TARGET_IN_SIGHT) && mob.canSee(target),
                health(target),
                readsSigns(needed),
                readsTactics(needed),
                moved),
        PlaceViews.read(mob, needed),
        group == null ? Situation.Group.UNKNOWN : group,
        null);
  }

  static boolean readsGroup(final Set<Fact> needed) {
    return !Collections.disjoint(needed, Fact.GROUP);
  }

  static boolean readsTactics(final Set<Fact> needed) {
    return !Collections.disjoint(needed, Fact.TACTICS);
  }

  static boolean readsSigns(final Set<Fact> needed) {
    return !Collections.disjoint(needed, SIGNS);
  }

  private static float health(final LivingEntity entity) {
    return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
  }
}
