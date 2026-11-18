package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.sun.SunShafts;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class ZoneHits {

  private ZoneHits() {}

  public static boolean weakens(final boolean vampire, final ZoneHit hit) {
    return vampire && hit.weaken().isPresent();
  }

  public static void apply(final LivingEntity victim, final ZoneHit hit) {
    for (final StatusEffectInstance effect : hit.effects()) {
      victim.addStatusEffect(new StatusEffectInstance(effect));
    }
    final boolean vampire = Vampires.isVampire(victim);
    if (weakens(vampire, hit)) {
      hit.weaken()
          .ifPresent(
              weaken -> Weakened.add(victim, weaken.source(), weaken.strength(), weaken.ticks()));
    }
    if (hit.igniteSeconds() > 0.0F) {
      victim.setOnFireFor(hit.igniteSeconds());
    }
    if (hit.sunTicks() > 0) {
      SunShafts.light(victim, hit.sunTicks());
    }
  }
}
