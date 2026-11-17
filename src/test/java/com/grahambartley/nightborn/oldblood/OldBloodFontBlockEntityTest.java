package com.grahambartley.nightborn.oldblood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class OldBloodFontBlockEntityTest {

  private static final DynamicRegistryManager REGISTRIES =
      DynamicRegistryManager.of(Registries.REGISTRIES);

  private static OldBloodFontBlockEntity font() {
    return new OldBloodFontBlockEntity(BlockPos.ORIGIN, OldBloodBlocks.FONT.getDefaultState());
  }

  @ParameterizedTest(name = "drained at {0}")
  @ValueSource(longs = {0L, 12_000L, 1_000_000L})
  @DisplayName("a drained font keeps how far it has refilled through a save and load")
  void roundTrips(final long drainedAt) {
    final OldBloodFontBlockEntity font = font();
    font.drain(drainedAt);
    final NbtCompound nbt = font.createNbt(REGISTRIES);
    final OldBloodFontBlockEntity loaded = font();
    loaded.read(nbt, REGISTRIES);
    assertEquals(false, loaded.full());
    assertEquals(0L, loaded.progress());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(booleans = {true})
  @DisplayName("a font placed by a structure starts full")
  void startsFull(final boolean full) {
    final OldBloodFontBlockEntity loaded = font();
    loaded.read(new NbtCompound(), REGISTRIES);
    assertEquals(full, loaded.full());
    assertEquals(full, font().full());
  }
}
