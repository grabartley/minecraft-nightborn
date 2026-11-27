package com.grahambartley.nightborn.tools.structures.vampire.lair;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Furnishing;
import java.util.ArrayList;
import java.util.List;

public final class LairPassage {

  public static final String ENTRANCE = "nightborn:lair_entrance";
  static final int HEADROOM = 2;

  private LairPassage() {}

  public static List<Pos> run(final Template t, final int along, final String outward) {
    final Pos in = Facing.STEP.get(Facing.OPPOSITE.get(outward));
    Pos at = mouth(t, along, outward);
    final List<Pos> cells = new ArrayList<>();
    while (t.inside(at.x(), 1, at.z()) && LairCave.roof(t, at.x(), at.z()) <= HEADROOM) {
      cells.add(at);
      at = at.offset(in);
    }
    return cells;
  }

  public static void cut(final Template t, final int along, final String outward) {
    final List<Pos> cells = run(t, along, outward);
    for (final Pos cell : cells) {
      t.set(cell.x(), 0, cell.z(), LairCave.ROCK.at(cell.x(), 0, cell.z()));
      for (int y = 1; y <= HEADROOM; y++) {
        t.set(cell.x(), y, cell.z(), BlockState.AIR);
      }
    }
    final Pos mouth = cells.getFirst();
    t.set(mouth.x(), HEADROOM, mouth.z(), "cobweb");
    final Pos inner = cells.getLast();
    t.set(inner.x(), 1, inner.z(), Furnishing.splatter(Facing.OPPOSITE.get(outward), 2));
    t.entrance(mouth.x(), 0, mouth.z(), ENTRANCE, outward);
  }

  private static Pos mouth(final Template t, final int along, final String outward) {
    return switch (outward) {
      case "north" -> new Pos(along, 0, 0);
      case "south" -> new Pos(along, 0, t.sizeZ() - 1);
      case "west" -> new Pos(0, 0, along);
      case "east" -> new Pos(t.sizeX() - 1, 0, along);
      default -> throw new IllegalArgumentException("A lair passage runs flat, not " + outward);
    };
  }
}
