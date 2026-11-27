package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.mob.combat.FightBack;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class GuildFoes {

  private GuildFoes() {}

  public static boolean hunts(final LivingEntity target) {
    return HunterTargets.hunts(
        Vampires.isVampire(target),
        target instanceof VampireBatEntity bat && bat.raidsCamp(),
        target instanceof PlayerEntity player && player.isCreative());
  }

  public static <T extends MobEntity & GuildFighter> boolean fights(
      final T fighter, final LivingEntity target) {
    return fighter.foeOf(target)
        || FightBack.answers(
            target == fighter.getAttacker() || target == fighter.getTarget(),
            false,
            false,
            true,
            target instanceof GuildFighter);
  }
}
