package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.gear.VampireBane;
import com.grahambartley.nightborn.landing.Landing;
import com.grahambartley.nightborn.skill.PowerReach;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public final class Lunges {

  private Lunges() {}

  public static boolean armed(final LivingEntity caster) {
    return caster.getMainHandStack().isIn(VampireBane.STAKES);
  }

  public static Optional<Vec3d> destination(
      final LivingEntity caster, final LivingEntity target, final LungePower power) {
    return Landing.reach(
        caster,
        LungePath.wanted(
            caster.getPos(),
            target.getPos(),
            caster.getStandingEyeHeight(),
            (caster.getWidth() + target.getWidth()) / 2.0 + power.gap()));
  }

  public static float blow(final float stakeBlow, final float stakeShare) {
    return stakeBlow * (1.0F + stakeShare);
  }

  public static void strike(
      final LivingEntity caster,
      final LivingEntity target,
      final Vec3d to,
      final LungePower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    Landing.move(caster, to);
    caster.swingHand(Hand.MAIN_HAND, true);
    if (SlayerOpenings.open(target) && SlayerDowns.down(caster, target, power.downs())) {
      caster
          .getWorld()
          .playSound(
              null,
              target.getBlockPos(),
              SoundEvents.ENTITY_PLAYER_ATTACK_CRIT,
              SoundCategory.PLAYERS,
              1.0F,
              0.6F);
      if (caster instanceof ServerPlayerEntity player) {
        player.sendMessage(Text.translatable("nightborn.slayer.heartstrike"), true);
      }
      return;
    }
    if (!PowerReach.touches(caster, target)) {
      return;
    }
    final float stakeBlow =
        VampireBane.strike(
            caster.getMainHandStack(),
            target,
            (float) caster.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE));
    target.damage(source(caster), blow(stakeBlow, power.stakeShare()));
  }

  private static DamageSource source(final LivingEntity caster) {
    return caster instanceof PlayerEntity player
        ? caster.getDamageSources().playerAttack(player)
        : caster.getDamageSources().mobAttack(caster);
  }
}
