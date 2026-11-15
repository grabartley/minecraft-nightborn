package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.ammo.BoltEntity;
import com.grahambartley.nightborn.ammo.GarlicFumesEffect;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.weakened.Weakened;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class HunterAmmoGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final float BOLT_SPEED = 3.15F;
  static final BlockPos HUNTER = new BlockPos(4, 2, 1);
  static final BlockPos TARGET = new BlockPos(4, 2, 5);
  static final int FIRE_AT = 5;
  static final int LANDED = FIRE_AT + 10;

  @GameTest(templateName = ARENA, tickLimit = 20)
  public void aCrossbowLoadsEveryKindOfHunterAmmo(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, HUNTER);
    final ItemStack crossbow = new ItemStack(Items.CROSSBOW);
    for (final Item ammo : AmmoItems.ALL) {
      hunter.getInventory().clear();
      hunter.getInventory().insertStack(new ItemStack(ammo, 4));
      context.assertTrue(
          hunter.getProjectileType(crossbow).isOf(ammo), "the crossbow draws " + ammo);
      context.assertFalse(
          hunter.getProjectileType(new ItemStack(Items.BOW)).isOf(ammo), "a bow never does");
    }
    TestPlayers.finish(context, hunter);
  }

  @GameTest(templateName = ARENA, tickLimit = LANDED + 5)
  public void aStakeBoltStakesADownedVampire(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, HUNTER);
    final VampireEntity vampire = AmmoTargets.vampire(context, TARGET);
    vampire.damage(vampire.getDamageSources().generic(), 1000.0F);
    context.assertTrue(
        Downed.state(vampire) != null && Downed.state(vampire).isDowned(), "the vampire is down");
    context.runAtTick(FIRE_AT, () -> fire(context, hunter, AmmoItems.STAKE_BOLT));
    context.runAtTick(
        LANDED,
        () -> {
          context.assertFalse(vampire.isAlive(), "the stake bolt finished the vampire");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = LANDED * 2 + 5)
  public void aSilverBoltHitsAVampireHarderThanAnArrow(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, HUNTER);
    final VampireEntity silvered = AmmoTargets.vampire(context, TARGET);
    final float[] arrowLoss = new float[1];
    context.runAtTick(FIRE_AT, () -> fire(context, hunter, Items.ARROW));
    context.runAtTick(
        LANDED,
        () -> {
          arrowLoss[0] = AmmoTargets.TOUGH - silvered.getHealth();
          silvered.setHealth(AmmoTargets.TOUGH);
          silvered.timeUntilRegen = 0;
          fire(context, hunter, AmmoItems.SILVER_BOLT);
        });
    context.runAtTick(
        LANDED * 2,
        () -> {
          final float silverLoss = AmmoTargets.TOUGH - silvered.getHealth();
          context.assertTrue(arrowLoss[0] > 0.0F, "the arrow hit");
          context.assertEquals(
              silverLoss - arrowLoss[0],
              (float) ServerConfigService.current().gear().silverDamage(),
              "silver adds its bonus, arrow " + arrowLoss[0] + " silver " + silverLoss);
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = LANDED + 5)
  public void aHolyWaterBoltScaldsAVampireAndShatters(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, HUNTER);
    final VampireEntity vampire = AmmoTargets.vampire(context, TARGET);
    context.runAtTick(FIRE_AT, () -> fire(context, hunter, AmmoItems.HOLY_WATER_BOLT));
    context.runAtTick(
        LANDED,
        () -> {
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(HolyWater.WEAKENED_SOURCE),
              "the holy water weakened the vampire");
          context.assertTrue(
              context
                  .getWorld()
                  .getEntitiesByClass(BoltEntity.class, context.getTestBox(), bolt -> true)
                  .isEmpty(),
              "the vial shattered");
          TestPlayers.finish(context, hunter);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = LANDED + 30)
  public void aGarlicBombBurstsIntoACloudThatWeakensAVampire(final TestContext context) {
    final ServerPlayerEntity hunter = TestPlayers.human(context, HUNTER);
    final VampireEntity vampire = AmmoTargets.vampire(context, TARGET);
    context.runAtTick(FIRE_AT, () -> fire(context, hunter, AmmoItems.GARLIC_BOMB));
    context.runAtTick(
        LANDED + 20,
        () -> {
          context.assertFalse(
              context
                  .getWorld()
                  .getEntitiesByClass(
                      AreaEffectCloudEntity.class, context.getTestBox().expand(4), cloud -> true)
                  .isEmpty(),
              "the bomb burst into a cloud");
          context.assertTrue(
              Weakened.sources(vampire).sources().containsKey(GarlicFumesEffect.WEAKENED_SOURCE),
              "the fumes weakened the vampire");
          TestPlayers.finish(context, hunter);
        });
  }

  private static void fire(
      final TestContext context, final ServerPlayerEntity hunter, final Item ammo) {
    final ItemStack crossbow = new ItemStack(Items.CROSSBOW);
    crossbow.set(
        DataComponentTypes.CHARGED_PROJECTILES,
        ChargedProjectilesComponent.of(new ItemStack(ammo)));
    hunter.setStackInHand(Hand.MAIN_HAND, crossbow);
    ((CrossbowItem) Items.CROSSBOW)
        .shootAll(context.getWorld(), hunter, Hand.MAIN_HAND, crossbow, BOLT_SPEED, 0.0F, null);
    context
        .getWorld()
        .getEntitiesByClass(
            PersistentProjectileEntity.class, context.getTestBox(), projectile -> true)
        .forEach(projectile -> projectile.setCritical(false));
  }
}
