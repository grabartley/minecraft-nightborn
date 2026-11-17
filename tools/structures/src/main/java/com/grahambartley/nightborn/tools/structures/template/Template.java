package com.grahambartley.nightborn.tools.structures.template;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Template {

  public static final String EMPTY = "minecraft:empty";

  private final String path;
  private final int sizeX;
  private final int sizeY;
  private final int sizeZ;
  private final Map<Pos, String> cells = new LinkedHashMap<>();
  private final Map<Pos, Map<String, Object>> blockData = new LinkedHashMap<>();
  private final List<Map<String, Object>> entities = new ArrayList<>();

  public Template(final String path, final int sizeX, final int sizeY, final int sizeZ) {
    this.path = path;
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.sizeZ = sizeZ;
  }

  public String path() {
    return path;
  }

  public int sizeX() {
    return sizeX;
  }

  public int sizeY() {
    return sizeY;
  }

  public int sizeZ() {
    return sizeZ;
  }

  public Map<Pos, String> cells() {
    return Collections.unmodifiableMap(cells);
  }

  public Map<Pos, Map<String, Object>> blockData() {
    return Collections.unmodifiableMap(blockData);
  }

  public List<Map<String, Object>> entities() {
    return Collections.unmodifiableList(entities);
  }

  void place(final Pos pos, final String state) {
    cells.put(pos, state);
  }

  public boolean inside(final int x, final int y, final int z) {
    return 0 <= x && x < sizeX && 0 <= y && y < sizeY && 0 <= z && z < sizeZ;
  }

  public void set(final int x, final int y, final int z, final String state) {
    set(x, y, z, state, null);
  }

  public void set(
      final int x, final int y, final int z, final String state, final Map<String, Object> data) {
    if (!inside(x, y, z)) {
      throw new IllegalArgumentException(
          path
              + ": "
              + x
              + ","
              + y
              + ","
              + z
              + " outside ("
              + sizeX
              + ", "
              + sizeY
              + ", "
              + sizeZ
              + ")");
    }
    final Pos pos = new Pos(x, y, z);
    cells.put(pos, BlockState.full(state));
    if (data != null && !data.isEmpty()) {
      blockData.put(pos, data);
    } else {
      blockData.remove(pos);
    }
  }

  public String get(final int x, final int y, final int z) {
    return cells.get(new Pos(x, y, z));
  }

  public void fill(
      final int x0,
      final int y0,
      final int z0,
      final int x1,
      final int y1,
      final int z1,
      final String state) {
    for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
      for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
        for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
          set(x, y, z, state);
        }
      }
    }
  }

  public void anchor(final int x, final int y, final int z, final String name, final String below) {
    jigsaw(x, y, z, "up_north", name, EMPTY, EMPTY, below, "rollable");
  }

  public void entrance(
      final int x, final int y, final int z, final String name, final String outward) {
    jigsaw(x, y, z, outward + "_up", name, EMPTY, EMPTY, get(x, y, z), "aligned");
  }

  public void jigsaw(
      final int x,
      final int y,
      final int z,
      final String orientation,
      final String name,
      final String target,
      final String pool,
      final String finalState,
      final String joint) {
    set(
        x,
        y,
        z,
        BlockState.of("jigsaw", "orientation", orientation),
        Nbt.compound(
            "id",
            "minecraft:jigsaw",
            "name",
            name,
            "target",
            target,
            "pool",
            pool,
            "joint",
            joint,
            "final_state",
            BlockState.full(finalState),
            "placement_priority",
            0,
            "selection_priority",
            0));
  }

  public void embed(final Template inner, final int dx, final int dy, final int dz) {
    inner
        .cells()
        .forEach(
            (pos, state) -> {
              final Pos to = pos.offset(dx, dy, dz);
              set(to.x(), to.y(), to.z(), state, inner.blockData().get(pos));
            });
  }

  public void entity(final int x, final int y, final int z, final Map<String, Object> data) {
    entities.add(
        Nbt.compound(
            "pos", NbtList.doubles(x + 0.5, y, z + 0.5),
            "blockPos", NbtList.ints(x, y, z),
            "nbt", data));
  }
}
