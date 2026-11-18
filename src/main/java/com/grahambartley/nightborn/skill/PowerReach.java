package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.servant.Servants;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class PowerReach {

  private PowerReach() {}

  public static boolean touches(
      final boolean alive,
      final boolean self,
      final boolean servesCaster,
      final boolean untouchable,
      final boolean casterPlayer,
      final boolean targetPlayer,
      final boolean pvp) {
    return alive
        && !self
        && !servesCaster
        && !untouchable
        && (!casterPlayer || !targetPlayer || pvp);
  }

  public static boolean touches(final LivingEntity caster, final LivingEntity target) {
    return touches(
        target.isAlive(),
        target == caster,
        Servants.serves(target, caster),
        target.isSpectator() || target instanceof PlayerEntity player && player.isCreative(),
        caster instanceof PlayerEntity,
        target instanceof PlayerEntity,
        pvp(caster));
  }

  public static boolean marks(final boolean boss) {
    return !boss;
  }

  public static boolean marks(final LivingEntity target) {
    return marks(target.getType().isIn(Holds.UNHOLDABLE));
  }

  public static boolean pvp(final LivingEntity caster) {
    return caster.getServer() == null || caster.getServer().isPvpEnabled();
  }
}
