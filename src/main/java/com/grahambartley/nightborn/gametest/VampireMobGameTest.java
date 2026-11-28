package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedRules;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.gear.StakedFall;
import com.grahambartley.nightborn.mob.BatEscort;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.Prey;
import com.grahambartley.nightborn.mob.ShadeRules;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.goal.FleeWhileEscapingGoal;
import com.grahambartley.nightborn.mob.goal.LieDownedGoal;
import com.grahambartley.nightborn.mob.goal.SeekShadeGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class VampireMobGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String YARD = "nightborn:vampire_yard";
  static final String NOON_BATCH = "vampire-mob-noon";
  static final String DAWN_BATCH = "vampire-mob-dawn";
  static final int BEFORE_DAWN = 22500;
  static final int TRIES = 40;
  static final String NIGHT_BATCH = "vampire-mob-night";
  static final int ESCAPED_AT = 110;
  static final int LIGHT_SETTLED = 20;

  private static int downedSecondsBefore;

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = DAWN_BATCH)
  public void pinDawn(final ServerWorld world) {
    WorldClock.freeze(world, BEFORE_DAWN);
  }

  @AfterBatch(batchId = DAWN_BATCH)
  public void unpinDawn(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNightAndShortDowns(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
    downedSecondsBefore = ServerConfigService.current().downed().downedSeconds();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(DownedSettings.DOWNED_SECONDS, 5));
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void restoreNightAndDowns(final ServerWorld world) {
    WorldClock.release(world);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current().with(DownedSettings.DOWNED_SECONDS, downedSecondsBefore));
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobIsAVampire(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    context.assertTrue(Vampires.isVampire(vampire), "the vampire mob counts as a vampire");
    context.assertFalse(Prey.isPrey(vampire), "a vampire mob is never prey");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSpawnedVampireMobStartsAtItsFullVampireHealth(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    vampire.initialize(
        context.getWorld(),
        context.getWorld().getLocalDifficulty(vampire.getBlockPos()),
        SpawnReason.SPAWN_EGG,
        null);
    context.assertTrue(
        vampire.getMaxHealth() > vampire.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH),
        "the vampire's own health boost applies from the start");
    context.assertEquals(vampire.getHealth(), vampire.getMaxHealth(), "it starts at full health");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aSwordDownsAVampireMobInsteadOfKillingIt(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final PlayerEntity hunter = context.createMockPlayer(GameMode.SURVIVAL);
    vampire.damage(vampire.getDamageSources().playerAttack(hunter), 1000.0F);
    vampire.damage(vampire.getDamageSources().inFire(), 1000.0F);
    vampire.damage(vampire.getDamageSources().fall(), 1000.0F);
    context.assertTrue(vampire.isAlive(), "a sword, fire and a fall do not kill a vampire mob");
    context.assertEquals(vampire.getHealth(), 1.0F, "the vampire mob stops at its last health");
    final DownedState state = Downed.state(vampire);
    context.assertTrue(state != null && state.isDowned(), "the vampire mob is downed");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aStakeKillsADownedVampireMob(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final PlayerEntity hunter = context.createMockPlayer(GameMode.SURVIVAL);
    vampire.damage(vampire.getDamageSources().playerAttack(hunter), 1000.0F);
    vampire.damage(NightbornDamage.stake(context.getWorld(), hunter), 1000.0F);
    context.assertTrue(vampire.isDead(), "a stake finishes a downed vampire mob");
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = StakedFall.FALL_TICKS + 10)
  public void aStakedVampireMobStaysDownUntilItsFallEnds(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final PlayerEntity hunter = context.createMockPlayer(GameMode.SURVIVAL);
    vampire.damage(vampire.getDamageSources().playerAttack(hunter), 1000.0F);
    vampire.damage(NightbornDamage.stake(context.getWorld(), hunter), 1000.0F);
    context.runAtTick(
        30,
        () -> {
          context.assertTrue(StakedFall.staked(vampire), "the stake marks the fall");
          context.assertFalse(vampire.isRemoved(), "the body outlasts vanilla's one second");
        });
    context.runAtTick(
        StakedFall.FALL_TICKS + 5,
        () -> {
          context.assertTrue(vampire.isRemoved(), "the body goes once the fall ends");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = NOON_BATCH, skyAccess = true, tickLimit = 80)
  public void theSunKillsAVampireMob(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    vampire.setHealth(3.0F);
    context.runAtTick(
        60,
        () -> {
          context.assertTrue(vampire.isDead(), "direct sun kills a vampire mob");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aDownedVampireMobLiesStillAndCannotBite(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final PlayerEntity human = context.createMockPlayer(GameMode.SURVIVAL);
    Downed.down(vampire, ServerConfigService.current().downed(), Optional.empty());
    vampire.setTarget(human);
    final LieDownedGoal goal = new LieDownedGoal(vampire);
    context.assertTrue(goal.canStart(), "a downed vampire mob lies down");
    goal.start();
    context.assertTrue(vampire.getTarget() == null, "a downed vampire mob drops its target");
    context.assertTrue(vampire.getNavigation().isIdle(), "a downed vampire mob does not move");
    context.assertFalse(vampire.tryAttack(human), "a downed vampire mob cannot bite");
    context.complete();
  }

  @GameTest(
      templateName = ARENA,
      batchId = NIGHT_BATCH,
      tickLimit = TestPlayers.READY + ESCAPED_AT + 10)
  public void anEscapingVampireMobFleesWhoeverDownedIt(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final VampireEntity control = still(context, new BlockPos(6, 2, 6));
    BloodAttachment.set(control, VampireHomeGameTest.HUNGRY);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> vampire.damage(vampire.getDamageSources().playerAttack(hunter), 1000.0F));
    context.runAtTick(
        TestPlayers.READY + ESCAPED_AT,
        () -> {
          context.assertTrue(
              DownedRules.flees(Downed.state(vampire)), "the vampire mob got up and escapes");
          context.assertTrue(vampire.getAttacker() == null, "the last hit is long forgotten");
          context.assertFalse(vampire.hunts(hunter), "an escaping vampire mob does not hunt");
          context.assertTrue(control.hunts(hunter), "a standing vampire mob hunts at night");
          context.assertFalse(vampire.tryAttack(hunter), "an escaping vampire mob does not bite");
          final FleeWhileEscapingGoal goal = new FleeWhileEscapingGoal(vampire, 1.4);
          boolean fled = false;
          for (int i = 0; i < TRIES && !fled; i++) {
            if (goal.canStart()) {
              goal.start();
              fled = vampire.getNavigation().getCurrentPath() != null;
            }
          }
          context.assertTrue(fled, "the vampire mob finds a way to flee");
          final Vec3d end =
              Vec3d.ofBottomCenter(vampire.getNavigation().getCurrentPath().getTarget());
          context.assertTrue(
              end.squaredDistanceTo(hunter.getPos())
                  > vampire.getPos().squaredDistanceTo(hunter.getPos()),
              "the vampire mob runs away from the hunter who downed it");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = NIGHT_BATCH, tickLimit = 20)
  public void aVampireMobBiteHealsItOnlyWithBlood(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 4));
    final IronGolemEntity golem = context.spawnEntity(EntityType.IRON_GOLEM, new BlockPos(3, 2, 4));
    cow.setAiDisabled(true);
    golem.setAiDisabled(true);
    vampire.setHealth(10.0F);
    context.assertTrue(vampire.tryAttack(cow), "a vampire mob bites prey");
    final float fed = vampire.getHealth();
    context.assertTrue(fed > 10.0F, "blood heals the vampire mob: " + fed);
    context.assertTrue(feeding(vampire), "a vampire mob that just fed counts as fed");
    context.assertFalse(vampire.tryAttack(golem), "an iron golem is not prey");
    vampire.setTarget(golem);
    context.assertTrue(vampire.tryAttack(golem), "a vampire mob fights back at its target");
    context.assertEquals(vampire.getHealth(), fed, "a bloodless bite heals nothing");
    context.complete();
  }

  @GameTest(templateName = ARENA, batchId = NOON_BATCH, tickLimit = 20)
  public void aVampireMobDoesNotHuntByDay(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(5, 2, 4));
    context.assertFalse(vampire.hunts(cow), "a vampire mob hunts only at night");
    context.complete();
  }

  @GameTest(templateName = YARD, batchId = DAWN_BATCH, skyAccess = true, tickLimit = 40)
  public void aVampireMobHeadsForTheNearestShadeAsDawnNears(final TestContext context) {
    roof(context, 0, 3);
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 7));
    context.runAtTick(
        LIGHT_SETTLED,
        () -> {
          context.assertTrue(ShadeRules.endangered(vampire), "open sky before dawn is a danger");
          assertHeadsFor(context, vampire, new BlockPos(4, 2, 3));
        });
  }

  @GameTest(templateName = YARD, batchId = DAWN_BATCH, skyAccess = true, tickLimit = 40)
  public void aVampireMobPassesShadeItCannotReach(final TestContext context) {
    for (int y = 2; y <= 4; y++) {
      for (int x = 3; x <= 7; x++) {
        for (int z = 0; z <= 4; z++) {
          final boolean inside = y < 4 && x > 3 && x < 7 && z > 0 && z < 4;
          context.setBlockState(new BlockPos(x, y, z), inside ? Blocks.AIR : Blocks.STONE);
        }
      }
    }
    roof(context, 7, 8);
    final VampireEntity vampire = still(context, new BlockPos(1, 2, 1));
    context.runAtTick(
        LIGHT_SETTLED,
        () -> {
          context.assertFalse(
              context.getWorld().isSkyVisible(context.getAbsolutePos(new BlockPos(5, 3, 2))),
              "the sealed room is shaded");
          final BlockPos target = startSeekingShade(context, vampire);
          final BlockPos spot = context.getRelativePos(target);
          final boolean inRoom =
              spot.getX() > 3 && spot.getX() < 7 && spot.getZ() > 0 && spot.getZ() < 4;
          context.assertFalse(inRoom, "it passes the sealed room it cannot walk into: " + spot);
          context.assertFalse(
              context.getWorld().isSkyVisible(target.up()), "it heads for cover: " + spot);
          context.complete();
        });
  }

  private static void roof(final TestContext context, final int fromZ, final int toZ) {
    for (int x = 0; x <= 8; x++) {
      for (int z = fromZ; z <= toZ; z++) {
        context.setBlockState(new BlockPos(x, 4, z), Blocks.STONE);
      }
    }
  }

  private static void assertHeadsFor(
      final TestContext context, final VampireEntity vampire, final BlockPos shade) {
    context.assertEquals(
        startSeekingShade(context, vampire),
        context.getAbsolutePos(shade),
        "it heads for the nearest shade it can reach");
    context.complete();
  }

  private static BlockPos startSeekingShade(
      final TestContext context, final VampireEntity vampire) {
    final SeekShadeGoal goal = new SeekShadeGoal(vampire, 1.3);
    vampire.setOnGround(true);
    context.assertTrue(goal.canStart(), "the vampire mob finds shade it can reach");
    goal.start();
    context.assertTrue(goal.shouldContinue(), "it keeps going while the sky is open");
    return vampire.getNavigation().getTargetPos();
  }

  @GameTest(templateName = YARD, batchId = DAWN_BATCH, skyAccess = true, tickLimit = 20)
  public void aVampireMobDropsItsPreyAsDawnNears(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    final CowEntity cow = context.spawnEntity(EntityType.COW, new BlockPos(6, 2, 6));
    cow.setAiDisabled(true);
    vampire.setTarget(cow);
    context.runAtTick(
        5,
        () -> {
          context.assertTrue(vampire.getTarget() == null, "the vampire mob gives up its prey");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobNeverBitesAVampirePlayerEvenWhenHit(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity player = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(Prey.isPrey(player), "a vampire player is not prey");
          vampire.damage(vampire.getDamageSources().playerAttack(player), 1.0F);
          vampire.setTarget(player);
          final float before = player.getHealth();
          context.assertFalse(vampire.tryAttack(player), "a vampire mob does not bite back");
          context.assertEquals(player.getHealth(), before, "the vampire player is unhurt");
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobBiteFeedsButNeverStartsBiteSickness(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float before = human.getHealth();
          context.assertTrue(vampire.tryAttack(human), "a vampire mob bites a human");
          context.assertTrue(human.getHealth() < before, "the bite draws blood");
          context.assertFalse(
              BiteSicknessAttachment.isSick(human), "a vampire mob's bite never turns");
          context.assertTrue(
              TestAdvancements.done(context, human, "love_bites"), "a bite earns Love Bites");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobKeepsItsLookSizeAndDownedStateThroughSaveAndLoad(
      final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    vampire.setAppearance(Identifier.of(Nightborn.MOD_ID, "female_morwen"), 1.62F, 0.88F);
    Downed.down(vampire, ServerConfigService.current().downed(), Optional.empty());
    final NbtCompound saved = vampire.writeNbt(new NbtCompound());
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    context.assertTrue(loaded != null, "a vampire mob can be created");
    loaded.readNbt(saved);
    context.assertEquals(loaded.lookId(), vampire.lookId(), "the look is kept");
    context.assertEquals(loaded.height(), 1.62F, "the height is kept");
    context.assertEquals(loaded.width(), 0.88F, "the build is kept");
    final DownedState state = Downed.state(loaded);
    context.assertTrue(state != null && state.isDowned(), "being downed is kept");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = YARD, skyAccess = true, tickLimit = 20)
  public void aVampireCanBringBats(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final int bats = BatEscort.gather(context.getWorld(), vampire, 100);
    context.assertTrue(bats >= 1 && bats <= 2, "a vampire brings one or two bats: " + bats);
    final int near =
        context
            .getWorld()
            .getEntitiesByType(
                NightbornMobs.VAMPIRE_BAT, Box.of(vampire.getPos(), 6, 6, 6), bat -> true)
            .size();
    context.assertEquals(near, bats, "the bats arrive beside the vampire");
    context.complete();
  }

  static VampireEntity still(final TestContext context, final BlockPos relative) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, relative);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static boolean feeding(final VampireEntity vampire) {
    return FeedingSessions.ticksSinceFed(vampire, vampire.getWorld().getTime()) >= 0;
  }
}
