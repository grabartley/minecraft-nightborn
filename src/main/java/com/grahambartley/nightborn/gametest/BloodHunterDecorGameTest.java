package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.decor.BloodFountainBlock;
import com.grahambartley.nightborn.decor.BloodSplatterBlock;
import com.grahambartley.nightborn.decor.ChaliceBlock;
import com.grahambartley.nightborn.decor.ChaliceBlood;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.RackHooks;
import com.grahambartley.nightborn.decor.WallDecorBlock;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.gear.GearItems;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class BloodHunterDecorGameTest implements FabricGameTest {

  private static final String ROOM = "nightborn:decor_room";
  private static final BlockPos WALL = new BlockPos(2, 2, 3);
  private static final BlockPos HUNG = WALL.north();
  private static final BlockPos FLOOR = new BlockPos(2, 1, 2);
  private static final BlockPos ON_FLOOR = FLOOR.up();

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aWeaponRackHoldsTwoWeaponsAndLosesNone(final TestContext context) {
    context.setBlockState(WALL, Blocks.STONE);
    context.setBlockState(
        HUNG,
        DecorBlocks.WEAPON_RACK.getDefaultState().with(WallDecorBlock.FACING, Direction.NORTH));
    final ItemStack sword = new ItemStack(Items.IRON_SWORD);
    sword.setDamage(9);
    final PlayerEntity player = holding(context, sword.copy());

    context.useBlock(HUNG, player, atHook(context, 0));
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOW));
    context.useBlock(HUNG, player, atHook(context, 1));
    context.assertTrue(player.getMainHandStack().isEmpty(), "both weapons hang on the rack");

    context.useBlock(HUNG, player, atHook(context, 0));
    context.assertTrue(
        ItemStack.areEqual(sword, player.getMainHandStack()), "the same sword comes back");

    context.getWorld().breakBlock(context.getAbsolutePos(HUNG), true);
    context.assertEquals(1, dropped(context, Items.BOW), "the bow still on the rack drops");
    context.assertEquals(
        1, dropped(context, DecorBlocks.WEAPON_RACK.asItem()), "the rack itself drops");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aWeaponRackRefusesWhatIsNotAWeapon(final TestContext context) {
    context.setBlockState(WALL, Blocks.STONE);
    context.setBlockState(
        HUNG,
        DecorBlocks.WEAPON_RACK.getDefaultState().with(WallDecorBlock.FACING, Direction.NORTH));
    final PlayerEntity player = holding(context, new ItemStack(Items.BREAD));

    context.useBlock(HUNG, player, atHook(context, 0));

    context.assertEquals(Items.BREAD, player.getMainHandStack().getItem(), "bread stays in hand");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aChaliceKeepsTheBloodPouredIntoIt(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(ON_FLOOR, DecorBlocks.CHALICE.getDefaultState());
    final PlayerEntity player = holding(context, BloodBottleItem.of(BloodQuality.PLAYER));

    context.useBlock(ON_FLOOR, player);
    context.assertEquals(
        ChaliceBlood.PLAYER,
        context.getBlockState(ON_FLOOR).get(ChaliceBlock.BLOOD),
        "the chalice holds player blood");
    context.assertEquals(
        Items.GLASS_BOTTLE, player.getMainHandStack().getItem(), "the bottle comes back empty");

    context.useBlock(ON_FLOOR, player);
    context.assertEquals(
        ChaliceBlood.EMPTY, context.getBlockState(ON_FLOOR).get(ChaliceBlock.BLOOD), "emptied");
    context.assertTrue(
        player.getMainHandStack().isOf(BloodItems.BLOOD_BOTTLE)
            && BloodBottleItem.qualityOf(player.getMainHandStack()) == BloodQuality.PLAYER,
        "the same blood goes back into the bottle");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = TestPlayers.READY + 10)
  public void aVampireDrinksFromAFullChalice(final TestContext context) {
    context.setBlockState(FLOOR, Blocks.STONE);
    context.setBlockState(
        ON_FLOOR,
        DecorBlocks.CHALICE.getDefaultState().with(ChaliceBlock.BLOOD, ChaliceBlood.VILLAGER));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          BloodAttachment.set(vampire, 1);
          vampire.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);

          context.useBlock(ON_FLOOR, vampire);

          context.assertTrue(BloodAttachment.get(vampire) > 1, "the vampire drank");
          context.assertEquals(
              ChaliceBlood.EMPTY, context.getBlockState(ON_FLOOR).get(ChaliceBlock.BLOOD), "empty");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aRedstoneSignalStopsTheFountainUntilItEnds(final TestContext context) {
    context.setBlockState(ON_FLOOR, DecorBlocks.BLOOD_FOUNTAIN.getDefaultState());
    final BlockPos beside = ON_FLOOR.east();

    context.setBlockState(beside, Blocks.REDSTONE_BLOCK);
    context.assertFalse(
        context.getBlockState(ON_FLOOR).get(BloodFountainBlock.FLOWING), "stopped while powered");

    context.setBlockState(beside, Blocks.AIR);
    context.assertTrue(
        context.getBlockState(ON_FLOOR).get(BloodFountainBlock.FLOWING), "flows again");
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void splatterFallsWithItsWall(final TestContext context) {
    context.setBlockState(WALL, Blocks.STONE);
    context.setBlockState(
        HUNG,
        DecorBlocks.BLOOD_SPLATTER
            .getDefaultState()
            .with(WallMountedBlock.FACE, BlockFace.WALL)
            .with(WallMountedBlock.FACING, Direction.NORTH)
            .with(BloodSplatterBlock.SPLAT, 2));
    context.expectBlock(DecorBlocks.BLOOD_SPLATTER, HUNG);

    context.setBlockState(WALL, Blocks.AIR);

    context.expectBlock(Blocks.AIR, HUNG);
    context.complete();
  }

  @GameTest(templateName = ROOM, tickLimit = 20)
  public void aCrucifixHangsOnAWallAndBreaksBackIntoItself(final TestContext context) {
    context.setBlockState(WALL, Blocks.STONE);
    final PlayerEntity player = holding(context, new ItemStack(GearItems.SILVER_CRUCIFIX));

    context.useStackOnBlock(player, player.getMainHandStack(), WALL, Direction.NORTH);

    context.checkBlockState(
        HUNG,
        state ->
            state.isOf(GearBlocks.SILVER_WALL_CRUCIFIX)
                && state.get(WallDecorBlock.FACING) == Direction.NORTH,
        () -> "a silver crucifix hangs facing out from the wall");
    context.getWorld().breakBlock(context.getAbsolutePos(HUNG), true);
    context.assertEquals(1, dropped(context, GearItems.SILVER_CRUCIFIX), "the crucifix drops");
    context.complete();
  }

  private static PlayerEntity holding(final TestContext context, final ItemStack stack) {
    final PlayerEntity player = context.createMockPlayer(GameMode.SURVIVAL);
    player.setStackInHand(Hand.MAIN_HAND, stack);
    return player;
  }

  private static BlockHitResult atHook(final TestContext context, final int hook) {
    final BlockPos pos = context.getAbsolutePos(HUNG);
    final Vec3d at = Vec3d.of(pos).add(RackHooks.centre(Direction.NORTH, hook));
    return new BlockHitResult(at, Direction.NORTH, pos, false);
  }

  private static int dropped(final TestContext context, final Item item) {
    final List<ItemEntity> drops =
        context
            .getWorld()
            .getEntitiesByClass(
                ItemEntity.class,
                new Box(context.getAbsolutePos(HUNG)).expand(2),
                entity -> entity.getStack().isOf(item));
    return drops.stream().mapToInt(entity -> entity.getStack().getCount()).sum();
  }
}
