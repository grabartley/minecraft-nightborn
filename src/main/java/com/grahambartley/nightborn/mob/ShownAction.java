package com.grahambartley.nightborn.mob;

import java.util.Set;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;

public final class ShownAction {

  static final int UNSEEN = Integer.MIN_VALUE;

  private int seen = UNSEEN;
  private boolean playing;
  private String clip = "";

  public boolean fresh(final int sequence) {
    if (seen == sequence) {
      return false;
    }
    final boolean first = seen == UNSEEN;
    seen = sequence;
    return !first;
  }

  public boolean begin(final int sequence, final String clip, final boolean held) {
    if (fresh(sequence) && !clip.isEmpty() && !held) {
      this.clip = clip;
      playing = true;
      return true;
    }
    return false;
  }

  public static boolean stops(final boolean current, final boolean blank) {
    return current && blank;
  }

  public static boolean holds(final boolean yields, final boolean playing, final boolean calm) {
    return yields && (playing || !calm);
  }

  public boolean playing() {
    return playing;
  }

  public void end() {
    playing = false;
  }

  public boolean showing(final Set<String> clips) {
    return playing && clips.contains(clip);
  }

  public <T extends GeoAnimatable> PlayState play(
      final AnimationState<T> state, final int sequence, final String clip) {
    return play(state, sequence, clip, false, true);
  }

  public <T extends GeoAnimatable> PlayState play(
      final AnimationState<T> state,
      final int sequence,
      final String clip,
      final boolean yields,
      final boolean calm) {
    if (begin(sequence, clip, holds(yields, playing, calm))) {
      state.getController().forceAnimationReset();
      return state.setAndContinue(ClipCache.once(this.clip));
    }
    if (stops(seen == sequence, clip.isEmpty())
        || !playing
        || state.getController().hasAnimationFinished()) {
      end();
      return PlayState.STOP;
    }
    return state.setAndContinue(ClipCache.once(this.clip));
  }
}
