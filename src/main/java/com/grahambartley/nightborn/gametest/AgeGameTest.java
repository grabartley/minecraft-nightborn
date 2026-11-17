package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeDeeds;
import com.grahambartley.nightborn.age.AgeProgress;
import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class AgeGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String NIGHT = "age-night";
  static final int ADULT = 1;
  static final int ELDER = 2;
  static final float FLEDGLING_HEALTH = 40.0F;

  @BeforeBatch(batchId = NIGHT)
  public void freezeAtMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT)
  public void releaseClock(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aFledglingWithNightsBloodAndTheDeedBecomesAnAdult(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 2, 4));
    villager.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(AgeAttachment.get(vampire).rank(), 0, "starts as a fledgling");
          AgeAttachment.set(vampire, new AgeProgress(0, 10, 100, 0));
          AgeDeeds.drained(vampire, villager);
          context.assertEquals(AgeAttachment.get(vampire).rank(), ADULT, "reaches adult");
          context.assertEquals(
              vampire.getMaxHealth(), FLEDGLING_HEALTH + 4.0F, "an adult has 4 more health");
          context.assertEquals(BloodCapacity.of(vampire), 22, "an adult has a larger blood bar");
          context.assertEquals(StageRanks.of(vampire), ADULT, "an adult pays hunters as an adult");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aDeedDoneEarlyCountsWhenTheStageOpens(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(2, 2, 4));
    villager.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          AgeDeeds.drained(vampire, villager);
          context.assertEquals(AgeAttachment.get(vampire).rank(), 0, "the stage is not open yet");
          context.assertTrue(AgeAttachment.get(vampire).deedDone(ADULT), "the deed is kept");
          AgeAttachment.set(vampire, AgeAttachment.get(vampire).lived(10));
          Ageing.drank(vampire, 100);
          context.assertEquals(AgeAttachment.get(vampire).rank(), ADULT, "the deed counts now");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void endingAGuildMasterIsTheEldersDeedAndNoStageIsSkipped(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(2, 2, 4));
    master.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          AgeAttachment.set(vampire, new AgeProgress(0, 100, 1500, 0));
          master.damage(vampire.getDamageSources().playerAttack(vampire), 1000.0F);
          context.assertTrue(master.isDead(), "the Guild Master is ended");
          context.assertTrue(
              AgeAttachment.get(vampire).deedDone(ELDER), "the elder's deed is done");
          context.assertEquals(
              AgeAttachment.get(vampire).rank(), 0, "elder is not reached before adult");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT, tickLimit = TestPlayers.READY + 20)
  public void aNightSleepersSkipCountsWhole(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertEquals(AgeAttachment.get(vampire).nights(), 0, "no night lived yet");
          vampire.removeAttached(AgeAttachment.WATCH);
          vampire.setAttached(AgeAttachment.CLOCK, 13000L);
          Ageing.tick(vampire);
          context.assertEquals(AgeAttachment.get(vampire).nights(), 0, "half a night is not most");
          vampire.setAttached(AgeAttachment.CLOCK, 13000L - 24000L);
          vampire.removeAttached(AgeAttachment.WATCH);
          context.getWorld().setTimeOfDay(24000L);
          Ageing.tick(vampire);
          context.getWorld().setTimeOfDay(WorldClock.MIDNIGHT);
          context.assertEquals(AgeAttachment.get(vampire).nights(), 1, "the skipped night counts");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void anOperatorSetsAStageAndACureTakesItAway(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(Ageing.setRank(vampire, ELDER), "the stage is set");
          context.assertEquals(
              vampire.getMaxHealth(), FLEDGLING_HEALTH + 8.0F, "an elder has 8 more health");
          BloodAttachment.set(vampire, 24);
          context.assertEquals(BloodAttachment.get(vampire), 24, "an elder holds 24 blood");
          VampireTransitions.makeHuman(vampire);
          context.assertEquals(AgeAttachment.get(vampire), AgeProgress.EMPTY, "age is gone");
          context.assertEquals(BloodCapacity.of(vampire), BloodAttachment.FULL, "a plain bar");
          context.assertEquals(vampire.getMaxHealth(), 20.0F, "a human's health");
          TestPlayers.finish(context, vampire);
        });
  }
}
