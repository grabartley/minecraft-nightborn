package com.grahambartley.nightborn.raid;

import java.util.ArrayList;
import java.util.List;

public final class Footsteps {

  private Footsteps() {}

  public static List<NoiseAct> of(final int walked, final int sprinted, final int jumps) {
    final List<NoiseAct> acts = new ArrayList<>();
    if (sprinted > 0) {
      acts.add(NoiseAct.RUN);
    } else if (walked > 0) {
      acts.add(NoiseAct.WALK);
    }
    for (int jump = 0; jump < jumps; jump++) {
      acts.add(NoiseAct.JUMP);
    }
    return acts;
  }
}
