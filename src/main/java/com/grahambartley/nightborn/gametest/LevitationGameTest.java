package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class LevitationGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final double FLOOR = 1.0;
  static final double HOVER = FLOOR + 1.0;

  private static VampireEntity still(final TestContext context, final BlockPos at) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, at);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static double feet(final TestContext context, final VampireEntity vampire) {
    return context.getRelative(vampire.getPos()).y;
  }

  @GameTest(templateName = ARENA, tickLimit = 60)
  public void aLevitatingVampireFloatsOneBlockAboveTheGround(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 1, 4));
    context.runAtTick(5, () -> context.assertTrue(Levitation.start(vampire), "it may levitate"));
    context.runAtTick(
        50,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, vampire) - HOVER) < 0.05,
              "feet one block up, at " + feet(context, vampire));
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 90)
  public void overADropItSinksSlowlyAndTakesNoFallDamage(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 3, 4));
    Levitation.start(vampire);
    final float health = vampire.getHealth();
    context.runAtTick(
        10,
        () ->
            context.assertTrue(
                feet(context, vampire) > HOVER + 0.4,
                "still sinking slowly, at " + feet(context, vampire)));
    context.runAtTick(
        80,
        () -> {
          context.assertTrue(
              Math.abs(feet(context, vampire) - HOVER) < 0.05, "settled one block up");
          context.assertEquals(vampire.getHealth(), health, "no fall damage");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 40)
  public void aWeakenedVampireLandsAtOnce(final TestContext context) {
    final VampireEntity vampire = still(context, new BlockPos(4, 1, 4));
    Levitation.start(vampire);
    context.runAtTick(10, () -> Weakened.add(vampire, "garlic", 10, 200));
    context.runAtTick(
        12,
        () -> {
          context.assertFalse(Levitation.isLevitating(vampire), "weakness lands it");
          context.assertFalse(Levitation.start(vampire), "and it cannot start again");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void onlyVampirePlayersLevitate(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(5, 1, 4));
    Levitation.request(vampire);
    Levitation.request(human);
    context.assertTrue(Levitation.isLevitating(vampire), "the vampire floats");
    context.assertFalse(Levitation.isLevitating(human), "the human does not");
    Levitation.request(vampire);
    context.assertFalse(Levitation.isLevitating(vampire), "pressing again lands");
    TestPlayers.finish(context, vampire, human);
  }

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void becomingHumanLands(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    Levitation.request(vampire);
    VampireTransitions.makeHuman(vampire);
    context.assertFalse(Levitation.isLevitating(vampire), "a human never stays levitating");
    TestPlayers.finish(context, vampire);
  }

  @GameTest(templateName = ARENA, tickLimit = 60)
  public void levitatingKeepsWalkingPace(final TestContext context) {
    final VampireEntity walker = still(context, new BlockPos(2, 1, 1));
    final VampireEntity floater = still(context, new BlockPos(6, 1, 1));
    walker.refreshPositionAndAngles(walker.getX(), walker.getY(), walker.getZ(), 0.0F, 0.0F);
    floater.refreshPositionAndAngles(floater.getX(), floater.getY(), floater.getZ(), 0.0F, 0.0F);
    context.runAtTick(5, () -> Levitation.start(floater));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= 25) {
            walker.setForwardSpeed(1.0F);
            floater.setForwardSpeed(1.0F);
            walker.setMovementSpeed(0.1F);
            floater.setMovementSpeed(0.1F);
          }
        });
    final Vec3d[] start = new Vec3d[2];
    context.runAtTick(
        30,
        () -> {
          start[0] = walker.getPos();
          start[1] = floater.getPos();
        });
    context.runAtTick(
        45,
        () -> {
          final double walked = walker.getPos().subtract(start[0]).horizontalLength();
          final double floated = floater.getPos().subtract(start[1]).horizontalLength();
          context.assertTrue(walked > 0.5, "the walker moved " + walked);
          context.assertTrue(
              Math.abs(floated - walked) < walked * 0.15,
              "floated " + floated + " against walked " + walked);
          context.complete();
        });
  }
}
