package com.grahambartley.nightborn.house;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Succession {

  private Succession() {}

  public static Optional<UUID> heir(final House house) {
    return house.players().stream()
        .filter(player -> HouseRules.mayFound(house.rank(player)))
        .reduce((best, next) -> house.rank(next) > house.rank(best) ? next : best);
  }

  public static List<UUID> overflow(final House house, final int capacity) {
    final int over = house.size() - Math.max(0, capacity);
    if (over <= 0) {
      return List.of();
    }
    final List<UUID> youngestFirst = new ArrayList<>(house.npcs().reversed());
    youngestFirst.sort(Comparator.comparingInt(house::rank));
    return List.copyOf(youngestFirst.subList(0, Math.min(over, youngestFirst.size())));
  }

  public static House trimmed(final House house, final int capacity) {
    House kept = house;
    for (final UUID wild : overflow(house, capacity)) {
      kept = kept.without(wild);
    }
    return kept;
  }
}
