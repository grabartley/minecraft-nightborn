package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StructureAudit {

  public static final List<String> HOUSEHOLD =
      List.of("_coffin", ":ornate_chair", ":ornate_table", ":lectern");
  public static final List<String> WALL_DECOR =
      List.of(
          ":portrait",
          ":trophy_mount",
          ":weapon_rack",
          "_velvet_drape",
          ":ladder",
          ":gold_wall_crucifix",
          ":wanted_poster");
  public static final List<String> FLOOR_DECOR =
      List.of(
          ":candelabra",
          ":ornate_chair",
          ":ornate_table",
          ":chest",
          ":lectern",
          ":gargoyle",
          ":gramophone",
          ":old_blood_font",
          ":blood_pool",
          ":barrel",
          ":chalice",
          ":candle");
  static final List<String> NOT_A_FLOOR =
      List.of(
          "_fence",
          "_wall",
          "_stairs",
          "_slab",
          "_coffin",
          "_pane",
          "_trapdoor",
          "_bed",
          "ornate_chair",
          "ornate_table",
          ":chest",
          ":lectern",
          "candelabra",
          "chalice",
          "gramophone",
          "old_blood_font",
          "bookshelf",
          ":barrel",
          "crafting_table",
          ":smoker",
          ":furnace",
          "flower_pot",
          "potted_",
          "glass",
          "iron_bars",
          "leaves",
          "azalea",
          "skull",
          "brewing_stand",
          "grindstone",
          ":bell",
          "blood_fountain",
          "lantern",
          "cauldron",
          "lightning_rod",
          ":chain",
          "gargoyle",
          ":hay_block",
          "pointed_dripstone");
  private static final int[][] SIDES = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
  private static final int[][] AROUND = {
    {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
  };

  private StructureAudit() {}

  public static String block(final String state) {
    return state == null ? "" : state.replaceAll("\\[.*", "");
  }

  private static boolean is(final String state, final List<String> blocks) {
    final String block = block(state);
    return blocks.stream().anyMatch(block::endsWith);
  }

  public static List<Pos> unreachedHousehold(final Template t, final Set<Pos> reached) {
    final List<Pos> missed = new ArrayList<>();
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      final Pos at = cell.getKey();
      if (!is(cell.getValue(), HOUSEHOLD) || cell.getValue().contains("part=head")) {
        continue;
      }
      if (!StructureWalk.beside(reached, at.x(), at.y(), at.z()) && !seated(t, reached, at)) {
        missed.add(at);
      }
    }
    return missed;
  }

  private static boolean seated(final Template t, final Set<Pos> reached, final Pos table) {
    if (!block(t.get(table.x(), table.y(), table.z())).endsWith(":ornate_table")) {
      return false;
    }
    for (final int[] side : SIDES) {
      final Pos chair = table.offset(side[0], 0, side[1]);
      if (block(t.get(chair.x(), chair.y(), chair.z())).endsWith(":ornate_chair")
          && StructureWalk.beside(reached, chair.x(), chair.y(), chair.z())) {
        return true;
      }
    }
    return false;
  }

  public static List<Pos> sealedFloors(final Template t, final Set<Pos> reached) {
    final Set<Pos> outside = outside(t);
    final List<Pos> sealed = new ArrayList<>();
    for (int x = 0; x < t.sizeX(); x++) {
      for (int y = 1; y < t.sizeY(); y++) {
        for (int z = 0; z < t.sizeZ(); z++) {
          final Pos at = new Pos(x, y, z);
          if (!reached.contains(at)
              && StructureWalk.stands(t, x, y, z)
              && !is(t.get(x, y - 1, z), NOT_A_FLOOR)
              && !is(t.get(x, y - 1, z), WALL_DECOR)
              && !StructureWalk.beside(reached, x, y - 1, z)
              && sheltered(t, at)
              && !exposed(outside, at)) {
            sealed.add(at);
          }
        }
      }
    }
    return sealed;
  }

  private static boolean sheltered(final Template t, final Pos feet) {
    for (int y = feet.y() + 2; y < t.sizeY(); y++) {
      final String above = t.get(feet.x(), y, feet.z());
      if (above != null && !StructureWalk.open(above)) {
        return true;
      }
    }
    return false;
  }

  private static boolean exposed(final Set<Pos> outside, final Pos feet) {
    for (final int[] side : SIDES) {
      for (int up = 0; up <= 1; up++) {
        if (outside.contains(feet.offset(side[0], up, side[1]))) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean air(final String state) {
    return state == null || (StructureWalk.open(state) && !state.contains("_door"));
  }

  static Set<Pos> outside(final Template t) {
    final Set<Pos> seen = new HashSet<>();
    final Deque<Pos> queue = new ArrayDeque<>();
    for (int x = 0; x < t.sizeX(); x++) {
      for (int y = 0; y < t.sizeY(); y++) {
        for (int z = 0; z < t.sizeZ(); z++) {
          final boolean edge =
              x == 0 || z == 0 || x == t.sizeX() - 1 || z == t.sizeZ() - 1 || y == t.sizeY() - 1;
          final Pos at = new Pos(x, y, z);
          if (edge && air(t.get(x, y, z)) && seen.add(at)) {
            queue.add(at);
          }
        }
      }
    }
    while (!queue.isEmpty()) {
      final Pos at = queue.poll();
      for (final int[] step : AROUND) {
        final Pos next = at.offset(step[0], step[1], step[2]);
        if (t.inside(next.x(), next.y(), next.z())
            && air(t.get(next.x(), next.y(), next.z()))
            && seen.add(next)) {
          queue.add(next);
        }
      }
    }
    return seen;
  }

  public static List<String> loose(final Template t) {
    final List<String> loose = new ArrayList<>();
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      final Pos at = cell.getKey();
      final String state = cell.getValue();
      final String held = holder(t, at, state);
      if (held != null && (held.isEmpty() || StructureWalk.open(held))) {
        loose.add(block(state) + " at " + at + " held by " + (held.isEmpty() ? "nothing" : held));
      }
    }
    return loose;
  }

  private static String holder(final Template t, final Pos at, final String state) {
    final String block = block(state);
    if (is(state, WALL_DECOR) || block.endsWith(":blood_splatter") && state.contains("face=wall")) {
      final Pos step = Facing.STEP.get(state.replaceAll(".*facing=(\\w+).*", "$1"));
      return orEmpty(t.get(at.x() - step.x(), at.y(), at.z() - step.z()));
    }
    if (block.endsWith("lantern") || block.endsWith(":chandelier")) {
      final boolean hanging = !state.contains("hanging=false");
      return orEmpty(t.get(at.x(), at.y() + (hanging ? 1 : -1), at.z()));
    }
    if (is(state, FLOOR_DECOR)
        || block.endsWith("_coffin") && !state.contains("part=head,")
        || block.endsWith(":blood_splatter") && state.contains("face=floor")) {
      return orEmpty(t.get(at.x(), at.y() - 1, at.z()));
    }
    return null;
  }

  private static String orEmpty(final String state) {
    return state == null ? "" : state;
  }

  public static List<Pos> doorsLeadingNowhere(final Template t, final Set<Pos> reached) {
    final List<Pos> nowhere = new ArrayList<>();
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      final Pos door = cell.getKey();
      final String state = cell.getValue();
      if (!block(state).endsWith("_door") || !state.contains("half=lower")) {
        continue;
      }
      final Pos step = Facing.STEP.get(state.replaceAll(".*facing=(\\w+).*", "$1"));
      if (!reached.contains(door)
          || !reachedOrOutside(t, reached, door.offset(step))
          || !reachedOrOutside(t, reached, door.offset(-step.x(), 0, -step.z()))) {
        nowhere.add(door);
      }
    }
    return nowhere;
  }

  private static boolean reachedOrOutside(final Template t, final Set<Pos> reached, final Pos at) {
    return reached.contains(at) || !t.inside(at.x(), at.y(), at.z());
  }

  public static List<Pos> humanOnly(final Set<Pos> human, final Set<Pos> mob, final Template t) {
    return human.stream()
        .filter(at -> !mob.contains(at))
        .filter(at -> StructureWalk.stands(t, StructureWalk.MOB, at.x(), at.y(), at.z()))
        .toList();
  }

  public static List<Pos> standingBeside(
      final Template t, final StructureWalk.Walker walker, final Coffins.Coffin coffin) {
    final List<Pos> found = new ArrayList<>();
    for (final Pos part : List.of(coffin.foot(), Coffins.head(coffin))) {
      for (final int[] step : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
        for (int dy = -1; dy <= 1; dy++) {
          final Pos at = part.offset(step[0], dy, step[1]);
          if (StructureWalk.stands(t, walker, at.x(), at.y(), at.z())) {
            found.add(at);
          }
        }
      }
    }
    return found;
  }
}
