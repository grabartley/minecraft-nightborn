package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.faction.Hunters;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mob.combat.CombatMoves;
import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public final class TargetSigns {

  static final double RUN_SPEED = 0.15;

  private TargetSigns() {}

  public static Situation.Target read(
      final LivingEntity mob,
      final LivingEntity target,
      final boolean inSight,
      final float health,
      final boolean signs,
      final Vec3d moved) {
    if (!signs) {
      return new Situation.Target(mob.distanceTo(target), inSight, health);
    }
    return new Situation.Target(
        mob.distanceTo(target),
        inSight,
        health,
        windingUp(target),
        running(mob.getPos(), target.getPos(), moved),
        target.hasStatusEffect(StatusEffects.SLOWNESS) || Holds.held(target).isPresent(),
        airborne(target.isOnGround(), target.isInFluid(), target.isClimbing(), target.hasVehicle()),
        Vampires.isVampire(target),
        Hunters.isHunter(target));
  }

  public static boolean running(final Vec3d mob, final Vec3d target, final Vec3d moved) {
    final Vec3d away = new Vec3d(target.x - mob.x, 0.0, target.z - mob.z);
    if (away.lengthSquared() < 1.0E-6) {
      return false;
    }
    return moved.x * away.normalize().x + moved.z * away.normalize().z >= RUN_SPEED;
  }

  public static boolean airborne(
      final boolean onGround, final boolean inFluid, final boolean climbing, final boolean riding) {
    return !onGround && !inFluid && !climbing && !riding;
  }

  private static boolean windingUp(final LivingEntity target) {
    return CombatMoves.draws(target)
        || target instanceof MobFighter fighter && fighter.mobFighting().telling();
  }
}
