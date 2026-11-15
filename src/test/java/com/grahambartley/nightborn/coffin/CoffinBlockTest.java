package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinBlockTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(CoffinWood.class)
  @DisplayName("a coffin is placed shut and empty")
  void startsShut(final CoffinWood wood) {
    final BlockState state = Coffins.get(wood).getDefaultState();
    assertFalse(state.get(CoffinBlock.OPEN));
    assertFalse(state.get(BedBlock.OCCUPIED));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(BedPart.class)
  @DisplayName("only the head cell carries the block entity that draws the lid")
  void drawsTheLidFromTheHead(final BedPart part) {
    final CoffinBlock coffin = (CoffinBlock) Coffins.get(CoffinWood.OAK);
    final BlockState state = coffin.getDefaultState().with(BedBlock.PART, part);
    if (part == BedPart.HEAD) {
      assertInstanceOf(CoffinBlockEntity.class, coffin.createBlockEntity(BlockPos.ORIGIN, state));
    } else {
      assertNull(coffin.createBlockEntity(BlockPos.ORIGIN, state));
    }
  }

  static Stream<Arguments> pairs() {
    return Stream.of(Direction.Type.HORIZONTAL.stream().toArray())
        .flatMap(
            facing -> Stream.of(true, false).map(open -> Arguments.of((Direction) facing, open)));
  }

  @ParameterizedTest(name = "facing {0}, head open {1}")
  @MethodSource("pairs")
  @DisplayName("the foot follows the head's lid so both halves agree")
  void footFollowsTheHead(final Direction facing, final boolean open) {
    final CoffinBlock coffin = (CoffinBlock) Coffins.get(CoffinWood.SPRUCE);
    final BlockState foot =
        coffin.getDefaultState().with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.FOOT);
    final BlockState head = foot.with(BedBlock.PART, BedPart.HEAD).with(CoffinBlock.OPEN, open);
    final BlockState updated =
        coffin.getStateForNeighborUpdate(
            foot, facing, head, null, BlockPos.ORIGIN, BlockPos.ORIGIN.offset(facing));
    assertEquals(open, updated.get(CoffinBlock.OPEN));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a foot whose head is gone goes with it")
  void footBreaksWithoutItsHead(final Direction facing) {
    final CoffinBlock coffin = (CoffinBlock) Coffins.get(CoffinWood.CHERRY);
    final BlockState foot =
        coffin.getDefaultState().with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.FOOT);
    final BlockState updated =
        coffin.getStateForNeighborUpdate(
            foot,
            facing,
            Blocks.AIR.getDefaultState(),
            null,
            BlockPos.ORIGIN,
            BlockPos.ORIGIN.offset(facing));
    assertEquals(Blocks.AIR.getDefaultState(), updated);
  }
}
