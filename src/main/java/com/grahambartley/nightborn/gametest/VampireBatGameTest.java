package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class VampireBatGameTest implements FabricGameTest {

  static final String COVER_BATCH = "vampire-bat-cover";
  static final String NOON_BATCH = "vampire-bat-noon";
  static final int HUNT_WINDOW = 80;

  private int turnChanceBefore;

  @BeforeBatch(batchId = COVER_BATCH)
  public void everyBatBiteTurns(final ServerWorld world) {
    turnChanceBefore = ServerConfigService.current().turning().vampireBatTurnChance();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(TurningSettings.VAMPIRE_BAT_TURN_CHANCE, 100));
  }

  @AfterBatch(batchId = COVER_BATCH)
  public void restoreBatTurnChance(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(TurningSettings.VAMPIRE_BAT_TURN_CHANCE, turnChanceBefore));
  }

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    VampireMobGameTest.pinTime(world, VampireMobGameTest.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    VampireMobGameTest.unpinTime(world);
  }

  @GameTest(
      templateName = VampireMobGameTest.ARENA,
      batchId = COVER_BATCH,
      tickLimit = TestPlayers.READY + HUNT_WINDOW)
  public void aVampireBatUnderCoverSwoopsOnAHumanAndCanTurnThem(final TestContext context) {
    final VampireBatEntity bat = hunter(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() > TestPlayers.READY && human.getHealth() < human.getMaxHealth()) {
            context.assertTrue(
                BiteSicknessAttachment.isSick(human), "the bat's bite starts sickness");
            context.assertTrue(bat.isAlive(), "the bat bit and lived");
            TestPlayers.finish(context, human);
          }
        });
  }

  @GameTest(
      templateName = VampireMobGameTest.YARD,
      batchId = NOON_BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + HUNT_WINDOW)
  public void aVampireBatInTheOpenLeavesHumansAloneByDay(final TestContext context) {
    hunter(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY + HUNT_WINDOW - 2,
        () -> {
          context.assertEquals(
              human.getHealth(), human.getMaxHealth(), "no bite in the open by day");
          TestPlayers.finish(context, human);
        });
  }

  private static VampireBatEntity hunter(final TestContext context) {
    final VampireBatEntity bat =
        context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(4, 3, 4));
    bat.setRoosting(false);
    return bat;
  }
}
