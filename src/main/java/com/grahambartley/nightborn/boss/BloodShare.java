package com.grahambartley.nightborn.boss;

public record BloodShare(int blood, float carry) {

  public static final BloodShare NONE = new BloodShare(0, 0.0F);

  public static BloodShare after(final float carry, final float dealt, final int percent) {
    if (dealt <= 0.0F || percent <= 0) {
      return new BloodShare(0, Math.max(0.0F, carry));
    }
    final float total = Math.max(0.0F, carry) + dealt * percent / 100.0F;
    final int blood = (int) Math.floor(total);
    return new BloodShare(blood, total - blood);
  }
}
