package com.grahambartley.nightborn.tools.structures.vampire.manor;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;
import java.util.List;

public final class Manor {

  public record Layout(String path, boolean roundTurret, boolean ruined) {}

  public static final Layout TURRET = new Layout("vampire/manor/turret", true, false);
  public static final Layout TOWER = new Layout("vampire/manor/tower", false, true);
  public static final List<Layout> LAYOUTS = List.of(TURRET, TOWER);

  public static final String ENTRANCE = "nightborn:manor_entrance";

  public static final int SIZE_X = 68;
  public static final int SIZE_Y = 48;
  public static final int SIZE_Z = 64;

  public static final int F0 = 5;
  public static final int F1 = 12;
  public static final int TOP = 18;

  public static final int HOUSE_W = 18;
  public static final int HOUSE_E = 58;
  public static final int HOUSE_N = 16;
  public static final int HOUSE_S = 42;
  public static final int ROOM_W = HOUSE_W + 2;
  public static final int ROOM_E = HOUSE_E - 2;
  public static final int ROOM_N = HOUSE_N + 2;
  public static final int ROOM_S = HOUSE_S - 2;
  public static final int MID_X = 38;
  public static final int HALL_W = MID_X - 7;
  public static final int HALL_E = MID_X + 7;
  public static final int CROSS = 29;

  public static final Stone WALL =
      new Stone(
          "nightborn:gothic_stone_bricks",
          "nightborn:mossy_gothic_stone_bricks",
          "nightborn:cracked_gothic_stone_bricks",
          26);
  public static final Stone PLINTH = WALL.worn(44);
  public static final Stone DRESSING =
      new Stone(
          "nightborn:carved_gothic_stone",
          "nightborn:mossy_gothic_stone_bricks",
          "nightborn:cracked_gothic_stone_bricks",
          12);
  public static final Stone LIMEWASH =
      new Stone(
          "nightborn:plaster",
          "nightborn:worn_limewashed_bricks",
          "nightborn:limewashed_bricks",
          46);
  public static final String TRIM = "nightborn:gothic_stone_brick_stairs";
  public static final String TRIM_SLAB = "nightborn:gothic_stone_brick_slab";
  public static final String TRIM_WALL = "nightborn:gothic_stone_brick_wall";
  public static final Roof.Cover SLATE =
      new Roof.Cover("deepslate_tile_stairs", "deepslate_tiles", "deepslate_tile_slab");
  public static final String STAINED = "nightborn:gothic_stained_glass_pane";
  public static final String PANE = "glass_pane";
  public static final String PLANKS = "nightborn:ebony_planks";
  public static final String PARQUET = "dark_oak_planks";
  public static final String BEAM = "stripped_dark_oak_log";
  public static final String RUBBLE = "cobbled_deepslate";
  public static final String DOOR = "dark_oak_door";
  public static final String DARK_OAK_COFFIN = "nightborn:dark_oak_coffin";
  public static final String SPRUCE_COFFIN = "nightborn:spruce_coffin";
  public static final String BURGUNDY = "nightborn:burgundy_velvet_drape";
  public static final String EMERALD = "nightborn:emerald_velvet_drape";
  public static final String GARGOYLE = "nightborn:gargoyle";

  private Manor() {}

  public static Template build(final Layout layout) {
    final Template t = new Template(layout.path(), SIZE_X, SIZE_Y, SIZE_Z);
    final Turret turret = new Turret(layout.roundTurret());
    ManorShell.build(t);
    Cellar.build(t);
    ManorRoofs.build(t);
    ManorFront.build(t);
    Chimney.ALL.forEach(chimney -> chimney.build(t));
    turret.build(t);
    StairHall.build(t);
    ManorWindows.build(t);
    GroundFloor.build(t);
    Bedchambers.build(t);
    Glasshouse.build(t, layout.ruined());
    if (layout.ruined()) {
      Ruin.fallenRoof(t);
    }
    Ivy.grow(t);
    Grounds.build(t);
    t.entrance(MID_X, 0, SIZE_Z - 1, ENTRANCE, "south");
    return t;
  }
}
