package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.weakened.Weakened;

public final class VampireHiss {

  private long nextHiss;
  private boolean warded;

  public static boolean mayHiss(
      final boolean client,
      final boolean alive,
      final boolean downed,
      final boolean rising,
      final long now,
      final long nextAllowed) {
    return !client && alive && !downed && !rising && Hissing.ready(now, nextAllowed);
  }

  public static boolean newlyWarded(final boolean nowWarded, final boolean wasWarded) {
    return nowWarded && !wasWarded;
  }

  public void hiss(final VampireEntity vampire) {
    final long now = vampire.getWorld().getTime();
    if (!mayHiss(
        vampire.getWorld().isClient(),
        vampire.isAlive(),
        Downed.state(vampire) != null,
        CoffinRise.pose(vampire).isPresent(),
        now,
        nextHiss)) {
      return;
    }
    nextHiss = Hissing.nextAllowed(now, ServerConfigService.current().mobs().vampireHissSeconds());
    vampire.triggerAnim(VampireAnimator.HISS_CONTROLLER, VampireAnimator.HISS_CLIP);
    vampire.speak(VampireVoice.HISS);
  }

  public void tickWard(final VampireEntity vampire) {
    final boolean nowWarded = Hissing.warded(Weakened.sources(vampire));
    if (newlyWarded(nowWarded, warded)) {
      hiss(vampire);
    }
    warded = nowWarded;
  }
}
