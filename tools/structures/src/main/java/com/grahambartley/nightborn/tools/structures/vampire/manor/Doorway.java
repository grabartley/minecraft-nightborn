package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Axis;
import com.grahambartley.nightborn.tools.structures.vampire.RouteRoom;

public record Doorway(
    Axis along, int from, int to, int line, int lining, int floor, String facing, String head) {

  public static final String EBONY_HEAD = "nightborn:ebony_stairs";

  public static Doorway doors(
      final Axis along,
      final int from,
      final int to,
      final int line,
      final int lining,
      final int floor,
      final String facing) {
    return new Doorway(along, from, to, line, lining, floor, facing, EBONY_HEAD);
  }

  public static Doorway arch(
      final Axis along, final int from, final int to, final int line, final int floor) {
    return new Doorway(along, from, to, line, line, floor, null, EBONY_HEAD);
  }

  public int width() {
    return to - from + 1;
  }

  public int height() {
    return width() >= 3 ? RouteRoom.HOUSEHOLD + 1 : RouteRoom.HOUSEHOLD;
  }

  public boolean hasLeaves() {
    return facing != null;
  }

  public String hinge(final int a) {
    final Pos right = Facing.STEP.get(Facing.CLOCKWISE.get(facing));
    final int toward = along == Axis.X ? right.x() : right.z();
    final boolean rightEnd = toward > 0 ? a == to : a == from;
    return rightEnd ? "right" : "left";
  }

  public void cut(final Template t) {
    final int top = floor + height() - 1;
    for (int a = from; a <= to; a++) {
      for (int c = Math.min(line, lining); c <= Math.max(line, lining); c++) {
        for (int y = floor; y <= top; y++) {
          t.set(along.x(a, c), y, along.z(a, c), BlockState.AIR);
        }
      }
    }
    for (int c = Math.min(line, lining); c <= Math.max(line, lining) && width() >= 3; c++) {
      shoulder(t, from, c, top, along.low());
      shoulder(t, to, c, top, along.high());
    }
    if (hasLeaves()) {
      leaf(t, from);
      if (to != from) {
        leaf(t, to);
      }
    }
  }

  private void shoulder(
      final Template t, final int a, final int c, final int y, final String side) {
    t.set(along.x(a, c), y, along.z(a, c), Parts.hood(head, side));
  }

  private void leaf(final Template t, final int a) {
    final int x = along.x(a, line);
    final int z = along.z(a, line);
    t.set(x, floor, z, Parts.door(Manor.DOOR, facing, "lower", hinge(a)));
    t.set(x, floor + 1, z, Parts.door(Manor.DOOR, facing, "upper", hinge(a)));
  }
}
