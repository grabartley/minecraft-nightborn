package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.enchant.EnchantedValues;
import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;

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

  public static CoatingTerms of(
      final Coating coating, final NightbornConfig config, final ItemStack weapon) {
    final CoatingTerms terms = of(coating, config);
    return terms.withHits(
        Math.round(EnchantedValues.apply(weapon, EnchantedValues.COATING_HITS, terms.hits())));
  }

  public CoatingTerms withHits(final int allowed) {
    return new CoatingTerms(damage, Math.max(0, allowed), ticks, weakenStrength, weakenTicks);
  }
}
