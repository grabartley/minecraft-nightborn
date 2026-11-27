package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.GuildOathOrder;
import com.grahambartley.nightborn.guild.GuildRevengeGoal;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.wheel.WheelEntry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class GuildMasterFightGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 5;
  static final int OATH_OVER = TestPlayers.READY + 8 * 20 + 5;

  private static GuildMasterEntity master(final TestContext context) {
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(3, 2, 3));
    master.setAiDisabled(true);
    return master;
  }

  private static VampireEntity vampire(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(5, 2, 3));
    vampire.setAiDisabled(true);
    return vampire;
  }

  @GameTest(templateName = ARENA, tickLimit = START + 5)
  public void aGuildMasterTakesAVampireAsAFoeButNeverAHunterOrAHuman(final TestContext context) {
    final GuildMasterEntity master = master(context);
    final VampireEntity vampire = vampire(context);
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(1, 2, 3));
    hunter.setAiDisabled(true);
    final PlayerEntity human = context.createMockPlayer(GameMode.SURVIVAL);
    context.runAtTick(
        START,
        () -> {
          context.assertTrue(master.canTarget(vampire), "he fights a vampire");
          context.assertFalse(master.canTarget(hunter), "never one of his hunters");
          context.assertFalse(master.canTarget(human), "and never a human");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 5)
  public void aGuildMasterDrawsHisSilverStakeToFightAVampire(final TestContext context) {
    final GuildMasterEntity master = master(context);
    final VampireEntity vampire = vampire(context);
    context.runAtTick(
        START,
        () -> {
          master.fight();
          context.assertTrue(master.getMainHandStack().isEmpty(), "he is at ease with no foe");
          master.setTarget(vampire);
          master.fight();
          context.assertTrue(master.armed(), "he readies himself for the fight");
          context.assertTrue(
              master.getMainHandStack().isOf(GearItems.SILVER_STAKE), "and draws a silver stake");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 5)
  public void aGuildMasterHitByAZombieTurnsOnItAndHisHuntersJoinIn(final TestContext context) {
    final GuildMasterEntity master = master(context);
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, new BlockPos(1, 2, 5));
    hunter.setAiDisabled(true);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(4, 2, 4));
    zombie.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          master.damage(context.getWorld().getDamageSources().mobAttack(zombie), 1.0F);
          final GuildRevengeGoal revenge = new GuildRevengeGoal(master);
          context.assertTrue(revenge.canStart(), "he answers the blow");
          revenge.start();
          context.assertTrue(master.getTarget() == zombie, "he turns on the zombie");
          context.assertTrue(hunter.getTarget() == zombie, "and his hunter joins in");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 10)
  public void aFightClosesTheGuildMastersCounter(final TestContext context) {
    final GuildMasterEntity master = master(context);
    final VampireEntity vampire = vampire(context);
    final ServerPlayerEntity customer = context.createMockCreativeServerPlayerInWorld();
    context.runAtTick(
        START,
        () -> {
          master.setCustomer(customer);
          context.assertFalse(master.onWatch(), "he starts no fight on sight while he trades");
          master.setTarget(vampire);
        });
    context.runAtTick(
        START + 2,
        () -> {
          context.assertFalse(master.hasCustomer(), "the fight closes his counter");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = OATH_OVER + 5)
  public void anOathEndsTheGuildMastersFightUntilItIsDone(final TestContext context) {
    final GuildMasterEntity master = master(context);
    final VampireEntity vampire = vampire(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(1, 2, 3));
    GuildMembership.prove(human);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          master.setTarget(vampire);
          master.fight();
          context.assertTrue(
              master.getMainHandStack().isOf(GearItems.SILVER_STAKE), "he fights with his stake");
          context.assertTrue(
              WheelUse.pick(human, master, GuildOathOrder.ID, WheelEntry.NO_ARGUMENT),
              "the human swears the oath");
          context.assertTrue(master.getTarget() == null, "he drops the fight");
          context.assertTrue(master.getMainHandStack().isEmpty(), "and puts his stake away");
          context.assertTrue(
              master.getOffHandStack().isOf(GearItems.GOLD_CRUCIFIX), "to raise the crucifix");
          context.assertFalse(master.onWatch(), "he looks for no foe during the oath");
          context.assertFalse(
              new GuildRevengeGoal(master).canStart(), "and answers no blow until it is done");
        });
    context.runAtTick(OATH_OVER, () -> TestPlayers.finish(context, human));
  }
}
