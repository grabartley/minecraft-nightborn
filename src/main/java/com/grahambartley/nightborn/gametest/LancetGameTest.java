package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class LancetGameTest implements FabricGameTest {

  static final String ARENA = "nightborn:vampire_arena";

  private static <E extends MobEntity> E still(final E mob, final float health) {
    mob.setAiDisabled(true);
    mob.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(health);
    mob.setHealth(health);
    return mob;
  }

  private static void arm(final ServerPlayerEntity player, final int bottles) {
    player.getInventory().clear();
    player.setStackInHand(Hand.MAIN_HAND, new ItemStack(BloodItems.LANCET));
    if (bottles > 0) {
      player.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE, bottles));
    }
  }

  private static void bleed(
      final TestContext context, final ServerPlayerEntity player, final LivingEntity creature) {
    UseEntityCallback.EVENT
        .invoker()
        .interact(player, context.getWorld(), Hand.MAIN_HAND, creature, null);
  }

  private static int bottles(final ServerPlayerEntity player, final BloodQuality quality) {
    int count = 0;
    for (int slot = 0; slot < player.getInventory().size(); slot++) {
      final ItemStack stack = player.getInventory().getStack(slot);
      if (stack.isOf(BloodItems.BLOOD_BOTTLE) && BloodBottleItem.qualityOf(stack) == quality) {
        count += stack.getCount();
      }
    }
    return count;
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aHumanBottlesACowAndNeverKillsIt(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)), 10.0F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          arm(human, 4);
          bleed(context, human, cow);
          context.assertEquals(cow.getHealth(), 6.0F, "the lancet draws a bottle's worth");
          context.assertEquals(bottles(human, BloodQuality.ANIMAL), 1, "one bottle of cow blood");
          bleed(context, human, cow);
          bleed(context, human, cow);
          context.assertTrue(cow.isAlive(), "the cow lives");
          context.assertEquals(cow.getHealth(), 2.0F, "the last health is never taken");
          context.assertEquals(bottles(human, BloodQuality.ANIMAL), 2, "only blood it had");
          context.assertEquals(
              human.getMainHandStack().getDamage(), 2, "the lancet wears with each bottle");
          context.assertFalse(FeedingSessions.isFeeding(human), "a lancet is not a bite");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aHumanBottlesAVillagerInsteadOfTrading(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final VillagerEntity villager =
        still(context.spawnEntity(EntityType.VILLAGER, new BlockPos(3, 2, 4)), 20.0F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          arm(human, 1);
          bleed(context, human, villager);
          context.assertEquals(
              bottles(human, BloodQuality.VILLAGER), 1, "a bottle of villager blood");
          context.assertTrue(villager.getHealth() < 20.0F, "the villager was hurt");
          context.assertTrue(villager.getCustomer() == null, "no trade opened");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aSneakingHumanBleedsThemselfForPlayerBlood(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          arm(human, 1);
          human.setSneaking(true);
          human.getMainHandStack().use(context.getWorld(), human, Hand.MAIN_HAND);
          context.assertEquals(
              bottles(human, BloodQuality.PLAYER), 1, "a bottle of the human's own blood");
          context.assertTrue(human.getHealth() < human.getMaxHealth(), "it hurt");
          human.setHealth(4.0F);
          human.getInventory().insertStack(new ItemStack(Items.GLASS_BOTTLE));
          human.getMainHandStack().use(context.getWorld(), human, Hand.MAIN_HAND);
          context.assertEquals(human.getHealth(), 4.0F, "a weak human is not bled further");
          TestPlayers.finish(context, human);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void aLancetDrawsNothingFromAPlayerOrWithoutABottle(final TestContext context) {
    final ServerPlayerEntity human = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity other = TestPlayers.human(context, new BlockPos(3, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(2, 2, 2)), 10.0F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          arm(human, 1);
          bleed(context, human, other);
          context.assertEquals(other.getHealth(), other.getMaxHealth(), "another player is spared");
          arm(human, 0);
          bleed(context, human, cow);
          context.assertEquals(cow.getHealth(), 10.0F, "no bottle, no blood drawn");
          TestPlayers.finish(context, human, other);
        });
  }

  @GameTest(templateName = ARENA, tickLimit = TestPlayers.READY + 10)
  public void twoPlayersOnOneCowNeverMakeBlood(final TestContext context) {
    final ServerPlayerEntity one = TestPlayers.human(context, new BlockPos(2, 2, 4));
    final ServerPlayerEntity two = TestPlayers.vampire(context, new BlockPos(4, 2, 4));
    final CowEntity cow = still(context.spawnEntity(EntityType.COW, new BlockPos(3, 2, 4)), 13.0F);
    context.runAtTick(
        TestPlayers.READY,
        () -> {
          arm(one, 5);
          arm(two, 5);
          for (int turn = 0; turn < 4; turn++) {
            bleed(context, one, cow);
            bleed(context, two, cow);
          }
          final int drawn = bottles(one, BloodQuality.ANIMAL) + bottles(two, BloodQuality.ANIMAL);
          context.assertEquals(drawn, 3, "three bottles from thirteen health");
          context.assertTrue(cow.isAlive(), "the cow lives");
          context.assertTrue(
              drawn * 4.0F <= 13.0F - cow.getHealth(), "no bottle holds blood the cow never lost");
          TestPlayers.finish(context, one, two);
        });
  }
}
