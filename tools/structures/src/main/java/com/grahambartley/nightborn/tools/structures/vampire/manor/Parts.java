package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;

public final class Parts {

  private Parts() {}

  public static String stair(final String block, final String facing) {
    return stair(block, facing, "bottom");
  }

  public static void flight(
      final Template t,
      final String block,
      final String fill,
      final int x,
      final int z,
      final String up,
      final int top,
      final int count,
      final int base) {
    final Pos step = Facing.STEP.get(up);
    for (int i = 0; i < count; i++) {
      final int sx = x - i * step.x();
      final int sz = z - i * step.z();
      t.set(sx, top - i, sz, stair(block, up));
      for (int y = base; y < top - i; y++) {
        t.set(sx, y, sz, fill);
      }
    }
  }

  public static String fence() {
    return BlockState.of("dark_oak_fence", "waterlogged", false);
  }

  public static String hood(final String block, final String facing) {
    return stair(block, facing, "top");
  }

  public static String stair(final String block, final String facing, final String half) {
    return BlockState.of(
        block, "facing", facing, "half", half, "shape", "straight", "waterlogged", false);
  }

  public static String slab(final String block, final String type) {
    return BlockState.of(block, "type", type, "waterlogged", false);
  }

  public static String door(
      final String block, final String facing, final String half, final String hinge) {
    return BlockState.of(
        block, "facing", facing, "half", half, "hinge", hinge, "open", false, "powered", false);
  }

  public static String log(final String block, final String axis) {
    return BlockState.of(block, "axis", axis);
  }

  public static String leaves(final String block) {
    return BlockState.of(block, "distance", 1, "persistent", true, "waterlogged", false);
  }

  public static String lantern(final boolean hanging) {
    return BlockState.of("lantern", "hanging", hanging, "waterlogged", false);
  }

  public static String vine(final String side) {
    return BlockState.of(
        "vine",
        "east",
        "east".equals(side),
        "north",
        "north".equals(side),
        "south",
        "south".equals(side),
        "up",
        "up".equals(side),
        "west",
        "west".equals(side));
  }

  public static String campfire(final String facing) {
    return BlockState.of(
        "campfire", "facing", facing, "lit", true, "signal_fire", false, "waterlogged", false);
  }
}
