package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vomit.Consumables;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class VomitGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "vomiting";
  static final int FINISHED = TestPlayers.READY + 45;

  @BeforeBatch(batchId = BATCH)
  public void pinMidnight(final ServerWorld world) {
    WorldClock.freeze(world, WorldClock.MIDNIGHT);
  }

  @AfterBatch(batchId = BATCH)
  public void release(final ServerWorld world) {
    WorldClock.release(world);
  }

  private static void swallow(
      final TestContext context, final ServerPlayerEntity player, final ItemStack stack) {
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          player.setStackInHand(Hand.MAIN_HAND, stack);
          player.setCurrentHand(Hand.MAIN_HAND);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void aVampireEatingBreadGainsNothingIsHurtAndRetches(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    BloodAttachment.set(vampire, 10);
    swallow(context, vampire, new ItemStack(Items.BREAD, 2));
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertEquals(vampire.getMainHandStack().getCount(), 1, "one bread used up");
          context.assertEquals(BloodAttachment.get(vampire), 10, "no blood from bread");
          context.assertTrue(
              vampire.getHealth() < vampire.getMaxHealth(), "hurt: " + Diagnostics.of(vampire));
          context.assertTrue(
              vampire.getRecentDamageSource() != null
                  && vampire.getRecentDamageSource().isOf(NightbornDamage.VOMIT),
              "hurt by vomiting: " + Diagnostics.of(vampire));
          context.assertTrue(vampire.hasStatusEffect(StatusEffects.NAUSEA), "nauseous");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void aPotionGivesAVampireNothingAndLeavesTheBottle(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    final ItemStack potion = new ItemStack(Items.POTION);
    potion.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.STRENGTH));
    swallow(context, vampire, potion);
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertFalse(vampire.hasStatusEffect(StatusEffects.STRENGTH), "no strength");
          context.assertTrue(vampire.getMainHandStack().isOf(Items.GLASS_BOTTLE), "the bottle");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void milkClearsNothingFromAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    vampire.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 2000));
    swallow(context, vampire, new ItemStack(Items.MILK_BUCKET));
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertTrue(vampire.hasStatusEffect(StatusEffects.SLOWNESS), "still slow");
          context.assertTrue(vampire.getMainHandStack().isOf(Items.BUCKET), "the bucket back");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void aBloodBottleStillFeedsAVampire(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    BloodAttachment.set(vampire, 5);
    swallow(context, vampire, BloodBottleItem.of(BloodQuality.PLAYER));
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertTrue(BloodAttachment.get(vampire) > 5, "blood restored");
          context.assertFalse(vampire.hasStatusEffect(StatusEffects.NAUSEA), "kept down");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void holyWaterIsNotVomitedButBurns(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    final ItemStack holy = new ItemStack(Items.POTION);
    holy.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(HolyWater.POTION));
    swallow(context, vampire, holy);
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertFalse(
              vampire.hasStatusEffect(StatusEffects.NAUSEA),
              "kept down: " + Diagnostics.of(vampire));
          context.assertTrue(
              vampire.getRecentDamageSource() == null
                  || !vampire.getRecentDamageSource().isOf(NightbornDamage.VOMIT),
              "not hurt by vomiting: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void aHumanEatsAsNormal(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(3, 1, 4));
    human.getHungerManager().setFoodLevel(10);
    swallow(context, human, new ItemStack(Items.BREAD));
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertTrue(human.getHungerManager().getFoodLevel() > 10, "fed");
          context.assertFalse(human.hasStatusEffect(StatusEffects.NAUSEA), "not sick");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = 140)
  public void vomitingAtLowHealthDownsAVampireButNeverKills(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(3, 1, 4));
    context.runAtTick(TestPlayers.READY - 1, () -> vampire.setHealth(2.0F));
    swallow(context, vampire, new ItemStack(Items.BREAD));
    context.runAtTick(
        FINISHED,
        () -> {
          context.assertTrue(vampire.isAlive(), "alive");
          context.assertTrue(Downed.state(vampire) != null, "downed: " + Diagnostics.of(vampire));
          context.assertTrue(
              vampire.getRecentDamageSource() != null
                  && vampire.getRecentDamageSource().isOf(NightbornDamage.VOMIT),
              "downed by vomiting: " + Diagnostics.of(vampire));
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
  public void theRejectTagTakesInSharedFoodAndPotionTags(final TestContext context) {
    context.assertTrue(
        new ItemStack(Items.BREAD).isIn(Consumables.VAMPIRE_REJECTS), "bread through c:foods");
    context.assertTrue(
        new ItemStack(Items.SPLASH_POTION).isIn(Consumables.VAMPIRE_REJECTS),
        "splash potion through c:potions");
    context.assertTrue(
        new ItemStack(Items.MILK_BUCKET).isIn(Consumables.VAMPIRE_REJECTS), "milk named");
    context.assertTrue(
        BloodBottleItem.of(BloodQuality.PLAYER).isIn(Consumables.VAMPIRE_CONSUMABLES),
        "blood bottle exempt");
    context.complete();
  }
}
