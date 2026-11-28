package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.faction.Hunters;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.Mending;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.UseAction;
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
      final boolean tactics,
      final Vec3d moved) {
    if (!signs && !tactics) {
      return new Situation.Target(mob.distanceTo(target), inSight, health);
    }
    final boolean slowed = slowed(target);
    return new Situation.Target(
        mob.distanceTo(target),
        inSight,
        health,
        windingUp(target),
        running(mob.getPos(), target.getPos(), moved),
        slowed,
        airborne(target.isOnGround(), target.isInFluid(), target.isClimbing(), target.hasVehicle()),
        Vampires.isVampire(target),
        Hunters.isHunter(target),
        aware(
            target instanceof MobEntity foe && foe.getTarget() == mob,
            target.getAttacker() == mob,
            mob.getAttacker() == target),
        Threat.of(target).at(mob.distanceTo(target), ServerConfigService.current().combat()),
        tactics
            && healing(
                target.getAttached(Mending.TYPE) != null,
                target.hasStatusEffect(StatusEffects.REGENERATION),
                target.isUsingItem() && mends(target.getActiveItem())),
        tactics && (ranged(target.getMainHandStack()) || ranged(target.getOffHandStack())),
        tactics && held(slowed, MindTies.mesmerised(target)));
  }

  public static boolean slowed(final LivingEntity body) {
    return body.hasStatusEffect(StatusEffects.SLOWNESS) || Holds.held(body).isPresent();
  }

  public static boolean held(final LivingEntity body) {
    return held(slowed(body), MindTies.mesmerised(body));
  }

  public static boolean healing(
      final boolean mending, final boolean regenerating, final boolean takingCure) {
    return mending || regenerating || takingCure;
  }

  public static boolean held(final boolean slowed, final boolean mesmerised) {
    return slowed || mesmerised;
  }

  public static boolean mends(final ItemStack used) {
    final UseAction action = used.getUseAction();
    return action == UseAction.DRINK
        || action == UseAction.EAT && used.contains(DataComponentTypes.FOOD);
  }

  public static boolean ranged(final ItemStack held) {
    return held.getItem() instanceof RangedWeaponItem || held.getItem() instanceof TridentItem;
  }

  public static boolean aware(
      final boolean huntsTheMob, final boolean struckByTheMob, final boolean struckTheMob) {
    return huntsTheMob || struckByTheMob || struckTheMob;
  }

  public static boolean running(final Vec3d mob, final Vec3d target, final Vec3d moved) {
    final Vec3d away = new Vec3d(target.x - mob.x, 0.0, target.z - mob.z);
    if (away.lengthSquared() < 1.0E-6) {
      return false;
    }
    return moved.dotProduct(away.normalize()) >= RUN_SPEED;
  }

  public static boolean airborne(
      final boolean onGround, final boolean inFluid, final boolean climbing, final boolean riding) {
    return !onGround && !inFluid && !climbing && !riding;
  }

  private static boolean windingUp(final LivingEntity target) {
    return Threat.draws(target)
        || target instanceof MobFighter fighter && fighter.mobFighting().telling();
  }
}
