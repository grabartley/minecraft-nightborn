package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinLayout;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.coffin.CoffinWood;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.coffin.RiseRules;
import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.BedPart;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity.SleepFailureReason;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.TeleportTarget;

public class StandingCoffinGameTest implements FabricGameTest {

  static final String DAY = "standing-coffin-day";
  static final String SKIP = "standing-coffin-skip";

  @BeforeBatch(batchId = DAY)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = DAY)
  public void releaseNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @BeforeBatch(batchId = SKIP)
  public void startAtNoon(final ServerWorld world) {
    WorldClock.runFrom(world, WorldClock.NOON);
    TestPlayers.disconnectEveryone(world.getServer());
  }

  @AfterBatch(batchId = SKIP)
  public void releaseSkip(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void sneakPlacingStandsTheCoffinUpWithItsLidTowardThePlacer(final TestContext context) {
    final ServerPlayerEntity placer = TestPlayers.human(context, new BlockPos(0, 2, 0));
    final Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    final BlockPos[] spots = {
      new BlockPos(2, 1, 2), new BlockPos(6, 1, 2), new BlockPos(2, 1, 6), new BlockPos(6, 1, 6)
    };
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placer.setSneaking(true);
          for (int i = 0; i < facings.length; i++) {
            placer.setYaw(facings[i].asRotation());
            context.useStackOnBlock(
                placer, new ItemStack(Coffins.get(CoffinWood.OAK)), spots[i], Direction.UP);
            final BlockPos foot = spots[i].up();
            final BlockState footState = context.getBlockState(foot);
            final BlockState headState = context.getBlockState(foot.up());
            context.assertTrue(
                footState.isOf(Coffins.get(CoffinWood.OAK))
                    && footState.get(BedBlock.PART) == BedPart.FOOT
                    && footState.get(CoffinBlock.STANDING),
                "the foot stands on the floor facing " + facings[i] + ", got " + footState);
            context.assertTrue(
                headState.isOf(Coffins.get(CoffinWood.OAK))
                    && headState.get(BedBlock.PART) == BedPart.HEAD
                    && headState.get(CoffinBlock.STANDING),
                "the head stands on the foot facing " + facings[i] + ", got " + headState);
            context.assertTrue(
                footState.get(BedBlock.FACING) == facings[i]
                    && headState.get(BedBlock.FACING) == facings[i],
                "its back is to the way the placer looked, " + facings[i]);
            context.assertTrue(
                context.getBlockState(foot.offset(facings[i])).isAir(),
                "nothing is laid flat along the floor");
          }
          TestPlayers.finish(context, placer);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void plainPlacingStillLaysTheCoffinFlat(final TestContext context) {
    final ServerPlayerEntity placer = TestPlayers.human(context, new BlockPos(0, 2, 0));
    final BlockPos floor = new BlockPos(4, 1, 6);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placer.setSneaking(false);
          placer.setYaw(Direction.NORTH.asRotation());
          context.useStackOnBlock(
              placer, new ItemStack(Coffins.get(CoffinWood.OAK)), floor, Direction.UP);
          final BlockState foot = context.getBlockState(floor.up());
          final BlockState head = context.getBlockState(floor.up().north());
          context.assertTrue(
              foot.isOf(Coffins.get(CoffinWood.OAK)) && !foot.get(CoffinBlock.STANDING),
              "the foot lies flat, got " + foot);
          context.assertTrue(
              head.isOf(Coffins.get(CoffinWood.OAK)) && head.get(BedBlock.PART) == BedPart.HEAD,
              "the head lies flat beyond it, got " + head);
          context.assertTrue(context.getBlockState(floor.up(2)).isAir(), "nothing stands up");
          TestPlayers.finish(context, placer);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aStandingCoffinNeedsTwoFreeCells(final TestContext context) {
    final ServerPlayerEntity placer = TestPlayers.human(context, new BlockPos(0, 2, 0));
    final BlockPos floor = new BlockPos(4, 1, 6);
    context.setBlockState(floor.up(2), Blocks.STONE);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          placer.setSneaking(true);
          placer.setYaw(Direction.NORTH.asRotation());
          context.useStackOnBlock(
              placer, new ItemStack(Coffins.get(CoffinWood.OAK)), floor, Direction.UP);
          context.assertTrue(
              context.getBlockState(floor.up()).isAir(),
              "a coffin with no room to stand is not placed");
          TestPlayers.finish(context, placer);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aVampireSleepsUprightOnTheFloorLookingOutThroughTheLid(final TestContext context) {
    final BlockPos foot = new BlockPos(4, 2, 6);
    final BlockPos head = CoffinStage.standing(context, foot, Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 7));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          context.assertTrue(vampire.isSleeping(), "the vampire rests: " + Diagnostics.of(vampire));
          context.assertTrue(
              vampire.getPose() == EntityPose.STANDING,
              "an upright sleeper keeps the standing pose, got " + vampire.getPose());
          final BlockPos absoluteFoot = context.getAbsolutePos(foot);
          context.assertTrue(
              vampire.getY() > absoluteFoot.getY() && vampire.getY() < absoluteFoot.getY() + 0.1,
              "their feet stand on the coffin's floor, y " + vampire.getY());
          context.assertTrue(
              vampire.getBlockPos().equals(absoluteFoot),
              "they stand inside the foot cell, at " + vampire.getBlockPos());
          context.assertTrue(
              vampire.getEyeY() > absoluteFoot.getY() + 1.5,
              "their eyes are at standing height, " + vampire.getEyeY());
          final Vec3d look = vampire.getRotationVector();
          context.assertTrue(
              look.z > 0.99, "they look out through the lid to the south, looking " + look);
          context.assertFalse(
              context.getBlockState(head).get(CoffinBlock.OPEN), "the lid shuts over them");
          context.assertTrue(
              insideTheCoffin(context, context.getAbsolutePos(head), vampire.getBoundingBox()),
              "their body stays inside the coffin's shell, box " + vampire.getBoundingBox());
          final Vec3d eye = Vec3d.ofCenter(context.getAbsolutePos(head.south(2)));
          final Vec3d target = vampire.getBoundingBox().getCenter();
          final Vec3d through = eye.add(target.subtract(eye).multiply(2.0));
          final double toCoffin =
              context
                  .getWorld()
                  .raycast(
                      new RaycastContext(
                          eye,
                          through,
                          RaycastContext.ShapeType.OUTLINE,
                          RaycastContext.FluidHandling.NONE,
                          ShapeContext.absent()))
                  .getPos()
                  .distanceTo(eye);
          final double toSleeper =
              vampire.getBoundingBox().raycast(eye, through).map(eye::distanceTo).orElse(99.0);
          context.assertTrue(
              toCoffin < toSleeper,
              "aiming at a shut coffin hits the coffin before the sleeper, coffin "
                  + toCoffin
                  + " sleeper "
                  + toSleeper);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DAY,
      skyAccess = true,
      tickLimit = TestPlayers.READY + 80)
  public void aShutStandingCoffinShadesItsSleeperAndAnOpenOneDoesNot(final TestContext context) {
    final BlockPos head = CoffinStage.standing(context, new BlockPos(4, 2, 6), Direction.NORTH);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 7));
    final ServerPlayerEntity exposed = TestPlayers.vampire(context, new BlockPos(1, 2, 2));
    final float[] settled = new float[1];
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          vampire.extinguish();
          settled[0] = vampire.getHealth();
          context.assertTrue(CoffinSleepers.shelteredBy(vampire), "a shut lid shelters them");
        });
    context.runAtTick(
        TestPlayers.READY + 60,
        () -> {
          context.assertTrue(vampire.isSleeping(), "the vampire still rests");
          context.assertTrue(
              exposed.isOnFire(),
              "the sun burns a vampire in the open: " + Diagnostics.of(exposed));
          context.assertTrue(
              vampire.getHealth() >= settled[0] && !vampire.isOnFire(),
              "the sun does not reach a shut standing coffin: " + Diagnostics.of(vampire));
          final ServerPlayerEntity opener = TestPlayers.human(context, new BlockPos(5, 2, 7));
          opener.setSneaking(true);
          context.useBlock(head, opener);
          context.assertTrue(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid opens");
          context.assertFalse(
              CoffinSleepers.shelteredBy(vampire), "an open lid no longer shelters them");
          TestPlayers.finish(context, vampire, exposed, opener);
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DAY,
      tickLimit = TestPlayers.READY + RiseRules.STEP_OUT_TICKS + 10)
  public void aSleeperStepsOutAndRespawnsOnTheFloorInFront(final TestContext context) {
    final BlockPos foot = new BlockPos(4, 2, 4);
    final BlockPos head = CoffinStage.standing(context, foot, Direction.EAST);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final BlockPos inFront = context.getAbsolutePos(foot.west());
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head, vampire);
          context.assertTrue(vampire.isSleeping(), "the vampire rests");
          context.assertTrue(
              context.getAbsolutePos(head).equals(vampire.getSpawnPointPosition()),
              "resting sets the respawn point to the coffin");
          final TeleportTarget respawn = vampire.getRespawnTarget(false, TeleportTarget.NO_OP);
          context.assertFalse(respawn.missingRespawnBlock(), "the standing coffin is a respawn");
          context.assertTrue(
              BlockPos.ofFloored(respawn.pos()).equals(inFront),
              "they respawn on the floor in front, at " + respawn.pos());
          vampire.wakeUp(false, true);
          context.assertTrue(
              CoffinRise.rising(vampire) && vampire.isSleeping(),
              "waking starts the step out with the vampire still held in the coffin");
          context.assertTrue(context.getBlockState(head).get(CoffinBlock.OPEN), "the lid opens");
        });
    context.runAtTick(
        TestPlayers.READY + RiseRules.STEP_OUT_TICKS - 2,
        () ->
            context.assertTrue(
                vampire.isSleeping(), "the vampire is held in the coffin until the step out ends"));
    context.runAtTick(
        TestPlayers.READY + RiseRules.STEP_OUT_TICKS + 2,
        () -> {
          context.assertTrue(
              vampire.getBlockPos().equals(inFront),
              "they step out onto the floor in front, at " + vampire.getBlockPos());
          context.assertTrue(
              vampire.getPose() == EntityPose.STANDING
                  && !vampire.isSleeping()
                  && !CoffinRise.rising(vampire),
              "they are awake and standing");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aBlockInFrontOfTheLidRefusesRest(final TestContext context) {
    final BlockPos foot = new BlockPos(4, 2, 4);
    final BlockPos head = CoffinStage.standing(context, foot, Direction.NORTH);
    context.setBlockState(head.south(), Blocks.STONE);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.assertTrue(
              vampire
                  .trySleep(context.getAbsolutePos(head))
                  .left()
                  .filter(reason -> reason == SleepFailureReason.OBSTRUCTED)
                  .isPresent(),
              "a coffin whose lid cannot open is obstructed");
          context.assertFalse(vampire.isSleeping(), "the vampire stays awake");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = TestPlayers.READY + 10)
  public void aSecondVampireIsRefusedAStandingCoffin(final TestContext context) {
    final BlockPos head = CoffinStage.standing(context, new BlockPos(4, 2, 4), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity first = TestPlayers.vampire(context, new BlockPos(3, 2, 5));
    final ServerPlayerEntity second = TestPlayers.vampire(context, new BlockPos(5, 2, 5));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          context.useBlock(head.down(), first);
          context.useBlock(head, second);
          context.assertTrue(first.isSleeping(), "using the lower cell lies the first one down");
          context.assertFalse(second.isSleeping(), "the second vampire is refused");
          TestPlayers.finish(context, first, second);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = 20)
  public void breakingEitherCellDropsOneCoffinAndTheWallBehindIsFree(final TestContext context) {
    final BlockPos top = CoffinStage.standing(context, new BlockPos(2, 2, 4), Direction.NORTH);
    final BlockPos bottom =
        CoffinStage.standing(context, new BlockPos(6, 2, 4), Direction.NORTH).down();
    final BlockPos wall = new BlockPos(4, 2, 3);
    final BlockPos kept = CoffinStage.standing(context, new BlockPos(4, 2, 4), Direction.NORTH);
    context.setBlockState(wall, Blocks.STONE);
    context.runAtTick(
        2,
        () -> {
          context.getWorld().breakBlock(context.getAbsolutePos(top), true);
          context.getWorld().breakBlock(context.getAbsolutePos(bottom), true);
          context.getWorld().breakBlock(context.getAbsolutePos(wall), false);
          context.expectBlock(Blocks.AIR, top);
          context.expectBlock(Blocks.AIR, top.down());
          context.expectBlock(Blocks.AIR, bottom);
          context.expectBlock(Blocks.AIR, bottom.up());
          context.assertTrue(
              CoffinBlock.isStanding(context.getBlockState(kept))
                  && CoffinBlock.isStanding(context.getBlockState(kept.down())),
              "taking away the wall behind leaves a standing coffin whole");
          final int dropped =
              context
                  .getWorld()
                  .getEntitiesByClass(ItemEntity.class, context.getTestBox(), item -> true)
                  .stream()
                  .mapToInt(item -> item.getStack().getCount())
                  .sum();
          context.assertTrue(dropped == 2, "two broken coffins drop two coffins, got " + dropped);
          context.complete();
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = SKIP,
      tickLimit = TestPlayers.READY + CoffinGameTest.SLEEP_READY + RiseRules.STEP_OUT_TICKS + 20)
  public void aVampireSleepingUprightPassesTheDay(final TestContext context) {
    final BlockPos head = CoffinStage.standing(context, new BlockPos(4, 2, 4), Direction.NORTH);
    CoffinStage.roof(context);
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(4, 2, 6));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          vampire.extinguish();
          context.useBlock(head, vampire);
        });
    context.runAtTick(
        TestPlayers.READY + CoffinGameTest.SLEEP_READY + RiseRules.STEP_OUT_TICKS + 10,
        () -> {
          final long time = Math.floorMod(context.getWorld().getTimeOfDay(), NightTime.DAY_LENGTH);
          context.assertTrue(
              time >= NightTime.DUSK && time < NightTime.DUSK + CoffinGameTest.SLEEP_READY,
              "a vampire resting upright passes the day to dusk, time " + time);
          context.assertFalse(vampire.isSleeping(), "they rise at dusk");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = 60)
  public void aSleepingVampireMobCannotBePushedOutThroughTheCoffin(final TestContext context) {
    final BlockPos standingHead =
        CoffinStage.standing(context, new BlockPos(2, 2, 4), Direction.NORTH);
    final BlockPos flatHead = CoffinStage.coffin(context, new BlockPos(6, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final VampireEntity upright = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(2, 2, 6));
    final VampireEntity lying = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(6, 2, 7));
    upright.setAiDisabled(true);
    lying.setAiDisabled(true);
    final BlockPos absoluteStanding = context.getAbsolutePos(standingHead);
    final BlockPos absoluteFlat = context.getAbsolutePos(flatHead);
    context.runAtTick(
        2,
        () -> {
          upright.sleep(absoluteStanding);
          lying.sleep(absoluteFlat);
          context.assertTrue(
              upright.getPose() == EntityPose.STANDING,
              "a mob in a standing coffin sleeps upright, got " + upright.getPose());
          context.assertTrue(
              lying.getPose() == EntityPose.SLEEPING, "a mob in a flat coffin lies down");
        });
    for (int tick = 5; tick < 45; tick += 5) {
      context.runAtTick(
          tick,
          () -> {
            upright.move(MovementType.SELF, new Vec3d(0.4, 0.0, 0.4));
            lying.move(MovementType.SELF, new Vec3d(-0.4, 0.0, 0.4));
            upright.setVelocity(0.5, 0.0, 0.5);
            lying.setVelocity(-0.5, 0.0, 0.5);
          });
    }
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(upright.isSleeping() && lying.isSleeping(), "both mobs still sleep");
          context.assertTrue(
              insideTheCoffin(context, absoluteStanding, upright.getBoundingBox()),
              "the upright mob's body stays inside its coffin's shell, box "
                  + upright.getBoundingBox());
          context.assertTrue(
              insideTheCoffin(context, absoluteFlat, lying.getBoundingBox()),
              "the lying mob's body stays inside its coffin's shell, box "
                  + lying.getBoundingBox());
          context.complete();
        });
  }

  @GameTest(
      templateName = CoffinStage.YARD,
      batchId = DAY,
      tickLimit = TestPlayers.READY + RiseRules.STEP_OUT_TICKS + 10)
  public void aWakingVampireIsClearOfTheCoffinOnEveryFacing(final TestContext context) {
    final Direction[] facings = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    final BlockPos[] feet = {
      new BlockPos(2, 2, 2), new BlockPos(6, 2, 2), new BlockPos(2, 2, 6), new BlockPos(6, 2, 6)
    };
    final BlockPos[] heads = new BlockPos[4];
    final ServerPlayerEntity[] vampires = new ServerPlayerEntity[4];
    for (int i = 0; i < 4; i++) {
      heads[i] = CoffinStage.standing(context, feet[i], facings[i]);
      vampires[i] = TestPlayers.vampire(context, feet[i].offset(facings[i].getOpposite()));
    }
    CoffinStage.roof(context);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          for (int i = 0; i < 4; i++) {
            context.useBlock(heads[i], vampires[i]);
            context.assertTrue(vampires[i].isSleeping(), "rests facing " + facings[i]);
            vampires[i].wakeUp(false, true);
          }
        });
    context.runAtTick(
        TestPlayers.READY + RiseRules.STEP_OUT_TICKS + 2,
        () -> {
          for (int i = 0; i < 4; i++) {
            context.assertTrue(
                !vampires[i].isSleeping() && context.getWorld().isSpaceEmpty(vampires[i]),
                "a vampire stepping out of a coffin facing "
                    + facings[i]
                    + " is awake and not inside it, at "
                    + vampires[i].getPos());
          }
          TestPlayers.finish(context, vampires);
        });
  }

  @GameTest(templateName = CoffinStage.YARD, batchId = DAY, tickLimit = RiseRules.RISE_TICKS + 20)
  public void aWokenVampireMobRisesOrStepsOutThenStandsClearFacingOut(final TestContext context) {
    final BlockPos standingHead =
        CoffinStage.standing(context, new BlockPos(2, 2, 4), Direction.NORTH);
    final BlockPos flatHead = CoffinStage.coffin(context, new BlockPos(6, 2, 6), Direction.NORTH);
    CoffinStage.roof(context);
    final VampireEntity upright = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(2, 2, 6));
    final VampireEntity lying = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(6, 2, 7));
    upright.setAiDisabled(true);
    lying.setAiDisabled(true);
    final BlockPos absoluteFlat = context.getAbsolutePos(flatHead);
    final Vec3d heels =
        CoffinLayout.heels(absoluteFlat, Direction.NORTH, lying.getEyeHeight(EntityPose.STANDING));
    final float outward = CoffinLayout.outwardYaw(Direction.NORTH);
    context.runAtTick(
        2,
        () -> {
          upright.sleep(context.getAbsolutePos(standingHead));
          lying.sleep(absoluteFlat);
          upright.wakeUp();
          lying.wakeUp();
          context.assertTrue(
              CoffinRise.rising(upright) && upright.isSleeping(),
              "a woken mob in a standing coffin starts to step out");
          context.assertTrue(
              CoffinRise.rising(lying)
                  && lying.isSleeping()
                  && lying.getPose() == EntityPose.STANDING,
              "a woken mob in a flat coffin starts to rise, got " + lying.getPose());
          context.assertTrue(
              lying.getPos().squaredDistanceTo(heels) < 1.0E-6,
              "the mob pivots on its heels at " + heels + ", not " + lying.getPos());
        });
    context.runAtTick(
        RiseRules.STEP_OUT_TICKS + 4,
        () ->
            context.assertTrue(
                !upright.isSleeping() && context.getWorld().isSpaceEmpty(upright),
                "the mob steps out clear of the standing coffin, at " + upright.getPos()));
    context.runAtTick(
        RiseRules.RISE_TICKS + 4,
        () -> {
          context.assertTrue(
              !lying.isSleeping() && !CoffinRise.rising(lying),
              "the mob is awake once the rise ends");
          context.assertTrue(
              context.getWorld().isSpaceEmpty(lying),
              "the mob stands clear of the flat coffin, at " + lying.getPos());
          context.assertTrue(
              Math.abs(MathHelper.wrapDegrees(lying.getYaw() - outward)) < 1.0F
                  && Math.abs(MathHelper.wrapDegrees(upright.getYaw() - outward)) < 1.0F,
              "both mobs still face the way they rose, yaws "
                  + lying.getYaw()
                  + " and "
                  + upright.getYaw());
          context.complete();
        });
  }

  private static boolean insideTheCoffin(
      final TestContext context, final BlockPos head, final Box body) {
    final BlockState headState = context.getWorld().getBlockState(head);
    final BlockPos other =
        head.offset(
            CoffinLayout.towardsOtherPart(
                BedPart.HEAD, headState.get(BedBlock.FACING), headState.get(CoffinBlock.STANDING)));
    final VoxelShape coffin = VoxelShapes.union(outline(context, head), outline(context, other));
    return !VoxelShapes.matchesAnywhere(
        VoxelShapes.cuboid(body.contract(1.0E-3)), coffin, BooleanBiFunction.ONLY_FIRST);
  }

  private static VoxelShape outline(final TestContext context, final BlockPos cell) {
    return context
        .getWorld()
        .getBlockState(cell)
        .getOutlineShape(context.getWorld(), cell)
        .offset(cell.getX(), cell.getY(), cell.getZ());
  }
}
