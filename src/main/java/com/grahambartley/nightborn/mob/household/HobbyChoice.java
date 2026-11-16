package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.mob.look.Weighted;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public final class HobbyChoice {

  private HobbyChoice() {}

  public static Optional<Hobby> pick(
      final Temperament temperament,
      final NightPhase phase,
      final Set<Hobby> available,
      final double roll) {
    if (phase == NightPhase.DAY) {
      return Optional.empty();
    }
    if (phase == NightPhase.GATHER && available.contains(Hobby.CONVERSE)) {
      return Optional.of(Hobby.CONVERSE);
    }
    return Weighted.pick(
        Arrays.stream(Hobby.values()).filter(available::contains).toList(),
        hobby -> temperament.hobbies().getOrDefault(hobby, 0),
        roll);
  }
}
