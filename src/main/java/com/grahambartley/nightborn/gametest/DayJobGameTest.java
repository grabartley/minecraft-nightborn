package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.config.ServantSettings;
import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.servant.ChestMark;
import com.grahambartley.nightborn.servant.ChestMarking;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayJobOrders;
import com.grahambartley.nightborn.servant.job.GatherTarget;
import com.grahambartley.nightborn.servant.job.Harvest;
import com.grahambartley.nightborn.servant.job.JobOrder;
import com.grahambartley.nightborn.servant.job.ScoutReport;
import com.grahambartley.nightborn.servant.job.ScoutTrip;
import com.grahambartley.nightborn.servant.job.goal.FetchBloodGoal;
import com.grahambartley.nightborn.servant.job.goal.GatherGoal;
import com.grahambartley.nightborn.servant.job.goal.ScoutGoal;
import com.grahambartley.nightborn.servant.job.goal.WatchTargetGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.ThrallRegistry;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.thrall.VillagerMemory;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public class DayJobGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String GATHER = "day-jobs-gather";
  static final String SCOUT = "day-jobs-scout";
  static final String WATCH = "day-jobs-watch";
  static final int NOON = 6000;
  static final int ROUND_TICKS = 20;
  static final int BLOOD_TICKS = 60;

  @BeforeBatch(batchId = GATHER)
  public void dayForGathering(final ServerWorld world) {
    freezeDay(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(ServantSettings.GATHER_RANGE, ServantSettings.GATHER_RANGE.min()));
  }

  @BeforeBatch(batchId = SCOUT)
  public void dayForScouting(final ServerWorld world) {
    freezeDay(world);
  }

  @BeforeBatch(batchId = WATCH)
  public void dayForWatching(final ServerWorld world) {
    freezeDay(world);
  }

  @AfterBatch(batchId = GATHER)
  public void thawAfterGathering(final ServerWorld world) {
    thaw(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(ServantSettings.GATHER_RANGE, ServantSettings.GATHER_RANGE.defaultValue()));
  }

  @AfterBatch(batchId = SCOUT)
  public void thawAfterScouting(final ServerWorld world) {
    thaw(world);
  }

  @AfterBatch(batchId = WATCH)
  public void thawAfterWatching(final ServerWorld world) {
    thaw(world);
  }

  private static void freezeDay(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(NOON);
  }

  private static void thaw(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  private static ThrallEntity thrall(
      final TestContext context, final ServerPlayerEntity owner, final BlockPos at) {
    final ThrallEntity thrall = context.spawnEntity(Thralls.THRALL, at);
    thrall.setAiDisabled(true);
    thrall.bind(owner.getUuid(), thrall.getVillagerData(), VillagerMemory.NONE);
    ThrallRegistry.of(context.getWorld().getServer()).bind(owner.getUuid(), thrall.getUuid());
    return thrall;
  }

  private static <T extends MobEntity> T still(
      final TestContext context, final EntityType<T> type, final BlockPos at) {
    final T mob = context.spawnEntity(type, at);
    mob.setAiDisabled(true);
    return mob;
  }

  @GameTest(templateName = ARENA, batchId = GATHER, tickLimit = TestPlayers.READY + 20)
  public void aThrallSetToGatherReapsRipeWheatSowsItAgainAndFillsTheChest(
      final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final BlockPos crop = new BlockPos(4, 2, 5);
    final BlockPos chest = new BlockPos(4, 2, 3);
    context.setBlockState(crop.down(), Blocks.FARMLAND);
    context.setBlockState(crop, ((CropBlock) Blocks.WHEAT).withAge(CropBlock.MAX_AGE));
    context.setBlockState(chest, Blocks.CHEST);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(4, 2, 4));
          context.assertEquals(
              ChestMarking.mark(owner, context.getAbsolutePos(chest)),
              Optional.of(ChestMark.FETCH),
              "a thrall's owner may mark a chest");
          context.assertTrue(
              WheelUse.pick(owner, thrall, DayJobOrders.ID, DayJob.GATHER.ordinal()),
              "the owner sets the thrall to gather from the wheel");
          context.assertTrue(thrall.doing(DayJob.GATHER), "the thrall is gathering");
          final GatherGoal<ThrallEntity> gather = new GatherGoal<>(thrall);
          context.assertTrue(gather.canStart(), "a marked chest lets the work begin");
          gather.start();
          for (int step = 0; step < ROUND_TICKS && gather.shouldContinue(); step++) {
            gather.tick();
          }
          context.assertFalse(gather.shouldContinue(), "the round ends once the field is bare");
          context.checkBlockState(
              crop,
              state -> state.isOf(Blocks.WHEAT) && !((CropBlock) Blocks.WHEAT).isMature(state),
              () -> "the wheat is reaped and sown again");
          final ChestBlockEntity filled = context.getBlockEntity(chest);
          context.assertEquals(filled.count(Items.WHEAT), 1, "the wheat reaches the chest");
          context.assertTrue(thrall.getInventory().isEmpty(), "nothing is kept back or lost");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = GATHER, tickLimit = 5)
  public void onlyTheLogsOfATreeAreGatheredNeverAPlayersBeams(final TestContext context) {
    final BlockPos tree = new BlockPos(2, 2, 2);
    final BlockPos beam = new BlockPos(6, 2, 6);
    context.setBlockState(tree, Blocks.OAK_LOG);
    context.setBlockState(tree.up(), Blocks.OAK_LOG);
    context.setBlockState(
        tree.up(2), Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, false));
    context.setBlockState(beam, Blocks.OAK_LOG);
    context.setBlockState(
        beam.up(), Blocks.OAK_LEAVES.getDefaultState().with(LeavesBlock.PERSISTENT, true));
    final ServerWorld world = context.getWorld();
    context.assertEquals(
        Harvest.kind(world, context.getAbsolutePos(tree), new SimpleInventory(1)),
        GatherTarget.WOOD,
        "a log under living leaves is a tree");
    context.assertEquals(
        Harvest.kind(world, context.getAbsolutePos(beam), new SimpleInventory(1)),
        GatherTarget.NONE,
        "a log under placed leaves is a player's work");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = GATHER, tickLimit = TestPlayers.READY + 20)
  public void oreIsMinedOnlyWithAPickaxeFromTheChestWhichWears(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final BlockPos iron = new BlockPos(4, 2, 5);
    final BlockPos diamond = new BlockPos(5, 2, 4);
    final BlockPos chest = new BlockPos(4, 2, 3);
    context.setBlockState(iron, Blocks.IRON_ORE);
    context.setBlockState(diamond, Blocks.DIAMOND_ORE);
    context.setBlockState(chest, Blocks.CHEST);
    final ChestBlockEntity box = context.getBlockEntity(chest);
    box.setStack(0, new ItemStack(Items.STONE_PICKAXE));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(4, 2, 4));
          ChestMarking.mark(owner, context.getAbsolutePos(chest));
          thrall.assign(JobOrder.at(DayJob.GATHER, thrall.getBlockPos()));
          final GatherGoal<ThrallEntity> gather = new GatherGoal<>(thrall);
          context.assertTrue(gather.canStart(), "the work begins");
          gather.start();
          for (int step = 0; step < ROUND_TICKS && gather.shouldContinue(); step++) {
            gather.tick();
          }
          context.expectBlock(Blocks.AIR, iron);
          context.expectBlock(Blocks.DIAMOND_ORE, diamond);
          context.assertEquals(box.count(Items.RAW_IRON), 1, "the iron comes home");
          context.assertEquals(box.count(Items.STONE_PICKAXE), 1, "and so does the pickaxe");
          context.assertEquals(
              box.getStack(0).getDamage() + box.getStack(1).getDamage(),
              1,
              "worn by the one block it mined");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = GATHER, tickLimit = TestPlayers.READY + 60)
  public void aServantSetToFetchBloodBottlesAnAnimalWithoutKillingIt(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    final BlockPos chest = new BlockPos(4, 2, 3);
    context.setBlockState(chest, Blocks.CHEST);
    final ChestBlockEntity box = context.getBlockEntity(chest);
    box.setStack(0, new ItemStack(BloodItems.LANCET));
    box.setStack(1, new ItemStack(Items.GLASS_BOTTLE, 2));
    final CowEntity cow = still(context, EntityType.COW, new BlockPos(4, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(4, 2, 4));
          ChestMarking.mark(owner, context.getAbsolutePos(chest));
          context.assertTrue(
              WheelUse.pick(owner, thrall, DayJobOrders.ID, DayJob.FETCH_BLOOD.ordinal()),
              "the owner sets the thrall to fetch blood");
          final FetchBloodGoal<ThrallEntity> fetch = new FetchBloodGoal<>(thrall);
          context.assertTrue(fetch.canStart(), "the work begins");
          fetch.start();
          for (int step = 0; step < BLOOD_TICKS && fetch.shouldContinue(); step++) {
            fetch.tick();
          }
          context.assertTrue(cow.isAlive(), "the lancet never kills");
          final int blood = box.count(BloodItems.BLOOD_BOTTLE);
          context.assertTrue(blood >= 1, "blood comes home in bottles");
          context.assertEquals(
              blood + box.count(Items.GLASS_BOTTLE), 2, "every bottle is accounted for");
          context.assertEquals(box.count(BloodItems.LANCET), 1, "the lancet comes back");
          context.assertTrue(thrall.getInventory().isEmpty(), "nothing is kept back");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = GATHER, tickLimit = TestPlayers.READY + 5)
  public void aFreedThrallDropsWhatItCarried(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(2, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(4, 2, 4));
          thrall.getInventory().addStack(new ItemStack(Items.WHEAT, 12));
          context.assertTrue(Thralls.free(thrall), "the thrall is freed");
          context.assertEquals(
              context
                  .getWorld()
                  .getEntitiesByClass(ItemEntity.class, context.getTestBox(), item -> true)
                  .stream()
                  .mapToInt(
                      item -> item.getStack().isOf(Items.WHEAT) ? item.getStack().getCount() : 0)
                  .sum(),
              12,
              "its wheat is on the ground");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = SCOUT, tickLimit = TestPlayers.READY + 20)
  public void aScoutReportsWhoIsThereAndTellsItsOwnerOnceHome(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    still(context, EntityType.VILLAGER, new BlockPos(6, 2, 6));
    still(context, EntityType.VILLAGER, new BlockPos(7, 2, 6));
    still(context, GuildMobs.HUNTER, new BlockPos(6, 2, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(5, 2, 5));
          final BlockPos home = context.getAbsolutePos(new BlockPos(1, 2, 3));
          thrall.assign(
              JobOrder.scout(
                  home,
                  ScoutTrip.to(
                      context.getAbsolutePos(new BlockPos(6, 2, 6)),
                      Identifier.of("nightborn", "hunter_camp"))));
          final ScoutGoal<ThrallEntity> scout = new ScoutGoal<>(thrall);
          context.assertTrue(scout.canStart(), "a scout sets out by day");
          scout.start();
          scout.tick();
          context.assertEquals(
              thrall.job().flatMap(JobOrder::trip).flatMap(ScoutTrip::report),
              Optional.of(new ScoutReport(2, 1, 1, 0)),
              "the scout counts the villagers, the hunter and its own vampire owner");
          thrall.refreshPositionAndAngles(home, 0.0F, 0.0F);
          scout.tick();
          context.assertTrue(thrall.job().isEmpty(), "home again, it reports and is done");
          TestPlayers.finish(context, owner);
        });
  }

  @GameTest(templateName = ARENA, batchId = WATCH, tickLimit = TestPlayers.READY + 20)
  public void aWatchDrivesOffAHunterNearTheCoffin(final TestContext context) {
    final ServerPlayerEntity owner = TestPlayers.vampire(context, new BlockPos(1, 2, 1));
    final HunterEntity hunter = still(context, GuildMobs.HUNTER, new BlockPos(6, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final ThrallEntity thrall = thrall(context, owner, new BlockPos(4, 2, 4));
          thrall.assign(
              JobOrder.at(DayJob.STAND_WATCH, context.getAbsolutePos(new BlockPos(4, 2, 4))));
          final WatchTargetGoal<ThrallEntity> watch = new WatchTargetGoal<>(thrall);
          context.assertTrue(watch.canStart(), "the watch sees an intruder");
          watch.start();
          context.assertTrue(thrall.getTarget() == hunter, "and goes for the hunter");
          context.assertTrue(hunter.getType().isIn(DownedTargeting.HUNTERS), "a guild hunter");
          TestPlayers.finish(context, owner);
        });
  }
}
