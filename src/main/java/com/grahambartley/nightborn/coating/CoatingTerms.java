package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.config.NightbornConfig;
import net.minecraft.SharedConstants;

public record CoatingTerms(int damage, int hits, int ticks, int weakenStrength, int weakenTicks) {

  public static CoatingTerms of(final Coating coating, final NightbornConfig config) {
    final int belowSilver = Math.max(0, config.gear().silverDamage() - 1);
    return new CoatingTerms(
        Math.min(coating.damage.get(config), belowSilver),
        coating.hits.get(config),
        coating.seconds.get(config) * SharedConstants.TICKS_PER_SECOND,
        coating.strength.get(config),
        coating.weakenSeconds.get(config) * SharedConstants.TICKS_PER_SECOND);
  }
}
