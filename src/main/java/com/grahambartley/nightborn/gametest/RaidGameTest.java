package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.guild.ContractMaps;
import com.grahambartley.nightborn.guild.ExplorationMaps;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.raid.HouseGuardGoal;
import com.grahambartley.nightborn.raid.HouseNoise;
import com.grahambartley.nightborn.raid.NoiseAct;
import com.grahambartley.nightborn.raid.Trails;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.Thralls;
import com.grahambartley.nightborn.wild.RumourMaps;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class RaidGameTest implements FabricGameTest {

  static final String ARENA = VampireMobGameTest.ARENA;
  static final String WAKE_BATCH = "raid-wake";
  static final String QUIET_BATCH = "raid-quiet";
  static final String GUARD_BATCH = "raid-guard";
  static final int LIMIT = TestPlayers.READY + 10;

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

  @BeforeBatch(batchId = GUARD_BATCH)
  public void pinNoonForGuards(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = GUARD_BATCH)
  public void releaseAfterGuards(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = ARENA, batchId = WAKE_BATCH, tickLimit = LIMIT)
  public void enoughNoiseWakesTheWholeHouseAgainstTheIntruder(final TestContext context) {
    final BlockPos home = new BlockPos(1, 2, 4);
    final BlockPos first = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final BlockPos second = CoffinStage.coffin(context, new BlockPos(6, 2, 1), Direction.SOUTH);
    final VampireEntity near = resident(context, first, home);
    final VampireEntity far = resident(context, second, home);
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(1, 2, 6));
    context.runAtTick(1, () -> near.sleep(context.getAbsolutePos(first)));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          HouseNoise.make(hunter, NoiseAct.LID);
          context.assertFalse(CoffinRise.rising(near), "one lid alone leaves the house asleep");
          far.sleep(context.getAbsolutePos(second));
          HouseNoise.make(hunter, NoiseAct.LID);
          context.assertTrue(CoffinRise.rising(near), "a second lid wakes the sleeper near it");
          context.assertTrue(
              CoffinRise.rising(far), "a housemate who heard only one lid wakes with the house");
          context.assertTrue(near.getTarget() == hunter, "the woken house turns on the intruder");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = QUIET_BATCH, tickLimit = LIMIT)
  public void aSingleBlowLeavesTheHouseAsleep(final TestContext context) {
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final VampireEntity sleeper = resident(context, head, new BlockPos(1, 2, 4));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(1, 2, 6));
    context.runAtTick(1, () -> sleeper.sleep(context.getAbsolutePos(head)));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          HouseNoise.make(hunter, NoiseAct.BLOW);
          context.assertTrue(sleeper.isSleeping(), "a single blow does not wake the house");
          context.assertFalse(CoffinRise.rising(sleeper), "the sleeper does not stir");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, batchId = GUARD_BATCH, tickLimit = LIMIT)
  public void aGuardThatSeesAnIntruderWakesTheHouse(final TestContext context) {
    final BlockPos home = new BlockPos(1, 2, 4);
    final BlockPos head = CoffinStage.coffin(context, new BlockPos(1, 2, 1), Direction.SOUTH);
    final VampireEntity sleeper = resident(context, head, home);
    final ThrallEntity guard = context.spawnEntity(Thralls.THRALL, new BlockPos(5, 2, 4));
    guard.setAiDisabled(true);
    guard.keepHouse(context.getAbsolutePos(home));
    final ServerPlayerEntity hunter = TestPlayers.human(context, new BlockPos(5, 2, 7));
    context.runAtTick(1, () -> sleeper.sleep(context.getAbsolutePos(head)));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final HouseGuardGoal watch =
              new HouseGuardGoal(guard, guard::house, () -> true, () -> {});
          context.assertTrue(watch.canStart(), "the guard sees the intruder");
          watch.start();
          context.assertTrue(guard.getTarget() == hunter, "the guard goes for the intruder");
          context.assertTrue(CoffinRise.rising(sleeper), "the guard wakes the house");
          context.assertTrue(sleeper.getTarget() == hunter, "the house turns on the intruder");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = LIMIT)
  public void aHunterWhoWoundsAVampireFollowsItsTrail(final TestContext context) {
    final VampireEntity quarry = VampireMobGameTest.still(context, new BlockPos(2, 2, 2));
    quarry.settleAt(context.getAbsolutePos(new BlockPos(2, 2, 2)));
    final ServerPlayerEntity hunter = TestPlayers.hunter(context, new BlockPos(6, 2, 6));
    final ServerPlayerEntity stranger = TestPlayers.human(context, new BlockPos(6, 2, 2));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          quarry.damage(hunter.getDamageSources().playerAttack(hunter), 1.0F);
          quarry.damage(stranger.getDamageSources().playerAttack(stranger), 1.0F);
          context.assertTrue(
              Trails.lead(hunter).filter(lead -> lead.equals(quarry.getPos())).isPresent(),
              "the hunter's trail leads to the wounded vampire");
          context.assertTrue(
              Trails.lead(stranger).isEmpty(), "a human who is not a hunter gets no trail");
          TestPlayers.finish(context, hunter, stranger);
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

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aRumourMapMarksTheHouseEntrance(final TestContext context) {
    final BlockPos entrance = context.getAbsolutePos(new BlockPos(0, 1, 0)).add(-260, 0, 410);
    final ItemStack map =
        ExplorationMaps.marking(context.getWorld(), entrance, (byte) 2, RumourMaps.NAME_KEY);
    final MapDecorationsComponent marks = map.get(DataComponentTypes.MAP_DECORATIONS);
    context.assertTrue(
        marks != null
            && marks.decorations().values().stream()
                .anyMatch(
                    mark ->
                        mark.type().matches(MapDecorationTypes.RED_X)
                            && (int) mark.x() == entrance.getX()
                            && (int) mark.z() == entrance.getZ()),
        "the rumour map puts its cross on the entrance");
    context.complete();
  }

  private static VampireEntity resident(
      final TestContext context, final BlockPos head, final BlockPos home) {
    final VampireEntity vampire = VampireMobGameTest.still(context, head.south());
    vampire.settleAt(context.getAbsolutePos(home));
    return vampire;
  }
}
