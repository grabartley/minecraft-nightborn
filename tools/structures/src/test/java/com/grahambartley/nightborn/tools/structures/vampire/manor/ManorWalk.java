package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class ManorWalk {

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
          ":candle");
  private static final int MAX_DROP = 3;

  private ManorWalk() {}

  static boolean open(final String state) {
    if (state == null) {
      return false;
    }
    final String block = state.replaceAll("\\[.*", "");
    return OPEN.stream().anyMatch(block::endsWith);
  }

  static boolean stair(final String state) {
    return state != null && state.replaceAll("\\[.*", "").endsWith("_stairs");
  }

  static boolean ladder(final String state) {
    return state != null && state.startsWith("minecraft:ladder");
  }

  static boolean stands(final Template t, final int x, final int y, final int z) {
    if (!t.inside(x, y, z) || !t.inside(x, y + 1, z) || y < 1) {
      return false;
    }
    final String feet = t.get(x, y, z);
    final String below = t.get(x, y - 1, z);
    return open(feet)
        && open(t.get(x, y + 1, z))
        && (ladder(feet) || !open(below) || ladder(below));
  }

  static Set<Pos> from(final Template t, final Pos start) {
    final Set<Pos> seen = new HashSet<>();
    final Deque<Pos> queue = new ArrayDeque<>();
    seen.add(start);
    queue.add(start);
    while (!queue.isEmpty()) {
      final Pos at = queue.poll();
      for (final int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        for (int dy = 1; dy >= -MAX_DROP; dy--) {
          final Pos to = at.offset(step[0], dy, step[1]);
          if (dy == 1
              && (!open(t.get(at.x(), at.y() + 2, at.z()))
                  || !stair(t.get(to.x(), to.y() - 1, to.z())))) {
            continue;
          }
          if (stands(t, to.x(), to.y(), to.z())) {
            if (seen.add(to)) {
              queue.add(to);
            }
            break;
          }
          if (dy <= 0 && !open(t.get(to.x(), to.y() + 1, to.z()))) {
            break;
          }
        }
      }
      if (ladder(t.get(at.x(), at.y(), at.z()))) {
        for (final int dy : new int[] {1, -1}) {
          final Pos to = at.offset(0, dy, 0);
          if (stands(t, to.x(), to.y(), to.z()) && seen.add(to)) {
            queue.add(to);
          }
        }
      }
    }
    return seen;
  }

  static boolean beside(final Set<Pos> reached, final int x, final int y, final int z) {
    for (final int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
      for (int dy = -1; dy <= 1; dy++) {
        if (reached.contains(new Pos(x + step[0], y + dy, z + step[1]))) {
          return true;
        }
      }
    }
    return false;
  }
}
