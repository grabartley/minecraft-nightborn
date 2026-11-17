package com.grahambartley.nightborn.name;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class KinNames {

  public record Held(String name, String epithet) {}

  public record Taken(Set<String> givens, Set<String> epithets) {}

  private KinNames() {}

  public static Taken taken(final Collection<Held> kin) {
    final Set<String> givens = new HashSet<>();
    final Set<String> epithets = new HashSet<>();
    for (final Held held : kin) {
      givens.add(Epithets.given(held.name(), held.epithet()).orElse(held.name()));
      if (!held.epithet().isEmpty()) {
        epithets.add(held.epithet());
      }
    }
    return new Taken(Set.copyOf(givens), Set.copyOf(epithets));
  }
}
