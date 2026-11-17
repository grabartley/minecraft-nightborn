package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.ward.WardTags;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public final class GarlicMeal {

  public static final String WEAKENED_SOURCE = "garlic_eaten";
  static final int NAUSEA_FACTOR = 3;
  private static final int TICKS_PER_SECOND = 20;

  public record Toll(int damage, int nauseaTicks, int weakenStrength, int weakenTicks, int blood) {}

  private GarlicMeal() {}

  public static boolean is(final ItemStack stack) {
    return stack.isIn(WardTags.GARLIC);
  }

  public static Toll toll(
      final boolean garlic,
      final int vomitDamage,
      final int nauseaTicks,
      final int blood,
      final WardSettings wards) {
    if (!garlic) {
      return new Toll(vomitDamage, nauseaTicks, 0, 0, blood);
    }
    return new Toll(
        vomitDamage + wards.eatenDamage(),
        nauseaTicks * NAUSEA_FACTOR,
        wards.touchStrength(),
        wards.eatenSeconds() * TICKS_PER_SECOND,
        Math.max(0, blood - wards.eatenBlood()));
  }

  static void sicken(final PlayerEntity vampire, final Toll toll) {
    if (toll.weakenTicks() > 0) {
      Weakened.add(vampire, WEAKENED_SOURCE, toll.weakenStrength(), toll.weakenTicks());
    }
    if (toll.blood() != BloodAttachment.get(vampire)) {
      BloodAttachment.set(vampire, toll.blood());
    }
  }
}
