package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coating.Coating;
import java.util.Optional;
import net.minecraft.util.Identifier;

public enum CoatingSheen {
  HOLY_WATER("holy_water_coating", Coating.HOLY_WATER, 0.0F, 24000L, 0.1F),
  GARLIC("garlic_coating", Coating.GARLIC, 52000L, 0.0F, -0.35F),
  BLOOD_FIRE("blood_fire", null, 0.0F, 9000L, 0.0F);

  static final double TIME_SCALE = 8.0;

  private final String name;
  private final Coating coating;
  private final float acrossPeriod;
  private final float downPeriod;
  private final float tilt;

  CoatingSheen(
      final String name,
      final Coating coating,
      final float acrossPeriod,
      final float downPeriod,
      final float tilt) {
    this.name = name;
    this.coating = coating;
    this.acrossPeriod = acrossPeriod;
    this.downPeriod = downPeriod;
    this.tilt = tilt;
  }

  public static Optional<CoatingSheen> of(final Optional<Coating> coating, final boolean burning) {
    if (burning) {
      return Optional.of(BLOOD_FIRE);
    }
    return coating.map(
        coat -> {
          for (final CoatingSheen sheen : values()) {
            if (sheen.coating == coat) {
              return sheen;
            }
          }
          throw new IllegalArgumentException("no sheen for " + coat);
        });
  }

  public String sheenName() {
    return name;
  }

  public Identifier texture() {
    return Identifier.of(Nightborn.MOD_ID, "textures/misc/" + name + ".png");
  }

  public float tilt() {
    return tilt;
  }

  public float across(final long millis, final double speed) {
    return drift(millis, speed, acrossPeriod);
  }

  public float down(final long millis, final double speed) {
    return drift(millis, speed, downPeriod);
  }

  static float drift(final long millis, final double speed, final float period) {
    if (period <= 0.0F || speed <= 0.0) {
      return 0.0F;
    }
    final long scaled = (long) (millis * speed * TIME_SCALE);
    return (float) (scaled % (long) period) / period;
  }
}
