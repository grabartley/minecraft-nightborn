package com.grahambartley.nightborn.client.state;

import java.util.Objects;

public record ClientState(GoreLevel gore) {

  public static final ClientState DEFAULTS = new ClientState(GoreLevel.FULL);

  public ClientState {
    Objects.requireNonNull(gore, "gore");
  }

  public ClientState withGore(final GoreLevel level) {
    return new ClientState(level);
  }
}
