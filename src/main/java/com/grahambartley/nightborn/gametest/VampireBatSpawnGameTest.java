package com.grahambartley.nightborn.gametest;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.mob.MobSpawns;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public class VampireBatSpawnGameTest implements FabricGameTest {

  static final String BATCH = "vampire-bat-spawns";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);
  static final int LIGHT_SETTLED = 10;

  @BeforeBatch(batchId = BATCH)
  public void pinNight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BATCH)
  public void unpinNight(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void noVampireBatSpawnsOnTheOpenSurfaceAtNight(final TestContext context) {
    final int relativeSeaLevel =
        context.getWorld().getSeaLevel() - context.getAbsolutePos(BlockPos.ORIGIN).getY();
    final BlockPos surface = new BlockPos(4, relativeSeaLevel + 1, 4);
    context.setBlockState(surface.down(), Blocks.STONE);
    try {
      final BlockPos spot = context.getAbsolutePos(surface);
      context.assertTrue(
          spot.getY() >= context.getWorld().getSeaLevel(), "the spot is above sea level");
      context.assertFalse(spawns(context, spot), "no vampire bat spawns in the open at night");
    } finally {
      context.setBlockState(surface.down(), Blocks.AIR);
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
    final BlockPos spot = groundAt(context);
    final PlacedStructure placed = place(context, VampireStructureGameTest.structureJson(64));
    try {
      context.assertTrue(
          spawns(context, spot), "a vampire bat spawns inside a structure by default");
    } finally {
      placed.remove();
    }
    context.complete();
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void aStructureCanKeepVampireBatsOut(final TestContext context) {
    final BlockPos spot = groundAt(context);
    final PlacedStructure placed = place(context, false);
    try {
      context.assertFalse(spawns(context, spot), "this structure's data spawns no bats");
    } finally {
      placed.remove();
    }
    context.complete();
  }

  @GameTest(templateName = VampireMobGameTest.YARD, batchId = BATCH, skyAccess = true)
  public void noVampireBatSpawnsWhereTwoAlreadyFly(final TestContext context) {
    final BlockPos spot = groundAt(context);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(2, 3, 2)).setAiDisabled(true);
    context.spawnEntity(NightbornMobs.VAMPIRE_BAT, new BlockPos(6, 3, 6)).setAiDisabled(true);
    final PlacedStructure placed = place(context, true);
    try {
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
    }
    context.complete();
  }

  private static boolean spawns(final TestContext context, final BlockPos spot) {
    final ServerWorld world = context.getWorld();
    return MobSpawns.batCanSpawn(
        NightbornMobs.VAMPIRE_BAT, world, SpawnReason.NATURAL, spot, world.getRandom());
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
    final Structure structure =
        Structure.STRUCTURE_CODEC
            .parse(
                RegistryOps.of(JsonOps.INSTANCE, context.getWorld().getRegistryManager()),
                JsonParser.parseString(json))
            .getOrThrow();
    return PlacedStructure.place(
        context.getWorld(), structure, VampireStructureGameTest.arena(context));
  }

  private static BlockPos groundAt(final TestContext context) {
    context.setBlockState(SPOT.down(), Blocks.STONE);
    return context.getAbsolutePos(SPOT);
  }
}
