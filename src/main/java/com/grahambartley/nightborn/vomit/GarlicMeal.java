package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.ward.WardTags;
import net.minecraft.item.ItemStack;

public final class GarlicMeal {

  public static final String WEAKENED_SOURCE = "garlic_eaten";
  static final int NAUSEA_FACTOR = 3;
  private static final int TICKS_PER_SECOND = 20;

  public record Toll(int damage, int nauseaTicks, int weakenStrength, int weakenTicks, int blood) {

    public static Toll plain(final int vomitDamage, final int nauseaTicks, final int blood) {
      return new Toll(vomitDamage, nauseaTicks, 0, 0, blood);
    }
  }

  private GarlicMeal() {}

  public static boolean is(final ItemStack stack) {
    return stack.isIn(WardTags.GARLIC);
  }

  public static Toll worsen(final Toll plain, final WardSettings wards) {
    return new Toll(
        plain.damage() + wards.eatenDamage(),
        plain.nauseaTicks() * NAUSEA_FACTOR,
        wards.touchStrength(),
        wards.eatenSeconds() * TICKS_PER_SECOND,
        Math.max(0, plain.blood() - wards.eatenBlood()));
  }
}
