package com.grahambartley.nightborn.render;

import java.util.Arrays;
import java.util.Optional;
import software.bernie.geckolib.animation.RawAnimation;

public enum WingedStrike {
  ROAR("roar"),
  HURT("hurt"),
  CLAW("claw");

  private final String animation;
  private final RawAnimation clip;

  WingedStrike(final String animation) {
    this.animation = animation;
    this.clip = RawAnimation.begin().thenPlay(animation);
  }

  public static Optional<WingedStrike> next(
      final Optional<WingedStrike> begun, final Optional<WingedStrike> playing) {
    if (begun.isPresent()
        && playing.map(clip -> begun.get().ordinal() <= clip.ordinal()).orElse(true)) {
      return begun;
    }
    return playing;
  }

  public static Optional<WingedStrike> playing(final RawAnimation animation) {
    return Arrays.stream(values()).filter(strike -> strike.clip.equals(animation)).findFirst();
  }

  public String animation() {
    return animation;
  }

  public RawAnimation clip() {
    return clip;
  }
}
