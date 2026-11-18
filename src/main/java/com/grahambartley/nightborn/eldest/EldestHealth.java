package com.grahambartley.nightborn.eldest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.BossHealth;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

public final class EldestHealth {

  static final Identifier CROWD = Identifier.of(Nightborn.MOD_ID, "eldest/crowd");

  private EldestHealth() {}

  public static boolean rescales(
      final boolean keepsToRoom,
      final boolean hadTarget,
      final boolean hasTarget,
      final boolean unhurt) {
    return keepsToRoom && !hadTarget && hasTarget && unhurt;
  }

  public static void scale(
      final LivingEntity eldest, final int players, final int percentPerPlayer) {
    BossHealth.scale(eldest, CROWD, players, percentPerPlayer);
  }
}
