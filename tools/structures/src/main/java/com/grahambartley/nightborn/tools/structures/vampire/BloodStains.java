package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class BloodStains {

  public static final String SPLATTER = "nightborn:blood_splatter";
  public static final String POOL = "nightborn:blood_pool";
  public static final int SHAPES = 4;
  static final List<String> SIDES = List.of("north", "east", "south", "west");
  static final List<String> KEEP_CLEAR_OF = List.of("door", "stairs", "ladder", "coffin");
  static final List<String> NOT_GROUND =
      List.of(
          "stairs",
          "slab",
          "carpet",
          "fence",
          "wall",
          "pane",
          "door",
          "trapdoor",
          "leaves",
          "glass",
          "bars",
          "coffin",
          "chest",
          "lectern",
          "table",
          "chair",
          "candelabra",
          "barrel",
          "bookshelf",
          "bed",
          "font",
          "fountain",
          "gramophone",
          "chalice",
          "dripstone",
          "jigsaw",
          "farmland",
          "blood",
          "dirt_path",
          "grass_block",
          "moss",
          "podzol",
          "gravel",
          "sand",
          "snow",
          "candle",
          "cobweb",
          "vine",
          "fern",
          "short_grass",
          "bush",
          "skull",
          "lantern",
          "chain",
          "flower",
          "torch",
          "portrait",
          "rack",
          "mount",
          "drape",
          "crucifix",
          "poster",
          "garlic",
          "bell",
          "cauldron",
          "pot",
          "potted");

  private BloodStains() {}

  public static Template scatter(final Template t, final BloodStainRules rules) {
    final Random random = new Random(t.path().hashCode());
    final List<Pos> floors = floors(t);
    final List<Pos> walled = floors.stream().filter(f -> walled(t, f)).toList();
    final List<Pos> centres = new ArrayList<>();
    for (final Pos candidate : shuffled(walled.isEmpty() ? floors : walled, random)) {
      if (centres.size() == rules.clusters()) {
        break;
      }
      if (centres.stream().allMatch(c -> apart(c, candidate) > rules.radius() * 2)) {
        centres.add(candidate);
      }
    }
    for (final Pos centre : centres) {
      final List<Pos> near =
          shuffled(
              floors.stream().filter(f -> apart(f, centre) <= rules.radius()).toList(), random);
      int next = 0;
      for (int i = 0; i < rules.pools() && next < near.size(); i++) {
        place(t, near.get(next++), POOL);
      }
      for (int i = 0; i < rules.floorSplatters() && next < near.size(); i++) {
        place(t, near.get(next++), floorSplatter(random));
      }
      int walls = 0;
      for (final Pos at : shuffled(near, random)) {
        if (walls == rules.wallSplatters()) {
          break;
        }
        if (wallSplatter(t, at, rules.wallMaxHeight(), random)) {
          walls++;
        }
      }
    }
    return t;
  }

  private static List<Pos> shuffled(final List<Pos> cells, final Random random) {
    final List<Pos> copy = new ArrayList<>(cells);
    Collections.shuffle(copy, random);
    return copy;
  }

  private static boolean walled(final Template t, final Pos floor) {
    return SIDES.stream()
        .map(Facing.STEP::get)
        .anyMatch(step -> ground(t.get(floor.x() + step.x(), floor.y(), floor.z() + step.z())));
  }

  public static List<Pos> floors(final Template t) {
    final List<Pos> floors = new ArrayList<>();
    for (int x = 1; x < t.sizeX() - 1; x++) {
      for (int y = 1; y < t.sizeY() - 2; y++) {
        for (int z = 1; z < t.sizeZ() - 1; z++) {
          if (floor(t, x, y, z)) {
            floors.add(new Pos(x, y, z));
          }
        }
      }
    }
    return floors;
  }

  static boolean floor(final Template t, final int x, final int y, final int z) {
    return BlockState.AIR.equals(t.get(x, y, z))
        && BlockState.AIR.equals(t.get(x, y + 1, z))
        && ground(t.get(x, y - 1, z))
        && roofed(t, x, y, z)
        && !nearAny(t, x, y, z)
        && !passage(t, x, y, z);
  }

  static boolean ground(final String state) {
    if (state == null || BlockState.AIR.equals(state)) {
      return false;
    }
    return NOT_GROUND.stream().noneMatch(word -> named(state, word));
  }

  static boolean named(final String state, final String word) {
    final String path = state.replaceAll("\\[.*", "").replaceAll(".*:", "");
    return ("_" + path + "_").contains("_" + word + "_");
  }

  private static boolean roofed(final Template t, final int x, final int y, final int z) {
    for (int above = y + 2; above < t.sizeY(); above++) {
      if (ground(t.get(x, above, z))) {
        return true;
      }
    }
    return false;
  }

  private static boolean nearAny(final Template t, final int x, final int y, final int z) {
    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        for (int dy = -1; dy <= 1; dy++) {
          final String there = t.get(x + dx, y + dy, z + dz);
          if (there != null && KEEP_CLEAR_OF.stream().anyMatch(word -> named(there, word))) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean passage(final Template t, final int x, final int y, final int z) {
    final boolean walledX = solid(t.get(x - 1, y, z)) && solid(t.get(x + 1, y, z));
    final boolean walledZ = solid(t.get(x, y, z - 1)) && solid(t.get(x, y, z + 1));
    return walledX || walledZ;
  }

  private static boolean solid(final String state) {
    return state != null && !BlockState.AIR.equals(state);
  }

  private static int apart(final Pos one, final Pos other) {
    return Math.max(
        Math.abs(one.x() - other.x()),
        Math.max(Math.abs(one.y() - other.y()), Math.abs(one.z() - other.z())));
  }

  private static void place(final Template t, final Pos at, final String state) {
    if (BlockState.AIR.equals(t.get(at.x(), at.y(), at.z()))) {
      t.set(at.x(), at.y(), at.z(), state);
    }
  }

  private static String floorSplatter(final Random random) {
    return BlockState.of(
        SPLATTER,
        "face",
        "floor",
        "facing",
        SIDES.get(random.nextInt(SIDES.size())),
        "splat",
        random.nextInt(SHAPES));
  }

  private static boolean wallSplatter(
      final Template t, final Pos floor, final int maxHeight, final Random random) {
    final int first = random.nextInt(maxHeight + 1);
    for (int tried = 0; tried <= maxHeight; tried++) {
      final int up = (first + tried) % (maxHeight + 1);
      final Pos at = floor.offset(0, up, 0);
      if (!BlockState.AIR.equals(t.get(at.x(), at.y(), at.z()))) {
        continue;
      }
      for (final String side : SIDES) {
        final Pos wall = at.offset(Facing.STEP.get(side));
        if (ground(t.get(wall.x(), wall.y(), wall.z()))) {
          t.set(
              at.x(),
              at.y(),
              at.z(),
              BlockState.of(
                  SPLATTER,
                  "face",
                  "wall",
                  "facing",
                  Facing.OPPOSITE.get(side),
                  "splat",
                  random.nextInt(SHAPES)));
          return true;
        }
      }
    }
    return false;
  }
}
