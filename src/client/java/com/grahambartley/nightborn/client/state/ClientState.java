package com.grahambartley.nightborn.client.state;

import java.util.Objects;

public record ClientState(GoreLevel gore, boolean skipCeremonyCamera) {

  public static final ClientState DEFAULTS = new ClientState(GoreLevel.FULL, false);

  public ClientState {
    Objects.requireNonNull(gore, "gore");
  }

  public ClientState withGore(final GoreLevel level) {
    return new ClientState(level, skipCeremonyCamera);
  }

  public ClientState withSkipCeremonyCamera(final boolean skip) {
    return new ClientState(gore, skip);
  }
}
