package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F0;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.F1;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_E;
import static com.grahambartley.nightborn.tools.structures.vampire.manor.Manor.HOUSE_W;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import java.util.List;

public record Chimney(int x0, int z0, int x1, int z1, List<Fire> fires) {

  public static final int ABOVE_ROOF = 3;

  public record Fire(int x, int z, int floor, String into) {

    public Pos firebox() {
      final Pos back = Facing.STEP.get(Facing.OPPOSITE.get(into));
      return new Pos(x + back.x(), floor + 1, z + back.z());
    }

    public Pos box() {
      final Pos back = Facing.STEP.get(Facing.OPPOSITE.get(into));
      return new Pos(x + 2 * back.x(), floor + 1, z + 2 * back.z());
    }

    public Pos hearth() {
      final Pos step = Facing.STEP.get(into);
      return new Pos(x + step.x(), floor, z + step.z());
    }
  }

  public static final Chimney WEST =
      new Chimney(
          HOUSE_W - 2,
          33,
          HOUSE_W - 1,
          36,
          List.of(new Fire(HOUSE_W + 1, 34, F0, "east"), new Fire(HOUSE_W + 1, 35, F1, "east")));
  public static final Chimney KITCHEN =
      new Chimney(
          HOUSE_E + 1,
          20,
          HOUSE_E + 2,
          23,
          List.of(new Fire(HOUSE_E - 1, 21, F0, "west"), new Fire(HOUSE_E - 1, 22, F1, "west")));
  public static final Chimney DINING =
      new Chimney(
          HOUSE_E + 1,
          32,
          HOUSE_E + 2,
          35,
          List.of(new Fire(HOUSE_E - 1, 33, F0, "west"), new Fire(HOUSE_E - 1, 34, F1, "west")));
  public static final List<Chimney> ALL = List.of(WEST, KITCHEN, DINING);

  public int top() {
    int highest = 0;
    for (int z = z0; z <= z1; z++) {
      highest = Math.max(highest, ManorRoofs.rise(z));
    }
    return Manor.TOP + highest + ABOVE_ROOF;
  }

  public boolean shaft(final int x, final int z) {
    final int low = fires.stream().mapToInt(fire -> fire.box().z()).min().orElse(z0);
    final int high = fires.stream().mapToInt(fire -> fire.box().z()).max().orElse(z1);
    return x0 <= x && x <= x1 && low <= z && z <= high;
  }

  public void build(final Template t) {
    final int top = top();
    for (int x = x0; x <= x1; x++) {
      for (int z = z0; z <= z1; z++) {
        final int height = shaft(x, z) ? top : Manor.TOP - 1;
        for (int y = 0; y <= height; y++) {
          final boolean band = y < F0 || y == top;
          t.set(x, y, z, band ? Manor.DRESSING.at(x, y, z) : Manor.WALL.at(x, y, z));
        }
        t.set(
            x,
            height + 1,
            z,
            shaft(x, z)
                ? Parts.slab(Manor.TRIM_SLAB, "bottom")
                : Parts.stair(Manor.TRIM, z < (z0 + z1) / 2.0 ? "south" : "north"));
      }
    }
    fires.forEach(fire -> light(t, fire, top));
  }

  private static void light(final Template t, final Fire fire, final int top) {
    final Pos box = fire.box();
    t.set(box.x(), box.y(), box.z(), Parts.campfire(fire.into()));
    for (int y = box.y() + 1; y <= top + 1; y++) {
      t.set(box.x(), y, box.z(), BlockState.AIR);
    }
    final Pos firebox = fire.firebox();
    for (int y = firebox.y(); y <= firebox.y() + 1; y++) {
      t.set(firebox.x(), y, firebox.z(), BlockState.AIR);
    }
    final Pos along = Facing.STEP.get(Facing.CLOCKWISE.get(fire.into()));
    for (int y = fire.floor() + 1; y <= fire.floor() + 3; y++) {
      for (int side = -1; side <= 1; side++) {
        final int x = fire.x() + along.x() * side;
        final int z = fire.z() + along.z() * side;
        final boolean opening = side == 0 && y <= fire.floor() + 2;
        t.set(x, y, z, opening ? BlockState.AIR : "polished_blackstone_bricks");
      }
    }
    final Pos hearth = fire.hearth();
    t.set(hearth.x(), hearth.y(), hearth.z(), "polished_blackstone");
    final int mantel = fire.floor() + 3;
    t.set(
        hearth.x(),
        mantel,
        hearth.z(),
        Parts.hood("polished_blackstone_brick_stairs", Facing.OPPOSITE.get(fire.into())));
    Furnishing.candles(t, hearth.x(), mantel + 1, hearth.z(), 3, false);
  }
}
