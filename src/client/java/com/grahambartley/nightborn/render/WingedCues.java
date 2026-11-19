package com.grahambartley.nightborn.render;

import java.util.Optional;

final class WingedCues {

  static final long ROAR_WINDOW_TICKS = 26;

  private long since = Long.MIN_VALUE;
  private int hurtTime;
  private boolean swinging;

  Optional<WingedStrike> observe(
      final long since, final long now, final int hurtTime, final boolean swinging) {
    final boolean changed = since != this.since && now - since < ROAR_WINDOW_TICKS;
    final boolean hurt = hurtTime > this.hurtTime;
    final boolean swung = swinging && !this.swinging;
    this.since = since;
    this.hurtTime = hurtTime;
    this.swinging = swinging;
    if (changed) {
      return Optional.of(WingedStrike.ROAR);
    }
    if (hurt) {
      return Optional.of(WingedStrike.HURT);
    }
    return swung ? Optional.of(WingedStrike.CLAW) : Optional.empty();
  }
}
