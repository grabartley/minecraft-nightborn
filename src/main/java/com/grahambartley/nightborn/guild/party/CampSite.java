package com.grahambartley.nightborn.guild.party;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.math.BlockPos;

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

  public static boolean apart(
      final Optional<BlockPos> lastCamp, final int x, final int z, final int apart) {
    return lastCamp
        .map(
            last -> {
              final int dx = x - last.getX();
              final int dz = z - last.getZ();
              return dx * dx + dz * dz >= apart * apart;
            })
        .orElse(true);
  }

  public static List<int[]> offsets(final int search, final int stride) {
    final List<int[]> offsets = new ArrayList<>();
    for (int dx = -search; dx <= search; dx += stride) {
      for (int dz = -search; dz <= search; dz += stride) {
        offsets.add(new int[] {dx, dz});
      }
    }
    offsets.sort(Comparator.comparingInt(offset -> offset[0] * offset[0] + offset[1] * offset[1]));
    return List.copyOf(offsets);
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
    return fits(survey, bounds, Optional.empty());
  }

  public static boolean fitsAround(
      final boolean fireStands, final Survey survey, final Bounds bounds, final BlockPos fire) {
    return fireStands && fits(survey, bounds, Optional.of(fire));
  }

  private static boolean fits(
      final Survey survey, final Bounds bounds, final Optional<BlockPos> fire) {
    for (int dx = bounds.minX() - 1; dx <= bounds.maxX() + 1; dx++) {
      for (int dz = bounds.minZ() - 1; dz <= bounds.maxZ() + 1; dz++) {
        final boolean beside =
            dx < bounds.minX() || dx > bounds.maxX() || dz < bounds.minZ() || dz > bounds.maxZ();
        if (!groundHolds(survey.at(dx, -1, dz), beside)) {
          return false;
        }
        for (int dy = 0; dy <= bounds.height(); dy++) {
          if (!isFire(fire, dx, dy, dz) && !open(survey.at(dx, dy, dz))) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private static boolean isFire(
      final Optional<BlockPos> fire, final int dx, final int dy, final int dz) {
    return fire.filter(at -> at.getX() == dx && at.getY() == dy && at.getZ() == dz).isPresent();
  }

  private static boolean groundHolds(final Ground ground, final boolean beside) {
    return beside ? ground != Ground.BUILT : ground == Ground.NATURAL;
  }

  private static boolean open(final Ground ground) {
    return ground == Ground.AIR || ground == Ground.CLEARABLE;
  }
}
