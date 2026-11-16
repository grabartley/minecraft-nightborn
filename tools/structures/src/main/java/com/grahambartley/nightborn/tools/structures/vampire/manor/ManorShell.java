package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.EAST_SPLIT;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MAIN_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_SPLIT;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.WING_W;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;

public final class ManorShell {

  public record Box(int x0, int z0, int x1, int z1) {

    public boolean inside(final int x, final int z) {
      return x0 < x && x < x1 && z0 < z && z < z1;
    }

    public boolean onWall(final int x, final int z) {
      return x0 <= x && x <= x1 && z0 <= z && z <= z1 && !inside(x, z);
    }
  }

  public static final Box WING = new Box(WING_W, MAIN_N, WING_E, WING_S);
  public static final Box MAIN = new Box(WING_E, MAIN_N, MAIN_E, MAIN_S);

  public static final List<Window> WINDOWS =
      List.of(
          Window.ground(23, MAIN_S, "south"),
          Window.ground(31, MAIN_S, "south"),
          Window.ground(34, MAIN_S, "south"),
          Window.ground(35, MAIN_S, "south"),
          Window.upper(34, MAIN_S, "south"),
          Window.upper(35, MAIN_S, "south"),
          Window.upper(16, WING_S, "south"),
          Window.upper(18, WING_S, "south"),
          Window.ground(WING_W, 10, "west"),
          Window.ground(WING_W, 15, "west"),
          Window.ground(WING_W, 19, "west"),
          Window.ground(WING_W, 25, "west"),
          Window.upper(WING_W, 10, "west"),
          Window.upper(WING_W, 15, "west"),
          Window.upper(WING_W, 19, "west"),
          Window.upper(WING_W, 25, "west"),
          Window.ground(15, MAIN_N, "north"),
          Window.ground(19, MAIN_N, "north"),
          Window.upper(15, MAIN_N, "north"),
          Window.upper(19, MAIN_N, "north"),
          Window.ground(34, MAIN_N, "north"),
          Window.ground(38, MAIN_N, "north"),
          Window.upper(34, MAIN_N, "north"),
          Window.upper(38, MAIN_N, "north"),
          Window.ground(MAIN_E, 9, "east"),
          Window.upper(MAIN_E, 9, "east"));

  private ManorShell() {}

  public static void build(final Template t) {
    for (final Box box : List.of(WING, MAIN)) {
      raise(t, box);
    }
    for (final Box box : List.of(WING, MAIN)) {
      hollow(t, box);
    }
    partitions(t);
    quoin(t, WING_W, MAIN_N, 1, 1);
    quoin(t, WING_W, WING_S, 1, -1);
    quoin(t, WING_E, WING_S, -1, -1);
    quoin(t, MAIN_E, MAIN_N, -1, 1);
    WINDOWS.forEach(window -> window.cut(t));
    doors(t);
  }

  private static void raise(final Template t, final Box box) {
    for (int x = box.x0(); x <= box.x1(); x++) {
      for (int z = box.z0(); z <= box.z1(); z++) {
        for (int y = 0; y < F0; y++) {
          t.set(x, y, z, Manor.DRESSING.at(x, y, z));
        }
        if (!box.onWall(x, z)) {
          continue;
        }
        for (int y = F0; y <= TOP; y++) {
          final boolean band = y == F0 || y == F1 || y == TOP;
          t.set(x, y, z, band ? Manor.DRESSING.at(x, y, z) : Manor.BRICK.at(x, y, z));
        }
      }
    }
  }

  private static void hollow(final Template t, final Box box) {
    for (int x = box.x0() + 1; x < box.x1(); x++) {
      for (int z = box.z0() + 1; z < box.z1(); z++) {
        t.set(x, F0, z, Manor.PLANKS);
        t.set(x, F1, z, Manor.PLANKS);
        t.set(x, TOP, z, Manor.PLANKS);
        for (int y = F0 + 1; y < TOP; y++) {
          if (y != F1) {
            t.set(x, y, z, BlockState.AIR);
          }
        }
      }
    }
  }

  private static void partitions(final Template t) {
    for (int y = F0 + 1; y < TOP; y++) {
      for (int x = WING_W + 1; x < WING_E; x++) {
        t.set(x, y, WING_SPLIT, panel(y));
      }
      for (int x = HALL_E + 1; x < MAIN_E; x++) {
        t.set(x, y, EAST_SPLIT, panel(y));
      }
      for (int z = MAIN_N + 1; z < MAIN_S; z++) {
        t.set(HALL_E, y, z, panel(y));
      }
    }
  }

  static String panel(final int y) {
    if (y == F1) {
      return Manor.PLANKS;
    }
    final int above = y > F1 ? y - F1 : y - F0;
    return above <= 2 ? Manor.PLANKS : "stripped_dark_oak_wood";
  }

  private static void quoin(
      final Template t, final int x, final int z, final int dx, final int dz) {
    for (int y = F0 + 1; y < TOP; y++) {
      if (y == F1) {
        continue;
      }
      t.set(x, y, z, Manor.DRESSING.at(x, y, z));
      if (y % 2 == 0) {
        t.set(x + dx, y, z, Manor.DRESSING.at(x + dx, y, z));
      } else {
        t.set(x, y, z + dz, Manor.DRESSING.at(x, y, z + dz));
      }
    }
  }

  private static void doors(final Template t) {
    final int ground = F0 + 1;
    final int upper = F1 + 1;
    Window.door(t, Manor.MID_X, ground, MAIN_N, "north");
    Window.door(t, 36, ground, MAIN_N, "north");
    Window.door(t, WING_E, ground, 20, "west");
    Window.door(t, WING_E, ground, 11, "west");
    Window.door(t, HALL_E, ground, 19, "east");
    Window.door(t, HALL_E, ground, 11, "east");
    Window.door(t, 17, ground, WING_SPLIT, "north");
    Window.door(t, 36, ground, EAST_SPLIT, "north");
    Window.door(t, WING_W, ground, 13, "west");
    Window.door(t, WING_E, upper, 19, "west");
    Window.door(t, HALL_E, upper, 18, "east");
    Window.door(t, 17, upper, WING_SPLIT, "north");
    Window.door(t, 36, upper, EAST_SPLIT, "north");
  }
}
