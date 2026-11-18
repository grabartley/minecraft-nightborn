package com.grahambartley.nightborn.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.util.Identifier;

public final class TrialDraw {

  public record Candidate(Identifier trial, boolean finishedBefore) {}

  private TrialDraw() {}

  public static Optional<TrialOffer> renewed(
      final TrialOffer current,
      final Optional<Side> side,
      final long period,
      final List<Candidate> open,
      final int size,
      final long seed,
      final Set<Identifier> paidThisPeriod) {
    if (current.current(side, period)) {
      return Optional.empty();
    }
    if (side.isEmpty()) {
      return current.equals(TrialOffer.NONE) ? Optional.empty() : Optional.of(TrialOffer.NONE);
    }
    final boolean taken = side.get() == Side.VAMPIRE;
    return Optional.of(
        new TrialOffer(
            side,
            period,
            draw(open, size, seed).stream()
                .map(
                    id ->
                        paidThisPeriod.contains(id)
                            ? new OfferedTrial(id, true, true, Tally.EMPTY)
                            : OfferedTrial.fresh(id, taken))
                .toList()));
  }

  public static List<Identifier> draw(
      final List<Candidate> candidates, final int size, final long seed) {
    final Random random = new Random(seed);
    final List<Identifier> fresh = new ArrayList<>();
    final List<Identifier> again = new ArrayList<>();
    candidates.stream()
        .sorted(Comparator.comparing(candidate -> candidate.trial().toString()))
        .forEach(candidate -> (candidate.finishedBefore() ? again : fresh).add(candidate.trial()));
    Collections.shuffle(fresh, random);
    Collections.shuffle(again, random);
    final List<Identifier> drawn = new ArrayList<>(fresh);
    drawn.addAll(again);
    return List.copyOf(drawn.subList(0, Math.min(Math.max(0, size), drawn.size())));
  }
}
