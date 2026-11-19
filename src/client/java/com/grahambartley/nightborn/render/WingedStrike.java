package com.grahambartley.nightborn.render;

import java.util.Arrays;
import java.util.Optional;
import software.bernie.geckolib.animation.RawAnimation;

enum WingedStrike {
  ROAR("roar"),
  HURT("hurt"),
  CLAW("claw");

  private final String animation;
  private final RawAnimation clip;

  WingedStrike(final String animation) {
    this.animation = animation;
    this.clip = RawAnimation.begin().thenPlay(animation);
  }

  static Optional<WingedStrike> next(
      final Optional<WingedStrike> begun, final Optional<WingedStrike> playing) {
    if (begun.isPresent()
        && playing.map(clip -> begun.get().ordinal() <= clip.ordinal()).orElse(true)) {
      return begun;
    }
    return playing;
  }

  static Optional<WingedStrike> playing(final RawAnimation animation) {
    return Arrays.stream(values()).filter(strike -> strike.clip.equals(animation)).findFirst();
  }

  String animation() {
    return animation;
  }

  RawAnimation clip() {
    return clip;
  }
}
