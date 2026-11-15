package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.EyeGlow;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sun.SunBurn;
import com.grahambartley.nightborn.ward.Wards;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.LivingEntity;

public final class VampireTicker {

  private VampireTicker() {}

  public static void tick(final LivingEntity vampire) {
    if (!vampire.isAlive()) {
      VisibleStateAttachment.set(vampire, VisibleState.NONE);
      return;
    }
    final NightbornConfig config = ServerConfigService.current();
    Downed.tick(vampire, config.downed());
    SunBurn.tick(vampire, config.sun());
    if (!vampire.isAlive()) {
      return;
    }
    VampireCore.tick(vampire, config.core(), config.blood());
    final DownedState downed = Downed.state(vampire);
    Wards.tick(vampire, config.wards());
    final int weakened = Weakened.tick(vampire, downed != null, config.weakened());
    Levitation.tick(vampire, weakened, downed != null);
    final boolean feeding = FeedingSessions.isFeeding(vampire);
    final boolean eyes =
        EyeGlow.glowing(
            feeding,
            FeedingSessions.ticksSinceFed(vampire, vampire.getWorld().getTime()),
            vampire.getHealth(),
            vampire.getMaxHealth());
    VisibleStateAttachment.set(
        vampire, new VisibleState(eyes, feeding, downed != null && downed.isDowned(), weakened));
  }
}
