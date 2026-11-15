package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class StructureHeartTest {

  private static BlockBox box(final int i) {
    return new BlockBox(i * 20, 60, 0, i * 20 + 9, 69, 9);
  }

  @ParameterizedTest(name = "{0} pieces")
  @ValueSource(ints = {0, 1, 2, 5})
  @DisplayName("the heart is the first piece placed, and a structure with no pieces has none")
  void isTheFirstPiece(final int count) {
    final List<StructurePiece> pieces =
        IntStream.range(0, count).mapToObj(i -> (StructurePiece) new Piece(box(i))).toList();
    assertEquals(count == 0 ? Optional.empty() : Optional.of(box(0)), StructureHeart.of(pieces));
  }

  private static final class Piece extends StructurePiece {

    Piece(final BlockBox box) {
      super(StructurePieceType.JIGSAW, 0, box);
    }

    @Override
    protected void writeNbt(final StructureContext context, final NbtCompound nbt) {}

    @Override
    public void generate(
        final StructureWorldAccess world,
        final StructureAccessor structures,
        final ChunkGenerator generator,
        final Random random,
        final BlockBox chunkBox,
        final ChunkPos chunk,
        final BlockPos pivot) {}
  }
}
