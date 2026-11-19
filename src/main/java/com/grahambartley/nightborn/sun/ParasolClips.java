package com.grahambartley.nightborn.sun;

import software.bernie.geckolib.animation.RawAnimation;

public final class ParasolClips {

  public static final RawAnimation OPENED = RawAnimation.begin().thenLoop("opened");
  public static final RawAnimation CLOSED = RawAnimation.begin().thenLoop("closed");
  public static final RawAnimation OPENING = RawAnimation.begin().thenPlayAndHold("open");
  public static final RawAnimation CLOSING = RawAnimation.begin().thenPlayAndHold("close");

  private ParasolClips() {}

  public static RawAnimation next(final RawAnimation playing, final boolean open) {
    final RawAnimation still = open ? OPENED : CLOSED;
    final RawAnimation moving = open ? OPENING : CLOSING;
    if (playing == null) {
      return still;
    }
    return still.equals(playing) || moving.equals(playing) ? playing : moving;
  }
}
