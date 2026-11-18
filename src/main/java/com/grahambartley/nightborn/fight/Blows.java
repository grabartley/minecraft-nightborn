package com.grahambartley.nightborn.fight;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record Blows(Map<UUID, Long> lastAt) {

  public static final Blows NONE = new Blows(Map.of());

  public Blows {
    lastAt = Map.copyOf(lastAt);
  }

  public Blows with(final UUID other, final long now, final int keepTicks) {
    final Map<UUID, Long> next = new HashMap<>();
    lastAt.forEach(
        (id, at) -> {
          if (now - at <= keepTicks) {
            next.put(id, at);
          }
        });
    next.put(other, now);
    return new Blows(next);
  }

  public boolean recent(final UUID other, final long now, final int windowTicks) {
    final Long at = lastAt.get(other);
    return at != null && now - at <= windowTicks;
  }
}
