package com.grahambartley.nightborn.hold;

import java.util.Optional;

public final class HoldRules {

  private HoldRules() {}

  public static boolean takes(
      final HoldState state, final long now, final boolean boss, final boolean alive) {
    return alive && !boss && !state.held(now) && now >= state.immuneUntil();
  }

  public static HoldState start(
      final HoldKind kind, final long now, final int ticks, final int immuneTicks) {
    final long until = now + Math.max(1, ticks);
    return new HoldState(Optional.of(kind), until, until + Math.max(0, immuneTicks));
  }

  public static boolean ended(final HoldState state, final long now) {
    return state.kind().isPresent() && now >= state.until();
  }
}
