package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CoffinLayoutTest {

  private static final BlockPos AT = new BlockPos(10, 64, -7);
  private static final double EPSILON = 1.0E-9;
  private static final double HALF_WIDTH = 0.3;
  private static final double SHUT_LID_INNER_FACE = 8.0 / 16.0;
  private static final double NEAR_PLANE = 0.05;

  static Stream<Arguments> cells() {
    return Direction.Type.HORIZONTAL.stream()
        .flatMap(
            facing ->
                Stream.of(true, false)
                    .flatMap(
                        standing ->
                            Stream.of(BedPart.values())
                                .map(part -> Arguments.of(facing, standing, part))));
  }

  static Stream<Arguments> otherParts() {
    return Stream.of(
        Arguments.of(BedPart.FOOT, Direction.NORTH, false, Direction.NORTH),
        Arguments.of(BedPart.HEAD, Direction.NORTH, false, Direction.SOUTH),
        Arguments.of(BedPart.FOOT, Direction.EAST, false, Direction.EAST),
        Arguments.of(BedPart.HEAD, Direction.WEST, false, Direction.EAST),
        Arguments.of(BedPart.FOOT, Direction.NORTH, true, Direction.UP),
        Arguments.of(BedPart.HEAD, Direction.NORTH, true, Direction.DOWN),
        Arguments.of(BedPart.FOOT, Direction.SOUTH, true, Direction.UP),
        Arguments.of(BedPart.HEAD, Direction.EAST, true, Direction.DOWN));
  }

  @ParameterizedTest(name = "{0} facing {1}, standing {2}: other part is {3}")
  @MethodSource("otherParts")
  @DisplayName("a flat coffin pairs along its facing like a bed, a standing one pairs up and down")
  void pairsTheCells(
      final BedPart part,
      final Direction facing,
      final boolean standing,
      final Direction expected) {
    assertEquals(expected, CoffinLayout.towardsOtherPart(part, facing, standing));
  }

  @ParameterizedTest(name = "facing {0}, standing {1}, from the {2}")
  @MethodSource("cells")
  @DisplayName("either cell finds the same head and foot, two different cells")
  void eitherCellFindsThePair(final Direction facing, final boolean standing, final BedPart part) {
    final BlockPos foot = AT;
    final BlockPos head =
        foot.offset(CoffinLayout.towardsOtherPart(BedPart.FOOT, facing, standing));
    final BlockPos from = part == BedPart.HEAD ? head : foot;
    assertEquals(head, CoffinLayout.head(from, part, facing, standing));
    assertEquals(foot, CoffinLayout.foot(from, part, facing, standing));
    assertNotEquals(head, foot);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("a flat sleeper lies low in the head cell's middle so the lid shuts over them")
  void liesLowInAFlatCoffin(final Direction facing) {
    final Vec3d spot = CoffinLayout.restingSpot(AT, facing, false);
    assertEquals(AT.getX() + 0.5, spot.x, EPSILON);
    assertEquals(AT.getY() + 3.0 / 16.0, spot.y, EPSILON);
    assertEquals(AT.getZ() + 0.5, spot.z, EPSILON);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName(
      "an upright sleeper stands on the foot cell's floor, inside the walls and clear of the lid")
  void standsInAStandingCoffin(final Direction facing) {
    final Vec3d spot = CoffinLayout.restingSpot(AT, facing, true);
    final Vec3d footMiddle = Vec3d.ofBottomCenter(AT.down());
    final Vec3d shift = spot.subtract(footMiddle);
    assertEquals(0.0, shift.y, EPSILON);
    final double towardsWall = shift.x * facing.getOffsetX() + shift.z * facing.getOffsetZ();
    final double sideways = shift.x * facing.getOffsetZ() - shift.z * facing.getOffsetX();
    assertEquals(0.0, sideways, EPSILON);
    final double fromBackWall = 0.5 - towardsWall;
    assertTrue(fromBackWall - HALF_WIDTH >= 0.0, "the body stays off the wall behind");
    assertTrue(
        SHUT_LID_INNER_FACE - fromBackWall > NEAR_PLANE,
        "the eye sits far enough behind the shut lid that the view cannot pass through it");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("an upright sleeper looks out through the lid, away from the wall behind")
  void looksOutThroughTheLid(final Direction facing) {
    final Vec3d look = Vec3d.fromPolar(0.0F, CoffinLayout.outwardYaw(facing));
    assertEquals(-facing.getOffsetX(), look.x, 1.0E-6);
    assertEquals(-facing.getOffsetZ(), look.z, 1.0E-6);
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName("the cells in front are the two the lid opens onto")
  void findsTheCellsInFront(final Direction facing) {
    final BlockPos head = AT;
    final Direction out = facing.getOpposite();
    assertEquals(
        List.of(head.offset(out), head.down().offset(out)), CoffinLayout.frontCells(head, facing));
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName(
      "a waking sleeper tries the floor straight in front first, never the coffin or the wall")
  void stepsOutOnTheFloorInFront(final Direction facing) {
    final BlockPos head = AT;
    final BlockPos foot = head.down();
    final List<BlockPos> cells = CoffinLayout.stepOutCells(head, facing);
    assertEquals(foot.offset(facing.getOpposite()), cells.getFirst());
    for (final BlockPos cell : cells) {
      assertEquals(foot.getY(), cell.getY());
      assertNotEquals(foot, cell);
      assertNotEquals(foot.offset(facing), cell);
    }
    assertEquals(cells.size(), cells.stream().distinct().count());
    assertFalse(cells.contains(head));
  }

  static Stream<Arguments> uprightSleepers() {
    return Direction.Type.HORIZONTAL.stream()
        .flatMap(
            facing -> Stream.of(1.8F, 1.95F, 2.0F).map(height -> Arguments.of(facing, height)));
  }

  @ParameterizedTest(name = "facing {0}, {1} tall")
  @MethodSource("uprightSleepers")
  @DisplayName("an upright sleeper's box sits wholly inside the standing coffin's shape")
  void fitsInsideTheStandingCoffin(final Direction facing, final float height) {
    final EntityDimensions standing =
        EntityDimensions.changing(0.6F, height).withEyeHeight(height * 0.9F);
    final EntityDimensions fitted = CoffinLayout.uprightSleeper(standing);
    assertEquals(standing.height(), fitted.height(), EPSILON);
    assertEquals(standing.eyeHeight(), fitted.eyeHeight(), EPSILON);
    final BlockPos head = AT;
    final Box body = fitted.getBoxAt(CoffinLayout.restingSpot(head, facing, true)).contract(1.0E-3);
    final VoxelShape coffin =
        VoxelShapes.union(
            CoffinShape.of(BedPart.HEAD, facing, true)
                .offset(head.getX(), head.getY(), head.getZ()),
            CoffinShape.of(BedPart.FOOT, facing, true)
                .offset(head.getX(), head.getY() - 1, head.getZ()));
    assertFalse(
        VoxelShapes.matchesAnywhere(VoxelShapes.cuboid(body), coffin, BooleanBiFunction.ONLY_FIRST),
        "the body " + body + " pokes out of the coffin");
  }
}
