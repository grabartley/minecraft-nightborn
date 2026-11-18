package com.grahambartley.nightborn.client.moment;

import com.grahambartley.nightborn.moment.Moment;
import com.grahambartley.nightborn.moment.MomentState;
import net.minecraft.util.math.MathHelper;

public final class CeremonyFade {

  static final float EDGE = 0.2F;
  static final float DEEPEST_NIGHT = 0.45F;

  private CeremonyFade() {}

  public static float card(final MomentState state, final float elapsed) {
    if (state.phase(elapsed) != MomentState.Phase.CARD) {
      return 0.0F;
    }
    final float t = state.cardProgress(elapsed);
    return MathHelper.clamp(Math.min(t, 1.0F - t) / EDGE, 0.0F, 1.0F);
  }

  public static float night(final MomentState state, final float elapsed) {
    if (state.moment() != Moment.TURNING) {
      return 0.0F;
    }
    return switch (state.phase(elapsed)) {
      case CAMERA -> DEEPEST_NIGHT * state.cameraProgress(elapsed);
      case CARD -> DEEPEST_NIGHT * (1.0F - state.cardProgress(elapsed));
      case OVER -> 0.0F;
    };
  }
}
