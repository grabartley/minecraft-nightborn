package com.grahambartley.nightborn.coating;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

public record CoatingMix(Coating coating, int target, int count) {

  public static final int MAX_AMMO = 8;

  public sealed interface Role {
    record Agent(Coating coating) implements Role {}

    record Weapon() implements Role {}

    record Ammo() implements Role {}

    record Empty() implements Role {}

    record Other() implements Role {}
  }

  public static <T> Optional<CoatingMix> of(
      final List<T> slots, final Function<T, Role> roleOf, final BiPredicate<T, T> sameAmmo) {
    Coating coating = null;
    int weapon = -1;
    int firstAmmo = -1;
    int ammo = 0;
    for (int slot = 0; slot < slots.size(); slot++) {
      final T item = slots.get(slot);
      switch (roleOf.apply(item)) {
        case Role.Agent agent -> {
          if (coating != null) {
            return Optional.empty();
          }
          coating = agent.coating();
        }
        case Role.Weapon ignored -> {
          if (weapon >= 0) {
            return Optional.empty();
          }
          weapon = slot;
        }
        case Role.Ammo ignored -> {
          if (firstAmmo >= 0 && !sameAmmo.test(slots.get(firstAmmo), item)) {
            return Optional.empty();
          }
          firstAmmo = firstAmmo < 0 ? slot : firstAmmo;
          ammo++;
        }
        case Role.Empty ignored -> {}
        case Role.Other ignored -> {
          return Optional.empty();
        }
      }
    }
    if (coating == null || (weapon >= 0) == (ammo > 0) || ammo > MAX_AMMO) {
      return Optional.empty();
    }
    return Optional.of(
        weapon >= 0
            ? new CoatingMix(coating, weapon, 1)
            : new CoatingMix(coating, firstAmmo, ammo));
  }
}
