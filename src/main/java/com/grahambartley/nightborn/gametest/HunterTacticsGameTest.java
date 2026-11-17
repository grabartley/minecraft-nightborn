package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.ammo.BoltEntity;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterAction;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.ward.Wards;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public class HunterTacticsGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 5;
  static final String STAKE_KIT = "nightborn:stake";
  static final String CROSSBOW_KIT = "nightborn:crossbow";

  private static HunterEntity hunter(final TestContext context, final BlockPos pos) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, pos);
    hunter.setAiDisabled(true);
    return hunter;
  }

  private static VampireEntity vampire(final TestContext context, final BlockPos pos) {
    final VampireEntity vampire = context.spawnEntity(NightbornMobs.VAMPIRE, pos);
    vampire.setAiDisabled(true);
    return vampire;
  }

  private static void fight(final HunterEntity hunter, final int ticks) {
    for (int tick = 0; tick < ticks; tick++) {
      hunter.fight();
    }
  }

  @GameTest(templateName = ARENA, tickLimit = START + 20)
  public void aCrossbowHunterLoosesAStakeBoltPartWayThroughItsShot(final TestContext context) {
    final HunterEntity hunter = hunter(context, new BlockPos(1, 2, 4));
    final VampireEntity vampire = vampire(context, new BlockPos(7, 2, 4));
    context.runAtTick(
        START,
        () -> {
          hunter.arm(CROSSBOW_KIT);
          hunter.setTarget(vampire);
          context.assertTrue(
              hunter.getMainHandStack().getItem() instanceof CrossbowItem, "it carries a crossbow");
          hunter.shootAt(vampire, 1.0F);
          fight(hunter, HunterAction.CROSSBOW_SHOT.releaseTick() - 1);
          context.assertEquals(
              hunter.action(), HunterAction.CROSSBOW_SHOT, "it shows the shot while it aims");
          context.assertTrue(
              context
                  .getWorld()
                  .getEntitiesByClass(BoltEntity.class, context.getTestBox(), bolt -> true)
                  .isEmpty(),
              "nothing flies before the release");
          fight(hunter, 1);
          context.assertEquals(
              context
                  .getWorld()
                  .getEntitiesByClass(
                      BoltEntity.class, context.getTestBox(), bolt -> bolt.getOwner() == hunter)
                  .size(),
              1,
              "one bolt leaves at the release");
          fight(hunter, HunterAction.CROSSBOW_SHOT.lengthTicks());
          context.assertEquals(hunter.action(), HunterAction.NONE, "and the shot ends");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 20)
  public void aHunterRaisesItsCrucifixAtAVampireInReachAndLowersItAfter(final TestContext context) {
    final HunterEntity hunter = hunter(context, new BlockPos(3, 2, 4));
    final VampireEntity vampire = vampire(context, new BlockPos(5, 2, 4));
    context.runAtTick(
        START,
        () -> {
          hunter.arm(STAKE_KIT);
          context.assertFalse(Wards.wards(hunter), "it holds no crucifix with nothing to hunt");
          hunter.setTarget(vampire);
          fight(hunter, 1);
          context.assertTrue(Wards.wards(hunter), "it holds a crucifix up at the vampire");
          context.assertEquals(hunter.action(), HunterAction.RAISE_CRUCIFIX, "and shows it raised");
          hunter.setTarget(null);
          fight(hunter, 1);
          context.assertFalse(Wards.wards(hunter), "it puts the crucifix away after");
          context.assertEquals(hunter.action(), HunterAction.NONE, "and lowers its arm");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 20)
  public void aHunterThrowsHolyWaterAtAVampireOutOfReach(final TestContext context) {
    final HunterEntity hunter = hunter(context, new BlockPos(1, 2, 4));
    final VampireEntity vampire = vampire(context, new BlockPos(7, 2, 4));
    context.runAtTick(
        START,
        () -> {
          hunter.arm(STAKE_KIT);
          hunter.setTarget(vampire);
          fight(hunter, 1);
          context.assertEquals(
              hunter.action(), HunterAction.THROW_HOLY_WATER, "it winds up a throw");
          context.assertTrue(
              HolyWater.isHolyWater(hunter.getOffHandStack()), "with holy water in its off hand");
          fight(hunter, HunterAction.THROW_HOLY_WATER.releaseTick());
          context.assertEquals(
              context
                  .getWorld()
                  .getEntitiesByClass(
                      PotionEntity.class,
                      context.getTestBox(),
                      potion -> HolyWater.isHolyWater(potion.getStack()))
                  .size(),
              1,
              "one flask of holy water is in the air");
          context.assertTrue(hunter.getOffHandStack().isEmpty(), "and its hand is empty again");
          fight(hunter, HunterAction.THROW_HOLY_WATER.lengthTicks());
          context.assertTrue(
              hunter.action() != HunterAction.THROW_HOLY_WATER,
              "it does not throw again straight away");
          context.complete();
        });
  }
}
