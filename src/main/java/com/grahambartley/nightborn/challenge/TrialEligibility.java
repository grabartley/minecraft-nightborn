package com.grahambartley.nightborn.challenge;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.Identifier;

public record TrialEligibility(
    Optional<Side> side,
    int ladderRank,
    Function<Identifier, OptionalInt> gateRank,
    Predicate<Identifier> owns,
    Set<Identifier> liveDeeds) {

  public boolean open(final Trial trial) {
    return side.isPresent()
        && trial.side() != Side.ANYONE
        && trial.side().holds(side)
        && liveDeeds.contains(trial.goal().match().deed())
        && gateMet(trial)
        && (trial.skills().isEmpty() || trial.skills().stream().anyMatch(owns));
  }

  private boolean gateMet(final Trial trial) {
    return trial
        .gate()
        .map(gate -> gateRank.apply(gate))
        .map(rank -> rank.isPresent() && ladderRank >= rank.getAsInt())
        .orElse(true);
  }
}
