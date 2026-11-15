package com.grahambartley.nightborn.client.anim;

import java.util.Comparator;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public record ClipChannel(List<Key> keys) {

  public static final ClipChannel EMPTY = new ClipChannel(List.of());

  public record Key(double time, Vec3d value, boolean smooth) {}

  public ClipChannel {
    keys = keys.stream().sorted(Comparator.comparingDouble(Key::time)).toList();
  }

  public boolean isEmpty() {
    return keys.isEmpty();
  }

  public Vec3d sample(final double time) {
    if (keys.isEmpty()) {
      return Vec3d.ZERO;
    }
    if (time <= keys.get(0).time()) {
      return keys.get(0).value();
    }
    final int last = keys.size() - 1;
    if (time >= keys.get(last).time()) {
      return keys.get(last).value();
    }
    int next = 1;
    while (keys.get(next).time() < time) {
      next++;
    }
    final Key before = keys.get(next - 1);
    final Key after = keys.get(next);
    final double t = (time - before.time()) / (after.time() - before.time());
    if (!before.smooth() && !after.smooth()) {
      return before.value().lerp(after.value(), t);
    }
    final Vec3d p0 = keys.get(Math.max(0, next - 2)).value();
    final Vec3d p3 = keys.get(Math.min(last, next + 1)).value();
    return new Vec3d(
        catmullRom(t, p0.x, before.value().x, after.value().x, p3.x),
        catmullRom(t, p0.y, before.value().y, after.value().y, p3.y),
        catmullRom(t, p0.z, before.value().z, after.value().z, p3.z));
  }

  public List<Double> peakTimesOfX(final double atLeast) {
    final List<Double> peaks = new java.util.ArrayList<>();
    for (int i = 1; i < keys.size() - 1; i++) {
      final double here = Math.abs(keys.get(i).value().x);
      if (here >= atLeast
          && here > Math.abs(keys.get(i - 1).value().x)
          && here > Math.abs(keys.get(i + 1).value().x)) {
        peaks.add(keys.get(i).time());
      }
    }
    return List.copyOf(peaks);
  }

  static double catmullRom(
      final double t, final double p0, final double p1, final double p2, final double p3) {
    final double t2 = t * t;
    final double t3 = t2 * t;
    return 0.5
        * (2.0 * p1
            + (-p0 + p2) * t
            + (2.0 * p0 - 5.0 * p1 + 4.0 * p2 - p3) * t2
            + (-p0 + 3.0 * p1 - 3.0 * p2 + p3) * t3);
  }
}
