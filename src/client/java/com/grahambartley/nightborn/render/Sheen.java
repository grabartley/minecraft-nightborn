package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coating.Coating;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.Identifier;

public enum Sheen {
  HOLY_WATER("holy_water_coating", Optional.of(Coating.HOLY_WATER), 0.0F, 24000L, 0.1F, 1.0F),
  GARLIC("garlic_coating", Optional.of(Coating.GARLIC), 52000L, 0.0F, -0.35F, 1.0F),
  BLOOD_FIRE("blood_fire", Optional.empty(), 0.0F, 9000L, 0.0F, 1.0F),
  MAIL_LINING("mail_lining", Optional.empty(), 120000L, 0.0F, 0.0F, 20.0F);

  static final double TIME_SCALE = 8.0;

  private final String name;
  private final Optional<Coating> coating;
  private final float acrossPeriod;
  private final float downPeriod;
  private final float tilt;
  private final float wornGrain;

  Sheen(
      final String name,
      final Optional<Coating> coating,
      final float acrossPeriod,
      final float downPeriod,
      final float tilt,
      final float wornGrain) {
    this.name = name;
    this.coating = coating;
    this.acrossPeriod = acrossPeriod;
    this.downPeriod = downPeriod;
    this.tilt = tilt;
    this.wornGrain = wornGrain;
  }

  public static List<Sheen> of(
      final Optional<Coating> coating, final boolean burning, final boolean lined) {
    final List<Sheen> sheens = new ArrayList<>();
    for (final Sheen sheen : values()) {
      if (sheen.shows(coating, burning, lined)) {
        sheens.add(sheen);
      }
    }
    return sheens;
  }

  private boolean shows(
      final Optional<Coating> coating, final boolean burning, final boolean lined) {
    return switch (this) {
      case BLOOD_FIRE -> burning;
      case MAIL_LINING -> lined;
      default -> this.coating.equals(coating);
    };
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

  public float wornGrain() {
    return wornGrain;
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
