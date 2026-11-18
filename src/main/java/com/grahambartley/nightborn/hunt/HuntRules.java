package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.config.HuntSettings;
import com.grahambartley.nightborn.core.NightTime;
import net.minecraft.util.math.MathHelper;

public final class HuntRules {

  public enum Level {
    NONE,
    RAID,
    HUNT
  }

  public record Dusk(HuntRecord record, int warned) {}

  static final int LOST_CHECKS = 30;
  static final float FARTHEST_TOLL = 0.3F;
  static final float NEAREST_TOLL = 1.0F;

  private HuntRules() {}

  public static Level level(final double notoriety, final HuntSettings settings) {
    if (notoriety > settings.huntNotoriety()) {
      return Level.HUNT;
    }
    return notoriety > settings.raidNotoriety() ? Level.RAID : Level.NONE;
  }

  public static double gain(
      final double notoriety,
      final double amount,
      final boolean quiet,
      final HuntSettings settings) {
    final double raised = notoriety + Math.max(0.0, amount);
    if (!quiet) {
      return raised;
    }
    for (final int threshold : new int[] {settings.raidNotoriety(), settings.huntNotoriety()}) {
      if (notoriety <= threshold) {
        return Math.min(raised, threshold);
      }
    }
    return raised;
  }

  public static HuntRecord settle(
      final HuntRecord record, final long night, final HuntSettings settings) {
    final boolean past = record.notoriety() > settings.huntNotoriety();
    return switch (record.phase()) {
      case NONE -> past && !record.quiet(night) ? record.warning(settings.warningNights()) : record;
      case WARNING, DUE -> past ? record : record.ended(night, settings.quietNights());
      case VISIT -> record;
    };
  }

  public static HuntRecord faded(final HuntRecord record, final HuntSettings settings) {
    return record.withNotoriety(record.notoriety() - settings.notorietyFade());
  }

  public static Dusk dusk(final HuntRecord record) {
    if (record.phase() != HuntRecord.Phase.WARNING) {
      return new Dusk(record, 0);
    }
    final int left = record.warningsLeft();
    return left == 0 ? new Dusk(record.due(), 0) : new Dusk(record.warning(left - 1), left);
  }

  public static boolean inArrivalWindow(final long timeOfDay, final HuntSettings settings) {
    final long time = Math.floorMod(timeOfDay, NightTime.DAY_LENGTH);
    return time >= NightTime.DUSK && time < settings.latestArrival();
  }

  public static boolean fitsArrivalWindow(
      final long timeOfDay, final int span, final HuntSettings settings) {
    return inArrivalWindow(timeOfDay, settings) && inArrivalWindow(timeOfDay + span, settings);
  }

  public static boolean canArrive(
      final boolean overworld, final boolean openSky, final boolean inWindow) {
    return overworld && openSky && inWindow;
  }

  public static boolean crossed(final long previous, final long now, final long boundary) {
    if (now <= previous) {
      return false;
    }
    final long from = Math.max(previous, now - NightTime.DAY_LENGTH);
    return Math.floorDiv(now - boundary, NightTime.DAY_LENGTH)
        > Math.floorDiv(from - boundary, NightTime.DAY_LENGTH);
  }

  public static boolean lost(final boolean dead, final int missingChecks) {
    return dead || missingChecks >= LOST_CHECKS;
  }

  public static float tollVolume(final int left, final int nights) {
    if (nights <= 1) {
      return NEAREST_TOLL;
    }
    final float nearness = (float) (nights - MathHelper.clamp(left, 1, nights)) / (nights - 1);
    return MathHelper.lerp(nearness, FARTHEST_TOLL, NEAREST_TOLL);
  }

  public static String warningLine(final int left) {
    if (left <= 1) {
      return "near";
    }
    return left == 2 ? "nearer" : "far";
  }
}
