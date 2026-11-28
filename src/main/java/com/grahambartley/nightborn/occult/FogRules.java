package com.grahambartley.nightborn.occult;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;

public final class FogRules {

  static final int PULSE_TICKS = 10;

  private FogRules() {}

  public static boolean pulses(final long now, final long startedAt) {
    return (now - startedAt) % PULSE_TICKS == 0;
  }

  public static boolean touches(
      final boolean alive,
      final boolean caster,
      final boolean servesCaster,
      final boolean spectating,
      final boolean seesThrough,
      final boolean allied) {
    return alive && !caster && !servesCaster && !spectating && !seesThrough && !allied;
  }

  public static boolean blinds(final RegistryEntry<StatusEffect> effect) {
    return effect.matches(StatusEffects.BLINDNESS) || effect.matches(StatusEffects.DARKNESS);
  }

  public static boolean reaches(final boolean blinding, final boolean clearSight) {
    return !(blinding && clearSight);
  }
}
