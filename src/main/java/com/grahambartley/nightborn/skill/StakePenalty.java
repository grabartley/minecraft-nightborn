package com.grahambartley.nightborn.skill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.util.Identifier;

public final class StakePenalty {

  private StakePenalty() {}

  public static int pointsLost(final int earned, final int percent) {
    return (int) Math.ceil(earned * Math.max(0, percent) / 100.0);
  }

  public static TreeProgress apply(
      final TreeProgress progress,
      final int percent,
      final Function<Identifier, Optional<Identifier>> bloodlineOf) {
    final int earned = progress.earned() - pointsLost(progress.earned(), percent);
    final List<BoughtSkill> kept = new ArrayList<>(progress.bought());
    final List<BoughtSkill> removed = new ArrayList<>();
    int spent = progress.spent();
    while (spent > earned && !kept.isEmpty()) {
      final BoughtSkill last = kept.removeLast();
      removed.add(last);
      spent -= last.paid();
    }
    final List<Identifier> bloodlines =
        progress.bloodlines().stream()
            .filter(
                line -> !belongs(removed, line, bloodlineOf) || belongs(kept, line, bloodlineOf))
            .toList();
    return new TreeProgress(earned, progress.blood(), kept, bloodlines);
  }

  private static boolean belongs(
      final List<BoughtSkill> skills,
      final Identifier line,
      final Function<Identifier, Optional<Identifier>> bloodlineOf) {
    return skills.stream()
        .anyMatch(b -> bloodlineOf.apply(b.skill()).filter(line::equals).isPresent());
  }
}
