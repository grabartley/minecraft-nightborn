package com.grahambartley.nightborn.guild;

import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;

public final class ShownHunterAction {

  private HunterAction shown = HunterAction.NONE;

  public boolean changes(final HunterAction action) {
    final boolean changed = action != shown;
    shown = action;
    return changed;
  }

  public <T extends GeoAnimatable> PlayState play(
      final AnimationState<T> state, final HunterAction action) {
    if (changes(action)) {
      state.getController().forceAnimationReset();
    }
    return action == HunterAction.NONE ? PlayState.STOP : state.setAndContinue(action.clip());
  }
}
