package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CoffinLidGameTest implements FabricGameTest {

  static final String NOON = "coffin-lid-noon";
  static final int SUN_REACHES = 40;

  @BeforeBatch(batchId = NOON)
  public void pinNoon(final ServerWorld world) {
    CoffinGameTest.pin(world, CoffinGameTest.NOON, false);
  }

  @AfterBatch(batchId = NOON)
  public void releaseNoon(final ServerWorld world) {
    CoffinGameTest.release(world);
  }

  @GameTest(
      templateName = CoffinGameTest.YARD,
      batchId = NOON,
      skyAccess = true,
      tickLimit = TestPlayers.READY + SUN_REACHES + 10)
  public void aHunterWhoOpensACoffinInTheSunBurnsAndWakesTheSleeper(final TestContext context) {
    final BlockPos head = CoffinGameTest.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(3, 2, 5));
    final float[] settled = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          vampire.extinguish();
          settled[0] = vampire.getHealth();
          hunter.setSneaking(true);
          context.useBlock(head, hunter);
          context.assertTrue(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the hunter opens the lid");
          context.assertTrue(vampire.isSleeping(), "opening the lid alone does not wake them");
        });
    context.runAtTick(
        TestPlayers.READY + SUN_REACHES,
        () -> {
          context.assertTrue(
              vampire.getHealth() < settled[0],
              "the sun reaches the open coffin: " + Diagnostics.of(vampire));
          context.assertFalse(
              vampire.isSleeping(), "the burn wakes the sleeper: " + Diagnostics.of(vampire));
          context.assertTrue(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the lid stays open as they rise");
          TestPlayers.finish(context, vampire, hunter);
        });
  }

  @GameTest(
      templateName = CoffinGameTest.YARD,
      batchId = NOON,
      tickLimit = TestPlayers.READY + SUN_REACHES + 10)
  public void anOpenLidInShadeLetsTheSleeperSleepOnAndShutsAgain(final TestContext context) {
    final BlockPos head = CoffinGameTest.coffin(context, new BlockPos(4, 2, 6), Direction.EAST);
    CoffinGameTest.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity other = TestPlayers.vampire(context, new BlockPos(6, 2, 4));
    final float[] settled = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.extinguish();
          other.extinguish();
          context.useBlock(head, vampire);
          settled[0] = vampire.getHealth();
          other.setSneaking(true);
          context.useBlock(head, other);
          context.assertTrue(
              context.getBlockState(head).get(CoffinBlock.OPEN),
              "a sneaking vampire opens an occupied coffin too");
        });
    context.runAtTick(
        TestPlayers.READY + SUN_REACHES,
        () -> {
          context.assertTrue(
              vampire.isSleeping() && vampire.getHealth() >= settled[0],
              "in shade the sleeper sleeps on unhurt: " + Diagnostics.of(vampire));
          context.useBlock(head, other);
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN), "sneak-using again shuts it");
          context.assertTrue(vampire.isSleeping(), "shutting the lid does not wake them");
          TestPlayers.finish(context, vampire, other);
        });
  }

  @GameTest(templateName = CoffinGameTest.YARD, batchId = NOON, tickLimit = TestPlayers.READY + 10)
  public void anOccupiedCoffinStaysShutToAPlainUseOrAFullHand(final TestContext context) {
    final BlockPos head = CoffinGameTest.coffin(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(3, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.extinguish();
          context.useBlock(head, vampire);
          context.useBlock(head, hunter);
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN), "a plain use is refused");
          hunter.setSneaking(true);
          hunter.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.STICK));
          context.useBlock(head, hunter);
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN),
              "a sneak-use with something in hand is refused");
          context.assertTrue(vampire.isSleeping(), "the sleeper is undisturbed");
          TestPlayers.finish(context, vampire, hunter);
        });
  }
}
