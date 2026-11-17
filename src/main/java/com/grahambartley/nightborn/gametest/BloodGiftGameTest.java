package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodGiftEffect;
import com.grahambartley.nightborn.blood.BloodGiftGrants;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.blood.PotionWard;
import com.grahambartley.nightborn.config.FeedingSettings;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Optional;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class BloodGiftGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";
  static final String BATCH = "blood-gift";
  static final Identifier HORSE = Identifier.of("nightborn", "horse");
  static final Identifier WITCH = Identifier.of("nightborn", "witch");

  @BeforeBatch(batchId = BATCH)
  public void neverResist(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(), ServerConfigService.current().with(FeedingSettings.RESIST_CHANCE, 0));
  }

  @AfterBatch(batchId = BATCH)
  public void restoreResist(final ServerWorld world) {
    FeedingSessions.clearAll();
    ServerConfigService.update(
        world.getServer(),
        ServerConfigService.current()
            .with(FeedingSettings.RESIST_CHANCE, FeedingSettings.RESIST_CHANCE.defaultValue()));
  }

  private static double speedBonus(final ServerPlayerEntity vampire) {
    return vampire.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
        / vampire.getAttributeBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 50)
  public void freshHorseBloodGivesFullSwiftness(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    final HorseEntity horse = context.spawnEntity(EntityType.HORSE, new BlockPos(3, 2, 4));
    horse.setAiDisabled(true);
    final double[] before = new double[1];
    context.runAtTick(TestPlayers.READY, () -> before[0] = speedBonus(vampire));
    context.runAtEveryTick(
        () -> {
          if (context.getTick() >= TestPlayers.READY
              && context.getTick() <= TestPlayers.READY + 30) {
            vampire.setSneaking(true);
            vampire.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            UseEntityCallback.EVENT
                .invoker()
                .interact(vampire, context.getWorld(), Hand.MAIN_HAND, horse, null);
          }
        });
    context.runAtTick(
        TestPlayers.READY + 35,
        () -> {
          context.assertTrue(
              vampire.hasStatusEffect(BloodGiftEffect.ENTRY), "the gift shows on screen");
          context.assertTrue(
              BloodGiftGrants.active(vampire).map(g -> g.gift().equals(HORSE)).orElse(false),
              "the horse's gift is active");
          context.assertTrue(
              Math.abs(speedBonus(vampire) / before[0] - 1.2) < 0.01,
              "fresh horse blood gives 20% more speed, got " + speedBonus(vampire) / before[0]);
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aBottleGivesTheGiftAtHalfStrengthAndReplacesTheLast(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          final double base = speedBonus(vampire);
          BloodGiftGrants.grant(vampire, WITCH, 1.0F);
          final ItemStack bottle = BloodBottleItem.of(BloodQuality.ANIMAL, Optional.of(HORSE));
          bottle.finishUsing(context.getWorld(), vampire);
          context.assertTrue(
              BloodGiftGrants.active(vampire).map(g -> g.gift().equals(HORSE)).orElse(false),
              "the new gift replaced the witch's");
          context.assertTrue(
              Math.abs(speedBonus(vampire) / base - 1.1) < 0.01,
              "a bottle gives half the speed, got " + speedBonus(vampire) / base);
          final StatusEffectInstance poison = new StatusEffectInstance(StatusEffects.POISON, 400);
          context.assertTrue(
              PotionWard.ward(vampire, poison).getDuration() == 400,
              "the witch's ward is gone with her gift");
          vampire.removeStatusEffect(BloodGiftEffect.ENTRY);
        });
    context.runAtTick(
        TestPlayers.READY + 3,
        () -> {
          context.assertTrue(BloodGiftGrants.active(vampire).isEmpty(), "the gift ended");
          context.assertTrue(
              Math.abs(speedBonus(vampire) - 1.0) < 0.01, "and took its speed with it");
          TestPlayers.finish(context, vampire);
        });
  }

  @GameTest(templateName = ARENA, batchId = BATCH, tickLimit = TestPlayers.READY + 10)
  public void aWitchsGiftHalvesAHarmfulPotion(final TestContext context) {
    final ServerPlayerEntity vampire = TestPlayers.vampire(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          BloodGiftGrants.grant(vampire, WITCH, 1.0F);
          context.assertTrue(
              PotionWard.ward(vampire, new StatusEffectInstance(StatusEffects.POISON, 400))
                      .getDuration()
                  == 200,
              "harmful effects last half as long");
          context.assertTrue(
              PotionWard.ward(vampire, new StatusEffectInstance(StatusEffects.SPEED, 400))
                      .getDuration()
                  == 400,
              "helpful ones are untouched");
          TestPlayers.finish(context, vampire);
        });
  }
}
