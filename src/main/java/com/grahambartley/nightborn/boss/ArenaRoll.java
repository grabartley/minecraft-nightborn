package com.grahambartley.nightborn.boss;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ArenaRoll {

  private Set<UUID> inside = Set.of();

  public record Change(Set<UUID> entered, Set<UUID> left) {

    public boolean none() {
      return entered.isEmpty() && left.isEmpty();
    }
  }

  public Change step(final Set<UUID> now) {
    final Set<UUID> entered = new HashSet<>(now);
    entered.removeAll(inside);
    final Set<UUID> left = new HashSet<>(inside);
    left.removeAll(now);
    inside = Set.copyOf(now);
    return new Change(Set.copyOf(entered), Set.copyOf(left));
  }

  public Set<UUID> inside() {
    return inside;
  }

  public Set<UUID> clear() {
    final Set<UUID> were = inside;
    inside = Set.of();
    return were;
  }
}
