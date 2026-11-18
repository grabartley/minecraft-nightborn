package com.grahambartley.nightborn.fight;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class RecentBlows {

  static final int KEEP_TICKS = 1200;

  public static final AttachmentType<Blows> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "recent_blows"));

  private RecentBlows() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(
        (victim, source, base, taken, blocked) -> {
          if (source.getAttacker() instanceof LivingEntity attacker && attacker != victim) {
            note(victim, attacker);
          }
        });
  }

  public static void note(final LivingEntity one, final LivingEntity other) {
    final long now = one.getWorld().getTime();
    one.setAttached(
        TYPE, one.getAttachedOrElse(TYPE, Blows.NONE).with(other.getUuid(), now, KEEP_TICKS));
    other.setAttached(
        TYPE, other.getAttachedOrElse(TYPE, Blows.NONE).with(one.getUuid(), now, KEEP_TICKS));
  }

  public static boolean fought(
      final LivingEntity one, final LivingEntity other, final int windowTicks) {
    return one.getAttachedOrElse(TYPE, Blows.NONE)
        .recent(other.getUuid(), one.getWorld().getTime(), windowTicks);
  }
}
