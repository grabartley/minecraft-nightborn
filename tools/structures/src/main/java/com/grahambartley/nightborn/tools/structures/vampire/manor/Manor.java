package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;
import java.util.List;

public final class Manor {

  public record Layout(String path, boolean roundTurret, boolean fallenGlasshouse) {}

  public static final Layout TURRET = new Layout("vampire/manor/turret", true, false);
  public static final Layout TOWER = new Layout("vampire/manor/tower", false, true);
  public static final List<Layout> LAYOUTS = List.of(TURRET, TOWER);

  public static final String ENTRANCE = "nightborn:manor_entrance";

  public static final int SIZE_X = 48;
  public static final int SIZE_Y = 34;
  public static final int SIZE_Z = 44;

  public static final int F0 = 3;
  public static final int F1 = 9;
  public static final int TOP = 14;

  public static final int WING_W = 12;
  public static final int WING_E = 22;
  public static final int WING_SPLIT = 17;
  public static final int MAIN_N = 8;
  public static final int MAIN_S = 22;
  public static final int MAIN_E = 40;
  public static final int WING_S = 27;
  public static final int HALL_E = 32;
  public static final int EAST_SPLIT = 15;
  public static final int MID_X = 27;

  public static final Stone BRICK = new Stone("bricks", "bricks", "bricks", 0);
  public static final Stone DRESSING =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 24);
  public static final String TRIM = "stone_brick_stairs";
  public static final String TRIM_SLAB = "stone_brick_slab";
  public static final String TRIM_WALL = "stone_brick_wall";
  public static final Roof.Cover SLATE =
      new Roof.Cover("deepslate_tile_stairs", "deepslate_tiles", "deepslate_tile_slab");
  public static final String STAINED = "nightborn:gothic_stained_glass_pane";
  public static final String PANE = "glass_pane";
  public static final String PLANKS = "dark_oak_planks";
  public static final String DOOR = "dark_oak_door";
  public static final String DARK_OAK_COFFIN = "nightborn:dark_oak_coffin";
  public static final String SPRUCE_COFFIN = "nightborn:spruce_coffin";
  public static final String BURGUNDY = "nightborn:burgundy_velvet_drape";
  public static final String EMERALD = "nightborn:emerald_velvet_drape";
  public static final String GARGOYLE = "nightborn:gargoyle";

  private Manor() {}

  public static Template build(final Layout layout) {
    final Template t = new Template(layout.path(), SIZE_X, SIZE_Y, SIZE_Z);
    ManorShell.build(t);
    ManorFront.build(t);
    ManorRoofs.build(t);
    Chimney.ALL.forEach(chimney -> chimney.build(t));
    final Turret turret = new Turret(layout.roundTurret());
    turret.build(t);
    StairHall.build(t);
    GroundFloor.build(t);
    Bedchambers.build(t, turret);
    Glasshouse.build(t, layout.fallenGlasshouse());
    Ivy.grow(t);
    Grounds.build(t);
    t.entrance(MID_X, 0, SIZE_Z - 1, ENTRANCE, "south");
    return t;
  }
}
