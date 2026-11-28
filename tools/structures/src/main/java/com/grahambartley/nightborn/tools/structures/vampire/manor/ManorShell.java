package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.CROSS;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HALL_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.ROOM_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.TOP;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import java.util.List;

public final class ManorShell {

  public record Box(int x0, int z0, int x1, int z1) {

    public boolean inside(final int x, final int z) {
      return x0 < x && x < x1 && z0 < z && z < z1;
    }

    public boolean onWall(final int x, final int z) {
      return x0 <= x && x <= x1 && z0 <= z && z <= z1 && !inside(x, z);
    }

    public boolean covers(final int x, final int z) {
      return x0 <= x && x <= x1 && z0 <= z && z <= z1;
    }
  }

  public static final Box HOUSE = new Box(HOUSE_W, HOUSE_N, HOUSE_E, HOUSE_S);
  public static final Box LINING = new Box(HOUSE_W + 1, HOUSE_N + 1, HOUSE_E - 1, HOUSE_S - 1);

  public static final Box LIBRARY = new Box(ROOM_W, ROOM_N, HALL_W - 1, CROSS - 1);
  public static final Box DRAWING_ROOM = new Box(ROOM_W, CROSS + 1, HALL_W - 1, ROOM_S);
  public static final Box KITCHEN = new Box(HALL_E + 1, ROOM_N, ROOM_E, CROSS - 1);
  public static final Box DINING_ROOM = new Box(HALL_E + 1, CROSS + 1, ROOM_E, ROOM_S);
  public static final Box HALL = new Box(HALL_W + 1, ROOM_N, HALL_E - 1, ROOM_S);
  public static final List<Box> SIDE_ROOMS = List.of(LIBRARY, DRAWING_ROOM, KITCHEN, DINING_ROOM);

  public static final int GROUND = F0 + 1;
  public static final int UPPER = F1 + 1;
  public static final int BACK_DOORS = 24;
  public static final int FRONT_DOORS = 34;
  public static final int KITCHEN_DOOR = 26;
  public static final int WEST_THROUGH = 24;
  public static final int EAST_THROUGH = 50;
  public static final int GLASSHOUSE_DOOR = 22;

  public static final List<Doorway> DOORWAYS =
      List.of(
          Doorway.doors(Axis.X, MID_X - 1, MID_X + 1, HOUSE_S, HOUSE_S - 1, GROUND, "north"),
          Doorway.arch(Axis.Z, BACK_DOORS, BACK_DOORS + 2, HALL_W, GROUND, Doorway.EBONY_HEAD),
          Doorway.arch(
              Axis.Z, FRONT_DOORS - 1, FRONT_DOORS + 1, HALL_W, GROUND, Doorway.EBONY_HEAD),
          Doorway.arch(
              Axis.Z, FRONT_DOORS - 1, FRONT_DOORS + 1, HALL_E, GROUND, Doorway.EBONY_HEAD),
          Doorway.doors(Axis.Z, KITCHEN_DOOR, KITCHEN_DOOR + 1, HALL_E, HALL_E, GROUND, "east"),
          Doorway.doors(Axis.X, WEST_THROUGH, WEST_THROUGH + 1, CROSS, CROSS, GROUND, "south"),
          Doorway.doors(Axis.X, EAST_THROUGH, EAST_THROUGH + 1, CROSS, CROSS, GROUND, "south"),
          Doorway.doors(
              Axis.X, EAST_THROUGH, EAST_THROUGH + 1, HOUSE_N, HOUSE_N + 1, GROUND, "north"),
          Doorway.doors(
              Axis.Z, GLASSHOUSE_DOOR, GLASSHOUSE_DOOR + 1, HOUSE_W, HOUSE_W + 1, GROUND, "west"),
          Doorway.doors(Axis.Z, BACK_DOORS, BACK_DOORS + 1, HALL_W, HALL_W, UPPER, "west"),
          Doorway.doors(Axis.Z, FRONT_DOORS, FRONT_DOORS + 1, HALL_W, HALL_W, UPPER, "west"),
          Doorway.doors(Axis.Z, BACK_DOORS, BACK_DOORS + 1, HALL_E, HALL_E, UPPER, "east"),
          Doorway.doors(Axis.Z, FRONT_DOORS, FRONT_DOORS + 1, HALL_E, HALL_E, UPPER, "east"),
          Doorway.doors(Axis.X, WEST_THROUGH, WEST_THROUGH + 1, CROSS, CROSS, UPPER, "south"),
          Doorway.doors(Axis.X, EAST_THROUGH, EAST_THROUGH + 1, CROSS, CROSS, UPPER, "south"));

  private ManorShell() {}

  public static void build(final Template t) {
    for (int x = HOUSE_W; x <= HOUSE_E; x++) {
      for (int z = HOUSE_N; z <= HOUSE_S; z++) {
        column(t, x, z);
      }
    }
    partitions(t);
    quoin(t, HOUSE_W, HOUSE_N, 1, 1);
    quoin(t, HOUSE_W, HOUSE_S, 1, -1);
    quoin(t, HOUSE_E, HOUSE_N, -1, 1);
    SIDE_ROOMS.forEach(room -> beams(t, room));
    DOORWAYS.forEach(doorway -> doorway.cut(t));
  }

  public static boolean floorRow(final int y) {
    return y == F0 || y == F1 || y == TOP;
  }

  private static void column(final Template t, final int x, final int z) {
    final boolean outer = HOUSE.onWall(x, z);
    final boolean lining = LINING.onWall(x, z);
    for (int y = 0; y < F0; y++) {
      t.set(x, y, z, outer ? Manor.PLINTH.at(x, y, z) : Manor.RUBBLE);
    }
    for (int y = F0; y <= TOP; y++) {
      if (outer) {
        t.set(x, y, z, floorRow(y) ? Manor.DRESSING.at(x, y, z) : Manor.WALL.at(x, y, z));
      } else if (lining) {
        t.set(x, y, z, floorRow(y) ? Manor.WALL.at(x, y, z) : finish(x, y, z));
      } else {
        t.set(x, y, z, floorRow(y) ? Manor.PLANKS : BlockState.AIR);
      }
    }
  }

  public static String finish(final int x, final int y, final int z) {
    final int above = y > F1 ? y - F1 : y - F0;
    if (above <= 2) {
      return Manor.PLANKS;
    }
    if (above == 3) {
      return Parts.log(Manor.BEAM, "y");
    }
    return Manor.LIMEWASH.at(x, y, z);
  }

  private static void partitions(final Template t) {
    for (int y = F0 + 1; y < TOP; y++) {
      for (int z = ROOM_N; z <= ROOM_S; z++) {
        t.set(HALL_W, y, z, partition(HALL_W, y, z));
        t.set(HALL_E, y, z, partition(HALL_E, y, z));
      }
      for (int x = ROOM_W; x < HALL_W; x++) {
        t.set(x, y, CROSS, partition(x, y, CROSS));
      }
      for (int x = HALL_E + 1; x <= ROOM_E; x++) {
        t.set(x, y, CROSS, partition(x, y, CROSS));
      }
    }
  }

  private static String partition(final int x, final int y, final int z) {
    return y == F1 ? Manor.PLANKS : finish(x, y, z);
  }

  private static void quoin(
      final Template t, final int x, final int z, final int dx, final int dz) {
    for (int y = F0 + 1; y < TOP; y++) {
      if (floorRow(y)) {
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

  public static List<Integer> beamLines(final Box room) {
    return List.of(room.z0() + 3, room.z1() - 3);
  }

  private static void beams(final Template t, final Box room) {
    for (final int z : beamLines(room)) {
      for (int x = room.x0(); x <= room.x1(); x++) {
        t.set(x, F1 - 1, z, Parts.log(Manor.BEAM, "x"));
      }
    }
  }
}
