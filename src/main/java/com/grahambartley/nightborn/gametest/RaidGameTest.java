package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.guild.ContractMaps;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.raid.HouseNoise;
import com.grahambartley.nightborn.raid.NoiseAct;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.GameMode;

public final class RaidGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final String WAKE_BATCH = "raid-wake";
  static final String QUIET_BATCH = "raid-quiet";

  @BeforeBatch(batchId = WAKE_BATCH)
  public void pinNoonForWaking(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = WAKE_BATCH)
  public void releaseAfterWaking(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = QUIET_BATCH)
  public void pinNoonForQuiet(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = QUIET_BATCH)
  public void releaseAfterQuiet(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = WAKE_BATCH, tickLimit = 20)
  public void enoughNoiseWakesTheWholeHouseAgainstTheIntruder(final TestContext context) {
    final BlockPos first = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final BlockPos second = CoffinStage.coffin(context, new BlockPos(6, 2, 1), Direction.SOUTH);
    final VampireEntity near = sleeper(context, first, new BlockPos(1, 2, 4));
    final VampireEntity far = sleeper(context, second, new BlockPos(1, 2, 4));
    final ServerPlayerEntity hunter = intruder(context, new BlockPos(1, 2, 6));
    context.runAtTick(
        3,
        () -> {
          HouseNoise.make(hunter, NoiseAct.LID);
          context.assertFalse(CoffinRise.rising(near), "one lid alone leaves the house asleep");
          HouseNoise.make(hunter, NoiseAct.LID);
          context.assertTrue(CoffinRise.rising(near), "a second lid wakes the nearest sleeper");
          context.assertTrue(CoffinRise.rising(far), "the rest of the house wakes with it");
          context.assertTrue(near.getTarget() == hunter, "the woken house turns on the intruder");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, batchId = QUIET_BATCH, tickLimit = 20)
  public void aSneakingHunterWhoOnlyStakesLeavesTheHouseAsleep(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final VampireEntity sleeper = sleeper(context, head, new BlockPos(1, 2, 4));
    final ServerPlayerEntity hunter = intruder(context, new BlockPos(1, 2, 6));
    hunter.setSneaking(true);
    context.runAtTick(
        3,
        () -> {
          HouseNoise.make(hunter, NoiseAct.BLOW);
          context.assertTrue(sleeper.isSleeping(), "a single blow does not wake the house");
          context.assertFalse(CoffinRise.rising(sleeper), "the sleeper does not stir");
          context.complete();
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 20)
  public void aContractMapShowsTheRegionAroundTheHouseUnmarked(final TestContext context) {
    final BlockPos house = context.getAbsolutePos(new BlockPos(0, 1, 0)).add(300, 0, -170);
    final ItemStack map = ContractMaps.map(context.getWorld(), house);
    final MapState state = FilledMapItem.getMapState(map, context.getWorld());
    context.assertTrue(state != null, "the contract comes with a filled map");
    context.assertTrue(
        Math.abs(state.centerX - house.getX()) <= 64
            && Math.abs(state.centerZ - house.getZ()) <= 64,
        "the house lies within 64 blocks of the map's centre");
    context.assertTrue(!state.getDecorations().iterator().hasNext(), "the house is not marked");
    context.complete();
  }

  private static VampireEntity sleeper(
      final TestContext context, final BlockPos head, final BlockPos home) {
    final VampireEntity vampire = VampireMobGameTest.still(context, head.south());
    vampire.settleAt(context.getAbsolutePos(home));
    context.runAtTick(1, () -> vampire.sleep(context.getAbsolutePos(head)));
    return vampire;
  }

  private static ServerPlayerEntity intruder(final TestContext context, final BlockPos relative) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    final BlockPos at = context.getAbsolutePos(relative);
    player.refreshPositionAndAngles(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, 0.0F, 0.0F);
    return player;
  }
}
