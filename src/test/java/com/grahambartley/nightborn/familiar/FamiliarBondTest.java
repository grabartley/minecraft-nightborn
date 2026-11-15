package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class FamiliarBondTest {

  private static final UUID FAMILIAR = UUID.fromString("7d9e1f6a-3c1b-4e8d-9a2f-1b2c3d4e5f60");
  private static final GlobalPos CHEST = GlobalPos.create(World.OVERWORLD, BlockPos.ORIGIN);

  static Stream<FamiliarBond> bonds() {
    return Stream.of(
        new FamiliarBond(FAMILIAR, ChestMarks.NONE),
        new FamiliarBond(FAMILIAR, new ChestMarks(Optional.of(CHEST), Optional.empty())),
        new FamiliarBond(FAMILIAR, new ChestMarks(Optional.of(CHEST), Optional.of(CHEST))));
  }

  @ParameterizedTest
  @MethodSource("bonds")
  @DisplayName("a bond survives a save and load")
  void codec(final FamiliarBond bond) {
    assertEquals(
        bond,
        FamiliarBond.CODEC
            .parse(
                NbtOps.INSTANCE, FamiliarBond.CODEC.encodeStart(NbtOps.INSTANCE, bond).getOrThrow())
            .getOrThrow());
  }

  @ParameterizedTest
  @MethodSource("bonds")
  @DisplayName("new marks keep the same familiar")
  void withMarks(final FamiliarBond bond) {
    final ChestMarks marks = new ChestMarks(Optional.empty(), Optional.of(CHEST));
    assertEquals(new FamiliarBond(FAMILIAR, marks), bond.withMarks(marks));
  }
}
