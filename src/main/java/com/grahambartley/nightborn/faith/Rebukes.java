package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.skill.AreaRules;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.ward.WardTags;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public final class Rebukes {

  private Rebukes() {}

  public static boolean armed(final LivingEntity caster) {
    return RebukeArc.armed(
        caster.isHolding(stack -> stack.isIn(WardTags.CRUCIFIXES)),
        Vampires.isVampire(caster),
        GiftAttributes.value(caster, GiftAttributes.WORN_WARD));
  }

  public static boolean silenced(final LivingEntity entity) {
    return FaithMarks.REBUKED.on(entity);
  }

  public static void rebuke(final LivingEntity caster, final RebukePower power) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final Vec3d look = caster.getRotationVector();
    final double range = power.range();
    for (final LivingEntity vampire :
        world.getEntitiesByClass(
            LivingEntity.class,
            caster.getBoundingBox().expand(range),
            candidate ->
                Vampires.isVampire(candidate)
                    && AreaRules.inside(candidate.squaredDistanceTo(caster), power.range())
                    && RebukeArc.inFront(
                        look.x,
                        look.z,
                        candidate.getX() - caster.getX(),
                        candidate.getZ() - caster.getZ(),
                        power.arcDegrees())
                    && PowerReach.touches(caster, candidate)
                    && PowerReach.marks(candidate))) {
      throwBack(caster, vampire, power);
    }
    flare(world, caster, look, power);
  }

  private static void throwBack(
      final LivingEntity caster, final LivingEntity vampire, final RebukePower power) {
    vampire.takeKnockback(
        power.push(), caster.getX() - vampire.getX(), caster.getZ() - vampire.getZ());
    vampire.velocityModified = true;
    FaithMarks.REBUKED.set(vampire, power.silenceTicks());
    if (vampire instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.faith.rebuked"), true);
    }
  }

  private static void flare(
      final ServerWorld world,
      final LivingEntity caster,
      final Vec3d look,
      final RebukePower power) {
    final double yaw = Math.atan2(look.z, look.x);
    final double half = Math.toRadians(power.arcDegrees() / 2.0);
    for (double angle = yaw - half; angle <= yaw + half; angle += 0.15) {
      for (double reach = 1.5; reach <= power.range(); reach += 1.5) {
        world.spawnParticles(
            ParticleTypes.END_ROD,
            caster.getX() + Math.cos(angle) * reach,
            caster.getBodyY(0.6),
            caster.getZ() + Math.sin(angle) * reach,
            1,
            0.05,
            0.05,
            0.05,
            0.01);
      }
    }
  }
}
