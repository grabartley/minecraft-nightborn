package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.config.CoreSettings;
import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.config.RaidSettings;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.goal.SleepInCoffinGoal;
import com.grahambartley.nightborn.mob.goal.WatchStrangerGoal;
import com.grahambartley.nightborn.mob.home.HomeCoffins;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class VampireHomeGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final String DAY_BATCH = "vampire-home-day";
  static final String NIGHT_BATCH = "vampire-home-night";
  static final String DRAIN_BATCH = "vampire-home-drain";
  static final String DAWN_BATCH = "vampire-home-dawn";
  static final String PEACEFUL_BATCH = "vampire-home-peaceful";
  static final String STRANGER_BATCH = "vampire-home-stranger";
  static final String KIN_BATCH = "vampire-home-kin";
  static final int NEAR_DAWN = 21900;
  static final int TRIES = 400;
  static final int UNLOADED = 20000;
  static final int HUNGRY = 4;
  static final int CLAIM_WAIT = 110;
  static final int DEATH_TICKS = 30;
  static final int FAR_AWAY = 200;
  static final int PAST_HOME = 24;
  static final int ATTACKER_FORGOTTEN = 120;

  private static int outingChanceBefore;
  private static int watchDaysBefore;
  private static int mobDrainBefore;
  private static int levitationDrainBefore;
  private static int dawnOutingChanceBefore;
  private static Difficulty difficultyBefore;

  @BeforeBatch(batchId = DAY_BATCH)
  public void pinNoonWithNoDayWatch(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
    watchDaysBefore = ServerConfigService.current().raids().watchDays();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(RaidSettings.WATCH_DAYS, 0));
  }

  @AfterBatch(batchId = DAY_BATCH)
  public void restoreNoonAndDayWatch(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(RaidSettings.WATCH_DAYS, watchDaysBefore));
  }

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinMidnightAndAlwaysGoOut(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
    outingChanceBefore = ServerConfigService.current().mobs().vampireOutingChance();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(MobSettings.VAMPIRE_OUTING_CHANCE, 100));
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void restoreNightAndOutings(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(MobSettings.VAMPIRE_OUTING_CHANCE, outingChanceBefore));
  }

  @BeforeBatch(batchId = DRAIN_BATCH)
  public void fastLevitationDrain(final ServerWorld world) {
    mobDrainBefore = ServerConfigService.current().mobs().vampireBloodDrainSeconds();
    levitationDrainBefore = ServerConfigService.current().core().levitationDrainSeconds();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(MobSettings.VAMPIRE_BLOOD_DRAIN_SECONDS, 600)
            .with(CoreSettings.LEVITATION_DRAIN_SECONDS, 1));
  }

  @AfterBatch(batchId = DRAIN_BATCH)
  public void restoreDrain(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(MobSettings.VAMPIRE_BLOOD_DRAIN_SECONDS, mobDrainBefore)
            .with(CoreSettings.LEVITATION_DRAIN_SECONDS, levitationDrainBefore));
  }

  @BeforeBatch(batchId = DAWN_BATCH)
  public void pinNearDawnAndAlwaysGoOut(final ServerWorld world) {
    WorldClock.freeze(world, NEAR_DAWN);
    dawnOutingChanceBefore = ServerConfigService.current().mobs().vampireOutingChance();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(MobSettings.VAMPIRE_OUTING_CHANCE, 100));
  }

  @AfterBatch(batchId = DAWN_BATCH)
  public void restoreDawnAndOutings(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(MobSettings.VAMPIRE_OUTING_CHANCE, dawnOutingChanceBefore));
  }

  @BeforeBatch(batchId = PEACEFUL_BATCH)
  public void peacefulMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
    difficultyBefore = world.getDifficulty();
    world.getServer().setDifficulty(Difficulty.PEACEFUL, true);
  }

  @AfterBatch(batchId = PEACEFUL_BATCH)
  public void restoreDifficulty(final ServerWorld world) {
    WorldClock.release(world);
    world.getServer().setDifficulty(difficultyBefore, true);
  }

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + 10)
  public void aHouseholdVampireClaimsTheNearestFreeCoffinAtHome(final TestContext context) {
    final BlockPos taken = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final BlockPos free = CoffinStage.coffin(context, new BlockPos(6, 2, 1), Direction.SOUTH);
    final UUID someoneElse = UUID.randomUUID();
    CoffinOwnership.claim(context.getWorld(), context.getAbsolutePos(taken), someoneElse);
    final VampireEntity vampire =
        keepingHouse(context, household(context, new BlockPos(4, 2, 4), taken));
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(
              vampire.homeLife().coffin().orElse(null),
              context.getAbsolutePos(free),
              "the vampire takes the free coffin, not the one already assigned");
          context.assertEquals(
              ownerOf(context, free), vampire.getUuid(), "the coffin records its vampire");
          context.assertEquals(
              ownerOf(context, taken), someoneElse, "the other coffin keeps its owner");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireKeepsItsHomeCoffinAndOutingThroughSaveAndLoad(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(4, 2, 4));
    final BlockPos home = context.getAbsolutePos(new BlockPos(4, 2, 4));
    vampire.settleAt(home);
    final NbtCompound nbt = new NbtCompound();
    vampire.writeNbt(nbt);
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    loaded.readNbt(nbt);
    context.assertEquals(loaded.homeLife().home().orElse(null), home, "the home is kept");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = DAY_BATCH, tickLimit = CLAIM_WAIT + 40)
  public void aVampireLiesDownInItsCoffinAtBedtime(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 2), Direction.SOUTH);
    final VampireEntity vampire =
        keepingHouse(context, household(context, new BlockPos(5, 2, 3), head));
    final SleepInCoffinGoal goal = new SleepInCoffinGoal(vampire);
    context.assertFalse(goal.canStart(), "with no coffin of its own it has nowhere to lie down");
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(
              vampire.homeLife().coffin().orElse(null),
              context.getAbsolutePos(head),
              "it has claimed its coffin");
          context.assertTrue(goal.canStart(), "at bedtime it makes for its coffin");
          goal.start();
          goal.tick();
          context.assertTrue(vampire.isSleeping(), "beside its coffin it lies down");
          context.assertEquals(
              vampire.getSleepingPosition().orElse(null),
              context.getAbsolutePos(head),
              "it sleeps in its own coffin");
          context.assertFalse(goal.canStart(), "a sleeping vampire does not look for bed again");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 120)
  public void aSleepingVampireRisesFromItsCoffinAtNight(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 2), Direction.SOUTH);
    final VampireEntity vampire = household(context, new BlockPos(5, 2, 3), head);
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    context.runAtTick(2, () -> vampire.sleep(context.getAbsolutePos(head)));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() > 5 && !vampire.isSleeping()) {
            context.complete();
          }
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 10)
  public void aHungryVampireGoesOutToFeedAndAFedOneStaysHome(final TestContext context) {
    final VampireEntity hungry = household(context, new BlockPos(2, 2, 2), null);
    final VampireEntity fed = household(context, new BlockPos(6, 2, 6), null);
    BloodAttachment.set(hungry, HUNGRY);
    BloodAttachment.set(fed, BloodAttachment.FULL);
    context.runAtTick(
        5,
        () -> {
          hungry.tickHome();
          fed.tickHome();
          context.assertEquals(
              hungry.getPositionTargetRange(),
              (float) ServerConfigService.current().mobs().vampireFeedingRange(),
              "a hungry vampire roams out to its feeding range");
          context.assertEquals(
              fed.getPositionTargetRange(),
              (float) ServerConfigService.current().mobs().vampireHomeRadius(),
              "a fed vampire stays about its home");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void onlyAHungryVampireHuntsAtNight(final TestContext context) {
    final VampireEntity hungry = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    final VampireEntity fed = VampireMobGameTest.still(context, new BlockPos(6, 2, 6));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 2, 4));
    villager.setAiDisabled(true);
    BloodAttachment.set(hungry, HUNGRY);
    BloodAttachment.set(fed, BloodAttachment.FULL);
    context.assertTrue(hungry.hunts(villager), "a hungry vampire treats a villager as prey");
    context.assertFalse(fed.hunts(villager), "a fed vampire leaves the villager alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aHungryVampireDoesNotHuntFarFromHome(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 2, 4));
    villager.setAiDisabled(true);
    BloodAttachment.set(vampire, HUNGRY);
    vampire.settleAt(context.getAbsolutePos(new BlockPos(2, 2, 2)).add(FAR_AWAY, 0, 0));
    context.assertFalse(
        vampire.hunts(villager), "prey beyond its feeding range of home is left alone");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void hurtingAVampireProvokesItsHousematesOnly(final TestContext context) {
    final BlockPos home = context.getAbsolutePos(new BlockPos(4, 2, 4));
    final VampireEntity hurt = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    final VampireEntity mate = VampireMobGameTest.still(context, new BlockPos(6, 2, 6));
    final VampireEntity neighbour = VampireMobGameTest.still(context, new BlockPos(2, 2, 6));
    hurt.settleAt(home);
    mate.settleAt(home);
    neighbour.settleAt(home.add(FAR_AWAY, 0, 0));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(4, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          hurt.damage(hurt.getDamageSources().playerAttack(hunter), 1.0F);
          context.assertTrue(mate.getAttacker() == hunter, "a housemate is provoked");
          context.assertTrue(neighbour.getAttacker() == null, "a vampire from another home is not");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 20)
  public void aProvokedVampireGivesUpTheChaseFarFromHome(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(2, 2, 2)).add(FAR_AWAY, 0, 0));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setAttacker(hunter);
          vampire.setTarget(hunter);
          vampire.tickHome();
          context.assertTrue(
              vampire.getTarget() == null, "past its chase range the vampire gives up");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = NIGHT_BATCH,
      tickLimit = TestPlayers.READY + ATTACKER_FORGOTTEN + 10)
  public void aFedVampireStruckOutsideItsHomeFightsBack(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    vampire.settleAt(context.getAbsolutePos(new BlockPos(2, 2, 2)).add(PAST_HOME, 0, 0));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(3, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.damage(vampire.getDamageSources().playerAttack(hunter), 1.0F);
          vampire.tickHome();
          context.assertTrue(
              vampire.isInWalkTargetRange(hunter.getBlockPos()),
              "a struck vampire may chase an attacker outside its home radius");
          vampire.setTarget(hunter);
        });
    context.runAtTick(
        TestPlayers.READY + ATTACKER_FORGOTTEN,
        () -> {
          context.assertTrue(vampire.getAttacker() == null, "vanilla has forgotten the attacker");
          vampire.tickHome();
          context.assertTrue(
              vampire.getTarget() == hunter, "a fed vampire keeps fighting whoever hit it");
          final float before = hunter.getHealth();
          context.assertTrue(vampire.tryAttack(hunter), "it strikes back");
          context.assertTrue(hunter.getHealth() < before, "and hurts its attacker");
          vampire.setTarget(null);
          vampire.tickHome();
          context.assertFalse(
              vampire.isInWalkTargetRange(hunter.getBlockPos()),
              "once it stops fighting it is back on its home leash");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireFightsBackAgainstAYoungerVampireThatStrikesIt(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    vampire.becomeStage(Identifier.of("nightborn", "elder"));
    final ServerPlayerEntity player = TestPlayers.vampire(context, new BlockPos(3, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(vampire.canTarget(player), "it leaves a vampire player alone");
          vampire.damage(vampire.getDamageSources().playerAttack(player), 1.0F);
          context.assertTrue(
              vampire.canTarget(player), "until a younger vampire player strikes it");
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, batchId = DRAIN_BATCH, tickLimit = 80)
  public void aLevitatingVampireMobLosesBlood(final TestContext context) {
    final VampireEntity floating = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    final VampireEntity standing = VampireMobGameTest.still(context, new BlockPos(6, 2, 6));
    BloodAttachment.set(floating, BloodAttachment.FULL);
    BloodAttachment.set(standing, BloodAttachment.FULL);
    context.runAtTick(2, () -> Levitation.start(floating));
    context.runAtTick(
        70,
        () -> {
          context.assertTrue(
              BloodAttachment.get(floating) < BloodAttachment.FULL,
              "levitating drains blood: " + BloodAttachment.get(floating));
          context.assertEquals(
              BloodAttachment.get(standing), BloodAttachment.FULL, "standing still costs little");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobWithAnEmptyBarStarves(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(4, 2, 4));
    BloodAttachment.set(vampire, 0);
    context.runAtTick(
        5,
        () -> {
          context.assertTrue(
              Weakened.sources(vampire).strength(Starvation.SOURCE) > 0,
              "an empty bar weakens the vampire mob as it does a player");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = DAWN_BATCH, tickLimit = 10)
  public void aHungryVampireStaysHomeWhenDawnIsTooNearToGoOut(final TestContext context) {
    final VampireEntity vampire = household(context, new BlockPos(4, 2, 4), null);
    BloodAttachment.set(vampire, HUNGRY);
    context.runAtTick(
        5,
        () -> {
          vampire.tickHome();
          context.assertTrue(vampire.homeLife().headsHome(vampire), "dawn is near: head home");
          context.assertEquals(
              vampire.getPositionTargetRange(),
              (float) ServerConfigService.current().mobs().vampireHomeRadius(),
              "a vampire near dawn keeps to its home, hungry or not");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aHungryVampireBiteFillsItsBlood(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(4, 2, 4));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(5, 2, 4));
    villager.setAiDisabled(true);
    BloodAttachment.set(vampire, HUNGRY);
    vampire.setTarget(villager);
    context.assertTrue(vampire.tryAttack(villager), "a hungry vampire bites its prey");
    context.assertTrue(
        BloodAttachment.get(vampire) > HUNGRY,
        "the bite fills its blood: " + BloodAttachment.get(vampire));
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = PEACEFUL_BATCH, tickLimit = TestPlayers.READY + 10)
  public void onPeacefulAHungryVampireSparesPlayersAndStays(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(4, 2, 4)));
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, new BlockPos(4, 2, 4));
    villager.setAiDisabled(true);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(6, 2, 6));
    BloodAttachment.set(vampire, HUNGRY);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(vampire.hunts(human), "on Peaceful a vampire never hunts a player");
          context.assertTrue(vampire.hunts(villager), "it still hunts a villager");
          vampire.checkDespawn();
          context.assertFalse(vampire.isRemoved(), "Peaceful does not clear a household");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = STRANGER_BATCH, tickLimit = TestPlayers.READY + 10)
  public void aFedVampireFollowsAHumanStrangerWithoutAttacking(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(1, 2, 1));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(1, 2, 1)));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    final ServerPlayerEntity stranger = TestPlayers.human(context, new BlockPos(7, 2, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final WatchStrangerGoal goal = new WatchStrangerGoal(vampire);
          boolean noticed = false;
          for (int i = 0; i < TRIES && !noticed; i++) {
            noticed = goal.canStart();
          }
          context.assertTrue(noticed, "a fed vampire notices a human stranger");
          vampire.setOnGround(true);
          goal.start();
          goal.tick();
          context.assertTrue(
              vampire.getNavigation().getCurrentPath() != null,
              "it follows a stranger standing off at a distance");
          context.assertFalse(vampire.hunts(stranger), "a fed vampire does not hunt the stranger");
          context.assertTrue(vampire.getTarget() == null, "nor does it target them");
          TestPlayers.finish(context, stranger);
        });
  }

  @GameTest(templateName = ARENA, batchId = KIN_BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireDoesNotWatchVampireOrCreativePlayers(final TestContext context) {
    final VampireEntity vampire = VampireMobGameTest.still(context, new BlockPos(1, 2, 1));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(1, 2, 1)));
    BloodAttachment.set(vampire, BloodAttachment.FULL);
    final ServerPlayerEntity kin = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    final ServerPlayerEntity builder = TestPlayers.human(context, new BlockPos(7, 2, 7));
    builder.changeGameMode(GameMode.CREATIVE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final WatchStrangerGoal goal = new WatchStrangerGoal(vampire);
          boolean noticed = false;
          for (int i = 0; i < TRIES && !noticed; i++) {
            noticed = goal.canStart();
          }
          context.assertFalse(noticed, "neither a vampire nor a creative player is a stranger");
          TestPlayers.finish(context, kin, builder);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + 10)
  public void aVampireKeepsItsCoffinWhileTheCoffinIsUnloaded(final TestContext context) {
    final VampireEntity vampire =
        keepingHouse(context, VampireMobGameTest.still(context, new BlockPos(4, 2, 4)));
    final BlockPos away = context.getAbsolutePos(new BlockPos(4, 2, 4)).add(UNLOADED, 0, 0);
    final NbtCompound nbt = new NbtCompound();
    vampire.writeNbt(nbt);
    nbt.putLong("Home", away.asLong());
    nbt.putLong("Coffin", away.north(2).asLong());
    vampire.readNbt(nbt);
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(
              vampire.homeLife().coffin().orElse(null),
              away.north(2),
              "a coffin in an unloaded chunk is kept, not dropped or reloaded");
          context.assertFalse(
              HomeCoffins.loaded(context.getWorld(), away),
              "checking the coffin never loads its chunk");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + 10)
  public void aVampireThatDiesFreesItsCoffin(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 1), Direction.SOUTH);
    final VampireEntity vampire =
        keepingHouse(context, household(context, new BlockPos(4, 2, 5), head));
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(
              ownerOf(context, head), vampire.getUuid(), "the vampire owns its coffin");
          vampire.remove(Entity.RemovalReason.KILLED);
          context.assertTrue(ownerOf(context, head) == null, "its death frees the coffin");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + DEATH_TICKS + 10)
  public void aStrangerCannotBreakALivingVampiresCoffin(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 1), Direction.SOUTH);
    final VampireEntity vampire =
        keepingHouse(context, household(context, new BlockPos(4, 2, 5), head));
    final ServerPlayerEntity stranger = TestPlayers.human(context, new BlockPos(2, 2, 3));
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(ownerOf(context, head), vampire.getUuid(), "the vampire owns it");
          context.assertFalse(
              stranger.interactionManager.tryBreakBlock(context.getAbsolutePos(head)),
              "a survival stranger cannot break it while the vampire lives");
          context.expectBlock(CoffinStage.darkOak(), head);
          vampire.damage(NightbornDamage.sun(context.getWorld()), Float.MAX_VALUE);
          context.assertTrue(vampire.isDead(), "the sun kills the vampire");
        });
    context.runAtTick(
        CLAIM_WAIT + DEATH_TICKS,
        () -> {
          context.assertTrue(ownerOf(context, head) == null, "its death to the sun frees it");
          context.assertTrue(
              stranger.interactionManager.tryBreakBlock(context.getAbsolutePos(head)),
              "then anyone can break it");
          TestPlayers.finish(context, stranger);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + 10)
  public void housematesNeverShareACoffin(final TestContext context) {
    final BlockPos left = CoffinStage.coffin(context, new BlockPos(2, 2, 1), Direction.SOUTH);
    final BlockPos right = CoffinStage.coffin(context, new BlockPos(6, 2, 1), Direction.SOUTH);
    final BlockPos home = new BlockPos(4, 2, 4);
    final VampireEntity first =
        keepingHouse(context, household(context, new BlockPos(3, 2, 5), home));
    final VampireEntity second =
        keepingHouse(context, household(context, new BlockPos(5, 2, 5), home));
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          final BlockPos firstCoffin = first.homeLife().coffin().orElse(null);
          final BlockPos secondCoffin = second.homeLife().coffin().orElse(null);
          context.assertTrue(
              firstCoffin != null && secondCoffin != null, "both housemates have a coffin");
          context.assertFalse(firstCoffin.equals(secondCoffin), "each has its own");
          context.assertTrue(
              ownerOf(context, left) != null && ownerOf(context, right) != null,
              "both coffins are taken");
          context.complete();
        });
  }

  private static VampireEntity household(
      final TestContext context, final BlockPos relative, final BlockPos home) {
    final VampireEntity vampire = VampireMobGameTest.still(context, relative);
    vampire.settleAt(context.getAbsolutePos(home == null ? relative : home));
    return vampire;
  }

  private static VampireEntity keepingHouse(
      final TestContext context, final VampireEntity vampire) {
    context.runAtEveryTick(
        () -> {
          if (vampire.isAlive()) {
            vampire.tickHome();
          }
        });
    return vampire;
  }

  private static UUID ownerOf(final TestContext context, final BlockPos head) {
    return CoffinOwnership.ownerAt(context.getWorld(), context.getAbsolutePos(head)).orElse(null);
  }
}
