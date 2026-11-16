package com.grahambartley.nightborn.tools.structures.vampire.castle;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;
import java.util.List;

public final class Castle {

  public record Layout(String path, boolean roundTowers, boolean ruinedWall, boolean fountain) {}

  public static final Layout BASTIONS = new Layout("vampire/castle/bastions", true, false, true);
  public static final Layout RUINED = new Layout("vampire/castle/ruined", false, true, false);
  public static final List<Layout> LAYOUTS = List.of(BASTIONS, RUINED);

  public static final int SIZE = 41;
  public static final int HEIGHT = 40;
  public static final int MID = SIZE / 2;
  public static final int GROUND = 1;
  public static final int FLOOR = 2;
  public static final int WALL_TOP = 10;
  public static final int TOWER_HEIGHT = 17;

  static final Stone WALL =
      new Stone(
          "nightborn:gothic_stone_bricks",
          "nightborn:mossy_gothic_stone_bricks",
          "nightborn:cracked_gothic_stone_bricks",
          22);
  static final Stone PLINTH =
      new Stone(
          "polished_blackstone_bricks",
          "polished_blackstone_bricks",
          "cracked_polished_blackstone_bricks",
          16);
  static final Roof.Cover ROOF =
      new Roof.Cover("deepslate_tile_stairs", "deepslate_tiles", "deepslate_tile_slab");
  static final String SHINGLE = BlockState.of("deepslate_tile_slab", "type", "bottom");
  static final String PLANKS = "dark_oak_planks";
  static final String GLASS = "nightborn:gothic_stained_glass_pane";
  static final String STEP = "nightborn:gothic_stone_brick_stairs";
  public static final String DARK_OAK_COFFIN = "nightborn:dark_oak_coffin";
  public static final String GOTHIC_COFFIN = "nightborn:gothic_stone_brick_coffin";
  public static final String LOOT = "nightborn:chests/castle";

  private Castle() {}

  public static Template build(final Layout layout) {
    final Template t = new Template(layout.path(), SIZE, HEIGHT, SIZE);
    t.fill(2, FLOOR, 2, SIZE - 3, HEIGHT - 1, SIZE - 3, BlockState.AIR);
    Courtyard.build(t, layout);
    CurtainWall.build(t, layout);
    for (final Tower tower : towers(layout)) {
      tower.build(t, WALL, WALL_TOP);
      tower.door(t, tower.cx() + Integer.signum(MID - tower.cx()) * tower.radius(), tower.cz());
      CurtainWall.walkwayDoors(t, tower);
    }
    Gatehouse.build(t);
    Keep.build(t);
    return t;
  }

  public static List<Tower> towers(final Layout layout) {
    final int far = SIZE - 5;
    return List.of(
        new Tower(4, 4, 4, TOWER_HEIGHT, layout.roundTowers()),
        new Tower(far, 4, 4, TOWER_HEIGHT, layout.roundTowers()),
        new Tower(4, far, 4, TOWER_HEIGHT, layout.roundTowers()),
        new Tower(far, far, 4, TOWER_HEIGHT, layout.roundTowers()));
  }
}
