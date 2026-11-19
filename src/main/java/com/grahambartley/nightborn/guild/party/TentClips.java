package com.grahambartley.nightborn.guild.party;

import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.animation.RawAnimation;

public final class TentClips {

  public static final String PITCH = "pitch";
  public static final String IDLE = "idle";
  public static final String STRIKE = "strike";
  public static final long PITCH_TICKS = 20L;

  private enum Seen {
    UNSEEN,
    STANDING,
    STRUCK
  }

  private Seen seen = Seen.UNSEEN;

  public Optional<List<String>> observe(final boolean struck, final long age) {
    final Seen now = struck ? Seen.STRUCK : Seen.STANDING;
    final Seen previous = this.seen;
    if (previous == now) {
      return Optional.empty();
    }
    this.seen = now;
    if (struck) {
      return Optional.of(List.of(STRIKE));
    }
    if (previous == Seen.UNSEEN && age < PITCH_TICKS) {
      return Optional.of(List.of(PITCH, IDLE));
    }
    return Optional.of(List.of(IDLE));
  }

  public static RawAnimation animation(final List<String> names) {
    RawAnimation animation = RawAnimation.begin();
    for (final String name : names) {
      animation = IDLE.equals(name) ? animation.thenLoop(name) : animation.thenPlay(name);
    }
    return animation;
  }
}
