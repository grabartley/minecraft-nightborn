package com.grahambartley.nightborn.client.particle;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class LiveDecals<D extends PlacedDecal<D>> {

  private final Deque<D> oldestFirst = new ArrayDeque<>();
  private final int limit;
  private final int ceiling;

  public LiveDecals(final int limit, final int ceiling) {
    this.limit = limit;
    this.ceiling = Math.max(limit, ceiling);
  }

  public boolean place(final D incoming) {
    oldestFirst.removeIf(PlacedDecal::gone);
    final Iterator<D> newestFirst = oldestFirst.descendingIterator();
    while (newestFirst.hasNext()) {
      final D decal = newestFirst.next();
      if (!decal.retiring() && decal.absorbs(incoming)) {
        decal.absorb(incoming);
        return false;
      }
    }
    oldestFirst.addLast(incoming);
    retireOverLimit();
    while (oldestFirst.size() > ceiling) {
      oldestFirst.removeFirst().dropNow();
    }
    return true;
  }

  public void clear() {
    oldestFirst.clear();
  }

  public int size() {
    return oldestFirst.size();
  }

  private void retireOverLimit() {
    long showing = oldestFirst.stream().filter(decal -> !decal.retiring()).count();
    for (final D decal : oldestFirst) {
      if (showing <= limit) {
        return;
      }
      if (!decal.retiring()) {
        decal.retire();
        showing--;
      }
    }
  }
}
