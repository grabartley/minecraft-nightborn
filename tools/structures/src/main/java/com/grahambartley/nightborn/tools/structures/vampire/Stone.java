package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.Template;

public record Stone(String base, String mossy, String cracked, int wear) {

  public static final int PERCENT = 100;

  public String at(final int x, final int y, final int z) {
    final int roll = roll(x, y, z);
    if (roll < wear / 2) {
      return mossy;
    }
    if (roll < wear) {
      return cracked;
    }
    return base;
  }

  public Stone worn(final int newWear) {
    return new Stone(base, mossy, cracked, newWear);
  }

  public void fill(
      final Template t,
      final int x0,
      final int y0,
      final int z0,
      final int x1,
      final int y1,
      final int z1) {
    for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
      for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
        for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
          t.set(x, y, z, at(x, y, z));
        }
      }
    }
  }

  public void walls(
      final Template t,
      final int x0,
      final int y0,
      final int z0,
      final int x1,
      final int y1,
      final int z1) {
    fill(t, x0, y0, z0, x1, y1, z0);
    fill(t, x0, y0, z1, x1, y1, z1);
    fill(t, x0, y0, z0, x0, y1, z1);
    fill(t, x1, y0, z0, x1, y1, z1);
  }

  public static int roll(final int x, final int y, final int z) {
    return Math.floorMod(x * 73_856_093 ^ y * 19_349_663 ^ z * 83_492_791, PERCENT);
  }
}
