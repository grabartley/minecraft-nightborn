package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.feeding.FeedingSessions;
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
  static final String ARENA = "nightborn:vampire_arena";

  @BeforeBatch(batchId = PVP_OFF)
  public void pvpOff(final ServerWorld world) {
    world.getServer().setPvpEnabled(false);
  }

  @AfterBatch(batchId = PVP_OFF)
  public void pvpBackOn(final ServerWorld world) {
    world.getServer().setPvpEnabled(true);
  }

  @BeforeBatch(batchId = PVP_ON)
  public void pvpOn(final ServerWorld world) {
    world.getServer().setPvpEnabled(true);
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
  public void withPvpOnAVampiresBiteStartsBiteSickness(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
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
