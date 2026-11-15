package com.grahambartley.nightborn.decor;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import software.bernie.geckolib.animation.RawAnimation;

public final class ToggleClips {

  public record Names(String activate, String active, String deactivate, String inactive) {

    public Set<String> all() {
      return Set.of(this.activate, this.active, this.deactivate, this.inactive);
    }
  }

  public static final Names FURNITURE = new Names("activate", "active", "deactivate", "inactive");

  private final Names names;
  private Boolean seen;

  public ToggleClips(final Names names) {
    this.names = names;
  }

  public Optional<List<String>> observe(final boolean active) {
    final Boolean previous = this.seen;
    this.seen = active;
    if (previous == null) {
      return Optional.of(List.of(this.hold(active)));
    }
    if (previous != active) {
      return Optional.of(
          List.of(active ? this.names.activate() : this.names.deactivate(), this.hold(active)));
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

  private String hold(final boolean active) {
    return active ? this.names.active() : this.names.inactive();
  }
}
