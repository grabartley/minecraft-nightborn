package com.grahambartley.nightborn.seeker;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.Optional;
import net.minecraft.util.math.Vec3d;

public final class SceneKills {

  private final int most;
  private final Deque<Scene> kills = new ArrayDeque<>();

  public SceneKills(final int most) {
    this.most = most;
  }

  public void add(final Scene kill) {
    kills.addFirst(kill);
    while (kills.size() > most) {
      kills.removeLast();
    }
  }

  public Optional<Scene> nearest(
      final Vec3d at, final double radius, final long now, final int freshTicks) {
    return kills.stream()
        .filter(kill -> kill.fresh(now, freshTicks))
        .filter(kill -> kill.where().squaredDistanceTo(at) <= radius * radius)
        .min(Comparator.comparingDouble(kill -> kill.where().squaredDistanceTo(at)));
  }

  public int size() {
    return kills.size();
  }

  public void clear() {
    kills.clear();
  }
}
