package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.config.DismembermentSettings;
import com.grahambartley.nightborn.gore.Blow;
import java.util.Optional;

public final class SeverRules {

  public static final long CUT_COOLDOWN_TICKS = 200L;

  private SeverRules() {}

  public record Victim(
      boolean maimable,
      boolean player,
      float maxHealth,
      float health,
      boolean staggered,
      boolean cutRecently) {}

  public static boolean severs(
      final DismembermentSettings settings,
      final Victim victim,
      final Blow blow,
      final boolean byPlayer,
      final boolean pvp,
      final float taken,
      final double roll) {
    return settings.enabled()
        && victim.health() > 0.0F
        && maimable(settings, victim, byPlayer, pvp)
        && cuts(blow, taken, share(settings.massiveBlow(), victim.maxHealth()))
        && taken >= share(settings.strongBlow(), victim.maxHealth())
        && open(victim, share(settings.badlyHurt(), victim.maxHealth()))
        && !victim.cutRecently()
        && roll < settings.severChance() / 100.0;
  }

  public static Optional<BodyPart> part(final Optional<BodyPart> aimed, final BodyState body) {
    return aimed.filter(BodyPart::limb).filter(part -> !body.missing(part));
  }

  static boolean maimable(
      final DismembermentSettings settings,
      final Victim victim,
      final boolean byPlayer,
      final boolean pvp) {
    if (victim.player()) {
      return settings.playersMaimed() && (!byPlayer || pvp);
    }
    return victim.maimable();
  }

  static boolean cuts(final Blow blow, final float taken, final float massive) {
    return !blow.barred() && (blow.severing() || taken >= massive);
  }

  static boolean open(final Victim victim, final float badlyHurt) {
    return victim.staggered() || victim.health() < badlyHurt;
  }

  static float share(final int percent, final float maxHealth) {
    return maxHealth * percent / 100.0F;
  }
}
