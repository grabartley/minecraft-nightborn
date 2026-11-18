package com.grahambartley.nightborn.ritual;

import java.util.Optional;

public enum AltarUse {
  SET_CHALICE(null),
  TAKE_CHALICE(null),
  POUR(null),
  START(null),
  BUSY(null),
  NO_CHALICE("nightborn.ritual.no_chalice"),
  NOT_FULL("nightborn.ritual.not_full"),
  NOT_NIGHT("nightborn.ritual.not_night"),
  PASS(null);

  public record Hand(boolean empty, boolean chalice, boolean sneaking) {}

  public record Altar(boolean active, boolean chaliceSet, boolean empty, boolean full) {}

  private final String refusal;

  AltarUse(final String refusal) {
    this.refusal = refusal;
  }

  public static AltarUse decide(
      final Altar altar, final Hand hand, final boolean vampire, final boolean night) {
    if (altar.active()) {
      return BUSY;
    }
    if (!altar.chaliceSet()) {
      if (hand.chalice()) {
        return SET_CHALICE;
      }
      return hand.empty() ? NO_CHALICE : PASS;
    }
    if (!hand.empty()) {
      return POUR;
    }
    if (hand.sneaking()) {
      return TAKE_CHALICE;
    }
    if (altar.full() && night) {
      return START;
    }
    if (vampire && !altar.empty()) {
      return POUR;
    }
    return altar.full() ? NOT_NIGHT : NOT_FULL;
  }

  public Optional<String> refusal() {
    return Optional.ofNullable(refusal);
  }
}
