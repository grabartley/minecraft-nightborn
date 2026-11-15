package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.config.CoatingSettings;
import com.grahambartley.nightborn.config.NightbornConfig;

public record CoatingTerms(int damage, int hits, int ticks, int weakenStrength, int weakenTicks) {

  static final int TICKS_PER_SECOND = 20;

  public static CoatingTerms of(final Coating coating, final NightbornConfig config) {
    final CoatingSettings settings = config.coatings();
    final int belowSilver = Math.max(0, config.gear().silverDamage() - 1);
    return switch (coating) {
      case HOLY_WATER ->
          new CoatingTerms(
              Math.min(settings.holyWaterDamage(), belowSilver),
              settings.holyWaterHits(),
              settings.holyWaterSeconds() * TICKS_PER_SECOND,
              settings.holyWaterStrength(),
              settings.holyWaterWeakenSeconds() * TICKS_PER_SECOND);
      case GARLIC ->
          new CoatingTerms(
              Math.min(settings.garlicDamage(), belowSilver),
              settings.garlicHits(),
              settings.garlicSeconds() * TICKS_PER_SECOND,
              settings.garlicStrength(),
              settings.garlicWeakenSeconds() * TICKS_PER_SECOND);
    };
  }
}
