package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.ammo.BoltEntity;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.CrossbowGoal;
import com.grahambartley.nightborn.guild.GuildData;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterAction;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.ward.Wards;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class HunterTacticsGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final int START = 5;
  static final String STAKE_KIT = "nightborn:stake";
  static final String CROSSBOW_KIT = "nightborn:crossbow";
  static final String SILVER_KIT = "nightborn:silver_stake";
  static final int DRAWN_FOR = 100;

  private static HunterEntity hunter(final TestContext context, final BlockPos pos) {
    final HunterEntity hunter = context.spawnEntity(GuildMobs.HUNTER, pos);
    hunter.setAiDisabled(true);
    TestHunters.unskilled(context, hunter);
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
          fight(hunter, 1);
          context.assertTrue(
              hunter.getMainHandStack().getItem() instanceof CrossbowItem, "it draws a crossbow");
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
  public void aHunterKeepsItsHandsEmptyUntilItHuntsAndPutsItsWeaponAwayAfter(
      final TestContext context) {
    final HunterEntity hunter = hunter(context, new BlockPos(1, 2, 4));
    final VampireEntity vampire = vampire(context, new BlockPos(7, 2, 2));
    context.runAtTick(
        START,
        () -> {
          hunter.arm(SILVER_KIT);
          fight(hunter, 1);
          context.assertFalse(hunter.armed(), "a hunter with nothing to hunt is at ease");
          context.assertTrue(hunter.getMainHandStack().isEmpty(), "with nothing in its hand");
          hunter.setTarget(vampire);
          fight(hunter, 1);
          context.assertTrue(hunter.armed(), "it readies itself when it hunts");
          context.assertTrue(
              hunter.getMainHandStack().isOf(GearItems.SILVER_STAKE), "and draws its stake");
          hunter.setTarget(null);
          fight(hunter, DRAWN_FOR - 1);
          context.assertTrue(hunter.armed(), "it stays ready for a while after");
          fight(hunter, 1);
          context.assertFalse(hunter.armed(), "then stands at ease again");
          context.assertTrue(hunter.getMainHandStack().isEmpty(), "and puts its stake away");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 20)
  public void onlyAHunterWithABoltStandsOffToShoot(final TestContext context) {
    final HunterEntity archer = hunter(context, new BlockPos(1, 2, 2));
    final HunterEntity staker = hunter(context, new BlockPos(1, 2, 6));
    final VampireEntity vampire = vampire(context, new BlockPos(7, 2, 4));
    context.runAtTick(
        START,
        () -> {
          archer.arm(CROSSBOW_KIT);
          staker.arm(STAKE_KIT);
          archer.setTarget(vampire);
          staker.setTarget(vampire);
          context.assertTrue(
              new CrossbowGoal(archer, archer::standsOff).canStart(),
              "a crossbow hunter stands off and shoots");
          context.assertFalse(
              new CrossbowGoal(staker, staker::standsOff).canStart(),
              "a stake hunter closes in instead");
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
  public void aStakeHunterAddsItsKitsDamageToAVampireButNotToAnyoneElse(final TestContext context) {
    final HunterEntity staker = hunter(context, new BlockPos(3, 2, 4));
    final VampireEntity vampire = vampire(context, new BlockPos(4, 2, 4));
    final PigEntity pig = context.spawnEntity(EntityType.PIG, new BlockPos(2, 2, 4));
    pig.setAiDisabled(true);
    context.runAtTick(
        START,
        () -> {
          staker.arm(STAKE_KIT);
          staker.equipStack(EquipmentSlot.MAINHAND, new ItemStack(GearItems.WOODEN_STAKE));
          final float blow =
              (float) staker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
          final float kit =
              GuildData.kits(staker.getRegistryManager())
                  .get(Identifier.of("nightborn", "stake"))
                  .damage();
          context.assertTrue(kit > 0.0F, "the stake kit carries damage of its own");
          context.assertEquals(
              lost(vampire, () -> staker.tryAttack(vampire)), blow + kit, "a vampire takes it");
          context.assertEquals(lost(pig, () -> staker.tryAttack(pig)), blow, "a pig does not");
          context.complete();
        });
  }

  private static float lost(final LivingEntity target, final Runnable blow) {
    target.timeUntilRegen = 0;
    final float before = target.getHealth();
    blow.run();
    return before - target.getHealth();
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
          fight(hunter, HunterAction.THROW_HOLY_WATER.lengthTicks() * 2);
          context.assertEquals(
              context
                  .getWorld()
                  .getEntitiesByClass(
                      PotionEntity.class,
                      context.getTestBox(),
                      potion -> HolyWater.isHolyWater(potion.getStack()))
                  .size(),
              1,
              "it does not throw again straight away");
          context.complete();
        });
  }

  @GameTest(templateName = ARENA, tickLimit = START + 20)
  public void aHunterSavedWithAWeaponInHandLoadsAtEaseWithEmptyHands(final TestContext context) {
    final HunterEntity hunter = hunter(context, new BlockPos(4, 2, 4));
    context.runAtTick(
        START,
        () -> {
          hunter.equipStack(EquipmentSlot.MAINHAND, new ItemStack(GearItems.WOODEN_STAKE));
          final NbtCompound saved = new NbtCompound();
          hunter.writeNbt(saved);
          final HunterEntity loaded = GuildMobs.HUNTER.create(context.getWorld());
          loaded.readNbt(saved);
          loaded.fight();
          context.assertFalse(loaded.armed(), "a loaded hunter with nothing to hunt is at ease");
          context.assertTrue(loaded.getMainHandStack().isEmpty(), "with nothing in its hand");
          context.complete();
        });
  }
}
