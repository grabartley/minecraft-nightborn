package com.grahambartley.nightborn.structure;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class FogZones {

  private FogZones() {}

  public static Optional<FogZone> nearest(
      final Collection<FogZone> zones, final double x, final double y, final double z) {
    return zones.stream()
        .filter(zone -> zone.contains(x, y, z))
        .max(Comparator.comparingDouble(zone -> zone.closeness(x, y, z)));
  }
}
