package com.grahambartley.nightborn.nemesis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NemesisEntryTest {

  static final UUID MOB = UUID.fromString("00000000-0000-0000-0000-000000000003");
  static final UUID QUARRY = UUID.fromString("00000000-0000-0000-0000-000000000001");

  static NemesisEntry entry(final int token) {
    final NbtCompound snapshot = new NbtCompound();
    snapshot.putString("id", "nightborn:hunter");
    return new NemesisEntry(
        MOB, Identifier.of("nightborn", "hunter"), "Silas", 12L, token, snapshot);
  }

  @ParameterizedTest(name = "entry token {0}, mark token {1}: current {2}")
  @CsvSource({"0, 0, true", "3, 3, true", "3, 2, false", "1, 2, false"})
  @DisplayName("only the copy that carries the latest token is the nemesis")
  void current(final int entryToken, final int markToken, final boolean current) {
    assertEquals(
        current, entry(entryToken).current(new NemesisMark(QUARRY, markToken, 0, "Silas")));
  }

  @ParameterizedTest(name = "visited on {0} with token {1}")
  @CsvSource({"20, 1", "30, 4"})
  @DisplayName("a visit moves the next night and the token, and keeps the rest")
  void visited(final long next, final int token) {
    final NemesisEntry visited = entry(0).visited(next, token);
    assertEquals(next, visited.nextVisit());
    assertEquals(token, visited.token());
    assertEquals(entry(0).snapshot(), visited.snapshot());
    assertEquals(
        visited,
        NemesisEntry.CODEC
            .parse(
                JsonOps.INSTANCE,
                NemesisEntry.CODEC.encodeStart(JsonOps.INSTANCE, visited).getOrThrow())
            .getOrThrow());
  }
}
