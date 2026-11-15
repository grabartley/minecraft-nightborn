package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PlayerFeedingGameTest implements FabricGameTest {

  static final String PVP_OFF = "pvp-off";
  static final String PVP_ON = "pvp-on";
  static final String HELD_BITE = "pvp-on-held-bite";
  static final int FIRST_DRINK = TestPlayers.READY + 25;
  static final String ARENA = "nightborn:vampire_arena";

  @BeforeBatch(batchId = PVP_OFF)
  public void pvpOff(final ServerWorld world) {
    BatchPvp.set(world.getServer(), false);
  }

  @AfterBatch(batchId = PVP_OFF)
  public void restoreAfterPvpOff(final ServerWorld world) {
    BatchPvp.restore(world.getServer());
  }

  @BeforeBatch(batchId = PVP_ON)
  public void pvpOn(final ServerWorld world) {
    BatchPvp.set(world.getServer(), true);
  }

  @AfterBatch(batchId = PVP_ON)
  public void restoreAfterPvpOn(final ServerWorld world) {
    BatchPvp.restore(world.getServer());
  }

  @BeforeBatch(batchId = HELD_BITE)
  public void pvpOnAndNoResisting(final ServerWorld world) {
    FeedingSessions.clearAll();
    BatchPvp.set(world.getServer(), true);
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(FeedingSettings.RESIST_CHANCE, 0));
  }

  @AfterBatch(batchId = HELD_BITE)
  public void restoreAfterHeldBite(final ServerWorld world) {
    FeedingSessions.clearAll();
    BatchPvp.restore(world.getServer());
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(FeedingSettings.RESIST_CHANCE, FeedingSettings.RESIST_CHANCE.defaultValue()));
  }

  @GameTest(templateName = ARENA, batchId = HELD_BITE, tickLimit = TestPlayers.READY + 80)
  public void switchingToTurningMidBiteDoesNotTurn(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            hold(context, vampire, human);
          }
        });
    context.runAtTick(
        FIRST_DRINK,
        () -> {
          context.assertTrue(human.getHealth() < human.getMaxHealth(), "the first drink landed");
          TurningBite.set(vampire, true);
        });
    context.runAtTick(
        TestPlayers.READY + 70,
        () -> {
          context.assertTrue(FeedingSessions.isFeeding(vampire), "the same bite held on");
          context.assertFalse(
              BiteSicknessAttachment.isSick(human), "a bite that began feeding never turns");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = HELD_BITE, tickLimit = TestPlayers.READY + 80)
  public void switchingToFeedingMidBiteKeepsTheSickness(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    TurningBite.set(vampire, true);
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            hold(context, vampire, human);
          }
        });
    context.runAtTick(
        FIRST_DRINK,
        () -> {
          context.assertTrue(BiteSicknessAttachment.isSick(human), "the turning bite took");
          TurningBite.set(vampire, false);
        });
    context.runAtTick(
        TestPlayers.READY + 70,
        () -> {
          context.assertTrue(
              BiteSicknessAttachment.isSick(human), "switching back does not undo the bite");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP_OFF, tickLimit = TestPlayers.READY + 40)
  public void withPvpOffAVampireCannotBiteAPlayer(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            hold(context, vampire, human);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () -> {
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the bite is refused");
          context.assertEquals(human.getHealth(), human.getMaxHealth(), "the human is unhurt");
          context.assertFalse(BiteSicknessAttachment.isSick(human), "and not sick");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP_ON, tickLimit = TestPlayers.READY + 40)
  public void withPvpOnAFeedingBiteNeverStartsBiteSickness(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY) {
            hold(context, vampire, human);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () -> {
          context.assertTrue(human.getHealth() < human.getMaxHealth(), "the human was bitten");
          context.assertFalse(BiteSicknessAttachment.isSick(human), "a feeding bite never turns");
          TestPlayers.finish(context, vampire, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = PVP_ON, tickLimit = TestPlayers.READY + 40)
  public void withPvpOnATurningBiteStartsBiteSickness(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    TurningBite.set(vampire, true);
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY && !BiteSicknessAttachment.isSick(human)) {
            hold(context, vampire, human);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 30,
        () -> {
          context.assertTrue(BiteSicknessAttachment.isSick(human), "the bite started the sickness");
          context.assertTrue(human.getHealth() < human.getMaxHealth(), "the human was bitten");
          TestPlayers.finish(context, vampire, human);
        });
  }

  private static void hold(
      final TestContext context, final ServerPlayerEntity vampire, final ServerPlayerEntity human) {
    vampire.setSneaking(true);
    UseEntityCallback.EVENT
        .invoker()
        .interact(vampire, context.getWorld(), Hand.MAIN_HAND, human, null);
  }
}
