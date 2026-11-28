package com.grahambartley.nightborn.mob.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.math.Vec3d;

public final class SharedScans<T> {

  private record Scan<T>(Vec3d centre, List<T> found) {}

  private final double reuse;
  private final List<Scan<T>> scans = new ArrayList<>();
  private long tick = Long.MIN_VALUE;

  public SharedScans(final double reuse) {
    this.reuse = reuse;
  }

  public List<T> near(final long now, final Vec3d at, final Function<Vec3d, List<T>> scanner) {
    if (now != tick) {
      scans.clear();
      tick = now;
    }
    for (final Scan<T> scan : scans) {
      if (scan.centre().squaredDistanceTo(at) <= reuse * reuse) {
        return scan.found();
      }
    }
    final List<T> found = List.copyOf(scanner.apply(at));
    scans.add(new Scan<>(at, found));
    return found;
  }
}
