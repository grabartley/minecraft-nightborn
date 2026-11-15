package com.grahambartley.nightborn.decor;

import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.animation.RawAnimation;

public final class ToggleClips {

  public static final String ACTIVATE = "activate";
  public static final String ACTIVE = "active";
  public static final String DEACTIVATE = "deactivate";
  public static final String INACTIVE = "inactive";

  private Boolean seen;

  public Optional<List<String>> observe(final boolean active) {
    final Boolean previous = this.seen;
    this.seen = active;
    if (previous == null) {
      return Optional.of(List.of(hold(active)));
    }
    if (previous != active) {
      return Optional.of(List.of(active ? ACTIVATE : DEACTIVATE, hold(active)));
    }
    return Optional.empty();
  }

  public static RawAnimation sequence(final List<String> names) {
    RawAnimation animation = RawAnimation.begin();
    for (int index = 0; index < names.size() - 1; index++) {
      animation = animation.thenPlay(names.get(index));
    }
    return animation.thenLoop(names.getLast());
  }

  private static String hold(final boolean active) {
    return active ? ACTIVE : INACTIVE;
  }
}
