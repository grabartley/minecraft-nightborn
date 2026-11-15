package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.mob.BatEscort;
import com.grahambartley.nightborn.mob.MobBite;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.Prey;
import com.grahambartley.nightborn.mob.VampireBatEntity;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;

public class VampireMobGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String YARD = "nightborn:vampire_yard";
  static final String NOON_BATCH = "vampire-mob-noon";
  static final String DAWN_BATCH = "vampire-mob-dawn";
  static final String BITE_BATCH = "vampire-mob-bite";
  static final int NOON = 6000;
  static final int BEFORE_DAWN = 22500;
  static final int TRIES = 40;
  static final int LANDED = 10;

  private int mobTurnChanceBefore;
  private int batTurnChanceBefore;

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    pinTime(world, NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    unpinTime(world);
  }

  @BeforeBatch(batchId = DAWN_BATCH)
  public void pinDawn(final ServerWorld world) {
    pinTime(world, BEFORE_DAWN);
  }

  @AfterBatch(batchId = DAWN_BATCH)
  public void unpinDawn(final ServerWorld world) {
    unpinTime(world);
  }

  @BeforeBatch(batchId = BITE_BATCH)
  public void everyBiteTurns(final ServerWorld world) {
    mobTurnChanceBefore = ServerConfigService.current().turning().vampireMobTurnChance();
    batTurnChanceBefore = ServerConfigService.current().turning().vampireBatTurnChance();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(TurningSettings.VAMPIRE_MOB_TURN_CHANCE, 100)
            .with(TurningSettings.VAMPIRE_BAT_TURN_CHANCE, 100));
  }

  @AfterBatch(batchId = BITE_BATCH)
  public void restoreTurnChances(final ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(TurningSettings.VAMPIRE_MOB_TURN_CHANCE, mobTurnChanceBefore)
            .with(TurningSettings.VAMPIRE_BAT_TURN_CHANCE, batTurnChanceBefore));
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobIsAVampire(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    context.assertTrue(Vampires.isVampire(vampire), "the vampire mob counts as a vampire");
    context.assertFalse(Prey.isPrey(vampire), "a vampire mob is never prey");
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
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
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

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void anEscapingVampireMobFleesItsHunter(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    final PlayerEntity hunter = context.createMockPlayer(GameMode.SURVIVAL);
    final Vec3d threat = context.getAbsolute(new Vec3d(1.5, 2.0, 4.5));
    hunter.setPosition(threat);
    context.runAtTick(
        LANDED,
        () -> {
          vampire.setAttacker(hunter);
          vampire.setAttached(Downed.TYPE, new DownedState(DownedState.Phase.ESCAPING, 1200));
          final FleeWhileEscapingGoal goal = new FleeWhileEscapingGoal(vampire, 1.4);
          boolean fled = false;
          for (int i = 0; i < TRIES && !fled; i++) {
            if (goal.canStart()) {
              goal.start();
              fled = vampire.getNavigation().getCurrentPath() != null;
            }
          }
          context.assertTrue(fled, "an escaping vampire mob finds a way to flee its hunter");
          context.assertFalse(vampire.tryAttack(hunter), "an escaping vampire mob does not fight");
          final Vec3d end =
              Vec3d.ofBottomCenter(vampire.getNavigation().getCurrentPath().getTarget());
          context.assertTrue(
              end.squaredDistanceTo(threat) > vampire.getPos().squaredDistanceTo(threat),
              "the vampire mob runs away from its hunter");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = DAWN_BATCH, skyAccess = true, tickLimit = 20)
  public void aVampireMobSeeksShadeAsDawnNears(final TestContext context) {
    for (int x = 0; x <= 8; x++) {
      for (int z = 0; z <= 3; z++) {
        context.setBlockState(new BlockPos(x, 4, z), Blocks.STONE);
      }
    }
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 7));
    final SeekShadeGoal goal = new SeekShadeGoal(vampire, 1.3);
    context.assertTrue(SeekShadeGoal.exposed(vampire), "open sky before dawn is a danger");
    Vec3d shade = null;
    for (int i = 0; i < TRIES && shade == null; i++) {
      shade = goal.locateShadedPos();
    }
    context.assertTrue(shade != null, "the vampire mob finds shade");
    final BlockPos head = BlockPos.ofFloored(shade).up();
    context.assertFalse(
        context.getWorld().isSkyVisible(head), "the spot it picks is under cover: " + head);
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobIgnoresVampirePlayersUnlessAttacked(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity player = TestPlayers.vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertFalse(Prey.isPrey(player), "a vampire player is not prey");
          context.assertFalse(vampire.tryAttack(player), "an unprovoked vampire mob leaves it be");
          vampire.setAttacker(player);
          context.assertTrue(vampire.tryAttack(player), "a vampire mob bites back when attacked");
          TestPlayers.finish(context, player);
        });
  }

  @GameTest(templateName = ARENA, batchId = BITE_BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireMobBiteCanStartBiteSickness(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final float before = human.getHealth();
          context.assertTrue(vampire.tryAttack(human), "a vampire mob bites a human");
          context.assertTrue(human.getHealth() < before, "the bite draws blood");
          context.assertTrue(BiteSicknessAttachment.isSick(human), "the bite starts sickness");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = BITE_BATCH, tickLimit = TestPlayers.READY + 10)
  public void aVampireBatBiteCanStartBiteSickness(final TestContext context) {
    final VampireBatEntity bat =
        context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(4, 3, 4));
    bat.setAiDisabled(true);
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final TurningSettings turning = ServerConfigService.current().turning();
          context.assertTrue(
              MobBite.bite(bat, human, 2.0F, turning.vampireBatTurnChance(), turning),
              "a vampire bat bites a human");
          context.assertTrue(BiteSicknessAttachment.isSick(human), "the bite starts sickness");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aVampireMobKeepsItsLookAndDownedStateThroughSaveAndLoad(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 2, 4));
    vampire.setLook(4);
    Downed.down(vampire, ServerConfigService.current().downed(), Optional.empty());
    final NbtCompound saved = vampire.writeNbt(new NbtCompound());
    final VampireEntity loaded = NightbornMobs.VAMPIRE.create(context.getWorld());
    context.assertTrue(loaded != null, "a vampire mob can be created");
    loaded.readNbt(saved);
    context.assertEquals(loaded.look().index(), 4, "the look is kept");
    final DownedState state = Downed.state(loaded);
    context.assertTrue(state != null && state.isDowned(), "being downed is kept");
    loaded.discard();
    context.complete();
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aNightVampireCanBringBats(final TestContext context) {
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

  private static VampireEntity still(final TestContext context, final BlockPos relative) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, relative);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static void pinTime(final ServerWorld world, final int time) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(time);
    world.setWeather(12000, 0, false, false);
  }

  private static void unpinTime(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }
}
