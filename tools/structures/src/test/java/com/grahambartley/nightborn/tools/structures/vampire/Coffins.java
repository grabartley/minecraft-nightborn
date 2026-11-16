package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class Coffins {

  public record Coffin(Pos foot, String block, String facing, boolean standing) {}

  private Coffins() {}

  public static List<Coffin> in(final Template t) {
    final List<Coffin> found = new ArrayList<>();
    for (final Map.Entry<Pos, String> cell : t.cells().entrySet()) {
      final BlockState.Parsed parsed = BlockState.parse(cell.getValue());
      if (parsed.block().endsWith("_coffin") && "foot".equals(parsed.properties().get("part"))) {
        found.add(
            new Coffin(
                cell.getKey(),
                parsed.block(),
                parsed.properties().get("facing"),
                "true".equals(parsed.properties().get("standing"))));
      }
    }
    return found;
  }

  public static Pos head(final Coffin coffin) {
    return coffin.standing()
        ? coffin.foot().offset(0, 1, 0)
        : coffin.foot().offset(Facing.STEP.get(coffin.facing()));
  }

  public static List<Pos> lidSides(final Coffin coffin) {
    final Pos side = Facing.STEP.get(Facing.CLOCKWISE.get(coffin.facing()));
    final List<Pos> sides = new ArrayList<>();
    for (final Pos part : List.of(coffin.foot(), head(coffin))) {
      sides.add(part.offset(side));
      sides.add(part.offset(-side.x(), 0, -side.z()));
    }
    return sides;
  }

  public static boolean headMatches(final Template t, final Coffin coffin) {
    final Pos head = head(coffin);
    final String state = t.get(head.x(), head.y(), head.z());
    if (state == null) {
      return false;
    }
    final BlockState.Parsed parsed = BlockState.parse(state);
    return parsed.block().equals(coffin.block())
        && "head".equals(parsed.properties().get("part"))
        && coffin.facing().equals(parsed.properties().get("facing"));
  }

  public static boolean open(final Template t, final Pos pos) {
    return BlockState.AIR.equals(t.get(pos.x(), pos.y(), pos.z()));
  }

  public static void assertSound(final Template t, final List<String> materials) {
    for (final Coffin coffin : in(t)) {
      org.junit.jupiter.api.Assertions.assertTrue(
          materials.contains(coffin.block()), coffin.toString());
      org.junit.jupiter.api.Assertions.assertTrue(headMatches(t, coffin), coffin.toString());
      for (final Pos side : lidSides(coffin)) {
        org.junit.jupiter.api.Assertions.assertTrue(
            open(t, side),
            coffin + " lid side " + side + " is " + t.get(side.x(), side.y(), side.z()));
      }
    }
  }

  public static void assertHung(final Template t) {
    t.cells()
        .forEach(
            (pos, state) -> {
              if (state.startsWith("nightborn:chandelier")) {
                final String above = t.get(pos.x(), pos.y() + 1, pos.z());
                org.junit.jupiter.api.Assertions.assertTrue(
                    above != null && !above.equals(BlockState.AIR), "chandelier at " + pos);
              }
            });
  }
}
