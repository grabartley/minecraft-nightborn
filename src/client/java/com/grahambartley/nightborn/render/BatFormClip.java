package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.mob.VampireBatPose;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import software.bernie.geckolib.animation.RawAnimation;

public enum BatFormClip {
  HANG(List.of(VampireBatPose.HANG.animation())),
  FLY(List.of(VampireBatPose.FLY.animation())),
  SWOOP(List.of(VampireBatPose.SWOOP.animation())),
  SETTLE(List.of("land", "rest")),
  REST(List.of("rest")),
  CRAWL(List.of("crawl")),
  TAKE_OFF(List.of("take_off", VampireBatPose.FLY.animation()));

  private final List<String> animations;
  private final RawAnimation clip;

  BatFormClip(final List<String> animations) {
    this.animations = animations;
    RawAnimation chain = RawAnimation.begin();
    for (final String animation : animations.subList(0, animations.size() - 1)) {
      chain = chain.thenPlay(animation);
    }
    this.clip = chain.thenLoop(animations.getLast());
  }

  public static BatFormClip next(final BatFormPose pose, final Optional<BatFormClip> playing) {
    final boolean grounded =
        playing.filter(clip -> clip == SETTLE || clip == REST || clip == CRAWL).isPresent();
    return switch (pose) {
      case HANG -> HANG;
      case SWOOP -> SWOOP;
      case CRAWL -> CRAWL;
      case REST -> {
        if (!grounded) {
          yield SETTLE;
        }
        yield playing.filter(clip -> clip == SETTLE).isPresent() ? SETTLE : REST;
      }
      case FLY -> grounded || playing.filter(clip -> clip == TAKE_OFF).isPresent() ? TAKE_OFF : FLY;
    };
  }

  public static Optional<BatFormClip> playing(final RawAnimation animation) {
    return Arrays.stream(values()).filter(clip -> clip.clip.equals(animation)).findFirst();
  }

  public List<String> animations() {
    return animations;
  }

  public RawAnimation clip() {
    return clip;
  }
}
