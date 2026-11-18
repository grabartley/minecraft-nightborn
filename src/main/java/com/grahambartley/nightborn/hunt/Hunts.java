package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.config.HuntSettings;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.inquisitor.GrandInquisitorEntity;
import com.grahambartley.nightborn.inquisitor.InquisitorArrival;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;

public final class Hunts {

  static final int CHECK_TICKS = 20;
  static final float TOLL_PITCH = 0.7F;

  private Hunts() {}

  public static void tick(final ServerPlayerEntity player) {
    BellRing.tick(player);
    final HuntRecord record = player.getAttached(HuntAttachment.RECORD);
    if (record == null) {
      return;
    }
    final long now = time(player);
    final Long previous = player.getAttached(HuntAttachment.CLOCK);
    player.setAttached(HuntAttachment.CLOCK, now);
    final HuntSettings settings = ServerConfigService.current().hunt();
    final long night = AgeNight.index(now);
    HuntRecord next = record;
    if (previous != null && HuntRules.crossed(previous, now, NightTime.DAWN)) {
      next = HuntRules.faded(next, settings);
    }
    next = HuntRules.settle(next, night, settings);
    if (previous != null && HuntRules.crossed(previous, now, NightTime.DUSK)) {
      final HuntRules.Dusk dusk = HuntRules.dusk(next);
      next = dusk.record();
      if (dusk.warned() > 0) {
        warn(player, dusk.warned(), settings);
      }
    }
    if (player.age % CHECK_TICKS == 0) {
      next = HuntVisits.check(player, next, now, settings);
    }
    store(player, next, night);
  }

  public static void add(final ServerPlayerEntity vampire, final double amount) {
    if (amount <= 0.0) {
      return;
    }
    final HuntSettings settings = ServerConfigService.current().hunt();
    final long night = AgeNight.index(time(vampire));
    final HuntRecord record = HuntAttachment.get(vampire);
    final double raised = HuntRules.gain(record.notoriety(), amount, record.quiet(night), settings);
    store(vampire, HuntRules.settle(record.withNotoriety(raised), night, settings), night);
  }

  public static boolean set(final ServerPlayerEntity vampire, final double notoriety) {
    final HuntRecord record = HuntAttachment.get(vampire);
    if (record.notoriety() == notoriety) {
      return false;
    }
    final long night = AgeNight.index(time(vampire));
    store(
        vampire,
        HuntRules.settle(
            record.withNotoriety(notoriety), night, ServerConfigService.current().hunt()),
        night);
    return true;
  }

  public static void visited(final ServerPlayerEntity quarry, final GrandInquisitorEntity man) {
    store(
        quarry,
        HuntAttachment.get(quarry).visitedBy(man.getUuid(), man.givenName()),
        AgeNight.index(time(quarry)));
  }

  public static void endVisit(final ServerPlayerEntity player) {
    final long night = AgeNight.index(time(player));
    store(
        player,
        HuntAttachment.get(player).ended(night, ServerConfigService.current().hunt().quietNights()),
        night);
  }

  public static void returnLater(final ServerPlayerEntity player) {
    store(player, HuntAttachment.get(player).returning(), AgeNight.index(time(player)));
  }

  public static void clear(final ServerPlayerEntity player) {
    HuntAttachment.get(player)
        .visitor()
        .ifPresent(man -> InquisitorArrival.dismiss(player.server.getOverworld(), man));
    HuntAttachment.clear(player);
  }

  public static long time(final ServerPlayerEntity player) {
    return player.server.getOverworld().getTimeOfDay();
  }

  private static void store(
      final ServerPlayerEntity player, final HuntRecord record, final long night) {
    if (record.idle(night)) {
      player.removeAttached(HuntAttachment.RECORD);
      player.removeAttached(HuntAttachment.CLOCK);
    } else if (!record.equals(player.getAttached(HuntAttachment.RECORD))) {
      player.setAttached(HuntAttachment.RECORD, record);
    }
  }

  private static void warn(
      final ServerPlayerEntity vampire, final int left, final HuntSettings settings) {
    vampire.playSoundToPlayer(
        HuntSounds.WARNING_TOLL,
        SoundCategory.AMBIENT,
        HuntRules.tollVolume(left, settings.warningNights()),
        TOLL_PITCH);
    vampire.sendMessage(
        Text.translatable("nightborn.hunt.warning." + HuntRules.warningLine(left)), false);
  }
}
