package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.BURGUNDY;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.EMERALD;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_N;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_S;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_W;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.MID_X;

import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayList;
import java.util.List;

public final class ManorWindows {

  public static final int STAIR_SILL = StairHall.LANDING + 2;
  public static final int STAIR_TALL = Manor.TOP - 1 - STAIR_SILL;

  public static final List<Window> WINDOWS = windows();

  private ManorWindows() {}

  public static void build(final Template t) {
    WINDOWS.forEach(window -> window.cut(t));
  }

  private static List<Window> windows() {
    final List<Window> all = new ArrayList<>();
    for (final int x : new int[] {22, 23, 27, 28}) {
      all.add(Window.ground(x, HOUSE_S, "south", BURGUNDY));
      all.add(Window.upper(x, HOUSE_S, "south", BURGUNDY));
      all.add(Window.ground(x, HOUSE_N, "north", EMERALD));
      all.add(Window.upper(x, HOUSE_N, "north", EMERALD));
    }
    for (final int x : new int[] {48, 49, 52, 53}) {
      all.add(Window.ground(x, HOUSE_S, "south", EMERALD));
      all.add(Window.upper(x, HOUSE_S, "south", BURGUNDY));
    }
    for (final int x : new int[] {48, 49, 53, 54}) {
      all.add(Window.upper(x, HOUSE_N, "north", EMERALD));
    }
    for (final int x : new int[] {47, 54, 55}) {
      all.add(Window.ground(x, HOUSE_N, "north", EMERALD));
    }
    for (int x = MID_X - 1; x <= MID_X + 1; x++) {
      all.add(Window.upper(x, HOUSE_S, "south", BURGUNDY).stained());
      all.add(Window.upper(x, HOUSE_N, "north", BURGUNDY).rows(STAIR_SILL, STAIR_TALL).stained());
    }
    for (final int z : new int[] {31, 32, 38, 39}) {
      all.add(Window.ground(HOUSE_W, z, "west", BURGUNDY));
      all.add(Window.upper(HOUSE_W, z, "west", BURGUNDY));
    }
    for (final int z : new int[] {20, 21, 26, 27}) {
      all.add(Window.upper(HOUSE_W, z, "west", EMERALD));
    }
    for (final int z : new int[] {19, 26, 27}) {
      all.add(Window.ground(HOUSE_E, z, "east", EMERALD));
      all.add(Window.upper(HOUSE_E, z, "east", EMERALD));
    }
    for (final int z : new int[] {31, 37}) {
      all.add(Window.ground(HOUSE_E, z, "east", EMERALD));
      all.add(Window.upper(HOUSE_E, z, "east", BURGUNDY));
    }
    return List.copyOf(all);
  }
}
