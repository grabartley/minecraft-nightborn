package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.EyeGlow;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sun.SunBurn;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.LivingEntity;

public final class VampireTicker {

  private VampireTicker() {}

  public static void tick(final LivingEntity vampire) {
    if (!vampire.isAlive()) {
      return;
    }
    final NightbornConfig config = ServerConfigService.current();
    Downed.tick(vampire, config.downed());
    SunBurn.tick(vampire, config.sun());
    if (!vampire.isAlive()) {
      return;
    }
    VampireCore.tick(vampire, config.core());
    final DownedState downed = Downed.state(vampire);
    final int weakened = Weakened.tick(vampire, downed != null, config.weakened());
    final boolean eyes =
        EyeGlow.glowing(
            FeedingSessions.isFeeding(vampire),
            FeedingSessions.ticksSinceFed(vampire, vampire.getWorld().getTime()),
            vampire.getHealth(),
            vampire.getMaxHealth());
    VisibleStateAttachment.set(
        vampire, new VisibleState(eyes, downed != null && downed.isDowned(), weakened));
  }
}
