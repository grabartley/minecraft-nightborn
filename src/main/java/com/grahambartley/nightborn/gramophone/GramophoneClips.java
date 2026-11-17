package com.grahambartley.nightborn.gramophone;

import java.util.List;
import java.util.Optional;

public final class GramophoneClips {

  public static final String IDLE = "idle";
  public static final String LOADED = "loaded";
  public static final String INSERT = "insert";
  public static final String PLAYING = "playing";
  public static final String STOP = "stop";
  public static final String EJECT = "eject";

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
    return switch (now) {
      case EMPTY -> List.of(EJECT, IDLE);
      case LOADED ->
          before == GramophoneState.PLAYING ? List.of(STOP, LOADED) : List.of(INSERT, LOADED);
      case PLAYING -> before == GramophoneState.EMPTY ? List.of(INSERT, PLAYING) : List.of(PLAYING);
    };
  }

  static String hold(final GramophoneState state) {
    return switch (state) {
      case EMPTY -> IDLE;
      case LOADED -> LOADED;
      case PLAYING -> PLAYING;
    };
  }
}
