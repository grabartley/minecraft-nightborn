package com.grahambartley.nightborn.structure;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

public final class StructureZones {

  private StructureZones() {}

  public static Optional<StructureZone> deepest(
      final Collection<StructureZone> zones, final double x, final double y, final double z) {
    return zones.stream()
        .filter(zone -> zone.area().contains(x, y, z))
        .max(Comparator.comparingDouble(zone -> zone.area().closeness(x, y, z)));
  }
}
