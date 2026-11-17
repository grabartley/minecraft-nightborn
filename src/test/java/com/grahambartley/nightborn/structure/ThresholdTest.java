package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.block.JigsawBlock;
import net.minecraft.block.enums.Orientation;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class ThresholdTest {

  private static final Identifier DOOR = Identifier.of("nightborn", "castle_entrance");

  private static StructureTemplate.StructureBlockInfo jigsaw(
      final BlockPos pos, final Orientation orientation, final String name) {
    final NbtCompound nbt = new NbtCompound();
    nbt.putString(Threshold.NAME_KEY, name);
    return new StructureTemplate.StructureBlockInfo(
        pos, Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, orientation), nbt);
  }

  @ParameterizedTest(name = "a floor at y {0} under a surface of {1} moves by {2}")
  @CsvSource({"64, 65, 0", "70, 65, -6", "60, 65, 4", "100, 180, 79"})
  void liftsTheFloorToJustUnderTheSurface(final int floorY, final int surface, final int lift) {
    assertEquals(lift, new Threshold(new BlockPos(3, floorY, 9), Direction.SOUTH).lift(surface));
  }

  @ParameterizedTest(name = "an entrance facing {0} looks at the ground at {1},{2}")
  @CsvSource({"SOUTH, 3, 10", "NORTH, 3, 8", "EAST, 4, 9", "WEST, 2, 9"})
  void readsTheGroundOneStepOutside(final Direction out, final int x, final int z) {
    assertEquals(new ColumnPos(x, z), new Threshold(new BlockPos(3, 64, 9), out).outside());
  }

  @ParameterizedTest(name = "a {0} jigsaw named {1} is the entrance: {2}")
  @CsvSource({
    "SOUTH_UP, nightborn:castle_entrance, true",
    "WEST_UP, nightborn:castle_entrance, true",
    "UP_NORTH, nightborn:castle_keep, false",
    "SOUTH_UP, minecraft:empty, false"
  })
  void findsTheEntranceByItsName(
      final Orientation orientation, final String name, final boolean found) {
    final BlockPos pos = new BlockPos(5, 70, 5);
    final Optional<Threshold> threshold =
        Threshold.named(
            DOOR,
            List.of(
                jigsaw(new BlockPos(0, 60, 0), Orientation.UP_NORTH, "nightborn:other"),
                jigsaw(pos, orientation, name)));
    assertEquals(
        found ? Optional.of(new Threshold(pos, orientation.getFacing())) : Optional.empty(),
        threshold);
  }
}
