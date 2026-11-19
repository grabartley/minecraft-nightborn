package com.grahambartley.nightborn.client.sight;

import com.grahambartley.nightborn.render.CoffinSleeperView;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class UnseenBodies {

  private UnseenBodies() {}

  public static boolean bodyHidden(final Entity entity) {
    return UnseenView.bodyHidden(
        VanishAttachment.isGone(entity),
        entity instanceof LivingEntity sleeper && CoffinSleeperView.hidden(sleeper));
  }

  public static boolean skipped(final Entity entity) {
    return UnseenView.hides(
        VanishAttachment.isGone(entity),
        entity instanceof LivingEntity sleeper && CoffinSleeperView.hidden(sleeper),
        RevealView.colour(entity).isPresent());
  }
}
