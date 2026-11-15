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

  static Stream<Arguments> standingNeighbours() {
    return Direction.Type.HORIZONTAL.stream()
        .flatMap(
            facing ->
                Stream.of(
                    Arguments.of(facing, Direction.UP, "head", true),
                    Arguments.of(facing, Direction.UP, "air", false),
                    Arguments.of(facing, Direction.UP, "flat head", false),
                    Arguments.of(facing, facing, "air", true),
                    Arguments.of(facing, facing.getOpposite(), "air", true),
                    Arguments.of(facing, Direction.DOWN, "air", true)));
  }

  @ParameterizedTest(name = "facing {0}, {2} to the {1}: kept {3}")
  @MethodSource("standingNeighbours")
  @DisplayName("a standing foot answers only to the cell above it, never to the wall behind")
  void standingFootPairsUpward(
      final Direction facing, final Direction side, final String neighbour, final boolean kept) {
    final CoffinBlock coffin = (CoffinBlock) Coffins.get(CoffinWood.MANGROVE);
    final BlockState foot =
        coffin
            .getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(BedBlock.PART, BedPart.FOOT)
            .with(CoffinBlock.STANDING, true);
    final BlockState neighbourState =
        switch (neighbour) {
          case "head" -> foot.with(BedBlock.PART, BedPart.HEAD).with(CoffinBlock.OPEN, true);
          case "flat head" ->
              foot.with(BedBlock.PART, BedPart.HEAD).with(CoffinBlock.STANDING, false);
          default -> Blocks.AIR.getDefaultState();
        };
    final BlockState updated =
        coffin.getStateForNeighborUpdate(
            foot, side, neighbourState, null, BlockPos.ORIGIN, BlockPos.ORIGIN.offset(side));
    assertEquals(kept, updated.isOf(coffin));
    if (kept && neighbour.equals("head")) {
      assertEquals(true, updated.get(CoffinBlock.OPEN));
    }
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a standing head without its foot below goes with it")
  void standingHeadBreaksWithoutItsFoot(final Direction facing) {
    final CoffinBlock coffin = (CoffinBlock) Coffins.get(CoffinWood.CRIMSON);
    final BlockState head =
        coffin
            .getDefaultState()
            .with(BedBlock.FACING, facing)
            .with(BedBlock.PART, BedPart.HEAD)
            .with(CoffinBlock.STANDING, true);
    final BlockState updated =
        coffin.getStateForNeighborUpdate(
            head,
            Direction.DOWN,
            Blocks.AIR.getDefaultState(),
            null,
            BlockPos.ORIGIN,
            BlockPos.ORIGIN.down());
    assertEquals(Blocks.AIR.getDefaultState(), updated);
  }

  static Stream<Arguments> standingStates() {
    final BlockState oak = Coffins.get(CoffinWood.OAK).getDefaultState();
    return Stream.of(
        Arguments.of(oak, false),
        Arguments.of(oak.with(CoffinBlock.STANDING, true), true),
        Arguments.of(Blocks.RED_BED.getDefaultState(), false),
        Arguments.of(Blocks.STONE.getDefaultState(), false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("standingStates")
  @DisplayName("only a coffin placed upright counts as standing")
  void knowsAStandingCoffin(final BlockState state, final boolean standing) {
    assertEquals(standing, CoffinBlock.isStanding(state));
  }
}
