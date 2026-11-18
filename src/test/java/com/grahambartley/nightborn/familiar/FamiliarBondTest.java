package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Uuids;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarBondTest {

  @ParameterizedTest
  @CsvSource({"7d9e1f6a-3c1b-4e8d-9a2f-1b2c3d4e5f60", "00000000-0000-0000-0000-000000000001"})
  @DisplayName("a bond survives a save and load")
  void codec(final UUID familiar) {
    final FamiliarBond bond = new FamiliarBond(familiar);
    assertEquals(
        bond,
        FamiliarBond.CODEC
            .parse(
                NbtOps.INSTANCE, FamiliarBond.CODEC.encodeStart(NbtOps.INSTANCE, bond).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest
  @CsvSource({"7d9e1f6a-3c1b-4e8d-9a2f-1b2c3d4e5f60", "00000000-0000-0000-0000-000000000001"})
  @DisplayName("a bond saved with chest marks still loads its familiar")
  void readsOlderSaves(final UUID familiar) {
    final NbtCompound saved = new NbtCompound();
    saved.put(
        "familiar", Uuids.INT_STREAM_CODEC.encodeStart(NbtOps.INSTANCE, familiar).getOrThrow());
    saved.put("marks", new NbtCompound());
    assertEquals(
        new FamiliarBond(familiar), FamiliarBond.CODEC.parse(NbtOps.INSTANCE, saved).getOrThrow());
  }
}
