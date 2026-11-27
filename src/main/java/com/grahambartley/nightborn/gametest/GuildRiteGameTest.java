package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.rite.GuildRite;
import com.grahambartley.nightborn.guild.rite.GuildRiteOrder;
import com.grahambartley.nightborn.guild.rite.GuildRites;
import com.grahambartley.nightborn.guild.rite.RiteCooldownAttachment;
import com.grahambartley.nightborn.guild.rite.RiteTithe;
import com.grahambartley.nightborn.guild.rite.RiteWay;
import com.grahambartley.nightborn.guild.rite.Sanctuary;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.rank.Ranking;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.Threshold;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class GuildRiteGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "guild-rite";
  static final BlockPos MASTER = new BlockPos(4, 2, 4);
  static final BlockPos HUNTER = new BlockPos(2, 2, 4);
  static final BlockPos DOORSTEP = new BlockPos(5, 1, 6);
  static final BlockPos ENTRANCE = new BlockPos(6, 2, 6);
  static final BlockPos NO_FLOOR = new BlockPos(5, 2, 6);
  static final int SHELTER_TICKS = 3 * 20;
  static final int SPARE = 10;
  static final int PURSE = 20;

  @BeforeBatch(batchId = BATCH)
  public void forgetRites(final ServerWorld world) {
    GuildRites.clearAll();
  }

  private static GuildMasterEntity guildMaster(final TestContext context) {
    final GuildMasterEntity master = context.spawnEntity(GuildMobs.GUILD_MASTER, MASTER);
    master.setAiDisabled(true);
    return master;
  }

  private static ServerPlayerEntity payingHunter(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, HUNTER);
    Ranking.setRank(hunter, 1);
    hunter.getInventory().insertStack(new ItemStack(Items.EMERALD, PURSE));
    return hunter;
  }

  private static GuildRite rite(final TestContext context) {
    return GuildRite.sending(context.getWorld().getRegistryManager()).orElseThrow();
  }

  private static GuildRite exact(final GuildRite rite) {
    final GuildRite.Reach reach = rite.reach();
    return new GuildRite(
        rite.timing(),
        rite.clips(),
        rite.sounds(),
        rite.light(),
        new GuildRite.Reach(reach.breakDistance(), 0, 0, reach.householdReach()));
  }

  private static int over(final GuildRite rite) {
    return TestPlayers.READY + rite.timing().castTicks() + rite.timing().leaveTicks() + SPARE;
  }

  private static CompletableFuture<Optional<RiteWay>> heardOf(
      final TestContext context, final BlockPos doorstep) {
    final BlockPos floor = context.getAbsolutePos(doorstep);
    return CompletableFuture.completedFuture(
        Optional.of(
            new RiteWay(floor.up(), world -> Optional.of(new Threshold(floor, Direction.EAST)))));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 220)
  public void aSwornHunterIsSentToTheEntranceAndShelteredThere(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity hunter = payingHunter(context);
    final GuildRite rite = rite(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> GuildRites.begin(hunter, master, rite, heardOf(context, DOORSTEP)));
    context.runAtTick(
        TestPlayers.READY + 5,
        () -> context.assertTrue(GuildRites.underway(hunter), "the rite is being cast"));
    context.runAtTick(
        over(rite),
        () -> {
          context.assertEquals(
              hunter.getBlockPos(),
              context.getAbsolutePos(ENTRANCE),
              "the hunter stands at the entrance");
          context.assertTrue(Sanctuary.sheltered(hunter), "the hunter is sheltered on arrival");
          final float health = hunter.getHealth();
          context.assertFalse(
              hunter.damage(hunter.getDamageSources().generic(), 5.0F),
              "nothing harms a sheltered hunter");
          context.assertEquals(hunter.getHealth(), health, "no health is lost");
          context.assertTrue(
              RiteCooldownAttachment.readyAt(hunter) > 0L, "the hunter must wait to ask again");
          context.assertEquals(
              RiteTithe.held(hunter),
              PURSE - ServerConfigService.current().guild().ritePrice(),
              "the tithe is paid once");
        });
    context.runAtTick(
        over(rite) + SHELTER_TICKS + SPARE,
        () -> {
          context.assertFalse(Sanctuary.sheltered(hunter), "the shelter has run out");
          context.assertTrue(
              hunter.damage(hunter.getDamageSources().generic(), 1.0F),
              "harm lands again once the shelter is over");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 160)
  public void aWoundBreaksTheRiteAndNobodyIsMoved(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity hunter = payingHunter(context);
    final GuildRite rite = rite(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> GuildRites.begin(hunter, master, rite, heardOf(context, DOORSTEP)));
    context.runAtTick(
        TestPlayers.READY + 10, () -> hunter.damage(hunter.getDamageSources().generic(), 1.0F));
    context.runAtTick(
        TestPlayers.READY + 15,
        () -> context.assertFalse(GuildRites.underway(hunter), "the wound broke the rite"));
    context.runAtTick(
        over(rite),
        () -> {
          context.assertEquals(
              hunter.getBlockPos(), context.getAbsolutePos(HUNTER), "the hunter was not moved");
          context.assertFalse(Sanctuary.sheltered(hunter), "no shelter without a sending");
          context.assertEquals(
              RiteCooldownAttachment.readyAt(hunter),
              RiteCooldownAttachment.READY,
              "a broken rite costs no wait");
          context.assertEquals(RiteTithe.held(hunter), PURSE, "a broken rite costs nothing");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 160)
  public void withNoSafeGroundAtTheEntranceNobodyIsMoved(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity hunter = payingHunter(context);
    final GuildRite rite = exact(rite(context));
    context.runAtTick(
        TestPlayers.READY,
        () -> GuildRites.begin(hunter, master, rite, heardOf(context, NO_FLOOR)));
    context.runAtTick(
        over(rite),
        () -> {
          context.assertFalse(GuildRites.underway(hunter), "the rite ended");
          context.assertEquals(
              hunter.getBlockPos(), context.getAbsolutePos(HUNTER), "the hunter was not moved");
          context.assertFalse(Sanctuary.sheltered(hunter), "no shelter without a sending");
          context.assertEquals(RiteTithe.held(hunter), PURSE, "a failed rite costs nothing");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 20)
  public void aHunterWithoutTheRankOrTheTitheIsTurnedAway(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity initiate = TestPlayers.hunter(context, HUNTER);
    initiate.getInventory().insertStack(new ItemStack(Items.EMERALD, PURSE));
    final ServerPlayerEntity poor = TestPlayers.hunter(context, new BlockPos(2, 2, 6));
    Ranking.setRank(poor, 1);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          GuildRites.ask(initiate, master);
          context.assertFalse(GuildRites.underway(initiate), "an initiate is turned away");
          context.assertEquals(RiteTithe.held(initiate), PURSE, "and keeps his emeralds");
          GuildRites.ask(poor, master);
          context.assertFalse(GuildRites.underway(poor), "a hunter with no tithe is turned away");
          TestPlayers.finish(context, initiate, poor);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 20)
  public void theRiteIsOfferedToSwornHuntersAndNeverToVampires(final TestContext context) {
    final GuildMasterEntity master = guildMaster(context);
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, HUNTER);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 2));
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final GuildRiteOrder order = new GuildRiteOrder();
          context.assertTrue(order.allows(hunter, master), "a sworn hunter may ask");
          context.assertFalse(order.allows(human, master), "an unsworn human may not");
          context.assertFalse(order.allows(vampire, master), "a vampire never may");
          TestPlayers.finish(context, hunter, human, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 20)
  public void strikingAnythingEndsTheShelterEarly(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, HUNTER);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, MASTER);
    vampire.setAiDisabled(true);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          Sanctuary.shelter(hunter, 3);
          context.assertTrue(Sanctuary.sheltered(hunter), "the hunter is sheltered");
          vampire.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          context.assertFalse(Sanctuary.sheltered(hunter), "a blow gives the shelter up");
          TestPlayers.finish(context, hunter);
        });
  }
}
