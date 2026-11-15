package com.grahambartley.nightborn.mob;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;

public final class ShownAction {

  static final int UNSEEN = Integer.MIN_VALUE;

  private int seen = UNSEEN;
  private boolean playing;

  public boolean fresh(final int sequence) {
    if (seen == sequence) {
      return false;
    }
    final boolean first = seen == UNSEEN;
    seen = sequence;
    return !first;
  }

  public <T extends GeoAnimatable> PlayState play(
      final AnimationState<T> state, final int sequence, final String clip) {
    if (fresh(sequence) && !clip.isEmpty()) {
      state.getController().forceAnimationReset();
      playing = true;
      return state.setAndContinue(ClipCache.once(clip));
    }
    if (!playing || state.getController().hasAnimationFinished()) {
      playing = false;
      return PlayState.STOP;
    }
    return state.setAndContinue(ClipCache.once(clip));
  }
}
