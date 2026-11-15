package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.sickness.SicknessStage;
import com.grahambartley.nightborn.vampire.Vampires;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class BiteSicknessGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

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
    BiteSickness.tick(human, threeDays, 1);
    context.assertFalse(Vampires.isVampire(human), "still sick under three days");
    BiteSickness.tick(human, withDays(1), 1);
    context.assertTrue(Vampires.isVampire(human), "one day has already run, so the player turns");
    TestPlayers.finish(context, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSkippedNightMovesTheSicknessOn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    final TurningSettings oneDay = withDays(1);
    BiteSickness.start(human, oneDay);
    BiteSicknessAttachment.set(human, 12000);
    BiteSickness.tick(human, oneDay, 11000);
    context.assertEquals(
        BiteSicknessAttachment.ticksRun(human), 23000, "the skipped night counts in full");
    BiteSickness.tick(human, oneDay, 1000);
    context.assertTrue(Vampires.isVampire(human), "the day ran out and the player turned");
    TestPlayers.finish(context, human);
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
    final TurningSettings current = ServerConfigService.current().turning();
    return new TurningSettings(
        current.biteTurnChance(),
        current.vampireMobTurnChance(),
        current.vampireBatTurnChance(),
        current.playersCanTurnPlayers(),
        days);
  }

  private static void eatGoldenApple(final TestContext context, final ServerPlayerEntity player) {
    final ItemStack apple = new ItemStack(Items.GOLDEN_APPLE);
    player.eatFood(context.getWorld(), apple, apple.get(DataComponentTypes.FOOD));
  }
}
