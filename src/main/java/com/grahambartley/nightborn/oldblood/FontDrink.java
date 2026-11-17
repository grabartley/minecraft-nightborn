package com.grahambartley.nightborn.oldblood;

import java.util.Optional;

public enum FontDrink {
  DRINKS(null),
  PASS(null),
  DRY("nightborn.old_blood.dry"),
  ALREADY_SICK("nightborn.old_blood.already_sick");

  private final String refusal;

  FontDrink(final String refusal) {
    this.refusal = refusal;
  }

  public static FontDrink decide(
      final boolean emptyHand, final boolean vampire, final boolean full, final boolean sick) {
    if (!emptyHand || vampire) {
      return PASS;
    }
    if (!full) {
      return DRY;
    }
    return sick ? ALREADY_SICK : DRINKS;
  }

  public Optional<String> refusal() {
    return Optional.ofNullable(refusal);
  }
}
