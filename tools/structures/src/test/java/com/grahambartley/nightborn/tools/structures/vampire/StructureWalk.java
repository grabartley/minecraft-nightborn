package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StructureWalk {

  public record Walker(boolean climbsLadders, int headroom) {}

  public static final Walker HUMAN = new Walker(true, 2);
  public static final Walker MOB = new Walker(false, RouteRoom.HOUSEHOLD);
  public static final Walker LORD = new Walker(false, RouteRoom.LORD);

  private static final List<String> OPEN =
      List.of(
          ":air",
          "_door",
          "_carpet",
          ":ladder",
          ":vine",
          ":cobweb",
          ":fern",
          ":short_grass",
          ":dead_bush",
          ":large_fern",
          ":candle",
          ":blood_splatter",
          ":blood_pool",
          ":portrait",
          ":wanted_poster");
  private static final int MAX_DROP = 3;
  private static final double STEP_HEIGHT = 0.5;
  private static final int[][] STEPS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

  private StructureWalk() {}

  public static boolean open(final String state) {
    if (state == null) {
      return false;
    }
    final String block = state.replaceAll("\\[.*", "");
    return OPEN.stream().anyMatch(block::endsWith);
  }

  public static boolean stair(final String state) {
    return state != null
        && state.replaceAll("\\[.*", "").endsWith("_stairs")
        && state.contains("half=bottom");
  }

  public static boolean backOn(final String stair, final int[] step) {
    final String toward =
        step[0] == 1 ? "west" : step[0] == -1 ? "east" : step[1] == 1 ? "north" : "south";
    return stair.contains("facing=" + toward);
  }

  public static boolean slab(final String state) {
    return state != null
        && state.replaceAll("\\[.*", "").endsWith("_slab")
        && state.contains("type=bottom");
  }

  public static double surface(final Template t, final Pos feet) {
    return slab(t.get(feet.x(), feet.y() - 1, feet.z())) ? feet.y() - 0.5 : feet.y();
  }

  public static boolean ladder(final String state) {
    return state != null && state.startsWith("minecraft:ladder");
  }

  public static boolean stands(final Template t, final int x, final int y, final int z) {
    return stands(t, HUMAN, x, y, z);
  }

  public static boolean stands(
      final Template t, final Walker walker, final int x, final int y, final int z) {
    if (y < 1 || !clear(t, x, y, z, walker.headroom())) {
      return false;
    }
    final String feet = t.get(x, y, z);
    final String below = t.get(x, y - 1, z);
    if (!walker.climbsLadders()) {
      return !open(below) && !snags(t, x, y, z, walker.headroom());
    }
    return ladder(feet) || !open(below) || ladder(below);
  }

  public static boolean snags(
      final Template t, final int x, final int y, final int z, final int height) {
    for (int up = 0; up < height; up++) {
      final String state = t.get(x, y + up, z);
      if (state != null && state.startsWith("minecraft:cobweb")) {
        return true;
      }
    }
    return false;
  }

  public static boolean clear(
      final Template t, final int x, final int y, final int z, final int height) {
    for (int up = 0; up < height; up++) {
      if (!sky(t, x, y + up, z) && (!t.inside(x, y + up, z) || !open(t.get(x, y + up, z)))) {
        return false;
      }
    }
    return true;
  }

  public static boolean sky(final Template t, final int x, final int y, final int z) {
    return y >= t.sizeY() && t.inside(x, t.sizeY() - 1, z);
  }

  public static Set<Pos> from(final Template t, final Pos start) {
    return from(t, HUMAN, start);
  }

  public static Set<Pos> mob(final Template t, final Pos start) {
    return from(t, MOB, start);
  }

  public static Set<Pos> from(final Template t, final Walker walker, final Pos start) {
    final Set<Pos> seen = new HashSet<>();
    final Deque<Pos> queue = new ArrayDeque<>();
    seen.add(start);
    queue.add(start);
    while (!queue.isEmpty()) {
      for (final Pos to : next(t, walker, queue.poll())) {
        if (seen.add(to)) {
          queue.add(to);
        }
      }
    }
    return seen;
  }

  public static Set<Pos> roomy(final Template t, final Walker walker, final Pos start) {
    final Map<Pos, Integer> squeeze = new HashMap<>();
    final Deque<Pos> queue = new ArrayDeque<>();
    squeeze.put(start, 0);
    queue.add(start);
    while (!queue.isEmpty()) {
      final Pos at = queue.poll();
      final int run = squeeze.get(at);
      for (final Pos to : next(t, walker, at)) {
        final int ahead = sideBySide(t, walker, at, to) ? 0 : run + 1;
        if (ahead <= RouteRoom.LONGEST_SQUEEZE
            && ahead < squeeze.getOrDefault(to, Integer.MAX_VALUE)) {
          squeeze.put(to, ahead);
          queue.add(to);
        }
      }
    }
    return squeeze.keySet();
  }

  public static boolean sideBySide(
      final Template t, final Walker walker, final Pos from, final Pos to) {
    final int dx = to.x() - from.x();
    final int dz = to.z() - from.z();
    for (final int side : new int[] {-1, 1}) {
      final int ex = dz * side;
      final int ez = dx * side;
      if (clear(t, from.x() + ex, from.y() + 1, from.z() + ez, walker.headroom() - 1)
          && clear(t, to.x() + ex, to.y() + 1, to.z() + ez, walker.headroom() - 1)) {
        return true;
      }
    }
    return false;
  }

  private static List<Pos> next(final Template t, final Walker walker, final Pos at) {
    final List<Pos> found = new ArrayList<>();
    for (final int[] step : STEPS) {
      for (int dy = 1; dy >= -MAX_DROP; dy--) {
        final Pos to = at.offset(step[0], dy, step[1]);
        if (dy == 1 && !climbs(t, walker, at, to, step)) {
          continue;
        }
        if (stands(t, walker, to.x(), to.y(), to.z())) {
          found.add(to);
          break;
        }
        if (dy <= 0 && !open(t.get(to.x(), to.y() + 1, to.z()))) {
          break;
        }
      }
    }
    if (walker.climbsLadders() && ladder(t.get(at.x(), at.y(), at.z()))) {
      for (final int dy : new int[] {1, -1}) {
        final Pos to = at.offset(0, dy, 0);
        if (stands(t, walker, to.x(), to.y(), to.z())) {
          found.add(to);
        }
      }
    }
    return found;
  }

  private static boolean climbs(
      final Template t, final Walker walker, final Pos at, final Pos to, final int[] step) {
    if (!open(t.get(at.x(), at.y() + walker.headroom(), at.z()))) {
      return false;
    }
    final String riser = t.get(to.x(), to.y() - 1, to.z());
    if (stair(riser)) {
      return !backOn(riser, step);
    }
    return surface(t, to) - surface(t, at) <= STEP_HEIGHT;
  }

  public static boolean beside(final Set<Pos> reached, final int x, final int y, final int z) {
    for (final int[] step : STEPS) {
      for (int dy = -1; dy <= 1; dy++) {
        if (reached.contains(new Pos(x + step[0], y + dy, z + step[1]))) {
          return true;
        }
      }
    }
    return false;
  }
}
