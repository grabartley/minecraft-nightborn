package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public final class StrikeHealing {

  private static final Map<UUID, Long> LAST_HEAL = new HashMap<>();

  private StrikeHealing() {}

  static void afterDamage(
      final LivingEntity target,
      final DamageSource source,
      final float baseDamage,
      final float damage,
      final boolean blocked) {
    if (blocked
        || damage <= 0.0F
        || !Vampires.isVampire(target)
        || !(source.getAttacker() instanceof LivingEntity striker)
        || source.getSource() != striker
        || striker == target) {
      return;
    }
    final double heal = GiftAttributes.value(striker, GiftAttributes.STRIKE_HEAL);
    final long now = striker.getWorld().getTime();
    final Long last = LAST_HEAL.get(striker.getUuid());
    if (!ready(
        heal,
        now,
        last == null ? Long.MIN_VALUE : last,
        GiftAttributes.value(striker, GiftAttributes.STRIKE_HEAL_INTERVAL))) {
      return;
    }
    LAST_HEAL.put(striker.getUuid(), now);
    striker.heal((float) heal);
  }

  public static boolean ready(
      final double heal, final long now, final long last, final double intervalTicks) {
    return heal > 0.0 && (last == Long.MIN_VALUE || now - last >= (long) intervalTicks);
  }

  public static void forget(final UUID player) {
    LAST_HEAL.remove(player);
  }

  public static void clearAll() {
    LAST_HEAL.clear();
  }
}
