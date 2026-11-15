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
  private static final double SHUT_LID_INNER_FACE = 10.0 / 16.0;
  private static final double NEAR_PLANE = 0.05;
  private static final double PLAYER_LENGTH = 32 * 0.9375 / 16.0;
  private static final double SLEEP_SHIFT = 1.62 - 0.1;
  private static final double END_WALL = 0.5 / 16.0;
  private static final double HEAD_END = 1.0;
  private static final double CUSHION_TOP = 2.0 / 16.0;
  private static final double HEAD_HALF_DEPTH = 4 * 0.9375 / 16.0;

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
  @DisplayName("a flat sleeper lies between the end walls, off the floor and under the shut lid")
  void liesInsideAFlatCoffin(final Direction facing) {
    final Vec3d spot = CoffinLayout.restingSpot(AT, facing, false);
    final Vec3d shift = spot.subtract(Vec3d.ofBottomCenter(AT));
    final double towardsHead = shift.x * facing.getOffsetX() + shift.z * facing.getOffsetZ();
    final double headTop = towardsHead + PLAYER_LENGTH - SLEEP_SHIFT;
    final double feet = towardsHead - SLEEP_SHIFT;
    assertTrue(headTop < HEAD_END - END_WALL, "the head stays off the head end");
    assertTrue(feet > -1.5 + END_WALL, "the feet stay off the foot end");
    assertTrue(shift.y - HEAD_HALF_DEPTH >= CUSHION_TOP, "the head rests on the cushion");
    assertTrue(shift.y + HEAD_HALF_DEPTH < SHUT_LID_INNER_FACE, "the lid shuts over the face");
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "EAST", "SOUTH", "WEST"})
  @DisplayName(
      "a rise from a flat coffin pivots on the drawn sleeper's heels, inside the foot cell")
  void risesFromTheSleepersHeels(final Direction facing) {
    final Vec3d resting = CoffinLayout.restingSpot(AT, facing, false);
    final Vec3d heels = CoffinLayout.heels(AT, facing, 1.62F);
    final Vec3d shift = heels.subtract(Vec3d.ofBottomCenter(AT));
    final double towardsHead = shift.x * facing.getOffsetX() + shift.z * facing.getOffsetZ();
    final double sideways = shift.x * facing.getOffsetZ() - shift.z * facing.getOffsetX();
    final double restingTowardsHead =
        resting.subtract(Vec3d.ofBottomCenter(AT)).x * facing.getOffsetX()
            + resting.subtract(Vec3d.ofBottomCenter(AT)).z * facing.getOffsetZ();
    assertEquals(restingTowardsHead - SLEEP_SHIFT, towardsHead, 1.0E-6);
    assertEquals(0.0, sideways, EPSILON);
    assertEquals(resting.y, heels.y, EPSILON);
    assertTrue(towardsHead > -1.5 + END_WALL, "the heels stay off the foot end");
    assertTrue(towardsHead < -0.5, "the heels are in the foot cell");
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
    assertTrue(shift.y >= END_WALL, "the feet stay off the foot end");
    assertTrue(
        shift.y + PLAYER_LENGTH < 1.5 + HEAD_END - END_WALL, "the head stays under the head end");
    final double towardsWall = shift.x * facing.getOffsetX() + shift.z * facing.getOffsetZ();
    final double sideways = shift.x * facing.getOffsetZ() - shift.z * facing.getOffsetX();
    assertEquals(0.0, sideways, EPSILON);
    final double fromBackWall = 0.5 - towardsWall;
    assertTrue(fromBackWall - HEAD_HALF_DEPTH >= CUSHION_TOP, "the head stays off the back");
    assertTrue(
        fromBackWall + HEAD_HALF_DEPTH < SHUT_LID_INNER_FACE, "the face stays behind the lid");
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
    assertEquals(Math.min(height, 31.5F / 16.0F), fitted.height(), EPSILON);
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
