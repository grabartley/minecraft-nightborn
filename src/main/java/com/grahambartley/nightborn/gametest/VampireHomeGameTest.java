package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.config.CoreSettings;
import com.grahambartley.nightborn.config.MobSettings;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.NightRoutine;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class VampireHomeGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final String DAY_BATCH = "vampire-home-day";
  static final String NIGHT_BATCH = "vampire-home-night";
  static final String DRAIN_BATCH = "vampire-home-drain";
  static final int HUNGRY = 4;
  static final int CLAIM_WAIT = 110;
  static final int FAR_AWAY = 200;

  private int outingChanceBefore;
  private int mobDrainBefore;
  private int levitationDrainBefore;

  @BeforeBatch(batchId = DAY_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = DAY_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
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

  @GameTest(templateName = ARENA, tickLimit = CLAIM_WAIT + 10)
  public void aHouseholdVampireClaimsTheNearestFreeCoffinAtHome(final TestContext context) {
    final BlockPos taken = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final BlockPos free = CoffinStage.coffin(context, new BlockPos(6, 2, 1), Direction.SOUTH);
    final UUID someoneElse = UUID.randomUUID();
    coffinAt(context, taken).setOwner(someoneElse);
    final VampireEntity vampire = household(context, new BlockPos(4, 2, 4), taken);
    context.runAtTick(
        CLAIM_WAIT,
        () -> {
          context.assertEquals(
              vampire.homeLife().coffin().orElse(null),
              context.getAbsolutePos(free),
              "the vampire takes the free coffin, not the one already assigned");
          context.assertEquals(
              coffinAt(context, free).owner(), vampire.getUuid(), "the coffin records its vampire");
          context.assertEquals(
              coffinAt(context, taken).owner(), someoneElse, "the other coffin keeps its owner");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aCoffinKeepsItsOwnerThroughSaveAndLoad(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(4, 2, 3), Direction.SOUTH);
    final CoffinBlockEntity coffin = coffinAt(context, head);
    final UUID owner = UUID.randomUUID();
    coffin.setOwner(owner);
    final NbtCompound saved = coffin.createNbt(context.getWorld().getRegistryManager());
    coffin.setOwner(null);
    coffin.read(saved, context.getWorld().getRegistryManager());
    context.assertEquals(coffin.owner(), owner, "the owner is kept");
    context.complete();
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
    final VampireEntity vampire = household(context, new BlockPos(5, 2, 3), head);
    context.runAtEveryTick(
        () -> {
          if (vampire.isSleeping()) {
            context.assertEquals(
                vampire.getSleepingPosition().orElse(null),
                context.getAbsolutePos(head),
                "it sleeps in its own coffin");
            context.complete();
          }
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
          context.assertEquals(
              hungry.getPositionTargetRange(),
              (float) ServerConfigService.current().mobs().vampireFeedingRange(),
              "a hungry vampire roams out to its feeding range");
          context.assertEquals(
              fed.getPositionTargetRange(),
              (float) NightRoutine.HOME_RADIUS,
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
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(2, 2, 2));
    vampire.settleAt(context.getAbsolutePos(new BlockPos(2, 2, 2)).add(FAR_AWAY, 0, 0));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(6, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setAttacker(hunter);
          vampire.setTarget(hunter);
        });
    context.runAtTick(
        TestPlayers.READY + 10,
        () -> {
          context.assertTrue(
              vampire.getTarget() == null, "past its chase range the vampire gives up");
          TestPlayers.finish(context, hunter);
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

  private static VampireEntity household(
      final TestContext context, final BlockPos relative, final BlockPos home) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, relative);
    vampire.settleAt(context.getAbsolutePos(home == null ? relative : home));
    return vampire;
  }

  private static CoffinBlockEntity coffinAt(final TestContext context, final BlockPos head) {
    return context.getBlockEntity(head);
  }
}
