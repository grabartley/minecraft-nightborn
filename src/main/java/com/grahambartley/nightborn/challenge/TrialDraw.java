package com.grahambartley.nightborn.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.util.Identifier;

public final class TrialDraw {

  public record Candidate(Identifier trial, boolean finishedBefore) {}

  private TrialDraw() {}

  public static Optional<ChallengeRecord> renewed(
      final ChallengeRecord record,
      final Optional<Side> side,
      final long period,
      final List<Candidate> open,
      final int size,
      final long seed) {
    final TrialOffer current = record.offer();
    if (current.current(side, period)) {
      return Optional.empty();
    }
    final List<TrialOffer> kept = new ArrayList<>();
    for (final TrialOffer shelved : record.shelved()) {
      if (shelved.period() == period && !shelved.side().equals(current.side())) {
        kept.add(shelved);
      }
    }
    if (current.side().isPresent() && current.period() == period) {
      kept.add(current);
    }
    final Optional<TrialOffer> back =
        kept.stream().filter(shelved -> shelved.side().equals(side)).findFirst();
    back.ifPresent(kept::remove);
    final TrialOffer next = back.orElseGet(() -> drawn(side, period, open, size, seed));
    final ChallengeRecord renewed = record.withShelved(next, kept);
    return renewed.equals(record) ? Optional.empty() : Optional.of(renewed);
  }

  static TrialOffer drawn(
      final Optional<Side> side,
      final long period,
      final List<Candidate> open,
      final int size,
      final long seed) {
    if (side.isEmpty()) {
      return TrialOffer.NONE;
    }
    final boolean taken = side.get() == Side.VAMPIRE;
    return new TrialOffer(
        side,
        period,
        draw(open, size, seed).stream().map(id -> OfferedTrial.fresh(id, taken)).toList());
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
