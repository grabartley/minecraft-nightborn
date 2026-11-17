package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.vanish.VanishAttachment;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.util.Identifier;

public final class SkillFeatures {

  static final Identifier AFTER_ATTACHMENTS_COPY =
      Identifier.of(Nightborn.MOD_ID, "after_attachments_copy");

  private SkillFeatures() {}

  public static void register() {
    Skills.register();
    SkillAttachment.register();
    PowerCooldownAttachment.register();
    SkillShop.register();
    Powers.register();
    VanishAttachment.register();
    Tracking.register();
    ServerPlayConnectionEvents.JOIN.register(
        (handler, sender, server) -> SkillAttributes.apply(handler.getPlayer()));
    ServerPlayerEvents.AFTER_RESPAWN.addPhaseOrdering(Event.DEFAULT_PHASE, AFTER_ATTACHMENTS_COPY);
    ServerPlayerEvents.AFTER_RESPAWN.register(
        AFTER_ATTACHMENTS_COPY, (oldPlayer, newPlayer, alive) -> SkillAttributes.apply(newPlayer));
  }
}
