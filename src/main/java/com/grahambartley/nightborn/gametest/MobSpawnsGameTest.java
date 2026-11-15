package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.MobSpawns;
import com.grahambartley.nightborn.mob.NightbornMobs;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class MobSpawnsGameTest implements FabricGameTest {

  static final String NOON_BATCH = "mob-spawns-noon";
  static final String NIGHT_BATCH = "mob-spawns-night";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.pin(world, VampireMobGameTest.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = NIGHT_BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.pin(world, VampireMobGameTest.MIDNIGHT);
  }

  @AfterBatch(batchId = NIGHT_BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(
      templateName = VampireMobGameTest.YARD,
      batchId = NOON_BATCH,
      skyAccess = true,
      tickLimit = 20)
  public void noVampireOrVampireBatSpawnsInTheOpenByDay(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos spot = groundAt(context);
    context.assertFalse(
        MobSpawns.vampireCanSpawn(
            NightbornMobs.VAMPIRE, world, SpawnReason.NATURAL, spot, world.getRandom()),
        "no vampire spawns under the noon sun");
    context.assertFalse(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.NATURAL, spot, world.getRandom()),
        "no vampire bat spawns under the noon sun");
    context.assertTrue(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.SPAWN_EGG, spot, world.getRandom()),
        "a spawn egg works at any hour");
    context.complete();
  }

  @GameTest(
      templateName = VampireMobGameTest.YARD,
      batchId = NIGHT_BATCH,
      skyAccess = true,
      tickLimit = 20)
  public void noVampireBatSpawnsWhereTwoAlreadyFly(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos spot = groundAt(context);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(2, 3, 2)).setAiDisabled(true);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(6, 3, 6)).setAiDisabled(true);
    context.assertFalse(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.NATURAL, spot, world.getRandom()),
        "no third vampire bat joins two nearby");
    context.assertTrue(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.SPAWN_EGG, spot, world.getRandom()),
        "a spawn egg still works among them");
    context.complete();
  }

  private static BlockPos groundAt(final TestContext context) {
    context.setBlockState(SPOT.down(), Blocks.STONE);
    return context.getAbsolutePos(SPOT);
  }
}
