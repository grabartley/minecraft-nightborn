package com.grahambartley.nightborn.tools.structures.camp;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.nbt.NbtList;
import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;
import java.util.Map;

public final class HunterCamp {

  public static final String PATH = "hunter_camp/camp";
  public static final int SIZE = 17;
  public static final int HEIGHT = 8;
  public static final int FIRE_X = 8;
  public static final int FIRE_Z = 10;
  static final int TRODDEN = 2;
  public static final String LOOT = "nightborn:chests/hunter_camp";
  public static final String HUNTER = "nightborn:hunter";
  public static final String GUILD_MASTER = "nightborn:guild_master";
  public static final List<int[]> HUNTER_SPOTS =
      List.of(new int[] {6, 1, 11}, new int[] {10, 1, 9}, new int[] {8, 1, 12});
  public static final int[] GUILD_MASTER_SPOT = {8, 1, 2};

  static final CampTent GUILD_TENT = new CampTent(0, 5, 8, 4, false, "brown_wool");
  static final CampTent BUNK_TENT = new CampTent(0, 4, 11, 3, true, "gray_wool");

  private HunterCamp() {}

  public static Template build() {
    final Template camp = new Template(PATH, SIZE, HEIGHT, SIZE);
    camp.fill(0, 1, 0, SIZE - 1, HEIGHT - 1, SIZE - 1, "air");
    ground(camp);
    GUILD_TENT.build(camp);
    BUNK_TENT.build(camp);
    guildTent(camp);
    bunkTent(camp);
    fireside(camp);
    supplies(camp);
    lanternPost(camp, 12, 14);
    lanternPost(camp, 4, 6);
    residents(camp);
    return camp;
  }

  private static void ground(final Template camp) {
    for (int x = 0; x < SIZE; x++) {
      for (int z = 0; z < SIZE; z++) {
        if (Math.floorMod(x * 7 + z * 13, 5) == 0) {
          camp.set(x, 0, z, "coarse_dirt");
        }
      }
    }
    camp.fill(5, 0, 1, 11, 0, 5, "coarse_dirt");
    camp.fill(1, 0, 9, 4, 0, 13, "coarse_dirt");
    for (int x = FIRE_X - TRODDEN; x <= FIRE_X + TRODDEN; x++) {
      for (int z = FIRE_Z - TRODDEN; z <= FIRE_Z + TRODDEN; z++) {
        camp.set(x, 0, z, "dirt_path");
      }
    }
    camp.set(FIRE_X, 0, FIRE_Z, "stone");
    camp.fill(FIRE_X, 0, 6, FIRE_X, 0, FIRE_Z - TRODDEN, "dirt_path");
    camp.fill(5, 0, FIRE_Z + 1, FIRE_X - TRODDEN, 0, FIRE_Z + 1, "dirt_path");
    camp.fill(FIRE_X + TRODDEN, 0, 7, SIZE - 1, 0, 7, "dirt_path");
  }

  private static void guildTent(final Template camp) {
    camp.fill(6, 1, 1, 10, 1, 4, "brown_carpet");
    camp.set(8, 1, 3, "cartography_table");
    camp.set(8, 2, 3, BlockState.of("candle", "candles", 3, "lit", true));
    camp.set(6, 1, 2, BlockState.of("lectern", "facing", "south"));
    camp.set(10, 1, 2, BlockState.of("chest", "facing", "south"), container("minecraft:chest"));
    camp.set(10, 1, 1, BlockState.of("barrel", "facing", "up"), container("minecraft:barrel"));
    camp.set(8, 4, 3, BlockState.of("lantern", "hanging", true));
    camp.set(8, 3, 1, BlockState.of("nightborn:gold_wall_crucifix", "facing", "south"));
    camp.set(6, 2, 1, poster("south", 0));
    camp.set(10, 2, 1, poster("south", 2));
    camp.set(8, 4, 5, BlockState.of("nightborn:garlic_string", "facing", "south", "hanging", true));
  }

  private static void bunkTent(final Template camp) {
    bed(camp, 10);
    bed(camp, 12);
    camp.set(1, 1, 11, BlockState.of("barrel", "facing", "up"), container("minecraft:barrel"));
    camp.set(
        1,
        2,
        11,
        BlockState.of("nightborn:weapon_rack", "facing", "east"),
        rack("nightborn:wooden_stake", "nightborn:silver_sword"));
    camp.set(2, 3, 11, BlockState.of("lantern", "hanging", true));
    camp.set(4, 3, 11, BlockState.of("nightborn:garlic_string", "facing", "east", "hanging", true));
  }

  private static void bed(final Template camp, final int z) {
    camp.set(1, 1, z, BlockState.of("brown_bed", "facing", "west", "part", "head"));
    camp.set(2, 1, z, BlockState.of("brown_bed", "facing", "west", "part", "foot"));
  }

  private static void fireside(final Template camp) {
    camp.set(FIRE_X, 1, FIRE_Z, BlockState.of("campfire", "facing", "north", "lit", true));
    camp.fill(
        11, 1, FIRE_Z - 1, 11, 1, FIRE_Z + 1, BlockState.of("stripped_spruce_log", "axis", "z"));
    camp.fill(
        FIRE_X - 1, 1, 13, FIRE_X + 1, 1, 13, BlockState.of("stripped_spruce_log", "axis", "x"));
  }

  private static void supplies(final Template camp) {
    camp.set(15, 1, 1, BlockState.of("hay_block", "axis", "y"));
    camp.set(15, 2, 1, BlockState.of("hay_block", "axis", "x"));
    camp.set(14, 1, 1, BlockState.of("hay_block", "axis", "z"));
    camp.set(15, 1, 2, BlockState.of("barrel", "facing", "up"), container("minecraft:barrel"));
    camp.set(13, 1, 1, "crafting_table");
    camp.set(15, 1, 4, "smithing_table");
    camp.set(13, 1, 4, BlockState.of("grindstone", "face", "floor", "facing", "north"));
    camp.set(15, 1, 5, "fletching_table");
    camp.fill(15, 1, 9, 15, 2, 9, BlockState.of("spruce_log", "axis", "y"));
    camp.set(
        14,
        2,
        9,
        BlockState.of("nightborn:weapon_rack", "facing", "west"),
        rack("nightborn:silver_stake", "minecraft:crossbow"));
    camp.set(14, 1, 9, poster("west", 1));
  }

  private static void lanternPost(final Template camp, final int x, final int z) {
    camp.fill(x, 1, z, x, 2, z, "spruce_fence");
    camp.set(x, 3, z, BlockState.of("lantern", "hanging", false));
  }

  private static void residents(final Template camp) {
    for (final int[] spot : HUNTER_SPOTS) {
      camp.entity(spot[0], spot[1], spot[2], resident(HUNTER));
    }
    camp.entity(
        GUILD_MASTER_SPOT[0], GUILD_MASTER_SPOT[1], GUILD_MASTER_SPOT[2], resident(GUILD_MASTER));
  }

  private static Map<String, Object> resident(final String id) {
    return Nbt.compound("id", id, "PersistenceRequired", Nbt.flag(true));
  }

  private static Map<String, Object> container(final String id) {
    return Nbt.compound("id", id, "LootTable", LOOT);
  }

  private static String poster(final String facing, final int portrait) {
    return BlockState.of("nightborn:wanted_poster", "facing", facing, "portrait", portrait);
  }

  private static Map<String, Object> rack(final String first, final String second) {
    return Nbt.compound(
        "id",
        "nightborn:weapon_rack",
        "Items",
        NbtList.compounds(List.of(hook(0, first), hook(1, second))));
  }

  private static Map<String, Object> hook(final int slot, final String item) {
    return Nbt.compound("Slot", (byte) slot, "id", item, "count", 1);
  }
}
