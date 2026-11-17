package com.grahambartley.nightborn.boss;

import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;

public final class PhaseTrack {

  static final String KEY = "BossPhase";
  static final float SLACK = 0.5F;

  private int index;

  public static double floor(final List<Integer> shares, final int index) {
    final int total = shares.stream().mapToInt(Integer::intValue).sum();
    if (total <= 0) {
      return 0.0;
    }
    int after = 0;
    for (int i = index + 1; i < shares.size(); i++) {
      after += shares.get(i);
    }
    return (double) after / total;
  }

  public static double ceiling(final List<Integer> shares, final int index) {
    return index <= 0 ? 1.0 : floor(shares, index - 1);
  }

  public static float held(final float health, final float floorHealth, final boolean uncapped) {
    return uncapped ? health : Math.max(health, floorHealth);
  }

  public static boolean spent(final float health, final float floorHealth, final boolean last) {
    return !last && health <= floorHealth + SLACK;
  }

  public static float barPercent(
      final float health, final float floorHealth, final float ceilingHealth) {
    return ceilingHealth <= floorHealth
        ? 0.0F
        : MathHelper.clamp((health - floorHealth) / (ceilingHealth - floorHealth), 0.0F, 1.0F);
  }

  public int index() {
    return index;
  }

  public boolean last(final int phases) {
    return index >= phases - 1;
  }

  public void advance(final int phases) {
    index = Math.min(index + 1, Math.max(0, phases - 1));
  }

  public void reset() {
    index = 0;
  }

  public void write(final NbtCompound nbt) {
    nbt.putInt(KEY, index);
  }

  public void read(final NbtCompound nbt) {
    index = Math.max(0, nbt.getInt(KEY));
  }
}
