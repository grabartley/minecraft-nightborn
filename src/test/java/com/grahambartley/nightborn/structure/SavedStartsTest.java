package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SavedStartsTest {

  static final int VERSION = 3955;
  static final Identifier LAIR = Identifier.of("nightborn", "underground_lair");

  static NbtCompound chunk(final int version, final String startId) {
    final NbtCompound starts = new NbtCompound();
    if (!startId.isEmpty()) {
      final NbtCompound start = new NbtCompound();
      start.putString("id", startId);
      starts.put(LAIR.toString(), start);
    }
    final NbtCompound structures = new NbtCompound();
    structures.put(SavedStarts.STARTS, starts);
    final NbtCompound chunk = new NbtCompound();
    chunk.putInt(SavedStarts.DATA_VERSION, version);
    chunk.put(SavedStarts.STRUCTURES, structures);
    return chunk;
  }

  @ParameterizedTest(name = "saved start id ''{0}'' -> present {1}")
  @CsvSource({"nightborn:underground_lair, true", "INVALID, false", "'', false"})
  @DisplayName("a saved chunk says whether the house really began there")
  void saved(final String startId, final boolean present) {
    assertEquals(Optional.of(present), SavedStarts.has(chunk(VERSION, startId), LAIR, VERSION));
  }

  @ParameterizedTest(name = "saved at version {0} -> unknown")
  @CsvSource({"3953", "1493", "0"})
  @DisplayName("a chunk saved by another game version is left to the seed")
  void otherVersion(final int version) {
    assertEquals(
        Optional.empty(),
        SavedStarts.has(chunk(version, "nightborn:underground_lair"), LAIR, VERSION));
  }

  @Test
  @DisplayName("a chunk never saved is left to the seed")
  void unsaved() {
    assertEquals(Optional.empty(), SavedStarts.has(null, LAIR, VERSION));
    assertEquals(Optional.empty(), SavedStarts.has(NbtString.of("x"), LAIR, VERSION));
  }
}
