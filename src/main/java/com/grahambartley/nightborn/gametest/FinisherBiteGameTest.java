package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class FinisherBiteGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "finisher-bite";

  @BeforeBatch(batchId = BATCH)
  public void clearBites(final ServerWorld world) {
    FeedingSessions.clearAll();
  }

  @AfterBatch(batchId = BATCH)
  public void clearBitesAfter(final ServerWorld world) {
    FeedingSessions.clearAll();
  }

  private static CowEntity hurtCow(final TestContext context, final float health) {
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4));
    cow.setAiDisabled(true);
    cow.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10.0);
    cow.setHealth(health);
    return cow;
  }

  private static ActionResult bite(
      final TestContext context, final ServerPlayerEntity vampire, final CowEntity cow) {
    vampire.setSneaking(false);
    vampire.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
    return UseEntityCallback.EVENT
        .invoker()
        .interact(vampire, context.getWorld(), Hand.MAIN_HAND, cow, null);
  }

  private static int drainTicks() {
    return ServerConfigService.current().feeding().finisherDrainTicks();
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 60)
  public void aFinisherBiteDrainsABadlyHurtEnemyAndHealsTheVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final CowEntity cow = hurtCow(context, 2.0F);
    final int[] bloodBefore = new int[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setHealth(10.0F);
          BloodAttachment.set(vampire, 5);
          bloodBefore[0] = BloodAttachment.get(vampire);
          bite(context, vampire, cow);
          context.assertTrue(FeedingSessions.finishing(vampire), "the finisher takes hold");
          context.assertTrue(
              AttackEntityCallback.EVENT
                      .invoker()
                      .interact(vampire, context.getWorld(), Hand.MAIN_HAND, cow, null)
                  == ActionResult.FAIL,
              "the vampire cannot strike while it drinks");
        });
    context.runAtTick(
        TestPlayers.READY + drainTicks() + 2L,
        () -> {
          context.assertFalse(cow.isAlive(), "the enemy is drained");
          context.assertTrue(vampire.getHealth() >= 14.0F, "the vampire healed four");
          context.assertTrue(
              BloodAttachment.get(vampire) > bloodBefore[0], "the vampire gained blood");
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the bite is over");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aFinisherNeverStartsAboveTheThreshold(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final CowEntity cow = hurtCow(context, 2.5F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          bite(context, vampire, cow);
          context.assertFalse(
              FeedingSessions.isFeeding(vampire), "a quarter of health is too much");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 60)
  public void aBlowBreaksTheFinisher(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final CowEntity cow = hurtCow(context, 2.0F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          bite(context, vampire, cow);
          context.assertTrue(FeedingSessions.finishing(vampire), "the finisher takes hold");
          vampire.damage(context.getWorld().getDamageSources().generic(), 2.0F);
          context.assertFalse(FeedingSessions.isFeeding(vampire), "the blow breaks the bite");
        });
    context.runAtTick(
        TestPlayers.READY + drainTicks() + 2L,
        () -> {
          context.assertTrue(cow.isAlive(), "the enemy lives");
          TestPlayers.finish(context, vampire);
        });
  }
}
