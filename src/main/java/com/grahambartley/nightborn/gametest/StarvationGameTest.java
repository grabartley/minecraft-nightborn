package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;

public class StarvationGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "starving";

  private Difficulty difficulty = Difficulty.NORMAL;

  @BeforeBatch(batchId = BATCH)
  public void starveFast(final ServerWorld world) {
    difficulty = world.getDifficulty();
    world.getServer().setDifficulty(Difficulty.HARD, true);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(BloodSettings.STARVING_HURT_DELAY_SECONDS, 0)
            .with(BloodSettings.STARVING_HURT_SECONDS, 1));
  }

  @AfterBatch(batchId = BATCH)
  public void restore(final ServerWorld world) {
    world.getServer().setDifficulty(difficulty, true);
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(
                BloodSettings.STARVING_HURT_DELAY_SECONDS,
                BloodSettings.STARVING_HURT_DELAY_SECONDS.defaultValue())
            .with(
                BloodSettings.STARVING_HURT_SECONDS,
                BloodSettings.STARVING_HURT_SECONDS.defaultValue()));
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 40)
  public void anEmptyBarWeakensAndBloodMakesItFade(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    Levitation.request(vampire);
    BloodAttachment.set(vampire, 0);
    context.runAtTick(
        5,
        () -> {
          context.assertEquals(
              Weakened.sources(vampire).strength(Starvation.SOURCE),
              BloodSettings.STARVING_STEP.defaultValue(),
              "one step weakened");
          context.assertFalse(Levitation.isLevitating(vampire), "starving lands it");
          BloodAttachment.set(vampire, BloodAttachment.FULL / 2);
        });
    context.runAtTick(
        8,
        () -> {
          context.assertEquals(
              Weakened.sources(vampire).strength(Starvation.SOURCE),
              BloodSettings.STARVING_STEP.defaultValue() / 2,
              "half the weakness left at half a bar");
          BloodAttachment.set(vampire, BloodAttachment.FULL);
        });
    context.runAtTick(
        11,
        () -> {
          context.assertEquals(
              Weakened.sources(vampire).strength(Starvation.SOURCE), 0, "gone on a full bar");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 200)
  public void starvingHurtsToHalfAHeartButNeverDownsOrKills(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 1, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.setHealth(4.0F);
          BloodAttachment.set(vampire, 0);
        });
    context.runAtTick(
        TestPlayers.READY + 120,
        () -> {
          context.assertTrue(vampire.isAlive(), "alive");
          context.assertEquals(vampire.getHealth(), 1.0F, "half a heart left");
          context.assertTrue(Downed.state(vampire) == null, "not downed");
          TestPlayers.finish(context, vampire);
        });
  }
}
