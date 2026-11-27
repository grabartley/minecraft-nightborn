package com.grahambartley.nightborn.vanish;

import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.skill.CastAim;
import com.grahambartley.nightborn.skill.PowerHeading;
import com.grahambartley.nightborn.skill.Powers;
import com.grahambartley.nightborn.smoke.SmokePoof;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;

public final class Vanish {

  private Vanish() {}

  public static Optional<Vec3d> destination(
      final LivingEntity caster,
      final VanishPower power,
      final PowerHeading held,
      final CastAim aim) {
    return destination(
        caster,
        power.distance() + WornAttributes.value(caster, WornAttributes.VANISH_REACH),
        held,
        aim);
  }

  public static Optional<Vec3d> destination(
      final LivingEntity caster,
      final double distance,
      final PowerHeading held,
      final CastAim aim) {
    final boolean mob = caster instanceof MobEntity;
    final Vec3d aimedSpot = mob ? aim.spot() : null;
    final LivingEntity foe = mob ? aim.subject(caster) : null;
    final Vec3d looking = VanishSpots.facing(caster.getYaw());
    final Vec3d facing =
        aimedSpot != null
            ? VanishSpots.toward(caster.getPos(), aimedSpot, looking)
            : foe == null ? looking : VanishSpots.toward(caster.getPos(), foe.getPos(), looking);
    for (final PowerHeading heading :
        VanishSpots.headings(
            foe != null,
            aimedSpot != null ? PowerHeading.FORWARD : held,
            caster.getRandom().nextBoolean())) {
      final Optional<Vec3d> spot =
          Landing.reach(caster, VanishSpots.wanted(caster.getEyePos(), facing, heading, distance));
      if (spot.isPresent()) {
        return spot;
      }
    }
    return Optional.empty();
  }

  public static void go(final LivingEntity caster, final Vec3d to, final VanishPower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    FeedingSessions.end(caster);
    SmokePoof.puff(caster);
    caster.setAttached(VanishAttachment.TYPE, caster.getWorld().getTime() + power.goneTicks());
    Landing.move(caster, to);
  }

  public static void tick(final LivingEntity entity) {
    if (VanishAttachment.due(
        entity.getWorld().getTime(), entity.getAttached(VanishAttachment.TYPE))) {
      entity.removeAttached(VanishAttachment.TYPE);
      SmokePoof.puff(entity);
      Powers.playEndSound(entity, power -> power.vanish().isPresent());
    }
  }
}
