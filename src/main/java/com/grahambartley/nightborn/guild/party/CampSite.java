package com.grahambartley.nightborn.guild.party;

import java.util.ArrayList;
import java.util.List;

public final class CampSite {

  public enum Ground {
    NATURAL,
    CLEARABLE,
    AIR,
    BUILT
  }

  @FunctionalInterface
  public interface Survey {
    Ground at(int dx, int dy, int dz);
  }

  public record Bounds(int minX, int maxX, int minZ, int maxZ, int height) {}

  public static final int CROWN = 32;

  private CampSite() {}

  public static Ground classify(
      final boolean air, final boolean clearable, final boolean natural, final boolean dry) {
    if (air) {
      return Ground.AIR;
    }
    if (clearable) {
      return Ground.CLEARABLE;
    }
    return natural && dry ? Ground.NATURAL : Ground.BUILT;
  }

  public static List<int[]> corners(final Bounds bounds) {
    final List<int[]> corners = new ArrayList<>();
    for (final int dx : new int[] {bounds.minX() - 1, bounds.maxX() + 1}) {
      for (final int dz : new int[] {bounds.minZ() - 1, bounds.maxZ() + 1}) {
        corners.add(new int[] {dx, dz});
      }
    }
    return corners;
  }

  public static boolean fits(final Survey survey, final Bounds bounds) {
    for (int dx = bounds.minX() - 1; dx <= bounds.maxX() + 1; dx++) {
      for (int dz = bounds.minZ() - 1; dz <= bounds.maxZ() + 1; dz++) {
        final boolean beside =
            dx < bounds.minX() || dx > bounds.maxX() || dz < bounds.minZ() || dz > bounds.maxZ();
        if (!groundHolds(survey.at(dx, -1, dz), beside)) {
          return false;
        }
        for (int dy = 0; dy <= bounds.height(); dy++) {
          if (!open(survey.at(dx, dy, dz))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean groundHolds(final Ground ground, final boolean beside) {
    return beside ? ground != Ground.BUILT : ground == Ground.NATURAL;
  }

  private static boolean open(final Ground ground) {
    return ground == Ground.AIR || ground == Ground.CLEARABLE;
  }
}
