package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.trap.Grounding;
import com.grahambartley.nightborn.trap.Nets;
import com.grahambartley.nightborn.trap.TrapDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public final class StakeThrows {

  private StakeThrows() {}

  public static boolean armed(final LivingEntity caster) {
    return !find(caster).isEmpty();
  }

  public static void launch(final LivingEntity caster, final StakeThrowPower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    final ItemStack held = find(caster);
    if (held.isEmpty()) {
      return;
    }
    final boolean keeps =
        caster instanceof MobEntity || caster instanceof PlayerEntity player && player.isCreative();
    final ItemStack thrown = keeps ? held.copyWithCount(1) : held.split(1);
    final ThrownStakeEntity stake = new ThrownStakeEntity(caster.getWorld(), caster, power, !keeps);
    stake.setItem(thrown);
    final Vec3d heading = Nets.heading(caster);
    stake.setVelocity(heading.x, heading.y, heading.z, power.speed(), 0.0F);
    caster.getWorld().spawnEntity(stake);
    caster.swingHand(Hand.MAIN_HAND, true);
  }

  static void land(
      final Entity stake,
      final LivingEntity owner,
      final LivingEntity target,
      final StakeThrowPower power) {
    if (!PowerReach.touches(owner, target)) {
      return;
    }
    target.damage(TrapDamage.of(stake, power.damageType(), owner), power.damage());
    if (Vampires.isVampire(target) && PowerReach.marks(target)) {
      Grounding.unwing(target);
      Holds.hold(owner, target, HoldKind.STAGGER, power.staggerTicks());
    }
    target
        .getWorld()
        .playSound(
            null,
            target.getBlockPos(),
            SoundEvents.ITEM_TRIDENT_HIT,
            SoundCategory.PLAYERS,
            1.0F,
            1.2F);
  }

  static ItemStack find(final LivingEntity caster) {
    if (caster.getMainHandStack().isIn(VampireBane.STAKES)) {
      return caster.getMainHandStack();
    }
    if (caster.getOffHandStack().isIn(VampireBane.STAKES)) {
      return caster.getOffHandStack();
    }
    if (caster instanceof PlayerEntity player) {
      for (final ItemStack stack : player.getInventory().main) {
        if (stack.isIn(VampireBane.STAKES)) {
          return stack;
        }
      }
    }
    return ItemStack.EMPTY;
  }
}
