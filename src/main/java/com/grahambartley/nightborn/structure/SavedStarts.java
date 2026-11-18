package com.grahambartley.nightborn.structure;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.scanner.NbtScanQuery;
import net.minecraft.nbt.scanner.SelectiveNbtCollector;
import net.minecraft.util.Identifier;

public final class SavedStarts {

  public static final String DATA_VERSION = "DataVersion";
  public static final String STRUCTURES = "structures";
  public static final String STARTS = "starts";
  public static final String INVALID = "INVALID";
  public static final int FIRST_STARTS_LAYOUT = 2860;

  private SavedStarts() {}

  public static SelectiveNbtCollector collector() {
    return new SelectiveNbtCollector(
        new NbtScanQuery(NbtInt.TYPE, DATA_VERSION),
        new NbtScanQuery(STRUCTURES, NbtCompound.TYPE, STARTS));
  }

  public static Optional<Boolean> has(
      final NbtElement saved, final Identifier structure, final int currentVersion) {
    if (!(saved instanceof NbtCompound chunk)
        || chunk.getInt(DATA_VERSION) < FIRST_STARTS_LAYOUT
        || chunk.getInt(DATA_VERSION) > currentVersion) {
      return Optional.empty();
    }
    final NbtCompound starts = chunk.getCompound(STRUCTURES).getCompound(STARTS);
    final String key = structure.toString();
    return Optional.of(
        starts.contains(key, NbtElement.COMPOUND_TYPE)
            && !INVALID.equals(starts.getCompound(key).getString("id")));
  }
}
