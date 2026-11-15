package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FamiliarRegistryTest {

  private static final UUID OWNER = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID FAMILIAR = UUID.fromString("00000000-0000-0000-0000-000000000002");
  private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000003");

  @ParameterizedTest(name = "freeing {0}: still kept {1}")
  @CsvSource({
    "00000000-0000-0000-0000-000000000002, false",
    "00000000-0000-0000-0000-000000000003, true"
  })
  @DisplayName("only the familiar an owner keeps frees their slot")
  void free(final UUID freed, final boolean stillKept) {
    final FamiliarRegistry registry = new FamiliarRegistry();
    registry.bind(OWNER, FAMILIAR);
    registry.free(OWNER, freed);
    assertEquals(stillKept, registry.keeps(OWNER, FAMILIAR));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000003"})
  @DisplayName("freeing an owner clears whichever familiar they keep")
  void freeOwner(final UUID familiar) {
    final FamiliarRegistry registry = new FamiliarRegistry();
    registry.bind(OWNER, familiar);
    registry.freeOwner(OWNER);
    assertTrue(registry.bondOf(OWNER).isEmpty());
    assertFalse(registry.keeps(OWNER, familiar));
  }

  @ParameterizedTest(name = "fetch {0}, deliver {1}")
  @CsvSource({"true, true", "true, false", "false, false"})
  @DisplayName("bonds and their marks survive a save and load")
  void saves(final boolean fetch, final boolean deliver) {
    final GlobalPos chest = GlobalPos.create(World.OVERWORLD, new BlockPos(3, 70, -9));
    final FamiliarBond bond =
        new FamiliarBond(
            FAMILIAR,
            new ChestMarks(
                fetch ? Optional.of(chest) : Optional.empty(),
                deliver ? Optional.of(chest) : Optional.empty()));
    final FamiliarRegistry registry = new FamiliarRegistry();
    registry.update(OWNER, bond);
    registry.bind(OTHER, OTHER);
    final FamiliarRegistry loaded =
        FamiliarRegistry.fromNbt(registry.writeNbt(new NbtCompound(), null), null);
    assertEquals(Optional.of(bond), loaded.bondOf(OWNER));
    assertTrue(loaded.keeps(OTHER, OTHER));
  }
}
