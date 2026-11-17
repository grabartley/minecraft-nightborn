package com.grahambartley.nightborn.ritual;

import java.util.Optional;

public enum AltarUse {
  PLACE(null),
  TAKE(null),
  START(null),
  BUSY(null),
  NOT_HUMAN("nightborn.ritual.not_human"),
  NOT_NIGHT("nightborn.ritual.not_night"),
  PASS(null);

  public record Hand(boolean empty, boolean relic, boolean alreadyPlaced, boolean sneaking) {}

  public record Altar(boolean active, int placed, int needed) {

    boolean complete() {
      return needed > 0 && placed >= needed;
    }
  }

  private final String refusal;

  AltarUse(final String refusal) {
    this.refusal = refusal;
  }

  public static AltarUse decide(
      final Altar altar, final Hand hand, final boolean human, final boolean night) {
    if (altar.active()) {
      return BUSY;
    }
    if (hand.relic()) {
      return hand.alreadyPlaced() ? PASS : PLACE;
    }
    if (!hand.empty() || altar.placed() == 0) {
      return PASS;
    }
    if (hand.sneaking() || !altar.complete()) {
      return TAKE;
    }
    if (!human) {
      return NOT_HUMAN;
    }
    return night ? START : NOT_NIGHT;
  }

  public Optional<String> refusal() {
    return Optional.ofNullable(refusal);
  }
}
