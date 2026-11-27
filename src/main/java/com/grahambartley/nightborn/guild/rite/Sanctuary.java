package com.grahambartley.nightborn.guild.rite;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.DamageTypeTags;

public final class Sanctuary {

  private Sanctuary() {}

  static void register() {
    SanctuaryEffect.register();
    ServerLivingEntityEvents.ALLOW_DAMAGE.register(Sanctuary::allows);
  }

  public static void shelter(final LivingEntity arrived, final int seconds) {
    if (seconds > 0) {
      arrived.addStatusEffect(
          new StatusEffectInstance(SanctuaryEffect.ENTRY, ticks(seconds), 0, true, false, true));
    }
  }

  public static int ticks(final int seconds) {
    return Math.max(0, seconds) * RiteAccess.TICKS_PER_SECOND;
  }

  public static boolean sheltered(final LivingEntity entity) {
    return entity.hasStatusEffect(SanctuaryEffect.ENTRY);
  }

  public static boolean shields(final boolean sheltered, final boolean bypasses) {
    return sheltered && !bypasses;
  }

  public static boolean forfeits(final boolean attackerSheltered, final boolean strikesAnother) {
    return attackerSheltered && strikesAnother;
  }

  private static boolean allows(
      final LivingEntity struck, final DamageSource source, final float amount) {
    if (source.getAttacker() instanceof LivingEntity attacker
        && forfeits(sheltered(attacker), attacker != struck)) {
      attacker.removeStatusEffect(SanctuaryEffect.ENTRY);
    }
    return !shields(sheltered(struck), source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY));
  }
}
