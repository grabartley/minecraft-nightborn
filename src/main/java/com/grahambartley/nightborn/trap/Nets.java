package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public final class Nets {

  private Nets() {}

  public static void launch(
      final LivingEntity caster, final Identifier skill, final NetPower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    final NetEntity net =
        new NetEntity(
            caster.getWorld(),
            caster,
            power,
            TrapGifts.extraDamage(TrapGifts.forPower(HeldTrapGifts.of(caster), skill)));
    final Vec3d heading = heading(caster);
    net.setVelocity(heading.x, heading.y, heading.z, power.speed(), 0.0F);
    caster.getWorld().spawnEntity(net);
  }

  static void land(
      final Entity net,
      final LivingEntity owner,
      final LivingEntity target,
      final NetPower power,
      final float extraDamage) {
    if (!Vampires.isVampire(target) || !PowerReach.touches(owner, target)) {
      return;
    }
    if (PowerReach.marks(target)) {
      final boolean flying = BatForm.isBat(target) || Levitation.isLevitating(target);
      Grounding.start(target, power.groundTicks());
      Deeds.record(owner, Deed.of(DeedKinds.TRAPPED));
      if (flying) {
        Deeds.record(owner, Deed.of(DeedKinds.GROUNDED));
      }
      if (target instanceof ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("nightborn.trap.netted"), true);
      }
    }
    if (extraDamage > 0.0F) {
      target.damage(TrapDamage.of(net, power.damageType(), owner), extraDamage);
    }
    target
        .getWorld()
        .playSound(
            null,
            target.getBlockPos(),
            SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE,
            SoundCategory.PLAYERS,
            1.0F,
            0.7F);
  }

  public static Vec3d heading(final LivingEntity caster) {
    if (caster instanceof MobEntity mob && mob.getTarget() != null) {
      return mob.getTarget().getEyePos().subtract(caster.getEyePos());
    }
    return caster.getRotationVector();
  }
}
