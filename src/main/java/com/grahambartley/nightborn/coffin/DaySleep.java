package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.OptionalLong;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class DaySleep {

  static final AttachmentType<Long> SLEPT_FROM =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "day_slept_from"));
  static final AttachmentType<Boolean> SKIPPED =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "day_skipped_asleep"));

  private DaySleep() {}

  public static void lay(final LivingEntity sleeper) {
    if (!(sleeper instanceof ServerPlayerEntity player) || !CoffinSleepers.inCoffin(player)) {
      return;
    }
    player.setAttached(SLEPT_FROM, player.getServerWorld().getTimeOfDay());
    player.removeAttached(SKIPPED);
  }

  static void skipped(final ServerPlayerEntity sleeper) {
    sleeper.setAttached(SKIPPED, true);
  }

  static boolean wholeDay(final ServerPlayerEntity sleeper) {
    final Long from = sleeper.getAttached(SLEPT_FROM);
    return RestedRules.wholeDay(
        sleeper.getAttachedOrElse(SKIPPED, false),
        from == null ? OptionalLong.empty() : OptionalLong.of(from),
        sleeper.getServerWorld().getTimeOfDay(),
        ServerConfigService.current().coffins().wholeDayWindow());
  }

  static void forget(final ServerPlayerEntity sleeper) {
    sleeper.removeAttached(SLEPT_FROM);
    sleeper.removeAttached(SKIPPED);
  }
}
