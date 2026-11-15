package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.MobSpawns;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.structure.VampireStructureStarts;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class VampireBatSpawnGameTest implements FabricGameTest {

  static final String BATCH = "vampire-bat-spawns";
  static final String CROWD_BATCH = "vampire-bat-crowd";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int LIGHT_SETTLED = 10;
  static final long SEED = 1975L;
  static final int ATTEMPTS = 20;

  @BeforeBatch(batchId = BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = CROWD_BATCH)
  public void pinNightForTheCrowd(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = CROWD_BATCH)
  public void unpinNightForTheCrowd(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void noVampireBatSpawnsOnTheOpenSurfaceAtNight(final TestContext context) {
    try {
      context.assertFalse(
          spawns(context, surfaceGround(context)), "no vampire bat spawns in the open at night");
    } finally {
      clearSurface(context);
    }
    context.complete();
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void aVampireBatSpawnsInADarkCaveBelowSeaLevel(final TestContext context) {
    for (int x = 3; x <= 5; x++) {
      for (int y = 1; y <= 4; y++) {
        for (int z = 3; z <= 5; z++) {
          context.setBlockState(new BlockPos(x, y, z), Blocks.STONE);
        }
      }
    }
    context.setBlockState(SPOT, Blocks.AIR);
    context.setBlockState(SPOT.up(), Blocks.AIR);
    final BlockPos spot = context.getAbsolutePos(SPOT);
    context.runAtTick(
        LIGHT_SETTLED,
        () -> {
          context.assertTrue(
              spot.getY() < context.getWorld().getSeaLevel(), "the cave is below sea level");
          context.assertTrue(context.getWorld().getLightLevel(spot) == 0, "the cave is fully dark");
          context.assertTrue(spawns(context, spot), "a vampire bat spawns in the dark cave");
          context.complete();
        });
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void aVampireBatSpawnsInsideAVampireStructure(final TestContext context) {
    final BlockPos spot = surfaceGround(context);
    final PlacedStructure placed = place(context, VampireStructureGameTest.structureJson(64));
    try {
      context.assertTrue(
          VampireStructureStarts.anyContains(context.getWorld(), spot, structure -> true),
          "the spot is inside the placed structure");
      context.assertTrue(
          spawns(context, spot), "a vampire bat spawns inside a structure by default");
    } finally {
      placed.remove();
      clearSurface(context);
    }
    context.complete();
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void aStructureCanKeepVampireBatsOut(final TestContext context) {
    final BlockPos spot = surfaceGround(context);
    final PlacedStructure placed = place(context, false);
    try {
      context.assertTrue(
          VampireStructureStarts.anyContains(context.getWorld(), spot, structure -> true),
          "the spot is inside the placed structure");
      context.assertFalse(spawns(context, spot), "this structure's data spawns no bats");
    } finally {
      placed.remove();
      clearSurface(context);
    }
    context.complete();
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = CROWD_BATCH, skyAccess = true)
  public void noVampireBatSpawnsWhereTwoAlreadyFly(final TestContext context) {
    final BlockPos spot = surfaceGround(context);
    final BlockPos surface = surface(context);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, surface.add(-2, 1, -2)).setAiDisabled(true);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, surface.add(2, 1, 2)).setAiDisabled(true);
    final PlacedStructure placed = place(context, true);
    try {
      context.assertTrue(
          VampireStructureStarts.anyContains(context.getWorld(), spot, structure -> true),
          "the spot is inside the placed structure");
      context.assertFalse(spawns(context, spot), "no third vampire bat joins two nearby");
      context.assertTrue(
          MobSpawns.batCanSpawn(
              NightbornMobs.VAMPIRE_BAT,
              context.getWorld(),
              SpawnReason.SPAWN_EGG,
              spot,
              context.getWorld().getRandom()),
          "a spawn egg still works among them");
    } finally {
      placed.remove();
      clearSurface(context);
    }
    context.complete();
  }

  private static boolean spawns(final TestContext context, final BlockPos spot) {
    final ServerWorld world = context.getWorld();
    final Random random = Random.create(SEED);
    for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
      if (MobSpawns.batCanSpawn(
          NightbornMobs.VAMPIRE_BAT, world, SpawnReason.NATURAL, spot, random)) {
        return true;
      }
    }
    return false;
  }

  private static PlacedStructure place(final TestContext context, final boolean bats) {
    return place(
        context,
        VampireStructureGameTest.structureJson(64)
            .replace(
                "\"household\": {",
                "\"spawns_vampire_bats\": " + bats + ",\n          \"household\": {"));
  }

  private static PlacedStructure place(final TestContext context, final String json) {
    return PlacedStructure.place(
        context.getWorld(),
        VampireStructureGameTest.decode(context.getWorld(), json).getOrThrow(),
        BlockBox.create(
            context.getAbsolutePos(BlockPos.ORIGIN), context.getAbsolutePos(surface(context))));
  }

  private static BlockPos surface(final TestContext context) {
    return new BlockPos(
        4,
        context.getWorld().getSeaLevel() - context.getAbsolutePos(BlockPos.ORIGIN).getY() + 1,
        4);
  }

  private static BlockPos surfaceGround(final TestContext context) {
    final BlockPos surface = surface(context);
    context.setBlockState(surface.down(), Blocks.STONE);
    final BlockPos spot = context.getAbsolutePos(surface);
    context.assertTrue(
        spot.getY() >= context.getWorld().getSeaLevel(), "the spot is above sea level");
    return spot;
  }

  private static void clearSurface(final TestContext context) {
    context.setBlockState(surface(context).down(), Blocks.AIR);
  }
}
