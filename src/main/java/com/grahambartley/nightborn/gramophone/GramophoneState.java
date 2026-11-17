package com.grahambartley.nightborn.gramophone;

public enum GramophoneState {
  EMPTY,
  LOADED,
  PLAYING;

  public static GramophoneState of(final boolean hasRecord, final boolean playing) {
    if (!hasRecord) {
      return EMPTY;
    }
    return playing ? PLAYING : LOADED;
  }
}
