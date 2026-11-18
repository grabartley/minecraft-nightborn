package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.GuildCounter;
import com.grahambartley.nightborn.guild.GuildCounterOrders;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.GuildOathOrder;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.moment.MomentAttachment;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.wheel.WheelEntry;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class OathGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int MOMENT_OVER = TestPlayers.READY + 8 * 20 + 5;

  private static GuildMasterEntity guildMaster(final TestContext context) {
    final GuildMasterEntity master =
        context.spawnEntity(GuildMobs.GUILD_MASTER, new BlockPos(4, 2, 4));
    master.setAiDisabled(true);
    return master;
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aHumanWhoStakesAVampireIsProvenButEarnsNoHunterPoints(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    human.setStackInHand(Hand.MAIN_HAND, new ItemStack(GearItems.WOODEN_STAKE));
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
          vampire.damage(human.getDamageSources().playerAttack(human), 1.0F);
          context.assertFalse(vampire.isAlive(), "the vampire is staked");
          context.assertEquals(
              SkillAttachment.get(human, SkillTree.HUNTER).earned(), 0, "no hunter points yet");
          context.assertFalse(GuildMembership.isHunter(human), "not a hunter yet");
          context.assertTrue(GuildMembership.maySwear(human), "but proven, so may swear");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = MOMENT_OVER + 5)
  public void aGuildMasterSwearsInAProvenHumanWhoIsUntouchableUntilTheMomentEnds(
      final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    GuildMembership.prove(human);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(
              WheelUse.pick(
                  human,
                  master,
                  new GuildCounterOrders(GuildCounter.Kind.CONTRACTS).id(),
                  WheelEntry.NO_ARGUMENT),
              "contracts are closed to a human not yet sworn");
          context.assertTrue(
              WheelUse.pick(human, master, GuildOathOrder.ID, WheelEntry.NO_ARGUMENT),
              "the human swears the oath");
          context.assertTrue(GuildMembership.isHunter(human), "and is now a hunter");
          context.assertTrue(TestAdvancements.done(context, human, "sworn_in"), "Sworn In");
          context.assertTrue(Moments.locked(human), "the oath is a moment");
          context.assertTrue(
              master.getOffHandStack().isOf(GearItems.GOLD_CRUCIFIX),
              "the Guild Master raises a crucifix");
          final float health = human.getHealth();
          human.damage(human.getDamageSources().generic(), 5.0F);
          context.assertEquals(human.getHealth(), health, "nothing hurts during the moment");
        });
    context.runAtTick(
        MOMENT_OVER,
        () -> {
          context.assertFalse(Moments.locked(human), "the moment ends");
          context.assertTrue(MomentAttachment.get(human) == null, "and leaves nothing behind");
          context.assertFalse(
              master.getOffHandStack().isOf(GearItems.GOLD_CRUCIFIX), "the crucifix is put away");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void turningIsAMomentTheNewVampireCannotBeHurtIn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(VampireTransitions.turnInCeremony(human), "the human turns");
          context.assertTrue(Vampires.isVampire(human), "and is a vampire");
          context.assertTrue(Moments.locked(human), "the turning is a moment");
          final float health = human.getHealth();
          human.damage(human.getDamageSources().generic(), 5.0F);
          context.assertEquals(human.getHealth(), health, "nothing hurts during the moment");
          context.assertFalse(GuildMembership.maySwear(human), "a vampire can never join");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aPlayerShovedDuringAMomentIsPutBackWhereTheyStood(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final Vec3d[] stood = new Vec3d[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(VampireTransitions.turnInCeremony(human), "the human turns");
          stood[0] = human.getPos();
          human.setPosition(stood[0].add(2.0, 0.0, 0.0));
          human.setVelocity(new Vec3d(1.0, 0.5, 0.0));
        });
    context.runAtTick(
        TestPlayers.READY + 2,
        () -> {
          context.assertTrue(
              human.getPos().squaredDistanceTo(stood[0]) < 1.0E-4, "the player is put back");
          context.assertTrue(
              human.getVelocity().horizontalLengthSquared() == 0.0,
              "and the shove is gone, but it moves at " + human.getVelocity());
          TestPlayers.finish(context, human);
        });
  }
}
