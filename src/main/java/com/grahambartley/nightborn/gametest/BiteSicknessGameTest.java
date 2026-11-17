package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.sickness.SicknessStage;
import com.grahambartley.nightborn.sickness.SicknessTicker;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class BiteSicknessGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String CLOCK_BATCH = "sickness-clock";
  static final String EFFECT_BATCH = "sickness-effect";
  static final int NIGHT_SKIPPED = 7000;

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aBiteStartsTheSicknessAndShowsIt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.assertTrue(
        BiteSickness.start(human, ServerConfigService.current().turning()), "the bite takes");
    context.assertEquals(
        BiteSicknessAttachment.ticksRun(human), 0, "the sickness starts from no time run");
    context.runAtTick(
        25,
        () -> {
          context.assertTrue(
              human.hasStatusEffect(BiteSicknessEffect.ENTRY), "the sickness shows as an effect");
          context.assertTrue(
              human.hasStatusEffect(StatusEffects.HUNGER), "the first day brings hunger");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void anotherBiteDoesNotRestartTheSickness(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    BiteSickness.start(human, ServerConfigService.current().turning());
    BiteSicknessAttachment.set(human, 1000);
    context.assertFalse(
        BiteSickness.start(human, ServerConfigService.current().turning()), "no second start");
    context.assertEquals(BiteSicknessAttachment.ticksRun(human), 1000, "time run is unchanged");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aGoldenAppleUnderWeaknessCures(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    BiteSickness.start(human, ServerConfigService.current().turning());
    human.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600));
    eatGoldenApple(context, human);
    context.assertFalse(BiteSicknessAttachment.isSick(human), "the golden apple cured it");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aGoldenAppleWithoutWeaknessDoesNotCure(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    BiteSickness.start(human, ServerConfigService.current().turning());
    eatGoldenApple(context, human);
    context.assertTrue(BiteSicknessAttachment.isSick(human), "still sick without Weakness");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void theSicknessRunningOutTurnsAndKeepsTheInventory(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    human.getInventory().insertStack(new ItemStack(Items.DIAMOND, 5));
    BiteSickness.start(human, ServerConfigService.current().turning());
    BiteSicknessAttachment.set(
        human,
        SicknessStage.totalTicks(ServerConfigService.current().turning().sicknessDays()) - 3);
    context.runAtTick(
        15,
        () -> {
          context.assertTrue(Vampires.isVampire(human), "the player turned");
          context.assertFalse(BiteSicknessAttachment.isSick(human), "turning ends the sickness");
          context.assertEquals(human.getInventory().count(Items.DIAMOND), 5, "inventory is kept");
          context.assertEquals(
              BloodAttachment.get(human), BloodAttachment.FULL, "a full blood bar");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aShorterLengthTurnsAPlayerAlreadyPastIt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final TurningSettings threeDays = withDays(3);
    BiteSickness.start(human, threeDays);
    BiteSicknessAttachment.set(human, 30000);
    SicknessTicker.tick(human, threeDays, 1);
    context.assertFalse(Vampires.isVampire(human), "still sick under three days");
    SicknessTicker.tick(human, withDays(1), 1);
    context.assertTrue(Vampires.isVampire(human), "one day has already run, so the player turns");
    TestPlayers.finish(context, human);
  }

  @BeforeBatch(batchId = CLOCK_BATCH)
  public void freezeTheClock(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = CLOCK_BATCH)
  public void releaseTheClock(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = EFFECT_BATCH)
  public void holdTheClock(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = EFFECT_BATCH)
  public void letTheClockGo(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = CLOCK_BATCH, tickLimit = 40)
  public void aSkippedNightMovesTheSicknessOn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    BiteSickness.start(human, ServerConfigService.current().turning());
    final int[] before = new int[1];
    context.runAtTick(
        10,
        () -> {
          before[0] = BiteSicknessAttachment.ticksRun(human);
          context.getWorld().setTimeOfDay(WorldClock.MIDNIGHT + NIGHT_SKIPPED);
        });
    context.runAtTick(
        15,
        () -> {
          final int grown = BiteSicknessAttachment.ticksRun(human) - before[0];
          context.assertTrue(
              grown >= NIGHT_SKIPPED, "the skipped night counted toward it, grew " + grown);
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = EFFECT_BATCH, tickLimit = 80)
  public void theEffectShrinksWhenLessTimeIsLeft(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final TurningSettings turning = ServerConfigService.current().turning();
    BiteSickness.start(human, turning);
    context.runAtTick(
        25,
        () -> {
          context.assertTrue(
              human.hasStatusEffect(BiteSicknessEffect.ENTRY), "the sickness shows first");
          BiteSicknessAttachment.set(
              human, SicknessStage.totalTicks(turning.sicknessDays()) / (SicknessStage.STAGES * 2));
        });
    context.runAtTick(
        60,
        () -> {
          final StatusEffectInstance shown = human.getStatusEffect(BiteSicknessEffect.ENTRY);
          context.assertTrue(
              shown != null && shown.getAmplifier() == 0, "still the first stage of the sickness");
          final int left =
              SicknessStage.ticksLeft(
                  BiteSicknessAttachment.ticksRun(human), turning.sicknessDays());
          context.assertTrue(
              shown != null && Math.abs(shown.getDuration() - left) <= 40,
              "the effect shows the time really left, "
                  + (shown == null ? "none" : shown.getDuration())
                  + " against "
                  + left);
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void nothingCuresAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    vampire.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 600));
    eatGoldenApple(context, vampire);
    context.assertTrue(Vampires.isVampire(vampire), "a golden apple does not cure a vampire");
    context.assertFalse(
        BiteSickness.start(vampire, ServerConfigService.current().turning()),
        "a vampire cannot catch bite sickness");
    TestPlayers.finish(context, vampire);
  }

  private static TurningSettings withDays(final int days) {
    return ServerConfigService.current().with(TurningSettings.SICKNESS_DAYS, days).turning();
  }

  private static void eatGoldenApple(final TestContext context, final ServerPlayerEntity player) {
    final ItemStack apple = new ItemStack(Items.GOLDEN_APPLE);
    player.eatFood(context.getWorld(), apple, apple.get(DataComponentTypes.FOOD));
  }
}
