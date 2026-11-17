package com.grahambartley.nightborn.boss;

import java.util.ArrayDeque;
import java.util.Deque;

public final class HitLog {

  private record Hit(long at, float amount) {}

  private final Deque<Hit> hits = new ArrayDeque<>();

  public void add(final long now, final float amount) {
    hits.addLast(new Hit(now, amount));
  }

  public int hits(final long now, final int window) {
    forget(now, window);
    return hits.size();
  }

  public float damage(final long now, final int window) {
    forget(now, window);
    float total = 0.0F;
    for (final Hit hit : hits) {
      total += hit.amount();
    }
    return total;
  }

  public void clear() {
    hits.clear();
  }

  private void forget(final long now, final int window) {
    while (!hits.isEmpty() && now - hits.peekFirst().at() >= window) {
      hits.removeFirst();
    }
  }
}
