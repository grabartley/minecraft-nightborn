package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.Unsticking;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.stage.StageStuck;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class UnstickingGameTest implements FabricGameTest {

  static final String YARD = "nightborn:vampire_yard";
  static final String BATCH = "unsticking";
  static final int SETTLE_TICKS = 10;
  static final StageStuck QUICK = new StageStuck(2, 0.5, 2.0, 1.5, 3, 1, 3, 10, 6);

  @BeforeBatch(batchId = BATCH)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BATCH)
  public void release(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 40)
  public void aVampireThatGetsNoCloserLandsStepsBackAndGivesUp(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(2, 2, 4));
    vampire.setAiDisabled(true);
    final VillagerEntity quarry = context.spawnEntity(EntityType.VILLAGER, new BlockPos(7, 2, 4));
    quarry.setAiDisabled(true);
    context.runAtTick(
        SETTLE_TICKS,
        () -> {
          vampire.setTarget(quarry);
          boolean landed = false;
          boolean stuck = false;
          for (int tick = 0; tick < 30 && vampire.getTarget() != null; tick++) {
            Unsticking.tick(vampire, QUICK);
            landed |= Unsticking.holdsDown(vampire);
            stuck |= Unsticking.stuck(vampire);
          }
          context.assertTrue(stuck, "it counts itself stuck");
          context.assertTrue(landed, "it is held on foot");
          context.assertTrue(vampire.getTarget() == null, "it gives up its quarry");
          context.assertTrue(Unsticking.forgoes(vampire, quarry), "and leaves it alone");
          context.assertFalse(vampire.canTarget(quarry), "so no target goal picks it again");
          context.complete();
        });
  }
}
