package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CrucifixShapesTest {

  static Stream<List<BlockPos>> saved() {
    return Stream.of(
        List.of(),
        List.of(new BlockPos(0, 64, 0)),
        List.of(new BlockPos(15, 70, 15), new BlockPos(16, 70, 15), new BlockPos(-1, -40, -1)),
        List.of(new BlockPos(30000000, 300, -30000000)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("saved")
  @DisplayName("found shapes survive a save and load")
  void roundTrips(final List<BlockPos> centres) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putLongArray("centres", centres.stream().mapToLong(BlockPos::asLong).toArray());
    final CrucifixShapes loaded = CrucifixShapes.fromNbt(nbt, null);
    centres.forEach(centre -> assertTrue(loaded.contains(centre), centre.toString()));
    assertFalse(loaded.isDirty());
    final CrucifixShapes reloaded =
        CrucifixShapes.fromNbt(loaded.writeNbt(new NbtCompound(), null), null);
    centres.forEach(centre -> assertTrue(reloaded.contains(centre), centre.toString()));
  }

  @Test
  @DisplayName("an empty save holds no shapes")
  void emptySave() {
    assertFalse(CrucifixShapes.fromNbt(new NbtCompound(), null).contains(BlockPos.ORIGIN));
  }
}
