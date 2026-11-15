package com.grahambartley.nightborn.gametest;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;

public class SunGameTest implements FabricGameTest {

  static final String BATCH = "sun";
  static final String ARENA = "nightborn:vampire_arena";
  static final int NOON = 6000;

  @BeforeBatch(batchId = BATCH)
  public void pinNoon(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
    world.setTimeOfDay(NOON);
    world.setWeather(12000, 0, false, false);
  }

  @AfterBatch(batchId = BATCH)
  public void restoreDaylight(final ServerWorld world) {
    world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(true, world.getServer());
  }

  @GameTest(
      templateName = ARENA,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void directSunBurnsAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(vampire.getHealth() < 20.0F, "the sun hurts a vampire");
          context.assertTrue(vampire.isOnFire(), "a vampire in the sun catches fire");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void fireResistanceDoesNotStopTheSun(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    vampire.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 1200));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertTrue(vampire.getHealth() < 20.0F, "fire resistance does not stop the sun");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void shadeProtectsAVampire(final TestContext context) {
    for (int x = 2; x <= 6; x++) {
      for (int z = 2; z <= 6; z++) {
        context.setBlockState(new BlockPos(x, 3, z), Blocks.STONE);
      }
    }
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 1, 4));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertFalse(vampire.getHealth() < 20.0F, "shade keeps a vampire whole");
          context.assertFalse(vampire.isOnFire(), "shade keeps a vampire from burning");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = ARENA,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 60)
  public void humansDoNotBurn(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(4, 1, 4));
    context.runAtTick(
        TestPlayers.READY + 45,
        () -> {
          context.assertEquals(human.getHealth(), 20.0F, "the sun does not hurt humans");
          context.assertFalse(human.isOnFire(), "humans do not catch fire in the sun");
          TestPlayers.finish(context, human);
        });
  }
}
