package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import com.grahambartley.nightborn.tools.structures.template.Connections;
import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Roof;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;
import java.util.List;

public final class MountainCastle {

  public record Layout(String path, boolean roundWest, boolean chapel) {}

  public static final Layout EYRIE = new Layout("vampire/mountain_castle/eyrie", true, true);
  public static final Layout BASTION = new Layout("vampire/mountain_castle/bastion", false, false);
  public static final List<Layout> LAYOUTS = List.of(EYRIE, BASTION);

  public static final String ANCHOR = "nightborn:mountain_castle_summit";

  public static final int SIZE = 63;
  public static final int HEIGHT = 96;
  public static final int MID = SIZE / 2;
  public static final int LOW = 26;
  public static final int UP = 32;

  public static final Stone WALL =
      new Stone(
          "nightborn:limewashed_bricks",
          "nightborn:worn_limewashed_bricks",
          "nightborn:worn_limewashed_bricks",
          20);
  public static final Stone FOOT =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 34);
  public static final Stone KEEP =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 22);
  public static final Stone PLASTER =
      new Stone("nightborn:plaster", "nightborn:plaster", "nightborn:plaster", 0);
  public static final Roof.Cover TILES =
      new Roof.Cover(
          "nightborn:clay_roof_tile_stairs",
          "nightborn:clay_roof_tiles",
          "nightborn:clay_roof_tile_slab");
  public static final String TRIM = "nightborn:limewashed_brick_stairs";
  public static final String TRIM_SLAB = "nightborn:limewashed_brick_slab";
  public static final String KEEP_STEP = "stone_brick_stairs";
  public static final String TIMBER = "dark_oak_log";
  public static final String PLANKS = "dark_oak_planks";
  public static final String STAINED = "nightborn:gothic_stained_glass_pane";
  public static final String PANE = "glass_pane";
  public static final String DARK_OAK_COFFIN = "nightborn:dark_oak_coffin";
  public static final String GOTHIC_COFFIN = "nightborn:gothic_stone_brick_coffin";
  public static final String BURGUNDY = "nightborn:burgundy_velvet_drape";
  public static final String EMERALD = "nightborn:emerald_velvet_drape";
  public static final String GARGOYLE = "nightborn:gargoyle";
  public static final String LOOT = "nightborn:chests/mountain_castle";

  private MountainCastle() {}

  public static Template build(final Layout layout) {
    final Template t = new Template(layout.path(), SIZE, HEIGHT, SIZE);
    Crag.build(t);
    Ramparts.build(t);
    for (final Turret turret : turrets(layout)) {
      turret.build(t);
    }
    Barbican.build(t);
    Donjon.build(t);
    DonjonRooms.build(t);
    HallWing.build(t);
    if (layout.chapel()) {
      Chapel.build(t);
    }
    Wards.build(t, layout);
    Crag.skirt(t);
    t.anchor(MID, UP, MID, ANCHOR, KEEP.base());
    Connections.connect(t);
    return t;
  }

  public static List<Turret> turrets(final Layout layout) {
    final boolean west = layout.roundWest();
    return List.of(
        new Turret(9, 7, 5, UP, Ramparts.TOP + 2 * Turret.STOREY, west, Ramparts.TOP),
        new Turret(53, 7, 4, UP, Ramparts.TOP + Turret.STOREY, !west, Ramparts.TOP),
        new Turret(8, 44, 4, UP, Ramparts.TOP + Turret.STOREY, !west, Ramparts.TOP),
        new Turret(54, 44, 4, UP, Ramparts.TOP + Turret.STOREY, west, Ramparts.TOP),
        new Turret(11, 57, 3, LOW, Ramparts.LOW_TOP + Turret.STOREY, true, Ramparts.LOW_TOP),
        new Turret(51, 57, 3, LOW, Ramparts.LOW_TOP + Turret.STOREY, true, Ramparts.LOW_TOP));
  }
}
