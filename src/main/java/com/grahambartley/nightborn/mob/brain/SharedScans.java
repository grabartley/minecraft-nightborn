package com.grahambartley.nightborn.mob.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.math.Vec3d;

public final class SharedScans<T> {

  private record Scan<T>(long tick, Vec3d centre, List<T> found) {}

  private final double reuse;
  private final int window;
  private final List<Scan<T>> scans = new ArrayList<>();

  public SharedScans(final double reuse, final int window) {
    this.reuse = reuse;
    this.window = window;
  }

  public List<T> near(final long now, final Vec3d at, final Function<Vec3d, List<T>> scanner) {
    scans.removeIf(scan -> now < scan.tick() || now - scan.tick() >= window);
    for (final Scan<T> scan : scans) {
      if (scan.centre().squaredDistanceTo(at) <= reuse * reuse) {
        return scan.found();
      }
    }
    final List<T> found = List.copyOf(scanner.apply(at));
    scans.add(new Scan<>(now, at, found));
    return found;
  }
}
