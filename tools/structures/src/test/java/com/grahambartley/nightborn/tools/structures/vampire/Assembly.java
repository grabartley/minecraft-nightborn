package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.Map;
import java.util.function.Predicate;

public final class Assembly {

  private Assembly() {}

  public static Template stacked(final Template lower, final Template upper, final String joint) {
    final Pos below = jigsaw(lower, data -> joint.equals(data.get("target")));
    final Pos above = jigsaw(upper, data -> joint.equals(data.get("name")));
    final int ux = below.x() - above.x();
    final int uy = below.y() + 1 - above.y();
    final int uz = below.z() - above.z();
    final int lx = Math.max(0, -ux);
    final int lz = Math.max(0, -uz);
    final Template whole =
        new Template(
            lower.path() + " under " + upper.path(),
            Math.max(lower.sizeX() + lx, upper.sizeX() + ux + lx),
            Math.max(lower.sizeY(), upper.sizeY() + uy),
            Math.max(lower.sizeZ() + lz, upper.sizeZ() + uz + lz));
    whole.embed(settled(lower, joint), lx, 0, lz);
    whole.embed(settled(upper, joint), ux + lx, uy, uz + lz);
    return whole;
  }

  public static Pos wayOut(final Template home) {
    return jigsaw(home, data -> String.valueOf(data.get("name")).endsWith("_entrance"))
        .offset(0, 1, 0);
  }

  private static Template settled(final Template t, final String joint) {
    final Template copy = new Template(t.path(), t.sizeX(), t.sizeY(), t.sizeZ());
    copy.embed(t, 0, 0, 0);
    t.blockData()
        .forEach(
            (pos, data) -> {
              if (joint.equals(data.get("name")) || joint.equals(data.get("target"))) {
                copy.set(pos.x(), pos.y(), pos.z(), String.valueOf(data.get("final_state")));
              }
            });
    return copy;
  }

  private static Pos jigsaw(final Template t, final Predicate<Map<String, Object>> which) {
    return t.blockData().entrySet().stream()
        .filter(data -> which.test(data.getValue()))
        .map(Map.Entry::getKey)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(t.path() + " has no such jigsaw"));
  }
}
