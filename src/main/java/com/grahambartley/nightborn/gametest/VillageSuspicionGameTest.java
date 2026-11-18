package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.Villages;
import com.grahambartley.nightborn.village.WitnessReports;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class VillageSuspicionGameTest implements FabricGameTest {

  static final String BATCH = "village-suspicion";
  static final String YARD = "nightborn:vampire_yard";
  static final BlockPos BELL = new BlockPos(1, 2, 1);
  static final int CHECKED = 45;

  @BeforeBatch(batchId = BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
    WitnessReports.clear();
  }

  @AfterBatch(batchId = BATCH)
  public void restoreDaylight(final ServerWorld world) {
    WorldClock.release(world);
    WitnessReports.clear();
  }

  @GameTest(templateName = YARD, batchId = BATCH, tickLimit = CHECKED + 5)
  public void aWitnessStoppedBeforeTheBellTellsNobody(final TestContext context) {
    final BlockPos bell = context.getAbsolutePos(BELL);
    final ServerPlayerEntity vampire = context.createMockCreativeServerPlayerInWorld();
    final VillagerEntity witness = witness(context, new BlockPos(7, 2, 7));
    WitnessReports.saw(context.getWorld(), witness, vampire, SuspicionCause.SEEN_FEEDING);
    context.runAtTick(1, witness::kill);
    context.runAtTick(
        CHECKED,
        () -> {
          context.assertTrue(
              Villages.of(context.getWorld()).at(bell).isEmpty(),
              "a silenced witness should raise no suspicion");
          context.complete();
        });
  }

  private static VillagerEntity witness(final TestContext context, final BlockPos at) {
    context.setBlockState(BELL, Blocks.BELL);
    final VillagerEntity villager = context.spawnEntity(EntityType.VILLAGER, at);
    villager.setAiDisabled(true);
    villager
        .getBrain()
        .remember(
            MemoryModuleType.MEETING_POINT,
            GlobalPos.create(context.getWorld().getRegistryKey(), context.getAbsolutePos(BELL)));
    return villager;
  }
}
