package com.grahambartley.nightborn.gramophone;

import java.util.List;
import java.util.Optional;

public final class GramophoneClips {

  public static final String IDLE = "idle";
  public static final String LOADED = "loaded";
  public static final String PLAYING = "playing";
  public static final String STOP = "stop";

  private GramophoneState seen;

  public Optional<List<String>> observe(final GramophoneState now) {
    final GramophoneState before = this.seen;
    this.seen = now;
    if (before == now) {
      return Optional.empty();
    }
    return Optional.of(before == null ? List.of(hold(now)) : change(before, now));
  }

  static List<String> change(final GramophoneState before, final GramophoneState now) {
    return before == GramophoneState.PLAYING && now == GramophoneState.LOADED
        ? List.of(STOP, LOADED)
        : List.of(hold(now));
  }

  static String hold(final GramophoneState state) {
    return switch (state) {
      case EMPTY -> IDLE;
      case LOADED -> LOADED;
      case PLAYING -> PLAYING;
    };
  }
}
