package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.decor.ChairBlock;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DrapeBlock;
import com.grahambartley.nightborn.decor.LightableFurnitureBlock;
import com.grahambartley.nightborn.decor.SeatEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

public class DecorGameTest implements FabricGameTest {

  private static final String ROOM = "nightborn:decor_room";
  private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
  private static final BlockPos ON_FLOOR = FLOOR.up();
  private static final BlockPos CEILING = new BlockPos(2, 3, 2);
  private static final BlockPos UNDER_CEILING = CEILING.down();

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void flintAndSteelLightsACandelabraAndWearsTheFlint(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(ON_FLOOR, DecorBlocks.CANDELABRA.getDefaultState());
    final PlayerEntity player = holding(context, new ItemStack(Items.FLINT_AND_STEEL));

    context.useBlock(ON_FLOOR, player);

    final BlockState state = context.getBlockState(ON_FLOOR);
    context.assertTrue(state.get(LightableFurnitureBlock.LIT), "candelabra should be lit");
    context.assertEquals(12, state.getLuminance(), "lit candelabra light");
    context.assertEquals(
        1, player.getMainHandStack().getDamage(), "flint and steel should take one use");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void anEmptyHandDousesALitCandelabra(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(
        ON_FLOOR, DecorBlocks.CANDELABRA.getDefaultState().with(LightableFurnitureBlock.LIT, true));

    context.useBlock(ON_FLOOR, holding(context, ItemStack.EMPTY));

    context.assertFalse(
        context.getBlockState(ON_FLOOR).get(LightableFurnitureBlock.LIT),
        "candelabra should be out");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aFireChargeLightsAHangingChandelierAndIsSpent(final TestContext context) {
    context.setBlockState(CEILING, Blocks.STONE);
    context.setBlockState(UNDER_CEILING, DecorBlocks.CHANDELIER.getDefaultState());
    final PlayerEntity player = holding(context, new ItemStack(Items.FIRE_CHARGE, 2));

    context.useBlock(UNDER_CEILING, player);

    final BlockState state = context.getBlockState(UNDER_CEILING);
    context.assertTrue(state.get(LightableFurnitureBlock.LIT), "chandelier should be lit");
    context.assertEquals(15, state.getLuminance(), "lit chandelier light");
    context.assertEquals(1, player.getMainHandStack().getCount(), "one fire charge spent");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aChandelierFallsWhenItsCeilingIsRemoved(final TestContext context) {
    context.setBlockState(CEILING, Blocks.STONE);
    context.setBlockState(UNDER_CEILING, DecorBlocks.CHANDELIER.getDefaultState());

    context.setBlockState(CEILING, Blocks.AIR);

    context.expectBlock(Blocks.AIR, UNDER_CEILING);
    context.expectItemAt(DecorBlocks.CHANDELIER.asItem(), UNDER_CEILING, 1.5);
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aCandelabraCannotStandOnAir(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.AIR);
    context.assertFalse(
        DecorBlocks.CANDELABRA
            .getDefaultState()
            .canPlaceAt(context.getWorld(), context.getAbsolutePos(ON_FLOOR)),
        "candelabra needs a floor under it");
    context.setBlockState(FLOOR, Blocks.STONE);
    context.assertTrue(
        DecorBlocks.CANDELABRA
            .getDefaultState()
            .canPlaceAt(context.getWorld(), context.getAbsolutePos(ON_FLOOR)),
        "candelabra stands on a floor");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void openingOneDrapeDrawsTheWholeColumn(final TestContext context) {
    final BlockState drape =
        DecorBlocks.BURGUNDY_VELVET_DRAPE
            .getDefaultState()
            .with(DrapeBlock.FACING, Direction.NORTH);
    context.setBlockState(ON_FLOOR, drape);
    context.setBlockState(ON_FLOOR.up(), drape);

    context.useBlock(ON_FLOOR, holding(context, ItemStack.EMPTY));

    final BlockState lower = context.getBlockState(ON_FLOOR);
    final BlockState upper = context.getBlockState(ON_FLOOR.up());
    context.assertTrue(lower.get(DrapeBlock.OPEN) && upper.get(DrapeBlock.OPEN), "both open");
    context.assertFalse(lower.get(DrapeBlock.TOP), "lower drape carries no rod");
    context.assertTrue(upper.get(DrapeBlock.TOP), "upper drape carries the rod");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void drapesFacingDifferentWaysDoNotJoin(final TestContext context) {
    context.setBlockState(
        ON_FLOOR,
        DecorBlocks.EMERALD_VELVET_DRAPE
            .getDefaultState()
            .with(DrapeBlock.FACING, Direction.NORTH));
    context.setBlockState(
        ON_FLOOR.up(),
        DecorBlocks.EMERALD_VELVET_DRAPE.getDefaultState().with(DrapeBlock.FACING, Direction.EAST));

    context.useBlock(ON_FLOOR, holding(context, ItemStack.EMPTY));

    context.assertTrue(context.getBlockState(ON_FLOOR).get(DrapeBlock.OPEN), "lower open");
    context.assertFalse(context.getBlockState(ON_FLOOR.up()).get(DrapeBlock.OPEN), "upper shut");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 40)
  public void sittingOnAChairSeatsThePlayerAndStandingRemovesTheSeat(final TestContext context) {
    context.setBlockState(
        ON_FLOOR,
        DecorBlocks.ORNATE_CHAIR.getDefaultState().with(ChairBlock.FACING, Direction.SOUTH));
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

    context.useBlock(ON_FLOOR, player);

    context.assertTrue(player.getVehicle() instanceof SeatEntity, "player should be seated");
    final SeatEntity seat = (SeatEntity) player.getVehicle();
    context.runAtTick(
        5,
        () -> {
          player.stopRiding();
        });
    context.runAtTick(
        10,
        () -> {
          context.assertTrue(seat.isRemoved(), "an empty seat should go away");
          context.complete();
        });
  }

  @GameTest(templateName = ROOM, tickLimit = 40)
  public void aSecondPlayerCannotTakeAnOccupiedChair(final TestContext context) {
    context.setBlockState(ON_FLOOR, DecorBlocks.ORNATE_CHAIR.getDefaultState());
    final ServerPlayerEntity first = context.createMockCreativeServerPlayerInWorld();
    final ServerPlayerEntity second = context.createMockCreativeServerPlayerInWorld();

    context.useBlock(ON_FLOOR, first);
    context.useBlock(ON_FLOOR, second);

    context.assertTrue(first.getVehicle() instanceof SeatEntity, "first player seated");
    context.assertFalse(second.hasVehicle(), "second player stays standing");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 40)
  public void breakingAChairUnseatsItsSitter(final TestContext context) {
    context.setBlockState(ON_FLOOR, DecorBlocks.ORNATE_CHAIR.getDefaultState());
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    context.useBlock(ON_FLOOR, player);

    context.runAtTick(5, () -> context.setBlockState(ON_FLOOR, Blocks.AIR));
    context.runAtTick(
        10,
        () -> {
          context.assertFalse(player.hasVehicle(), "sitter should be on their feet");
          context.complete();
        });
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aLitCandelabraStaysLitWhenClickedWithAnItem(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(
        ON_FLOOR, DecorBlocks.CANDELABRA.getDefaultState().with(LightableFurnitureBlock.LIT, true));

    context.useBlock(ON_FLOOR, holding(context, new ItemStack(Items.STICK)));

    context.assertTrue(
        context.getBlockState(ON_FLOOR).get(LightableFurnitureBlock.LIT), "still lit");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void openingTheMiddleOfAColumnDrawsItAll(final TestContext context) {
    final BlockState drape =
        DecorBlocks.EMERALD_VELVET_DRAPE.getDefaultState().with(DrapeBlock.FACING, Direction.NORTH);
    context.setBlockState(FLOOR, drape);
    context.setBlockState(ON_FLOOR, drape);
    context.setBlockState(CEILING, drape);

    context.useBlock(ON_FLOOR, holding(context, ItemStack.EMPTY));

    for (final BlockPos at : new BlockPos[] {FLOOR, ON_FLOOR, CEILING}) {
      context.assertTrue(context.getBlockState(at).get(DrapeBlock.OPEN), "open at " + at);
    }
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aDrapeHungUnderAnOpenColumnJoinsItOpen(final TestContext context) {
    final BlockState open =
        DecorBlocks.BURGUNDY_VELVET_DRAPE
            .getDefaultState()
            .with(DrapeBlock.FACING, Direction.NORTH)
            .with(DrapeBlock.OPEN, true);
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(CEILING, open);
    final PlayerEntity player = holding(context, new ItemStack(DecorBlocks.BURGUNDY_VELVET_DRAPE));
    player.setYaw(0.0f);

    context.useStackOnBlock(player, player.getMainHandStack(), FLOOR, Direction.UP);

    final BlockState placed = context.getBlockState(ON_FLOOR);
    context.assertTrue(placed.isOf(DecorBlocks.BURGUNDY_VELVET_DRAPE), "drape placed");
    context.assertTrue(placed.get(DrapeBlock.OPEN), "joins the column open");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aSneakingPlayerDoesNotSitDown(final TestContext context) {
    context.setBlockState(ON_FLOOR, DecorBlocks.ORNATE_CHAIR.getDefaultState());
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.setSneaking(true);

    context.useBlock(ON_FLOOR, player);

    context.assertFalse(player.hasVehicle(), "sneaking player stays standing");
    context.complete();
  }

  private static PlayerEntity holding(final TestContext context, final ItemStack stack) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    return player;
  }
}
