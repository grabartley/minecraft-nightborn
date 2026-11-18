package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.sun.DirectSun;
import com.grahambartley.nightborn.sun.ParasolWear;
import com.grahambartley.nightborn.sun.Parasols;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ParasolGameTest implements FabricGameTest {

  static final String BATCH = "parasol";
  static final String YARD = "nightborn:vampire_yard";
  static final int SHADED = 40;
  static final int WEARS_OUT = 60;
  static final double STRIDE = 0.25;
  static final double PACE = 4.0;
  static final int WALK_TICKS = 80;

  @BeforeBatch(batchId = BATCH)
  public void pinNoon(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.NOON);
  }

  @AfterBatch(batchId = BATCH)
  public void releaseNoon(final ServerWorld world) {
    WorldClock.release(world);
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = SHADED + 5)
  public void anOpenParasolShadesItsHolderUntilItCloses(final TestContext context) {
    final VampireEntity vampire = shaded(context);
    final ItemStack parasol = vampire.getStackInHand(Hand.MAIN_HAND);
    context.runAtTick(
        SHADED,
        () -> {
          context.assertTrue(DirectSun.reaches(vampire), "the vampire stands in direct sun");
          context.assertFalse(vampire.isOnFire(), "an open parasol keeps the sun off");
          context.assertTrue(parasol.getDamage() > 0, "the parasol wears while the sun is on it");
          Parasols.setOpen(vampire, parasol, false);
        });
    context.runAtTick(
        SHADED + 1,
        () -> {
          context.assertTrue(vampire.isOnFire(), "the vampire burns the tick it closes");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = 20)
  public void aBlowClosesTheParasol(final TestContext context) {
    final VampireEntity vampire = shaded(context);
    vampire.setInvulnerable(false);
    final ZombieEntity zombie = context.spawnEntity(EntityType.ZOMBIE, new BlockPos(6, 2, 4));
    zombie.setAiDisabled(true);
    zombie.setInvulnerable(true);
    context.runAtTick(
        5,
        () -> {
          vampire.damage(context.getWorld().getDamageSources().mobAttack(zombie), 1.0F);
          context.assertFalse(
              Parasols.isOpen(vampire.getStackInHand(Hand.MAIN_HAND)), "a blow closes it");
        });
    context.runAtTick(
        6,
        () -> {
          context.assertTrue(vampire.isOnFire(), "the struck vampire burns at once");
          context.complete();
        });
  }

  @GameTest(templateName = YARD, batchId = BATCH, skyAccess = true, tickLimit = WEARS_OUT)
  public void aParasolWornThroughBreaksAndItsHolderBurns(final TestContext context) {
    final VampireEntity vampire = shaded(context);
    vampire.getStackInHand(Hand.MAIN_HAND).setDamage(ParasolWear.DURABILITY - 1);
    context.runAtEveryTick(
        () -> {
          if (vampire.getStackInHand(Hand.MAIN_HAND).isEmpty()) {
            context.assertTrue(vampire.isOnFire(), "the holder burns the tick it breaks");
            context.complete();
          }
        });
  }

  @GameTest(
      templateName = YARD,
      batchId = BATCH,
      skyAccess = true,
      tickLimit = TestPlayers.READY + WALK_TICKS + 5)
  public void aVampirePlayerWalksInTheSunUnderAParasolInTheOffHand(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final ItemStack parasol = new ItemStack(Parasols.PARASOL);
    vampire.setStackInHand(Hand.OFF_HAND, parasol);
    Parasols.setOpen(vampire, parasol, true);
    final Vec3d start = context.getAbsolute(new Vec3d(2.5, 2.0, 4.5));
    context.runAtEveryTick(
        () -> {
          final long walked = Math.max(0L, context.getTick() - TestPlayers.READY);
          final double along = (walked * STRIDE) % (2.0 * PACE);
          final double x = start.x + (along < PACE ? along : 2.0 * PACE - along);
          vampire.refreshPositionAfterTeleport(x, start.y, start.z);
        });
    context.runAtTick(
        TestPlayers.READY + WALK_TICKS,
        () -> {
          context.assertFalse(vampire.isOnFire(), "the parasol shades its holder");
          context.assertTrue(
              TestAdvancements.done(context, vampire, "made_in_the_shade"),
              "walking in the sun under a parasol earns Made In The Shade");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, tickLimit = 2)
  public void woolMendsAParasol(final TestContext context) {
    context.assertTrue(
        Parasols.PARASOL.canRepair(
            new ItemStack(Parasols.PARASOL), new ItemStack(Items.BLACK_WOOL)),
        "wool mends a parasol");
    context.complete();
  }

  private static VampireEntity shaded(final TestContext context) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, new BlockPos(4, 2, 4));
    vampire.setAiDisabled(true);
    vampire.setInvulnerable(true);
    final ItemStack parasol = new ItemStack(Parasols.PARASOL);
    vampire.setStackInHand(Hand.MAIN_HAND, parasol);
    Parasols.setOpen(vampire, parasol, true);
    return vampire;
  }
}
