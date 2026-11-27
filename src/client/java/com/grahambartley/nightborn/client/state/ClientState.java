package com.grahambartley.nightborn.client.state;

import java.util.Objects;

public record ClientState(GoreLevel gore, FormViewLevel formViews, boolean skipCeremonyCamera) {

  public static final ClientState DEFAULTS =
      new ClientState(GoreLevel.FULL, FormViewLevel.FULL, false);

  public ClientState {
    Objects.requireNonNull(gore, "gore");
    Objects.requireNonNull(formViews, "formViews");
  }

  public ClientState withGore(final GoreLevel level) {
    return new ClientState(level, formViews, skipCeremonyCamera);
  }

  public ClientState withFormViews(final FormViewLevel level) {
    return new ClientState(gore, level, skipCeremonyCamera);
  }

  public ClientState withSkipCeremonyCamera(final boolean skip) {
    return new ClientState(gore, formViews, skip);
  }
}
