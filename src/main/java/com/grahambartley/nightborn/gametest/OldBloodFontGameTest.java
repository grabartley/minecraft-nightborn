package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.oldblood.OldBloodBlocks;
import com.grahambartley.nightborn.oldblood.OldBloodFontBlock;
import com.grahambartley.nightborn.oldblood.OldBloodFontBlockEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.SicknessStage;
import com.grahambartley.nightborn.sickness.SicknessTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class OldBloodFontGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String REFILL_BATCH = "old-blood-refill";
  static final BlockPos FONT = new BlockPos(4, 2, 4);

  private static int refillDaysBefore;

  @BeforeBatch(batchId = REFILL_BATCH)
  public void refillInADay(final ServerWorld world) {
    refillDaysBefore = ServerConfigService.current().turning().oldBloodRefillDays();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(TurningSettings.OLD_BLOOD_REFILL_DAYS, 1));
  }

  @AfterBatch(batchId = REFILL_BATCH)
  public void restoreRefill(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(TurningSettings.OLD_BLOOD_REFILL_DAYS, refillDaysBefore));
  }

  private static OldBloodFontBlockEntity font(final TestContext context) {
    context.setBlockState(FONT, OldBloodBlocks.FONT.getDefaultState());
    return context.getBlockEntity(FONT);
  }

  private static void drink(final TestContext context, final ServerPlayerEntity player) {
    player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    context.useBlock(FONT, player);
  }

  private static boolean filled(final TestContext context) {
    return context.getBlockState(FONT).get(OldBloodFontBlock.LEVEL).full();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aHumanWhoDrinksOldBloodFallsSickAndTurnsADayLater(final TestContext context) {
    final OldBloodFontBlockEntity font = font(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(filled(context), "a new font is full");
          drink(context, human);
          context.assertTrue(BiteSicknessAttachment.isSick(human), "the old blood starts it");
          context.assertFalse(font.full(), "one drink empties the font");
          context.assertFalse(filled(context), "the font shows it is dry");
          SicknessTicker.tick(
              human, ServerConfigService.current().turning(), SicknessStage.TICKS_PER_DAY);
          context.assertTrue(Vampires.isVampire(human), "a day later the human turns");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aFontCannotBeEmptiedTwiceAndGivesAVampireNothing(final TestContext context) {
    final OldBloodFontBlockEntity font = font(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity first = TestPlayers.human(context, new BlockPos(4, 2, 6));
    final ServerPlayerEntity second = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          drink(context, vampire);
          context.assertTrue(font.full(), "a vampire takes nothing from the font");
          drink(context, first);
          drink(context, second);
          context.assertTrue(BiteSicknessAttachment.isSick(first), "the first drinker is sick");
          context.assertFalse(BiteSicknessAttachment.isSick(second), "the dry font gives nothing");
          drink(context, first);
          context.assertFalse(font.full(), "it stays dry");
          TestPlayers.finish(context, vampire, first, second);
        });
  }

  @GameTest(templateName = ARENA, batchId = REFILL_BATCH, tickLimit = 20)
  public void aDryFontRefillsInTwiceItsDaysWithNoHousehold(final TestContext context) {
    final OldBloodFontBlockEntity font = font(context);
    final long now = context.getWorld().getTime();
    font.drain(now - SicknessStage.TICKS_PER_DAY);
    font.refill(context.getWorld());
    context.assertFalse(font.full(), "one day is not enough with no household");
    font.drain(now - 2L * SicknessStage.TICKS_PER_DAY);
    font.refill(context.getWorld());
    context.assertTrue(font.full(), "two days refill it with no household");
    context.assertTrue(filled(context), "the font shows it is full again");
    context.complete();
  }
}
