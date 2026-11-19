package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.AreaRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

public final class TimedMark {

  private final AttachmentType<Long> type;

  TimedMark(final String path) {
    type = AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, path));
  }

  public void set(final Entity entity, final int ticks) {
    final long until = entity.getWorld().getTime() + ticks;
    final Long current = entity.getAttached(type);
    entity.setAttached(type, current == null ? until : Math.max(current, until));
  }

  public boolean on(final Entity entity) {
    final Long until = entity.getAttached(type);
    if (until == null) {
      return false;
    }
    if (AreaRules.lasts(entity.getWorld().getTime(), until)) {
      return true;
    }
    entity.removeAttached(type);
    return false;
  }
}
