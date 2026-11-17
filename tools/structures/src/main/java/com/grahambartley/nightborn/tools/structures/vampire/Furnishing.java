package com.grahambartley.nightborn.tools.structures.vampire;

import com.grahambartley.nightborn.tools.structures.nbt.Nbt;
import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Facing;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;

public final class Furnishing {

  public static final String LOOT = "nightborn:chests/vampire_structure";
  public static final String BOOK = "minecraft:writable_book";
  public static final String PORTRAIT = "nightborn:portrait";
  public static final int PORTRAITS = 3;
  public static final String GRAMOPHONE = "nightborn:gramophone";
  public static final List<String> DISCS =
      List.of(
          "nightborn:music_disc_prelude_in_a_minor",
          "nightborn:music_disc_variation_25",
          "nightborn:music_disc_la_malinconia",
          "nightborn:music_disc_adagio_in_f_minor");

  private Furnishing() {}

  public static void coffin(
      final Template t,
      final String coffin,
      final int x,
      final int y,
      final int z,
      final String facing) {
    final Pos head = Facing.STEP.get(facing);
    t.set(x, y, z, coffinPart(coffin, facing, "foot", false));
    t.set(x + head.x(), y, z + head.z(), coffinPart(coffin, facing, "head", false));
  }

  public static void standingCoffin(
      final Template t,
      final String coffin,
      final int x,
      final int y,
      final int z,
      final String back) {
    t.set(x, y, z, coffinPart(coffin, back, "foot", true));
    t.set(x, y + 1, z, coffinPart(coffin, back, "head", true));
  }

  public static void chest(
      final Template t, final int x, final int y, final int z, final String facing) {
    chest(t, x, y, z, facing, LOOT);
  }

  public static void chest(
      final Template t,
      final int x,
      final int y,
      final int z,
      final String facing,
      final String lootTable) {
    t.set(
        x,
        y,
        z,
        BlockState.of("chest", "facing", facing, "type", "single", "waterlogged", false),
        Nbt.compound("id", "minecraft:chest", "LootTable", lootTable));
  }

  public static void lectern(
      final Template t, final int x, final int y, final int z, final String facing) {
    t.set(
        x,
        y,
        z,
        BlockState.of("lectern", "facing", facing, "has_book", true, "powered", false),
        Nbt.compound(
            "id", "minecraft:lectern", "Book", Nbt.compound("id", BOOK, "count", 1), "Page", 0));
  }

  public static void portrait(
      final Template t, final int x, final int y, final int z, final String facing) {
    t.set(
        x,
        y,
        z,
        BlockState.of(PORTRAIT, "facing", facing, "portrait", Stone.roll(x, y, z) % PORTRAITS));
  }

  public static void gramophone(
      final Template t, final int x, final int y, final int z, final String facing) {
    t.set(
        x,
        y,
        z,
        BlockState.of(GRAMOPHONE, "facing", facing, "has_record", true, "playing", false),
        Nbt.compound(
            "id",
            GRAMOPHONE,
            "RecordItem",
            Nbt.compound("id", DISCS.get(Stone.roll(x, y, z) % DISCS.size()), "count", 1),
            "Ambient",
            true));
  }

  public static void candles(
      final Template t, final int x, final int y, final int z, final int count, final boolean lit) {
    t.set(x, y, z, BlockState.of("candle", "candles", count, "lit", lit, "waterlogged", false));
  }

  public static void chair(
      final Template t, final int x, final int y, final int z, final String facing) {
    t.set(x, y, z, BlockState.of("nightborn:ornate_chair", "facing", facing));
  }

  public static void table(final Template t, final int x, final int y, final int z) {
    t.set(x, y, z, "nightborn:ornate_table");
  }

  public static void candelabra(final Template t, final int x, final int y, final int z) {
    t.set(x, y, z, BlockState.of("nightborn:candelabra", "facing", "north", "lit", false));
  }

  public static void chandelier(final Template t, final int x, final int y, final int z) {
    t.set(x, y, z, BlockState.of("nightborn:chandelier", "facing", "north", "lit", false));
  }

  public static void chalice(
      final Template t, final int x, final int y, final int z, final String blood) {
    t.set(x, y, z, BlockState.of("nightborn:chalice", "blood", blood));
  }

  public static String splatter(final String facing, final int splat) {
    return BlockState.of(
        "nightborn:blood_splatter", "face", "floor", "facing", facing, "splat", splat);
  }

  public static void drape(
      final Template t,
      final String drape,
      final int x,
      final int y0,
      final int z,
      final int height,
      final String facing) {
    for (int y = y0; y < y0 + height; y++) {
      t.set(
          x,
          y,
          z,
          BlockState.of(drape, "facing", facing, "open", false, "top", y == y0 + height - 1));
    }
  }

  static String coffinPart(
      final String coffin, final String facing, final String part, final boolean standing) {
    return BlockState.of(
        coffin,
        "facing",
        facing,
        "part",
        part,
        "occupied",
        false,
        "open",
        false,
        "standing",
        standing);
  }
}
