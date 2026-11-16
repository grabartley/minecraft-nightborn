package com.grahambartley.nightborn.tools.structures.template;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Facing {

  public static final Map<String, String> OPPOSITE =
      Map.of(
          "north", "south", "south", "north", "east", "west", "west", "east", "up", "down", "down",
          "up");

  public static final Map<String, Pos> STEP = steps();

  public static final Map<String, String> CLOCKWISE =
      Map.of("north", "east", "east", "south", "south", "west", "west", "north");

  private Facing() {}

  private static Map<String, Pos> steps() {
    final Map<String, Pos> steps = new LinkedHashMap<>();
    steps.put("north", new Pos(0, 0, -1));
    steps.put("south", new Pos(0, 0, 1));
    steps.put("east", new Pos(1, 0, 0));
    steps.put("west", new Pos(-1, 0, 0));
    steps.put("up", new Pos(0, 1, 0));
    steps.put("down", new Pos(0, -1, 0));
    return Collections.unmodifiableMap(steps);
  }
}
