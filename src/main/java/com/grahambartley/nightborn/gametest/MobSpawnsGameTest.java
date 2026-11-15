package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.MobSpawns;
import com.grahambartley.nightborn.mob.NightbornMobs;
import java.util.Arrays;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class MobSpawnsGameTest implements FabricGameTest {

  static final String NOON_BATCH = "mob-spawns-noon";
  static final BlockPos SPOT = new BlockPos(4, 2, 4);

  @BeforeBatch(batchId = NOON_BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = NOON_BATCH)
  public void unpinNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(
      templateName = VampireMobGameTest.YARD,
      batchId = NOON_BATCH,
      skyAccess = true,
      tickLimit = 20)
  public void noVampireBatSpawnsInTheOpenByDay(final TestContext context) {
    final ServerWorld world = context.getWorld();
    final BlockPos spot = groundAt(context);
    context.assertFalse(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.NATURAL, spot, world.getRandom()),
        "no vampire bat spawns in daylight");
    context.assertTrue(
        MobSpawns.batCanSpawn(
            NightbornMobs.VAMPIRE_BAT, world, SpawnReason.SPAWN_EGG, spot, world.getRandom()),
        "a spawn egg works at any hour");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 1)
  public void noBiomeSpawnsVampireMobs(final TestContext context) {
    final List<Identifier> spawning =
        context
            .getWorld()
            .getRegistryManager()
            .get(RegistryKeys.BIOME)
            .streamEntries()
            .filter(
                biome ->
                    Arrays.stream(SpawnGroup.values())
                        .flatMap(
                            group ->
                                biome
                                    .value()
                                    .getSpawnSettings()
                                    .getSpawnEntries(group)
                                    .getEntries()
                                    .stream())
                        .anyMatch(entry -> entry.type == NightbornMobs.VAMPIRE))
            .map(biome -> biome.registryKey().getValue())
            .toList();
    context.assertTrue(
        spawning.isEmpty(), "vampire mobs spawn only in their structures, not in " + spawning);
    context.complete();
  }

  private static BlockPos groundAt(final TestContext context) {
    context.setBlockState(SPOT.down(), Blocks.STONE);
    return context.getAbsolutePos(SPOT);
  }
}
