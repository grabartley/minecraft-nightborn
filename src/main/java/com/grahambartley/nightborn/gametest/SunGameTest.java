package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.sun.DirectSun;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class SunGameTest implements FabricGameTest {

  static final String BATCH = "sun";
  static final String DUSK_BATCH = "sun-dusk";
  static final int DUSK = 12800;
  static final int SIDE_BY_SIDE_TICKS = 400;
  static final String YARD = "nightborn:vampire_yard";
  static final int BURN_OUT_TICKS = 900;
  static final int IGNITED = 20;

  @BeforeBatch(batchId = BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = BATCH)
  public void restoreDaylight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = DUSK_BATCH)
  public void pinDusk(final ServerWorld world) {
    WorldClock.freeze(world, DUSK);
  }

  @AfterBatch(batchId = DUSK_BATCH)
  public void restoreAfterDusk(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = SIDE_BY_SIDE_TICKS + 1)
  public void aVampireMobBurnsAtNoonLikeAZombie(final TestContext context) {
    sideBySide(context, true);
  }

  @GameTest(
      templateName = YARD,
      batchId = DUSK_BATCH,
      skyAccess = true,
      tickLimit = SIDE_BY_SIDE_TICKS + 1)
  public void aVampireMobStopsBurningAtDuskLikeAZombie(final TestContext context) {
    sideBySide(context, false);
  }

  private static void sideBySide(final TestContext context, final boolean burns) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(3, 2, 4));
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(5, 2, 4));
    vampire.setAiDisabled(true);
    zombie.setAiDisabled(true);
    vampire.setInvulnerable(true);
    zombie.setInvulnerable(true);
    final boolean[] caught = new boolean[2];
    context.runAtEveryTick(
        () -> {
          caught[0] |= vampire.isOnFire();
          caught[1] |= zombie.isOnFire();
        });
    context.runAtTick(
        SIDE_BY_SIDE_TICKS,
        () -> {
          context.assertEquals(caught[1], burns, "the zombie burns: " + burns);
          context.assertEquals(
              caught[0], caught[1], "the vampire burns exactly when the zombie does");
          context.complete();
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void directSunBurnsAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(vampire.getHealth() < 20.0F, "the sun hurts a vampire");
          context.assertTrue(vampire.isOnFire(), "a vampire in the sun catches fire");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "sun_dried"),
              "burning in the sun and living earns Sun Dried");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = BURN_OUT_TICKS)
  public void aVampireMobInTheOpenBurnsUntilItDies(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    context.runAtEveryTick(
        () -> {
          if (context.getTick() < IGNITED) {
            return;
          }
          if (!vampire.isAlive()) {
            context.complete();
            return;
          }
          context.assertTrue(
              vampire.isOnFire() && DirectSun.on(vampire),
              "a vampire mob in the open never stops burning, tick " + context.getTick());
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = BURN_OUT_TICKS)
  public void powderSnowDoesNotShelterAVampireMobFromTheSun(final TestContext context) {
    final BlockPos feet = new BlockPos(4, 2, 4);
    context.setBlockState(feet, Blocks.POWDER_SNOW);
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, feet);
    vampire.setAiDisabled(true);
    final boolean[] sunk = new boolean[1];
    context.runAtEveryTick(
        () -> {
          sunk[0] |= vampire.inPowderSnow;
          if (vampire.isAlive()) {
            return;
          }
          context.assertTrue(sunk[0], "the vampire stood in powder snow");
          context.complete();
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void fireResistanceDoesNotStopTheSun(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    vampire.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(
              vampire.getHealth() < 20.0F,
              "fire resistance does not stop the sun: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void shadeProtectsAVampire(final TestContext context) {
    for (int x = 2; x <= 6; x++) {
      for (int z = 2; z <= 6; z++) {
        context.setBlockState(new BlockPos(x, 4, z), Blocks.STONE);
      }
    }
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final float[] settled = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.extinguish();
          settled[0] = vampire.getHealth();
        });
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertFalse(
              vampire.getHealth() < settled[0],
              "shade keeps a vampire whole: " + Diagnostics.of(vampire));
          context.assertFalse(vampire.isOnFire(), "shade keeps a vampire from burning");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void humansDoNotBurn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertEquals(human.getHealth(), 20.0F, "the sun does not hurt humans");
          context.assertFalse(human.isOnFire(), "humans do not catch fire in the sun");
          TestPlayers.finish(context, human);
        });
  }
}
